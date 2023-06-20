package cc.globalserver.ArgumentsCollector;

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

    @Override
    public void onEnable() {
        this.getCommand("ac").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player && command.getName().equalsIgnoreCase("ac") && args.length >= 2) {
            Player player = (Player) sender;
            UUID playerId = player.getUniqueId();

            String cmd = args[0];
            String[] messages = new String[args.length - 1];
            System.arraycopy(args, 1, messages, 0, args.length - 1);

            playerInputs.put(playerId, new ArrayList<>());
            messagesQueue.put(playerId, messages);
            commandQueue.put(playerId, cmd);

            player.sendMessage(ChatColor.LIGHT_PURPLE + "⌌" + ChatColor.GRAY + "(" + ChatColor.GOLD + (1) + "/" + messages.length + ChatColor.GRAY + ") " + ChatColor.WHITE + messages[0]);
            return true;
        }
        return false;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (playerInputs.containsKey(playerId)) {
            List<String> inputs = playerInputs.get(playerId);
            String[] messages = messagesQueue.get(playerId);
            String cmd = commandQueue.get(playerId);

            String message = event.getMessage();

            if ("q".equalsIgnoreCase(message)) {
                playerInputs.remove(playerId);
                messagesQueue.remove(playerId);
                commandQueue.remove(playerId);
                player.sendMessage(ChatColor.DARK_RED + "已取消执行");
                event.setCancelled(true);
                return;
            }

            inputs.add(message);
            player.sendMessage(ChatColor.LIGHT_PURPLE + "⌎" + ChatColor.AQUA + message);

            if (inputs.size() < messages.length) {
                player.sendMessage(ChatColor.LIGHT_PURPLE + "⌌" + ChatColor.GRAY + "(" + ChatColor.GOLD + (inputs.size() + 1) + "/" + messages.length + ChatColor.GRAY + ") " + ChatColor.WHITE + messages[inputs.size()]);
                event.setCancelled(true);
                return;
            }

            StringBuilder commandToExecute = new StringBuilder(cmd);
            for (String input : inputs) {
                commandToExecute.append(" ").append(input);
            }

            Bukkit.getScheduler().runTask(this, () -> player.performCommand(commandToExecute.toString()));

            playerInputs.remove(playerId);
            messagesQueue.remove(playerId);
            commandQueue.remove(playerId);
            event.setCancelled(true);
        }
    }
}

