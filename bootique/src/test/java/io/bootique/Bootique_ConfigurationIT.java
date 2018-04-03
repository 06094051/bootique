package io.bootique;

import io.bootique.config.ConfigurationFactory;
import io.bootique.meta.application.OptionMetadata;
import io.bootique.type.TypeRef;
import io.bootique.unit.BQInternalTestFactory;
import org.junit.Rule;
import org.junit.Test;

import java.util.Map;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

public class Bootique_ConfigurationIT {

    @Rule
    public BQInternalTestFactory runtimeFactory = new BQInternalTestFactory();

    @Test
    public void testEmptyConfig() {
        BQRuntime runtime = runtimeFactory.app("--config=src/test/resources/io/bootique/empty.yml").createRuntime();

        Map<String, String> config = runtime.getInstance(ConfigurationFactory.class)
                .config(new TypeRef<Map<String, String>>() {
                }, "");
        assertEquals("{}", config.toString());
    }

    @Test
    public void testConfigEmptyConfig() {
        BQRuntime runtime = runtimeFactory.app("--config=src/test/resources/io/bootique/test1.yml",
                "--config=src/test/resources/io/bootique/empty.yml").createRuntime();

        Map<String, String> config = runtime.getInstance(ConfigurationFactory.class)
                .config(new TypeRef<Map<String, String>>() {
                }, "");
        assertEquals("{a=b}", config.toString());
    }

    @Test
    public void testDIConfig() {

        BQRuntime runtime = runtimeFactory.app()
                .module(b -> BQCoreModule.extend(b)
                        .addConfig("classpath:io/bootique/diconfig1.yml")
                        .addConfig("classpath:io/bootique/diconfig2.yml"))
                .createRuntime();

        Map<String, String> config = runtime.getInstance(ConfigurationFactory.class)
                .config(new TypeRef<Map<String, String>>() {
                }, "");
        assertEquals("{a=1, b=2, c=3}", config.toString());
    }

    @Test
    public void testDIConfig_VsCliOrder() {

        BQRuntime runtime = runtimeFactory.app("-c", "classpath:io/bootique/cliconfig.yml")
                .module(b -> BQCoreModule.extend(b)
                        .addConfig("classpath:io/bootique/diconfig1.yml")
                        .addConfig("classpath:io/bootique/diconfig2.yml"))
                .createRuntime();

        Map<String, Integer> config = runtime.getInstance(ConfigurationFactory.class)
                .config(new TypeRef<Map<String, Integer>>() {
                }, "");
        assertEquals("{a=5, b=2, c=6}", config.toString());
    }

    @Test
    public void testDIOnOptionConfig() {

        Function<String, String> configReader =
                arg -> {
                    BQRuntime runtime = runtimeFactory.app(arg)
                            .module(b -> BQCoreModule.extend(b)
                                    .addConfigOnOption("opt", "classpath:io/bootique/diconfig1.yml")
                                    .addConfigOnOption("opt", "classpath:io/bootique/diconfig2.yml")
                                    .addOption(OptionMetadata.builder("opt").build()))
                            .createRuntime();

                    Map<String, Integer> config =
                            runtime.getInstance(ConfigurationFactory.class)
                                    .config(new TypeRef<Map<String, Integer>>() {
                                    }, "");

                    return config.toString();
                };

        assertEquals("{}", configReader.apply(""));
        assertEquals("{a=1, b=2, c=3}", configReader.apply("--opt"));
    }

    @Test
    public void testDIOnOptionConfig_OverrideWithOption() {

        Function<String, String> configReader =
                arg -> {
                    BQRuntime runtime = runtimeFactory.app(arg)
                            .module(b -> BQCoreModule.extend(b)
                                    .addConfigOnOption("opt", "classpath:io/bootique/diconfig1.yml")
                                    .addConfigOnOption("opt", "classpath:io/bootique/diconfig2.yml")
                                    .addOption("a", "opt"))
                            .createRuntime();

                    return runtime.getInstance(ConfigurationFactory.class)
                            .config(new TypeRef<Map<String, Integer>>() {
                            }, "").toString();
                };

        assertEquals("{}", configReader.apply(""));
        assertEquals("{a=8, b=2, c=3}", configReader.apply("--opt=8"));
    }

    @Test
    public void testConfigConfig() {
        BQRuntime runtime = runtimeFactory.app("--config=src/test/resources/io/bootique/test1.yml",
                "--config=src/test/resources/io/bootique/test2.yml").createRuntime();

        Map<String, String> config = runtime.getInstance(ConfigurationFactory.class)
                .config(new TypeRef<Map<String, String>>() {
                }, "");
        assertEquals("{a=e, c=d}", config.toString());
    }

    @Test
    public void testConfigConfig_Reverse() {
        BQRuntime runtime = runtimeFactory.app("--config=src/test/resources/io/bootique/test2.yml",
                "--config=src/test/resources/io/bootique/test1.yml").createRuntime();

        Map<String, String> config = runtime.getInstance(ConfigurationFactory.class)
                .config(new TypeRef<Map<String, String>>() {
                }, "");
        assertEquals("{a=b, c=d}", config.toString());
    }

    @Test
    public void testConfigEnvOverrides_Alias() {
        BQRuntime runtime = runtimeFactory.app("--config=src/test/resources/io/bootique/test3.yml")
                .declareVar("a", "V1")
                .declareVar("c.m.f", "V2")
                .declareVar("c.m.k", "V3")
                .var("V1", "K")
                .var("V2", "K1")
                .var("V3", "4")
                .createRuntime();

        Bean1 b1 = runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, "");

        assertEquals("K", b1.a);
        assertEquals(4, b1.c.m.k);
        assertEquals("n", b1.c.m.l);
        assertEquals("K1", b1.c.m.f);
    }

    static class Bean1 {
        private String a;
        private Bean2 c;

        public void setA(String a) {
            this.a = a;
        }

        public void setC(Bean2 c) {
            this.c = c;
        }
    }

    static class Bean2 {

        private Bean3 m;

        public void setM(Bean3 m) {
            this.m = m;
        }
    }

    static class Bean3 {
        private int k;
        private String f;
        private String l;

        public void setK(int k) {
            this.k = k;
        }

        public void setF(String f) {
            this.f = f;
        }

        public void setL(String l) {
            this.l = l;
        }
    }

    static class Bean4 {
        private Map<String, String> m;

        public void setM(Map<String, String> m) {
            this.m = m;
        }
    }
}
