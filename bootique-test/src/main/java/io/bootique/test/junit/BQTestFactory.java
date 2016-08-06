package io.bootique.test.junit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import io.bootique.test.BQTestRuntime;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.ExternalResource;

import com.google.inject.multibindings.MapBinder;
import io.bootique.BQCoreModule;
import io.bootique.Bootique;
import io.bootique.config.ConfigurationFactory;

/**
 * Manages a simple Bootique stack within a lifecycle of the a JUnit test. It
 * doesn't run any commands by default and is usually used for accessing
 * initialized standard services, such as {@link ConfigurationFactory}, etc.
 * <p>
 * Instances should be annotated within the unit tests with {@link Rule} or
 * {@link ClassRule}. E.g.:
 * 
 * <pre>
 * public class MyTest {
 * 
 * 	&#64;Rule
 * 	public BQTestFactory testFactory = new BQTestFactory();
 * }
 * </pre>
 * 
 * @since 0.15
 */
public class BQTestFactory extends ExternalResource {

	private Collection<BQTestRuntime> runtimes;

	@Override
	protected void after() {
		Collection<BQTestRuntime> localRuntimes = this.runtimes;

		if (localRuntimes != null) {
			localRuntimes.forEach(runtime -> {
				try {
					runtime.stop();
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

		private Collection<BQTestRuntime> runtimes;
		private Consumer<Bootique> configurator;
		private Map<String, String> properties;

		private Builder(Collection<BQTestRuntime> runtimes) {
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

		public BQTestRuntime build(String... args) {

			Consumer<Bootique> localConfigurator = configurator;

			if (!properties.isEmpty()) {

				Consumer<Bootique> propsConfigurator = bootique -> bootique.module(binder -> {
					MapBinder<String, String> mapBinder = BQCoreModule.contributeProperties(binder);
					properties.forEach((k, v) -> mapBinder.addBinding(k).toInstance(v));
				});

				localConfigurator = localConfigurator.andThen(propsConfigurator);
			}

			BQTestRuntime runtime = new BQTestRuntime(localConfigurator, args);
			runtimes.add(runtime);
			return runtime;
		}
	}
}
