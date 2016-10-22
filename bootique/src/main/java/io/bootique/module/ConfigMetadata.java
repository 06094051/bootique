package io.bootique.module;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Descriptor of a configuration object.
 *
 * @since 0.21
 */
public class ConfigMetadata extends ConfigPropertyMetadata {

    private Collection<ConfigPropertyMetadata> properties;

    public ConfigMetadata() {
        this.properties = new ArrayList<>();
    }

    public static Builder builder() {
        return new Builder(new ConfigMetadata());
    }

    @Override
    public <T> T accept(ConfigMetadataVisitor<T> visitor) {
        return visitor.visitConfigMetadata(this);
    }

    public Collection<ConfigPropertyMetadata> getProperties() {
        return properties;
    }

    public static class Builder extends ConfigPropertyMetadata.Builder<ConfigMetadata, Builder> {

        public Builder(ConfigMetadata toBuild) {
            super(toBuild);
        }

        public Builder addProperty(ConfigPropertyMetadata property) {
            toBuild.properties.add(property);
            return this;
        }

        public Builder addProperties(Collection<? extends ConfigPropertyMetadata> properties) {
            toBuild.properties.addAll(properties);
            return this;
        }
    }
}
