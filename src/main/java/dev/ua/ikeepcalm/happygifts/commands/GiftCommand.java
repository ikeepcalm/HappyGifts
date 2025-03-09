package dev.ua.ikeepcalm.happygifts.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.ua.ikeepcalm.happygifts.HappyGifts;
import dev.ua.ikeepcalm.happygifts.gifts.Gift;
import dev.ua.ikeepcalm.happygifts.gifts.guis.GiftGui;
import dev.ua.ikeepcalm.happygifts.gifts.guis.GiftHistoryGui;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Handles all gift-related commands
 */
public class GiftCommand {
    private final HappyGifts plugin;

    public GiftCommand(HappyGifts plugin) {
        this.plugin = plugin;
        register();
    }

    /**
     * Register all gift commands
     */
    public void register() {
        new CommandAPICommand("gift")
                .withPermission("happygifts.gift")
                .withSubcommand(createCreateCommand())
                .withSubcommand(createSendCommand())
                .withSubcommand(createViewCommand())
                .executesPlayer((player, args) -> {
                    openMainGUI(player);
                })
                .register();

        new CommandAPICommand("happygifts")
                .withPermission("happygifts.admin")
                .withSubcommand(createReloadCommand())
                .withSubcommand(createHelpCommand())
                .register();
    }

    /**
     * Create reload command
     */
    private CommandAPICommand createReloadCommand() {
        return new CommandAPICommand("reload")
                .withPermission("happygifts.admin")
                .executesPlayer((player, args) -> {
                    plugin.reloadConfig();
                    plugin.getLanguageManager().reload();
                    player.sendMessage(plugin.getLanguageManager().getText("command.gift.reload"));
                });
    }

    /**
     * Create a new gift command
     */
    private CommandAPICommand createCreateCommand() {
        return new CommandAPICommand("create")
                .withPermission("happygifts.gift.create")
                .executesPlayer((player, args) -> {
                    new GiftGui(plugin).openGiftCreationMenu(player);
                    player.sendMessage(plugin.getLanguageManager().getText("command.gift.create"));
                });
    }

    /**
     * Create a send gift command
     */
    private CommandAPICommand createSendCommand() {
        return new CommandAPICommand("send")
                .withPermission("happygifts.gift.send")
                .withArguments(new PlayerArgument("recipient"))
                .executesPlayer((player, args) -> {
                    Player recipient = (Player) args.get(0);

                    if (plugin.getGiftManager().getActiveGift(player.getUniqueId()) == null) {
                        plugin.getGiftManager().setActiveGift(
                                player.getUniqueId(),
                                new Gift(player.getUniqueId())
                        );
                    }

                    if (recipient != null) {
                        plugin.getGiftManager().getActiveGift(player.getUniqueId()).setRecipient(recipient.getUniqueId());
                    }

                    new GiftGui(plugin).openGiftCreationMenu(player);

                    if (recipient != null) {
                        player.sendMessage(plugin.getLanguageManager().getTextWithPlaceholders(
                                "command.gift.send", recipient.getName()));
                    }
                });
    }

    /**
     * Create a view gifts command
     */
    private CommandAPICommand createViewCommand() {
        return new CommandAPICommand("view")
                .withPermission("happygifts.gift.view")
                .executesPlayer((player, args) -> {
                    new GiftHistoryGui(plugin).openHistoryMenu(player);
                });
    }

    /**
     * Create a help command
     */
    private CommandAPICommand createHelpCommand() {
        return new CommandAPICommand("help")
                .withPermission("happygifts.gift.help")
                .executesPlayer((player, args) -> {
                    sendHelpMessage(player);
                });
    }

    /**
     * Open the main gift GUI
     */
    private void openMainGUI(Player player) {
        new GiftGui(plugin).openMainMenu(player);
    }

    /**
     * Send help message to a player
     */
    private void sendHelpMessage(Player player) {
        Component header = plugin.getLanguageManager().getText("command.gift.help.header");
        List<Component> helpLines = plugin.getLanguageManager().getTextList("command.gift.help.commands");
        Component footer = plugin.getLanguageManager().getText("command.gift.help.footer");

        player.sendMessage(header);
        for (Component line : helpLines) {
            player.sendMessage(line);
        }

        player.sendMessage(footer);
    }
}