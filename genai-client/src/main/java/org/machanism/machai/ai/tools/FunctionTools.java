package org.machanism.machai.ai.tools;

import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.GenAIProvider;

public interface FunctionTools {

	void applyTools(GenAIProvider provider);

	default void setConfigurator(Configurator configurator) {
	};

}
