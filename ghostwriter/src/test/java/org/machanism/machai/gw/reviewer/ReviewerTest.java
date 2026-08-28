package org.machanism.machai.gw.reviewer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ReviewerTest {

	@TempDir
	Path project;

	@Test
	void reviewersExposeTheirSupportedExtensions() {
		assertArrayEquals(new String[] { "html", "htm", "xml" }, new HtmlReviewer().getSupportedFileExtensions());
		assertArrayEquals(new String[] { "java" }, new JavaReviewer().getSupportedFileExtensions());
		assertArrayEquals(new String[] { "md" }, new MarkdownReviewer().getSupportedFileExtensions());
		assertArrayEquals(new String[] { "py" }, new PythonReviewer().getSupportedFileExtensions());
		assertArrayEquals(new String[] { "ts" }, new TypeScriptReviewer().getSupportedFileExtensions());
		assertArrayEquals(new String[] { "puml" }, new PumlReviewer().getSupportedFileExtensions());
		assertArrayEquals(new String[] { "txt" }, new TextReviewer().getSupportedFileExtensions());
	}

	@Test
	void javaReviewerRecognizesGuidanceAndExtractsPackageNames() throws Exception {
		File source = write("Example.java", "/* @guidance: document it */\npackage demo.sample;");

		String prompt = new JavaReviewer().perform(project.toFile(), source);

		assertTrue(prompt != null && !prompt.isEmpty());
		assertEquals("demo.sample", JavaReviewer.extractPackageName("// package ignored\npackage demo.sample;"));
		assertEquals("<default package>", JavaReviewer.extractPackageName("class Example {}"));
		assertNull(new JavaReviewer().perform(project.toFile(), write("Plain.java", "class Plain {}")));
	}

	@Test
	void javaReviewerUsesPackageInfoTemplateWhenGuidanceExists() throws Exception {
		File source = write("package-info.java", "// @guidance: package docs\npackage demo;");

		String prompt = new JavaReviewer().perform(project.toFile(), source);

		assertTrue(prompt.contains("package-info.java"));
		assertTrue(!prompt.contains("package docs"));
	}

	@Test
	void markupReviewersReturnPromptOnlyForMatchingGuidanceComments() throws Exception {
		String html = new HtmlReviewer().perform(project.toFile(), write("page.html", "<!-- @guidance: improve -->\n<body/>"));
		String markdown = new MarkdownReviewer().perform(project.toFile(), write("readme.md", "<!-- @guidance: improve -->\n# title"));

		assertTrue(html.contains("page.html"));
		assertTrue(markdown.contains("readme.md"));
		assertNull(new HtmlReviewer().perform(project.toFile(), write("plain.xml", "<root/>")));
		assertNull(new MarkdownReviewer().perform(project.toFile(), write("plain.md", "# no guidance")));
	}

	@Test
	void languageReviewersSupportLineAndBlockGuidanceAndRejectBlankText() throws Exception {
		String pythonLine = new PythonReviewer().perform(project.toFile(), write("one.py", "# @guidance: improve typing"));
		String pythonBlock = new PythonReviewer().perform(project.toFile(), write("two.py", "''' @guidance: improve docs '''"));
		String typeScriptLine = new TypeScriptReviewer().perform(project.toFile(), write("one.ts", "// @guidance: improve typing"));
		String typeScriptBlock = new TypeScriptReviewer().perform(project.toFile(), write("two.ts", "/* @guidance: improve docs */"));

		assertTrue(pythonLine != null && !pythonLine.isEmpty());
		assertTrue(pythonBlock != null && !pythonBlock.isEmpty());
		assertTrue(typeScriptLine != null && !typeScriptLine.isEmpty());
		assertTrue(typeScriptBlock != null && !typeScriptBlock.isEmpty());
		assertNull(new PythonReviewer().perform(project.toFile(), write("blank.py", "# @guidance:   ")));
		assertNull(new TypeScriptReviewer().perform(project.toFile(), write("blank.ts", "/* @guidance:   */")));
	}

	@Test
	void pumlAndTextReviewersHandleApplicableFilesAndArguments() throws Exception {
		PumlReviewer puml = new PumlReviewer();
		assertTrue(puml.perform(project.toFile(), write("diagram.puml", "' @guidance: update diagram")).contains("diagram.puml"));
		assertNull(puml.perform(project.toFile(), write("plain.puml", "@startuml\n@enduml")));
		assertThrows(NullPointerException.class, () -> puml.perform(null, new File("x")));

		TextReviewer text = new TextReviewer();
		assertTrue(text.perform(project.toFile(), write("@guidance.txt", "shared instruction")).contains("shared instruction"));
		assertNull(text.perform(project.toFile(), write("notes.txt", "shared instruction")));
		assertEquals("  ", text.getPrompt(project.toFile(), project.resolve("@guidance.txt").toFile(), "  "));
	}

	private File write(String name, String content) throws Exception {
		Path file = project.resolve(name);
		Files.write(file, content.getBytes(StandardCharsets.UTF_8));
		return file.toFile();
	}
}
