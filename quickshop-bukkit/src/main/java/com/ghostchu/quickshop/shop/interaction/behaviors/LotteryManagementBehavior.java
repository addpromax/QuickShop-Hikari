package com.ghostchu.quickshop.shop.interaction.behaviors;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopType;
import com.ghostchu.quickshop.api.shop.interaction.InteractionBehavior;
import com.ghostchu.quickshop.api.shop.interaction.InteractionClick;
import com.ghostchu.quickshop.api.shop.interaction.InteractionType;
import com.ghostchu.quickshop.api.shop.permission.BuiltInShopPermissionGroup;
import com.ghostchu.quickshop.menu.ShopKeeperMenu;
import net.tnemc.menu.core.compatibility.MenuPlayer;
import net.tnemc.menu.core.manager.MenuManager;
import net.tnemc.menu.core.viewer.MenuViewer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Handles lottery management when shop owners interact with lottery shops
 */
public class LotteryManagementBehavior implements InteractionBehavior {

    @Override
    public String identifier() {
        return "LOTTERY_MANAGEMENT";
    }

    @Override
    public void handle(@NotNull QuickShopAPI plugin, @Nullable Shop shop, @NotNull Player player, 
                      @NotNull PlayerInteractEvent event, @NotNull InteractionClick clickType, 
                      @Nullable InteractionType interaction) {
        if (shop == null || shop.getShopType() != ShopType.LOTTERY) {
            return;
        }

        // Check if player has management permissions
        String group = shop.getPlayerGroup(player.getUniqueId());
        if (!group.equalsIgnoreCase(BuiltInShopPermissionGroup.STAFF.getNamespacedNode())
            && !group.equalsIgnoreCase(BuiltInShopPermissionGroup.ADMINISTRATOR.getNamespacedNode())) {
            // If not staff or admin, show preview instead
            new LotteryPreviewBehavior().handle(plugin, shop, player, event, clickType, interaction);
            return;
        }

        event.setCancelled(true);
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);

        // Open lottery management GUI
        MenuViewer viewer = new MenuViewer(player.getUniqueId());
        viewer.addData(ShopKeeperMenu.SHOP_DATA_ID, shop.getShopId());
        viewer.addData("LOTTERY_MANAGEMENT", true);

        MenuManager.instance().addViewer(viewer);

        MenuPlayer menuPlayer = QuickShop.getInstance().createMenuPlayer(player);
        MenuManager.instance().open("qs:lottery-management", 1, menuPlayer);
    }
}
