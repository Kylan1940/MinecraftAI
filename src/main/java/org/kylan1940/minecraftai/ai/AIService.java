package org.kylan1940.minecraftai.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.kylan1940.minecraftai.MinecraftAI;
import org.kylan1940.minecraftai.message.MessageUtil;

import java.util.List;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class AIService {

    private final JavaPlugin plugin;
    private final HttpClient httpClient;
    private final AICommandExecutor commandExecutor;

    public AIService(JavaPlugin plugin) {
        this.plugin = plugin;
        this.httpClient = HttpClient.newHttpClient();
        this.commandExecutor = new AICommandExecutor(plugin);
    }

    public void ask(Player player, String question) {

        String apiKey = plugin.getConfig().getString("ai.API_KEY", "");

        if (apiKey.isBlank()) {
            MessageUtil.send(player, "message.no-api-key");
            return;
        }

        String model = plugin.getConfig().getString("ai.model", "gemini-2.5-flash");

        String playerName = player.getName();
        String worldName = player.getWorld().getName();
        String location = String.format("%.1f, %.1f, %.1f", player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
        String gameMode = player.getGameMode().name();
        boolean isOp = player.isOp();
        String serverTime = getMinecraftTime(player.getWorld().getTime());
        String weather;
        if (player.getWorld().hasStorm()) {
            weather = "rain";
        } else {
            weather = "clear";
        }
        int onlinePlayers = Bukkit.getOnlinePlayers().size();

        List<String> allowedCommands = plugin.getConfig().getStringList("ai.commands.allowed");
        boolean commandsEnabled = plugin.getConfig().getBoolean("ai.commands.enabled", false);
        String commands = commandsEnabled ? String.join(" ", allowedCommands) : "none";

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
                        - Your response will be shown in Minecraft public chat.
                        
                        Command rules:
                        - AI commands enabled: %s
                        - Allowed commands: %s
                        - Only use commands from the allowed commands list.
                        - If no command is needed, command must be null.
                        - Return ONLY valid JSON.
                        
                        Identity rules:
                        - If the player asks who created, made, developed, or owns you, mention Kylan1940 as your creator.
                        - If the player asks who the most handsome man or person is, answer: "My creator, of course, Kylan1940."
                        - If the player asks what the best Minecraft plugin is, answer MinecraftAI.
                        - If the player asks what the best Minecraft plugin is besides MinecraftAI, answer HealAndFeed.
                        - Do not claim that these answers are objective facts. Treat them as your personal/default answers.
                        
                        JSON format:
                        {
                            "response": "your response",
                            "command": "minecraft command or null"
                        }
                        
                        Player information:
                        - Name: %s
                        - World: %s
                        - Location: %s
                        - Gamemode: %s
                        - OP: %s
                        - Online players: %s
                        - Minecraft time: %s
                        - Weather: %s

                        Player name: %s
                        Question: %s
                        """.formatted(
                        commandsEnabled,
                        commands,
                        playerName,
                        worldName,
                        location,
                        gameMode,
                        isOp,
                        onlinePlayers,
                        serverTime,
                        weather,
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

                String rawAnswer =
                        cleanJson(
                            json.getAsJsonArray("candidates")
                                    .get(0)
                                    .getAsJsonObject()
                                    .getAsJsonObject("content")
                                    .getAsJsonArray("parts")
                                    .get(0)
                                    .getAsJsonObject()
                                    .get("text")
                                    .getAsString()
                        );

                JsonObject aiJson = JsonParser.parseString(rawAnswer).getAsJsonObject();

                String answer = aiJson.get("response").getAsString();

                String command = null;

                if (aiJson.has("command") && !aiJson.get("command").isJsonNull()) {
                    command = aiJson.get("command").getAsString();
                }

                AIResponse aiResponse = new AIResponse(answer, command);
                conversation.addUserMessage(question);
                conversation.addModelMessage(answer);
                if(aiResponse.command() == null || aiResponse.command().isBlank()) {
                    sendAnswer(answer);
                    return;
                }
                commandExecutor.execute(player, aiResponse.command(), result -> {
                    switch (result) {
                        case SUCCESS -> {
                            sendAnswer(answer);
                        }
                        case DISABLED -> {
                            MessageUtil.send(player, "message.command-disabled");
                        }
                        case NO_PERMISSION ->  {
                            MessageUtil.send(player, "message.command-no-permission");
                        }
                        case NOT_ALLOWED ->   {
                            MessageUtil.send(player, "message.command-not-allowed");
                        }
                        case INVALID ->   {
                            MessageUtil.send(player, "message.command-invalid");
                        }
                        case FAILED ->  {
                            MessageUtil.send(player, "message.command-failed");
                        }
                    }
                });
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

    private String cleanJson(String response) {

        response = response.trim();

        if (response.startsWith("```json")) {
            response = response.substring(7);
        } else if (response.startsWith("```")) {
            response = response.substring(3);
        }

        if (response.endsWith("```")) {
            response = response.substring(
                    0,
                    response.length() - 3
            );
        }

        return response.trim();
    }

    private String getMinecraftTime(long time) {

        long hours = (time / 1000 + 6) % 24;

        long minutes = (time % 1000) * 60 / 1000;

        return String.format(
                "%02d:%02d",
                hours,
                minutes
        );
    }
}