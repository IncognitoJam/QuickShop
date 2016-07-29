package org.maxgamer.quickshop.shop;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class Info {
    private Location loc;
    private ShopAction action;
    private ItemStack item;
    private Block last;
    private Shop shop;

    public Info(Location loc, ShopAction action, ItemStack item, Block last) {
        this.loc = loc;
        this.action = action;
        this.last = last;
        if (item != null)
            this.item = item.clone();
    }

    public Info(Location loc, ShopAction action, ItemStack item, Block last, Shop shop) {
        this.loc = loc;
        this.action = action;
        this.last = last;
        if (item != null)
            this.item = item.clone();
        if (shop != null) {
            this.shop = shop.clone();
        }
    }

    public boolean hasChanged(Shop shop) {
        if (this.shop.isUnlimited() != shop.isUnlimited())
            return true;
        if (this.shop.getShopType() != shop.getShopType())
            return true;
        if (!this.shop.getOwner().equals(shop.getOwner()))
            return true;
        if (this.shop.getPrice() != shop.getPrice())
            return true;
        if (!this.shop.getLocation().equals(shop.getLocation()))
            return true;
        return !this.shop.matches(shop.getItem());
    }

    public ShopAction getAction() {
        return this.action;
    }

    public Location getLocation() {
        return this.loc;
    }

    /*
     * public Material getMaterial(){ return this.item.getType(); } public byte
     * getData(){ return this.getData(); }
     */
    public ItemStack getItem() {
        return this.item;
    }

    public void setAction(ShopAction action) {
        this.action = action;
    }

    public Block getSignBlock() {
        return this.last;
    }
}