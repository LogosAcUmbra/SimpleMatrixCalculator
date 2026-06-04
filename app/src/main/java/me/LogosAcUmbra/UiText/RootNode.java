package me.LogosAcUmbra.UiText;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.jspecify.annotations.NonNull;
import tools.jackson.databind.JsonNode;

import static me.LogosAcUmbra.Utils.JsonConfig.JSON_MAPPER;

public class RootNode extends ExistingNode<RootNode> {

    protected PromptGroupNode prompts;
    protected DirGroupNode dirs;

    protected RootNode(
            JsonNode rootNode, int indentLev, int parenTotalIndentLev,
            PromptGroupNode prompts, DirGroupNode dirs
    ) {
        super(rootNode, indentLev, parenTotalIndentLev);
        this.prompts = prompts;
        this.dirs = dirs;
    }

    protected RootNode(
            JsonNode rootNode, int indentLev, int parenTotalIndentLev, Int2ObjectMap<RootNode> useIndentCache,
            PromptGroupNode prompts, DirGroupNode dirs
    ) {
        super(rootNode, indentLev, parenTotalIndentLev, useIndentCache);
        this.prompts = prompts;
        this.dirs = dirs;
    }

    protected static @NonNull RootNode create(JsonNode rootNode, PromptGroupNode prompts, DirGroupNode dirs) {
        return new RootNode(
                rootNode, 0, 0,
                prompts, dirs
        );
    }

    @Override
    protected @NonNull RootNode self() {
        return this;
    }

    public static @NonNull RootNode of(JsonNode rootNode) {
        int indentLev = UiTextNode.getIndentLevOf(rootNode);
        if (indentLev != 0) {
            throw new IllegalArgumentException("prompts root cannot have extra indent lev");
        }

        PromptIndentFormat promptIndentFormat =
                JSON_MAPPER.treeToValue(rootNode.path("promptIndentFormat"), PromptIndentFormat.class);
        DirIndentFormat dirIndentFormat =
                JSON_MAPPER.treeToValue(rootNode.path("dirIndentFormat"), DirIndentFormat.class);

        PromptGroupNode prompts = PromptGroupNode.of(promptIndentFormat, rootNode.path("prompts"));
        DirGroupNode dirs = DirGroupNode.of(dirIndentFormat, rootNode.path("dirs"));
        return create(rootNode, prompts, dirs);
    }

    @Override
    public @NonNull RootNode immutableSetParentIndent(int newParentTotalIndentLev) {
        return new RootNode(
                this.rawNode, this.indentLev, newParentTotalIndentLev, this.useIndentCache,
                this.prompts.useIndent(newParentTotalIndentLev), this.dirs.useIndent(newParentTotalIndentLev)
        );
    }

    public PromptGroupNode prompts() {
        return prompts;
    }
    public DirGroupNode dirs() {
        return dirs;
    }

    @Override
    public @NonNull UiTextNode<?> path(@NonNull String propertyName) {
        return switch (propertyName) {
            case ("prompt") -> prompts;
            case ("dir") -> dirs;
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

}
