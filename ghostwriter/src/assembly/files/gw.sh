#!/bin/bash

# -------------------------------------------------------------------
# You can define any property from gw.properties as an environment variable here.
# For example, to set CodeMie credentials:
# export GENAI_USERNAME=your_codemie_username
# export GENAI_PASSWORD=your_codemie_password

# -------------------------------------------------------------------
# Alternatively, you can pass properties as Java system parameters using the -D option.
# For example:
# java -DGENAI_USERNAME=your_codemie_username -DGENAI_PASSWORD=your_codemie_password -jar "$(dirname "$0")/gw.jar" "$@"

# -------------------------------------------------------------------
# By default, this will run the application with any additional arguments passed to the script.

java -jar "$(dirname "$0")/gw.jar" "$@"