# Maintain Consistency and Formatting:

- Review the Java class source code and include comprehensive Javadoc comments for all classes, methods, and fields, adhering to established best practices.
- Ensure that each Javadoc comment provides clear explanations of the purpose, parameters, return values, and any exceptions thrown.
- When generating Javadoc, if you encounter code blocks inside `<pre>` tags, escape `<` and `>` as `&lt;` and `&gt;` as `&gt;` in `<pre>` content for Javadoc. Ensure that the code is properly escaped and formatted for Javadoc.
- Do not use escaping in `{@code ...}` tags. 
- Escape the closing javadoc tag in javadoc content, as it was breaking javadoc compilation.  
- Follow a consistent style and structure for all Javadoc comments.
- Use proper Markdown or HTML formatting for readability.
- Make sure that the comment text does not contain the pair of characters: an asterisk followed by a forward slash.
- If so, always use the HTML entity `*&#47;` to prevent Javadoc compilation from breaking.
  