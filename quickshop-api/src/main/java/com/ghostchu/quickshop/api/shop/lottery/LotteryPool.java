package com.ghostchu.quickshop.api.shop.lottery;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Interface for managing lottery pools in shops
 * New logic: All items in the shop container are potential lottery items with 0% probability by default
 */
public interface LotteryPool {
    
    /**
     * Sets the probability for a specific item type
     * 
     * @param itemKey The key identifying the item type
     * @param probability The probability (0.0 to 1.0)
     */
    void setItemProbability(@NotNull String itemKey, double probability);
    
    /**
     * Gets the probability for a specific item type
     * 
     * @param itemKey The key identifying the item type
     * @return The probability (0.0 if not set)
     */
    double getItemProbability(@NotNull String itemKey);
    
    /**
     * Removes the probability setting for a specific item type (resets to 0%)
     * 
     * @param itemKey The key identifying the item type
     */
    void removeItemProbability(@NotNull String itemKey);
    
    /**
     * Gets all configured item probabilities
     * 
     * @return Map of item keys to probabilities
     */
    @NotNull
    Map<String, Double> getAllProbabilities();
    
    /**
     * Gets all available items from the container with their probabilities
     * This combines container inventory with probability settings
     * 
     * @param containerItems The items available in the container
     * @return List of lottery items
     */
    @NotNull
    List<LotteryItem> getLotteryItems(@NotNull List<ItemStack> containerItems);
    
    /**
     * Draws a random item from the lottery pool based on container contents
     * 
     * @param containerItems The items available in the container
     * @return A random ItemStack based on probabilities, or null if no items or all probabilities are 0
     */
    @Nullable
    ItemStack drawItem(@NotNull List<ItemStack> containerItems);
    
    /**
     * Gets the total probability of all configured items
     * 
     * @return The sum of all item probabilities
     */
    double getTotalProbability();
    
    /**
     * Clears all probability settings
     */
    void clear();
    
    /**
     * Checks if any probabilities are configured
     * 
     * @return true if no probabilities are set, false otherwise
     */
    boolean isEmpty();
    
    /**
     * Generates a unique key for an ItemStack for probability mapping
     * 
     * @param itemStack The ItemStack to generate a key for
     * @return A unique string key
     */
    @NotNull
    String generateItemKey(@NotNull ItemStack itemStack);
}
