#!/bin/bash

# Uncomment and set these variables if needed
# export GENAI_PASSWORD=
# export GENAI_USERNAME=

java -jar "$(dirname "$0")/gw.jar" "$@"