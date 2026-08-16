[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/P5P1DB75)

# MinecraftAI

AI Assistant Plugin for Minecraft Java Server powered by Google Gemini.

> **Important:** Please read this `README.md` and the `config.yml` carefully before using or configuring MinecraftAI. Some features, especially AI command execution, depend on permissions and configuration settings.

# Description

MinecraftAI is an AI assistant plugin for Minecraft Java Server. It allows players to interact with an AI directly through Minecraft chat.

MinecraftAI supports multiple chat modes, conversation history, cooldowns, AI-generated Minecraft commands, command whitelisting, and configurable AI behavior.

If you have a question, find a bug, or want to suggest a feature, please make an [issue](https://github.com/Kylan1940/MinecraftAI/issues/new).

# Features

>- Google Gemini AI Integration
>- Chat-based AI Assistant
>- Public/Mention/Private Mode
>- Conversation History, Limit, and Reset
>- AI Command Execution
>- Permissions
>- Cooldown System
>- Player Information Context
>- Async AI Requests
>- Configuration Support
>- Compatible with 7 Server Software (Paper, Spigot, CraftBukkit, Folia, Purpur, Pufferfish, Leaves)

# Download

| Version | JAR                                                                       | Minecraft Support | Software Support |
|---------|---------------------------------------------------------------------------|-------------------|------------------|
| DEV-0.1 | [DOWNLOAD](https://github.com/Kylan1940/MinecraftAI/releases/tag/DEV-0.1) | Check release | Paper, Spigot, CraftBukkit, Folia, Purpur, Pufferfish, Leaves |

# Installation

1. Download the latest [MinecraftAI release](https://github.com/Kylan1940/MinecraftAI/releases/tag/DEV-0.1).
2. Put the `.jar` file into the server's `plugins` folder.
3. Start or restart the server.
4. Open `plugins/MinecraftAI/config.yml`.
5. Configure your Gemini API key.
6. Carefully review the AI command configuration and permissions.
7. Restart the server or reload the plugin if supported.

# Configuration

The configuration file is located at:

```text
plugins/MinecraftAI/config.yml
```

## AI Configuration

```yaml
ai:
  API_KEY: "YOUR_GEMINI_API_KEY"
  model: "gemini-2.5-flash"
```

### API Key

`API_KEY` is required for MinecraftAI to communicate with Google Gemini.

```yaml
API_KEY: "YOUR_GEMINI_API_KEY"
```

> **Security:** Never publish your API key in GitHub, screenshots, logs, or other public places.

### Model

Configure the Gemini model used by MinecraftAI:

```yaml
model: "gemini-2.5-flash"
```

Make sure the model is supported by the Gemini API and your API configuration. Never change it if you don't know what you do.

# Conversation

MinecraftAI stores conversation history for each player.

```yaml
conversation:
  max-messages: 20
```

`max-messages` controls the maximum number of messages stored in a player's conversation.

When the limit is reached, older messages are removed.

You can also clear your conversation using:

```text
/ai clear
```

# AI Modes

MinecraftAI supports three chat modes.

## Public

```text
/ai mode public
```

In `public` mode, normal player messages can be sent to the AI.

## Mention

```text
/ai mode mention
```

In `mention` mode, the AI only responds when its configured name is mentioned.

For example, if the AI name is `James`:

```text
James, what is the weather?
```

The AI name can be configured in `config.yml`.

## Private

```text
/ai mode private
```

Private mode allows selected players to interact with the AI privately according to MinecraftAI's private-player system.

# AI Commands

MinecraftAI can generate and execute Minecraft commands based on player requests.

For example:

```text
change day
```

The AI may generate:

```text
/time set day
```

MinecraftAI then validates the command before executing it.

**Important:** AI command execution is disabled or restricted unless the required configuration and permission are available.

# AI Command Configuration

AI command execution can be configured in `config.yml`:

```yaml
ai:
  commands:
    enabled: true
    allowed:
      - tp
      - give
      - kill
      - weather
      - time
```

## `enabled`

Controls whether MinecraftAI is allowed to execute AI-generated commands.

```yaml
enabled: true
```

Set it to:

```yaml
enabled: false
```

if you do not want MinecraftAI to execute commands.

## `allowed`

Defines which Minecraft commands AI is allowed to execute.

Example:

```yaml
allowed:
  - tp
  - give
  - kill
  - weather
  - time
```

Only commands included in this list can be executed.

For example, if:

```yaml
allowed:
  - time
  - weather
```

the AI cannot execute:

```text
/give
/tp
/kill
```

even if it generates those commands.

**Important:** Only add commands that you trust MinecraftAI to execute. Changing this list directly changes what the AI is allowed to control on your server.

# AI Command Permission

AI command execution requires:

```text
minecraftai.execute
```

Players without this permission cannot execute AI-generated commands.

The `/ai` command itself requires:

```text
minecraftai.command
```

Do not confuse these permissions:

| Permission | Description | Default |
|------------|-------------|---------|
| `minecraftai.command` | Use `/ai` commands | OP      |
| `minecraftai.execute` | Allow AI-generated Minecraft commands | OP      |

A player can therefore use MinecraftAI without necessarily being allowed to execute AI commands.

# Commands

| Command | Description | Permission | Default | Console Support |
|---------|-------------|------------|---------|-----------------|
| `/ai start` | Start MinecraftAI | `minecraftai.command` | OP | NO |
| `/ai end` | Stop MinecraftAI | `minecraftai.command` | OP | NO |
| `/ai mode <mode>` | Change AI mode | `minecraftai.command` | OP | NO |
| `/ai status` | Show AI status | `minecraftai.command` | OP | NO |
| `/ai clear` | Clear your AI conversation | `minecraftai.command` | OP | NO |

### Available Modes

| Mode | Description |
|------|-------------|
| `public` | AI can respond to normal chat |
| `mention` | AI responds when its name is mentioned |
| `private` | AI responds to configured private players |

# Messages

MinecraftAI messages can be configured through `config.yml`.

Example:

```yaml
message:
  thinking: "%prefix%&7Thinking..."
  no-api-key: "%prefix%&cAPI key has not been configured."
  cooldown: "%prefix%&cPlease wait &e%seconds% seconds&c."
  error: "%prefix%&cAI Failed to get a response from AI."
  response: "%prefix%&f%response%"
```

MinecraftAI supports color codes using:

```text
&
```

Example:

```yaml
prefix: "&bJames &8» "
```

# Configuration Placeholders

Common placeholders include:

| Placeholder | Description |
|-------------|-------------|
| `%prefix%` | Configured AI prefix |
| `%name%` | AI name |
| `%response%` | AI response |
| `%seconds%` | Remaining cooldown time |
| `%status%` | Current AI status |
| `%mode%` | Current AI mode |

Check the comments in `config.yml` for the currently supported placeholders before changing message formats.

# Player Context

MinecraftAI can provide the AI with information about the player and server environment, including:

>- Player name
>- World
>- Location
>- Gamemode
>- OP status
>- AI command permission
>- Online player count
>- Minecraft time
>- Weather

This allows the AI to provide more context-aware responses.

# Command Safety

MinecraftAI performs several checks before executing an AI-generated command.

Commands are checked against the configured whitelist.

MinecraftAI also blocks command chaining patterns such as:

```text
;
&&
||
newline
```

The player must also have:

```text
minecraftai.execute
```

before an AI-generated command can be executed.

**Warning:** AI command execution gives the AI the ability to interact with your Minecraft server through the commands you whitelist. Only enable commands that you understand and are comfortable allowing the AI to execute.

# Configuration Warning

> **IMPORTANT:** Always read both this `README.md` and the comments inside `config.yml` carefully before deploying MinecraftAI.

Changing configuration values such as:

```yaml
ai.commands.enabled
ai.commands.allowed
ai.conversation.max-messages
```

can directly affect how MinecraftAI behaves on your server.

In particular, adding commands to:

```yaml
ai.commands.allowed
```

can give the AI permission to perform those commands for players who have:

```text
minecraftai.execute
```

Do not add commands to the whitelist without understanding their effects.

# Issues

If you find a bug, have a question, or want to request a feature, please create an [issue](https://github.com/Kylan1940/MinecraftAI/issues/new).

# License

See the repository for license information.

# Credits

Developed by **Kylan1940**.

- GitHub: [Kylan1940](https://github.com/Kylan1940)
- MinecraftAI: [GitHub Repository](https://github.com/Kylan1940/MinecraftAI)
