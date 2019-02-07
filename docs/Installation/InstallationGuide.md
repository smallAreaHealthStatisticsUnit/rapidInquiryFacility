---
layout: default
title: The RIF Installation Guide
---

See the [Quickstart Guide](QuickstartGuide) for the basic steps. This document has the details.


The older [Building and Installing the RIF](/introduction/building-and-installation) document goes into painstaking detail about the various options. You shouldn't normally need that, but the information is there if you do.

For most installations you can just run the appropriate installer for your platform: `rifInstaller.exe` for Windows; `rifInstaller` for Mac; or `rifInstaller_linux` for Linux.

Below we describe some details and differences in the various options for using the installers.

## Using the Python Script Directly

The recommended way to run the installer is via the binary executable for your platform, as described in the [Quickstart Guide](QuickstartGuide) . That is the best solution for most people. But if you are installing on a platform for which we don't provide a binary, or have special requirements, or are a developer, you can run the underlying Python script directly.

You will need to have Python 3.7+ installed. The script was originally developed against 3.7.1. Other 3.x versions may work, but they have not been tested.

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




### Prebuilt WARs and scripts

Download the database dumps for your platform from...

Download the three `WAR` files from...

### Directly from GitHub

This assumes you have the following command-line tools installed: `git`, Maven (the `mvn` command).

Clone the repository:

```
git clone https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility.git
```

Build:

```
mvn clean install
```



- Database
	- PostgreSQL
	- SQL Server
- Tomcat
	- Deploying the WARs
- Configuration

