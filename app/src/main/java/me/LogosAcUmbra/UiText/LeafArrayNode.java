package me.LogosAcUmbra.UiText;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class LeafArrayNode extends ExistingNode<LeafArrayNode> {
    @NonNull LeafNode @NonNull [] leafs;
    @Nullable String txtCache;

    protected LeafArrayNode(
            JsonNode rawNode, int indentLev, int parentTotalIndentLev,
            @NonNull LeafNode @NonNull [] leafs
    ) {
        super(rawNode, indentLev, parentTotalIndentLev);
        this.leafs = leafs;
    }

    @Override
    protected @NonNull LeafArrayNode self() {
        return this;
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
        Optional<LeafArrayNode> leafArrayNode = optOfExistJNode(rawNode, parentTotalIndentLev, defaultIndentLev);
        if (leafArrayNode.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("node (%s) does not contain a valid UiTextLeafArrayNode structure", rawNode)
            );
        }
        return leafArrayNode.get();
    }

    public static @NonNull Optional<LeafArrayNode> optOf(
            JsonNode rawNode
    ) {
        return optOfExistJNode(rawNode, 0, 0);
    }

    public static @NonNull Optional<LeafArrayNode> optOf(
            JsonNode rawNode, int parentTotalIndentLev
    ) {
        return optOfExistJNode(rawNode, parentTotalIndentLev, 0);
    }

    public static @NonNull Optional<LeafArrayNode> optOf(
            JsonNode rawNode, int parentTotalIndentLev, int defaultIndentLev
    ) {
        if (rawNode.isMissingNode()) {
            return Optional.empty();
        }
        return optOfExistJNode(rawNode, parentTotalIndentLev, defaultIndentLev);
    }

    @Override
    public @NonNull LeafArrayNode immutableSetParentIndent(int newParentTotalIndentLev) {
        LeafNode [] leafs = new LeafNode[this.leafs.length];
        for (int i = 0; i < leafs.length; ++i) {
            leafs[i] = this.leafs[i].useIndent(newParentTotalIndentLev);
        }
        return new LeafArrayNode(
                this.rawNode, this.indentLev, newParentTotalIndentLev,
                leafs
        );
    }

    @Override
    public @NonNull UiTextNode<?> path(@NonNull String propertyName) {
        return MissingNode.getInstance();
    }

    @Override
    public @NonNull UiTextNode<?> path(int index) {
        if (index < 0 || index >= leafs.length) {
            return MissingNode.getInstance();
        }
        return leafs[index];
    }

    /**
     * @return String combining {@link LeafNode#txt} of leaf forall leaf in leafs
     */
    @Override
    public @NonNull String txt() {
        if (txtCache != null) {
            return txtCache;
        }
        StringBuilder sb = new StringBuilder();
        for (LeafNode leaf : leafs) {
            String txt = leaf.txt();
            sb.append(txt);
        }
        txtCache = sb.toString();
        return txtCache;
    }

    @Override
    public @NonNull String txt(Object... args) {
        return String.format(txt(), args);
    }

    public @NonNull LeafNode[] getLeafs() {
        return leafs;
    }

    public @NonNull String[] toStrArr() throws IllegalStateException {
        int size = leafs.length;
        String[] result = new String[size];
        for (int i = 0; i < size; ++i) {
            result[i] = leafs[i].txt();
        }
        return result;
    }

    @Override
    public @NonNull ExistingNodeType getExistingNodeType() {
        return ExistingNodeType.ARRAY;
    }

    @Override
    public @NonNull LeafArrayNode asArr() {
        return this;
    }

    public @NonNull LeafNode get(int index) {
        Objects.checkIndex(index, leafs.length);
        return leafs[index];
    }

    protected static @NonNull Optional<LeafArrayNode> optOfExistJNode(
            JsonNode rawNode, int parentTotalIndentLev, int defaultIndentLev
    ) {
        int indentLev = getIndentLevOf(rawNode, defaultIndentLev);
        return optOfInternal(rawNode, indentLev, parentTotalIndentLev);
    }

    /**
     * access: package + child
     * <p>
     *     Array{@literal <String>}, {"indentLev": int #optional, "leafs": Array{@literal <String>} }
     * </p>
     * @param rawNode a <b> NOT MISSING </b> JsonNode instance (i.e. return false on {@link JsonNode#isMissingNode()})
     * @param indentLev the indentLev <b> PARSED </b> from rawNode (should get from {@link UiTextNode#getIndentLevOf})
     * @param parentTotalIndentLev default value for indentLev if {rawNode.path("indentLev")} is missing
     * @return a new LeafNode instance wrapped with {@code Optional.of()}
     * if the node is in a valid structure for an UiTextLeafNode instance,
     * else {@code Optional.empty()}
     */
    protected static @NonNull Optional<LeafArrayNode> optOfInternal(
            JsonNode rawNode, int indentLev, int parentTotalIndentLev
    ) {
        if (rawNode.isNull()) {
            return Optional.empty();
        }
        ArrayNode leafsNode = null;
        if (rawNode.isArray()) {
            leafsNode = rawNode.asArray();
        } else {
            if (!rawNode.isObject()) {
                return Optional.empty();
            }
            ObjectNode objNode = rawNode.asObject();
            for (Map.Entry<String, JsonNode> pair : objNode.properties()) {
                if (pair.getKey().equals("indentLev")) {
                    continue;
                }
                if (!pair.getKey().equals("leafs")) {
                    return Optional.empty(); // LeafArrayNode must not have any other properties
                }
                JsonNode leafsJNode = pair.getValue();
                if (!leafsJNode.isArray()) { // node.leafs has illegal type
                    return Optional.empty();
                }
                leafsNode = leafsJNode.asArray();
                // continue // construct after ensuring no other properties
            }
            if (leafsNode == null) { // node has no explicit "leafs" property
                return Optional.empty();
            }
        }
        LeafNode[] leafs = new LeafNode[leafsNode.size()];
        for (int i = 0; i < leafsNode.size(); ++i) {
            Optional<LeafNode> opLeafNode = LeafNode.optOfExistJNode(
                    leafsNode.get(i), parentTotalIndentLev + indentLev, 0
            );
            if (opLeafNode.isEmpty()) { // LeafArrayNode can only contain valid LeafNode instances
                return Optional.empty();
            }
            leafs[i] = opLeafNode.get();
        }
        return Optional.of(
                new LeafArrayNode(rawNode, indentLev, parentTotalIndentLev, leafs)
        );
    }
}
