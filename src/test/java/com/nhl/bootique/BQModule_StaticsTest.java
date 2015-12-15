package com.nhl.bootique;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.nhl.bootique.env.EnvironmentProperties;

public class BQModule_StaticsTest {

	@Test
	public void testBindProperty() {
		Injector i = Guice.createInjector(b -> {
			BQModule.bindProperty(b, "a", "b");
			BQModule.bindProperty(b, "c", "d");

			b.bind(MapInspector.class);
		});

		MapInspector inspector = i.getInstance(MapInspector.class);

		assertEquals("b", inspector.map.get("a"));
		assertEquals("d", inspector.map.get("c"));
	}

	static class MapInspector {

		Map<String, String> map;

		@Inject
		public MapInspector(@EnvironmentProperties Map<String, String> map) {
			this.map = map;
		}
	}
}
