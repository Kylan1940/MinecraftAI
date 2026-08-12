package org.kylan1940.minecraftai.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.kylan1940.minecraftai.MinecraftAI;

import java.util.ArrayList;
import java.util.List;

public class Conversation {

    private final List<Message> messages = new ArrayList<>();

    public void addUserMessage(String message) {
        messages.add(new Message("user", message));
    }

    public void addModelMessage(String message) {
        messages.add(new Message("model", message));
    }

    private void addMessage(String role, String text) {

        int maxMessages = MinecraftAI.getInstance()
                .getConfig()
                .getInt("ai.conversation.max-messages", 20);

        messages.add(new Message(role, text));

        while (messages.size() > maxMessages) {
            messages.remove(0);
        }
    }

    public List<Message> getMessages() {
        return messages;
    }

    public JsonArray toJson() {

        JsonArray contents = new JsonArray();

        for (Message message : messages) {

            JsonObject content = new JsonObject();
            JsonArray parts = new JsonArray();

            JsonObject text = new JsonObject();
            text.addProperty("text", message.text());

            parts.add(text);

            content.addProperty("role", message.role());
            content.add("parts", parts);

            contents.add(content);
        }

        return contents;
    }

    public void clear() {
        messages.clear();
    }

    public int size() {
        return messages.size();
    }

    public record Message(
            String role,
            String text
    ) {
    }
}