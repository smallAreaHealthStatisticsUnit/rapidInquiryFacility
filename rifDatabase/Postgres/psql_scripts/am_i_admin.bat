@ECHO OFF
REM
REM Check if an administrator
REM
NET SESSION >nul 2>&1
if %errorlevel% equ 0 (
    ECHO Y
) else (
	ECHO N
)
exit /b 0