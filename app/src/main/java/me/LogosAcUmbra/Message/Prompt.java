package me.LogosAcUmbra.Message;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public record Prompt(
        @NonNull String ask,
        @NonNull String err,
        @Nullable String quitMsg,
        @Nullable String quitSpecifier
) {

    public Prompt {
        Objects.requireNonNull(ask, "`ask` cannot be null");
        // err can be null when there is no criteria needed (although it is rare)
        // quitMsg and quitSpecifier can be null when there is no quitMsg function for the prompt
        if (Objects.isNull(quitMsg) ^ Objects.isNull(quitSpecifier)) {
            throw new IllegalArgumentException(
                    "`quitMsg` and `quitSpecifier` must both be null or both be non-null"
            );
        }
    }

}
