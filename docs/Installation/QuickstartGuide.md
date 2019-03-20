---
layout: default
title: "Installing the RIF: Quickstart Guide"
---

This is the simplest way to get and install a brand-new copy of the Rapid Inquiry Facility (RIF). It is suitable for all simple installations. For more complex situations, or for upgrading an existing installation, see the full [Installation Guide](InstallationGuide).

## Prerequisites

Before installing the RIF you must have the following installed:

* A database. The RIF can use either [PostgreSQL](https://www.postgresql.org) or [Microsoft SQL Server](https://www.microsoft.com/en-us/sql-server/sql-server-2017) (only on Windows).
* [Apache Tomcat](https://tomcat.apache.org).
* [The R runtime environment](https://www.r-project.org). Getting this set up can be complex, so we provide guides for [Windows](../Installation/R_setup_on_Windows) and [Mac](../Installation/R_setup_on_Mac).

You must also have administrative rights on the machine on which you are installing.

## Setting the Path

The installation process creates the database schemas used by the RIF, by running a series of SQL scripts. To do so it needs to be able to find the appropriate commands: `psql` for PostgreSQL, and `SQLCMD.EXE` for SQL Server.

To that end you must add the folder containing the appropriate one of those commands to the `PATH` environment variable. If you are unfamiliar with setting environment variables, [you can find instructions here](https://www.computerhope.com/issues/ch000549.htm) for how to do it in various versions of Windows.

The relevant value to add to the path will be something like:

```
C:\Program Files\Microsoft SQL Server\140\Tools\Binn
```

for SQL Server on Windows, or:

```
C:\Program Files\PostgreSQL\9.5\bin
```

for Postgres on Windows, or:

```
/usr/local/bin
```

(which example will almost certainly be on your `PATH` already) for Postgres on other platforms. Exact values will depend on the version of the database system and the specific details of your system.

On Unix-based systems, including Mac and Linux, you can set it in the user's `.profile` or `.bashrc` file, or similar.

## Getting the Installer

Download a RIF installer from the [GitHub site](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility). We recommend always getting the latest version available from the [Releases tab](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/releases).

For Windows, save `rifInstaller.exe` to the machine you are installing on. For Mac you'll want `rifInstaller_mac`. For Linux it's `rifInstaller_linux`.

## Warning

**Running the installer as described below will destroy any existing RIF database you have. Make sure to back up an existing version if you still need its data.**

## Running the Installer

Run the installer from the command line. We don't recommend running it by double-clicking the icon in Windows Explorer or the Finder. It will run, but when it finishes you won't see the output to know whether it was successful.

**Note: On Windows, elevated privileges are needed.**

On Windows, open  the Start Menu. Right-click on  _Command Prompt_ and choose **"Run as administrator"**. Answer "Yes" to the confirmation dialog.

On Mac, open the _Terminal_ app. On Linux open a terminal.

In any case use the `cd` command to move to the folder to which you saved the installer, above. Then on Windows type:

```
rifInstaller.exe
```
and press Enter.

On Mac and Linux you might have to make it executable first:

```
chmod +x rifInstaller
./rifInstaller
```

where `rifInstaller` is the appropriate one for the platform.

## Responding to the Prompts

Next you have to respond to a series of prompts. They should mostly be self explanatory. The default value, or current value if you have run the command before, appears in square brackets after the prompt text. For example:

```
Database type (pg or ms) [pg]
```

That shows that the  user has run the command before and that they chose "pg" (for PostgreSQL) as the type of database.

The installer stores values from earlier runs in a file called `rifInstall.ini` in the `.rif` subfolder of your home folder.

For detailed information on the prompt values and responses, see the full [Installation Guide](InstallationGuide#answering-the-prompts).

## Waiting

After you have answered the prompts the installer may run for several minutes. You will see a lot of output on the screen as it builds the various database schemas. A successful end shows this message:

```
******************************
*                            *
* Installation complete.     *
*                            *
******************************
```

If the database is Microsoft SQL Server, that is followed by:

```
************************************************************
*                                                          *
* Remember to create an ODBC datasource as per the         *
* installation instructions, before running the RIF.       *
*                                                          *
************************************************************
```

## Creating the ODBC Connection

As the completion message above suggests, installations on Windows with SQL Server need an ODBC connection. Use the _ODBC Data Source Administrator_ application to create a _System DSN_ called **SQLServer13**. It should use _SQL Server authentication_.

## Problems

If the installer ends with a message that indicates that there was a problem, you should examine the messages before that to work out what caused the problem, and [consult the Troubleshooting Guide](Troubleshooting).

## Taxonomies

A default RIF installation has the ICD9 and a sample of the ICD10 data. See the relevant  [section of the full  guide](InstallationGuide#taxonomies) for more details.

