package com.ljmrhj;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import net.md_5.bungee.api.chat.*;
import java.util.*;

public class ecplugin extends JavaPlugin implements Listener {

    // MOTD 相关变量
    private Set<UUID> returningPlayers = new HashSet<>();
    private FileConfiguration config;

    @Override
    public void onEnable() {
        // 初始化配置
        saveDefaultConfig();
        config = getConfig();
        
        // 加载老玩家数据
        loadReturningPlayers();
        
        // 注册事件
        getServer().getPluginManager().registerEvents(this, this);
        
        // 注册命令
        Objects.requireNonNull(getCommand("link")).setExecutor(this);
        
        getLogger().info("[EteralCraft] 插件已启用!");
    }

    private void loadReturningPlayers() {
        if (config.contains("returning-players")) {
            for (String uuidString : config.getStringList("returning-players")) {
                returningPlayers.add(UUID.fromString(uuidString));
            }
        }
    }

    // 命令处理
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("aboutserver")) {
            handleAboutServer(sender);
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("eteralcrafthelp")) {
            handleHelp(sender);
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("link")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "只有玩家可以使用此命令!");
                return true;
            }
            handleLink((Player) sender);
            return true;
        }
        return false;
    }

    private void handleAboutServer(CommandSender sender) {
        sender.sendMessage("EteralCraft服务器是PixelWorld的下一代");
        sender.sendMessage("于2025.7.20开始创建，2025.7.30开放");
    }

    private void handleHelp(CommandSender sender) {
        sender.sendMessage("ecplugin §aEteralCraftPlugin");
        sender.sendMessage("ecplugin version:§a1.1.250807+1.0.1");
        sender.sendMessage("api version:§aspigotAPI 1.21.8-R0-1-SNAPSHOT");
        sender.sendMessage("§amotd§r+§aabout§r+§alink");
        sender.sendMessage("by ljmrhj");
        sender.sendMessage("bulid_time:Thu August 07 21:19:04 UTC 2025");
        sender.sendMessage("§e----§6eteralcraft help index:§e--§c1 §6/ §c1 §e---");
        sender.sendMessage("§4/aboutserver§r: 关于服务器");
        sender.sendMessage("§4/eteralcrafthelp§r: help");
        sender.sendMessage("§4/link§r: 商店");
    }

    private void handleLink(Player player) {
        if (!player.hasPermission("ecplugin.link.use")) {
            player.sendMessage(ChatColor.RED + "你没有权限使用此命令!");
            return;
        }

        TextComponent message = new TextComponent("§a-->>>>点击这里进入金币商店<<<<--§r");
        message.setColor(net.md_5.bungee.api.ChatColor.GOLD);
        message.setBold(true);
        message.setUnderlined(true);
        message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://minepay.top/#/shop?id=7714"));
        message.setHoverEvent(new HoverEvent(
            HoverEvent.Action.SHOW_TEXT,
            new ComponentBuilder("点击将打开浏览器").create()
        ));

        player.spigot().sendMessage(message);
        player.sendMessage(ChatColor.GRAY + "↑ 你可以点击上面的链接");
    }

    // 玩家加入事件
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        boolean isFirstJoin = !returningPlayers.contains(player.getUniqueId());
        
        if (isFirstJoin) {
            sendFirstJoinWelcome(player);
            returningPlayers.add(player.getUniqueId());
            saveReturningPlayers();
        } else {
            sendWelcomeBack(player);
        }
        
        playWelcomeSound(player);
    }

    private void sendFirstJoinWelcome(Player player) {
        String message = config.getString("messages.first-join", "&a欢迎 &6{player} &a首次加入服务器!")
                .replace("{player}", player.getName());
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        
        if (config.getBoolean("broadcast-first-join", true)) {
            String broadcastMsg = config.getString("messages.first-join-broadcast", "&e欢迎新玩家 &6{player} &e加入服务器!")
                    .replace("{player}", player.getName());
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', broadcastMsg));
        }
    }

    private void sendWelcomeBack(Player player) {
        String message = config.getString("messages.welcome-back", "&a欢迎回来, &6{player}&a!")
                .replace("{player}", player.getName());
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    private void saveReturningPlayers() {
        config.set("returning-players", returningPlayers.stream()
                .map(UUID::toString)
                .toList());
        saveConfig();
    }

    private void playWelcomeSound(Player player) {
        if (config.getBoolean("sound.enabled", true)) {
            try {
                Sound sound = Sound.valueOf(config.getString("sound.type", "BLOCK_NOTE_BLOCK_PLING"));
                float volume = (float) config.getDouble("sound.volume", 1.0);
                float pitch = (float) config.getDouble("sound.pitch", 1.0);
                player.playSound(player.getLocation(), sound, volume, pitch);
            } catch (IllegalArgumentException e) {
                getLogger().warning("配置的音效类型无效: " + config.getString("sound.type"));
            }
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("[EteralCraft] 插件已禁用!");
    }
}