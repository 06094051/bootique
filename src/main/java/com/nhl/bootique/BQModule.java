package com.nhl.bootique;

import java.util.Arrays;

import com.google.common.base.Preconditions;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.nhl.bootique.command.Command;
import com.nhl.bootique.command.ConfigCommand;
import com.nhl.bootique.command.DefaultCommand;
import com.nhl.bootique.command.FailoverHelpCommand;
import com.nhl.bootique.command.HelpCommand;
import com.nhl.bootique.config.CliConfigurationSource;
import com.nhl.bootique.config.ConfigurationSource;
import com.nhl.bootique.env.DefaultEnvironment;
import com.nhl.bootique.env.Environment;
import com.nhl.bootique.env.EnvironmentProperties;
import com.nhl.bootique.factory.FactoryConfigurationService;
import com.nhl.bootique.factory.YamlFactoryConfigurationService;
import com.nhl.bootique.jackson.DefaultJacksonService;
import com.nhl.bootique.jackson.JacksonService;
import com.nhl.bootique.jopt.Args;
import com.nhl.bootique.jopt.Options;
import com.nhl.bootique.jopt.OptionsProvider;
import com.nhl.bootique.log.BootLogger;
import com.nhl.bootique.log.DefaultBootLogger;
import com.nhl.bootique.run.DefaultRunner;
import com.nhl.bootique.run.Runner;

public class BQModule implements Module {

	private String[] args;

	private static MapBinder<String, String> propsBinder(Binder binder) {
		return MapBinder.newMapBinder(binder, String.class, String.class, EnvironmentProperties.class);
	}

	/**
	 * Utility method for the bundle modules to bind their own default
	 * properties.
	 */
	public static void bindProperty(Binder binder, String key, String value) {
		propsBinder(binder).addBinding(key).toInstance(key);
	}

	/**
	 * Utility method for the bundle modules to bind their own default
	 * properties.
	 */
	@SafeVarargs
	public static void bindCommands(Binder binder, Class<? extends Command>... commands) {
		Multibinder<Command> commandBinder = Multibinder.newSetBinder(binder, Command.class);
		Arrays.asList(Preconditions.checkNotNull(commands)).forEach(ct -> commandBinder.addBinding().to(ct));
	}

	public BQModule(String[] args) {
		this.args = args;
	}

	@Override
	public void configure(Binder binder) {

		binder.bind(BootLogger.class).to(DefaultBootLogger.class);
		binder.bind(JacksonService.class).to(DefaultJacksonService.class);
		binder.bind(String[].class).annotatedWith(Args.class).toInstance(args);
		binder.bind(Runner.class).to(DefaultRunner.class).in(Singleton.class);
		binder.bind(Options.class).toProvider(OptionsProvider.class).in(Singleton.class);
		binder.bind(ConfigurationSource.class).to(CliConfigurationSource.class).in(Singleton.class);
		binder.bind(FactoryConfigurationService.class).to(YamlFactoryConfigurationService.class);
		binder.bind(Environment.class).to(DefaultEnvironment.class);

		binder.bind(Command.class).annotatedWith(DefaultCommand.class).to(FailoverHelpCommand.class)
				.in(Singleton.class);

		BQModule.bindCommands(binder, HelpCommand.class, ConfigCommand.class);

		// don't bind anything to properties yet, but still declare the binding
		BQModule.propsBinder(binder);
	}
}
