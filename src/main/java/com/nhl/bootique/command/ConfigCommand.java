package com.nhl.bootique.command;

import com.nhl.bootique.cli.CommandLine;
import com.nhl.bootique.cli.OptionsBuilder;

public class ConfigCommand implements Command {

	public static final String CONFIG_OPTION = "config";

	@Override
	public CommandOutcome run(CommandLine options) {
		return CommandOutcome.skipped();
	}

	@Override
	public void configOptions(OptionsBuilder optionsBuilder) {
		optionsBuilder.add(CONFIG_OPTION, "Specifies YAML config file path.").requiresArgument("config_file");
	}
}
