package io.bootique.env;

import io.bootique.log.BootLogger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

/**
 * An {@link Environment} implementation that reads properties and variables from the Map passed in constructor.
 */
public class DefaultEnvironment implements Environment {

    /**
     * If present, enables boot sequence tracing to STDERR.
     */
    public static final String TRACE_PROPERTY = "bq.trace";

    private Map<String, String> properties;

    @Deprecated
    private Map<String, String> variables;

    public static Builder withSystemPropertiesAndVariables(BootLogger logger) {
        return new Builder(logger).includeSystemProperties().includeSystemVariables();
    }

    protected DefaultEnvironment() {
    }

    @Override
    public String getProperty(String name) {
        return properties.get(name);
    }

    @Override
    public Map<String, String> subproperties(String prefix) {
        return filterByPrefix(properties, prefix, ".");
    }

    @Override
    public String getVariable(String name) {
        return variables.get(name);
    }

    @Override
    public Map<String, String> variables(String prefix) {
        return filterByPrefix(variables, prefix, "_");
    }

    protected Map<String, String> filterByPrefix(Map<String, String> unfiltered, String prefix, String separator) {
        String lPrefix = prefix.endsWith(separator) ? prefix : prefix + separator;
        int len = lPrefix.length();

        return unfiltered.entrySet().stream().filter(e -> e.getKey().startsWith(lPrefix))
                .collect(toMap(e -> e.getKey().substring(len), e -> e.getValue()));
    }

    public static class Builder {
        private Map<String, String> diProperties;
        private Map<String, String> diVariables;
        private Collection<DeclaredVariable> declaredVariables;
        private boolean includeSystemProperties;
        private boolean includeSystemVariables;
        private BootLogger logger;

        private Builder(BootLogger logger) {
            this.logger = logger;
        }

        public DefaultEnvironment build() {

            DefaultEnvironment env = new DefaultEnvironment();

            env.properties = buildProperties();
            env.variables = buildVariables();

            return env;
        }

        public Builder includeSystemProperties() {
            includeSystemProperties = true;
            return this;
        }

        public Builder includeSystemVariables() {
            includeSystemVariables = true;
            return this;
        }

        public Builder diProperties(Map<String, String> diProperties) {
            this.diProperties = diProperties;
            return this;
        }

        public Builder diVariables(Map<String, String> diVariables) {
            this.diVariables = diVariables;
            return this;
        }

        public Builder declaredVariables(Collection<DeclaredVariable> declaredVariables) {
            this.declaredVariables = declaredVariables;
            return this;
        }

        protected Map<String, String> buildProperties() {

            Map<String, String> properties = new HashMap<>();

            // order of config overrides
            // 1. DI properties
            // 2. System properties
            // 3. DI declared vars
            // 4. System declared vars

            if (this.diProperties != null) {
                properties.putAll(this.diProperties);
            }

            if (includeSystemProperties) {
                // override DI props from system...
                System.getProperties().forEach((k, v) -> properties.put((String) k, (String) v));
            }

            declaredVariables.forEach(dv -> mergeValue(dv, properties, diVariables));

            if(includeSystemVariables) {
                Map<String, String> systemVars = System.getenv();
                declaredVariables.forEach(dv -> mergeValue(dv, properties, systemVars));
            }

            return properties;
        }

        private void mergeValue(DeclaredVariable dv, Map<String, String> properties, Map<String, String> vars) {
            String value = vars.get(dv.getName());
            if(value != null) {
                properties.put(dv.getConfigPath(), value);
            }
        }

        @Deprecated
        protected Map<String, String> buildVariables() {

            Map<String, String> allVars = new HashMap<>();

            diVariables.keySet().forEach(this::warnOfDeprecatedVar);
            allVars.putAll(this.diVariables);

            if (includeSystemVariables) {
                Map<String, String> systemVars = System.getenv();
                systemVars.keySet().forEach(this::warnOfDeprecatedVar);
                allVars.putAll(systemVars);
            }

            return allVars;
        }

        //  Will go away when we stop supporting BQ_ vars completely.
        @Deprecated
        private void warnOfDeprecatedVar(String var) {

            if (var.startsWith(FRAMEWORK_VARIABLES_PREFIX)) {
                logger.stderr(("WARN: The use of BQ_* variables is deprecated. Consider declaring '"
                        + var
                        + "' explicitly using an app-specific name."));
            }
        }
    }
}
