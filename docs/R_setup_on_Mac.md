# Setting Up the RIF's R Environment On Mac

(Including ODBC setup.)

This is specific to MacOS, but the general principles will apply to Linux and other Unixes too.

## Installation

### Installing R

I did the basic install using [Homebrew](https://brew.sh):

```
brew install r
```

but there are other ways, such as [downloading and installing a standard package](https://cran.r-project.org/bin/macosx/). Please follow the instructions there, or elsewhere as necessary.

### Installing ODBC

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

## Additional R Setup

You should be able to run R from the Mac terminal now. [Follow the main instructions](../rifWebApplication/Readme.md#433-r-packages) to ensure all the required R packages are setup.

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
export R_JRI=/usr/local/lib/R/3.5/site-library/rJava/jri
```

That slightly odd formulation in the first line is using the `R` command to get R to tell the shell what its home directory is. The second is the path to the directory containing `JRI.jar` and other files, including `libjri.jnilib`, which I believe is R's native library.

The actual paths on your system may vary, of course.

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
