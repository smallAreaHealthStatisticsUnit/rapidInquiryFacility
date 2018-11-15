---
layout: default
title: Database design documentation
---

The database design documentation was created using http://dbmstools.sourceforge.net/. See the [README.md](https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/tree/master/rifDatabase/ERD#rif-erd) file in the rifDatabase/ERD directory for how to re-create it. This is the current SAHSULAND database. The core schema and the SAHSUland example data are documented.

* [SAHSUland core rif40 schema](https://rawgit.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/master/rifDatabase/ERD/rif40/index-sahsuland-postgres8.html)

* [SAHSUland example data - the rif_data schema](https://rawgit.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/master/rifDatabase/ERD/sahsuland/index-sahsuland-postgres8.html)

* [Example test case data; also show structure of the output tables - the rif_studies schema](https://rawgit.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility/master/rifDatabase/ERD/rif_studies/index-sahsuland-postgres8.html)

The following are not documented:

* Partitioning (rif40_partitions schema). This will be a separate series of WIKIs.
* PL/pgsql code in rif40_&lt;code area/type, e.g. R, sql, sm - for statemachine, geo&gt;_pkgs. Again this will be documented separately.

There is also a [Powerpoint]({{ site.baseurl }}/development/RIF%20database.pptx) presentation of the new RIF database design.
