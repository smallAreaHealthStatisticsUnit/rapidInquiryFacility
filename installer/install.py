#!/usr/bin/env python
"""
Installer for the Rapid Inquiry Facility (RIF).

Prerequisites: Tomcat; either PostgreSQL or Microsoft SQL Server; R

"""
__author__ = "Martin McCallion"
__email__ = "m.mccallion@imperial.ac.uk"

import os
import sys
from collections import namedtuple
import shutil
import argparse
import configparser


def main():

    # Check that we have all the values we need
    # db_type, script_root, cat_home, war_dir = check_arguments()

    args = check_arguments()
    print("About to install with the following settings:"
          "\n\tDB: {} "
          "\n\tScripts directory: {} "
          "\n\tTomcat directory: {}"
          "\n\tWAR files directory: {}"
          .format(long_db_name(args.db_type),
                  args.script_root,
                  args.cat_home,
                  args.war_dir))

    #
    # prompt for go/no-go
    #

    # Run SQL scripts...


    # Deploy WAR files
    war_files = ["rifServices.war", "taxonomies.war", "statistics.war",
                 "RIF40.war"]
    for f in war_files:
        pass
        # shutil.copy(f, cat_home + "/webapps")

# enddef main()


def long_db_name(db):

    return "Microsoft SQL Server" if db == "ms" else "PostgreSQL"


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

if __name__ == "__main__":
    # print("Initialising. Arguments are {}".format(sys.argv))
    sys.exit(main())
