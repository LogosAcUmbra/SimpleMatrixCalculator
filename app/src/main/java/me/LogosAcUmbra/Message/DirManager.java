package me.LogosAcUmbra.Message;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public class DirManager {

    public enum DirMsgType {
        TITLE, INTERRUPT_MSG, FINISH_MSG
    }

    private DirMsgIndent msgIndent;

    private DirEntry menu;
    private DirEntry createMatrix;

    private int indentSize;

    public DirManager(JsonNode rootNode, ObjectMapper mapper)
            throws JacksonException {

        this.msgIndent = mapper.treeToValue(rootNode.path("msgIndent"), DirMsgIndent.class);

        this.menu = mapper.treeToValue(rootNode.path("menu"), DirEntry.class);
        this.createMatrix = mapper.treeToValue(rootNode.path("createMatrix"), DirEntry.class);
    }

    public String menu(DirMsgType msgType) {
        return " ".repeat(menu.indentLev() * indentSize) + indented(menu, msgType);
    }
    public String createMatrix(DirMsgType msgType) {
        return " ".repeat(createMatrix.indentLev() * indentSize) + indented(createMatrix, msgType);
    }

    public String menuCore(Object... paths) {
        return getIndentedStrFromNodePaths(menu.txt(), paths);
    }
    public String createMatrixCore(Object... paths) {
        return getIndentedStrFromNodePaths(createMatrix.txt(), paths);
    }

    public String menu(JsonNode node) {
        return " ".repeat(menu.indentLev() * indentSize) + node.asString();
    }

    private String indented(DirEntry dirEntry, DirMsgType msgType) {
        int indentLev;
        String msg;
        switch (msgType) {
//            case INDENT_LEV:
//                throw new IllegalArgumentException("indentLev is an integer, not a message");
            case TITLE:
                indentLev = msgIndent.title();
                msg = dirEntry.title();
                break;
//            case TXT:
//                throw new IllegalArgumentException("txt is a pack of messages, not a message");
            case INTERRUPT_MSG:
                indentLev = msgIndent.interruptMsg();
                msg = dirEntry.interruptMsg();
                break;
            case FINISH_MSG:
                indentLev = msgIndent.finishMsg();
                msg = dirEntry.finishMsg();
                break;
            default:
                throw new RuntimeException("Unreachable Code Reached");
        }
        return " ".repeat(indentLev * indentSize) + msg;
    }
    public String getIndentedStrFromNodePaths(JsonNode node, Object... paths) {
        String msg;
        for (Object path : paths) {
            if (path instanceof String s) {
                node = node.path(s);
                if (node.isMissingNode()) {
                    throw new IllegalArgumentException(String.format("path `%s` does not exists", s));
                }
            } else if (path instanceof Integer i) {
                node = node.path(i);
                if (node.isMissingNode()) {
                    throw new IllegalArgumentException(String.format("path `%d` does not exists", i));
                }
            } else {
                throw new IllegalArgumentException("Invalid Type");
            }
        }
        msg = node.asString();
        return " ".repeat(menu.indentLev() * indentSize) + msg;
    }

}
