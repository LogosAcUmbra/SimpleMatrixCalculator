package me.LogosAcUmbra.UiText;

import org.jspecify.annotations.NonNull;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.Map;
import java.util.Optional;


public class LeafNode extends ExistingNode {

    protected final @NonNull String text;

    protected LeafNode(
            @NonNull JsonNode rawNode, int indentLev, int parentTotalIndentLev,
            @NonNull String text
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
        int indentLev = getIndentLevOf(rawNode, defaultIndentLev);
        return tryOfInternal(rawNode, indentLev, parentTotalIndentLev);
    }


    protected static @NonNull LeafNode ofExistJNode(@NonNull JsonNode rawNode, int parentTotalIndentLev, int defaultIndentLev) {
        int indentLev = getIndentLevOf(rawNode, defaultIndentLev);
        return ofInternal(rawNode, indentLev, parentTotalIndentLev);
    }
    protected static @NonNull Optional<LeafNode> tryOfExistJNode(@NonNull JsonNode rawNode, int parentTotalIndentLev, int defaultIndentLev) {
        int indentLev = getIndentLevOf(rawNode, defaultIndentLev);
        return tryOfInternal(rawNode, indentLev, parentTotalIndentLev);
    }

    /**
     * access: package + child
     * @param rawNode a <b> NOT MISSING </b> JsonNode instance (i.e. return false on {@link JsonNode#isMissingNode()})
     * @param indentLev the indentLev <b> PARSED </b> from rawNode (should get from {@link UiTextNode#getIndentLevOf})
     * @param parentTotalIndentLev parentTotalIndentLev
     * @return a new LeafNode instance
     * @throws IllegalArgumentException if the node is not in valid structure for an UiTextLeafNode instance
     */
    protected static @NonNull LeafNode ofInternal(@NonNull JsonNode rawNode, int indentLev, int parentTotalIndentLev)
            throws IllegalArgumentException
    {
        try {
            if (rawNode.isNull()) {
                return LeafNode.ofEmpty(rawNode, indentLev, parentTotalIndentLev);
            }
            if (rawNode.isString()) {
                return LeafNode.ofString(rawNode, indentLev, parentTotalIndentLev);
            }
            if (!rawNode.isObject()) {
                throw new IllegalArgumentException("node is neither null, string nor map.");
            }
            ObjectNode objNode = rawNode.asObject();
            JsonNode lineJNode = null;
            for (Map.Entry<String, JsonNode> pair : objNode.properties()) {
                if (pair.getKey().equals("indentLev")) {
                    continue;
                }
                if (!pair.getKey().equals("line")) {
                    throw new IllegalArgumentException("node must has no properties other than \"indentLev\" and \"line\" .");
                }
                // pair = "line" entry
                lineJNode = pair.getValue();
                // continue // construct after ensuring no other properties
            }
            if (lineJNode == null) { // missing node
                // LeafNode can have no explicit "line" property, assumed to be null
                return ofEmpty(
                        rawNode, indentLev, parentTotalIndentLev
                );
            }
            if (!lineJNode.isString()) { // "line" node has illegal value type
                throw new IllegalArgumentException("node.line has to be a String");
            }
            return new LeafNode(
                    rawNode, indentLev, parentTotalIndentLev,
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
     *     null, String, {"indentLev": int #optional, "line": String #optional }
     * </p>
     * @param rawNode a <b> NOT MISSING </b> JsonNode instance (i.e. return false on {@link JsonNode#isMissingNode()})
     * @param indentLev the indentLev <b> PARSED </b> from rawNode (should get from {@link UiTextNode#getIndentLevOf})
     * @param parentTotalIndentLev default value for indentLev if {rawNode.path("indentLev")} is missing
     * @return a new LeafNode instance wrapped with {@code Optional.of()}
     * if the node is in a valid structure for an UiTextLeafNode instance,
     * else {@code Optional.empty()}
     */
    static @NonNull Optional<LeafNode> tryOfInternal(@NonNull JsonNode rawNode, int indentLev, int parentTotalIndentLev) {
        if (rawNode.isNull()) {
            return Optional.of( LeafNode.ofEmpty(rawNode, indentLev, parentTotalIndentLev) );
        }
        if (rawNode.isString()) {
            return Optional.of( LeafNode.ofString(rawNode, indentLev, parentTotalIndentLev) );
        }
        if (!rawNode.isObject()) {
            return Optional.empty();
        }
        ObjectNode objNode = rawNode.asObject();
        JsonNode lineJNode = null;
        for (Map.Entry<String, JsonNode> pair : objNode.properties()) {
            if (pair.getKey().equals("indentLev")) {
                continue;
            }
            if (!pair.getKey().equals("line")) {
                return Optional.empty(); // LeafNode must not have any other properties
            }
            // pair = "line" entry
            lineJNode = pair.getValue();
            // continue // construct after ensuring no other properties
        }
        if (lineJNode == null) { // missing node
            // LeafNode can have no explicit "line" property, assumed to be null
            return Optional.of( ofEmpty(
                    rawNode, indentLev, parentTotalIndentLev
            ));
        }
        if (!lineJNode.isString()) { // "line" node has illegal value type
            return Optional.empty();
        }
        return Optional.of(
                new LeafNode(
                        rawNode, indentLev, parentTotalIndentLev,
                        lineJNode.stringValue()
                )
        );
    }

    /**
     * access: private helper
     * @return new LeafNode(
     * {@code jsonNode}, {@code indentLev}, {@code parentTotalIndentLev},
     * LeafNodeType.NULL, null
     * )
     * */
    private static @NonNull LeafNode ofEmpty(@NonNull JsonNode rawNode, int indentLev, int parentTotalIndentLev) {
        return new LeafNode(
                rawNode, indentLev, parentTotalIndentLev,
                ""
        );
    }
    /**
     * private helper
     * @return new LeafNode(
     *            {@code rawNode}, {@code indentLev}, {@code parentTotalIndentLev},
     *            LeafNodeType.STRING, {@code rawNode}.stringValue()
     *    )
     * @throws tools.jackson.databind.exc.JsonNodeException -- from rawNode.stringValue()
     * */
    private static @NonNull LeafNode ofString(@NonNull JsonNode rawNode, int indentLev, int parentTotalIndentLev) {
        return new LeafNode(
                rawNode, indentLev, parentTotalIndentLev,
                rawNode.stringValue()
        );
    }

    @Override
    public @NonNull String txt() {
        return " ".repeat((indentLev + parentTotalIndentLev) * UiTextManager.getInstance().getSetting().indentSize) + text;
    }

    @Override
    public @NonNull String txt(Object... args) {
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
        return rawNode.isNull();
    }
    @Override
    public boolean isString() {
        return rawNode.isString();
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
    public @NonNull LeafNode useIndent(int newParentTotalIndentLev) {
        if (newParentTotalIndentLev == this.parentTotalIndentLev) {
            return this;
        }
        if (useIndentCache.containsKey(newParentTotalIndentLev)) {
            return (LeafNode) useIndentCache.get(newParentTotalIndentLev);
        }
        LeafNode result = new LeafNode(
                this.rawNode, this.indentLev, newParentTotalIndentLev,
                this.text
        );
        useIndentCache.put(newParentTotalIndentLev, result);
        return result;
    }
}
