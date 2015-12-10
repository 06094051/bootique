package com.nhl.launcher;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.nhl.launcher.command.Command;
import com.nhl.launcher.command.ConfigCommand;
import com.nhl.launcher.command.DefaultCommand;
import com.nhl.launcher.command.FailoverHelpCommand;
import com.nhl.launcher.command.HelpCommand;
import com.nhl.launcher.config.CliConfigurationSource;
import com.nhl.launcher.config.ConfigurationSource;
import com.nhl.launcher.config.FactoryConfigurationService;
import com.nhl.launcher.config.YamlFactoryConfigurationService;
import com.nhl.launcher.env.DefaultEnvironment;
import com.nhl.launcher.env.Environment;
import com.nhl.launcher.env.EnvironmentProperties;
import com.nhl.launcher.jackson.DefaultJacksonService;
import com.nhl.launcher.jackson.JacksonService;
import com.nhl.launcher.jopt.Args;
import com.nhl.launcher.jopt.Options;
import com.nhl.launcher.jopt.OptionsProvider;

public class BootstrapModule implements Module {

	public static final String COMMANDS_KEY = "com.nhl.launcher.commands";
	private String[] args;

	public static MapBinder<String, String> propertiesBinder(Binder binder) {
		return MapBinder.newMapBinder(binder, String.class, String.class, EnvironmentProperties.class);
	}

	public BootstrapModule(String[] args) {
		this.args = args;
	}

	@Override
	public void configure(Binder binder) {

		binder.bind(JacksonService.class).to(DefaultJacksonService.class);
		binder.bind(String[].class).annotatedWith(Args.class).toInstance(args);
		binder.bind(Runner.class).to(DefaultRunner.class).in(Singleton.class);
		binder.bind(Options.class).toProvider(OptionsProvider.class).in(Singleton.class);
		binder.bind(ConfigurationSource.class).to(CliConfigurationSource.class).in(Singleton.class);
		binder.bind(FactoryConfigurationService.class).to(YamlFactoryConfigurationService.class);
		binder.bind(Environment.class).to(DefaultEnvironment.class);

		binder.bind(Command.class).annotatedWith(DefaultCommand.class).to(FailoverHelpCommand.class)
				.in(Singleton.class);

		Multibinder<Command> commands = Multibinder.newSetBinder(binder, Command.class);

		commands.addBinding().to(HelpCommand.class);
		commands.addBinding().to(ConfigCommand.class);

		// don't bind anything to properties yet, but declare the binding
		propertiesBinder(binder);
	}
}
