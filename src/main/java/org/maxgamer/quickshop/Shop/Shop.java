package org.maxgamer.quickshop.shop;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public interface Shop {
    Shop clone();

    int getRemainingStock();

    int getRemainingSpace();

    boolean matches(ItemStack paramItemStack);

    Location getLocation();

    double getPrice();

    void setPrice(double paramDouble);

    void update();

    short getDurability();

    UUID getOwner();

    ItemStack getItem();

    void remove(ItemStack paramItemStack, int paramInt);

    void add(ItemStack paramItemStack, int paramInt);

    void sell(Player paramPlayer, int paramInt);

    void buy(Player paramPlayer, int paramInt);

    void setOwner(UUID paramString);

    void setUnlimited(boolean paramBoolean);

    boolean isUnlimited();

    ShopType getShopType();

    boolean isBuying();

    boolean isSelling();

    void setShopType(ShopType paramShopType);

    void setSignText();

    void setSignText(String[] paramArrayOfString);

    List<Sign> getSigns();

    boolean isAttached(Block paramBlock);

    String getDataName();

    void delete();

    void delete(boolean paramBoolean);

    boolean isValid();

    void onUnload();

    void onLoad();

    void onClick();
}