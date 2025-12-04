package org.machanism.machai.core;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.machanism.machai.core.embedding.EmbeddingBuilder;
import org.machanism.machai.core.embedding.EmbeddingProvider;
import org.machanism.machai.schema.BIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BindexRegister extends ScanProject implements Closeable {

	private static Logger logger = LoggerFactory.getLogger(BindexRegister.class);

	private EmbeddingProvider embeddingProvider;

	private boolean overwrite;

	public BindexRegister() {
		super();
		embeddingProvider = new EmbeddingProvider("machanism", "bindex");
	}

	public String processProject(File projectDir) {
		BIndex bindex;
		try {
			bindex = getBindex(projectDir);

			String regId = null;
			if (bindex != null) {
				Document document = embeddingProvider.getDocument(bindex);
				if (document == null || overwrite) {
					EmbeddingBuilder embeddingBuilder = new EmbeddingBuilder().provider(embeddingProvider);
					regId = embeddingBuilder.bindex(bindex).build();
					logger.info("embeddingId: " + regId);
				} else {
					regId = ((ObjectId) document.get("_id")).toString();
				}
			}

			return regId;
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public void close() throws IOException {
		embeddingProvider.close();
	}

	public BindexRegister overwrite(boolean overwrite) {
		this.overwrite = overwrite;
		return this;
	}

}
