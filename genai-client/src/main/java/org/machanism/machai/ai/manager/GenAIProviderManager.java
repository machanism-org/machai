package org.machanism.machai.ai.manager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang.StringUtils;
import org.machanism.machai.ai.none.NoneProvider;

public class GenAIProviderManager {

	public static GenAIProvider getProvider(String chatModel) {
		String providerName = StringUtils.substringBefore(chatModel, ":");
		String chatModelName = StringUtils.substringAfter(chatModel, ":");

		if (StringUtils.isBlank(chatModelName)) {
			chatModelName = providerName;
			providerName = NoneProvider.NAME;
		}

		String className;
		if (StringUtils.contains(providerName, '.')) {
			className = providerName;
		} else {
			String packageName = providerName.substring(0, 1).toLowerCase() + providerName.substring(1);
			className = String.format("org.machanism.machai.ai.%s.%sProvider", packageName, providerName);
		}

		GenAIProvider provider;
		try {
			@SuppressWarnings("unchecked")
			Class<? extends GenAIProvider> providerClass = (Class<? extends GenAIProvider>) Class.forName(className);
			Constructor<? extends GenAIProvider> constructor = providerClass.getConstructor();
			provider = constructor.newInstance();
			provider.model(chatModelName);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| ClassNotFoundException | NoSuchMethodException | SecurityException e) {
			throw new IllegalArgumentException("GenAI provider: " + providerName + " is not supported.", e);
		}

		return provider;
	}

}
