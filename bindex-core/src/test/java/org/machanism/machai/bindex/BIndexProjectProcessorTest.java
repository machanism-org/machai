package org.machanism.machai.bindex;

import org.junit.jupiter.api.Test;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.schema.BIndex;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.fasterxml.jackson.databind.ObjectMapper;

class BIndexProjectProcessorTest {

    static class TestProcessor extends BIndexProjectProcessor {

		@Override
		public void processProject(ProjectLayout processor) {
			// TODO Auto-generated method stub
			
		}}

    @Test
    void getBindexReturnsNullWhenFileDoesNotExist() {
        TestProcessor processor = new TestProcessor();
        File tempDir = new File("/tmp/nonexistent");
        BIndex bindex = processor.getBindex(tempDir);
        assertNull(bindex);
    }

    @Test
    void getBindexReturnsBIndexWhenFileExists() throws Exception {
        TestProcessor processor = new TestProcessor();
        File dir = new File(System.getProperty("java.io.tmpdir"));
        File bindexFile = new File(dir, BIndexProjectProcessor.BINDEX_FILE_NAME);
        BIndex testBIndex = mock(BIndex.class);
        new ObjectMapper().writeValue(bindexFile, testBIndex);
        BIndex result = processor.getBindex(dir);
        assertNotNull(result);
        bindexFile.delete();
    }

    @Test
    void getBindexFileReturnsCorrectFile() {
        TestProcessor processor = new TestProcessor();
        File dir = new File("/tmp/testdir");
        File file = processor.getBindexFile(dir);
        assertEquals(new File(dir, BIndexProjectProcessor.BINDEX_FILE_NAME), file);
    }
}
