package me.LogosAcUmbra.UiText;

import org.jspecify.annotations.NonNull;
import tools.jackson.databind.JsonNode;

public class PromptNode extends ExistingNode{
    protected final @NonNull LeafNode ask;
    protected final @NonNull LeafNode err;
    protected final @NonNull LeafNode quitMsg;
    protected final @NonNull LeafNode quitSpecifier;

    protected PromptNode(
            @NonNull JsonNode jNode,
            int indentLev,
            int parentTotalIndentLev,
            @NonNull LeafNode ask,
            @NonNull LeafNode err,
            @NonNull LeafNode quitMsg,
            @NonNull LeafNode quitSpecifier
    ) {
        super(jNode, indentLev, parentTotalIndentLev);
        this.ask = ask;
        this.err = err;
        this.quitMsg = quitMsg;
        this.quitSpecifier = quitSpecifier;
    }

    private static @NonNull PromptNode create(
            @NonNull JsonNode jNode,
            @NonNull LeafNode ask,
            @NonNull LeafNode err,
            @NonNull LeafNode quitMsg,
            @NonNull LeafNode quitSpecifier
    ) {
        return new PromptNode(
                jNode, 0, 0,
                ask, err, quitMsg, quitSpecifier
        );
    }

    public static @NonNull PromptNode of(PromptIndentFormat promptIndentFormat, @NonNull JsonNode jNode)
            throws IllegalArgumentException
    {
        try {

            JsonNode jAsk = jNode.path("ask");
            JsonNode jErr = jNode.path("err");
            JsonNode jQuitMsg = jNode.path("quitMsg");
            JsonNode jQuitSpecifier = jNode.path("quitSpecifier");

            LeafNode ask = LeafNode.of(jAsk, 0, promptIndentFormat.ask());
            LeafNode err = LeafNode.of(jErr, 0, promptIndentFormat.err());
            LeafNode quitMsg = LeafNode.of(jQuitMsg, 0, promptIndentFormat.quitMsg());
            LeafNode quitSpecifier = LeafNode.of(jQuitSpecifier, 0, 0);

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

            return create(jNode, ask, err, quitMsg, quitSpecifier);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    String.format("node (%s) cannot be parsed into a PromptNode instance", jNode),
                    e
            );
        }

    }

    @Override
    public @NonNull UiTextNode path(@NonNull String propertyName) {
        return switch (propertyName) {
            case ("ask") -> ask;
            case ("err") -> err;
            case ("quitMsg") -> quitMsg;
            case ("quitSpecifier") -> quitSpecifier;
            default -> MissingNode.getInstance();
        };
    }

    @Override
    public @NonNull UiTextNode path(int index) {
        return MissingNode.getInstance();
    }

    @Override
    public @NonNull ExistingNodeType getExistingNodeType() {
        return ExistingNodeType.FIXED_BRANCH;
    }

    @Override
    public @NonNull PromptNode useIndentOf(UiTextNode node) {
        if (node.isMissing()) {
            throw new IllegalArgumentException("UiTextNode node is a missing node");
        }
        return useIndentOf( (ExistingNode) node );
    }

    @Override
    public @NonNull PromptNode useIndentOf(ExistingNode eNode) {
        return useIndent(eNode.parentTotalIndentLev + eNode.indentLev);
    }

    @Override
    public @NonNull PromptNode useIndent(int newExtraIndentLev) {
        return new PromptNode(
                this.rawNode, this.indentLev, newExtraIndentLev,
                ask.useIndent(newExtraIndentLev),
                err.useIndent(newExtraIndentLev),
                quitMsg.useIndent(newExtraIndentLev),
                quitSpecifier.useIndent(newExtraIndentLev)
        );
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

//    private static IllegalArgumentException nodeNotText(String propertyName, JsonNode correspondingJNode)
//            throws IllegalArgumentException {
//        throw new IllegalArgumentException(String.format(
//                "node.%s (%s) cannot be parsed into a text", propertyName, correspondingJNode
//        ));
//    }
//    private static IllegalArgumentException nodeShouldNotText(String propertyName, JsonNode correspondingJNode)
//            throws IllegalArgumentException {
//        throw new IllegalArgumentException(String.format(
//                "node.%s (%s) should not be able to parsed into a text", propertyName, correspondingJNode
//        ));
//    }
    private static IllegalArgumentException nodeTextShouldNotNull(String propertyName, JsonNode correspondingJNode)
            throws IllegalArgumentException {
        throw new IllegalArgumentException(String.format(
                "node.%s (%s) should not have value of null", propertyName, correspondingJNode
        ));
    }
}
