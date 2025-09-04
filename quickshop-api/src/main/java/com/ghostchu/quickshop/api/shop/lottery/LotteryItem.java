package com.ghostchu.quickshop.api.shop.lottery;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an item in a lottery pool with its associated probability
 */
public class LotteryItem {
    
    @NotNull
    private final ItemStack item;
    
    private final double probability;
    
    private final int minAmount;
    
    private final int maxAmount;

    /**
     * Creates a new LotteryItem
     * 
     * @param item The item to be rewarded
     * @param probability The probability of getting this item (0.0 to 1.0)
     * @param minAmount Minimum amount of this item to give
     * @param maxAmount Maximum amount of this item to give
     */
    public LotteryItem(@NotNull ItemStack item, double probability, int minAmount, int maxAmount) {
        this.item = item.clone();
        this.probability = Math.max(0.0, Math.min(1.0, probability)); // Clamp between 0 and 1
        this.minAmount = Math.max(1, minAmount);
        this.maxAmount = Math.max(this.minAmount, maxAmount);
    }

    /**
     * Creates a new LotteryItem with fixed amount
     * 
     * @param item The item to be rewarded
     * @param probability The probability of getting this item (0.0 to 1.0)
     */
    public LotteryItem(@NotNull ItemStack item, double probability) {
        this(item, probability, item.getAmount(), item.getAmount());
    }

    /**
     * Gets the item
     * 
     * @return The item
     */
    @NotNull
    public ItemStack getItem() {
        return item.clone();
    }

    /**
     * Gets the probability
     * 
     * @return The probability (0.0 to 1.0)
     */
    public double getProbability() {
        return probability;
    }

    /**
     * Gets the minimum amount
     * 
     * @return The minimum amount
     */
    public int getMinAmount() {
        return minAmount;
    }

    /**
     * Gets the maximum amount
     * 
     * @return The maximum amount
     */
    public int getMaxAmount() {
        return maxAmount;
    }

    /**
     * Generates a random amount between min and max
     * 
     * @return A random amount
     */
    public int getRandomAmount() {
        if (minAmount == maxAmount) {
            return minAmount;
        }
        return minAmount + (int) (Math.random() * (maxAmount - minAmount + 1));
    }

    /**
     * Creates an ItemStack with random amount
     * 
     * @return ItemStack with random amount
     */
    @NotNull
    public ItemStack createReward() {
        ItemStack reward = item.clone();
        reward.setAmount(getRandomAmount());
        return reward;
    }
}
