package com.nhl.bootique.env;

import static java.util.stream.Collectors.toMap;

import java.util.HashMap;
import java.util.Map;

import com.google.inject.Inject;

public class DefaultEnvironment implements Environment {

	public static final String FRAMEWORK_PROPERTIES_PREFIX = "bq";

	private Map<String, String> properties;

	@Inject
	public DefaultEnvironment(@EnvironmentProperties Map<String, String> diProperties) {
		this.properties = new HashMap<>(diProperties);

		// override DI props from system...
		System.getProperties().entrySet().stream()
				.forEach(e -> properties.put((String) e.getKey(), (String) e.getValue()));
	}

	@Override
	public String getProperty(String name) {
		return properties.get(name);
	}

	@Override
	public Map<String, String> subproperties(String prefix) {

		String lPrefix = prefix.endsWith(".") ? prefix : prefix + ".";
		int len = lPrefix.length();

		return properties.entrySet().stream().filter(e -> e.getKey().startsWith(lPrefix))
				.collect(toMap(e -> e.getKey().substring(len), e -> e.getValue()));
	}

	@Override
	public Map<String, String> frameworkProperties() {
		return subproperties(FRAMEWORK_PROPERTIES_PREFIX);
	}
}
