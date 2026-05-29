package me.LogosAcUmbra.UiText;

import org.jspecify.annotations.NonNull;
import tools.jackson.databind.JsonNode;

public class PromptGroupNode extends ExistingNode {

    private final PromptNode choices;
    private final PromptNode rows;
    private final PromptNode cols;
    private final PromptNode elementAt;

    protected PromptGroupNode(
            @NonNull JsonNode jNode, int indentLev, int parentTotalIndentLev,
            @NonNull PromptNode choices, @NonNull PromptNode rows, @NonNull PromptNode cols, @NonNull PromptNode elementAt) {
        super(jNode, indentLev, parentTotalIndentLev);
        this.choices = choices;
        this.rows = rows;
        this.cols = cols;
        this.elementAt = elementAt;
    }

    public static @NonNull PromptGroupNode of(PromptIndentFormat promptIndentFormat, JsonNode jNode)
            throws IllegalArgumentException
    {

        PromptNode choices = PromptNode.of(promptIndentFormat, jNode.path("choices"));
        PromptNode rows = PromptNode.of(promptIndentFormat, jNode.path("rows"));
        PromptNode cols = PromptNode.of(promptIndentFormat, jNode.path("cols"));
        PromptNode elementAt = PromptNode.of(promptIndentFormat, jNode.path("elementAt"));
        return new PromptGroupNode(jNode, 0, 0, choices, rows, cols, elementAt);
    }

    @Override
    public @NonNull UiTextNode path(@NonNull String propertyName) {
        return switch (propertyName) {
            case ("choices") -> choices;
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

    public PromptNode choices() {return choices;}
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

    // @Override
    public @NonNull PromptGroupNode addIndentOf(UiTextNode node) {
        if (node.isMissing()) {
            throw new IllegalArgumentException("UiTextNode node is a missing node");
        }
        return useIndentOf( (ExistingNode) node );
    }

    // @Override
    public @NonNull PromptGroupNode addIndentOf(ExistingNode eNode) {
        return addIndent(eNode.parentTotalIndentLev + eNode.indentLev);
    }

    // @Override
    public @NonNull PromptGroupNode addIndent(int extraParentTotalIndentLev) {
        return useIndent(extraParentTotalIndentLev + this.parentTotalIndentLev);
    }

    @Override
    public @NonNull PromptGroupNode useIndent(int newParentTotalIndentLev) {
        if (newParentTotalIndentLev == this.parentTotalIndentLev) {
            return this;
        }
        if (useIndentCache.containsKey(newParentTotalIndentLev)) {
            return (PromptGroupNode) useIndentCache.get(newParentTotalIndentLev);
        }
        PromptGroupNode result =  new PromptGroupNode(
                this.rawNode, this.indentLev, newParentTotalIndentLev,
                choices.useIndent(newParentTotalIndentLev),
                rows.useIndent(newParentTotalIndentLev),
                cols.useIndent(newParentTotalIndentLev),
                elementAt.useIndent(newParentTotalIndentLev)
        );
        useIndentCache.put(newParentTotalIndentLev, result);
        return result;
    }
}
