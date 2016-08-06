package io.bootique.config.jackson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.bootique.config.jackson.JsonNodeYamlParser;

public class JsonNodeYamlParserTest {

	@Test
	public void testApply() {

		InputStream in = new ByteArrayInputStream("a: b\nb: c".getBytes());
		ObjectMapper mapper = new ObjectMapper();

		JsonNode node = new JsonNodeYamlParser(mapper).apply(in);
		assertNotNull(node);

		assertEquals("b", node.get("a").asText());
		assertEquals("c", node.get("b").asText());
	}

}
