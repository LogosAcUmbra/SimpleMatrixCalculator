package me.LogosAcUmbra.UiText;


import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;


public class DynamicBranchNode extends ExistingNode<DynamicBranchNode> {

    protected final @Nullable HashMap<String, UiTextNode<?>> pathKeys;
    protected final @Nullable ArrayList<UiTextNode<?>> pathIndices;

    protected DynamicBranchNode(
            @NonNull JsonNode rawNode,
                int indentLev,
                int parentTotalIndentLev,
            @Nullable HashMap<String, UiTextNode<?>> pathKeys,
            @Nullable ArrayList<UiTextNode<?>> pathIndices
    ) {
        super(rawNode, indentLev, parentTotalIndentLev);
        this.pathKeys = pathKeys;
        this.pathIndices = pathIndices;
    }

    @Override
    protected @NonNull DynamicBranchNode self() {
        return this;
    }

    public static @NonNull DynamicBranchNode of(@NonNull JsonNode rawNode) {
        return of(rawNode, 0, 0);
    }
    public static @NonNull DynamicBranchNode of(@NonNull JsonNode rawNode, int parentTotalIndentLev) {
        return of(rawNode, parentTotalIndentLev, 0);
    }
    public static @NonNull DynamicBranchNode of(@NonNull JsonNode rawNode, int parentTotalIndentLev, int defaultIndentLev) {
        if (rawNode.isMissingNode()) {
            throw new IllegalArgumentException(String.format("rawNode (%s) is a missingNode", rawNode));
        }
        int indentLev = getIndentLevOf(rawNode, defaultIndentLev);
        return ofInternal(rawNode, indentLev, parentTotalIndentLev);
    }

    public static @NonNull Optional<DynamicBranchNode> optOf(JsonNode rawNode) {
        return optOf(rawNode, 0, 0);
    }
    public static @NonNull Optional<DynamicBranchNode> optOf(JsonNode rawNode, int parentTotalIndentLev) {
        return optOf(rawNode, parentTotalIndentLev, 0);
    }
    public static @NonNull Optional<DynamicBranchNode> optOf(JsonNode rawNode, int parentTotalIndentLev, int defaultIndentLev) {
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
     * @return a new {@code DynamicBranchNode} instance
     */
    protected static @NonNull DynamicBranchNode ofExistJNode(JsonNode rawNode, int parentTotalIndentLev, int defaultIndentLev) {
        int indentLev = getIndentLevOf(rawNode, defaultIndentLev);
        return ofInternal(rawNode, indentLev, parentTotalIndentLev);
    }

    @Override
    public @NonNull DynamicBranchNode immutableSetParentIndent(int newParentTotalIndentLev) {
        // lazy initialization
        HashMap<String, UiTextNode<?>> pathKeys = (this.pathKeys == null) ? null : new HashMap<>(this.pathKeys.size());
        ArrayList<UiTextNode<?>> pathIndices = (this.pathIndices == null) ? null : new ArrayList<>(this.pathIndices.size());

        return new DynamicBranchNode(
                this.rawNode, this.indentLev, newParentTotalIndentLev,
                pathKeys, pathIndices
        );
    }

    /**
     * access: package + child
     * @param rawNode a <b> NOT MISSING </b> JsonNode instance (i.e. return false on {@link JsonNode#isMissingNode()})
     * @param indentLev the indentLev <b> PARSED </b> from rawNode (should get from {@link UiTextNode#getIndentLevOf})
     * @param parentTotalIndentLev parentTotalIndentLev
     * @return a new DynamicBranchNode instance
     */
    protected static @NonNull DynamicBranchNode ofInternal(
            @NonNull JsonNode rawNode, int indentLev, int parentTotalIndentLev
    ) {
        HashMap<String, UiTextNode<?>> pathKeys = (rawNode.isObject()) ? (new HashMap<>()) : (null);
        ArrayList<UiTextNode<?>> pathIndices = (rawNode.isArray()) ? (new ArrayList<>()) : (null);
        return new DynamicBranchNode(rawNode, indentLev, parentTotalIndentLev, pathKeys, pathIndices);
    }


    @Override
    public @NonNull UiTextNode<?> path(@NonNull String propertyName) {
        if (pathKeys == null) { // this node is not a map
            return MissingNode.getInstance();
        }
        UiTextNode<?> cache = pathKeys.get(propertyName);
        if (cache != null) { // cache hit
            return cache;
        }
        JsonNode nextJsonNode = rawNode.path(propertyName);
        UiTextNode<?> nextMsgNode = UiTextNode.of(nextJsonNode, parentTotalIndentLev + indentLev);
        pathKeys.put(propertyName, nextMsgNode);
        return nextMsgNode;
    }
    @Override
    public @NonNull UiTextNode<?> path(int index) {
        if (pathIndices == null) { // this node is not an array
            return MissingNode.getInstance();
        }
        if (index < 0) {
            throw new IllegalArgumentException(String.format("index (%d) must be non-negative", index));
        }
        if (index < pathIndices.size()) {
            UiTextNode<?> cache = pathIndices.get(index);
            if (cache != null) { // cache hit
                return cache;
            }
        } else {
            pathIndices.ensureCapacity(index + 1);
            pathIndices.addAll(Collections.nCopies(index + 1 - pathIndices.size(), null));
        }

        JsonNode nextJsonNode = rawNode.path(index);
        UiTextNode<?> nextMsgNode = UiTextNode.of(nextJsonNode, parentTotalIndentLev + indentLev);
        pathIndices.set(index, nextMsgNode);
        return nextMsgNode;
    }

    @Override
    public @NonNull ExistingNodeType getExistingNodeType() {
        return ExistingNodeType.DYNAMIC_BRANCH;
    }

    public @NonNull Optional<LeafNode> tryToLeafNode() {
        return LeafNode.optOf(rawNode, parentTotalIndentLev);
    }

}