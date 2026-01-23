package org.machanism.machai.bindex.fixtures;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.machanism.machai.ai.manager.GenAIProvider;

/**
 * Minimal test double for {@link GenAIProvider} capturing prompts and returning
 * a configurable response.
 */
public final class FakeGenAIProvider implements GenAIProvider {

    private final List<String> instructions = new ArrayList<>();
    private final List<String> prompts = new ArrayList<>();
    private final List<File> promptFiles = new ArrayList<>();
    private File inputsLogFile;

    private String response;

    public FakeGenAIProvider respondWith(String response) {
        this.response = response;
        return this;
    }

    public List<String> getInstructions() {
        return Collections.unmodifiableList(instructions);
    }

    public List<String> getPrompts() {
        return Collections.unmodifiableList(prompts);
    }

    public List<File> getPromptFiles() {
        return Collections.unmodifiableList(promptFiles);
    }

    public File getInputsLogFile() {
        return inputsLogFile;
    }

    @Override
    public void instructions(String instructions) {
        this.instructions.add(instructions);
    }

    @Override
    public void prompt(String prompt) {
        this.prompts.add(prompt);
    }

    @Override
    public void promptFile(File file, String templateKey) throws IOException {
        if (file == null) {
            throw new NullPointerException("file");
        }
        this.promptFiles.add(file);
    }

    @Override
    public void inputsLog(File file) {
        this.inputsLogFile = file;
    }

    @Override
    public String perform() {
        return response;
    }

    public static String utf8(String s) {
        return new String(s.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }

    @Override
    public void addFile(File file) throws IOException, FileNotFoundException {
        throw new UnsupportedOperationException("Not needed for unit tests");
    }

    @Override
    public void addFile(URL fileUrl) throws IOException, FileNotFoundException {
        throw new UnsupportedOperationException("Not needed for unit tests");
    }

    @Override
    public List<Float> embedding(String text) {
        throw new UnsupportedOperationException("Not needed for unit tests");
    }

    @Override
    public void clear() {
        // no-op
    }

    @Override
    public void addTool(String name, String description, Function<Object[], Object> function, String... paramsDesc) {
        throw new UnsupportedOperationException("Not needed for unit tests");
    }

    @Override
    public void model(String chatModelName) {
        // no-op
    }

    @Override
    public void setWorkingDir(File workingDir) {
        // no-op
    }

    @Override
    public void close() {
        // no-op
    }
}
