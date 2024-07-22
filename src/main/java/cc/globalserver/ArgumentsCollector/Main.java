package cc.globalserver.ArgumentsCollector;

import me.clip.placeholderapi.PlaceholderAPI;

import cc.globalserver.ArgumentsCollector.Metrics;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main extends JavaPlugin implements CommandExecutor, Listener {

    private final HashMap<UUID, List<String>> playerInputs = new HashMap<>();
    private final HashMap<UUID, String[]> messagesQueue = new HashMap<>();
    private final HashMap<UUID, String> commandQueue = new HashMap<>();
    private final HashMap<UUID, Boolean> useFormatting = new HashMap<>();
    private final HashMap<UUID, String> buttonResponse = new HashMap<>();
    private final HashMap<UUID, String> buttonTexts = new HashMap<>();
    private final HashMap<UUID, Boolean> waitingForButtonClick = new HashMap<>();

    @Override
    public void onEnable() {
        getLogger().info("Welcome to Arguments Collector by Learting!");
        this.getCommand("ac").setExecutor(this);
        this.getCommand("ac_click").setExecutor(this);

        // bstats
        int pluginId = 18870; // <-- Replace with the id of your plugin!
        Metrics metrics = new Metrics(this, pluginId);

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        getLogger().info("Goodbye!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player && command.getName().equalsIgnoreCase("ac") && args.length >= 2) {
            Player player = (Player) sender;
            UUID playerId = player.getUniqueId();
            boolean formatFlag = false;
            if ("-f".equalsIgnoreCase(args[0])) {
                formatFlag = true;
                args = removeFirstElement(args);
            }

            StringBuilder cmdBuilder = new StringBuilder();
            boolean inQuotes = false;
            List<String> argList = new ArrayList<>();
            for (int i = 0; i < args.length; i++) {
                String arg = args[i].replace("\\\"", "__ESCAPED_QUOTE__"); // Replace escaped quotes
                if (arg.startsWith("\"")) {
                    inQuotes = true;
                    arg = arg.substring(1);
                }
                if (inQuotes && arg.endsWith("\"") && !arg.endsWith("\\\"")) {
                    inQuotes = false;
                    arg = arg.substring(0, arg.length() - 1);
                }
                arg = arg.replace("__ESCAPED_QUOTE__", "\""); // Restore escaped quotes

                if (inQuotes || cmdBuilder.length() > 0) {
                    cmdBuilder.append(arg).append(i < args.length - 1 ? " " : "");
                } else {
                    argList.add(arg);
                }
                if (!inQuotes && cmdBuilder.length() > 0) {
                    argList.add(cmdBuilder.toString());
                    cmdBuilder.setLength(0);
                }
            }
            if (cmdBuilder.length() > 0) {
                argList.add(cmdBuilder.toString());
            }

            String cmd = argList.get(0);
            String[] messages = argList.subList(1, argList.size()).toArray(new String[0]);
            playerInputs.put(playerId, new ArrayList<>());
            messagesQueue.put(playerId, messages);
            commandQueue.put(playerId, cmd);
            useFormatting.put(playerId, formatFlag);
            handleNextMessage(player, 0);
            return true;
        }
        return false;
    }

    private String[] removeFirstElement(String[] args) {
        String[] newArray = new String[args.length - 1];
        System.arraycopy(args, 1, newArray, 0, args.length - 1);
        return newArray;
    }

    private void handleNextMessage(Player player, int index) {
        UUID playerId = player.getUniqueId();
        String[] messages = messagesQueue.get(playerId);

        if (index < messages.length) {
            String nextMessage = messages[index];
            if (nextMessage.startsWith("$[")) {
                // Wait for button click
                waitingForButtonClick.put(playerId, true);
                Pattern pattern = Pattern.compile("\\$\\[(.*?)\\](.*)");
                Matcher matcher = pattern.matcher(nextMessage);
                if (matcher.find()) {
                    String buttons = matcher.group(1);
                    String prompt = matcher.group(2);
                    prompt = applyPlaceholders(player, prompt, new ArrayList<>());
                    String[] buttonPairs = buttons.split(",\\s*");
                    TextComponent buttonsComponent = new TextComponent(ChatColor.LIGHT_PURPLE + "⏐  ");
                    for (String buttonPair : buttonPairs) {
                        String[] keyValue = buttonPair.split(":\\s*");
                        String buttonText = keyValue[0].replaceAll("'", "").trim();
                        String buttonValue = keyValue[1].replaceAll("'", "").trim();

                        TextComponent buttonComponent = new TextComponent(ChatColor.YELLOW + "[" + ChatColor.GREEN + ChatColor.translateAlternateColorCodes('&', buttonText) + ChatColor.YELLOW + "]  ");
                        buttonComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ac_click " + playerId + " " + buttonValue + " " + buttonText));

                        buttonsComponent.addExtra(buttonComponent);
                    }

                    player.sendMessage(ChatColor.LIGHT_PURPLE + "⌌" + ChatColor.GRAY + "(" + ChatColor.GOLD + (index + 1) + "/" + messages.length + ChatColor.GRAY + ") " + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', prompt));
                    player.spigot().sendMessage(buttonsComponent);
                }
            } else {
                String prompt = applyPlaceholders(player, nextMessage, new ArrayList<>());
                player.sendMessage(ChatColor.LIGHT_PURPLE + "⌌" + ChatColor.GRAY + "(" + ChatColor.GOLD + (index + 1) + "/" + messages.length + ChatColor.GRAY + ") " + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', prompt));
                // Not for button click
                waitingForButtonClick.put(playerId, false);
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (playerInputs.containsKey(playerId)) {
            // Return early if waiting for button click
            if (waitingForButtonClick.getOrDefault(playerId, false)) {
                String message = event.getMessage();
                if ("q".equalsIgnoreCase(message)) {
                    playerInputs.remove(playerId);
                    messagesQueue.remove(playerId);
                    commandQueue.remove(playerId);
                    useFormatting.remove(playerId);
                    waitingForButtonClick.remove(playerId);
                    player.sendMessage(ChatColor.DARK_RED + "已取消执行 Operation Cancelled");
                }
                event.setCancelled(true);
                return;
            }

            List<String> inputs = playerInputs.get(playerId);
            String[] messages = messagesQueue.get(playerId);
            String cmd = commandQueue.get(playerId);
            boolean formatFlag = useFormatting.get(playerId);

            String message = event.getMessage();

            if ("q".equalsIgnoreCase(message)) {
                playerInputs.remove(playerId);
                messagesQueue.remove(playerId);
                commandQueue.remove(playerId);
                useFormatting.remove(playerId);
                player.sendMessage(ChatColor.DARK_RED + "🗙");
                event.setCancelled(true);
                return;
            }

            if (buttonResponse.containsKey(playerId)) {
                message = buttonResponse.get(playerId);
                buttonResponse.remove(playerId);
            }

            inputs.add(message);

            String displayMessage = buttonTexts.getOrDefault(playerId, message);
            player.sendMessage(ChatColor.LIGHT_PURPLE + "⌎" + ChatColor.AQUA + ChatColor.translateAlternateColorCodes('&', displayMessage));
            buttonTexts.remove(playerId);

            if (inputs.size() < messages.length) {
                handleNextMessage(player, inputs.size());
                event.setCancelled(true);
                return;
            }

            String commandToExecute;
            if (formatFlag) {
                commandToExecute = applyPlaceholders(player, cmd, inputs);
            } else {
                StringBuilder commandBuilder = new StringBuilder(cmd);
                for (String input : inputs) {
                    commandBuilder.append(" ").append(input);
                }
                commandToExecute = commandBuilder.toString();
            }
            
            getLogger().info("Player: " + player.getName().toString() + " |||| UUID: " + player.getUniqueId().toString() + " |||| Command: " + commandToExecute);

            player.getScheduler().run(this, t -> player.performCommand(commandToExecute), null);

            playerInputs.remove(playerId);
            messagesQueue.remove(playerId);
            commandQueue.remove(playerId);
            useFormatting.remove(playerId);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onButtonClick(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        String command = event.getMessage();

        if (command.startsWith("/ac_click ")) {
            String[] args = command.split(" ");
            if (args.length == 4 && args[1].equals(playerId.toString())) {
                String response = args[2];
                String buttonText = args[3];
                buttonResponse.put(playerId, response);
                buttonTexts.put(playerId, buttonText);
                AsyncPlayerChatEvent chatEvent = new AsyncPlayerChatEvent(false, player, response, new HashSet<>(Bukkit.getOnlinePlayers()));
                // Reset
                waitingForButtonClick.put(playerId, false);
                event.setCancelled(true);
                // Call with delay, for fixing button twice-click issue
                player.getScheduler().execute(this, () -> {
                    AsyncPlayerChatEvent delayedChatEvent = new AsyncPlayerChatEvent(false, player, response, new HashSet<>(Bukkit.getOnlinePlayers()));
                    Bukkit.getPluginManager().callEvent(delayedChatEvent);
                }, null, 1L);
            }
        }
    }

    private String applyPlaceholders(Player player, String cmd, List<String> inputs) {
        String replacedCmd = cmd;
        int inputIndex = 0;
        while (replacedCmd.contains("%s") && inputIndex < inputs.size()) {
            replacedCmd = replacedCmd.replaceFirst("%s", inputs.get(inputIndex));
            inputIndex++;
        }

        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            replacedCmd = PlaceholderAPI.setPlaceholders(player, replacedCmd);
        }

        return replacedCmd;
    }
}

