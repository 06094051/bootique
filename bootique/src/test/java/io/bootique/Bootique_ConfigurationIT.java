package io.bootique;

import io.bootique.config.ConfigurationFactory;
import io.bootique.type.TypeRef;
import io.bootique.unit.BQInternalTestFactory;
import org.junit.Rule;
import org.junit.Test;

import java.util.Map;

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
    public void testConfigEnvOverrides() {
        BQRuntime runtime = runtimeFactory.app("--config=src/test/resources/io/bootique/test2.yml").var("BQ_A", "F")
                .createRuntime();

        Map<String, String> config = runtime.getInstance(ConfigurationFactory.class)
                .config(new TypeRef<Map<String, String>>() {
                }, "");

        assertEquals("{a=F, c=d}", config.toString());
    }

    @Test
    public void testConfigEnvOverrides_Nested() {
        BQRuntime runtime = runtimeFactory.app("--config=src/test/resources/io/bootique/test3.yml")
                .var("BQ_A", "F")
                .var("BQ_C_M_F", "F1").var("BQ_C_M_K", "3")
                .createRuntime();

        Bean1 b1 = runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, "");

        assertEquals("F", b1.a);
        assertEquals(3, b1.c.m.k);
        assertEquals("n", b1.c.m.l);
        assertEquals("F1", b1.c.m.f);
    }

    @Test
    public void testConfigEnvOverrides_Alias() {
        BQRuntime runtime = runtimeFactory.app("--config=src/test/resources/io/bootique/test3.yml")
                .varAlias("BQ_A", "V1")
                .varAlias("BQ_C_M_F", "V2")
                .varAlias("BQ_C_M_K", "V3")
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

}
