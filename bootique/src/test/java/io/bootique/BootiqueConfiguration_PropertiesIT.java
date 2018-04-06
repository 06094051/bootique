package io.bootique;

import io.bootique.config.ConfigurationFactory;
import io.bootique.unit.BQInternalTestFactory;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BootiqueConfiguration_PropertiesIT {

    @Rule
    public BQInternalTestFactory testFactory = new BQInternalTestFactory();

    private BQInternalTestFactory.Builder app() {
        return testFactory.app("--config=classpath:io/bootique/Bootique_Configuration_Properties_IT.yml");
    }

    @Test
    public void testOverride() {
        BQRuntime runtime = app()
                .property("bq.testOverride.c", "D")
                .createRuntime();

        TestOverrideBean b = runtime
                .getInstance(ConfigurationFactory.class)
                .config(TestOverrideBean.class, "testOverride");

        assertEquals("b", b.a);
        assertEquals("D", b.c);
    }

    @Test
    public void testConfig_OverrideWithProperties() {
        BQRuntime runtime = app()
                .property("bq.testOverrideNested.m.z", "2")
                .createRuntime();

        TestOverrideNestedBean b = runtime.getInstance(ConfigurationFactory.class)
                .config(TestOverrideNestedBean.class, "testOverrideNested");

        assertEquals("y", b.m.x);
        assertEquals(2, b.m.z);
    }

    static class TestOverrideBean {
        private String a;
        private String c;


        public void setA(String a) {
            this.a = a;
        }

        public void setC(String c) {
            this.c = c;
        }
    }

    static class TestOverrideNestedBean {
        private Bean4M m;

        public void setM(Bean4M m) {
            this.m = m;
        }
    }

    static class Bean4M {
        private String x;
        private int z;

        public void setX(String x) {
            this.x = x;
        }

        public void setZ(int z) {
            this.z = z;
        }
    }
}
