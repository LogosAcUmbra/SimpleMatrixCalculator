package me.LogosAcUmbra.UiText;


import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeType;

import java.util.Optional;


public abstract sealed class UiTextNode permits ExistingNode, MissingNode {

    protected UiTextNode() {}

    public static @NonNull UiTextNode of(JsonNode rawNode) {
        return of(rawNode, 0);
    }
    public static @NonNull UiTextNode of(JsonNode rawNode, int parentTotalIndentLev) {
        return of(rawNode, parentTotalIndentLev, 0);
    }
    public static @NonNull UiTextNode of(JsonNode rawNode, int parentTotalIndentLev, int defaultIndentLev) {
        if (rawNode.isMissingNode()) {
            return MissingNode.getInstance();
        }
        return ExistingNode.ofExistJNode(rawNode, parentTotalIndentLev, defaultIndentLev);
    }


    public abstract @NonNull UiTextNode path(@NonNull String propertyName);
    public abstract @NonNull UiTextNode path(int index);

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

    public @NonNull UiTextNode useIndentOf(UiTextNode node) {
        if (node.isMissing()) {
            throw new IllegalArgumentException("the given node is a missing node");
        }
        ExistingNode eNode = (ExistingNode) node;
        return useIndentOf(eNode);
    }
    public abstract @NonNull UiTextNode useIndentOf(ExistingNode eNode);

    public abstract @NonNull UiTextNode useIndent(int newExtraIndentLev);

    public Optional<ExistingNode> tryToExistingNode() throws IllegalStateException {
        if (isMissing()) {
            return Optional.empty();
        }
        return Optional.of((ExistingNode) this);
    }

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