@echo off
rem Extract Java ME Javadoc from SDK to project lib/javame/javadoc
set SDK_DIR=D:\Java_ME_platform_SDK_3.4
set DEST_DIR=%~dp0..\lib\javame\javadoc
if not exist "%DEST_DIR%" mkdir "%DEST_DIR%"

echo Copying Javadoc archives from %SDK_DIR%\docs\api to %DEST_DIR%
for %%F in (midp-2.0.zip cldc-1.1.zip jsr135.zip jsr172.zip) do (
  if exist "%SDK_DIR%\docs\api\%%F" (
    echo Extracting %%F ...
    powershell -Command "Expand-Archive -Path '%SDK_DIR%\docs\api\%%F' -DestinationPath '%DEST_DIR%\%%~nF' -Force"
  ) else (
    echo Skipping %%F (not found)
  )
)

echo Done. Please open the Java Projects view in VS Code and attach the Javadoc folders if needed.
pause
