package me.LogosAcUmbra.Message;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public class PromptManager {

    public enum PromptMsgType {
        ASK, ERR, QUIT_MSG, QUIT_SPECIFIER
    }

    private final ObjectMapper mapper;

    private final PromptMsgIndent promptMsgIndent;

    //    private final PromptGroup promptGroup;
    private final Prompt rows;
    private final Prompt cols;
    private final Prompt elementAt;

    private int indentSize = 2;

    protected PromptManager(ObjectMapper mapper, JsonNode rootNode, int indentSize) {
        this.mapper = mapper;

        JsonNode pilrNode = rootNode.path("promptIndentLevRelative");
        this.promptMsgIndent = mapper.treeToValue(pilrNode, PromptMsgIndent.class);

        JsonNode promptNode = rootNode.path("prompt");
        this.rows = mapper.treeToValue(promptNode, Prompt.class);
        this.cols = mapper.treeToValue(promptNode, Prompt.class);
        this.elementAt = mapper.treeToValue(promptNode, Prompt.class);

        this.indentSize = indentSize;
    }


    public PromptMsgIndent getPromptIndentLevRelative() {
        return promptMsgIndent;
    }

    public String rows(PromptMsgType field) {
        return indented(rows, field);
    }
    public String cols(PromptMsgType field) {
        return indented(cols, field);
    }
    public String elementAt(PromptMsgType field) {
        return indented(elementAt, field);
    }

    public String fRows(PromptMsgType field, Object... args) { return String.format(indented(rows, field), args); }
    public String fCols(PromptMsgType field, Object... args) { return String.format(indented(cols, field), args); }
    public String fElementAt(PromptMsgType field, Object... args) { return String.format(indented(elementAt, field), args); }

    private String indented(Prompt prompt, PromptMsgType field) {
        int indentLev;
        String msg;
        switch (field) {
            case PromptMsgType.ASK:
                indentLev = promptMsgIndent.ask();
                msg = prompt.ask(); break;
            case PromptMsgType.ERR:
                indentLev = promptMsgIndent.err();
                msg = prompt.err(); break;
            case PromptMsgType.QUIT_MSG:
                indentLev = promptMsgIndent.quitMsg();
                msg = prompt.quitMsg(); break;
            case PromptMsgType.QUIT_SPECIFIER:
                return prompt.quitSpecifier();
            default:
                throw new RuntimeException("Unreachable Code Reached");
        }
        return " ".repeat(indentLev * indentSize) + msg;
    }
    public void setIndentSize(int indentSize) {
        this.indentSize = indentSize;
    }
}
