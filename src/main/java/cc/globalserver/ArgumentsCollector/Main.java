package cc.globalserver.ArgumentsCollector;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Main extends JavaPlugin implements CommandExecutor, Listener {

    private final HashMap<UUID, List<String>> playerInputs = new HashMap<>();
    private final HashMap<UUID, String[]> messagesQueue = new HashMap<>();
    private final HashMap<UUID, String> commandQueue = new HashMap<>();
    private final HashMap<UUID, Boolean> useFormatting = new HashMap<>();

    @Override
    public void onEnable() {
        getLogger().info("Welcome to Arguments Collector by Learting!");
        this.getCommand("ac").setExecutor(this);
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
            for (String arg : args) {
                if (arg.startsWith("\"")) {
                    inQuotes = true;
                    arg = arg.substring(1);
                }
                if (arg.endsWith("\"")) {
                    inQuotes = false;
                    arg = arg.substring(0, arg.length() - 1);
                }

                if (inQuotes) {
                    cmdBuilder.append(arg).append(" ");
                } else {
                    if (cmdBuilder.length() > 0) {
                        cmdBuilder.append(arg);
                        argList.add(cmdBuilder.toString());
                        cmdBuilder.setLength(0);
                    } else {
                        argList.add(arg);
                    }
                }
            }

            String cmd = argList.get(0);
            String[] messages = new String[argList.size() - 1];
            argList.subList(1, argList.size()).toArray(messages);

            playerInputs.put(playerId, new ArrayList<>());
            messagesQueue.put(playerId, messages);
            commandQueue.put(playerId, cmd);
            useFormatting.put(playerId, formatFlag);

            player.sendMessage(ChatColor.LIGHT_PURPLE + "⌌" + ChatColor.GRAY + "(" + ChatColor.GOLD + (1) + "/" + messages.length + ChatColor.GRAY + ") " + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', messages[0]));
            return true;
        }
        return false;
    }

    private String[] removeFirstElement(String[] args) {
        String[] newArray = new String[args.length - 1];
        System.arraycopy(args, 1, newArray, 0, args.length - 1);
        return newArray;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (playerInputs.containsKey(playerId)) {
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
                player.sendMessage(ChatColor.DARK_RED + "已取消执行 Operation Cancelled");
                event.setCancelled(true);
                return;
            }

            inputs.add(message);
            player.sendMessage(ChatColor.LIGHT_PURPLE + "⌎" + ChatColor.AQUA + message);

            if (inputs.size() < messages.length) {
                player.sendMessage(ChatColor.LIGHT_PURPLE + "⌌" + ChatColor.GRAY + "(" + ChatColor.GOLD + (inputs.size() + 1) + "/" + messages.length + ChatColor.GRAY + ") " + ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', messages[inputs.size()]));
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

            Bukkit.getScheduler().runTask(this, () -> player.performCommand(commandToExecute));

            playerInputs.remove(playerId);
            messagesQueue.remove(playerId);
            commandQueue.remove(playerId);
            useFormatting.remove(playerId);
            event.setCancelled(true);
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
