package com.ghostchu.quickshop.shop.lottery;

import com.ghostchu.quickshop.api.shop.lottery.LotteryItem;
import com.ghostchu.quickshop.api.shop.lottery.LotteryPool;
import com.ghostchu.quickshop.util.Util;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Simple implementation of LotteryPool
 * New logic: Stores only probability mappings, gets actual items from container
 */
public class SimpleLotteryPool implements LotteryPool {
    
    private final Map<String, Double> itemProbabilities;
    private final Random random;
    
    public SimpleLotteryPool() {
        this.itemProbabilities = new HashMap<>();
        this.random = new Random();
    }
    
    @Override
    public void setItemProbability(@NotNull String itemKey, double probability) {
        if (probability <= 0) {
            itemProbabilities.remove(itemKey);
        } else {
            itemProbabilities.put(itemKey, Math.min(1.0, Math.max(0.0, probability)));
        }
    }
    
    @Override
    public double getItemProbability(@NotNull String itemKey) {
        return itemProbabilities.getOrDefault(itemKey, 0.0);
    }
    
    @Override
    public void removeItemProbability(@NotNull String itemKey) {
        itemProbabilities.remove(itemKey);
    }
    
    @Override
    @NotNull
    public Map<String, Double> getAllProbabilities() {
        return new HashMap<>(itemProbabilities);
    }
    
    @Override
    @NotNull
    public List<LotteryItem> getLotteryItems(@NotNull List<ItemStack> containerItems) {
        // Group items by key to avoid duplicates
        Map<String, ItemStack> uniqueItems = new HashMap<>();
        
        for (ItemStack item : containerItems) {
            if (item != null && !item.getType().isAir()) {
                String key = generateItemKey(item);
                uniqueItems.put(key, item);
            }
        }
        
        // Create LotteryItems with probabilities (default 0% for items without set probability)
        return uniqueItems.entrySet().stream()
            .map(entry -> {
                String itemKey = entry.getKey();
                ItemStack item = entry.getValue();
                double probability = getItemProbability(itemKey);
                
                // For lottery, min and max amount are based on the item's current stack size
                int amount = item.getAmount();
                return new LotteryItem(item.clone(), probability, 1, Math.max(1, amount));
            })
            .collect(Collectors.toList());
    }
    
    @Override
    @Nullable
    public ItemStack drawItem(@NotNull List<ItemStack> containerItems) {
        List<LotteryItem> lotteryItems = getLotteryItems(containerItems);
        
        if (lotteryItems.isEmpty()) {
            return null;
        }
        
        // Filter items with probability > 0
        List<LotteryItem> validItems = lotteryItems.stream()
            .filter(item -> item.getProbability() > 0)
            .collect(Collectors.toList());
        
        if (validItems.isEmpty()) {
            return null;
        }
        
        double totalWeight = validItems.stream()
            .mapToDouble(LotteryItem::getProbability)
            .sum();
            
        if (totalWeight <= 0) {
            return null;
        }
        
        double randomValue = random.nextDouble() * totalWeight;
        double currentWeight = 0;
        
        for (LotteryItem lotteryItem : validItems) {
            currentWeight += lotteryItem.getProbability();
            if (randomValue <= currentWeight) {
                // Check if the item is still available in the container
                String itemKey = generateItemKey(lotteryItem.getItem());
                for (ItemStack containerItem : containerItems) {
                    if (containerItem != null && !containerItem.getType().isAir() 
                        && generateItemKey(containerItem).equals(itemKey)) {
                        return lotteryItem.createReward();
                    }
                }
            }
        }
        
        // Fallback
        if (!validItems.isEmpty()) {
            return validItems.get(validItems.size() - 1).createReward();
        }
        
        return null;
    }
    
    @Override
    public double getTotalProbability() {
        return itemProbabilities.values().stream()
            .mapToDouble(Double::doubleValue)
            .sum();
    }
    
    @Override
    public void clear() {
        itemProbabilities.clear();
    }
    
    @Override
    public boolean isEmpty() {
        return itemProbabilities.isEmpty();
    }
    
    @Override
    @NotNull
    public String generateItemKey(@NotNull ItemStack itemStack) {
        // Generate a unique key based on material, meta, and NBT
        return Util.serialize(itemStack);
    }
}