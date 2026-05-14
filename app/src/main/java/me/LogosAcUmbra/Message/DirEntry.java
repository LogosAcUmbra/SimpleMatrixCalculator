package me.LogosAcUmbra.Message;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Objects;

public record DirEntry(
        int indentLev,
        @NonNull String title,
        @NonNull JsonNode txt,
        @Nullable String interruptMsg,
        @NonNull String finishMsg
) {
    public DirEntry {
        if (indentLev < 0) {
            throw new IllegalArgumentException("`indentLev` must be > 0");
        }
        Objects.requireNonNull(title, "`title` must be non null");
        Objects.requireNonNull(txt, "`txt` must be non null");
        // interruptMsg can be null if there is no function for interrupting
        Objects.requireNonNull(finishMsg, "`finishMsg` must be non null");
    }
}