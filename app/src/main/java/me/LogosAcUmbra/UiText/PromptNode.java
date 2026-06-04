package me.LogosAcUmbra.UiText;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.jspecify.annotations.NonNull;
import tools.jackson.databind.JsonNode;

public class PromptNode extends ExistingNode<PromptNode> {
    protected final @NonNull LeafNode ask;
    protected final @NonNull LeafNode err;
    protected final @NonNull LeafNode quitMsg;
    protected final @NonNull LeafNode quitSpecifier;

    protected PromptNode(
            @NonNull JsonNode jNode, int indentLev,
            @NonNull LeafNode ask,
            @NonNull LeafNode err,
            @NonNull LeafNode quitMsg,
            @NonNull LeafNode quitSpecifier
    ) {
        super(jNode, indentLev, 0);
        this.ask = ask;
        this.err = err;
        this.quitMsg = quitMsg;
        this.quitSpecifier = quitSpecifier;
    }

    protected PromptNode(
            @NonNull JsonNode jNode, int indentLev, int parentTotalIndentLev,
            @NonNull Int2ObjectMap<PromptNode> useIndentCache,
            @NonNull LeafNode ask,
            @NonNull LeafNode err,
            @NonNull LeafNode quitMsg,
            @NonNull LeafNode quitSpecifier
    ) {
        super(jNode, indentLev, parentTotalIndentLev, useIndentCache);
        this.ask = ask;
        this.err = err;
        this.quitMsg = quitMsg;
        this.quitSpecifier = quitSpecifier;
    }

    public static @NonNull PromptNode of(PromptIndentFormat promptIndentFormat, @NonNull JsonNode jNode)
            throws IllegalArgumentException
    {
        int parentTotalIndentLev = 0; // prompt group node should have no indent
        int indentLev = UiTextNode.getIndentLevOf(jNode);
        int parentTotalIndentLevOfChildren = parentTotalIndentLev + indentLev;
        try {
            JsonNode jAsk = jNode.path("ask");
            JsonNode jErr = jNode.path("err");
            JsonNode jQuitMsg = jNode.path("quitMsg");
            JsonNode jQuitSpecifier = jNode.path("quitSpecifier");

            LeafNode ask = LeafNode.of(jAsk, parentTotalIndentLevOfChildren, promptIndentFormat.ask());
            LeafNode err = LeafNode.of(jErr, parentTotalIndentLevOfChildren, promptIndentFormat.err());
            LeafNode quitMsg = LeafNode.of(jQuitMsg, parentTotalIndentLevOfChildren, promptIndentFormat.quitMsg());
            LeafNode quitSpecifier = LeafNode.of(jQuitSpecifier, parentTotalIndentLevOfChildren, 0);

            if (ask.isNull()) {
                throw nodeTextShouldNotNull("ask", jAsk);
            }
            // err can be null when there is no criteria needed (although it is rare)
            // quitMsg and quitSpecifier can be null when there is no quitMsg function for the prompt
            if (quitMsg.isNull() ^ quitSpecifier.isNull()) {
                throw new IllegalArgumentException(String.format(
                        "node.quitMsg (%s) and node.quitSpecifier (%s) must both be null or both be non-null",
                        jQuitMsg, jQuitSpecifier
                ));
            }

            return new PromptNode(jNode, indentLev, ask, err, quitMsg, quitSpecifier);

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    String.format("node (%s) cannot be parsed into a PromptNode instance", jNode),
                    e
            );
        }

    }

    @Override
    protected @NonNull PromptNode self() {
        return this;
    }

    @Override
    public @NonNull PromptNode immutableSetParentIndent(int newParentTotalIndentLev) {
        return new PromptNode(
                this.rawNode, this.indentLev, newParentTotalIndentLev, this.useIndentCache,
                ask.useIndent(newParentTotalIndentLev),
                err.useIndent(newParentTotalIndentLev),
                quitMsg.useIndent(newParentTotalIndentLev),
                quitSpecifier.useIndent(newParentTotalIndentLev)
        );
    }

    @Override
    public @NonNull UiTextNode<?> path(@NonNull String propertyName) {
        return switch (propertyName) {
            case ("ask") -> ask;
            case ("err") -> err;
            case ("quitMsg") -> quitMsg;
            case ("quitSpecifier") -> quitSpecifier;
            default -> MissingNode.getInstance();
        };
    }

    @Override
    public @NonNull UiTextNode<?> path(int index) {
        return MissingNode.getInstance();
    }

    @Override
    public @NonNull ExistingNodeType getExistingNodeType() {
        return ExistingNodeType.FIXED_BRANCH;
    }

    public LeafNode ask() {
        return ask;
    }

    public LeafNode err() {
        return err;
    }

    public LeafNode quitMsg() {
        return quitMsg;
    }

    public LeafNode quitSpecifier() {
        return quitSpecifier;
    }

    private static IllegalArgumentException nodeTextShouldNotNull(String propertyName, JsonNode correspondingJNode)
            throws IllegalArgumentException {
        throw new IllegalArgumentException(String.format(
                "node.%s (%s) should not have value of null", propertyName, correspondingJNode
        ));
    }
}
