package me.LogosAcUmbra.UiText;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jspecify.annotations.NonNull;
import tools.jackson.databind.JsonNode;

public class PromptGroupNode extends ExistingNode<PromptGroupNode> {

    private final PromptNode choices;
    private final PromptNode rows;
    private final PromptNode cols;
    private final PromptNode elementAt;

    protected PromptGroupNode(
            @NonNull JsonNode jNode,
            @NonNull PromptNode choices, @NonNull PromptNode rows, @NonNull PromptNode cols, @NonNull PromptNode elementAt) {
        super(jNode, 0, 0);
        this.choices = choices;
        this.rows = rows;
        this.cols = cols;
        this.elementAt = elementAt;
    }

    protected PromptGroupNode(
            @NonNull JsonNode jNode, int indentLev, int parentTotalIndentLev,
            @NonNull Int2ObjectMap<PromptGroupNode> useIndentCache,
            @NonNull PromptNode choices, @NonNull PromptNode rows, @NonNull PromptNode cols, @NonNull PromptNode elementAt) {
        super(jNode, indentLev, parentTotalIndentLev, useIndentCache);
        this.choices = choices;
        this.rows = rows;
        this.cols = cols;
        this.elementAt = elementAt;
    }

    public static @NonNull PromptGroupNode of(PromptIndentFormat promptIndentFormat, JsonNode jNode)
            throws IllegalArgumentException
    {
        int indentLev = UiTextNode.getIndentLevOf(jNode);
        if (indentLev != 0) {
            throw new IllegalArgumentException("prompts root cannot have indent lev");
        }
        PromptNode choices = PromptNode.of(promptIndentFormat, jNode.path("choices"));
        PromptNode rows = PromptNode.of(promptIndentFormat, jNode.path("rows"));
        PromptNode cols = PromptNode.of(promptIndentFormat, jNode.path("cols"));
        PromptNode elementAt = PromptNode.of(promptIndentFormat, jNode.path("elementAt"));
        return new PromptGroupNode(jNode, choices, rows, cols, elementAt);
    }

    @Override
    protected @NonNull PromptGroupNode self() {
        return this;
    }

    @Override
    public @NonNull PromptGroupNode immutableSetParentIndent(int newParentTotalIndentLev) {
        return new PromptGroupNode(
                this.rawNode, this.indentLev, newParentTotalIndentLev, this.useIndentCache,
                choices.useIndent(newParentTotalIndentLev),
                rows.useIndent(newParentTotalIndentLev),
                cols.useIndent(newParentTotalIndentLev),
                elementAt.useIndent(newParentTotalIndentLev)
        );
    }

    @Override
    public @NonNull UiTextNode<?> path(@NonNull String propertyName) {
        return switch (propertyName) {
            case ("choices") -> choices;
            case ("rows") -> rows;
            case ("cols") -> cols;
            case ("elementAt") -> elementAt;
            default -> MissingNode.getInstance();
        };
    }

    @Override
    public @NonNull UiTextNode<?> path(int index) {
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
}
