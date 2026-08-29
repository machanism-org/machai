package org.machanism.machai.mcp.maven.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

class MCPServerToolsTest {

    @Test
    void defaultDelayWaitsForTheConfiguredGracePeriod() throws Exception {
        long started = System.nanoTime();

        new MCPServerTools().waitBeforeExit();

        assertTrue(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started) >= 900);
    }

    @Test
    void defaultLogUsageCompletes() {
        assertDoesNotThrow(() -> new MCPServerTools().logUsage());
    }


    @Test
    void stopMcpServerReturnsImmediatelyAndShutsDownWithRequestedCode() throws Exception {
        TestTools tools = new TestTools(false);

        String response = tools.stopMcpServer(7);

        assertEquals("MCP server shutdown initiated.", response);
        assertTrue(tools.exitCalled.await(2, TimeUnit.SECONDS));
        assertEquals(7, tools.exitCode);
        assertTrue(tools.usageLogged);
    }

    @Test
    void stopMcpServerStillLogsUsageAndExitsWhenDelayIsInterrupted() throws Exception {
        TestTools tools = new TestTools(true);

        tools.stopMcpServer(3);

        assertTrue(tools.exitCalled.await(2, TimeUnit.SECONDS));
        assertEquals(3, tools.exitCode);
        assertTrue(tools.usageLogged);
        assertTrue(tools.shutdownThreadInterrupted);
    }

    private static final class TestTools extends MCPServerTools {
        private final boolean interruptDelay;
        private final CountDownLatch exitCalled = new CountDownLatch(1);
        private volatile boolean usageLogged;
        private volatile boolean shutdownThreadInterrupted;
        private volatile int exitCode = Integer.MIN_VALUE;

        private TestTools(boolean interruptDelay) {
            this.interruptDelay = interruptDelay;
        }

        @Override protected void waitBeforeExit() throws InterruptedException {
            if (interruptDelay) {
                throw new InterruptedException("test interruption");
            }
        }

        @Override protected void logUsage() {
            usageLogged = true;
            shutdownThreadInterrupted = Thread.currentThread().isInterrupted();
        }
        @Override protected void exit(int code) {
            exitCode = code;
            exitCalled.countDown();
        }
    }
}
