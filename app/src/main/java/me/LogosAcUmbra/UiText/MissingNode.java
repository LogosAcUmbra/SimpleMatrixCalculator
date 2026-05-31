package me.LogosAcUmbra.UiText;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeType;

import java.util.Optional;

public non-sealed class MissingNode extends UiTextNode<MissingNode> {

    private static final @NonNull MissingNode INSTANCE = new MissingNode();

    private MissingNode() {}

    public static MissingNode getInstance() {
        return INSTANCE;
    }


    @Override
    public @NonNull MissingNode path(@NonNull String propertyName) {
        return INSTANCE;
    }

    @Override
    public @NonNull MissingNode path(int index) {
        return INSTANCE;
    }


    @Override
    public boolean isMissing() {
        return true;
    }

    @Override
    public @Nullable String txt() throws IllegalStateException {
        throw nodeIsMissingException();
    }

    @Override
    public @NonNull String txt(Object... args) throws IllegalStateException {
        throw nodeIsMissingException();
    }

    @Override
    public JsonNode getRawNode() {
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
    public @NonNull LeafArrayNode asArr() throws IllegalStateException {
        throw nodeIsMissingException();
    }

    @Override
    public @NonNull MissingNode useIndentOf(UiTextNode<?> node) throws IllegalStateException {
        throw nodeIsMissingException();
    }

    @Override
    public @NonNull MissingNode addIndentOf(UiTextNode<?> node) {
        throw nodeIsMissingException();
    }

    @Override
    public @NonNull MissingNode useIndentOfExisting(ExistingNode<?> eNode) {
        throw nodeIsMissingException();
    }

    @Override
    public @NonNull MissingNode addIndentOfExisting(ExistingNode<?> eNode) {
        throw nodeIsMissingException();
    }

    @Override
    public @NonNull MissingNode useIndent(int newParentTotalIndentLev) {
        throw nodeIsMissingException();
    }

    @Override
    public @NonNull MissingNode addIndent(int extraParentTotalIndentLev) {
        throw nodeIsMissingException();
    }

    @Override
    public Optional<ExistingNode<?>> optToExistingNode() throws IllegalStateException {
        return Optional.empty();
    }

    protected IllegalStateException nodeIsMissingException() {
        return new IllegalStateException("node is a missing node");
    }
}
