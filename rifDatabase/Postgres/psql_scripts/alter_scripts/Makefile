#
#
USER=$(USERNAME)
RUN=powershell -file run.ps1
PSQL=psql
PSQL_FLAGS=-U rif40 -d sahsuland_dev -w -e -v testuser=$(USER)
SOURCES=v4_0_alter_1.sql v4_0_alter_2.sql
DEV_SOURCES=v4_0_alter_3.sql v4_0_alter_4.sql

TARGETS=$(SOURCES:.sql=.rpt)
DEV_TARGETS=$(DEV_SOURCES:.sql=.rpt)

.SUFFIXES: .sql .rpt
.sql.rpt:
	$(RUN) $(CURDIR)/$@ $(PSQL) $(PSQL_FLAGS) -f alter_scripts\$< 
	
all: $(TARGETS)
dev: $(TARGETS) $(DEV_TARGETS)

clean: 
	del /q $(TARGETS)
devclean: 
	del /q $(TARGETS) $(DEV_TARGETS)
	
#
# Eof