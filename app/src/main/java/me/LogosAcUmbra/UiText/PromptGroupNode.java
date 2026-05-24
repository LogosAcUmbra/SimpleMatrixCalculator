package me.LogosAcUmbra.UiText;

import org.jspecify.annotations.NonNull;
import tools.jackson.databind.JsonNode;

public class PromptGroupNode extends ExistingNode {

    private final PromptNode rows;
    private final PromptNode cols;
    private final PromptNode elementAt;

    protected PromptGroupNode(
            @NonNull JsonNode jNode, int indentLev, int parentTotalIndentLev,
            @NonNull PromptNode rows, @NonNull PromptNode cols, @NonNull PromptNode elementAt) {
        super(jNode, indentLev, parentTotalIndentLev);
        this.rows = rows;
        this.cols = cols;
        this.elementAt = elementAt;
    }

    public static @NonNull PromptGroupNode of(PromptIndentFormat promptIndentFormat, JsonNode jNode)
            throws IllegalArgumentException
    {

        PromptNode rows = PromptNode.of(promptIndentFormat, jNode.path("rows"));
        PromptNode cols = PromptNode.of(promptIndentFormat, jNode.path("cols"));
        PromptNode elementAt = PromptNode.of(promptIndentFormat, jNode.path("elementAt"));
        return new PromptGroupNode(jNode, 0, 0, rows, cols, elementAt);
    }

    @Override
    public @NonNull UiTextNode path(@NonNull String propertyName) {
        return switch (propertyName) {
            case ("rows") -> rows;
            case ("cols") -> cols;
            case ("elementAt") -> elementAt;
            default -> MissingNode.getInstance();
        };
    }

    @Override
    public @NonNull UiTextNode path(int index) {
        return MissingNode.getInstance();
    }

    public PromptNode rows() {
        return rows;
    }
    public PromptNode cols() {
        return cols;
    }
    public PromptNode elementAt() {
        return elementAt;
    }

    @Override
    public @NonNull ExistingNodeType getExistingNodeType() {
        return ExistingNodeType.FIXED_BRANCH;
    }

    @Override
    public @NonNull PromptGroupNode useIndentOf(UiTextNode node) {
        if (node.isMissing()) {
            throw new IllegalArgumentException("UiTextNode node is a missing node");
        }
        return useIndentOf( (ExistingNode) node );
    }

    @Override
    public @NonNull PromptGroupNode useIndentOf(ExistingNode eNode) {
        return useIndent(eNode.parentTotalIndentLev + eNode.indentLev);
    }

    @Override
    public @NonNull PromptGroupNode useIndent(int newExtraIndentLev) {
        return new PromptGroupNode(
                this.rawNode, this.indentLev, newExtraIndentLev,
                rows.useIndent(newExtraIndentLev), cols.useIndent(newExtraIndentLev), elementAt.useIndent(newExtraIndentLev)
        );
    }

//    @Override
//    public @NonNull PromptGroupNode useIndentOf(UiTextNode node) throws IllegalArgumentException {
//        if (node.isMissing()) {
//            throw new IllegalArgumentException("the given node is a missing node");
//        }
//        ExistingNode eNode = (ExistingNode) node;
//        return of(
//                this.setting, this.rawNode, this.indentLev, eNode.parentTotalIndentLev,
//                this.rows, this.cols, this.elementAt
//        );
//    }
}
