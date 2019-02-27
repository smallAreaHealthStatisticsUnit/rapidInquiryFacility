---
layout: default
title: Setting Up the RIF's R Environment On Mac
---

(Including ODBC setup.)

This is specific to MacOS, but the general principles will apply to Linux and other Unixes too.

## Installation

I originally did the basic install using [Homebrew](https://brew.sh):

```
brew install r
```

but I don't recommend that now. The Homebrew version has issues with the INLA library (see [bug #27](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/27)), so  [downloading and installing a standard package](https://cran.r-project.org/bin/macosx/) is now the recommended approach on Mac. Please follow the instructions there.

## R Packages

[See Setting Up R Packages](R_setup_packages).

## Getting R to Run from Java

It's quite likely that if you try to run a study at this point you will get an error in the log like this:

```
UnsatisfiedLinkError: no jri in java.library.path
```

or very similar wording. We are calling a non-JVM application from the JVM, and it can't find its libraries. On Linux you can probably fix that by adding the correct value to `LD_LIBRARY_PATH`, but things are a little different on the Mac.

You'll need to set up several things to get everything to work, as follows.

### R Home

In your `.profile` or `.bashrc` file, you'll need:

```
export R_HOME=$(R RHOME)
export R_JRI=/Library/Frameworks/R.framework/Versions/Current/Resources/library/rJava/jri
```

That slightly odd formulation in the first line is using the `R` command to get R to tell the shell what its home directory is. The second is the path to the directory containing `JRI.jar` and other files, including `libjri.jnilib`, which I believe is R's native library.

The actual paths on your system may vary, of course.

I also added the following, which I cribbed from the `run` script used by the `examples` directory provided by JRI. I'm not totally sure it's all necessary:

```
R_SHARE_DIR=/Library/Frameworks/R.framework/Resources/share
export R_SHARE_DIR
R_INCLUDE_DIR=/Library/Frameworks/R.framework/Resources/include
export R_INCLUDE_DIR

JRI_LD_PATH=${R_HOME}/lib:${R_HOME}/bin:
if test -z "$LD_LIBRARY_PATH"; then
  LD_LIBRARY_PATH=$JRI_LD_PATH
else
  LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$JRI_LD_PATH
fi
export LD_LIBRARY_PATH
```

Lastly I had to make a symlink like this:

```
ln -f -s $(/usr/libexec/java_home)/jre/lib/server/libjvm.dylib /usr/local/lib
```

I got that from this [Stack Overflow answer](https://stackoverflow.com/a/35852152/1517620), which in turn
pointed to [this one](https://stackoverflow.com/a/31039105), which both shows and explains the symlink command above.

In short, you will probably get some problems to do with dynamic libraries, but they do have solutions.

### Tomcat Runtime Environment

We're running in Tomcat, so it has to know where to load libraries from. It provides an extension mechanism that lets you set things up without having to touch the provided scripts.

In Tomcat's `bin` directory, which is `/usr/local/Cellar/tomcat/9.0.6/libexec/bin` on my machine, create a file called `setenv.sh` if it does not already exist. It should contain the following:

```
#!/bin/sh

JAVA_OPTS="$JAVA_OPTS -Djava.library.path=${R_JRI}"
```

That ensures that `JRI.jar` will be on the runtime classpath.

You will also have to give `setenv.sh` execute permission:

```
chmod +x setenv.sh
```

from inside its directory.

## Running

With all that set up, start Tomcat and try running a study.

## Obsolete

### Installing ODBC

**Note:** This whole section is no longer necessary. Database access is now done using JDBC via the RJDBC package (see [issue #35](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/issues/35)). _Unless_ you’re using SQL Server, which is unlikely on a Mac. SQL Server DB access still needs ODBC at present.

I'm keeping the section here for now, for historical reference.

The RIF's R scripts connect to the database via ODBC. Originally Windows only, it turns out that this technology is now available for Unix-based platforms, including the Mac.

```
brew install psqlodbc
```

That's the PostgreSQL version. I also ran:

```
brew install unixodbc
```

I'm not sure that both were necessary.

### ODBC setup

You can set up the data sources with a couple of simple text files. [This page has the details](https://boriel.com/en/2013/01/16/postgresql-odbc-connection-from-mac-os-x/) of how to set them up.

Here's `odbcinst.ini`:

```
[PostgreSQL Unicode]
Description     = PostgreSQL ODBC driver (Unicode version)
Driver          = psqlodbcw.so
Debug           = 0
CommLog         = 1
UsageCount      = 1
```

And `.odbc.ini`:

```
[PostgreSQL35W]
Driver      = PostgreSQL Unicode
ServerName  = localhost
Port        = 5432
Database    = sahsuland
Username    = postgres
Password    = postgres
#Protocol    = 9.1.6
Debug       = 1
```

The second one makes the data source `PostgreSQL35W` available to the R runtime. And to others, if any were to try to use it: you can test the connection with the command:

```
isql -v PostgreSQL35W
```

