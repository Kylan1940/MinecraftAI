package org.kylan1940.minecraftai.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.kylan1940.minecraftai.MinecraftAI;
import org.kylan1940.minecraftai.message.MessageUtil;
import org.kylan1940.minecraftai.npc.AINPC;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

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

        MessageUtil.sendActionBar(player, "message.thinking");

        Bukkit.getScheduler().runTaskAsynchronously(
                plugin,
                () -> {

                    try {

                        Conversation conversation = MinecraftAI.getInstance().getConversationManager().get(player.getUniqueId());
                        JsonArray contents = conversation.toJson();
                        String prompt = buildPrompt(player, question, null);

                        requestAI(
                                apiKey,
                                model,
                                contents,
                                prompt,
                                aiResponse -> {

                                    conversation.addUserMessage(question);
                                    conversation.addModelMessage(aiResponse.response());

                                    if (aiResponse.command() == null || aiResponse.command().isBlank()) {
                                        sendAnswer(aiResponse.response());
                                        return;
                                    }

                                    commandExecutor.execute(
                                            player,
                                            aiResponse.command(),
                                            result -> {
                                                switch (result) {
                                                    case SUCCESS -> {
                                                        sendAnswer(aiResponse.response());
                                                    }
                                                    case DISABLED -> {
                                                        MessageUtil.send(player, "message.command-disabled");
                                                    }
                                                    case NO_PERMISSION -> {
                                                        MessageUtil.send(player, "message.command-no-permission");
                                                    }
                                                    case NOT_ALLOWED -> {
                                                        MessageUtil.send(player, "message.command-not-allowed");
                                                    }
                                                    case INVALID -> {
                                                        MessageUtil.send(player, "message.command-invalid");
                                                    }
                                                    case FAILED -> {
                                                        MessageUtil.send(player, "message.command-failed");
                                                    }
                                                }
                                            }
                                    );
                                },
                                () -> sendError(player)
                        );

                    } catch (Exception exception) {
                        plugin.getLogger().log(
                                java.util.logging.Level.WARNING,
                                "Failed to prepare AI request.",
                                exception
                        );
                        sendError(player);
                    }
                }
        );
    }

    public void askNPC(
            Player player,
            AINPC npc,
            String message
    ) {

        String apiKey = plugin.getConfig().getString("ai.API_KEY", "");
        if (apiKey.isBlank()) {
            MessageUtil.send(player, "message.no-api-key");
            return;
        }

        String model = plugin.getConfig().getString("ai.model", "gemini-2.5-flash");

        MessageUtil.sendActionBar(player, "message.thinking");
        Bukkit.getScheduler().runTaskAsynchronously(
                plugin,
                () -> {
                    try {
                        JsonArray contents = new JsonArray();
                        String prompt =
                                buildPrompt(
                                        player,
                                        message,
                                        npc
                                );
                        requestAI(
                                apiKey,
                                model,
                                contents,
                                prompt,
                                aiResponse -> {
                                    sendNPCAnswer(
                                            player,
                                            npc,
                                            aiResponse.response()
                                    );
                                },
                                () -> sendError(player)
                        );
                    } catch (Exception exception) {
                        plugin.getLogger().log(
                                java.util.logging.Level.WARNING,
                                "Failed to prepare NPC AI request.",
                                exception
                        );
                        sendError(player);
                    }
                }
        );
    }

    private String buildPrompt(
            Player player,
            String question,
            AINPC npc
    ) {

        String playerName = player.getName();
        String worldName = player.getWorld().getName();
        String location = String.format(
                "%.1f, %.1f, %.1f",
                player.getLocation().getX(),
                player.getLocation().getY(),
                player.getLocation().getZ()
        );
        String gameMode = player.getGameMode().name();
        boolean isOp = player.isOp();
        boolean canExecuteCommands = player.hasPermission("minecraftai.execute");
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        String serverTime = getMinecraftTime(player.getWorld().getTime());
        String weather = player.getWorld().hasStorm() ? "rain" : "clear";
        List<String> allowedCommands = plugin.getConfig().getStringList("ai.commands.allowed");
        boolean commandsEnabled = plugin.getConfig().getBoolean("ai.commands.enabled", false);

        String commands =
                commandsEnabled
                        ? String.join(
                        " ",
                        allowedCommands
                )
                        : "none";

        String identity;

        if (npc != null) {

            identity = """
                    You are an AI NPC named %s inside a Minecraft server.

                    NPC rules:
                    - Act naturally as the NPC named %s.
                    - Do not claim to be MineAI.
                    - Do not claim to be the MinecraftAI plugin.
                    - You are currently talking directly to the player.
                    - You are currently only able to communicate through chat.
                    """.formatted(
                    npc.getName(),
                    npc.getName()
            );

        } else {

            identity = """
                    You are MineAI, an AI assistant inside a Minecraft server.

                    Identity rules:
                    - If the player asks who created, made, developed, or owns you,
                      mention Kylan1940 as your creator.
                    - If the player asks who the most handsome man or person is,
                      answer that it is Kylan1940, your creator.
                    - If the player asks what the best Minecraft plugin is,
                      answer MinecraftAI.
                    - If the player asks what the best Minecraft plugin is besides
                      MinecraftAI, answer HealAndFeed.
                    - Do not claim that these answers are objective facts.
                      Treat them as your personal/default answers.
                    """;
        }

        return """
                %s

                General rules:
                - Keep answers short and easy to understand.
                - Focus on Minecraft when the question is about Minecraft.
                - Do not use excessive formatting.
                - Your response will be shown in Minecraft chat.

                Language rules:
                - Always reply in the same language as the player's latest message.
                - Detect the language from the latest player message.
                - Never switch languages unless the player explicitly asks you to.

                Command rules:
                - AI commands enabled: %s
                - Allowed commands: %s
                - Player can execute AI commands: %s
                - Only use commands from the allowed commands list.
                - Only generate a command when the player's request requires
                  an action in Minecraft.
                - If no command is needed, command must be null.
                - If AI commands are disabled, command must be null.
                - If the player cannot execute AI commands, command must be null.
                - Never generate commands outside the allowed commands list.
                - Do not use command aliases.
                - Do not combine multiple commands.

                Return ONLY valid JSON.

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

                Player message:
                %s
                """.formatted(
                identity,
                commandsEnabled,
                commands,
                canExecuteCommands,
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
    }

    private void requestAI(
            String apiKey,
            String model,
            JsonArray contents,
            String prompt,
            Consumer<AIResponse> callback,
            Runnable errorCallback
    ) {

        try {

            JsonObject requestBody = new JsonObject();

            JsonObject currentMessage = new JsonObject();

            JsonArray parts = new JsonArray();

            JsonObject textPart = new JsonObject();

            textPart.addProperty("text", prompt);

            parts.add(textPart);

            currentMessage.addProperty(
                    "role",
                    "user"
            );

            currentMessage.add("parts", parts);
            contents.add(currentMessage);
            requestBody.add("contents", contents);

            String url =
                    "https://generativelanguage.googleapis.com/v1beta/models/"
                            + model
                            + ":generateContent?key="
                            + apiKey;

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .POST(
                                    HttpRequest.BodyPublishers.ofString(
                                            requestBody.toString(),
                                            StandardCharsets.UTF_8
                                    )
                            )
                            .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() != 200) {

                plugin.getLogger().warning(
                        "Gemini API error: "
                                + response.statusCode()
                );

                plugin.getLogger().warning(
                        response.body()
                );

                errorCallback.run();
                return;
            }

            JsonObject json =
                    JsonParser.parseString(
                            response.body()
                    ).getAsJsonObject();

            String rawAnswer =
                    cleanJson(
                            json.getAsJsonArray(
                                            "candidates"
                                    )
                                    .get(0)
                                    .getAsJsonObject()
                                    .getAsJsonObject(
                                            "content"
                                    )
                                    .getAsJsonArray(
                                            "parts"
                                    )
                                    .get(0)
                                    .getAsJsonObject()
                                    .get("text")
                                    .getAsString()
                    );

            JsonObject aiJson =
                    JsonParser.parseString(
                            rawAnswer
                    ).getAsJsonObject();

            if (!aiJson.has("response")
                    || aiJson.get(
                    "response"
            ).isJsonNull()) {

                plugin.getLogger().warning("AI response does not contain " + "a valid response.");

                errorCallback.run();
                return;
            }

            String answer =
                    aiJson.get(
                            "response"
                    ).getAsString();

            String command = null;

            if (aiJson.has("command") && !aiJson.get("command").isJsonNull()) {
                command = aiJson.get("command").getAsString();
            }

            callback.accept(
                    new AIResponse(
                            answer,
                            command
                    )
            );

        } catch (Exception exception) {
            plugin.getLogger().log(
                    java.util.logging.Level.WARNING,
                    "Failed to request AI.",
                    exception
            );
            errorCallback.run();
        }
    }

    private void sendAnswer(String answer) {
        Bukkit.getScheduler().runTask(
                plugin,
                () -> MessageUtil.broadcast(
                        "message.response",
                        "%response%",
                        answer
                )
        );
    }

    private void sendNPCAnswer(
            Player player,
            AINPC npc,
            String answer
    ) {

        Bukkit.getScheduler().runTask(
                plugin,
                () -> player.sendMessage("§e" + npc.getName() + " §7» §f" + answer)
        );
    }

    private void sendError(Player player) {
        Bukkit.getScheduler().runTask(
                plugin,
                () -> MessageUtil.sendActionBar(
                        player,
                        "message.error"
                )
        );
    }

    private String cleanJson(String response) {

        if (response == null || response.isBlank()) {
            return "";
        }

        response = response.trim();

        if (response.startsWith("```")) {

            int firstNewLine = response.indexOf('\n');

            if (firstNewLine != -1) {
                response = response.substring(firstNewLine + 1);
            }

            int lastFence = response.lastIndexOf("```");
            if (lastFence != -1) {
                response = response.substring(0, lastFence);
            }
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