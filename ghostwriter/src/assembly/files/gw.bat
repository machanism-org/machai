@echo off

REM -------------------------------------------------------------------
REM You can define any property from gw.properties as an environment variable here.
REM For example, to set CodeMie credentials:
REM SET GENAI_USERNAME=your_codemie_username
REM SET GENAI_PASSWORD=your_codemie_password

REM -------------------------------------------------------------------
REM Alternatively, you can pass properties as Java system parameters using the -D option.
REM For example:
REM java -DGENAI_USERNAME=your_codemie_username -DGENAI_PASSWORD=your_codemie_password -jar %~dp0/gw.jar %*

REM -------------------------------------------------------------------
REM By default, this will run the application with any additional arguments passed to the script.

java -jar %~dp0/gw.jar %*