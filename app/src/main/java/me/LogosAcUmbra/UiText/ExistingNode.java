package me.LogosAcUmbra.UiText;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeType;

import java.util.Optional;


public abstract non-sealed class ExistingNode<T extends ExistingNode<T>> extends UiTextNode<ExistingNode<T>> {

    protected final @NonNull JsonNode rawNode;
    protected final int indentLev; // relative indent level: no matter how many indent the upper level has, u want `indentLev` more indent
    protected final int parentTotalIndentLev; // the number of indent of the upper level

    protected final Int2ObjectMap<T> useIndentCache;

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
            @NonNull Int2ObjectMap<T> useIndentCache
    ) {
        this.rawNode = rawNode;
        this.indentLev = indentLev;
        this.parentTotalIndentLev = parentTotalIndentLev;
        this.useIndentCache = useIndentCache;
    }

    protected abstract @NonNull T self();

    public static @NonNull ExistingNode<?> of(JsonNode rawNode) {
        return of(rawNode, 0, 0);
    }
    public static @NonNull ExistingNode<?> of(JsonNode rawNode, int parentTotalIndentLev) {
        return of(rawNode, parentTotalIndentLev, 0);
    }
    public static @NonNull ExistingNode<?> of(JsonNode rawNode, int parentTotalIndentLev, int defaultIndentLev) {
        if (rawNode.isMissingNode()) {
            throw new IllegalArgumentException(String.format("node (%s) is a missing node", rawNode));
        }
        return ofExistJNode(rawNode, parentTotalIndentLev, defaultIndentLev);
    }

    public static @NonNull Optional<? extends ExistingNode<?>> optOf(JsonNode rawNode) {
        return optOf(rawNode, 0, 0);
    }
    public static @NonNull Optional<? extends ExistingNode<?>> optOf(JsonNode rawNode, int parentTotalIndentLev) {
        return optOf(rawNode, parentTotalIndentLev, 0);
    }
    public static @NonNull Optional<? extends ExistingNode<?>> optOf(JsonNode rawNode, int parentTotalIndentLev, int defaultIndentLev) {
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
    protected static @NonNull ExistingNode<?> ofExistJNode(JsonNode rawNode, int parentTotalIndentLev, int defaultIndentLev) {
        int indentLev = getIndentLevOf(rawNode, defaultIndentLev);
        Optional<LeafNode> optLeaf = LeafNode.optOfInternal(rawNode, indentLev, parentTotalIndentLev);
        if (optLeaf.isPresent()) {
            return optLeaf.get();
        }
        Optional<LeafArrayNode> optLeafArrayNode = LeafArrayNode.optOfInternal(rawNode, indentLev, parentTotalIndentLev);
        if (optLeafArrayNode.isPresent()) {
            return optLeafArrayNode.get();
        }
        return DynamicBranchNode.ofInternal(rawNode, indentLev, parentTotalIndentLev);
    }

    public abstract @NonNull T immutableSetParentIndent(int newParentTotalIndentLev);

    public abstract @NonNull UiTextNode<?> path(@NonNull String propertyName);
    public abstract @NonNull UiTextNode<?> path(int index);

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

    @Override
    public @NonNull T useIndentOf(UiTextNode<?> node) {
        if (node.isMissing()) {
            throw new IllegalArgumentException("the given node is a missing node");
        }
        return useIndentOfExisting((ExistingNode<?>) node);
    }

    @Override
    public @NonNull T addIndentOf(UiTextNode<?> node) {
        if (node.isMissing()) {
            throw new IllegalArgumentException("the given node is a missing node");
        }
        return addIndentOfExisting((ExistingNode<?>) node);
    }

    @Override
    public @NonNull T useIndentOfExisting(ExistingNode<?> eNode) {
        return useIndent(eNode.parentTotalIndentLev + eNode.indentLev);
    }

    @Override
    public @NonNull T addIndentOfExisting(ExistingNode<?> eNode) {
        return addIndent(eNode.parentTotalIndentLev + eNode.indentLev);
    }

    @Override
    public @NonNull T useIndent(int parentTotalIndentLev) {
        if (parentTotalIndentLev == this.parentTotalIndentLev) {
            if (!useIndentCache.containsKey(parentTotalIndentLev)) {
                useIndentCache.put(parentTotalIndentLev, self());
            }
            return self();
        }
        return useIndentHelper(parentTotalIndentLev);
    }

    @Override
    public @NonNull T addIndent(int extraParentTotalIndentLev) {
        return useIndentHelper(extraParentTotalIndentLev + this.parentTotalIndentLev);
    }

    private @NonNull T useIndentHelper(int newParentTotalIndentLev) {
        if (useIndentCache.containsKey(newParentTotalIndentLev)) {
            return useIndentCache.get(newParentTotalIndentLev);
        }
        T result = immutableSetParentIndent(newParentTotalIndentLev);
        useIndentCache.put(newParentTotalIndentLev, result);
        return result;
    }

    @Override
    public Optional<ExistingNode<?>> optToExistingNode() throws IllegalStateException {
        return Optional.of(this);
    }

    @Override
    public @NonNull JsonNodeType getJsonNodeType() {
        return rawNode.getNodeType();
    }
}