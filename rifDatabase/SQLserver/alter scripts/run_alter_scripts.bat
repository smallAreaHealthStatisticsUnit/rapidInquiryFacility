REM Slightly weirdly, this script is designed to be run from its parent
REM directory. That's because I wrote it as part of the install functions in
REM late 2018/early 2019, and the function that calls it from there was already
REM behaving that way.

sqlcmd -E -d sahsuland -b -m-1 -e -r1 -i v4_0_alter_10.sql -v pwd="%cd%"
sqlcmd -E -d sahsuland -b -m-1 -e -r1 -i v4_0_alter_11.sql -v pwd="%cd%"
