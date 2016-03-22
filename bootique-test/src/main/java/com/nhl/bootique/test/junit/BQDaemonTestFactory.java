package com.nhl.bootique.test.junit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.ExternalResource;

import com.google.inject.multibindings.MapBinder;
import com.nhl.bootique.BQCoreModule;
import com.nhl.bootique.BQRuntime;
import com.nhl.bootique.Bootique;
import com.nhl.bootique.test.BQDaemonTestRuntime;

/**
 * Manages a "daemon" Bootique stack within a lifecycle of the a JUnit test.
 * This allows to start background servers so that tests can execute requests
 * against them, etc.
 * <p>
 * Instances should be annotated within the unit tests with {@link Rule} or
 * {@link ClassRule}. E.g.:
 * 
 * <pre>
 * public class MyTest {
 * 
 * 	&#64;Rule
 * 	public BQDaemonTestFactory testFactory = new BQDaemonTestFactory();
 * }
 * </pre>
 * 
 * @since 0.15
 */
public class BQDaemonTestFactory extends ExternalResource {

	private Collection<BQDaemonTestRuntime> runtimes;

	@Override
	protected void after() {
		Collection<BQDaemonTestRuntime> localRuntimes = this.runtimes;

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

		private static final Function<BQRuntime, Boolean> AFFIRMATIVE_STARTUP_CHECK = runtime -> true;

		private Collection<BQDaemonTestRuntime> runtimes;
		private Function<BQRuntime, Boolean> startupCheck;
		private Consumer<Bootique> configurator;
		private Map<String, String> properties;
		private long startupTimeout;
		private TimeUnit startupTimeoutTimeUnit;

		private Builder(Collection<BQDaemonTestRuntime> runtimes) {

			this.startupTimeout = 5;
			this.startupTimeoutTimeUnit = TimeUnit.SECONDS;
			this.runtimes = runtimes;
			this.properties = new HashMap<>();
			this.configurator = DO_NOTHING_CONFIGURATOR;
			this.startupCheck = AFFIRMATIVE_STARTUP_CHECK;
		}

		public Builder property(String key, String value) {
			properties.put(key, value);
			return this;
		}

		/**
		 * Appends configurator to any existing configurators.
		 * 
		 * @param configurator
		 *            configurator function.
		 * @return this builder.
		 */
		public Builder configurator(Consumer<Bootique> configurator) {
			Objects.requireNonNull(configurator);
			this.configurator = this.configurator != null ? this.configurator.andThen(configurator) : configurator;
			return this;
		}

		public Builder startupCheck(Function<BQRuntime, Boolean> startupCheck) {
			this.startupCheck = Objects.requireNonNull(startupCheck);
			return this;
		}

		public Builder startupTimeout(long timeout, TimeUnit unit) {
			this.startupTimeout = timeout;
			this.startupTimeoutTimeUnit = unit;
			return this;
		}

		/**
		 * Starts the test app in a background thread.
		 * 
		 * @param args
		 *            String[] emulating command-line arguments passed to a Java
		 *            app.
		 */
		public void start(String... args) {

			Consumer<Bootique> localConfigurator = configurator;

			if (!properties.isEmpty()) {

				Consumer<Bootique> propsConfigurator = bootique -> bootique.module(binder -> {
					MapBinder<String, String> mapBinder = BQCoreModule.contributeProperties(binder);
					properties.forEach((k, v) -> mapBinder.addBinding(k).toInstance(v));
				});

				localConfigurator = localConfigurator.andThen(propsConfigurator);
			}

			BQDaemonTestRuntime runtime = new BQDaemonTestRuntime(localConfigurator, startupCheck);
			runtimes.add(runtime);
			runtime.start(startupTimeout, startupTimeoutTimeUnit, args);
		}
	}
}
