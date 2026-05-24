package me.LogosAcUmbra.UiText;

import org.jspecify.annotations.NonNull;
import tools.jackson.databind.JsonNode;

import static me.LogosAcUmbra.Utils.JsonConfig.JSON_MAPPER;

public class RootNode extends ExistingNode {

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

    protected static @NonNull RootNode create(JsonNode rootNode, PromptGroupNode prompts, DirGroupNode dirs) {
        return new RootNode(
                rootNode, 0, 0,
                prompts, dirs
        );
    }

    public static @NonNull RootNode of(JsonNode rootNode) {
        PromptIndentFormat promptIndentFormat =
                JSON_MAPPER.treeToValue(rootNode.path("promptIndentFormat"), PromptIndentFormat.class);
        DirIndentFormat dirIndentFormat =
                JSON_MAPPER.treeToValue(rootNode.path("dirIndentFormat"), DirIndentFormat.class);

        PromptGroupNode prompts = PromptGroupNode.of(promptIndentFormat, rootNode.path("prompts"));
        DirGroupNode dirs = DirGroupNode.of(dirIndentFormat, rootNode.path("dirs"));
        return create(rootNode, prompts, dirs);
    }

    public PromptGroupNode prompts() {
        return prompts;
    }
    public DirGroupNode dirs() {
        return dirs;
    }

    @Override
    public @NonNull UiTextNode path(@NonNull String propertyName) {
        return switch (propertyName) {
            case ("prompt") -> prompts;
            case ("dir") -> dirs;
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
    public @NonNull RootNode useIndentOf(UiTextNode node) {
        if (node.isMissing()) {
            throw new IllegalArgumentException("UiTextNode node is a missing node");
        }
        return useIndentOf( (ExistingNode) node );
    }

    @Override
    public @NonNull RootNode useIndentOf(ExistingNode eNode) {
        return useIndent(eNode.parentTotalIndentLev + eNode.indentLev);
    }

    @Override
    public @NonNull RootNode useIndent(int newExtraIndentLev) {
        return new RootNode(
                this.rawNode, this.indentLev, newExtraIndentLev,
                this.prompts.useIndent(newExtraIndentLev), this.dirs.useIndent(newExtraIndentLev)
        );
    }
}
