package me.LogosAcUmbra.UiText;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeType;

import java.util.Optional;


public abstract non-sealed class ExistingNode extends UiTextNode {

    protected final @NonNull JsonNode rawNode;
    protected final int indentLev; // relative indent level: no matter how many indent the upper level has, u want `indentLev` more indent
    protected final int parentTotalIndentLev; // the number of indent of the upper level

    protected final Int2ObjectMap<ExistingNode> useIndentCache;

    protected ExistingNode(
            @NonNull JsonNode rawNode,
            int indentLev,
            int parentTotalIndentLev
    ) {
        this.rawNode = rawNode;
        this.indentLev = indentLev;
        this.parentTotalIndentLev = parentTotalIndentLev;
        this.useIndentCache = new Int2ObjectOpenHashMap<>();
    }

    protected ExistingNode(
            @NonNull JsonNode rawNode,
            int indentLev,
            int parentTotalIndentLev,
            @NonNull Int2ObjectMap<ExistingNode> useIndentCache
    ) {
        this.rawNode = rawNode;
        this.indentLev = indentLev;
        this.parentTotalIndentLev = parentTotalIndentLev;
        this.useIndentCache = useIndentCache;
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
     * @param rawNode a <b> NOT MISSING </b> JsonNode instance (i.e. return false on {@link JsonNode#isMissingNode()})
     * @param parentTotalIndentLev parentTotalIndentLev
     * @param defaultIndentLev default value for indentLev if {rawNode.path("indentLev")} is missing
     * @return a new {@code DynamicBranchNode} or {@code LeafNode} instance
     */
    protected static @NonNull ExistingNode ofExistJNode(JsonNode rawNode, int parentTotalIndentLev, int defaultIndentLev) {
        int indentLev = getIndentLevOf(rawNode, defaultIndentLev);
        Optional<LeafNode> optLeaf = LeafNode.tryOfInternal(rawNode, indentLev, parentTotalIndentLev);
        if (optLeaf.isPresent()) {
            return optLeaf.get();
        }
        Optional<LeafArrayNode> optLeafArrayNode = LeafArrayNode.tryOfInternal(rawNode, indentLev, parentTotalIndentLev);
        if (optLeafArrayNode.isPresent()) {
            return optLeafArrayNode.get();
        }
        return DynamicBranchNode.ofInternal(rawNode, indentLev, parentTotalIndentLev);
    }

    public abstract @NonNull UiTextNode path(@NonNull String propertyName);
    public abstract @NonNull UiTextNode path(int index);

    @Override
    public boolean isMissing() {
        return false;
    }


    public @Nullable String txt() throws IllegalStateException {
        throw new IllegalStateException(String.format("node (%s) is not a leaf node", rawNode));
    }
    public @NonNull String txt(Object... args) throws IllegalStateException {
        throw new IllegalStateException(String.format("node (%s) is not a leaf node", rawNode));
    }

    @Override
    public @NonNull JsonNode getRawNode() {
        return rawNode;
    }

    @Override
    public int getIndentLev() throws IllegalStateException {
        return parentTotalIndentLev + indentLev;
    }

    @Override
    public @NonNull LeafArrayNode asArr() throws IllegalStateException {
        throw new IllegalStateException(String.format(
                "node (%s) is not a LeafArrayNode", rawNode
        ));
    }


    public @NonNull JsonNodeType getJsonNodeType() {
        return rawNode.getNodeType();
    }


}