@echo off
REM Run the JS/TS recipe transformer.
REM Prerequisites: cd engine-js && npm install && npm run build
REM Usage: run-js.cmd <inputDir> <outputDir>
REM Example: run-js.cmd resources\input\angular-sample output\js
set ENGINE_JS=%~dp0engine-js
if not exist "%ENGINE_JS%\dist\run.js" (
  echo Build engine-js first: cd engine-js ^&^& npm install ^&^& npm run build
  exit /b 1
)
node "%ENGINE_JS%\dist\run.js" --config "%~dp0config-js.json" --resources "%~dp0resources" %*
