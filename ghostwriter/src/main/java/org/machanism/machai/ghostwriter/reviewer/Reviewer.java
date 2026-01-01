package org.machanism.machai.ghostwriter.reviewer;

import java.io.File;
import java.io.IOException;

public interface Reviewer {

	String perform(File projectDir, File file) throws IOException;

}
