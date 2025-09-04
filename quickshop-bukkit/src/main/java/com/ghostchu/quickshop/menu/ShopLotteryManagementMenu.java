package com.ghostchu.quickshop.menu;

import com.ghostchu.quickshop.menu.lottery.MainPage;
import com.ghostchu.quickshop.menu.shared.QuickShopMenu;

/**
 * ShopLotteryManagementMenu
 * 
 * Menu for managing lottery shop configurations
 * 
 * @author QuickShop-Hikari
 * @since 6.3.0.0
 */
public class ShopLotteryManagementMenu extends QuickShopMenu {

    public static final int LOTTERY_MANAGEMENT_MAIN = 1;
    public static final String SHOP_DATA_ID = "SHOP_ID";

    public ShopLotteryManagementMenu() {
        this.rows = 6;
        this.name = "qs:lottery-management";

        setOpen((open) -> open.getMenu().setTitle(legacy(open.getPlayer().identifier(), "gui.lottery-management.title")));

        addPage(new MainPage());
    }
}
