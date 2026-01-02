# GenAI Client

GenAI Client is a Java library that provides convenient integration with GenAI APIs such as OpenAI and other providers. It enables flexible configuration and usage through a unified interface, including command-function tools, file operations, and prompt management.

## Features
- Unified abstraction for GenAI providers (OpenAI and beyond)
- Command-function, file, and system tools for automation use cases
- Flexible prompt resources and customization
- Simple integration via Maven

## Installation
Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>org.machanism.machai</groupId>
    <artifactId>genai-client</artifactId>
    <version>0.0.2-SNAPSHOT</version>
</dependency>
```

## Usage
1. Configure the desired GenAI provider and API keys in your code/resources.
2. Use the `GenAIProvider` and `GenAIProviderManager` classes to access provider features.
3. Check the [prompts.properties](src/main/resources/prompts.properties) file for prompt configuration.

## Documentation
See `src/site/markdown` for additional documentation (site folder may require manual setup).

## Support
For issues or questions, contact: info@machanism.org

## License
This project is licensed under the Apache License 2.0. See [LICENSE.txt](LICENSE.txt) for details.