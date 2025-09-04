package com.ghostchu.quickshop.menu.lottery;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.lottery.LotteryItem;
import com.ghostchu.quickshop.api.shop.lottery.LotteryPool;
import com.ghostchu.quickshop.menu.shared.QuickShopPage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.tnemc.item.AbstractItemStack;
import net.tnemc.item.bukkit.BukkitItemStack;
import net.tnemc.menu.core.builder.IconBuilder;
import net.tnemc.menu.core.callbacks.page.PageOpenCallback;
import net.tnemc.menu.core.icon.action.ActionType;
import net.tnemc.menu.core.icon.action.impl.ChatAction;
import net.tnemc.menu.core.icon.action.impl.RunnableAction;
import net.tnemc.menu.core.viewer.MenuViewer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Main page for lottery management GUI
 * New logic: Shows all container items with their probability settings
 */
public class MainPage extends QuickShopPage {

    public MainPage() {
        super(1);
        setOpen(this::handle);
    }

    public void handle(PageOpenCallback callback) {
        callback.getPage().getIcons().clear();

        final UUID id = callback.getPlayer().identifier();

        final Optional<MenuViewer> viewer = callback.getPlayer().viewer();
        if (viewer.isPresent()) {
            final Optional<Shop> shop = getShop(viewer.get());
            if (shop.isPresent() && shop.get().isLottery()) {
                LotteryPool lotteryPool = shop.get().getLotteryPool();
                if (lotteryPool == null) {
                    // Initialize empty lottery pool
                    lotteryPool = new com.ghostchu.quickshop.shop.lottery.SimpleLotteryPool();
                    shop.get().setLotteryPool(lotteryPool);
                }

                loadLotteryItems(callback, viewer.get(), shop.get(), lotteryPool);
            }
        }
    }

    private void loadLotteryItems(PageOpenCallback callback, MenuViewer viewer, Shop shop, LotteryPool lotteryPool) {
        final UUID playerId = callback.getPlayer().identifier();
        final Player player = Bukkit.getPlayer(playerId);
        
        if (player == null) return;

        // Clear existing icons
        callback.getPage().getIcons().clear();

        // Get all items from container instead of LotteryPool
        List<ItemStack> containerItems = shop.getContainerItems();
        List<LotteryItem> lotteryItems = lotteryPool.getLotteryItems(containerItems);
        DecimalFormat df = new DecimalFormat("#.##%");
        
        if (lotteryItems.isEmpty()) {
            // Show message when no items in container
            callback.getPage().addIcon(
                new IconBuilder(QuickShop.getInstance().stack().of("BARRIER", 1)
                    .display(Component.text("容器为空", NamedTextColor.RED))
                    .lore(List.of(Component.text("请先在抽奖箱中放入物品", NamedTextColor.GRAY))))
                    .withSlot(22)
                    .build()
            );
            return;
        }
        
        // Display lottery items (max 45 slots for items)
        for (int i = 0; i < lotteryItems.size() && i < 45; i++) {
            LotteryItem lotteryItem = lotteryItems.get(i);
            final String itemKey = lotteryPool.generateItemKey(lotteryItem.getItem());
            
            ItemStack displayItem = lotteryItem.getItem().clone();
            AbstractItemStack<?> abstractItem = new BukkitItemStack().of(displayItem);
            
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("概率: " + df.format(lotteryItem.getProbability()), NamedTextColor.YELLOW));
            lore.add(Component.text("数量: " + lotteryItem.getItem().getAmount(), NamedTextColor.GRAY));
            lore.add(Component.empty());
            lore.add(Component.text("左键: 修改概率", NamedTextColor.GREEN));
            lore.add(Component.text("右键: 重置概率为0%", NamedTextColor.RED));
            
            abstractItem.lore(lore);
            
            // Left click to modify probability
            IconBuilder iconBuilder = new IconBuilder(abstractItem)
                .withSlot(i)
                .withActions(
                    new ChatAction((message) -> {
                        if (!message.getMessage().isEmpty()) {
                            try {
                                double newProbability = Double.parseDouble(message.getMessage().replace("%", "")) / 100.0;
                                if (newProbability < 0 || newProbability > 1) {
                                    org.bukkit.entity.Player bukPlayer = Bukkit.getPlayer(message.getPlayer().identifier());
                                if (bukPlayer != null) {
                                    QuickShop.getInstance().text().of(bukPlayer, "lottery.invalid-probability").send();
                                }
                                    return true;
                                }
                                
                                // Update the item probability by key
                                lotteryPool.setItemProbability(itemKey, newProbability);
                                
                                org.bukkit.entity.Player bukPlayer2 = Bukkit.getPlayer(message.getPlayer().identifier());
                                if (bukPlayer2 != null) {
                                    QuickShop.getInstance().text().of(bukPlayer2, "lottery.probability-updated", 
                                        df.format(newProbability)).send();
                                }
                                
                                // Close menu and refresh
                                message.getPlayer().inventory().close();
                                return true;
                                
                            } catch (NumberFormatException e) {
                                org.bukkit.entity.Player bukPlayer3 = Bukkit.getPlayer(message.getPlayer().identifier());
                                if (bukPlayer3 != null) {
                                    QuickShop.getInstance().text().of(bukPlayer3, "lottery.invalid-number").send();
                                }
                                return true;
                            }
                        }
                        
                        // Show prompt
                        org.bukkit.entity.Player bukPlayer4 = Bukkit.getPlayer(message.getPlayer().identifier());
                        if (bukPlayer4 != null) {
                            QuickShop.getInstance().text().of(bukPlayer4, "lottery.enter-probability", 
                                df.format(lotteryItem.getProbability())).send();
                        }
                        return false;
                    }, ActionType.LEFT_CLICK)
                )
                // Right click to reset probability to 0%
                .withActions(
                    new RunnableAction((click) -> {
                        lotteryPool.removeItemProbability(itemKey);
                        QuickShop.getInstance().text().of(player, "lottery.probability-reset").send();
                        
                        // Refresh the page
                        loadLotteryItems(callback, viewer, shop, lotteryPool);
                    }, ActionType.RIGHT_CLICK)
                );
            
            callback.getPage().addIcon(iconBuilder.build());
        }

        // Clear all probabilities button (slot 50)
        callback.getPage().addIcon(
            new IconBuilder(QuickShop.getInstance().stack().of("RED_WOOL", 1)
                .display(Component.text("重置所有概率", NamedTextColor.RED))
                .lore(List.of(Component.text("点击以将所有物品概率重置为0%", NamedTextColor.WHITE))))
            .withSlot(50)
            .withActions(new RunnableAction((click) -> {
                LotteryPool pool = shop.getLotteryPool();
                if (pool != null) {
                    pool.clear();
                    QuickShop.getInstance().text().of(player, "lottery.all-probabilities-reset").send();
                    
                    // Refresh the page
                    loadLotteryItems(callback, viewer, shop, lotteryPool);
                }
            }))
            .build()
        );
    }
}