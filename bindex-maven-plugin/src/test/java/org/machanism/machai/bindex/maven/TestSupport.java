package org.machanism.machai.bindex.maven;

import java.lang.reflect.Field;

final class TestSupport {
	private TestSupport() {
	}

	static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
		Field f = findField(target.getClass(), fieldName);
		f.setAccessible(true);
		f.set(target, value);
	}

	private static Field findField(Class<?> type, String fieldName) throws NoSuchFieldException {
		Class<?> current = type;
		while (current != null) {
			try {
				return current.getDeclaredField(fieldName);
			} catch (NoSuchFieldException ignored) {
				current = current.getSuperclass();
			}
		}
		throw new NoSuchFieldException(fieldName);
	}
}
