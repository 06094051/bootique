package io.bootique.jopt;

import io.bootique.cli.Cli;
import io.bootique.log.BootLogger;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import javax.swing.*;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * {@link Cli} implementation on top of {@link JOptionPane} library.
 */
public class JoptCli implements Cli {

	private OptionParser parser;
	private OptionSet optionSet;
	private BootLogger bootLogger;
	private String commandName;

	public JoptCli(BootLogger bootLogger, OptionParser parser, OptionSet parsed, String commandName) {
		this.parser = parser;
		this.optionSet = parsed;
		this.bootLogger = bootLogger;
		this.commandName = commandName;
	}

	@Override
	public String commandName() {
		return commandName;
	}

	@Override
	public boolean hasOption(String optionName) {
		return optionSet.has(optionName);
	}

	@Override
	public List<String> optionStrings(String name) {
		return optionSet.valuesOf(name).stream().map(o -> String.valueOf(o)).collect(toList());
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> standaloneArguments() {
		return (List<String>) optionSet.nonOptionArguments();
	}
}
