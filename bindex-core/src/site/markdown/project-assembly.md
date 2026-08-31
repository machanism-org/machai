<!-- @guidance: 
- Generate or update the content as follows.  
- If any section or content already exists, update it with the latest and most accurate information instead of duplicating or skipping it.
- Analyze additional information from page: `https://machanism.org/ai-assembly/index.html` (selector:`.md-content`) and use it to create a content the current page.
- Add `https://machanism.org/ai-assembly/index.html` link as a reference to additional information.
# Page content
- Analyze the `src/main/resources/acts/assembly.toml` file and use diadram `images/assembly-act-workflow.png` in this section.
- Write a general description of the Act feature and its main functionality, using clear and simple language suitable for users who may not have prior technical knowledge or experience with the project.
- A clear, concise description of the act's purpose and when it should be used.
- Organize your output so that each act is easy to identify and understand.
- Ensure your descriptions are user-friendly and help the reader quickly determine the function and appropriate use case for each act.
-->

# Project Assembly

The **Assembly** act turns a plain-language application request into an initial,
usable project. Describe what you want to build and any important preferences, such as
the programming language, framework, database, platform, integrations, or deployment
environment. The act uses generative AI, semantic library search, and structured Bindex
metadata to recommend suitable components, create project files, and build the result.
It is designed for a curated library ecosystem, helping you start with practical,
maintainable implementations while retaining control of the technical decisions.

Assembly is a starting point for development, not a substitute for review. Always check
the generated code, dependencies, configuration, licenses, security, and behavior before
using the project in production.

## What the Assembly Act Does

Assembly combines a Large Language Model (LLM) with a curated library ecosystem. Its
process is designed to reuse relevant libraries rather than recreate their functionality
from scratch:

![Assembly Act workflow](images/assembly-act-workflow.png)

1. **Reads your request** — You provide a natural-language description of the application,
   its purpose, and its main features. Include technical requirements when you know them.
   For example: *"Create a REST API application for managing a user login using Spring
   Boot and Commercetools."*
2. **Finds candidate libraries** — The act sends your initial request to
   `pick-libraries`. Semantic search ranks libraries by their intended use, so results can
   match the meaning of your request rather than only its exact keywords. Review the
   recommended components to ensure they fit your needs.
3. **Reviews library metadata** — For each matching candidate, the act uses `get-bindex`
   to retrieve its Bindex JSON description and uses the Bindex schema to interpret that
   information consistently. The metadata can include features, integration points,
   examples, authorship, and licensing information that help the act use the library
   rather than recreate its functionality from scratch.
4. **Plans and generates the project** — The LLM uses your request and the selected Bindex
   information to create a suitable directory structure, build and dependency files (such
   as `pom.xml`, `build.gradle`, or `package.json`), source-code templates, entry points,
   API endpoints, and integration examples.
5. **Builds and corrects the project** — The act cleans and builds the generated project
   and fixes errors it encounters, with the goal of leaving a functional implementation.
6. **Documents the result** — The generated project includes a detailed `README.md`
   explaining the project, its configuration, and how to use it. You can then adapt the
   files to your own standards and requirements.

If the request does not contain information needed to continue, Assembly asks for the
missing details. The default project structure is **Clean Architecture**, unless you
specify a different structure. In an interactive session, you can review the suggested
libraries and clarify requirements before continuing.

## When to Use This Act

Use Assembly when you want to:

- **Create a new project quickly** without manually preparing boilerplate, build files,
  dependencies, and an initial directory structure.
- **Prototype an application** and receive a buildable implementation to review and extend.
- **Reuse existing libraries** that match your requirements instead of writing common
  functionality from scratch.
- **Explore integrations** by having the assistant identify relevant components and
  generate initial configuration and example integration code.

Assembly is most effective when you provide a clear goal and enough detail for library
selection. It is not the right choice when you need a fully production-ready system without
engineering review, or when strict requirements must be decided before any generated code
is considered. You remain responsible for reviewing the recommendations and verifying that
the finished project meets your functional, security, quality, and licensing requirements.

## How Library Selection Works

Each library in the ecosystem can have a `bindex.json` descriptor. The descriptor is
generated from project artifacts such as build files, source code, and other metadata. It
records useful information about the library, including its capabilities, integration
points, examples, authorship, and license. Bindex files are indexed with semantic
embeddings in a vector database. This allows Assembly to find libraries by intent and
then use their documented integration information when generating the project.

The act also uses the Bindex schema to interpret this structured information consistently.
The resulting project may include configuration files, initial implementation code, and
customization guidance for the selected libraries. Developers remain responsible for
verifying that the choices and generated implementation satisfy their functional, security,
quality, and licensing requirements.

## Tips for Better Results

- State the application's purpose and the most important features.
- Name the preferred language, framework, database, platform, or deployment environment
  when those choices matter.
- Describe required integrations and constraints, rather than requesting only a generic
  application.
- Review the generated source code, dependencies, configuration, build output, and
  `README.md` before continuing development or deploying the project.

## Reference

For additional information about AI Assembly, including its library-selection and project-
generation approach, see the [AI Assembly documentation](https://machanism.org/ai-assembly/index.html).
