package me.LogosAcUmbra.UiText;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.jspecify.annotations.NonNull;
import tools.jackson.databind.JsonNode;

public class DirGroupNode extends ExistingNode<DirGroupNode> {

    private final @NonNull DirNode menu;
    private final @NonNull DirNode createMatrix;

    protected DirGroupNode(@NonNull JsonNode rawNode, int indentLev,
                           @NonNull DirNode menu, @NonNull DirNode createMatrix) {
        super(rawNode, indentLev, 0);
        this.menu = menu;
        this.createMatrix = createMatrix;
    }

    protected DirGroupNode(@NonNull JsonNode rawNode, int indentLev, int parentTotalIndentLev,
                           @NonNull Int2ObjectMap<DirGroupNode> useIndentCache,
                           @NonNull DirNode menu, @NonNull DirNode createMatrix) {
        super(rawNode, indentLev, parentTotalIndentLev, useIndentCache);
        this.menu = menu;
        this.createMatrix = createMatrix;
    }

    public static @NonNull DirGroupNode of(@NonNull DirIndentFormat dirIndentFormat, JsonNode jNode) {
        int indentLev = UiTextNode.getIndentLevOf(jNode);
        try {
            DirNode menu = DirNode.of(dirIndentFormat, jNode.path("menu"), indentLev);
            DirNode createMatrix = DirNode.of(dirIndentFormat, jNode.path("createMatrix"), indentLev);
            return new DirGroupNode(jNode, indentLev, menu, createMatrix);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    String.format("node (%s) cannot be parsed to DirGroup", jNode),
                    e
            );
        }
    }

    public DirNode menu() {
        return menu;
    }
    public DirNode createMatrix() {
        return createMatrix;
    }


    @Override
    protected @NonNull DirGroupNode self() {
        return this;
    }

    @Override
    public @NonNull DirGroupNode immutableSetParentIndent(int newParentTotalIndentLev) {
        return new DirGroupNode(
                this.rawNode, this.indentLev, newParentTotalIndentLev, this.useIndentCache,
                this.menu.useIndent(newParentTotalIndentLev), this.createMatrix.useIndent(newParentTotalIndentLev)
        );
    }

    @Override
    public @NonNull UiTextNode<?> path(@NonNull String propertyName) {
        return switch (propertyName) {
            case ("menu") -> menu;
            case ("createMatrix") -> createMatrix;
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
