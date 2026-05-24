package me.LogosAcUmbra.UiText;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DirIndentFormat(
    int title, int body, int interruptMsg, int finishMsg
) {
    public DirIndentFormat(
            @JsonProperty("title") int title,
            @JsonProperty("body") int body,
            @JsonProperty("interruptMsg") int interruptMsg,
            @JsonProperty("finishMsg") int finishMsg
    ) {
        if (title < 0) {
            throw new IllegalArgumentException("msgIndent for title must be > 0");
        }
        if (body < 0) {
            throw new IllegalArgumentException("msgIndent for body must be > 0");
        }
        if (interruptMsg < 0) {
            throw new IllegalArgumentException("msgIndent for interruptMsg must be > 0");
        }
        if (finishMsg < 0) {
            throw new IllegalArgumentException("msgIndent for finishMsg must be > 0");
        }

        this.title = title;
        this.body = body;
        this.interruptMsg = interruptMsg;
        this.finishMsg = finishMsg;
    }
}
