package me.LogosAcUmbra.UiText;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.node.JsonNodeType;

public non-sealed class MissingNode extends UiTextNode {

    private static final @NonNull MissingNode INSTANCE = new MissingNode();

    private MissingNode() {}

    public static MissingNode getInstance() {
        return INSTANCE;
    }


    @Override
    public @NonNull UiTextNode path(@NonNull String propertyName) {
        return INSTANCE;
    }

    @Override
    public @NonNull UiTextNode path(int index) {
        return INSTANCE;
    }


    @Override
    public boolean isMissing() {
        return true;
    }

    @Override
    public @Nullable String get() throws IllegalStateException {
        throw nodeIsMissingException();
    }

    @Override
    public @NonNull String get(Object... args) throws IllegalStateException {
        throw nodeIsMissingException();
    }

    @Override
    public int getIndentLev() throws IllegalStateException {
        throw nodeIsMissingException();
    }

    @Override
    public @NonNull JsonNodeType getJsonNodeType() {
        return JsonNodeType.MISSING;
    }

    @Override
    public @NonNull ExistingNodeType getExistingNodeType() {
        return ExistingNodeType.NOT_EXIST;
    }

    @Override
    public @NonNull UiTextNode useIndentOf(UiTextNode node) throws IllegalStateException {
        throw nodeIsMissingException();
    }

    @Override
    public @NonNull UiTextNode useIndentOf(ExistingNode node) {
        throw nodeIsMissingException();
    }

    @Override
    public @NonNull UiTextNode useIndent(int newExtraIndentLev) {
        throw nodeIsMissingException();
    }

    protected IllegalStateException nodeIsMissingException() {
        return new IllegalStateException("node is a missing node");
    }
}
