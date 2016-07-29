package com.pb.spigot.quickshop.util;

import com.pb.spigot.quickshop.QuickShop;
import com.pb.spigot.quickshop.shop.Shop;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.io.File;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

@SuppressWarnings("deprecation")
public class MsgUtil {
    private static QuickShop plugin;
    private static YamlConfiguration messages;
    private static HashMap<UUID, LinkedList<String>> player_messages = new HashMap<UUID, LinkedList<String>>();

    static {
        plugin = QuickShop.instance;
    }

    /**
     * Loads all the messages from messages.yml
     */
    public static void loadCfgMessages() {
        // Load messages.yml
        File messageFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messageFile.exists()) {
            plugin.getLogger().info("Creating messages.yml");
            plugin.saveResource("messages.yml", true);
        }
        // Store it
        messages = YamlConfiguration.loadConfiguration(messageFile);
        messages.options().copyDefaults(true);
        // Load default messages
        InputStream defMessageStream = plugin.getResource("messages.yml");
        YamlConfiguration defMessages = YamlConfiguration.loadConfiguration(defMessageStream);
        messages.setDefaults(defMessages);
        // Parse colour codes
        Util.parseColours(messages);
    }

    /**
     * loads all player purchase messages from the database.
     */
    public static void loadTransactionMessages() {    //TODO Converted to UUID
        player_messages.clear(); // Delete old messages
        try {
            ResultSet rs = plugin.getDB().getConnection().prepareStatement("SELECT * FROM messages").executeQuery();
            while (rs.next()) {
                UUID owner = UUID.fromString(rs.getString("owner"));
                String message = rs.getString("message");
                LinkedList<String> messages = player_messages.get(owner);
                if (messages == null) {
                    messages = new LinkedList<>();
                    player_messages.put(owner, messages);
                }
                messages.add(message);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Could not load transaction messages from database. Skipping.");
        }
    }

    /**
     * @param playerUuid  The name of the player to message
     * @param message The message to send them Sends the given player a message if
     *                they're online. Else, if they're not online, queues it for
     *                them in the database.
     */
    public static void send(UUID playerUuid, String message) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerUuid);
        if (player == null || !player.isOnline()) {
            LinkedList<String> messages = player_messages.get(playerUuid);
            if (messages == null) {
                messages = new LinkedList<>();
                player_messages.put(playerUuid, messages);
            }
            messages.add(message);
            String q = "INSERT INTO messages (owner, message, time) VALUES (?, ?, ?)";
            plugin.getDB().execute(q, playerUuid.toString(), message, System.currentTimeMillis());
        } else {
            player.getPlayer().sendMessage(message);
        }
    }

    /**
     * Deletes any messages that are older than a week in the database, to save
     * on space.
     */
    public static void clean() {
        System.out.println("Cleaning purchase messages from database that are over a week old...");
        // 604800,000 msec = 1 week.
        long weekAgo = System.currentTimeMillis() - 604800000;
        plugin.getDB().execute("DELETE FROM messages WHERE time < ?", weekAgo);
    }

    /**
     * Empties the queue of messages a player has and sends them to the player.
     *
     * @param p The player to message
     * @return true if success, false if the player is offline or null
     */
    public static boolean flush(OfflinePlayer p) {    //TODO Changed to UUID
        if (p != null && p.isOnline()) {
            UUID pName = p.getUniqueId();
            LinkedList<String> msgs = player_messages.get(pName);
            if (msgs != null) {
                for (String msg : msgs) {
                    p.getPlayer().sendMessage(msg);
                }
                plugin.getDB().execute("DELETE FROM messages WHERE owner = ?", pName.toString());
                msgs.clear();
            }
            return true;
        }
        return false;
    }

    public static void sendShopInfo(Player p, Shop shop) {
        sendShopInfo(p, shop, shop.getRemainingStock());
    }

    public static void sendShopInfo(Player p, Shop shop, int stock) {
        // Potentially faster with an array?
        ItemStack items = shop.getItem();
        p.sendMessage("");
        p.sendMessage("");
        p.sendMessage(ChatColor.DARK_PURPLE + "+---------------------------------------------------+");
        p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.shop-information"));
        p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.owner", Bukkit.getOfflinePlayer(shop.getOwner()).getName() == null ? (shop.isUnlimited() ? "AdminShop" : "Unknown") : Bukkit.getOfflinePlayer(shop.getOwner()).getName()));
        p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.item", shop.getDataName()));
        if (NMS.isPotion(items.getType())) {
            String effects = CustomPotionsName.getEffects(items);
            if (!effects.isEmpty()) {
                p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.effects", effects));
            }
        }
        if (Util.isTool(items.getType())) {
            p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.damage-percent-remaining", Util.getToolPercentage(items)));
        }
        if (shop.isSelling()) {
            p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.stock", "" + stock));
        } else {
            int space = shop.getRemainingSpace();
            p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.space", "" + space));
        }
        if (shop.isBuying()) {
            p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.price-per-buy", shop.getDataName(), Util.format(shop.getPrice() * (1 - plugin.getConfig().getDouble("tax"))), Util.format(shop.getPrice()), Util.format(shop.getPrice() * plugin.getConfig().getDouble("tax"))));
            p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.this-shop-is-buying"));
        } else {
            p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.price-per-sell", shop.getDataName(), Util.format(shop.getPrice() * (plugin.getConfig().getDouble("tax") + 1)), Util.format(shop.getPrice()), Util.format(shop.getPrice() * plugin.getConfig().getDouble("tax"))));
            p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.this-shop-is-selling"));
        }
        Map<Enchantment, Integer> enchants = items.getItemMeta().getEnchants();
        if (enchants != null && !enchants.isEmpty()) {
            p.sendMessage(ChatColor.DARK_PURPLE + "+--------------------" + MsgUtil.getMessage("menu.enchants") + "-----------------------+");
            for (Entry<Enchantment, Integer> entries : enchants.entrySet()) {
                p.sendMessage(ChatColor.DARK_PURPLE + "| " + ChatColor.YELLOW + entries.getKey().getName() + " " + entries.getValue());
            }
        }
        try {
            Class.forName("org.bukkit.inventory.meta.EnchantmentStorageMeta");
            if (items.getItemMeta() instanceof EnchantmentStorageMeta) {
                EnchantmentStorageMeta storage = (EnchantmentStorageMeta) items.getItemMeta();
                storage.getStoredEnchants();
                enchants = storage.getStoredEnchants();
                if (enchants != null && !enchants.isEmpty()) {
                    p.sendMessage(ChatColor.DARK_PURPLE + "+-----------------" + MsgUtil.getMessage("menu.stored-enchants") + "--------------------+");
                    for (Entry<Enchantment, Integer> entries : enchants.entrySet()) {
                        p.sendMessage(ChatColor.DARK_PURPLE + "| " + ChatColor.YELLOW + entries.getKey().getName() + " " + entries.getValue());
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            // They don't have an up to date enough build of CB to do this.
            // TODO: Remove this when it becomes redundant
        }
        p.sendMessage(ChatColor.DARK_PURPLE + "+---------------------------------------------------+");
    }

    public static void sendPurchaseSuccess(Player p, Shop shop, int amount, double tax) {
        p.sendMessage(ChatColor.DARK_PURPLE + "+---------------------------------------------------+");
        p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.successful-purchase"));
        if (tax > 0) {
            p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.item-name-and-price-tax", "" + amount, shop.getDataName(), Util.format((amount * shop.getPrice()) + tax), Util.format(tax)));
        } else {
            p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.item-name-and-price", "" + amount, shop.getDataName(), Util.format(amount * shop.getPrice())));
        }
        Map<Enchantment, Integer> enchants = shop.getItem().getItemMeta().getEnchants();
        if (enchants != null && !enchants.isEmpty()) {
            p.sendMessage(ChatColor.DARK_PURPLE + "+--------------------" + MsgUtil.getMessage("menu.enchants") + "-----------------------+");
            for (Entry<Enchantment, Integer> entries : enchants.entrySet()) {
                p.sendMessage(ChatColor.DARK_PURPLE + "| " + ChatColor.YELLOW + entries.getKey().getName() + " " + entries.getValue());
            }
        }
        enchants = shop.getItem().getItemMeta().getEnchants();
        if (enchants != null && !enchants.isEmpty()) {
            p.sendMessage(ChatColor.DARK_PURPLE + "+-----------------" + MsgUtil.getMessage("menu.stored-enchants") + "--------------------+");
            for (Entry<Enchantment, Integer> entries : enchants.entrySet()) {
                p.sendMessage(ChatColor.DARK_PURPLE + "| " + ChatColor.YELLOW + entries.getKey().getName() + " " + entries.getValue());
            }
        }
        try {
            Class.forName("org.bukkit.inventory.meta.EnchantmentStorageMeta");
            if (shop.getItem().getItemMeta() instanceof EnchantmentStorageMeta) {
                EnchantmentStorageMeta storage = (EnchantmentStorageMeta) shop.getItem().getItemMeta();
                storage.getStoredEnchants();
                enchants = storage.getStoredEnchants();
                if (enchants != null && !enchants.isEmpty()) {
                    p.sendMessage(ChatColor.DARK_PURPLE + "+-----------------" + MsgUtil.getMessage("menu.stored-enchants") + "--------------------+");
                    for (Entry<Enchantment, Integer> entries : enchants.entrySet()) {
                        p.sendMessage(ChatColor.DARK_PURPLE + "| " + ChatColor.YELLOW + entries.getKey().getName() + " " + entries.getValue());
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            // They don't have an up to date enough build of CB to do this.
            // TODO: Remove this when it becomes redundant
        }
        p.sendMessage(ChatColor.DARK_PURPLE + "+---------------------------------------------------+");
    }

    public static void sendSellSuccess(Player p, Shop shop, int amount, double tax) {
        p.sendMessage(ChatColor.DARK_PURPLE + "+---------------------------------------------------+");
        p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.successfully-sold"));
        if (tax > 0) {
            p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.sold-item-name-and-price-tax", "" + amount, shop.getDataName(), Util.format((amount * shop.getPrice()) - tax), Util.format(tax)));
        } else {
            p.sendMessage(ChatColor.DARK_PURPLE + "| " + MsgUtil.getMessage("menu.item-name-and-price", "" + amount, shop.getDataName(), Util.format(amount * shop.getPrice())));
        }
        Map<Enchantment, Integer> enchants = shop.getItem().getItemMeta().getEnchants();
        if (enchants != null && !enchants.isEmpty()) {
            p.sendMessage(ChatColor.DARK_PURPLE + "+--------------------" + MsgUtil.getMessage("menu.enchants") + "-----------------------+");
            for (Entry<Enchantment, Integer> entries : enchants.entrySet()) {
                p.sendMessage(ChatColor.DARK_PURPLE + "| " + ChatColor.YELLOW + entries.getKey().getName() + " " + entries.getValue());
            }
        }
        try {
            Class.forName("org.bukkit.inventory.meta.EnchantmentStorageMeta");
            if (shop.getItem().getItemMeta() instanceof EnchantmentStorageMeta) {
                EnchantmentStorageMeta storage = (EnchantmentStorageMeta) shop.getItem().getItemMeta();
                storage.getStoredEnchants();
                enchants = storage.getStoredEnchants();
                if (enchants != null && !enchants.isEmpty()) {
                    p.sendMessage(ChatColor.DARK_PURPLE + "+--------------------" + MsgUtil.getMessage("menu.stored-enchants") + "-----------------------+");
                    for (Entry<Enchantment, Integer> entries : enchants.entrySet()) {
                        p.sendMessage(ChatColor.DARK_PURPLE + "| " + ChatColor.YELLOW + entries.getKey().getName() + " " + entries.getValue());
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            // They don't have an up to date enough build of CB to do this.
            // TODO: Remove this when it becomes redundant
        }
        p.sendMessage(ChatColor.DARK_PURPLE + "+---------------------------------------------------+");
    }

    public static String getMessage(String loc, String... args) {
        String raw = messages.getString(loc);
        if (raw == null || raw.isEmpty()) {
            return "Invalid message: " + loc;
        }
        if (args == null) {
            return raw;
        }
        for (int i = 0; i < args.length; i++) {
            raw = raw.replace("{" + i + "}", args[i] == null ? "null" : args[i]);
        }
        return raw;
    }
}
