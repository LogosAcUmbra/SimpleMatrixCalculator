package me.LogosAcUmbra.UiText;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeType;

import java.util.Optional;


public abstract non-sealed class ExistingNode extends UiTextNode {

    protected final @NonNull JsonNode rawNode;
    protected final int indentLev; // relative indent level: no matter how many indent the upper level has, u want `indentLev` more indent
    protected final int parentTotalIndentLev; // the number of indent of the upper level

    protected ExistingNode(
            @NonNull JsonNode rawNode,
            int indentLev,
            int parentTotalIndentLev
    ) {
        this.rawNode = rawNode;
        this.indentLev = indentLev;
        this.parentTotalIndentLev = parentTotalIndentLev;
    }

    public static @NonNull ExistingNode of(JsonNode rawNode) {
        return of(rawNode, 0, 0);
    }
    public static @NonNull ExistingNode of(JsonNode rawNode, int parentTotalIndentLev) {
        return of(rawNode, parentTotalIndentLev, 0);
    }
    public static @NonNull ExistingNode of(JsonNode rawNode, int parentTotalIndentLev, int defaultIndentLev) {
        if (rawNode.isMissingNode()) {
            throw new IllegalArgumentException(String.format("node (%s) is a missing node", rawNode));
        }
        return ofExistJNode(rawNode, parentTotalIndentLev, defaultIndentLev);
    }

    public static @NonNull Optional<? extends ExistingNode> tryOf(JsonNode rawNode) {
        return tryOf(rawNode, 0, 0);
    }
    public static @NonNull Optional<? extends ExistingNode> tryOf(JsonNode rawNode, int parentTotalIndentLev) {
        return tryOf(rawNode, parentTotalIndentLev, 0);
    }
    public static @NonNull Optional<? extends ExistingNode> tryOf(JsonNode rawNode, int parentTotalIndentLev, int defaultIndentLev) {
        if (rawNode.isMissingNode()) {
            return Optional.empty();
        }
        return Optional.of(
                ofExistJNode(rawNode, parentTotalIndentLev, defaultIndentLev)
        );
    }



    /**
     * access: package + child
     * @param rawNode a <b> NOT MISSING </b> JsonNode instance
     * @param parentTotalIndentLev parentTotalIndentLev
     * @param defaultIndentLev default value for indentLev if {rawNode.path("indentLev")} is missing
     * @return a new {@code DynamicBranchNode} or {@code LeafNode} instance
     */
    protected static @NonNull ExistingNode ofExistJNode(JsonNode rawNode, int parentTotalIndentLev, int defaultIndentLev) {
        Optional<LeafNode> opLeaf = LeafNode.tryOfExistJNode(rawNode, parentTotalIndentLev, defaultIndentLev);
        if (opLeaf.isPresent()) {
            return opLeaf.get();
        }
        return DynamicBranchNode.ofExistJNode(rawNode, parentTotalIndentLev, defaultIndentLev);
    }

    public abstract @NonNull UiTextNode path(@NonNull String propertyName);
    public abstract @NonNull UiTextNode path(int index);

    @Override
    public boolean isMissing() {
        return false;
    }


    public @Nullable String get() throws IllegalStateException {
        throw new IllegalStateException(String.format("node (%s) is not a leaf node", rawNode));
    }
    public @NonNull String get(Object... args) throws IllegalStateException {
        throw new IllegalStateException(String.format("node (%s) is not a leaf node", rawNode));
    }

    @Override
    public @NonNull JsonNode getRawNode() {
        return rawNode;
    }

    @Override
    public int getIndentLev() throws IllegalStateException {
        return parentTotalIndentLev;
    }

    public @NonNull JsonNodeType getJsonNodeType() {
        return rawNode.getNodeType();
    }



}