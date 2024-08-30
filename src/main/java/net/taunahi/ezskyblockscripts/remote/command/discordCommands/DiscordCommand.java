package net.taunahi.ezskyblockscripts.remote.command.discordCommands;

import com.google.gson.JsonObject;
import net.taunahi.ezskyblockscripts.Taunahi;
import net.taunahi.ezskyblockscripts.remote.WebsocketHandler;
import net.taunahi.ezskyblockscripts.remote.command.commands.Command;
import net.taunahi.ezskyblockscripts.remote.struct.RemoteMessage;
import net.taunahi.ezskyblockscripts.remote.util.RemoteUtils;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.java_websocket.WebSocket;

public class DiscordCommand {
    public String name;
    public String description;
    public Option[] options = null;

    public DiscordCommand(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void addOptions(Option... options) {
        this.options = options;
    }

    public void execute(SlashCommandInteractionEvent event) {
        throw new UnsupportedOperationException("Unimplemented method 'execute'");
    }

    public SlashCommandData getSlashCommand() {
        if (name.isEmpty() || description.isEmpty()) {
            throw new UnsupportedOperationException("Unimplemented method 'getSlashCommand'");
        }
        SlashCommandData data = Commands.slash(name, description);
        if (options != null) {
            for (Option option : options) {
                data.addOption(option.type, option.name, option.description, option.required, option.autocomplete);
            }
        }
        return data;
    }

    protected void sendMessage(WebSocket webSocket) {
        sendMessage(webSocket, new JsonObject());
    }

    protected void sendMessage(WebSocket webSocket, JsonObject args) {
        RemoteMessage message = new RemoteMessage(name, args);
        if (webSocket == null) {
            RemoteUtils.getCommand(
                            WebsocketHandler.commands,
                            (cmd) -> cmd.getClass().getAnnotation(Command.class).label().equalsIgnoreCase(name))
                    .ifPresent(cmd -> cmd.execute(message));
            return;
        }
        webSocket.send(Taunahi.gson.toJson(message));
    }
}
