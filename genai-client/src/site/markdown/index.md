# GenAI Client
<!-- @guidance: 
- Analyze the source file and create a Maven Site-style introductory home page for your project.
- Do not use the horizontal rule separator between sections. 
- Supports GenAI providers:
	1. OpenAI: Example to use: `OpenAI:{CHAT_MODEL}`.
	2. Ae: Example to use: `Ae:{CONFIG_NAME}`, `CONFIG_NAME` should be defined in `ae.xml` file.
-->

Welcome to the GenAI Client Home Page
=====================================

GenAI Client is a flexible, extendable platform designed to connect with various Generative AI providers. This project enables seamless integration with AI models for chat, completion, and creativity-driven tasks.

Key Features
============
- **Multi-provider support** for popular Generative AI platforms.
- Easy-to-configure provider settings.
- Suitable for both REST and embedded usage scenarios.
- Maven-based project layout.

Supported Providers
===================
- **OpenAI**: Integrate with OpenAI models by using:
    - `OpenAI:{CHAT_MODEL}`
      - Example: `OpenAI:gpt-4`, `OpenAI:gpt-3.5-turbo`
- **Ae**: Integrate with Ae using configurations defined in your `ae.xml` resource:
    - `Ae:{CONFIG_NAME}`
      - Example: `Ae:default`, where `default` is an entry in the `ae.xml` config file.
      
Project Layout
==============
- **Source code:** `genai-client/src/main/java`
- **Resources:** `genai-client/src/main/resources`
- **Test code:** `genai-client/src/test/java`
- **Test resources:** `genai-client/src/test/resources`
- **Project documents:** `genai-client/src/site`

Quick Start Guide
=================
1. Clone the repository.
2. Configure your provider settings in the resources folder.
3. Build and run using Maven.
4. Consult the documentation for provider specifics.

For details, see other pages in this Maven Site or consult your AI provider's documentation.
