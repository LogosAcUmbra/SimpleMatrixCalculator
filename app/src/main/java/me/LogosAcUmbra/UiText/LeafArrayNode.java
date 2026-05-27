package me.LogosAcUmbra.UiText;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class LeafArrayNode extends ExistingNode {
    @NonNull LeafNode @NonNull [] lines;
    @Nullable String txtCache;

    protected LeafArrayNode(
            JsonNode rawNode, int indentLev, int parentTotalIndentLev,
            @NonNull LeafNode @NonNull [] lines
    ) {
        super(rawNode, indentLev, parentTotalIndentLev);
        this.lines = lines;
    }

    public static @NonNull LeafArrayNode of(
            JsonNode rawNode
    ) {
        return of(rawNode, 0, 0);
    }

    public static @NonNull LeafArrayNode of(
            JsonNode rawNode, int parentTotalIndentLev
    ) {
        return of(rawNode, parentTotalIndentLev, 0);
    }

    public static @NonNull LeafArrayNode of(
            JsonNode rawNode, int parentTotalIndentLev, int defaultIndentLev
    ) {
        if (rawNode.isMissingNode()) {
            throw new IllegalArgumentException("node is a missing node");
        }
        Optional<LeafArrayNode> leafArrayNode = tryOfExistJNode(rawNode, parentTotalIndentLev, defaultIndentLev);
        if (leafArrayNode.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("node (%s) does not contain a valid UiTextLeafArrayNode structure", rawNode)
            );
        }
        return leafArrayNode.get();
    }

    public static @NonNull Optional<LeafArrayNode> tryOf(
            JsonNode rawNode
    ) {
        return tryOfExistJNode(rawNode, 0, 0);
    }

    public static @NonNull Optional<LeafArrayNode> tryOf(
            JsonNode rawNode, int parentTotalIndentLev
    ) {
        return tryOfExistJNode(rawNode, parentTotalIndentLev, 0);
    }

    public static @NonNull Optional<LeafArrayNode> tryOf(
            JsonNode rawNode, int parentTotalIndentLev, int defaultIndentLev
    ) {
        if (rawNode.isMissingNode()) {
            return Optional.empty();
        }
        return tryOfExistJNode(rawNode, parentTotalIndentLev, defaultIndentLev);
    }

    @Override
    public @NonNull UiTextNode path(@NonNull String propertyName) {
        return MissingNode.getInstance();
    }

    @Override
    public @NonNull UiTextNode path(int index) {
        if (index < 0 || index >= lines.length) {
            return MissingNode.getInstance();
        }
        return lines[index];
    }

    @Override
    public @NonNull String txt() throws IllegalStateException {
        if (txtCache != null) {
            return txtCache;
        }
        StringBuilder sb = new StringBuilder();
        for (LeafNode line : lines) {
            String txt = line.txt();
            if (txt == null) {
                continue; // ignore null text
            }
            sb.append(txt);
        }
        txtCache = sb.toString();
        return txtCache;
    }

    @Override
    public @NonNull String txt(Object... args) throws IllegalStateException {
        return String.format(txt(), args);
    }

    @Override
    public @NonNull ExistingNodeType getExistingNodeType() {
        return ExistingNodeType.ARRAY;
    }

    @Override
    public @NonNull LeafArrayNode useIndentOf(UiTextNode node) {
        return (LeafArrayNode) ( (UiTextNode) this ).useIndentOf(node);
    }

    @Override
    public @NonNull LeafArrayNode useIndentOf(ExistingNode eNode) {
        return useIndent(eNode.parentTotalIndentLev + eNode.indentLev);
    }

    @Override
    public @NonNull LeafArrayNode useIndent(int newParentTotalIndentLev) {
        if (newParentTotalIndentLev == parentTotalIndentLev) {
            return this;
        }
        if (useIndentCache.containsKey(newParentTotalIndentLev)) {
            return (LeafArrayNode) useIndentCache.get(newParentTotalIndentLev);
        }

        LeafNode [] lines = new LeafNode[this.lines.length];
        for (int i = 0; i < lines.length; ++i) {
            lines[i] = this.lines[i].useIndent(newParentTotalIndentLev);
        }
        LeafArrayNode result = new LeafArrayNode(
                this.rawNode, this.indentLev, newParentTotalIndentLev,
                lines
        );
        useIndentCache.put(newParentTotalIndentLev, result);
        return result;
    }


    public @NonNull LeafNode get(int index) {
        Objects.checkIndex(index, lines.length);
        return lines[index];
    }

    protected static @NonNull Optional<LeafArrayNode> tryOfExistJNode(
            JsonNode rawNode, int parentTotalIndentLev, int defaultIndentLev
    ) {
        int indentLev = getIndentLevOf(rawNode, defaultIndentLev);
        return tryOfInternal(rawNode, indentLev, parentTotalIndentLev);
    }

    /**
     * access: package + child
     * <p>
     *     Array{@literal <String>}, {"indentLev": int #optional, "lines": Array{@literal <String>} }
     * </p>
     * @param rawNode a <b> NOT MISSING </b> JsonNode instance (i.e. return false on {@link JsonNode#isMissingNode()})
     * @param indentLev the indentLev <b> PARSED </b> from rawNode (should get from {@link UiTextNode#getIndentLevOf})
     * @param parentTotalIndentLev default value for indentLev if {rawNode.path("indentLev")} is missing
     * @return a new LeafNode instance wrapped with {@code Optional.of()}
     * if the node is in a valid structure for an UiTextLeafNode instance,
     * else {@code Optional.empty()}
     */
    protected static @NonNull Optional<LeafArrayNode> tryOfInternal(
            JsonNode rawNode, int indentLev, int parentTotalIndentLev
    ) {
        if (rawNode.isNull()) {
            return Optional.empty();
        }
        ArrayNode linesNode = null;
        if (rawNode.isArray()) {
            linesNode = rawNode.asArray();
        } else {
            if (!rawNode.isObject()) {
                return Optional.empty();
            }
            ObjectNode objNode = rawNode.asObject();
            for (Map.Entry<String, JsonNode> pair : objNode.properties()) {
                if (pair.getKey().equals("indentLev")) {
                    continue;
                }
                if (!pair.getKey().equals("lines")) {
                    return Optional.empty(); // LeafArrayNode must not have any other properties
                }
                JsonNode linesJNode = pair.getValue();
                if (!linesJNode.isArray()) { // node.lines has illegal type
                    return Optional.empty();
                }
                linesNode = linesJNode.asArray();
                // continue // construct after ensuring no other properties
            }
            if (linesNode == null) { // node has no explicit "lines" property
                return Optional.empty();
            }
        }
        LeafNode[] lines = new LeafNode[linesNode.size()];
        for (int i = 0; i < linesNode.size(); ++i) {
            Optional<LeafNode> opLeafNode = LeafNode.tryOfExistJNode(
                    linesNode.get(i), parentTotalIndentLev + indentLev, 0
            );
            if (opLeafNode.isEmpty()) { // LeafArrayNode can only contain valid LeafNode instances
                return Optional.empty();
            }
            lines[i] = opLeafNode.get();
        }
        return Optional.of(
                new LeafArrayNode(rawNode, indentLev, parentTotalIndentLev, lines)
        );
    }
}
