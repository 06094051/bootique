package com.nhl.bootique.unit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.junit.rules.ExternalResource;

import com.google.inject.multibindings.MapBinder;
import com.nhl.bootique.BQCoreModule;
import com.nhl.bootique.BQRuntime;
import com.nhl.bootique.Bootique;
import com.nhl.bootique.log.DefaultBootLogger;

public class BQInternalTestFactory extends ExternalResource {

	private Collection<BQRuntime> runtimes;

	@Override
	protected void after() {
		Collection<BQRuntime> localRuntimes = this.runtimes;

		if (localRuntimes != null) {
			localRuntimes.forEach(runtime -> {
				try {
					runtime.shutdown();
				} catch (Exception e) {
					// ignore...
				}
			});
		}
	}

	@Override
	protected void before() {
		this.runtimes = new ArrayList<>();
	}

	public Builder newRuntime() {
		return new Builder(runtimes);
	}

	public static class Builder {

		private static final Consumer<Bootique> DO_NOTHING_CONFIGURATOR = bootique -> {
		};

		private Collection<BQRuntime> runtimes;
		private Consumer<Bootique> configurator;
		private Map<String, String> properties;

		private Builder(Collection<BQRuntime> runtimes) {
			this.runtimes = runtimes;
			this.properties = new HashMap<>();
			this.configurator = DO_NOTHING_CONFIGURATOR;
		}

		public Builder property(String key, String value) {
			properties.put(key, value);
			return this;
		}

		public Builder configurator(Consumer<Bootique> configurator) {
			this.configurator = Objects.requireNonNull(configurator);
			return this;
		}

		public BQRuntime build(String... args) {

			Consumer<Bootique> localConfigurator = configurator;

			if (!properties.isEmpty()) {

				Consumer<Bootique> propsConfigurator = bootique -> bootique.module(binder -> {
					MapBinder<String, String> mapBinder = BQCoreModule.contributeProperties(binder);
					properties.forEach((k, v) -> mapBinder.addBinding(k).toInstance(v));
				});

				localConfigurator = localConfigurator.andThen(propsConfigurator);
			}

			Bootique bootique = Bootique.app(args).bootLogger(new DefaultBootLogger(true));
			localConfigurator.accept(bootique);
			BQRuntime runtime = bootique.createRuntime();

			runtimes.add(runtime);
			return runtime;
		}
	}
}
