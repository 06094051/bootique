package com.nhl.launcher;

import static java.util.stream.Collectors.joining;

import java.util.Arrays;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.ProvisionException;
import com.nhl.launcher.command.CommandOutcome;
import com.nhl.launcher.jopt.Args;

import joptsimple.OptionException;

/**
 * A wrapper around launcher DI container.
 */
class LauncherRuntime {

	private Injector injector;

	public LauncherRuntime(Injector injector) {
		this.injector = injector;
	}

	public Runner getRunner() {
		return injector.getInstance(Runner.class);
	}

	public String[] getArgs() {
		return injector.getInstance(Key.get(String[].class, Args.class));
	}

	public String getArgsAsString() {
		return Arrays.asList(getArgs()).stream().collect(joining(" "));
	}

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
