package com.nhl.bootique.config;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

class JsonPropertiesResolver {

	static JsonNode findChild(JsonNode node, String path) {
		return lastPathComponent(node, path).map(t -> t.node).orElse(new ObjectNode(null));
	}

	static void resolve(JsonNode node, Map<String, String> properties) {
		properties.entrySet().forEach(e -> {

			PathTuple target = lastPathComponent(node, e.getKey()).get();
			target.fillMissingParents();

			if (!(target.parent.node instanceof ObjectNode)) {
				throw new IllegalArgumentException("Invalid property '" + e.getKey() + "'");
			}

			ObjectNode parentObjectNode = (ObjectNode) target.parent.node;
			parentObjectNode.put(target.incomingPath, e.getValue());
		});
	}

	static Optional<PathTuple> lastPathComponent(JsonNode node, String path) {
		return asStream(node, path).reduce((a, b) -> b);
	}

	private static Stream<PathTuple> asStream(JsonNode node, String path) {
		PathTuple head = new PathTuple(node, path);
		return StreamSupport.stream(head.spliterator(), false);
	}

	static class PathTuple implements Iterable<PathTuple> {

		String remainingPath;
		String incomingPath;
		JsonNode node;
		PathTuple parent;

		PathTuple(JsonNode node, String remainingPath) {
			this(node, null, null, remainingPath);
		}

		PathTuple(JsonNode node, PathTuple parent, String incomingPath, String remainingPath) {
			this.node = node;
			this.parent = parent;
			this.incomingPath = incomingPath;

			if (remainingPath != null && remainingPath.endsWith(".")) {
				remainingPath = remainingPath.substring(0, remainingPath.length() - 1);
			}
			this.remainingPath = remainingPath;
		}

		private PathTuple createNext() {
			if (remainingPath == null || remainingPath.length() == 0) {
				return null;
			}

			int dot = remainingPath.indexOf('.');
			String pre = dot > 0 ? remainingPath.substring(0, dot) : remainingPath;
			String post = dot > 0 ? remainingPath.substring(dot + 1) : "";

			JsonNode child = node != null ? node.get(pre) : null;
			return new PathTuple(child, this, pre, post);
		}

		void fillMissingParents() {
			parent.fillMissingNodes(incomingPath, node, new JsonNodeFactory(true));
		}

		void fillMissingNodes(String field, JsonNode child, JsonNodeFactory nodeFactory) {

			if (node == null) {
				node = new ObjectNode(nodeFactory);
				parent.fillMissingNodes(incomingPath, node, nodeFactory);
			}

			if (child != null) {
				if (node instanceof ObjectNode) {
					((ObjectNode) node).set(field, child);
				} else {
					throw new IllegalArgumentException(
							"Node '" + incomingPath + "' is unexpected in the middle of the path");
				}
			}
		}

		@Override
		public Iterator<PathTuple> iterator() {
			return new Iterator<PathTuple>() {

				private PathTuple current = PathTuple.this;
				private PathTuple next = current.createNext();

				@Override
				public boolean hasNext() {
					return current != null;
				}

				@Override
				public PathTuple next() {

					if (!hasNext()) {
						throw new NoSuchElementException("Past iterator end");
					}

					PathTuple r = current;
					current = next;
					next = current != null ? current.createNext() : null;
					return r;
				}
			};
		}
	}

}
