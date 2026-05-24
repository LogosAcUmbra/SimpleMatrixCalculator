package me.LogosAcUmbra.UiText;


import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static me.LogosAcUmbra.Utils.JsonConfig.JSON_MAPPER;

public class UiTextManager {
//
//    private static final @NonNull JsonMapper MAPPER = JSON_MAPPER;
//


    private static final @NonNull String JSON_FILENAME = "UiText.json";

    private static @Nullable UiTextManager INSTANCE;

    private final @NonNull Setting setting;
    private final @NonNull RootNode root;


    private UiTextManager(@NonNull Setting setting,
                        @NonNull RootNode root
    ) {
        this.setting = setting;
        this.root = root;
    }

    private static @NonNull UiTextManager create() throws IOException {
        JsonNode rootJsonNode;
        InputStream is = UiTextManager.class.getClassLoader().getResourceAsStream(JSON_FILENAME);
        if (is == null) {
            throw new FileNotFoundException("resources/UiText.json does not exist");
        }
        try (is) {
            rootJsonNode = JSON_MAPPER.readTree(is);
        }
        RootNode rootTextNode = RootNode.of(rootJsonNode);

        return new UiTextManager(Setting.of(2), rootTextNode);
    }

    // lazy
    public static @NonNull UiTextManager getInstance() {
        if (INSTANCE == null) {
            try {
                INSTANCE = create();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return INSTANCE;
    }

    public @NonNull RootNode root() {
        return root;
    }


    public @NonNull Setting getSetting() {
        return setting;
    }

}
