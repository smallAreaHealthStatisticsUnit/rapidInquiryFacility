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
RIF40_DB_JAVA_FILES  = RifDBLogin.java
RIF40_DB_CLASS_FILES = $(RIF40_DB_JAVA_FILES:%.java=%.class)

#
# Targets
#
all: rif40_db.jar

clean:
	$(RM) rif40_db.jar $(RIF40_DB_CLASS_FILES)

rif40_db.jar: $(RIF40_DB_CLASS_FILES)
	jar -cvf rif40_db.jar $(RIF40_DB_CLASS_FILES)

#
# Dependencies
#
$(RIF40_DB_CLASS_FILES): $(RIF40_DB_JAVA_FILES)
