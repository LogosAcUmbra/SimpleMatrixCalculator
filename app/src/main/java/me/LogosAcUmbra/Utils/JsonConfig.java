package me.LogosAcUmbra.Utils;

import tools.jackson.databind.json.JsonMapper;

public class JsonConfig {

    // Prevent anyone from instantiating this utility class
    private JsonConfig() {}

    public static final JsonMapper JSON_MAPPER = JsonMapper.builder()
            //.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();
}