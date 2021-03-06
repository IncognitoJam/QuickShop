package com.pb.spigot.quickshop.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import com.pb.spigot.quickshop.QuickShop;

/**
 * @author Netherfoam
 */
public class ChatListener implements Listener {
    QuickShop plugin;

    public ChatListener(QuickShop plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(final AsyncPlayerChatEvent e) {
        if (!plugin.getShopManager().getActions().containsKey(e.getPlayer().getUniqueId())) {
            return;
        }
        plugin.getShopManager().handleChat(e.getPlayer(), e.getMessage());
        e.setCancelled(true);
    }
}