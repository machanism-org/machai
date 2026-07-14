package org.machanism.machai.ai.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.machanism.machai.ai.provider.Genai;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovers and applies {@link FunctionTools} implementations using Java's {@link ServiceLoader} mechanism.
 * <p>
 * This class acts as the entry point for registering a curated set of local capabilities (such as file access,
 * command execution, and HTTP retrieval) with a {@link Genai} provider. Implementations are discovered from the
 * classpath (typically via {@code META-INF/services} provider configuration) and then applied to the provider
 * in discovery order.
 * </p>
 *
 * <h2>Usage Example</h2>
 * <pre>
 * Genai provider = ...;
 * Class&lt;?&gt; appClass = ...;
 * FunctionToolsLoader loader = new FunctionToolsLoader();
 * loader.applyTools(provider, appClass);
 * </pre>
 *
 * <p>
 * The loader maintains a list of discovered {@link FunctionTools} instances and applies them to the provider,
 * filtered by application class compatibility.
 * </p>
 *
 * @author Viktor Tovstyi
 */
public class FunctionToolsLoader {

    /** Logger for debug output during tool discovery and application. */
    private static final Logger logger = LoggerFactory.getLogger(FunctionToolsLoader.class);

    /** List of discovered {@link FunctionTools} instances, loaded from the classpath. */
    private final List<FunctionTools> functionTools = new ArrayList<>();

    /**
     * Constructs a new {@code FunctionToolsLoader} and discovers available {@link FunctionTools}
     * implementations using {@link ServiceLoader}.
     * <p>
     * Each discovered tool is added to the internal list and logged for debugging.
     * </p>
     */
    public FunctionToolsLoader() {
        ServiceLoader<FunctionTools> functionToolServiceLoader = ServiceLoader.load(FunctionTools.class);
        for (FunctionTools functionTool : functionToolServiceLoader) {
            functionTools.add(functionTool);
            logger.debug("Discovered FunctionTools: {}", functionTool.getClass().getName());
        }
    }

    /**
     * Applies all discovered {@link FunctionTools} installers to the given provider,
     * filtered by application class compatibility.
     * <p>
     * For each compatible tool, both tool functions and prompts are registered with the provider.
     * </p>
     *
     * @param provider the {@link Genai} provider instance to augment with tool functions
     * @param appClass the application class requesting tool assignment; only tools compatible with this class are applied
     * @throws IllegalArgumentException if a discovered installer cannot be instantiated
     */
    public void applyTools(Genai provider, Class<?> appClass) {
        for (FunctionTools functionTool : functionTools) {
            Class<? extends FunctionTools> functionToolsClass = functionTool.getClass();
            boolean supported = isSupportedFor(appClass, functionToolsClass);

            if (supported) {
                provider.addTools(functionTool);
                provider.addPrompts(functionTool);
				provider.addResources(functionTool);
            }
        }
    }

    /**
     * Checks whether the given {@link FunctionTools} implementation supports assignment to the specified application class.
     * <p>
     * If the {@link SupportedFor} annotation is present, only classes listed in its value are considered compatible.
     * If the annotation is absent, compatibility is assumed.
     * </p>
     *
     * @param appClass           the application class requesting tool assignment
     * @param functionToolsClass the FunctionTools implementation class
     * @return {@code true} if the tool is compatible with the application class, {@code false} otherwise
     */
    private boolean isSupportedFor(Class<?> appClass, Class<? extends FunctionTools> functionToolsClass) {
        SupportedFor supportedApplications = functionToolsClass.getAnnotation(SupportedFor.class);
        if (supportedApplications != null) {
            for (Class<?> supportedClass : supportedApplications.value()) {
                if (supportedClass.isAssignableFrom(appClass)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }
}