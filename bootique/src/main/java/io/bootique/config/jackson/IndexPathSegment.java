package io.bootique.config.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

/**
 * A path segment with remaining path being an array index.
 */
class IndexPathSegment extends PathSegment<ArrayNode> {

    // a symbolic index that allows to append values to array without knowing the length
    private static final String PAST_END_INDEX = ".length";

    protected IndexPathSegment(ArrayNode node, PathSegment parent, String incomingPath, String remainingPath) {
        super(node, parent, incomingPath, remainingPath);

        if (remainingPath != null) {

            if (remainingPath.length() < 3) {
                throw new IllegalArgumentException("The path must start with array index [NNN]. Instead got: " + remainingPath);
            }

            if (remainingPath.charAt(0) != ARRAY_INDEX_START) {
                throw new IllegalArgumentException("The path must start with array index [NNN]. Instead got: " + remainingPath);
            }
        }
    }

    @Override
    protected PathSegment parseNextNotEmpty(String path) {
        int len = path.length();

        // looking for ']' or '].'
        // start at index 1.. The first char is known to be '['
        for (int i = 1; i < len; i++) {
            char c = path.charAt(i);

            if (c == IndexPathSegment.ARRAY_INDEX_END) {

                // 1. [NNN]
                if (i == len - 1) {
                    return createValueChild(path.substring(0, i + 1));
                }
                // 2. [NNN].aaaa (i.e. in the second case the dot must follow closing paren)
                else if (path.charAt(i + 1) == PathSegment.DOT) {
                    return createPropertyChild(path.substring(0, i + 1), path.substring(i + 2));
                }
                // 3. [NNN][MMM] TODO => createIndexedChild
                // 4. Invalid path
                else {
                    throw new IllegalStateException("Invalid path after array index: " + path);
                }
            }
        }

        throw new IllegalStateException("No closing array index parenthesis: " + path);
    }

    @Override
    protected void fillMissingNodes(String field, JsonNode child, JsonNodeFactory nodeFactory) {

        if (node == null || node.isNull()) {
            node = new ArrayNode(nodeFactory);
            parent.fillMissingNodes(incomingPath, node, nodeFactory);
        }

        if (child != null) {
            writeChild(field, child);
        }
    }

    @Override
    JsonNode readChild(String childName) {
        return node != null ? node.get(toIndex(childName)) : null;
    }

    @Override
    void writeChild(String childName, String value) {
        JsonNode childNode = value == null ? node.nullNode() : node.textNode(value);
        writeChild(childName, childNode);
    }

    private void writeChild(String childName, JsonNode childNode) {
        int index = toIndex(childName);

        // allow replacing elements at index
        if (index < node.size()) {
            node.set(index, childNode);
        }
        // allow appending elements to the end of the array...
        else if (index == node.size()) {
            node.add(childNode);
        } else {
            throw new ArrayIndexOutOfBoundsException("Array index out of bounds: " + index + ". Size: " + node.size());
        }
    }

    protected int toIndex(String indexWithParenthesis) {

        if (indexWithParenthesis.length() < 3) {
            throw new IllegalArgumentException("Invalid array index. Must be in format [NNN]. Instead got " + indexWithParenthesis);
        }

        String indexString = indexWithParenthesis.substring(1, indexWithParenthesis.length() - 1);

        // format: [.length] or [NNN]

        if (PAST_END_INDEX.equals(indexString)) {
            return node.size();
        }

        int index;
        try {
            index = Integer.parseInt(indexString);
        } catch (NumberFormatException nfex) {
            throw new IllegalArgumentException("Non-int array index. Must be in format [NNN]. Instead got " + indexWithParenthesis);
        }

        if (index < 0) {
            throw new ArrayIndexOutOfBoundsException("Invalid negative array index: " + indexWithParenthesis);
        }

        return index;
    }
}
