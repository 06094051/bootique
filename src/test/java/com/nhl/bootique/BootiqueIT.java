package com.nhl.bootique;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.nhl.bootique.annotation.Args;
import com.nhl.bootique.it.ItestModuleProvider;

public class BootiqueIT {

	private String[] args;

	@Before
	public void before() {
		args = new String[] { "a", "b", "c" };
	}

	@Test
	public void testAutoLoadedProviders() {
		Collection<BQModuleProvider> autoLoaded = Bootique.app(args).autoLoadedProviders();

		assertEquals(1, autoLoaded.size());
		autoLoaded.forEach(m -> assertTrue(m instanceof ItestModuleProvider));
	}

	@Test
	public void testCreateInjector() {
		Injector i = Bootique.app(args).createInjector();

		String[] args = i.getInstance(Key.get(String[].class, Args.class));
		assertSame(this.args, args);
	}

	@Test
	public void testCreateInjector_Overrides() {
		Injector i = Bootique.app(args).override(BQCoreModule.class).with(M0.class).createInjector();

		String[] args = i.getInstance(Key.get(String[].class, Args.class));
		assertSame(M0.ARGS, args);
	}

	@Test
	public void testCreateInjector_Overrides_Multi_Level() {
		Injector i = Bootique.app(args).override(BQCoreModule.class).with(M0.class).override(M0.class).with(M1.class)
				.createInjector();

		String[] args = i.getInstance(Key.get(String[].class, Args.class));
		assertSame(M1.ARGS, args);
	}

	static class M0 implements Module {

		static String[] ARGS = { "1", "2", "3" };

		@Override
		public void configure(Binder binder) {
			binder.bind(String[].class).annotatedWith(Args.class).toInstance(ARGS);
		}
	}

	static class M1 implements Module {

		static String[] ARGS = { "x", "y", "z" };

		@Override
		public void configure(Binder binder) {
			binder.bind(String[].class).annotatedWith(Args.class).toInstance(ARGS);
		}
	}
}
