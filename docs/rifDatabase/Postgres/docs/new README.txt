PATH += C:\MinGW\msys\1.0\bin;C:\MinGW\bin;

c:\mingw\bin\mingw-get install msys-wget
mingw-get install msys-which

Set postgres password

# Postgres hugepages
#
# Allow 128G, 2M size, 64 pages, postgres group (26)
vm.nr_hugepages = 65536
vm.hugetlb_shm_group = 26

cat /proc/meminfo | grep -i huge
sysctl -p

mkdir -p /usr/local/pgsql/etc
chown postgres:peterh /usr/local/pgsql/etc
chmod 6775 /usr/local/pgsql/etc
ls -al /usr/local/pgsql/etc
[root@darwin Downloads]# ls -al /usr/local/pgsql/etc
total 8
drwsrwsr-x 2 postgres peterh 4096 Mar 11 18:36 .
drwxr-xr-x 7 root     root   4096 Mar 11 18:36 ..


[root@darwin Downloads]# cat ~/.pgpass 
localhost:5432:*:postgres:Se11afield2014

chmod 600 ~/.pgpass 

Add testuser (username in lower) to ~/.pgpass, password is username
Add rif40, password same as postgres
Add notarifuser, pasword is username

make db_setup
