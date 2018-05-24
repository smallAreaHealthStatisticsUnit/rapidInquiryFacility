# Platform Independence for Building and Installing the RIF

These are some notes I made in March 2018, on getting the RIF installed and running on my Mac. The changes I had to make should at least act as a guide to installing it on Linux or other Unixes in the future.

Note that this is still very much a work in progress.

## Tomcat and Postgres

Installing these is straightforward. You can just download and run the relevant installers. I used [Homebrew](https://brew.sh), since I have that installed:

```
brew install tomcat
brew install postgresql
brew install postgis
```

For Linux there will be similar commands using `apt-get` or `yum`, depending on your distro.

## Building the WAR Files

Rather than trying to emulate the Windows `java_build.bat` script, I just did this manually, with the relevant Maven commands:

```
mvn clean install -Dmaven.test.skip=true
```

in the root gives us `rifServices.war` and `taxonomyservices.war`. For the webapp you currently have to run the same command in the `rifWebApplication` directory, and it doesn't create a complete WAR file. But more on that later.

I installed the two WAR files using the Tomcat Manager application. I had to increase the maximum allowed size of WAR file in Tomcat, as `rifServices.war` is bigger than the default maximum, and so wouldn't deploy. This means editing `web.xml` in the manager app. I doubled the maximum from 50 to 100MB.

For the webapp I emulated what I have on Windows by creating a `RIF40` directory in Tomcat's `webapps` directory, and copying into it the contents of the generated `WEB-INF` directory.

That is unsatisfactory, but works for now.

## Configuring the Database

The config file `postgresql.conf` was in `/usr/local/var/postgres/`. I ran:

```
initdb /usr/local/var/postgres/ -E utf8
```

as recommended [for example, here](https://mjanja.ch/2016/04/using-homebrews-postgresql-mac-os-x/). That reported an error, saying the directory already existed and was not empty. I'm not quite sure with the Homebrew run had created it or if I had previously run the above command and forgotten, but the database cluster was installed in that directory. 

The next step was to create a script

## Database Installation Script

I modelled the Bash script on the existing Windows batch file, `rif40_database_install.bat`. That script is at `rifDatabase/Postgres/production/rif40_database_install.bat`. The new one is `rifDatabase/Postgres/psql_scripts/rif40_database_install.sh`. I have not put it in the `production` subdirectory because it is not yet production quality.

The major limitations at the moment are that it has several hardcoded values and it doesn't prompt the user for anything (except a single go/no-go).

But it sets the necessary variables and runs `db_create.sql`, which works. So at least it is a viable proof of concept.

## Tweaks to Get the Database to Create

Note first that the version of `db_create.sql` is _not_ the one in `master` on GitHub. Instead it is the "August 2017" one that Peter gave me to run on my PC (`rifDemo/August 2017/Postgres/production/rif40_database_install.sh`). There are whole sections in that version that are not in the GitHub one so presumably that version needs to be merged into `master` at some point.

- Version check: my version of PostgreSQL is 10.3, and the version checking in the script used string parsing which broke when the major level became greater than 9. I changed it to use a numeric version.
- I had to make some tweaks to the user `martin`, which I had created. It had also created a role of the same name, and that role had no password, which caused problems. Presumably I missed something out compared to running the Windows script, as I didn't have that problem there.
- The SQL script `sahsuland.sql` is called as part of the run, and it ended with some errors regarding the role `peter` not existing. I ignored those for now.

## Tweaks to Get the RIF to Run

I then had to manually revoke all elevated privileges for my user `martin`. I had created it as superuser, and that isn't allowed for RIF users.

Manually created a schema called `martin` in the `sahsuland` database. Again, I must have missed something out in running the scripts.

Had to run the `v4_0_alter-9.sql` script, because of missing columns. But that was true on Windows, too.









