package org.maxgamer.quickshop.shop;

public enum ShopType {
    SELLING(0), BUYING(1);
    private int id;

    ShopType(int id) {
        this.id = id;
    }

    public static ShopType fromID(int id) {
        for (ShopType type : ShopType.values()) {
            if (type.id == id) {
                return type;
            }
        }
        return null;
    }

    public static int toID(ShopType shopType) {
        return shopType.id;
    }

    public int toID() {
        return id;
    }
}