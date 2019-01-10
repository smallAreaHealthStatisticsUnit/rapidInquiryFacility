#!/usr/bin/env python
"""
Installer for the Rapid Inquiry Facility (RIF).

Prerequisites: Tomcat; either PostgreSQL or Microsoft SQL Server; R

"""


__author__ = "Martin McCallion"
__email__ = "m.mccallion@imperial.ac.uk"

import argparse
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
default_parser.add_section("MAIN")
default_config = default_parser["MAIN"]
user_parser.add_section("MAIN")
user_config = user_parser["MAIN"]
running_bundled = False
base_path = ""

def main():

    global running_bundled
    global base_path

    # This sends output to the specified file as well as stdout.
    with Logger("install.log"):

        # Check for where we're running
        if getattr( sys, 'frozen', False ) :
            running_bundled = True
            try:
                # PyInstaller bundles create a temp folder when run,
                # and store its path in _MEIPASS. This feels like a hack,
                # but it is the documented way to get at the bundled files.
                base_path = sys._MEIPASS
            except Exception:
                base_path = os.path.abspath(".")

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
                db_script = (settings.script_root / "Postgres" / "production"
                             / "db_create.sql")
            else:
                # Assumes both that it's SQL Server, and that we're
                # running on Windows. Linux versions of SQLServer
                # exist, but we'll deal with them later if necessary.
                db_script = (settings.script_root / "SQLserver" /
                             "installation" / "rebuild_all.bat")

                print("About to run {}; switching to {}".format(
                    db_script, db_script.parent))

            result = subprocess.run([str(db_script)],
                                    cwd=db_script.parent)

            if result.returncode is not None and result.returncode != 0:
                print("Something went wrong with creating the "
                      "database. \n\tErrors: {}".format(result.stderr))
                print("Database not created")
            else:
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

def get_settings():
    """Prompt the user for the installation settings.

    Gets the current values from ~/.rif/rifInstall.ini, if that
    file exists. Writes back to the same file (replacing it) after the
    user has confirmed. If the file does not exist, we load the defaults from
    install.ini in the current directory.
    """
    global running_bundled

    # Create the RIF home directory and properties file if they don't exist
    home_dir = Path.home()
    rif_home = home_dir / ".rif"
    rif_home.mkdir(parents=True, exist_ok=True)
    user_props = rif_home / "rifInstall.ini"
    user_props.touch(exist_ok=True)
    if running_bundled:
        default_props = Path(base_path) / "install.ini"
    else:
        default_props = Path.cwd() / "install.ini"

    default_parser.read(default_props)
    user_parser.read(user_props)

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
        db_script_root = Path(base_path)
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
        war_dir = Path(base_path) / "warfiles"

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
    """Gets a new value from the user, prompting with the current value
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
    """Creates the RIF startup properties file."""

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


# def check_arguments():
#     """Checks the command-line arguments, displaying the usage message if
#     there is a problem, or if requested.
#     """
#
#     parser = argparse.ArgumentParser(description="Install the RIF")
#     db_group = parser.add_mutually_exclusive_group()
#     db_group.add_argument("--ms", help="Database is Microsoft SQL Server",
#                           action="store_true")
#     db_group.add_argument("--pg", help="Database is PostgreSQL",
#                           action="store_true")
#     parser.add_argument("--home",
#                         help="The home directory for Tomcat. If not "
#                              "specified, the environment variable "
#                              "CATALINA_HOME will be used.")
#     parser.add_argument("--script-root",
#                         help="The directory containing the  scripts to build "
#                              "the database. If not specified the current "
#                              "directory will be used.",
#                         dest="scripts")
#     parser.add_argument("--warfiles-dir",
#                         help="Location of the WAR files for the application. If"
#                              " not specified the current directory will be "
#                              "used.",
#                         dest="wars")
#
#     args = parser.parse_args()
#
#     # Database
#     # ========
#     if not args.ms and not args.pg:
#         print("One of --ms or --pg must be specified to set the database type")
#         sys.exit(1)
#
#     db_type = "ms" if args.ms else "pg"
#
#     # Tomcat home
#     # ===========
#     # Use the home value if specified; if not we try CATALINA_HOME, and if
#     # that's not set then we exit with an error.
#     if args.home:
#         cat_home = args.home
#     else:
#         cat_home = os.getenv("CATALINA_HOME")
#
#     if cat_home is None or cat_home == '':
#         print("CATALINA_HOME is not set in the environment and no value "
#               "given for --home.")
#         sys.exit(1)
#
#     # Scripts
#     # =======
#     # Use the SQL script directory if given; otherwise assume it's the current
#     # directory
#     script_root = args.scripts
#     if not script_root:
#         script_root = "."
#
#     # Warfiles
#     # ========
#     war_dir = args.wars
#     if not war_dir:
#         war_dir = "."
#
#     # Using a named tuple for the return value for simplicity of creation and
#     # clarity of naming.
#     Args = namedtuple("Args", [DB_TYPE, "script_root", "cat_home",
#                                "war_dir"])
#     return Args(db_type, str.strip(script_root), str.strip(cat_home),
#                 str.strip(war_dir))


# I got this from https://stackoverflow.com/a/24583265/1517620
class Logger(object):
    """Lumberjack class - duplicates sys.stdout to a log file and it's okay."""
    #source: https://stackoverflow.com/q/616645

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
    # print("Initialising. Arguments are {}".format(sys.argv))
    sys.exit(main())
