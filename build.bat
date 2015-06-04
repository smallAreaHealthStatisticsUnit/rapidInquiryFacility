powershell -ExecutionPolicy ByPass -file run.ps1 build.log rapidInquiryFacility mvn.cmd --errors clean validate compile package install war:war war:inplace
if %errorlevel% neq 0 exit /b %errorlevel%
