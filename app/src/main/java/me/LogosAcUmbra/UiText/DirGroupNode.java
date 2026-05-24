package me.LogosAcUmbra.UiText;

import org.jspecify.annotations.NonNull;
import tools.jackson.databind.JsonNode;

public class DirGroupNode extends ExistingNode {

    private final @NonNull DirNode menu;
    private final @NonNull DirNode createMatrix;

    protected DirGroupNode(@NonNull JsonNode rawNode, int indentLev, int parentTotalIndentLev,
                           @NonNull DirNode menu, @NonNull DirNode createMatrix) {
        super(rawNode, indentLev, parentTotalIndentLev);
        this.menu = menu;
        this.createMatrix = createMatrix;
    }

    public static @NonNull DirGroupNode of(@NonNull DirIndentFormat dirIndentFormat, JsonNode jNode) {
        int indentLev = UiTextNode.getIndentLevOf(jNode);
        try {
            DirNode menu = DirNode.of(dirIndentFormat, jNode.path("menu"), indentLev);
            DirNode createMatrix = DirNode.of(dirIndentFormat, jNode.path("createMatrix"), indentLev);
            return new DirGroupNode(jNode, indentLev, 0, menu, createMatrix);
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
    public @NonNull UiTextNode path(@NonNull String propertyName) {
        return switch (propertyName) {
            case ("menu") -> menu;
            case ("createMatrix") -> createMatrix;
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
    public @NonNull DirGroupNode useIndentOf(UiTextNode node) {
        if (node.isMissing()) {
            throw new IllegalArgumentException("UiTextNode node is a missing node");
        }
        return useIndentOf( (ExistingNode) node );
    }

    @Override
    public @NonNull DirGroupNode useIndentOf(ExistingNode eNode) {
        return useIndent(eNode.parentTotalIndentLev + eNode.indentLev);
    }

    @Override
    public @NonNull DirGroupNode useIndent(int newExtraIndentLev) {
        return new DirGroupNode(
                this.rawNode, this.indentLev, newExtraIndentLev,
                this.menu.useIndent(newExtraIndentLev), this.createMatrix.useIndent(newExtraIndentLev)
        );
    }
}
