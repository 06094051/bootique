package com.nhl.bootique;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.nhl.bootique.log.BootLogger;

public class ModuleMergerTest {

	private BootLogger mockLogger;

	private List<BQModuleProvider> mockProviders;
	private List<Module> testModules;

	@Before
	public void before() {
		this.mockLogger = mock(BootLogger.class);

		// module types are used as keys in Bootique, so lets' define a bunch of
		// distinct types without using mocks
		this.testModules = Arrays.asList(new M0(), new M1(), new M2(), new M3(), new M4());

		this.mockProviders = new ArrayList<>();
		testModules.forEach(m -> {
			mockProviders.add(createProvider(m, null));
		});
	}

	private BQModuleProvider createProvider(Module m, Class<? extends Module> replaces) {
		BQModuleProvider providerMock = mock(BQModuleProvider.class);
		when(providerMock.module()).thenReturn(m);
		when(providerMock.replaces()).thenReturn(Optional.ofNullable(replaces));
		return providerMock;
	}

	@Test
	public void testGetModules_Empty() {
		assertTrue(new ModuleMerger(Collections.emptyList(), mockLogger).getModules().isEmpty());
	}

	@Test
	public void testGetModules_One() {

		Collection<BQModuleProvider> providers = Arrays.asList(mockProviders.get(2));

		Collection<Module> modules = new ModuleMerger(providers, mockLogger).getModules();
		assertEquals(1, modules.size());

		assertTrue(modules.contains(testModules.get(2)));
	}

	@Test
	public void testGetModules_Two() {

		Collection<BQModuleProvider> providers = Arrays.asList(mockProviders.get(2), mockProviders.get(1));

		Collection<Module> modules = new ModuleMerger(providers, mockLogger).getModules();
		assertEquals(2, modules.size());

		assertTrue(modules.contains(testModules.get(1)));
		assertTrue(modules.contains(testModules.get(2)));
	}

	@Test
	public void testGetModules_Three_Dupes() {

		Collection<BQModuleProvider> providers = Arrays.asList(mockProviders.get(2), mockProviders.get(1),
				mockProviders.get(2));

		Collection<Module> modules = new ModuleMerger(providers, mockLogger).getModules();
		assertEquals(2, modules.size());

		assertTrue(modules.contains(testModules.get(1)));
		assertTrue(modules.contains(testModules.get(2)));
	}

	@Test
	public void testGetModules_Two_Replaces() {

		mockProviders.set(0, createProvider(testModules.get(0), M3.class));

		Collection<BQModuleProvider> providers = Arrays.asList(mockProviders.get(0), mockProviders.get(3));
		Collection<Module> modules = new ModuleMerger(providers, mockLogger).getModules();
		assertEquals(1, modules.size());

		assertTrue(modules.contains(testModules.get(0)));
	}

	class M0 implements Module {

		@Override
		public void configure(Binder binder) {
			// noop
		}
	}

	class M1 implements Module {

		@Override
		public void configure(Binder binder) {
			// noop
		}
	}

	class M2 implements Module {

		@Override
		public void configure(Binder binder) {
			// noop
		}
	}

	class M3 implements Module {

		@Override
		public void configure(Binder binder) {
			// noop
		}
	}

	class M4 implements Module {

		@Override
		public void configure(Binder binder) {
			// noop
		}
	}
}
