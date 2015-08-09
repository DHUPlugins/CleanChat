package co.jackdalton.cleanchat;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;

public final class ChatListener implements Listener {
    public cleanchat cc;
    public ChatListener(cleanchat plugin) {
        cc = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    @EventHandler
    public void onChat(PlayerChatEvent e) {
        cc.messageSent(e);
    }
}