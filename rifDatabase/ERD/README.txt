The ERD was created by reverse engineering SAHUSland using pgmodeler (https://github.com/pgmodeler/pgmodeler)

It will be re-organised tidyly shortly.

The documents in docs can be generated using dbmstools. The paths are relative to the Oracle install directory on turing

Create XML schema

HOST ../dbmstools-0.4.5rc1/main/dbmsjy.py db2xml.py -j jdbc:postgresql://turing.private.net:5432/sahsuland\?user=rif40\&password=^1 -o ../install/xml/sahsuland.xml -d postgres8 -s rif40 -g -v

Edit documentation to remove:

1. <." in code. Really this should be declared as CDATA
2. Functional indexes

Traceback (most recent call last):
  File "../dbmstools-0.4.5rc1/main/xml2doc.py", line 990, in ?
    main()
  File "../dbmstools-0.4.5rc1/main/xml2doc.py", line 985, in main
    generateDoc(args[0], options)
  File "../dbmstools-0.4.5rc1/main/xml2doc.py", line 967, in generateDoc
    schema = databaselib.loadDatabase(schemaPath)
  File "/home/EPH/peterh/src/SAHSU/projects/rif/V4.0/create/dbmstools-0.4.5rc1/main/common/databaselib.py", line 1042, in loadDatabase
  File "/home/EPH/peterh/src/SAHSU/projects/rif/V4.0/create/dbmstools-0.4.5rc1/main/common/xmlutils.py", line 239, in loadDom
  File "/usr/lib64/python2.4/site-packages/_xmlplus/dom/minidom.py", line 1915, in parse
    return expatbuilder.parse(file)
  File "/usr/lib64/python2.4/site-packages/_xmlplus/dom/expatbuilder.py", line 926, in parse
    result = builder.parseFile(fp)
  File "/usr/lib64/python2.4/site-packages/_xmlplus/dom/expatbuilder.py", line 207, in parseFile
    parser.Parse(buffer, 0)
xml.parsers.expat.ExpatError: not well-formed (invalid token): line 504, column 77
Exit 1


Generate documentation

../dbmstools-0.4.5rc1/main/xml2doc.py -d postgres8 -f -v -g -w -o ../install/docs ../install/xml/sahsuland.xml


