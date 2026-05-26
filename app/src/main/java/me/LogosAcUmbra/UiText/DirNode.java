package me.LogosAcUmbra.UiText;

import org.jspecify.annotations.NonNull;
import tools.jackson.databind.JsonNode;


public class DirNode extends ExistingNode {

    private final @NonNull LeafNode title;
    private final @NonNull UiTextNode body;
    private final @NonNull LeafNode interruptMsg;
    private final @NonNull LeafNode finishMsg;


    protected DirNode(
            @NonNull JsonNode rawNode, int dirIndentLev, int parentTotalIndentLev,
            @NonNull LeafNode title,
            @NonNull UiTextNode body,
            @NonNull LeafNode interruptMsg,
            @NonNull LeafNode finishMsg
    ) {
        super(rawNode, dirIndentLev, parentTotalIndentLev);
        this.title = title;
        this.body = body;
        this.interruptMsg = interruptMsg;
        this.finishMsg = finishMsg;
    }

    public static @NonNull DirNode of(DirIndentFormat dirIndentFormat, JsonNode jNode, int parentTotalIndentLev)
            throws IllegalArgumentException {

        int dirIndentLev = UiTextNode.getIndentLevOf(jNode);
        LeafNode title;
        UiTextNode body;
        LeafNode interruptMsg;
        LeafNode finishMsg;
        try {

            JsonNode jTitle = jNode.path("title");
            JsonNode jBody = jNode.path("body");
            JsonNode jInterruptMsg = jNode.path("interruptMsg");
            JsonNode jFinishMsg = jNode.path("finishMsg");

            title = LeafNode.of(
                    jTitle,
                    parentTotalIndentLev + dirIndentLev,
                    dirIndentFormat.title()
            );
            body = UiTextNode.of(
                    jBody,
                    parentTotalIndentLev + dirIndentLev,
                    dirIndentFormat.body()
            );
            interruptMsg = LeafNode.of(
                    jInterruptMsg,
                    parentTotalIndentLev + dirIndentLev,
                    dirIndentFormat.interruptMsg()
            );
            finishMsg = LeafNode.of(
                    jFinishMsg,
                    parentTotalIndentLev + dirIndentLev,
                    dirIndentFormat.finishMsg()
            );

            if (body.isLeaf()) { throw nodeShouldNotLeaf("body", jBody); }

            if (title.isNull()) {
                throw nodeTextShouldNotNull("title", jTitle);
            }
            if (finishMsg.isNull()) {
                throw nodeTextShouldNotNull("finishMsg", jFinishMsg);
            }

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format("node (%s) cannot be parsed into a dirNode", jNode), e);
        }
        return new DirNode(jNode, dirIndentLev, parentTotalIndentLev, title, body, interruptMsg, finishMsg);
    }

    public @NonNull LeafNode title() {
        return title;
    }

    public @NonNull UiTextNode body() {
        return body;
    }

    public @NonNull LeafNode interruptMsg() {
        return interruptMsg;
    }

    public @NonNull LeafNode finishMsg() {
        return finishMsg;
    }

    @Override
    public @NonNull UiTextNode path(@NonNull String propertyName) {
        return switch (propertyName) {
            case "title" -> title;
            case "body" -> body;
            case "interruptMsg" -> interruptMsg;
            case "finishMsg" -> finishMsg;
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
    public @NonNull DirNode useIndentOf(UiTextNode node) {
        if (node.isMissing()) {
            throw new IllegalArgumentException("UiTextNode node is a missing node");
        }
        return useIndentOf( (ExistingNode) node );
    }

    @Override
    public @NonNull DirNode useIndentOf(ExistingNode eNode) {
        return useIndent(eNode.parentTotalIndentLev + eNode.indentLev);
    }

    @Override
    public @NonNull DirNode useIndent(int newExtraIndentLev) {
        return new DirNode(
                this.rawNode, this.indentLev, newExtraIndentLev,
                this.title.useIndent(newExtraIndentLev),
                this.body.useIndent(newExtraIndentLev),
                this.interruptMsg.useIndent(newExtraIndentLev),
                this.finishMsg.useIndent(newExtraIndentLev)
        );
    }

    private static IllegalArgumentException nodeShouldNotLeaf(@NonNull String propertyName, @NonNull JsonNode correspondingJNode)
            throws IllegalArgumentException {
        throw new IllegalArgumentException(String.format(
                "node.%s (%s) should not be able to be parsed into a LeafNode", propertyName, correspondingJNode
        ));
    }
    private static IllegalArgumentException nodeTextShouldNotNull(String propertyName, JsonNode correspondingJNode)
            throws IllegalArgumentException {
        throw new IllegalArgumentException(String.format(
                "node.%s (%s) should not have value of null", propertyName, correspondingJNode
        ));
    }

}