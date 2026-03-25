package org.machanism.machai.ai.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

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
