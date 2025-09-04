package com.ghostchu.quickshop.shop.interaction.behaviors;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.QuickShopAPI;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.ShopType;
import com.ghostchu.quickshop.api.shop.interaction.InteractionBehavior;
import com.ghostchu.quickshop.api.shop.interaction.InteractionClick;
import com.ghostchu.quickshop.api.shop.interaction.InteractionType;
import com.ghostchu.quickshop.api.shop.lottery.LotteryItem;
import com.ghostchu.quickshop.api.shop.lottery.LotteryPool;
import com.ghostchu.quickshop.util.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles lottery preview when players right-click lottery shop signs
 */
public class LotteryPreviewBehavior implements InteractionBehavior {

    @Override
    public String identifier() {
        return "LOTTERY_PREVIEW";
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

        LotteryPool lotteryPool = shop.getLotteryPool();
        if (lotteryPool == null || lotteryPool.isEmpty()) {
            ((QuickShop)plugin).text().of(player, "lottery.shop-empty").send();
            return;
        }

        // Create preview inventory
        Inventory previewInventory = Bukkit.createInventory(null, 54, 
            ((QuickShop)plugin).text().of("lottery.preview-title", shop.ownerName()).forLocale(player.locale().getLanguage()));

        List<ItemStack> containerItems = shop.getContainerItems();
        List<LotteryItem> lotteryItems = lotteryPool.getLotteryItems(containerItems);
        DecimalFormat df = new DecimalFormat("#.##%");
        
        for (int i = 0; i < lotteryItems.size() && i < 54; i++) {
            LotteryItem lotteryItem = lotteryItems.get(i);
            ItemStack displayItem = lotteryItem.getItem().clone();
            
            // Add probability information to lore
            ItemMeta meta = displayItem.getItemMeta();
            if (meta == null) {
                meta = Bukkit.getItemFactory().getItemMeta(displayItem.getType());
            }
            
            List<Component> lore = meta.lore();
            if (lore == null) {
                lore = new ArrayList<>();
            }
            
            lore.add(Component.empty());
            lore.add(Component.text("抽奖概率: ", NamedTextColor.GOLD)
                .append(Component.text(df.format(lotteryItem.getProbability()), NamedTextColor.YELLOW)));
            
            if (lotteryItem.getMinAmount() != lotteryItem.getMaxAmount()) {
                lore.add(Component.text("数量范围: ", NamedTextColor.GOLD)
                    .append(Component.text(lotteryItem.getMinAmount() + "-" + lotteryItem.getMaxAmount(), NamedTextColor.YELLOW)));
            }
            
            meta.lore(lore);
            displayItem.setItemMeta(meta);
            displayItem.setAmount(Math.max(1, (lotteryItem.getMinAmount() + lotteryItem.getMaxAmount()) / 2));
            
            previewInventory.setItem(i, displayItem);
        }

        // Add information item
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.displayName(Component.text("抽奖信息", NamedTextColor.GOLD));
        
        List<Component> infoLore = new ArrayList<>();
        infoLore.add(Component.text("商店主人: ", NamedTextColor.GRAY).append(shop.ownerName()));
        infoLore.add(Component.text("抽奖价格: ", NamedTextColor.GRAY)
            .append(Component.text(((QuickShop)plugin).getShopManager().format(shop.getPrice(), shop), NamedTextColor.YELLOW)));
        infoLore.add(Component.text("总概率: ", NamedTextColor.GRAY)
            .append(Component.text(df.format(lotteryPool.getTotalProbability()), NamedTextColor.YELLOW)));
        infoLore.add(Component.text("物品数量: ", NamedTextColor.GRAY)
            .append(Component.text(String.valueOf(lotteryItems.size()), NamedTextColor.YELLOW)));
        
        infoMeta.lore(infoLore);
        infoItem.setItemMeta(infoMeta);
        previewInventory.setItem(53, infoItem);

        player.openInventory(previewInventory);
    }
}
