package org.machanism.machai.project.layout;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.gradle.tooling.model.DomainObjectSet;
import org.gradle.tooling.model.GradleProject;

final class StubGradleProjectFactory {

	private StubGradleProjectFactory() {
	}

	static GradleProject project(String name, DomainObjectSet<? extends GradleProject> children) {
		InvocationHandler handler = new GradleProjectInvocationHandler(name, children);
		return (GradleProject) Proxy.newProxyInstance(
				GradleProject.class.getClassLoader(),
				new Class<?>[] { GradleProject.class },
				handler);
	}

	@SafeVarargs
	static DomainObjectSet<GradleProject> domainObjectSet(GradleProject... projects) {
		List<GradleProject> list = projects == null ? Collections.<GradleProject>emptyList() : Arrays.asList(projects);
		return domainObjectSet(list);
	}

	static DomainObjectSet<GradleProject> domainObjectSet(List<GradleProject> projects) {
		return (DomainObjectSet<GradleProject>) Proxy.newProxyInstance(
				DomainObjectSet.class.getClassLoader(),
				new Class<?>[] { DomainObjectSet.class },
				new DomainObjectSetInvocationHandler<GradleProject>(projects));
	}

	private static final class GradleProjectInvocationHandler implements InvocationHandler {
		private final String name;
		private final DomainObjectSet<? extends GradleProject> children;

		private GradleProjectInvocationHandler(String name, DomainObjectSet<? extends GradleProject> children) {
			this.name = name;
			this.children = children;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) {
			String methodName = method.getName();
			if ("getName".equals(methodName)) {
				return name;
			}
			if ("getChildren".equals(methodName)) {
				return children;
			}
			if ("toString".equals(methodName)) {
				return "GradleProject[name=" + name + "]";
			}
			if ("hashCode".equals(methodName)) {
				return System.identityHashCode(proxy);
			}
			if ("equals".equals(methodName)) {
				return proxy == args[0];
			}
			throw new UnsupportedOperationException("Method not supported in test stub: " + method);
		}
	}

	private static final class DomainObjectSetInvocationHandler<T> implements InvocationHandler {
		private final List<T> items;

		private DomainObjectSetInvocationHandler(List<T> items) {
			this.items = items;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) {
			String methodName = method.getName();
			if ("isEmpty".equals(methodName)) {
				return items.isEmpty();
			}
			if ("getAll".equals(methodName)) {
				return items;
			}
			if ("iterator".equals(methodName)) {
				return items.iterator();
			}
			if ("size".equals(methodName)) {
				return items.size();
			}
			if ("toString".equals(methodName)) {
				return "DomainObjectSet" + items;
			}
			if ("hashCode".equals(methodName)) {
				return System.identityHashCode(proxy);
			}
			if ("equals".equals(methodName)) {
				return proxy == args[0];
			}
			throw new UnsupportedOperationException("Method not supported in test stub: " + method);
		}
	}
}
