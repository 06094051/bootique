package com.nhl.bootique;

import static java.util.stream.Collectors.joining;

import java.util.Arrays;
import java.util.Objects;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.ProvisionException;
import com.nhl.bootique.annotation.Args;
import com.nhl.bootique.command.CommandOutcome;
import com.nhl.bootique.log.BootLogger;
import com.nhl.bootique.run.Runner;
import com.nhl.bootique.shutdown.ShutdownManager;

import joptsimple.OptionException;

/**
 * A wrapper around launcher DI container.
 */
public class BQRuntime {

	private Injector injector;

	public BQRuntime(Injector injector) {
		this.injector = injector;
	}

	/**
	 * Returns a DI-bound instance of a given class, throwing if this class is
	 * not explicitly bound in DI.
	 * 
	 * @since 0.12
	 * @param type
	 *            a type of instance object to return
	 * @return a DI-bound instance of a given class.
	 */
	public <T> T getInstance(Class<T> type) {
		Binding<T> binding = injector.getExistingBinding(Key.get(type));

		// note that Guice default behavior is to attempt creating a binding on
		// the fly, if there's no explicit one available. We are overriding this
		// behavior.
		return Objects.requireNonNull(binding, "No binding for type: " + type).getProvider().get();
	}

	public BootLogger getBootLogger() {
		return getInstance(BootLogger.class);
	}

	public Runner getRunner() {
		return getInstance(Runner.class);
	}

	public String[] getArgs() {
		return injector.getInstance(Key.get(String[].class, Args.class));
	}

	public String getArgsAsString() {
		return Arrays.asList(getArgs()).stream().collect(joining(" "));
	}

	/**
	 * Registers a JVM shutdown hook that is delegated to
	 * {@link ShutdownManager}.
	 * 
	 * @since 0.11
	 */
	public void addJVMShutdownHook() {

		// resolve all Injector services needed for shutdown eagerly and outside
		// shutdown thread to ensure that shutdown hook will not fail due to
		// misconfiguration, etc.

		ShutdownManager shutdownManager = injector.getInstance(ShutdownManager.class);
		BootLogger logger = getBootLogger();

		Runtime.getRuntime().addShutdownHook(new Thread("bootique-shutdown") {

			@Override
			public void run() {
				shutdown(shutdownManager, logger);
			}
		});
	}

	/**
	 * Executes Bootique runtime shutdown, allowing all interested DI services
	 * to perform cleanup.
	 * 
	 * @since 0.12
	 */
	public void shutdown() {
		ShutdownManager shutdownManager = injector.getInstance(ShutdownManager.class);
		BootLogger logger = getBootLogger();

		shutdown(shutdownManager, logger);
	}

	protected void shutdown(ShutdownManager shutdownManager, BootLogger logger) {
		shutdownManager.shutdown().forEach((s, th) -> {
			logger.stderr(String.format("Error performing shutdown of '%s': %s", s.getClass().getSimpleName(),
					th.getMessage()));
		});
	}

	/**
	 * @deprecated since 0.12 use either {@link Bootique#run()} or
	 *             {@link BQRuntime#getRunner()}.
	 * @return the outcome of executing the runner.
	 */
	@Deprecated
	public CommandOutcome run() {
		try {
			return getRunner().run();
		}
		// handle startup Guice exceptions
		catch (ProvisionException e) {
			return (e.getCause() instanceof OptionException) ? CommandOutcome.failed(1, e.getCause().getMessage())
					: CommandOutcome.failed(1, e);
		}
	}

}
