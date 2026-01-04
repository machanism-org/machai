![](images/machai-ghostwriter-logo.png)

# Ghostwriter

Ghostwriter is an advanced documentation engine for Machai projects. It automates the scanning, analysis, and assembly of project documentation, driven by rules and guidance embedded throughout the codebase and supporting resources. At its core is the `DocsProcessor`, which orchestrates:

- **Automated Project Scanning:** Detects the layout, traverses all sources and documentation folders (including `src/main/java`, `src/main/resources`, `src/test/java`, `src/test/resources`, and `src/site/markdown`), and efficiently excludes irrelevant directories.
- **Guidance-Driven Extraction:** Finds and parses `@guidance` comments embedded in Markdown (`.md`), Java (`.java`), and custom resource files. These guidance tags steer documentation generation according to best practices and project-specific rules.
- **Extensible Reviewers:** Utilizes specialized reviewers for each format (`MarkdownReviewer`, `JavaReviewer`, `PythonReviewer`, and `TextReviewer`) to extract context, comments, and functional details from diverse file types.
- **Contextual Content Assembly:** Integrates extracted guidance with project layout descriptions and additional context to generate comprehensive, rule-based documentation.
- **AI-Powered Synthesis:** Connects with generative AI models to produce clear, focused documentation by merging code-derived knowledge, developer-supplied guidance, and contextual project information.
- **Modular and Scalable Architecture:** Built to process monolithic and multi-module projects. Detects sources, tests, documents, modules, and automates documentation creation across complex or large codebases.

Ghostwriter empowers developers to generate high-quality documentation automatically, reduce manual effort, and maintain consistency. It serves as the backbone for Machai and Machanism guides, tutorials, and project references—making application assembly and onboarding faster and easier.

## License

Machai is licensed under the Apache License 2.0.  
You can view the full license text [here](LICENSE).

## Contact

If you have any questions or need support, feel free to reach out:
- Official Website: [Machanism](https://machanism.org)
- Email: [develop@machanism.org](mailto:develop@machanism.org)

Machai simplifies and accelerates application assembly, empowering developers to focus on innovation.
