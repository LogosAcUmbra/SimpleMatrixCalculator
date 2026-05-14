package me.LogosAcUmbra.Message;


import com.google.common.reflect.ClassPath;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class MessageManager {
    private final String jsonFileName = "messages.json";
    private final JsonNode messagesJsonRoot;
    private final ObjectMapper mapper;
    private int indentSize;
    private final PromptManager promptManager;

    public MessageManager(int indentSize) throws IOException {
        this.indentSize = indentSize;
        InputStream is = getClass().getClassLoader().getResourceAsStream(jsonFileName);
        if (is == null) {
            throw new FileNotFoundException("resources/messages.json does not exist");
        }
        try (is) {
            this.mapper = new ObjectMapper();
            this.messagesJsonRoot = this.mapper.readTree(is);
            this.promptManager = new PromptManager(this.mapper, this.messagesJsonRoot, this.indentSize);
        }
    }

    public PromptManager prompt() {
        return promptManager;
    }
    public void setIndentSize(int indentSize) {
        this.indentSize = indentSize;
        promptManager.setIndentSize(indentSize);
    }
}
