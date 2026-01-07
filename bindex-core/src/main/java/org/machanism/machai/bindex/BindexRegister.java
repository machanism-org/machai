package org.machanism.machai.bindex;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import org.machanism.machai.ai.manager.GenAIProvider;
import org.machanism.machai.project.layout.ProjectLayout;
import org.machanism.machai.schema. Bindex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BindexRegister handles registration and update of  Bindex documents for given projects,
 * leveraging Picker for registration id management.
 * <p>
 * Usage Example:
 * <pre>
 *     try(BindexRegister register = new BindexRegister(provider)) {
 *         register.processFolder(layout);
 *     }
 * </pre>
 *
 * This class supports registration, ID lookup, and document update via Picker.
 *
 * @author Viktor Tovstyi
 * @since 0.0.2
 * @see  BindexProjectProcessor
 */
public class BindexRegister extends  BindexProjectProcessor implements Closeable {

    /** Logger instance for the BindexRegister class. */
    private static Logger logger = LoggerFactory.getLogger(BindexRegister.class);

    /** Picker instance for registration id management. */
    private Picker picker;

    /** Update flag to force registration document updates. */
    private boolean update;

    /**
     * Constructs a BindexRegister with specified GenAIProvider.
     *
     * @param provider GenAIProvider used for Picker instantiation
     */
    public BindexRegister(GenAIProvider provider) {
        super();
        picker = new Picker(provider);
    }

    /**
     * Processes folder registration using the provided project layout, optionally updating IDs.
     *
     * @param projectLayout ProjectLayout to process
     * @throws IllegalArgumentException If an IO error occurs during registration
     */
    public void processFolder(ProjectLayout projectLayout) {
         Bindex bindex;
        try {
            File projectDir = projectLayout.getProjectDir();
            bindex = getBindex(projectDir);

            String regId = null;
            if (bindex != null) {
                regId = picker.getRegistredId(bindex);
                if (regId == null || update) {
                    regId = picker.create(bindex);
                    logger.info("Registration id: {}", regId);
                }
            }

        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Closes the Picker resource.
     *
     * @throws IOException If Picker close fails
     */
    @Override
    public void close() throws IOException {
        picker.close();
    }

    /**
     * Sets the update mode for registration, enabling overwrite if true.
     *
     * @param overwrite Whether to force update registration
     * @return This BindexRegister instance for chained calls
     */
    public BindexRegister update(boolean overwrite) {
        this.update = overwrite;
        return this;
    }

}
