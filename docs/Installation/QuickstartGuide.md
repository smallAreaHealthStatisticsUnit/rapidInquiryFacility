---
layout: default
title: "Installing the RIF: Quickstart Guide"
---

This describes the simplest way to get and install a brand-new copy or the RIF. It is suitable for all simple installations. For more complex situations, or for upgrading an existing installation, see the full [Installation Guide](InstallationGuide).

## Prerequisites

Before installing the Rapid Inquiry Facility you must have the following installed:

1. A database. PostgreSQL and Microsoft SQL Server are supported.
2. Apache Tomcat.

Assuming those are installed and you have administrative rights on the machine on which you are installing, proceed as follows.

## Getting the Installer

Download a RIF installer from the [GitHub site](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility). We recommend always getting the latest version available from the [Releases tab](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/releases).

For Windows save `rifInstaller.exe`. For Mac it's `rifInstaller`.

## Running the Installer

You should run the installer from the command line. We don't recommend running it by double-clicking the icon in Windows Exporer or the Finder. It will run, but when it completes you won't see the output to know whether it completed successfully.

On Windows, select _Command Prompt_ from the Start menu. Right-click on the icon and select "Run as administrator". Answer "Yes" to the confirmation dialog.

On Mac launch the _Terminal_ app.

In either case use the `cd` command to move to the folder to which you saved the installer above. On Windows type:

```
rifInstaller.exe
```

On Mac you might have to make it executable first:

```
chmod +x rifInstaller
./rifInstaller
```

## Responding to the Prompts

Next you have to respond to a series of prompts. The first is to select the database type.
