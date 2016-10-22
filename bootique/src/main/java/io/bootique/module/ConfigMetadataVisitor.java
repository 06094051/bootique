package io.bootique.module;

/**
 * @since 0.21
 */
public interface ConfigMetadataVisitor<T> {

    default T visitConfigMetadata(ConfigMetadata metadata) {
        return null;
    }

    default T visitConfigPropertyMetadata(ConfigPropertyMetadata metadata) {
        return null;
    }
}
