package me.LogosAcUmbra.Message;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DirMsgIndent (
    int title, int txt, int interruptMsg, int finishMsg
) {
    public DirMsgIndent(
            @JsonProperty("title") int title,
            @JsonProperty("txt") int txt,
            @JsonProperty("interruptMsg") int interruptMsg,
            @JsonProperty("finishMsg") int finishMsg
    ) {
        if (title < 0) {
            throw new IllegalArgumentException("msgIndent for title must be > 0");
        }
        if (txt < 0) {
            throw new IllegalArgumentException("msgIndent for txt must be > 0");
        }
        if (interruptMsg < 0) {
            throw new IllegalArgumentException("msgIndent for interruptMsg must be > 0");
        }
        if (finishMsg < 0) {
            throw new IllegalArgumentException("msgIndent for finishMsg must be > 0");
        }

        this.title = title;
        this.txt = txt;
        this.interruptMsg = interruptMsg;
        this.finishMsg = finishMsg;
    }
}
