package me.LogosAcUmbra.UiText;


import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeType;

import java.util.Optional;


public abstract sealed class UiTextNode<T extends UiTextNode<T>>
        permits ExistingNode, MissingNode {

    protected UiTextNode() {}

    public static @NonNull UiTextNode<?> of(JsonNode rawNode) {
        return of(rawNode, 0);
    }
    public static @NonNull UiTextNode<?> of(JsonNode rawNode, int parentTotalIndentLev) {
        return of(rawNode, parentTotalIndentLev, 0);
    }
    public static @NonNull UiTextNode<?> of(JsonNode rawNode, int parentTotalIndentLev, int defaultIndentLev) {
        if (rawNode.isMissingNode()) {
            return MissingNode.getInstance();
        }
        return ExistingNode.ofExistJNode(rawNode, parentTotalIndentLev, defaultIndentLev);
    }


    public abstract @NonNull UiTextNode<?> path(@NonNull String propertyName);
    public abstract @NonNull UiTextNode<?> path(int index);

    public abstract boolean isMissing();

    public boolean isLeaf() { return false; }
    public boolean isNull() { return false; }
    public boolean isString() { return false; }

    public abstract @Nullable String txt() throws IllegalStateException;
    public abstract @NonNull String txt(Object... args) throws IllegalStateException;

    public abstract JsonNode getRawNode();
    public abstract int getIndentLev() throws IllegalStateException;

    public abstract @NonNull JsonNodeType getJsonNodeType();
    public abstract @NonNull ExistingNodeType getExistingNodeType();

    public abstract @NonNull LeafArrayNode asArr() throws IllegalStateException;

    public abstract @NonNull T useIndentOf(UiTextNode<?> node);
    public abstract @NonNull T addIndentOf(UiTextNode<?> node);

    public abstract @NonNull T useIndentOfExisting(ExistingNode<?> eNode);
    public abstract @NonNull T addIndentOfExisting(ExistingNode<?> eNode);

    public abstract @NonNull T useIndent(int parentTotalIndentLev);
    public abstract @NonNull T addIndent(int extraParentTotalIndentLev);

    public abstract Optional<ExistingNode<?>> optToExistingNode() throws IllegalStateException;

    public static int getIndentLevOf(@NonNull JsonNode jNode) {
        return getIndentLevOf(jNode, 0);
    }
    public static int getIndentLevOf(@NonNull JsonNode jNode, int defaultIndentLev) {
        JsonNode pathIndentLev = jNode.path("indentLev");
        return (
                (!pathIndentLev.isMissingNode() && pathIndentLev.isInt()) ?
                        pathIndentLev.asInt() : defaultIndentLev
        );
    }

}