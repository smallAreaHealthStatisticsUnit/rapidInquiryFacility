#!/usr/bin/env python
"""
Installer for the Rapid Inquiry Facility (RIF).

Prerequisites: Tomcat; either PostgreSQL or Microsoft SQL Server; R

"""

__author__ = "Martin McCallion"
__email__ = "m.mccallion@imperial.ac.uk"

import os
import shutil
import subprocess
import sys
from collections import namedtuple
from configparser import ConfigParser, ExtendedInterpolation
from distutils.util import strtobool
from pathlib import Path

WAR_FILES_LOCATION = "war_files_location"
TOMCAT_HOME = "tomcat_home"
SCRIPT_HOME = "script_home"
DB_TYPE = "db_type"
DEVELOPMENT_MODE = "development_mode"
EXTRACT_DIRECTORY = "extract_directory"

all_settings = {DEVELOPMENT_MODE: "Development mode?",
                DB_TYPE: "Database type",
                SCRIPT_HOME: "Directory for SQL scripts",
                TOMCAT_HOME: "Home directory for Tomcat",
                WAR_FILES_LOCATION: "Directory containing the WAR files",
                EXTRACT_DIRECTORY: "Directory for files extracted by studies",
                }

# We have the default settings file in the current directory and the user's
# version in their home. We use the [MAIN] section in each for most of the
# settings, and the database-specific ones and the [NOPROMPT ones
default_parser = ConfigParser(allow_no_value=True,
                              interpolation=ExtendedInterpolation())
default_parser.optionxform = str # Preserve case in keys
user_parser = ConfigParser(allow_no_value=True,
                           interpolation=ExtendedInterpolation())
user_parser.optionxform = str # Preserve case in keys
default_config = ConfigParser()
user_config = ConfigParser()

running_bundled = False
user_props = Path()


def main():

    initialise_config()

    # This sends output to the specified file as well as stdout.
    with Logger("install.log"):

        settings = get_settings()

        # prompt for go/no-go
        print("About to install with the following settings:"
              "\n\tDevelopment mode: {}"
              "\n\tDB: {} "
              "\n\tScripts directory: {} "
              "\n\tTomcat home directory: {}"
              "\n\tWAR files directory: {}"
              "\n\tExtract directory: {}"
                .format(bool(settings.dev_mode),
                        long_db_name(settings.db_type),
                        settings.script_root,
                        settings.cat_home,
                        settings.war_dir,
                        settings.extract_dir))
        if input("Continue? [No]: "):

            # Run SQL scripts
            if settings.db_type == "pg":
                db_scripts = [(settings.script_root / "Postgres" / "production"
                             / "db_create.sh")]
            else:
                # Assumes both that it's SQL Server, and that we're
                # running on Windows. Linux versions of SQLServer
                # exist, but we'll deal with them later if necessary.

                # Some files need to have special permissions granted,
                # or the database loading steps fail
                set_special_db_permissions()
                db_scripts = get_windows_scripts(settings)

            db_created = False
            for s in db_scripts:
                print("About to run {}; switching to {}".format(
                    s, s.parent))
                result = subprocess.run([str(s)], cwd=s.parent)

                if result.returncode is not None and result.returncode != 0:
                    print("Something went wrong when running the {} script"
                          "\n\tErrors: {}".format(s, result.stderr))
                    print("Database creation not complete")
                    break
                db_created = True

            if db_created:
                # Deploy WAR files
                for f in get_war_files(settings):
                    shutil.copy(f, settings.cat_home / "webapps")

                # Generate RIF startup properties file
                create_properties_file(settings)

                msg = "Installation complete."
                if settings.db_type == "ms":
                    msg += (" Remember to create an ODBC datasource as per "
                            "the installation instructions, before running "
                            "the RIF.")
                print(msg)


def initialise_config():
    """Set up the initial details, home directories, files, etc. """

    global running_bundled
    global base_path
    global user_props
    global default_config
    global user_config

    # Check for where we're running: if "frozen" is true, we're in a
    # PyInstaller bundle; otherwise just a script.
    running_bundled = getattr(sys, "frozen", False)
    if running_bundled:
        try:
            # PyInstaller bundles create a temp folder when run,
            # and stores its path in _MEIPASS. This feels like a hack,
            # but it is the documented way to get at the bundled files.
            base_path = Path(sys._MEIPASS)
        except Exception:
            base_path = Path.cwd()
    else:
        base_path = Path.cwd()

    # Create the RIF home directory and properties file if they don't exist,
    #  and load them if they do.

    print("Base path is {}".format(base_path))

    home_dir = Path.home()
    rif_home = home_dir / ".rif"
    rif_home.mkdir(parents=True, exist_ok=True)
    user_props = rif_home / "rifInstall.ini"
    user_props.touch(exist_ok=True)
    default_props = base_path / "install.ini"
    default_parser.read(default_props)
    user_parser.read(user_props)
    default_config = default_parser["MAIN"]
    user_config = user_parser["MAIN"]


def get_settings():
    """Prompt the user for the installation settings.

    Gets the current values from ~/.rif/rifInstall.ini, if that
    file exists. Writes back to the same file (replacing it) after the
    user has confirmed. If the file does not exist, we load the defaults from
    install.ini in the current directory.
    """

    # Check if we're in development mode (but only if we're running
    # from scripts)
    if running_bundled:
        dev_mode = False
    else:
        reply = get_value_from_user(DEVELOPMENT_MODE)
        dev_mode = strtobool(reply)

    # Database type and script root
    db_type = get_value_from_user(DB_TYPE)
    if running_bundled:
        db_script_root = base_path
    else:
        db_script_root = Path(get_value_from_user(SCRIPT_HOME,
                                                  is_path=True)).resolve()

    # Tomcat home: if it's not set we use the environment variable
    tomcat_home = get_value_from_user(TOMCAT_HOME, is_path=True)

    # In development we assume that this script is being run from installer/
    # under the project root. The root directory is thus one level up.
    if dev_mode:
        war_dir = Path.cwd().resolve().parent
    else:
        war_dir = base_path / "warfiles"

    extract_dir = get_value_from_user(EXTRACT_DIRECTORY, is_path=True)

    # Update the user's config file
    # user_config["key"] = "reply"
    # user_parser
    props_file = open(user_props, "w")
    user_parser.write(props_file)

    # Using a named tuple for the return value for simplicity of creation and
    # clarity of naming.
    Settings = namedtuple("Settings", "db_type, script_root, cat_home, "
                                      "war_dir, dev_mode, extract_dir")
    return Settings(db_type, db_script_root, tomcat_home, war_dir, dev_mode,
                    extract_dir)


def get_value_from_user(key, is_path=False):
    """Get a new value from the user, prompting with the current value
       from the config files if one exists.
       :param key: the setting being processed
       :param is_path: whether or not the setting is a path-like object
    """

    current_value = ""
    if user_config is not None and key in user_config:
        current_value = user_config[key]
    elif key in default_config:
        current_value = default_config[key]
    reply = input("{} [{}] ".format(all_settings.get(key), current_value))
    if reply is None or reply.strip() == "":
        reply = current_value

    # Special handling for Tomcat's home directory
    if key == TOMCAT_HOME:
        # The second test below is to catch no value being given by the user
        while reply is None or reply.strip() == "":
            tomcat_home_str = os.getenv("CATALINA_HOME")

            # Make sure we have a value.
            if tomcat_home_str is None or tomcat_home_str.strip() == "":
                print("CATALINA_HOME is not set in the environment and no "
                      "value given for {}."
                      .format(all_settings.get(TOMCAT_HOME)))
            else:
                reply = tomcat_home_str

    if is_path:
        returned_reply = Path(reply.strip()).resolve()
    else:
        returned_reply = reply.strip()

    # Update the user's config value
    if key == DEVELOPMENT_MODE:
        # Just to make sure we get "True" or "False" in the file
        user_parser["MAIN"][key] = str(bool(reply))
    else:
        user_parser["MAIN"][key] = str(returned_reply)
    return returned_reply


def get_war_files(settings):
    """Return a list of the WAR files to deploy"""

    if settings.dev_mode:
        war_files = [
            settings.war_dir / "rifServices" / "target" / "rifServices.war",
            settings.war_dir / "taxonomyServices" / "target" /
            "taxonomies.war",
            settings.war_dir / "statsService" / "target" / "statistics.war",
            settings.war_dir / "rifWebApplication" / "target" / "RIF40.war"
        ]
    else:
        # If not development, just copy the files from the specified
        # directory
        war_files = [settings.war_dir / "rifServices.war",
                     settings.war_dir / "taxonomies.war",
                     settings.war_dir / "statistics.war",
                     settings.war_dir / "RIF40.war"]
    return war_files


def create_properties_file(settings):
    """Create the RIF startup properties file."""

    props_file = Path(settings.cat_home / "conf" /
                      "RIFServiceStartupProperties.properties")

    # Get the settings from the appropriate sections of the ini file.
    short_db = short_db_name(settings.db_type)
    db_config = default_parser[short_db]

    if short_db not in user_parser:
        user_parser.add_section(short_db)
    db_config_user = user_parser[short_db]

    with props_file.open("w") as output_properties_file:
        for key in db_config:
            # Users can override by editing their user config file
            if key in db_config_user:
                value = db_config_user[key]
            else:
                value = db_config[key]

            output_properties_file.writelines(
                "database.{} = {}\n".format(key, value))

        output_properties_file.writelines(
            "extractDirectory = {}\n".format(str(settings.extract_dir)))

        if "NOPROMPT" in default_parser:
            unprompted_config = default_parser["NOPROMPT"]
            for key in unprompted_config:
                output_properties_file.writelines(
                    "{} = {}\n".format(key, unprompted_config[key]))


def long_db_name(db):

    return "Microsoft SQL Server" if db.strip() == "ms" else "PostgreSQL"


def short_db_name(db):

    return "MSSQL" if db.strip() == "ms" else "POSTGRES"


def set_special_db_permissions():
    """Set special permissions that are needed for the Windows scripts to run.

       Several scripts are used by the BULK LOAD SQL command, and the early
       versions of this script failed because of not having permission to
       read those scripts. The complexity is that the permission is needed
       by the user that runs the SQL Server service, not the current user.
       As a precaution we grant all permissions, and as we can't easily know
       the user in question, we grant it to the special "Everyone" account.

       Lastly one of the scripts creates a backup of the database, and the
       same service user needs write access to the directory where that is
       created.
    """
    backup_path = base_path / "SQLserver" / "production"
    geo_path = base_path / "GeospatialData" / "tileMaker"
    data_loader_path = base_path / "DataLoaderData" / "SAHSULAND"
    files_to_permit = [f for f in geo_path.glob("mssql_*") if f.is_file()]
    files_to_permit.extend(f for f in data_loader_path.iterdir() if f.is_file())
    files_to_permit.append(backup_path)
    for f in files_to_permit:
        print("Granting Windows permissions for {}".format(f))
        set_windows_permissions(str(f))


def get_windows_scripts(settings):
    """Get the list of SQL scripts to run on the Windows platform."""

    win_root = settings.script_root / "SQLserver"
    scripts = [win_root / "installation" / "rebuild_all.bat"]
    alter_dir = win_root / "alter scripts"
    scripts.extend([f for f in alter_dir.iterdir() if f.is_file()])
    return scripts


def set_windows_permissions(file_name):
    """Grant all access for Everyone on the specified file.

       Code copied from Stack Overflow:
       https://stackoverflow.com/a/43244697/1517620
    """
    # These imports don't apply on Mac or Linux, but PyInstaller doesn't
    # complain.
    import ntsecuritycon
    import win32security

    entries = [{'AccessMode': win32security.GRANT_ACCESS,
                'AccessPermissions': 0,
                'Inheritance': win32security.CONTAINER_INHERIT_ACE |
                               win32security.OBJECT_INHERIT_ACE,
                'Trustee': {'TrusteeType': win32security.TRUSTEE_IS_USER,
                            'TrusteeForm': win32security.TRUSTEE_IS_NAME,
                            'Identifier': ''}}]

    entries[0]['AccessPermissions'] = ntsecuritycon.GENERIC_ALL
    entries[0]['Trustee']['Identifier'] = "Everyone"

    sd = win32security.GetNamedSecurityInfo(file_name, win32security.SE_FILE_OBJECT,
            win32security.DACL_SECURITY_INFORMATION)
    dacl = sd.GetSecurityDescriptorDacl()
    dacl.SetEntriesInAcl(entries)
    win32security.SetNamedSecurityInfo(file_name, win32security.SE_FILE_OBJECT,
        win32security.DACL_SECURITY_INFORMATION |
        win32security.UNPROTECTED_DACL_SECURITY_INFORMATION,
        None, None, dacl, None)


class Logger(object):
    """Lumberjack class - duplicates sys.stdout to a log file and it's okay."""
    # I got this from https://stackoverflow.com/a/24583265/1517620

    def __init__(self, filename="install.log", mode="ab", buff=0):
        self.stdout = sys.stdout
        self.file = open(filename, mode, buff)
        sys.stdout = self

    def __del__(self):
        self.close()

    def __enter__(self):
        pass

    def __exit__(self, *args):
        self.close()

    def write(self, message):
        self.stdout.write(message)
        self.file.write(message.encode("utf-8"))

    def flush(self):
        self.stdout.flush()
        self.file.flush()
        os.fsync(self.file.fileno())

    def close(self):
        if self.stdout != None:
            sys.stdout = self.stdout
            self.stdout = None

        if self.file != None:
            self.file.close()
            self.file = None


if __name__ == "__main__":
    sys.exit(main())
