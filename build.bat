powershell -ExecutionPolicy ByPass -file run.ps1 build.log . mvn.cmd --errors --fail-at-end --file rifGenericLibrary --file rifServices clean validate compile package install 
if %errorlevel% neq 0 exit /b %errorlevel%
powershell -ExecutionPolicy ByPass -file run.ps1 build.log rifServices mvn.cmd --errors --fail-at-end war:war war:inplace
if %errorlevel% neq 0 exit /b %errorlevel%
