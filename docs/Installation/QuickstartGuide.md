---
layout: default
title: "Installing the RIF: Quickstart Guide"
---

## Prerequisites

Before installing the Rapid Inquiry Facility you mus have the following installed:

1. A database. PostgreSQL and Microsoft SQL Server are supported.
2. Apache Tomcat.

Assuming those are installed and you have administrative rights on the machine on which you are installing, proceed as follows.

## Getting the RIF

### Prebuilt WARs and scripts

Download the database dumps for your platform from...

Download the three `WAR` files from...

### Directly from GitHub

This assumes you have the following command-line tools installed: `git`, Maven (the `mvn` command).

Clone the repository:

```
git clone https://github.com/smallAreaHealthStatisticsUnit/rapidInquiryFacility.git
```

Build:

```
mvn clean install
```



- Database
	- PostgreSQL
	- SQL Server
- Tomcat
	- Deploying the WARs
- Configuration

