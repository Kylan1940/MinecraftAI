package org.kylan1940.minecraftai.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.kylan1940.minecraftai.MinecraftAI;
import org.kylan1940.minecraftai.message.MessageUtil;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class AIService {

    private final JavaPlugin plugin;
    private final HttpClient httpClient;

    public AIService(JavaPlugin plugin) {
        this.plugin = plugin;
        this.httpClient = HttpClient.newHttpClient();
    }

    public void ask(Player player, String question) {

        String apiKey = plugin.getConfig().getString("ai.API_KEY", "");

        if (apiKey.isBlank()) {
            MessageUtil.send(player, "message.no-api-key");
            return;
        }

        String model = plugin.getConfig().getString("ai.model", "gemini-2.5-flash");

        MessageUtil.sendActionBar(player, "message.thinking");

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            try {

                Conversation conversation = MinecraftAI.getInstance().getConversationManager().get(player.getUniqueId());

                JsonObject requestBody = new JsonObject();

                JsonArray contents = conversation.toJson();

                JsonObject currentMessage = new JsonObject();

                JsonArray parts = new JsonArray();

                JsonObject textPart = new JsonObject();

                String prompt = """
                        You are MineAI, an AI assistant inside a Minecraft server.

                        Rules:
                        - Answer using the same language as the player.
                        - Keep answers short and easy to understand.
                        - Focus on Minecraft when the question is about Minecraft.
                        - Do not use excessive formatting.
                        - Your answer will be shown in Minecraft public chat.

                        Player name: %s
                        Question: %s
                        """.formatted(
                        player.getName(),
                        question
                );

                textPart.addProperty("text", prompt);

                parts.add(textPart);

                currentMessage.addProperty("role", "user");
                currentMessage.add("parts", parts);
                contents.add(currentMessage);

                requestBody.add("contents", contents);

                String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header(
                                "Content-Type",
                                "application/json"
                        )
                        .POST(
                                HttpRequest.BodyPublishers
                                        .ofString(
                                                requestBody.toString(),
                                                StandardCharsets.UTF_8
                                        )
                        )
                        .build();

                HttpResponse<String> response =
                        httpClient.send(
                                request,
                                HttpResponse.BodyHandlers
                                        .ofString()
                        );

                if (response.statusCode() != 200) {

                    plugin.getLogger().warning("Gemini API error: " + response.statusCode());

                    plugin.getLogger().warning(response.body());

                    sendError(player);
                    return;
                }

                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();

                String answer =
                        json.getAsJsonArray("candidates")
                                .get(0)
                                .getAsJsonObject()
                                .getAsJsonObject("content")
                                .getAsJsonArray("parts")
                                .get(0)
                                .getAsJsonObject()
                                .get("text")
                                .getAsString();

                conversation.addUserMessage(
                        question
                );

                conversation.addModelMessage(
                        answer
                );

                sendAnswer(answer);

            } catch (Exception exception) {

                plugin.getLogger().warning("Failed to request AI: " + exception.getMessage());

                sendError(player);
            }
        });
    }

    private void sendAnswer(String answer) {

        Bukkit.getScheduler().runTask(
                plugin,
                () -> {
                    MessageUtil.broadcast("message.response", "%response%", answer);
                }
        );
    }

    private void sendError(Player player) {

        Bukkit.getScheduler().runTask(
                plugin,
                () -> {
                    MessageUtil.sendActionBar(player, "message.error");
                }
        );
    }
}