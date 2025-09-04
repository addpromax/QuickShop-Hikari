package com.ghostchu.quickshop.shop.interaction.behaviors;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.economy.transaction.EconomyTransaction;
import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopType;
import com.ghostchu.quickshop.api.shop.interaction.InteractionBehavior;
import com.ghostchu.quickshop.api.shop.interaction.InteractionClick;
import com.ghostchu.quickshop.api.shop.interaction.InteractionType;
import com.ghostchu.quickshop.economy.transaction.QSEconomyTransaction;
import com.ghostchu.quickshop.obj.QUserImpl;
import com.ghostchu.quickshop.shop.lottery.LotteryUtil;
import com.ghostchu.quickshop.util.Util;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;

/**
 * Handles lottery drawing behavior when players interact with lottery shops
 */
public class LotteryBehavior implements InteractionBehavior {

    @Override
    public String identifier() {
        return "LOTTERY";
    }

    @Override
    public void handle(@NotNull QuickShopAPI plugin, @Nullable Shop shop, @NotNull Player player, 
                      @NotNull PlayerInteractEvent event, @NotNull InteractionClick clickType, 
                      @Nullable InteractionType interaction) {
        if (shop == null || shop.getShopType() != ShopType.LOTTERY) {
            return;
        }

        event.setCancelled(true);
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);

        if (shop.isFrozen()) {
            ((QuickShop)plugin).text().of(player, "shop-cannot-trade-when-freezing").send();
            return;
        }

        if (!shop.inventoryAvailable()) {
            ((QuickShop)plugin).text().of(player, "lottery.shop-empty").send();
            return;
        }

        // Check if player can afford the lottery
        QUser qUser = QUserImpl.createFullFilled(player);
        double lotteryPrice = shop.getPrice();

        if (lotteryPrice > 0) {
            EconomyTransaction transaction = QSEconomyTransaction.builder()
                .from(qUser)
                .to(shop.getOwner())
                .amount(BigDecimal.valueOf(lotteryPrice))
                .currency(shop.getCurrency())
                .world(player.getWorld().getName())
                .benefitManager(shop.getShopBenefit())
                .tax(BigDecimal.ZERO)
                .taxer(null)
                .build();

            if (!transaction.completable()) {
                ((QuickShop)plugin).text().of(player, "you-cant-afford-to-buy", 
                    ((QuickShop)plugin).getShopManager().format(lotteryPrice, shop)).send();
                return;
            }

            if (!transaction.safeCommit()) {
                ((QuickShop)plugin).text().of(player, "economy-transaction-failed", ((QSEconomyTransaction)transaction).lastError()).send();
                return;
            }
        }

        // Draw from lottery
        ItemStack reward = shop.drawLottery(qUser);
        
        if (reward == null) {
            ((QuickShop)plugin).text().of(player, "lottery.draw-failed").send();
            // Refund if no reward
            if (lotteryPrice > 0) {
                EconomyTransaction refund = QSEconomyTransaction.builder()
                    .from(shop.getOwner())
                    .to(qUser)
                    .amount(BigDecimal.valueOf(lotteryPrice))
                    .currency(shop.getCurrency())
                    .world(player.getWorld().getName())
                    .benefitManager(shop.getShopBenefit())
                    .tax(BigDecimal.ZERO)
                    .taxer(null)
                    .build();
                refund.safeCommit();
            }
            return;
        }

        // Give reward to player
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(reward);
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), reward);
            ((QuickShop)plugin).text().of(player, "lottery.reward-dropped").send();
        }

        // Send success message
        String rewardText = reward.getAmount() + "x " + LotteryUtil.getItemName(reward);
        ((QuickShop)plugin).text().of(player, "lottery.draw-success", rewardText).send();
        
        // Log the transaction
        ((QuickShop)plugin).logEvent(LotteryUtil.createLotteryDrawLog(qUser, shop, reward, lotteryPrice));
        
        // Notify shop owner if not unlimited
        if (!shop.isUnlimited() && !shop.getOwner().getUniqueId().equals(player.getUniqueId())) {
            shop.getOwner().getBukkitPlayer().ifPresent(ownerPlayer -> {
                ((QuickShop)plugin).text().of(ownerPlayer, "lottery.player-drew-from-shop", 
                    player.getName(), rewardText, 
                    ((QuickShop)plugin).getShopManager().format(lotteryPrice, shop)).send();
            });
        }
    }
}
