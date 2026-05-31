package me.LogosAcUmbra.UiText;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.jspecify.annotations.NonNull;
import tools.jackson.databind.JsonNode;


public class DirNode extends ExistingNode<DirNode> {

    private final @NonNull LeafNode title;
    private final @NonNull UiTextNode<?> body;
    private final @NonNull UiTextNode<?> interruptMsg;
    private final @NonNull LeafNode finishMsg;


    protected DirNode(
            @NonNull JsonNode rawNode, int dirIndentLev, int parentTotalIndentLev,
            @NonNull LeafNode title,
            @NonNull UiTextNode<?> body,
            @NonNull UiTextNode<?> interruptMsg,
            @NonNull LeafNode finishMsg
    ) {
        super(rawNode, dirIndentLev, parentTotalIndentLev);
        this.title = title;
        this.body = body;
        this.interruptMsg = interruptMsg;
        this.finishMsg = finishMsg;
    }
    protected DirNode(
            @NonNull JsonNode rawNode, int dirIndentLev, int parentTotalIndentLev,
            @NonNull Int2ObjectMap<DirNode> useIndentCache,
            @NonNull LeafNode title,
            @NonNull UiTextNode<?> body,
            @NonNull UiTextNode<?> interruptMsg,
            @NonNull LeafNode finishMsg
    ) {
        super(rawNode, dirIndentLev, parentTotalIndentLev, useIndentCache);
        this.title = title;
        this.body = body;
        this.interruptMsg = interruptMsg;
        this.finishMsg = finishMsg;
    }

    public static @NonNull DirNode of(DirIndentFormat dirIndentFormat, JsonNode jNode, int parentTotalIndentLev)
            throws IllegalArgumentException {

        int dirIndentLev = UiTextNode.getIndentLevOf(jNode);
        int parentTotalIndentLevOfChildren = parentTotalIndentLev + dirIndentLev;
        LeafNode title;
        UiTextNode<?> body;
        UiTextNode<?> interruptMsg;
        LeafNode finishMsg;
        try {

            JsonNode jTitle = jNode.path("title");
            JsonNode jBody = jNode.path("body");
            JsonNode jInterruptMsg = jNode.path("interruptMsg");
            JsonNode jFinishMsg = jNode.path("finishMsg");

            title = LeafNode.of(
                    jTitle,
                    parentTotalIndentLevOfChildren,
                    dirIndentFormat.title()
            );
            body = UiTextNode.of(
                    jBody,
                    parentTotalIndentLevOfChildren,
                    dirIndentFormat.body()
            );
            interruptMsg = UiTextNode.of(
                    jInterruptMsg,
                    parentTotalIndentLevOfChildren,
                    dirIndentFormat.interruptMsg()
            );
            finishMsg = LeafNode.of(
                    jFinishMsg,
                    parentTotalIndentLevOfChildren,
                    dirIndentFormat.finishMsg()
            );

            if (body.isMissing()) { throw nodeShouldNotMissing("body", jBody); }
            if (interruptMsg.isMissing()) { throw nodeShouldNotMissing("interruptMsg", jInterruptMsg); }

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

    public @NonNull UiTextNode<?> body() {
        return body;
    }

    public @NonNull UiTextNode<?> interruptMsg() {
        return interruptMsg;
    }

    public @NonNull LeafNode finishMsg() {
        return finishMsg;
    }

    @Override
    protected @NonNull DirNode self() {
        return this;
    }

    @Override
    public @NonNull DirNode immutableSetParentIndent(int newParentTotalIndentLev) {
        return new DirNode(
                this.rawNode, this.indentLev, newParentTotalIndentLev, this.useIndentCache,
                this.title.useIndent(newParentTotalIndentLev),
                this.body.useIndent(newParentTotalIndentLev),
                this.interruptMsg.useIndent(newParentTotalIndentLev),
                this.finishMsg.useIndent(newParentTotalIndentLev)
        );
    }

    @Override
    public @NonNull UiTextNode<?> path(@NonNull String propertyName) {
        return switch (propertyName) {
            case "title" -> title;
            case "body" -> body;
            case "interruptMsg" -> interruptMsg;
            case "finishMsg" -> finishMsg;
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


    private static IllegalArgumentException nodeShouldNotMissing(@NonNull String propertyName, @NonNull JsonNode correspondingJNode)
            throws IllegalArgumentException {
        throw new IllegalArgumentException(String.format(
                "node.%s (%s) should not be missing", propertyName, correspondingJNode
        ));
    }
    private static IllegalArgumentException nodeTextShouldNotNull(String propertyName, JsonNode correspondingJNode)
            throws IllegalArgumentException {
        throw new IllegalArgumentException(String.format(
                "node.%s (%s) should not have value of null", propertyName, correspondingJNode
        ));
    }

}