package io.bootique.command;

import io.bootique.BQModuleProvider;
import io.bootique.BQRuntime;
import io.bootique.Bootique;
import io.bootique.application.CommandMetadata;
import io.bootique.cli.Cli;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CommandsIT {

	private String[] args;

	@Before
	public void before() {
		args = new String[] { "a", "b", "c" };
	}

	@Test
	public void testModuleCommands() {
		BQModuleProvider provider = Commands.builder().build();
		BQRuntime runtime = Bootique.app(args).module(provider).createRuntime();
		CommandManager commandManager = runtime.getInstance(CommandManager.class);

		Collection<Command> commands = commandManager.getCommands();
		assertEquals(1, commands.size());

		Map<String, List<Command>> map = mapCommands(commands);
		assertTrue(map.containsKey(HelpCommand.class.getName()));
	}

	@Test
	public void testNoModuleCommands() {
		BQModuleProvider provider = Commands.builder().noModuleCommands().build();
		BQRuntime runtime = Bootique.app(args).module(provider).createRuntime();
		CommandManager commandManager = runtime.getInstance(CommandManager.class);
		assertTrue(commandManager.getCommands().isEmpty());
	}

	@Test
	public void testModule_ExtraCommandAsType() {
		BQModuleProvider provider = Commands.builder(C1.class).build();
		BQRuntime runtime = Bootique.app(args).module(provider).createRuntime();
		CommandManager commandManager = runtime.getInstance(CommandManager.class);

		Collection<Command> commands = commandManager.getCommands();
		assertEquals(2, commands.size());

		Map<String, List<Command>> map = mapCommands(commands);
		assertTrue(map.containsKey(C1.class.getName()));
		assertTrue(map.containsKey(HelpCommand.class.getName()));
	}

	@Test
	public void testModule_ExtraCommandAsInstance() {
		BQModuleProvider provider = Commands.builder().add(new C1()).build();
		BQRuntime runtime = Bootique.app(args).module(provider).createRuntime();
		CommandManager commandManager = runtime.getInstance(CommandManager.class);

		Collection<Command> commands = commandManager.getCommands();
		assertEquals(2, commands.size());

		Map<String, List<Command>> map = mapCommands(commands);
		assertTrue(map.containsKey(C1.class.getName()));
		assertTrue(map.containsKey(HelpCommand.class.getName()));
	}

	@Test
	public void testModule_ExtraCommandOverride() {
		BQModuleProvider provider = Commands.builder().add(C2_Help.class).build();
		BQRuntime runtime = Bootique.app(args).module(provider).createRuntime();
		CommandManager commandManager = runtime.getInstance(CommandManager.class);

		Collection<Command> commands = commandManager.getCommands();
		assertEquals(1, commands.size());

		Map<String, List<Command>> map = mapCommands(commands);
		assertTrue(map.containsKey(C2_Help.class.getName()));
	}

	private Map<String, List<Command>> mapCommands(Collection<Command> commands) {

		Map<String, List<Command>> map = new HashMap<>();
		commands.forEach(c -> {
			map.computeIfAbsent(c.getClass().getName(), s -> new ArrayList<>()).add(c);
		});

		return map;
	}

	static class C1 implements Command {
		@Override
		public CommandOutcome run(Cli cli) {
			return CommandOutcome.succeeded();
		}
	}

	static class C2_Help implements Command {
		@Override
		public CommandOutcome run(Cli cli) {
			return CommandOutcome.succeeded();
		}

		@Override
		public CommandMetadata getMetadata() {
			return CommandMetadata.builder("help").build();
		}
	}
}
