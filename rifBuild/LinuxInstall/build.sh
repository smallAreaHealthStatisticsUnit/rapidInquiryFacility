#! /bin/sh
#
# SAHSU RIF software stack build script for Redhat Enterprise Linux 6.6
# This deliberately does not use the Fredora Extra Packages for Enterprise Linux (EPEL) repoository so the system is plain vanilla Redhat
# and in particular there is no possibility of graphical program DLL hell.
#
# EL6 yum requirements: full development chain (gcc, make, cmake etc)
#
# readline
# libxslt 
# libX11 libXt 
# lapack
# texinfo 
# sqlite
#
# Eventually I got bored and installed all development libraries..
#
# yum install "*-devel"
#
MODULE=`basename $0`
LOG=$MODULE.log
cmd_status=0
#
# Set to 1 to force rebuild
#
FORCE_REBUILD=0
#
cmd() {
	MPWD=`pwd`
	DIR=$1
	CMD=$2
	shift; shift
	ARGS=$*
	cd $DIR
	cmd_status=$?
	if [ $cmd_status != "0" ]; then
		echo "$MODULE: Error! cd $DIR failed with status: $cmd_status"  | tee -a $LOG
		exit 1
	fi
	echo "UNIX[$DIR]> $CMD $ARGS" | tee -a $LOG
	$CMD $ARGS 2>&1  | tee -a $LOG
	cmd_status=${PIPESTATUS[0]}
	if [ $cmd_status != "0" ]; then
		echo "$MODULE: Error! $CMD failed with status: $cmd_status" | tee -a $LOG
		exit 2
	else
		echo "$MODULE: $CMD OK: $cmd_status"
	fi
	cd $MPWD
	cmd_status=$?
	if [ $cmd_status != "0" ]; then
		echo "$MODULE: Error! cd $MPWD failed with status: $cmd_status" | tee -a $LOG
		exit 3
	fi
}
#
# RIF specific bash shell setup. Do not use csh/tcsh/zsh etc
#	
rm -f $LOG
touch $LOG
if [ ! -f /etc/profile.d/rif.sh -o $FORCE_REBUILD = 1 ]; then
	cmd etc/profile.d cp rif.sh /etc/profile.d
elif [ etc/profile.d/rif.sh -nt /etc/profile.d/rif.sh ]; then
	cmd etc/profile.d cp rif.sh /etc/profile.d
fi
. ./etc/profile.d/rif.sh

#
# Extra ld.so.conf files
#
LD_SO_CONF_LIST="R.conf local.conf grass.conf postgres.conf"
for FILE in $LD_SO_CONF_LIST
do
	if [ ! -f /etc/ld.so.conf.d/$FILE -o $FORCE_REBUILD = 1 ]; then
		cmd etc/ld.so.conf.d cp $FILE /etc/ld.so.conf.d/.
	elif [ etc/ld.so.conf.d/$FILE -nt /etc/ld.so.conf.d/$FILE ]; then
		cmd etc/ld.so.conf.d cp $FILE /etc/ld.so.conf.d/.
		cmd . ldconfig
	fi
done
#
# Postgres
#
# Remove old redhat 8.4 version
# Initialise DB if required
# If you want to move the database create/edit /etc/sysconfig/pgsql/postgresql and set PGDATA
# You will need to remove PGDATA to force a reinit
# Version checking has bee removed from the startup script - beware if you upgrade the database (Postgres will grumble anyway)
#
RESTART_POSTGRES=0
if [ ! -f /etc/init.d/postgresql -o $FORCE_REBUILD = 1  -o -f /usr/bin/psql ]; then
	cmd postgresql-9.4.1 ./configure --with-tcl --with-perl --with-pam --with-ldap --with-openssl --with-libxml --with-libxslt --with-python 
	cmd postgresql-9.4.1 make clean 
	cmd postgresql-9.4.1 make world 
	cmd postgresql-9.4.1 make install-world
	if [ -f /usr/bin/psql ]; then
		cmd . yum -y remove postgresql
	fi
	cmd etc/init.d cp postgresql /etc/init.d/.
	if [ -f /etc/sysconfig/pgsql/postgresql ]; then
		. /etc/sysconfig/pgsql/postgresql 
		PGDATA=${PGDATA:="XXXX"}
		if [ $PGDATA != "XXXX" ]; then
			if [ ! -d $PGDATA ]; then
				cmd . mkdir -p $PGDATA
			fi
			cmd . chown -R postgres `dirname $PGDATA`
		fi
	fi
	if [ ! -f $PGDATA/postgresql.conf ]; then
		cmd . /etc/init.d/postgresql initdb
	fi
	cmd . chkconfig postgresql on
	RESTART_POSTGRES=1
	cmd . /etc/init.d/postgresql restart
fi
if [ -f /etc/sysconfig/pgsql/postgresql ]; then
	. /etc/sysconfig/pgsql/postgresql 
fi
if [ postgresql.conf -nt $PGDATA/postgresql.conf ]; then
	cmd . cp postgresql.conf $PGDATA/.
	RESTART_POSTGRES=1
fi
if [ etc/init.d/postgresql -nt /etc/init.d/postgresql ]; then
	cmd etc/init.d cp postgresql /etc/init.d/.
	RESTART_POSTGRES=1
fi
if [ $RESTART_POSTGRES = "1" ]; then
	cmd . /etc/init.d/postgresql restart
fi
#
# R
#
if [ ! -x /usr/local/bin/R -o $FORCE_REBUILD = 1 ]; then
	cmd R-3.1.2 ./configure --with-blas --with-lapack --with-tcltk --with-libpng --with-jpeglib --with-libtiff --with-system-zlib --with-system-bzlib --with-system-pcre --with-system-tre --with-system-xz --with-ICU --enable-R-shlib
	cmd R-3.1.2 ./configure --help
	cmd R-3.1.2 make clean 
	cmd R-3.1.2 make 
	export R_HOME=
	cmd R-3.1.2 make check 
	cmd R-3.1.2 make install-info
	cmd R-3.1.2 make install-pdf
	cmd R-3.1.2 make install 
	cmd . ldconfig
fi
#
# GEOS (for GRASS, GDAL)
#
if [ ! -r /usr/local/lib/libgeos.a -o $FORCE_REBUILD = 1 ]; then
	cmd geos-3.4.2 ./configure
	cmd geos-3.4.2 make clean 
	cmd geos-3.4.2 make 
	cmd geos-3.4.2 make install 
fi
#
# wxPython (for Grass)
#
if [ ! -r /usr/lib/python2.6/site-packages/wx.pth -o $FORCE_REBUILD = 1 ]; then
	cmd wxPython-src-3.0.2.0/wxPython python ./build-wxpython.py --install --build_dir=../bld
fi
#
# Python Numpy (for Grass)
#
if [ ! -r /usr/lib64/python2.6/site-packages/numpy-1.9.2-py2.6.egg-info -o $FORCE_REBUILD = 1 ]; then
	cmd numpy-1.9.2 python setup.py build
	cmd numpy-1.9.2 python setup.py install
fi
#
# Proj4 (for GRASS)
#
if [ ! -r /usr/local/lib/libproj.a -o $FORCE_REBUILD = 1 ]; then
	cmd proj-4.8.0 ./configure 
	cmd proj-4.8.0 make clean 
	cmd proj-4.8.0 make 
	cmd proj-4.8.0 make install 
fi
#
# GDAL (for PostGIS)
#
if [ ! -r /usr/local/lib/libgdal.a -o $FORCE_REBUILD = 1 ]; then
	cmd gdal-1.11.2 ./configure --with-png=internal --with-liblzma=yes --with-geotiff=internal --with-libtiff=internal --with-jpeg=internal --with-pg=/usr/local/pgsql/bin/pg_config 
	cmd gdal-1.11.2 make clean
	cmd gdal-1.11.2 make 
	cmd gdal-1.11.2 make install
fi
#
# GRASS (for GDAL)
#
REBUILD_GDAL=0
if [ ! -x /usr/local/bin/grass70 -o $FORCE_REBUILD = 1 ]; then
	cmd grass-7.0.0 ./configure --with-freetype=no --with-lapack --with-readline --with-postgres --with-gdal --with-geos --with-wxwidgets=/usr/local/bin/wx-config
	cmd grass-7.0.0 make clean
	cmd grass-7.0.0 make
	cmd grass-7.0.0 make install
	cmd . ldconfig
	cmd . /usr/local/bin/grass70 --version
	REBUILD_GDAL=1
fi
#
# Spatialite (for QGIS)
#
#if [ ! -r /usr/local/lib/libspatialite.a -o $FORCE_REBUILD = 1 ]; then
#	cmd libspatialite-amalgamation-2.3.0 ./configure
#	cmd libspatialite-amalgamation-2.3.0 make clean
#	cmd libspatialite-amalgamation-2.3.0 make 
#	cmd libspatialite-amalgamation-2.3.0 make install
#fi
#
# QGIS CANNOT BE BUILT (requires QT 4.7, current version 4.6.2 from 2010)
# Earlier version complain: Qt QTWEBKIT library not found.
#
#cmd qgis-2.8.1 mkdir -p build-master
#cmd qgis-2.8.1/build-master cmake --help
#cmd qgis-2.8.1/build-master cmake `pwd`/qgis-2.8.1
#cmd qgis-1.7.4 mkdir -p build-master
#cmd qgis-1.7.4/build-master cmake --help
#cmd qgis-1.7.4/build-master cmake `pwd`/qgis-1.7.4
#
# libKML (for GDAL - needs 1.3.0 if ever released)
#
#cmd libkml-1.2.0 ./configure
#cmd libkml-1.2.0 make clean
#cmd libkml-1.2.0 make 
#cmd libkml-1.2.0 make install
#
# GDAL (with GRASS)
#
if [ $REBUILD_GDAL = 1 ]; then
	cmd gdal-1.11.2 ./configure --with-png=internal --with-liblzma=yes --with-geotiff=internal --with-libtiff=internal --with-jpeg=internal --with-pg=/usr/local/pgsql/bin/pg_config --with-grass=/usr/local/grass-7.0.0
		--with-libkml=/usr/local --with-libkml-inc=/usr/local/include --with-libkml-lib="-L/usr/local/lib"
	cmd gdal-1.11.2 make clean
	cmd gdal-1.11.2 make 
	cmd gdal-1.11.2 make install
fi
#
# JSON-C
#
if [ ! -r /usr/local/lib/libjson-c.a -o $FORCE_REBUILD = 1 ]; then
	cmd json-c-0.12 autoreconf -v --force --install
	cmd json-c-0.12 ./configure
	cmd json-c-0.12 make clean
	cmd json-c-0.12 make 
	cmd json-c-0.12 make install
fi
#
# PostGIS - requires GEOS, Proj4, GDAL, libXML2, JSON-C
#
if [ ! -r /usr/local/pgsql/lib/postgis-2.1.so -o $FORCE_REBUILD = 1 ]; then
	cmd postgis-2.1.5 ./configure -with-pgconfig=/usr/local/pgsql/bin/pg_config --with-gui
	cmd postgis-2.1.5 make clean
	cmd postgis-2.1.5 make
	cmd postgis-2.1.5 make install
fi
#
# Node.js
#
if [ ! -x /usr/local/bin/node -o $FORCE_REBUILD = 1 ]; then
	cmd node-v0.12.0 ./configure
	cmd node-v0.12.0 make clean
	cmd node-v0.12.0 make
	cmd node-v0.12.0 make install
fi
#
# wxWidgets (for PG Admin III)
#
if [ ! -r /usr/local/lib/libwx_gtk2u_core-3.0.so -o $FORCE_REBUILD = 1 ]; then
	cmd wxWidgets-3.0.2 ./configure --with-gtk --enable-gtk2 --enable-unicode
	cmd wxWidgets-3.0.2 make clean
	cmd wxWidgets-3.0.2 make
	cmd wxWidgets-3.0.2 make install
fi
#
# PGAdmin III
#
if [ ! -x /usr/local/pgadmin3/bin/pgadmin3 -o $FORCE_REBUILD = 1 ]; then
	cmd pgadmin3-1.20.0 ./configure --with-wx-version=3.0 --enable-databasedesigner
	cmd pgadmin3-1.20.0 ./configure --help
	cmd pgadmin3-1.20.0 make clean
	cmd pgadmin3-1.20.0 make
	cmd pgadmin3-1.20.0 make install
fi
#
# Scite editor
#
if [ ! -x /usr/bin/SciTE -o $FORCE_REBUILD = 1 ]; then
	cmd scintilla/gtk make clean
	cmd scite/gtk make clean
	cmd scintilla/gtk make
	cmd scite/gtk make
	cmd scite/gtk make install
fi
#
# PLR
#
#FORCE_REBUILD=1
if [ ! -r /usr/local/pgsql/lib/plr.so -o $FORCE_REBUILD = 1 ]; then
	export USE_PGXS=1
	cmd plr rm "*.o" plr.so
	cmd plr make
	cmd plr install plr.so /usr/local/pgsql/lib
	cmd plr install "plr*.sql" /usr/local/pgsql/share/extension
	cmd plr install plr.control /usr/local/pgsql/share/extension
fi
cmd . ldconfig
cmd plr ldd /usr/local/pgsql/lib/postgis-2.1.so
cmd plr ldd /usr/local/pgsql/lib/plr.so
#
# Maven
#
#FORCE_REBUILD=1
if [ ! -x /usr/local/apache-maven-3.2.2/bin/mvn -o $FORCE_REBUILD = 1 ]; then
	MPWD=`pwd`
	cmd $MPWD wget http://mirror.olnevhost.net/pub/apache/maven/binaries/apache-maven-3.2.2-bin.tar.gz
	cmd /usr/local tar zxvf $MPWD/apache-maven-3.2.2-bin.tar.gz
        if [ -x /usr/local/bin/mvn ]; then
        	cmd $MPWD rm /usr/local/bin/mvn
	fi
        cmd $MPWD ln -s /usr/local/apache-maven-3.2.2/bin/mvn /usr/local/bin/mvn
fi
cmd . mvn --version

#
#
# Eof
