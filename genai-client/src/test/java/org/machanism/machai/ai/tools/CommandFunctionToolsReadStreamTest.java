package org.machanism.machai.ai.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertSame;
import org.machanism.machai.ai.tools.LimitedStringBuilder;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Additional unit tests to cover private stream-reading paths.
 */
class CommandFunctionToolsReadStreamTest {

    @Test
    void readStream_whenIOException_callsErrorConsumerAndDoesNotThrow() throws Exception {
        // Arrange
        CommandFunctionTools tools = new CommandFunctionTools();

        // Force IOException in InputStreamReader by passing an InputStream that always throws.
        java.io.InputStream failing = new java.io.InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("boom");
            }
        };

        LimitedStringBuilder output = new LimitedStringBuilder(256);
        final StringBuilder errors = new StringBuilder();

        Object lineConsumer = newLineConsumerProxy(line -> output.append("LINE:").append(line));
        Object errorConsumer = newErrorConsumerProxy(e -> errors.append(e.getMessage()));

        // Act
        invokePrivateReadStream(tools, failing, StandardCharsets.UTF_8.name(), output, lineConsumer, errorConsumer);

        // Assert
        assertEquals("boom", errors.toString());
        assertEquals(0, output.length());
    }

    @Test
    void readStream_whenLinesPresent_appendsAndCallsLineConsumer() throws Exception {
        // Arrange
        CommandFunctionTools tools = new CommandFunctionTools();

        ByteArrayInputStream in = new ByteArrayInputStream("a\nb\n".getBytes(StandardCharsets.UTF_8));
        LimitedStringBuilder output = new LimitedStringBuilder(256);
        final StringBuilder seen = new StringBuilder();

        Object lineConsumer = newLineConsumerProxy(line -> seen.append(line).append(","));
        Object errorConsumer = newErrorConsumerProxy(e -> seen.append("ERR:" + e.getMessage()));

        // Act
        invokePrivateReadStream(tools, in, StandardCharsets.UTF_8.name(), output, lineConsumer, errorConsumer);

        // Assert
        assertEquals("a,b,", seen.toString());
        assertEquals("a\n" + "b\n", output.getLastText());
    }

    @Test
    void append_whenTextIsNull_shouldReturnThisInstanceWithoutChanges() {
        // TestMate-6a9da4775c13c9aff019ce727aa99de3
        // Arrange
        LimitedStringBuilder builder = new LimitedStringBuilder(10);
        builder.append("init");
        int initialLength = builder.length();
        String initialText = builder.getLastText();
        // Act
        LimitedStringBuilder result = builder.append(null);
        // Assert
        assertSame(builder, result);
        assertEquals(initialLength, builder.length());
        assertEquals(initialText, builder.getLastText());
    }

    @Test
    void append_whenTotalLengthWithinMaxSize_shouldAppendTextAndNotTruncate() {
        // TestMate-5e40a18edb3a09676ef9d8710b6a1a99
        // Arrange
        LimitedStringBuilder builder = new LimitedStringBuilder(10);
        builder.append("abc");
        // Act
        LimitedStringBuilder result = builder.append("def");
        // Assert
        assertSame(builder, result);
        assertEquals(6, builder.length());
        assertEquals("abcdef", builder.getLastText());
        assertFalse(builder.getLastText().contains("truncated"));
    }

    @Test
    void append_whenTotalLengthExceedsMaxSize_shouldTrimStartAndSetTruncated() {
        // TestMate-936634bdef1c32db75a09bc56e913d4d
        // Arrange
        LimitedStringBuilder builder = new LimitedStringBuilder(5);
        builder.append("123");
        String textToAppend = "456";
        // Act
        LimitedStringBuilder result = builder.append(textToAppend);
        // Assert
        assertSame(builder, result);
        assertEquals(5, builder.length());
        String expectedText = "(Previous content has been truncated)...23456";
        assertEquals(expectedText, builder.getLastText());
        assertTrue(builder.getLastText().contains("truncated"));
    }

    @Test
    void append_whenInputTextLongerThanMaxSize_shouldRetainOnlyLastPart() {
        // TestMate-3d380dccd25d78fff746c035b766c98f
        // Arrange
        int maxSize = 3;
        LimitedStringBuilder builder = new LimitedStringBuilder(maxSize);
        String inputText = "abcdef";
        String expectedRetained = "def";
        String truncationPrefix = "(Previous content has been truncated)...";
        // Act
        LimitedStringBuilder result = builder.append(inputText);
        // Assert
        assertSame(builder, result);
        assertEquals(maxSize, builder.length());
        assertEquals(truncationPrefix + expectedRetained, builder.getLastText());
    }

    @Test
    void getLastText_whenNoTruncation_shouldReturnContentOnly() {
        // TestMate-79f40cd1dfbbe36d30cd4bc7c54c9250
        // Arrange
        int maxSize = 10;
        LimitedStringBuilder builder = new LimitedStringBuilder(maxSize);
        String testString = "Hello";
        builder.append(testString);
        // Act
        String result = builder.getLastText();
        // Assert
        assertEquals(testString, result);
        assertFalse(result.contains("(Previous content has been truncated)..."));
        assertEquals(testString.length(), builder.length());
    }

    @Test
    void getLastText_afterClear_shouldReturnEmptyStringWithoutPrefix() {
        // TestMate-7b0af228c7b970b3aba003e4371791ec
        // Arrange
        int maxSize = 5;
        LimitedStringBuilder builder = new LimitedStringBuilder(maxSize);
        builder.append("123456");
        String truncationPrefix = "(Previous content has been truncated)...";
        // Act
        builder.clear();
        String result = builder.getLastText();
        // Assert
        assertEquals("", result);
        assertFalse(result.contains(truncationPrefix));
        assertEquals(0, builder.length());
    }

    @Test
    void getLastText_whenEmpty_shouldReturnEmptyString() {
        // TestMate-a20efe6a40171d8636554950edff1306
        // Arrange
        int maxSize = 100;
        LimitedStringBuilder builder = new LimitedStringBuilder(maxSize);
        // Act
        String result = builder.getLastText();
        // Assert
        assertEquals("", result);
        assertEquals(0, builder.length());
    }

    @Test
    void getLastText_whenTruncatedAtExactBoundary_shouldReturnPrefixAndContent() {
        // TestMate-13939968f6f1556094af30c733ade00c
        // Arrange
        int maxSize = 3;
        LimitedStringBuilder builder = new LimitedStringBuilder(maxSize);
        String inputText = "1234";
        String expectedContent = "234";
        String truncationPrefix = "(Previous content has been truncated)...";
        String expectedFullText = truncationPrefix + expectedContent;
        // Act
        builder.append(inputText);
        String result = builder.getLastText();
        // Assert
        assertEquals(maxSize, builder.length());
        assertEquals(expectedFullText, result);
        assertTrue(result.startsWith(truncationPrefix));
    }

    private static void invokePrivateReadStream(CommandFunctionTools tools, java.io.InputStream is, String charsetName,
            LimitedStringBuilder output, Object lineConsumer, Object errorConsumer) throws Exception {
        Method m = CommandFunctionTools.class.getDeclaredMethod(
                "readStream",
                java.io.InputStream.class,
                String.class,
                LimitedStringBuilder.class,
                Class.forName("org.machanism.machai.ai.tools.CommandFunctionTools$LineConsumer"),
                Class.forName("org.machanism.machai.ai.tools.CommandFunctionTools$ErrorConsumer"));
        m.setAccessible(true);
        m.invoke(tools, is, charsetName, output, lineConsumer, errorConsumer);
    }

    private interface LineSink {
        void accept(String line);
    }

    private interface ErrorSink {
        void accept(IOException e);
    }

    private static Object newLineConsumerProxy(LineSink sink) throws Exception {
        Class<?> type = Class.forName("org.machanism.machai.ai.tools.CommandFunctionTools$LineConsumer");
        return java.lang.reflect.Proxy.newProxyInstance(
                CommandFunctionToolsReadStreamTest.class.getClassLoader(),
                new Class<?>[] { type },
                (proxy, method, args) -> {
                    if ("accept".equals(method.getName())) {
                        sink.accept((String) args[0]);
                        return null;
                    }
                    throw new UnsupportedOperationException(method.toString());
                });
    }

    private static Object newErrorConsumerProxy(ErrorSink sink) throws Exception {
        Class<?> type = Class.forName("org.machanism.machai.ai.tools.CommandFunctionTools$ErrorConsumer");
        return java.lang.reflect.Proxy.newProxyInstance(
                CommandFunctionToolsReadStreamTest.class.getClassLoader(),
                new Class<?>[] { type },
                (proxy, method, args) -> {
                    if ("accept".equals(method.getName())) {
                        sink.accept((IOException) args[0]);
                        return null;
                    }
                    throw new UnsupportedOperationException(method.toString());
                });
    }
}
