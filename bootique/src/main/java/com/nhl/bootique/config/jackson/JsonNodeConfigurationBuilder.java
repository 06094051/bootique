package com.nhl.bootique.config.jackson;

import java.net.URL;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A helper that orchestrates configuration loading.
 * 
 * @since 0.17
 */
public class JsonNodeConfigurationBuilder {

	private Supplier<Stream<URL>> resourceStreamSupplier;
	private Function<URL, JsonNode> parser;
	private BinaryOperator<JsonNode> merger;
	private Function<JsonNode, JsonNode> overrider;

	public static JsonNodeConfigurationBuilder builder() {
		return new JsonNodeConfigurationBuilder();
	}

	protected JsonNodeConfigurationBuilder() {
	}

	public JsonNodeConfigurationBuilder resources(Supplier<Stream<URL>> streamSupplier) {
		this.resourceStreamSupplier = streamSupplier;
		return this;
	}

	public JsonNodeConfigurationBuilder overrider(Function<JsonNode, JsonNode> overrider) {
		this.overrider = overrider;
		return this;
	}

	public JsonNodeConfigurationBuilder parser(Function<URL, JsonNode> parser) {
		this.parser = parser;
		return this;
	}

	public JsonNodeConfigurationBuilder merger(BinaryOperator<JsonNode> merger) {
		this.merger = merger;
		return this;
	}

	public JsonNode build() {

		Objects.requireNonNull(resourceStreamSupplier);
		Objects.requireNonNull(parser);
		Objects.requireNonNull(merger);

		JsonNode rootNode;

		try (Stream<URL> sources = resourceStreamSupplier.get()) {

			rootNode = sources.map(parser::apply).reduce(merger)
					.orElseGet(() -> new ObjectNode(new JsonNodeFactory(true)));
		}

		return overrider.apply(rootNode);
	}
}
