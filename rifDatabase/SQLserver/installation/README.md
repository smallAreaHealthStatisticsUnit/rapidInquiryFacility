To install database, run in this order (I think):

sqlcmd -d sahsuland_dev -i rif40_database_creation.sql (must set password)
sqlcmd -d sahsuland_dev -i rif40_user_roles.sql (optional rif40_test_user.sql with password)
rif40_install_tables.bat
rif40_install_views.bat
rif40_install_sequences.bat
rif40_install_log_error_handling.bat
rif40_install_functions.bat
rif40_install_table_triggers.bat
rif40_install_view_triggers.bat
rif40_data_install_tables.bat
rif40_import_data.bat (but change path, take away comments in the rif40_import_sahsuland.sql script)
