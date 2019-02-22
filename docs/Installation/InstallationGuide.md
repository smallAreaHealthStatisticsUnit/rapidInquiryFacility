---
layout: default
title: The RIF Installation Guide
---

See the [Quickstart Guide](QuickstartGuide) for the basic steps. This document has the details.


The older [Building and Installing the RIF](introduction/building-and-installation) document goes into painstaking detail about the various options. You shouldn't normally need that, but the information is there if you do.

For most installations you can just run the appropriate installer for your platform: `rifInstaller.exe` for Windows; `rifInstaller_mac` for Mac; or `rifInstaller_linux` for Linux.

Below we describe some details and differences in the various options for using the installers.

## Using the Python Script Directly

The recommended way to run the installer is via the binary executable for your platform, as described in the [Quickstart Guide](QuickstartGuide) . That is the best solution for most people. But if you are installing on a platform for which there is no binary, or you have special requirements, or are a developer, you can run the underlying Python script directly.

You will need to have Python 3.7+ installed. The script was originally developed against 3.7.1.

### Prerequisites

As well as Python, you will need to install the `pyinstaller` package:
```
pip install pyinstaller
```

You will also need the command-line Maven tool to build the RIF

### Clone the RIF repository

Clone the repository to the machine where you are installing:

```
git clone https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility.git
```

Build the RIF (so that you have the WAR files available for deploying):

```
cd rapidInquiryFacility
mvn clean install
```

Move to the install directory and run the script:

```
cd installer
python install.py
```

Or, on Unix-based systems including Mac:

```
cd installer
./install.py
```

## Answering the Prompts

Whichever way you run the installer, you will see a series of prompts. These tables have some details about them.

**Common:**

| Prompt                                   | Meaning                                                         | Responses                                             | Notes
| ---------------------------------------- | --------------------------------------------------------------- | ----------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------
| Continue?                                | Start the installation?                                         | y/n                                                   | Pressing Enter with no value will stop the process
| Development mode?                        | Is this installation running in a development environment       | y/n                                                   | Not shown when running the binary executable
| Database type (pg or ms                  | Which database platform?                                        | 'pg' for PostgreSQL, or 'ms' for Microsoft SQL Server |
| Directory for SQL scripts                | Where are the SQL scripts that are needed for the installation? | A directory path                                      | Not shown when running the binary executable
| Home directory for Tomcat                | The base directory used by Apache Tomcat                        | Leave blank if CATALINA_HOME is set                   | Defaults to the value of the CATALINA_HOME environment variable. Used to specify where the WAR files will be deployed to.
| Directory for files extracted by studies | The RIF extracts files to this directory in normal use          | A directory to which the user has write permissions   |

**When the database is Postgres:**

| Prompt                                       | Meaning                                             | Responses                                                                                             | Notes
| -------------------------------------------- | --------------------------------------------------- | ----------------------------------------------------------------------------------------------------- | -----------------------------------------------------------------------------------------------------
| Name of the new database                     | The name of the database that is to be created      | Leave blank for the default of sahsuland, or provide a name                                           |
| User name for the new database (and the RIF) |                                                     | The desired user name                                                                                 | This will be created in Postgres or SQL Server, and it is the name you will use to log in to the RIF.
| Password for the new user                    | New password                                        |                                                                                                       | This is repeated for confimation
| Password for the 'rif40' user                | New or existing password                            | If the user 'rif40' does not exist, it will be created. If it does exist, give the existing password. | The user 'rif40' will be created if it does not exist. This is repeated for confirmation.
| Password for the 'postgres' user             | Existing password                                   | The password for the 'postgres' user, which is the administrator.                                     | You will have created this when you installed Postgres
| Continue?                                    | Last chance to quit before the database scripts run | y/n                                                                                                   |

**When the database is SQL Server (note that these are shown after some SQL script output appears):**

| Prompt                                 | Meaning                                             | Responses           | Notes
| -------------------------------------- | --------------------------------------------------- | ------------------- | ---------------------------------------------------------------------------------
| Continue?                              | Last chance to quit before the database scripts run | y/n                 |
| New user [default peter]:              |                                                     | A username          |
| New user password [default Peter!@$~]: |                                                     | The user's password | You don't get a second prompt to confirm, but the password is shown in clear text
| Press any key to continue . . .        |                                                     |                     |
| Press any key to continue . . .        |                                                     |                     |

## Taxonomies

One of the features of the RIF is its Taxonomy Service. This service allows users to select diseases and conditions by various coding systems, such as the International Classification of Diseases (ICD). A default installation of  the RIF will have the complete set of ICD9 data, and a sample of the ICD10 data.

The relevant files are contained in the `taxonomies.war` file, and so are expanded by Tomcat into its `webapps/taxonomies/WEB-INF/classes` directory. We recommend that you don't change or add to the files in there, as the entire directory is replaced whenever a new version of `taxonomies.war` is deployed. Instead, updated versions of the files should be placed in Tomcat's `conf` directory.

For more details, including how to use the full set of ICD10 data, see the [Taxonomy Services Guide](../taxonomyServices/Taxonomy-Services). To add a custom taxonomy, see [How to Add a New Taxonomy](../taxonomyServices/adding-a-new-taxonomy).

