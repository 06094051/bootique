package io.bootique.command;

import com.google.inject.Module;
import io.bootique.BQCoreModule;
import io.bootique.cli.Cli;
import io.bootique.meta.application.CommandMetadata;
import io.bootique.meta.application.OptionMetadata;
import io.bootique.unit.BQInternalTestFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CommandDecoratorIT {

    @Rule
    public BQInternalTestFactory testFactory = new BQInternalTestFactory();

    private ExecutableOnceCommand mainCommand;
    private SuccessfulCommand successfulCommand;
    private FailingCommand failingCommand;

    @Before
    public void before() {
        this.mainCommand = new ExecutableOnceCommand("a");
        this.successfulCommand = new SuccessfulCommand();
        this.failingCommand = new FailingCommand();
    }

    @Test
    public void testAlsoRun_Instance() {

        Command cmd = mock(Command.class);
        when(cmd.run(any())).thenReturn(CommandOutcome.succeeded());

        new AppRunner(CommandDecorator.alsoRun(cmd)).runAndWaitExpectingSuccess();

        assertTrue(mainCommand.isExecuted());
        verify(cmd).run(any(Cli.class));
    }

    @Test
    public void testAlsoRun_NameRef() {

        CommandDecorator decorator = CommandDecorator.alsoRun("s");

        new AppRunner(decorator).runAndWaitExpectingSuccess();
        assertTrue(successfulCommand.isExecuted());
        assertFalse(successfulCommand.hasFlagOption());
    }

    @Test
    public void testAlsoRun_NameRef_WithArgs() {

        CommandDecorator decorator = CommandDecorator.alsoRun("s", "--sflag");

        new AppRunner(decorator).runAndWaitExpectingSuccess();
        assertTrue(successfulCommand.isExecuted());
        assertTrue(successfulCommand.hasFlagOption());
    }

    @Test
    public void testAlsoRun_TypeRef() {

        CommandDecorator decorator = CommandDecorator.alsoRun(SuccessfulCommand.class);

        new AppRunner(decorator).runAndWaitExpectingSuccess();
        assertTrue(successfulCommand.isExecuted());
        assertFalse(successfulCommand.hasFlagOption());
    }

    @Test
    public void testAlsoRun_TypeRef_WithArgs() {

        CommandDecorator decorator = CommandDecorator.alsoRun(SuccessfulCommand.class, "--sflag");

        new AppRunner(decorator).runAndWaitExpectingSuccess();
        assertTrue(successfulCommand.isExecuted());
        assertTrue(successfulCommand.hasFlagOption());
    }

    @Test
    public void testBeforeRun_Instance() {

        Command cmd = mock(Command.class);
        when(cmd.run(any())).thenReturn(CommandOutcome.succeeded());

        CommandDecorator decorator = CommandDecorator.beforeRun(cmd);

        new AppRunner(decorator).runExpectingSuccess();
        verify(cmd).run(any(Cli.class));
    }

    @Test
    public void testBeforeRun_Failure_NameRef() {

        CommandDecorator decorator = CommandDecorator.beforeRun("f");

        new AppRunner(decorator).runExpectingFailure();
    }

    @Test
    public void testBeforeRun_Failure_NameRef_WithArgs() {

        CommandDecorator decorator = CommandDecorator.beforeRun("f", "--fflag");

        new AppRunner(decorator).runExpectingFailure();
        assertTrue(failingCommand.hasFlagOption());
    }

    @Test
    public void testBeforeRun_Failure_TypeRef() {
        CommandDecorator decorator = CommandDecorator.beforeRun(FailingCommand.class);

        new AppRunner(decorator).runExpectingFailure();
    }

    @Test
    public void testBeforeRun_Failure_TypeRef_WithArgs() {
        CommandDecorator decorator = CommandDecorator.beforeRun(FailingCommand.class, "--fflag");
        new AppRunner(decorator).runExpectingFailure();

        assertTrue(failingCommand.hasFlagOption());
    }

    @Test
    public void testBeforeAndAlsoRun() {

        Command c1 = mock(Command.class);
        when(c1.run(any())).thenReturn(CommandOutcome.succeeded());

        Command c2 = mock(Command.class);
        when(c2.run(any())).thenReturn(CommandOutcome.succeeded());

        Command c3 = mock(Command.class);
        when(c3.run(any())).thenReturn(CommandOutcome.succeeded());

        Command c4 = mock(Command.class);
        when(c4.run(any())).thenReturn(CommandOutcome.succeeded());

        CommandDecorator decorator = CommandDecorator
                .builder()
                .beforeRun(c1).beforeRun(c2)
                .alsoRun(c3).alsoRun(c4)
                .build();

        new AppRunner(decorator).runAndWaitExpectingSuccess();
        assertTrue(mainCommand.isExecuted());
        verify(c1).run(any(Cli.class));
        verify(c2).run(any(Cli.class));
        verify(c3).run(any(Cli.class));
        verify(c4).run(any(Cli.class));
    }

    private static class SuccessfulCommand extends ExecutableOnceCommand {

        private static final String NAME = "s";
        private static final String FLAG_OPT = "sflag";

        SuccessfulCommand() {
            super(NAME, FLAG_OPT);
        }

        public boolean hasFlagOption() {
            return cliRef.get().hasOption(FLAG_OPT);
        }
    }

    private static class FailingCommand extends ExecutableOnceCommand {

        private static final String NAME = "f";
        private static final String FLAG_OPT = "fflag";

        FailingCommand() {
            super(NAME, FLAG_OPT);
        }

        void assertFailure(CommandOutcome outcome) {
            assertTrue(isExecuted());
            assertFalse(outcome.isSuccess());
            assertNull(outcome.getException());
            assertEquals("Some of the commands failed", outcome.getMessage());
        }

        public boolean hasFlagOption() {
            return cliRef.get().hasOption(FLAG_OPT);
        }

        @Override
        public CommandOutcome run(Cli cli) {
            super.run(cli);
            return CommandOutcome.failed(1, NAME);
        }
    }

    private static class ExecutableOnceCommand extends CommandWithMetadata {

        protected final AtomicReference<Cli> cliRef;

        public ExecutableOnceCommand(String commandName) {
            this(commandName, Optional.empty());
        }

        public ExecutableOnceCommand(String commandName, String flagOption) {
            this(commandName, Optional.of(flagOption));
        }

        private ExecutableOnceCommand(String commandName, Optional<String> flagOption) {
            super(buildMetadata(commandName, flagOption));
            this.cliRef = new AtomicReference<>();
        }

        private static CommandMetadata buildMetadata(String commandName, Optional<String> flagOption) {
            CommandMetadata.Builder builder = CommandMetadata.builder(commandName);
            flagOption.ifPresent(o -> builder.addOption(OptionMetadata.builder(o)));
            return builder.build();
        }

        @Override
        public CommandOutcome run(Cli cli) {
            if (!cliRef.compareAndSet(null, cli)) {
                throw new IllegalStateException("Already executed");
            }

            return CommandOutcome.succeeded();
        }

        public boolean isExecuted() {
            return cliRef.get() != null;
        }
    }

    private class AppRunner {
        private CommandDecorator decorator;
        private Module module;

        public AppRunner(CommandDecorator decorator) {
            this.decorator = decorator;
        }

        public AppRunner module(Module module) {
            this.module = module;
            return this;
        }

        public void runExpectingSuccess() {
            assertTrue(decorateAndRun().isSuccess());
            assertTrue(mainCommand.isExecuted());
        }

        public void runAndWaitExpectingSuccess() {
            assertTrue(decorateRunAndWait().isSuccess());
            assertTrue(mainCommand.isExecuted());
        }

        public void runExpectingFailure() {
            assertFalse(decorateAndRun().isSuccess());
            assertFalse(mainCommand.isExecuted());
        }

        public void runAndWaitExpectingFailure() {
            assertFalse(decorateRunAndWait().isSuccess());
            assertFalse(mainCommand.isExecuted());
        }

        private CommandOutcome decorateAndRun() {
            BQInternalTestFactory.Builder builder = testFactory
                    .app("--a")
                    .module(b -> BQCoreModule.extend(b)
                            .addCommand(mainCommand)
                            .addCommand(successfulCommand)
                            .addCommand(failingCommand)
                            .decorateCommand(mainCommand.getClass(), decorator));

            if (module != null) {
                builder.module(module);
            }

            return builder.createRuntime().run();
        }

        private CommandOutcome decorateRunAndWait() {

            CommandOutcome outcome = decorateAndRun();

            // wait for the parallel commands to finish
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            return outcome;
        }

    }
}
