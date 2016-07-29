package com.pb.spigot.quickshop.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import com.pb.spigot.quickshop.QuickShop;
import com.pb.spigot.quickshop.util.Util;

public class InventoryListener implements Listener {
    private QuickShop plugin;

    public InventoryListener(QuickShop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        for (int i = 0; i < event.getInventory().getContents().length; i++) {
            try {
                ItemStack is = event.getInventory().getContents()[i];
                if (itemStackCheck(is)) {
                    plugin.getLogger().warning("[Exploit alert] " + event.getPlayer().getName() + " had a QuickShop display item on inventory: " + event.getInventory().getType() + ":" + event.getInventory().getTitle());
                    Util.sendMessageToOps(ChatColor.RED + "[QuickShop][Exploit alert] " + event.getPlayer().getName() + " had a QuickShop display item on inventory: " + event.getInventory().getType() + ":" + event.getInventory().getTitle());
                    is.setAmount(0);
                    is.setType(Material.AIR);
                    event.getInventory().clear(i);
                }
            } catch (Exception e) {
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        try {
            if (itemStackCheck(event.getCurrentItem()) || itemStackCheck(event.getCursor())) {
                event.setCancelled(true);
                plugin.getLogger().warning("[Exploit alert] " + event.getWhoClicked().getName() + " had a QuickShop display item on inventory: " + event.getInventory().getType() + ":" + event.getInventory().getTitle());
                Util.sendMessageToOps(ChatColor.RED + "[QuickShop][Exploit alert] " + event.getWhoClicked().getName() + " had a QuickShop display item on inventory: " + event.getInventory().getType() + ":" + event.getInventory().getTitle());
                event.getCursor().setAmount(0);
                event.getCursor().setType(Material.AIR);
                event.getCurrentItem().setAmount(0);
                event.getCurrentItem().setType(Material.AIR);
                event.setResult(Result.DENY);

            }
        } catch (Exception e) {
        }
    }

    @EventHandler
    void onInventoryPickupItem(InventoryPickupItemEvent event) {
        try {
            ItemStack is = event.getItem().getItemStack();
            if (itemStackCheck(is)) {
                event.setCancelled(true);
                plugin.getLogger().warning("[Exploit alert] Inventory " + event.getInventory() + " picked up display item " + is);
                Util.sendMessageToOps(ChatColor.RED + "[QuickShop][Exploit alert]  Inventory " + event.getInventory() + " picked up display item " + is);
                event.getItem().remove();
            }
        } catch (Exception e) {
        }
    }

    boolean itemStackCheck(ItemStack is) {
        return is != null && is.getItemMeta() != null && is.getItemMeta().hasDisplayName() && is.getItemMeta().getDisplayName().startsWith(ChatColor.RED + "QuickShop ");
    }
}
