# RIF ERD

The ERD was created by reverse engineering SAHSUland using pgmodeler (https://github.com/pgmodeler/pgmodeler). This was unsatisfactory, so the ERDs were generated using dbmstools (http://dbmstools.sourceforge.net/). This is an old unloved tool; with faults; but it does generate simple HTML based documentatino with a per object ERD; avoid the need for the tidying phase with pgmodeller.

The ERDs have been split into two:

* [SAHSUland core rif40 schema](github-windows://openRepo/https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility?branch=master&filepath=rifDatabase%2FERD%2Frif40%2Findex-sahsuland-postgres8.html)

* [SAHSUland example data - the rif_data schema](github-windows://openRepo/https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility?branch=master&filepath=rifDatabase%2FERD%2Fsahsuland%2Findex-sahsuland-postgres8.html)

* [Example test case data; also show structure of the output tables - the rif_studies schema](github-windows://openRepo/https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility?branch=master&filepath=rifDatabase%2FERD%2Frif_studies%2Findex-sahsuland-postgres8.html)

**You will need a Windows version of the github client installed to view these schemas or point your browser to the rapidInquiryFacility/rifDatabase/ERD/rif40 or sahsuland or rif_studies directory.**

The following are not documented:

* Partitioning (rif40_partitions schema). This will be a separate series of WIKIs.
* PL/pgsql code in rif40_&lt;code area/type, e.g. R, sql, sm - for statemachine, geo&gt;_pkgs. Again this will be documented separately.

A Makefile is provided to build the doucmentation. 

The documents in docs can be. The paths are relative to the Oracle create directory on turing

Can be viewed using Github for Windows as: github-windows://openRepo/https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility?branch=master&filepath=rifDatabase%2FERD%2Fdocs%2Findex-sahsuland-postgres8.html

Make has four targets:

* clean - clean all (i.e. docs)
* veryclean - all and remove dbms_tools
* dbms_tools - get dbms_tools and install
* all - build docs

You will need to have rapidInquiryFacility as a local repository; and have the database available.

Unlike the other RIF database Makefiles this is Linux only bacause it

* Uses sed to hack the XML ERD into a parseable form
* Uses awk to extract the rif40_password ffrom ~/.pgpass
* Use wget and unzip to install dbms_tools

Requires python and grpahviz

XML styntac errors E.g. 

```
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
```

Specifically the following are handled:

* View definitions needs CDATA
* current_user() is not handled correctly
* The usual description substitutions (& => &amp; etc). These get very specific in the sahusland example data and you cannot use CDATA in a description field
* Functional indexes blow the program and are removed

Beware that unhandled &lt;, &gt; in the description field will cause parse failure.
