package me.LogosAcUmbra.UiText;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;

import java.util.Optional;


public class LeafNode extends ExistingNode {

    protected @Nullable String text;

    protected LeafNode(
            @NonNull JsonNode rawNode, int indentLev, int parentTotalIndentLev,
            @Nullable String text
    ) {
        super(rawNode, indentLev, parentTotalIndentLev);
        this.text = text;
    }

    public static @NonNull LeafNode of(@NonNull JsonNode rawNode)
            throws IllegalArgumentException {
        return of(rawNode, 0, 0);
    }
    public static @NonNull LeafNode of(@NonNull JsonNode rawNode, int parentTotalIndentLev)
            throws IllegalArgumentException {
        return of(rawNode, parentTotalIndentLev, 0);
    }
    public static @NonNull LeafNode of(@NonNull JsonNode rawNode, int parentTotalIndentLev, int defaultIndentLev)
            throws IllegalArgumentException {
        if (rawNode.isMissingNode()) {
            throw new IllegalArgumentException("node is missing node");
        }
        return ofExistJNode(rawNode, parentTotalIndentLev, defaultIndentLev);
    }
    public static @NonNull Optional<LeafNode> tryOf(@NonNull JsonNode rawNode) {
        return tryOf(rawNode, 0, 0);
    }
    public static @NonNull Optional<LeafNode> tryOf(@NonNull JsonNode rawNode, int parentTotalIndentLev) {
        return tryOf(rawNode, parentTotalIndentLev, 0);
    }
    public static @NonNull Optional<LeafNode> tryOf(@NonNull JsonNode rawNode, int parentTotalIndentLev, int defaultIndentLev) {
        if (rawNode.isMissingNode()) {
            return Optional.empty();
        }
        return tryOfExistJNode(rawNode, parentTotalIndentLev, defaultIndentLev);
    }

    /**
     * access: package + child
     * @param rawNode a <b> NOT MISSING </b> JsonNode instance
     * @param parentTotalIndentLev parentTotalIndentLev
     * @param defaultIndentLev default value for indentLev if {rawNode.path("indentLev")} is missing
     * @return a new LeafNode instance
     * @throws IllegalArgumentException if the node is not in valid structure for an UiTextLeafNode instance
     */
    protected static @NonNull LeafNode ofExistJNode(@NonNull JsonNode rawNode, int parentTotalIndentLev, int defaultIndentLev)
            throws IllegalArgumentException
    {
        try {
            if (rawNode.isNull()) {
                return LeafNode.ofNull(rawNode, parentTotalIndentLev, defaultIndentLev);
            }
            if (rawNode.isString()) {
                return LeafNode.ofString(rawNode, parentTotalIndentLev, defaultIndentLev);
            }
            if (!rawNode.isObject()) {
                throw new IllegalArgumentException("node is neither null, string nor map.");
            }
            JsonNode indentLevJNode = rawNode.path("indentLev");
            if (indentLevJNode.isMissingNode()) {
                throw new IllegalArgumentException("node is map but node.indentLev is missing");
            }
            if (!indentLevJNode.isInt()) {
                throw new IllegalArgumentException("node is map but node.indentLev is not an integer");
            }
            JsonNode lineJNode = rawNode.path("line");
            if (lineJNode.isMissingNode()) {
                return new LeafNode(
                        rawNode, indentLevJNode.intValue(), parentTotalIndentLev,
                        null
                );
            }
            if (!lineJNode.isString()) {
                throw new IllegalArgumentException("node is map but node.line is not a string");
            }
            return new LeafNode(
                    rawNode, indentLevJNode.intValue(), parentTotalIndentLev,
                    lineJNode.stringValue()
            );
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    String.format("node (%s) does not contain a valid UiTextLeafNode structure", rawNode),
                    e
            );
        }
    }


    /**
     * access: package + child
     * <p>
     *     null, String, {"indentLev": int, Optional("text": String]) }
     * </p>
     * @param rawNode a <b> NOT MISSING </b> JsonNode instance
     * @param parentTotalIndentLev parentTotalIndentLev
     * @param defaultIndentLev default value for indentLev if {rawNode.path("indentLev")} is missing
     * @return a new LeafNode instance wrapped with {@code Optional.of()}
     * if the node is in a valid structure for an UiTextLeafNode instance,
     * else {@code Optional.empty()}
     */
    static @NonNull Optional<LeafNode> tryOfExistJNode(@NonNull JsonNode rawNode, int parentTotalIndentLev, int defaultIndentLev) {
        if (rawNode.isMissingNode()) {
            return Optional.empty();
        }
        if (rawNode.isNull()) {
            return Optional.of( LeafNode.ofNull(rawNode, parentTotalIndentLev, defaultIndentLev) );
        }
        if (rawNode.isString()) {
            return Optional.of( LeafNode.ofString(rawNode, parentTotalIndentLev, defaultIndentLev) );
        }
        if (!rawNode.isObject()) {
            return Optional.empty();
        }
        JsonNode indentLevJNode = rawNode.path("indentLev");
        if (indentLevJNode.isMissingNode() || !indentLevJNode.isInt()) {
            return Optional.empty();
        }
        JsonNode lineJNode = rawNode.path("line");
        if (!lineJNode.isMissingNode()) {
            if (!lineJNode.isString()) {
                return Optional.empty();
            }
            return Optional.of(
                    new LeafNode(
                            rawNode, indentLevJNode.intValue(), parentTotalIndentLev,
                            lineJNode.stringValue()
                    )
            );
        }
        return Optional.of(
                new LeafNode(
                        rawNode, indentLevJNode.intValue(), parentTotalIndentLev,
                        null
                )
        );
    }

    /**
     * access: private helper
     * @return new LeafNode(
     * {@code jsonNode}, {@code defaultIndentLev}, {@code parentTotalIndentLev},
     * LeafNodeType.NULL, null
     * )
     * */
    private static @NonNull LeafNode ofNull(@NonNull JsonNode rawNode, int parentTotalIndentLev, int defaultIndentLev) {
        return new LeafNode(
                rawNode, defaultIndentLev, parentTotalIndentLev,
                null
        );
    }
    /**
     * private helper
     * @return new LeafNode(
     *            {@code rawNode}, {@code defaultIndentLev}, {@code parentTotalIndentLev},
     *            LeafNodeType.STRING, {@code rawNode}.stringValue()
     *    )
     * @throws tools.jackson.databind.exc.JsonNodeException -- from rawNode.stringValue()
     * */
    private static @NonNull LeafNode ofString(@NonNull JsonNode rawNode, int parentTotalIndentLev, int defaultIndentLev) {
        return new LeafNode(
                rawNode, defaultIndentLev, parentTotalIndentLev,
                rawNode.stringValue()
        );
    }

    @Override
    public @Nullable String get() {
        return " ".repeat((indentLev + parentTotalIndentLev) * UiTextManager.getInstance().getSetting().indentSize) + text;
    }

    @Override
    public @NonNull String get(Object... args) {
        if (text == null) {
            throw new IllegalArgumentException(String.format("node (%s) is not string", rawNode));
        }
        return " ".repeat((indentLev + parentTotalIndentLev) * UiTextManager.getInstance().getSetting().indentSize) + String.format(text, args);
    }

    @Override
    public @NonNull UiTextNode path(@NonNull String propertyName) {
        return MissingNode.getInstance();
    }

    @Override
    public @NonNull UiTextNode path(int index) {
        return MissingNode.getInstance();
    }

    @Override
    public boolean isLeaf() {
        return true;
    }
    @Override
    public boolean isNull() {
        return text == null;
    }
    @Override
    public boolean isString() {
        return text != null;
    }

    @Override
    public @NonNull ExistingNodeType getExistingNodeType() {
        return ExistingNodeType.LEAF;
    }

    @Override
    public @NonNull LeafNode useIndentOf(ExistingNode eNode) {
        return useIndent(eNode.parentTotalIndentLev + eNode.indentLev);
    }

    @Override
    public @NonNull LeafNode useIndent(int newExtraIndentLev) {
        return new LeafNode(
                this.rawNode, this.indentLev, newExtraIndentLev,
                this.text
        );
    }
}
