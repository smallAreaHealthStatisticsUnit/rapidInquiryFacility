RIF Standalone documentation
============================
 
- [RIF Web Application and Middleware Installation](RIF_Web_Application_Installation.html)
- [Windows Postgres Install using pg_dump and scripts](RIF_Postgres_Install.html)
- [SQL Server Production Database Installation](RIF_SQLserver_Install.html)
- [RIF Manual Data Loading](RIF_manual_data_loading.html)
- [Tile maker Manual](tileMaker.html)
- [Database Management Manual](databaseManagementManual.html)
- [RIF Data Loader Manual](RIF_Data_Loader_Manual.pdf)
- [RIF v4 0 Manual](RIF_v40_Manual.pdf)

**Note: images are still referenced using github. This documentation will not work on a private (air gapped) network.**

# Printing documents direct from GitHub

The HTML version of this github markdown was created using *grip*. Python 2.7 (for Node.js) needs to be installed and on your path. You can then point your web browser at: *http://localhost:6419/* (or wherever else grip chooses)
and chose "save as HTML" in the browser. Do not do this in the github repository or it will make a mess.

To install:
```
python -m pip install grip
```

To document this: *rifWebApplication\Readme.md*
```
C:\Users\phamb\Documents\GitHub\rapidInquiryFacility>python -m grip rifWebApplication\Readme.md
 * Running on http://localhost:6419/ (Press CTRL+C to quit)
 * Downloading style https://assets-cdn.github.com/assets/frameworks-592c4aa40e940d1b0607a3cf272916ff.css
 * Downloading style https://assets-cdn.github.com/assets/github-96ebb1551fc5dba84c6d2a0fa7b1cfcf.css
 * Downloading style https://assets-cdn.github.com/assets/site-348211d27070b0d7bb5d31b1ac3d265b.css
 * Cached all downloads in C:\Users\phamb\.grip\cache-4.5.2
127.0.0.1 - - [13/Apr/2018 14:27:31] "GET / HTTP/1.1" 200 -
127.0.0.1 - - [13/Apr/2018 14:27:31] "GET /__/grip/asset/frameworks-592c4aa40e940d1b0607a3cf272916ff.css HTTP/1.1" 200 -
127.0.0.1 - - [13/Apr/2018 14:27:31] "GET /__/grip/asset/github-96ebb1551fc5dba84c6d2a0fa7b1cfcf.css HTTP/1.1" 200 -
127.0.0.1 - - [13/Apr/2018 14:27:31] "GET /__/grip/asset/site-348211d27070b0d7bb5d31b1ac3d265b.css HTTP/1.1" 200 -
127.0.0.1 - - [13/Apr/2018 14:27:31] "GET /__/grip/static/octicons/octicons.css HTTP/1.1" 200 -
127.0.0.1 - - [13/Apr/2018 14:27:31] "GET /__/grip/static/octicons/octicons.woff2?ef21c39f0ca9b1b5116e5eb7ac5eabe6 HTTP/1.1" 200 -
127.0.0.1 - - [13/Apr/2018 14:27:33] "GET /__/grip/static/favicon.ico HTTP/1.1" 200 -
127.0.0.1 - - [13/Apr/2018 14:27:33] "GET /__/grip/static/favicon.ico HTTP/1.1" 200 -
127.0.0.1 - - [13/Apr/2018 14:29:02] "GET / HTTP/1.1" 200 -
127.0.0.1 - - [13/Apr/2018 14:29:02] "GET /__/grip/asset/frameworks-592c4aa40e940d1b0607a3cf272916ff.css HTTP/1.1" 200 -
127.0.0.1 - - [13/Apr/2018 14:29:02] "GET /__/grip/asset/github-96ebb1551fc5dba84c6d2a0fa7b1cfcf.css HTTP/1.1" 200 -
127.0.0.1 - - [13/Apr/2018 14:29:02] "GET /__/grip/asset/site-348211d27070b0d7bb5d31b1ac3d265b.css HTTP/1.1" 200 -
127.0.0.1 - - [13/Apr/2018 14:29:02] "GET /__/grip/static/octicons/octicons.css HTTP/1.1" 200 -
127.0.0.1 - - [13/Apr/2018 14:29:03] "GET /__/grip/static/octicons/octicons.woff2?ef21c39f0ca9b1b5116e5eb7ac5eabe6 HTTP/1.1" 200 -
127.0.0.1 - - [13/Apr/2018 14:29:04] "GET /__/grip/static/favicon.ico HTTP/1.1" 200 -
127.0.0.1 - - [13/Apr/2018 14:32:14] "GET / HTTP/1.1" 200 -
----------------------------------------
Exception happened during processing of request from ('127.0.0.1', 59782)
Traceback (most recent call last):
  File "C:\Python27\lib\SocketServer.py", line 596, in process_request_thread
    self.finish_request(request, client_address)
  File "C:\Python27\lib\SocketServer.py", line 331, in finish_request
    self.RequestHandlerClass(request, client_address, self)
  File "C:\Python27\lib\SocketServer.py", line 654, in __init__
    self.finish()
  File "C:\Python27\lib\SocketServer.py", line 713, in finish
    self.wfile.close()
  File "C:\Python27\lib\socket.py", line 283, in close
    self.flush()
  File "C:\Python27\lib\socket.py", line 307, in flush
    self._sock.sendall(view[write_offset:write_offset+buffer_size])
error: [Errno 10054] An existing connection was forcibly closed by the remote host
----------------------------------------
127.0.0.1 - - [13/Apr/2018 14:32:14] "GET / HTTP/1.1" 200 -
127.0.0.1 - - [13/Apr/2018 14:32:14] "GET /__/grip/asset/frameworks-592c4aa40e940d1b0607a3cf272916ff.css HTTP/1.1" 200 -
127.0.0.1 - - [13/Apr/2018 14:32:14] "GET /__/grip/asset/site-348211d27070b0d7bb5d31b1ac3d265b.css HTTP/1.1" 200 -
127.0.0.1 - - [13/Apr/2018 14:32:14] "GET /__/grip/static/octicons/octicons.css HTTP/1.1" 200 -
127.0.0.1 - - [13/Apr/2018 14:32:14] "GET /__/grip/asset/github-96ebb1551fc5dba84c6d2a0fa7b1cfcf.css HTTP/1.1" 200 -
127.0.0.1 - - [13/Apr/2018 14:32:14] "GET /__/grip/static/octicons/octicons.woff2?ef21c39f0ca9b1b5116e5eb7ac5eabe6 HTTP/1.1" 200 -
 * Shutting down...
```

# Build the docs directory

Use ```make doc```; e.g.:

```
C:\Users\phamb\Documents\GitHub\rapidInquiryFacility>make doc
python -m grip rifWebApplication\Readme.md --export docs\RIF_Web_Application_Installation.html
Exporting to docs\RIF_Web_Application_Installation.html
python -m grip rifDatabase\Postgres\production\windows_install_from_pg_dump.md --export docs\RIF_Postgres_Install.html
Exporting to docs\RIF_Postgres_Install.html
python -m grip rifDatabase\SQLserver\production\INSTALL.md --export docs\RIF_SQLserver_Install.html
Exporting to docs\RIF_SQLserver_Install.html
python -m grip docs\README.md --export docs\index.html
Exporting to docs\index.html
"C:\Program Files\7-Zip\7z.exe" a -r docs.7z "docs\\*"

7-Zip 18.01 (x64) : Copyright (c) 1999-2018 Igor Pavlov : 2018-01-28

Scanning the drive:
5 files, 2632290 bytes (2571 KiB)

Creating archive: docs.7z

Add new data to archive: 5 files, 2632290 bytes (2571 KiB)


Files read from disk: 5
Archive size: 139160 bytes (136 KiB)
Everything is Ok
"C:\Program Files\7-Zip\7z.exe" l docs.7z

7-Zip 18.01 (x64) : Copyright (c) 1999-2018 Igor Pavlov : 2018-01-28

Scanning the drive for archives:
1 file, 139160 bytes (136 KiB)

Listing archive: docs.7z

--
Path = docs.7z
Type = 7z
Physical Size = 139160
Headers Size = 304
Method = LZMA2:3m
Solid = +
Blocks = 1

   Date      Time    Attr         Size   Compressed  Name
------------------- ----- ------------ ------------  ------------------------
2018-04-18 11:34:38 ....A       573158       138856  docs\index.html
2018-04-18 11:24:43 ....A         4533               docs\README.md
2018-04-18 11:34:35 ....A       591286               docs\RIF_Postgres_Install.html
2018-04-18 11:34:37 ....A       657294               docs\RIF_SQLserver_Install.html
2018-04-18 11:34:34 ....A       806019               docs\RIF_Web_Application_Installation.html
------------------- ----- ------------ ------------  ------------------------
2018-04-18 11:34:38            2632290       138856  5 files
```

