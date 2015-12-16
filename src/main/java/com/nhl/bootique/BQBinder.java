package com.nhl.bootique;

import java.util.Arrays;
import java.util.Collection;

import com.google.common.base.Preconditions;
import com.google.inject.Binder;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.nhl.bootique.command.Command;
import com.nhl.bootique.env.EnvironmentProperties;

/**
 * A helper class that allows to contribute commands and properties to Bootique
 * runtime. Create and invoke this binder insider your Module's "configure"
 * method to provide your own properties and/or Commands.
 * 
 * @since 0.8
 */
public class BQBinder {

	public static BQBinder contributeTo(Binder binder) {
		return new BQBinder(binder);
	}

	private Binder binder;

	BQBinder(Binder binder) {
		this.binder = binder;
	}

	MapBinder<String, String> propsBinder() {
		return MapBinder.newMapBinder(binder, String.class, String.class, EnvironmentProperties.class);
	}

	/**
	 * Utility method to contribute custom environment properties to DI.
	 */
	public void property(String key, String value) {
		propsBinder().addBinding(key).toInstance(value);
	}

	/**
	 * Utility method to contribute custom commands to DI.
	 */
	@SafeVarargs
	public final void commandTypes(Class<? extends Command>... commands) {
		commandTypes(Arrays.asList(Preconditions.checkNotNull(commands)));
	}

	/**
	 * Utility method to contribute custom commands to DI.
	 */
	public void commandTypes(Collection<Class<? extends Command>> commands) {
		Multibinder<Command> commandBinder = Multibinder.newSetBinder(binder, Command.class);
		Preconditions.checkNotNull(commands).forEach(ct -> commandBinder.addBinding().to(ct));
	}

	/**
	 * Utility method to contribute custom commands to DI.
	 */
	public void commands(Collection<? extends Command> commands) {
		Multibinder<Command> commandBinder = Multibinder.newSetBinder(binder, Command.class);
		Preconditions.checkNotNull(commands).forEach(c -> commandBinder.addBinding().toInstance(c));
	}
}
