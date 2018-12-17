#!/usr/bin/env python
"""
Installer for the Rapid Inquiry Facility (RIF).

Prerequisites: Tomcat; either PostgreSQL or Microsoft SQL Server; R

"""

__author__ = "Martin McCallion"
__email__ = "m.mccallion@imperial.ac.uk"

import os
import sys
import argparse
import shutil
import subprocess
from collections import namedtuple
from pathlib import Path
from configparser import ConfigParser, ExtendedInterpolation
from distutils.util import strtobool
from contextlib import contextmanager

all_settings = {"development_mode": "Development mode?",
                "db_type": "Database type",
                "script_home": "Directory for SQL scripts",
                "tomcat_home": "Home directory for Tomcat",
                "war_files_location": "Directory containing the WAR files",
                }

# We have the default settings file in the current directory and the user's
# version in their home. We only user the [DEFAULT] section in each (for
# now).
default_parser = ConfigParser(allow_no_value=True,
                              interpolation=ExtendedInterpolation())
user_parser = ConfigParser(allow_no_value=True,
                           interpolation=ExtendedInterpolation())
default_config = default_parser["DEFAULT"]
user_config = user_parser["DEFAULT"]

@contextmanager
def logger(output):
    """Redirecting standard output."""
    stdout = sys.stdout
    sys.stdout = output
    try:
        yield
    finally:
        sys.stdout = stdout

def main():

    # This sends output to the specified file as well as stdout.
    with logger("install.log"):
        settings = get_settings()

        # prompt for go/no-go
        print("About to install with the following settings:"
              "\n\tDevelopment mode: {}"
              "\n\tDB: {} "
              "\n\tScripts directory: {} "
              "\n\tTomcat home directory: {}"
              "\n\tWAR files directory: {}"
                .format(bool(settings.dev_mode),
                      long_db_name(settings.db_type),
                      settings.script_root,
                      settings.cat_home,
                      settings.war_dir))
        if input("Continue? [No]: "):

            # Run SQL scripts
            if settings.db_type == "pg":
                db_script = settings.script_root / "Postgres" / "production" / \
                            "db_create.sql"
            else:
                # Assumes both that it's SQL Server, and that we're running on
                # Windows. Linux versions of SQLServer exist, but we'll deal
                # with them later if necessary.
                db_script = settings.script_root / "SQLserver" / "installation" / \
                            "rebuild_all.bat"

                print("About to run {}; switching to {}".format(
                    db_script, db_script.parent))

                result = subprocess.run([str(db_script)],
                                        cwd=db_script.parent,
                                        stdout=subprocess.PIPE,
                                        stderr=subprocess.PIPE)

            # Deploy WAR files
            for f in get_war_files(settings):
                shutil.copy(f, settings.cat_home / "webapps")

# enddef main()


def get_settings():
    """Prompt the user for the installation settings.

    Gets the current values from ~/.rif/rifInstall.ini, if that
    file exists. Writes back to the same file (replacing it) after the
    user has confirmed. If the file does not exist, we load the defaults from
    install.ini in the current directory.
    """

    # Create the RIF home directory and properties file if they don't exist
    home_dir = Path.home()
    rif_home = home_dir / ".rif"
    rif_home.mkdir(parents=True, exist_ok=True)
    user_props = rif_home / "rifInstall.ini"
    user_props.touch(exist_ok=True)
    default_props = Path.cwd() / "install.ini"

    default_parser.read(default_props)
    user_parser.read(user_props)

    # Check if we're in development mode
    reply = get_value_from_user("development_mode")
    dev_mode = strtobool(reply)

    # Database type and script root
    db_type = get_value_from_user("db_type")
    db_script_root = Path(get_value_from_user("script_home",
                                              is_path=True)).resolve()

    # Tomcat home: if it's not set we use the environment variable
    tomcat_home = get_value_from_user("tomcat_home", is_path=True)

    # In development we assume that this script is being run from installer/
    # under the project root. The root directory is thus one level up.
    if dev_mode:
        war_dir = Path.cwd().resolve().parent
    else:
        war_dir = get_value_from_user("war_files_location", is_path=True)

    # Update the user's config file
    # user_config["key"] = "reply"
    # user_parser
    props_file = open(user_props, "w")
    user_parser.write(props_file)

    # Using a named tuple for the return value for simplicity of creation and
    # clarity of naming.
    Settings = namedtuple("Settings", ["db_type", "script_root", "cat_home",
                               "war_dir", "dev_mode"])
    return Settings(db_type, db_script_root, tomcat_home, war_dir, dev_mode)


def get_value_from_user(key, is_path=False):
    """Gets a new value from the user, prompting with the current value
       from the config files if one exists.
       :param key: the setting being processed
       :param is_path: whether or not the setting is a path-like object
    """

    current_value = ""
    if key in user_config:
        current_value = user_config[key]
    elif key in default_config:
        current_value = default_config[key]
    reply = input("{} [{}] ".format(all_settings.get(key), current_value))
    if reply is None or reply.strip() == "":
        reply = current_value

    # Special handling for Tomcat's home directory
    if key == "tomcat_home":
        # The second test below is to catch no value being given by the user
        while reply is None or reply.strip() == "":
            tomcat_home_str = os.getenv("CATALINA_HOME")

            # Make sure we have a value.
            if tomcat_home_str is None or tomcat_home_str.strip() == "":
                print("CATALINA_HOME is not set in the environment and no value "
                      "given for {}.".format(all_settings.get("tomcat_home")))
            else:
                reply = tomcat_home_str

    if is_path:
        returned_reply = Path(reply.strip()).resolve()
    else:
        returned_reply = reply.strip()

    # Update the user's config value
    if key == "development_mode":
        # Just to make sure we get "True" or "False" in the file
        user_parser["DEFAULT"][key] = str(bool(reply))
    else:
        user_parser["DEFAULT"][key] = str(returned_reply)
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


def long_db_name(db):

    return "Microsoft SQL Server" if db.strip() == "ms" else "PostgreSQL"


def check_arguments():
    """Checks the command-line arguments, displaying the usage message if
    there is a problem, or if requested.
    """

    parser = argparse.ArgumentParser(description="Install the RIF")
    db_group = parser.add_mutually_exclusive_group()
    db_group.add_argument("--ms", help="Database is Microsoft SQL Server",
                          action="store_true")
    db_group.add_argument("--pg", help="Database is PostgreSQL",
                          action="store_true")
    parser.add_argument("--home",
                        help="The home directory for Tomcat. If not "
                             "specified, the environment variable "
                             "CATALINA_HOME will be used.")
    parser.add_argument("--script-root",
                        help="The directory containing the  scripts to build "
                             "the database. If not specified the current "
                             "directory will be used.",
                        dest="scripts")
    parser.add_argument("--warfiles-dir",
                        help="Location of the WAR files for the application. If"
                             " not specified the current directory will be "
                             "used.",
                        dest="wars")

    args = parser.parse_args()

    # Database
    # ========
    if not args.ms and not args.pg:
        print("One of --ms or --pg must be specified to set the database type")
        sys.exit(1)

    db_type = "ms" if args.ms else "pg"

    # Tomcat home
    # ===========
    # Use the home value if specified; if not we try CATALINA_HOME, and if
    # that's not set then we exit with an error.
    if args.home:
        cat_home = args.home
    else:
        cat_home = os.getenv("CATALINA_HOME")

    if cat_home is None or cat_home == '':
        print("CATALINA_HOME is not set in the environment and no value "
              "given for --home.")
        sys.exit(1)

    # Scripts
    # =======
    # Use the SQL script directory if given; otherwise assume it's the current
    # directory
    script_root = args.scripts
    if not script_root:
        script_root = "."

    # Warfiles
    # ========
    war_dir = args.wars
    if not war_dir:
        war_dir = "."

    # Using a named tuple for the return value for simplicity of creation and
    # clarity of naming.
    Args = namedtuple("Args", ["db_type", "script_root", "cat_home",
                               "war_dir"])
    return Args(db_type, str.strip(script_root), str.strip(cat_home),
                str.strip(war_dir))


# I got this from https://stackoverflow.com/a/24583265/1517620
# class Logger(object):
#     """Lumberjack class - duplicates sys.stdout to a log file and it's okay."""
#     #source: https://stackoverflow.com/q/616645
#
#     def __init__(self, filename="install.log", mode="ab", buff=0):
#         self.stdout = sys.stdout
#         self.file = open(filename, mode, buff)
#         sys.stdout = self
#
#     def __del__(self):
#         self.close()
#
#     def __enter__(self):
#         pass
#
#     def __exit__(self, *args):
#         self.close()
#
#     def write(self, message):
#         self.stdout.write(message)
#         self.file.write(message.encode("utf-8"))
#
#     def flush(self):
#         self.stdout.flush()
#         self.file.flush()
#         os.fsync(self.file.fileno())
#
#     def close(self):
#         if self.stdout != None:
#             sys.stdout = self.stdout
#             self.stdout = None
#
#         if self.file != None:
#             self.file.close()
#             self.file = None


if __name__ == "__main__":
    # print("Initialising. Arguments are {}".format(sys.argv))
    sys.exit(main())
