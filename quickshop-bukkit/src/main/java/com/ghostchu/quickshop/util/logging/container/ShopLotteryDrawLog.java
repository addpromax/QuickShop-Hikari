package com.ghostchu.quickshop.util.logging.container;

import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.ShopInfoStorage;
import lombok.Data;

@Data
public class ShopLotteryDrawLog {
    
    private static int v = 2;
    private String player;
    private ShopInfoStorage shop;
    private String itemWon;
    private int amountWon;
    private double pricePaid;
    
    public ShopLotteryDrawLog(final QUser player, final ShopInfoStorage shop, final String itemWon, final int amountWon, final double pricePaid) {
        this.player = player.serialize();
        this.shop = shop;
        this.itemWon = itemWon;
        this.amountWon = amountWon;
        this.pricePaid = pricePaid;
    }
}
