#
# Build programs
#
JAVAC = javac
JAVA = java
# JAVACLAGS = -verbose -g -Xlint 
JAVACFLAGS = -g -Xlint 
JAVAFLAGS =
#
# Rules
#
.SUFFIXES: .class .java

.java.class:
	$(JAVAC) $(JAVACFLAGS) $*.java

#
# Source/object code
# 
RIF40_DB_CLASS_FILES = dumpdata.class
RIF40_DB_JAVA_FILES  = $(RIF40_DB_CLASS_FILES:%.class=%.java)

#
# Targets
#
all: subdirs dumpdata.jar

subdirs:
	$(MAKE) -C rif40_db all

clean:
	$(RM) dumpdata.jar $(RIF40_DB_CLASS_FILES)
	$(MAKE) -C rif40_db clean

dumpdata.jar: $(RIF40_DB_CLASS_FILES)
	jar -cvf dumpdata.jar $(RIF40_DB_CLASS_FILES) rif40_db/

#
# Dependencies
#
$(RIF40_DB_CLASS_FILES): $(RIF40_DB_JAVA_FILES)
#
linux_test: all
	$(JAVA) $(JAVAFLAGS) -cp `pwd`:postgresql-9.2-1002.jdbc4.jar:dumpdata.jar dumpdata rif40_studies
windows_test: all
	$(JAVA) $(JAVAFLAGS) -cp P:\src\SAHSU\projects\rif\V4.0\create\install;postgresql-9.2-1002.jdbc4.jar;rif40_db\rif40_db.class dumpdata rif40_studies

