package com.ghostchu.quickshop.shop.lottery;

import com.ghostchu.quickshop.api.obj.QUser;
import com.ghostchu.quickshop.api.shop.Shop;
import com.ghostchu.quickshop.api.shop.lottery.LotteryItem;
import com.ghostchu.quickshop.api.shop.lottery.LotteryPool;
import com.ghostchu.quickshop.util.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Utility class for lottery operations
 */
public class LotteryUtil {
    
    /**
     * Gets the display name of an ItemStack, falling back to its material name if no custom name is set.
     *
     * @param itemStack The ItemStack to get the name for.
     * @return The display name of the item.
     */
    @NotNull
    public static String getItemName(@NotNull ItemStack itemStack) {
        Component name = Util.getItemStackName(itemStack);
        return PlainTextComponentSerializer.plainText().serialize(name);
    }
    
    /**
     * Serializes a lottery pool to YAML configuration
     * 
     * @param lotteryPool The lottery pool to serialize
     * @return YAML string representation
     */
    @NotNull
    public static String serializeLotteryPool(@Nullable LotteryPool lotteryPool) {
        if (lotteryPool == null || lotteryPool.isEmpty()) {
            return "";
        }
        
        YamlConfiguration config = new YamlConfiguration();
        Map<String, Double> probabilities = lotteryPool.getAllProbabilities();
        
        ConfigurationSection probabilitiesSection = config.createSection("probabilities");
        for (Map.Entry<String, Double> entry : probabilities.entrySet()) {
            probabilitiesSection.set(entry.getKey(), entry.getValue());
        }
        
        return config.saveToString();
    }
    
    /**
     * Deserializes a lottery pool from YAML configuration
     * 
     * @param yamlString The YAML string to deserialize
     * @return Deserialized lottery pool, or null if failed
     */
    @Nullable
    public static LotteryPool deserializeLotteryPool(@Nullable String yamlString) {
        if (yamlString == null || yamlString.trim().isEmpty()) {
            return new SimpleLotteryPool();
        }
        
        try {
            YamlConfiguration config = new YamlConfiguration();
            config.loadFromString(yamlString);
            
            SimpleLotteryPool lotteryPool = new SimpleLotteryPool();
            
            // Check for new format (probabilities)
            ConfigurationSection probabilitiesSection = config.getConfigurationSection("probabilities");
            if (probabilitiesSection != null) {
                for (String itemKey : probabilitiesSection.getKeys(false)) {
                    double probability = probabilitiesSection.getDouble(itemKey, 0.0);
                    lotteryPool.setItemProbability(itemKey, probability);
                }
                return lotteryPool;
            }
            
            // Legacy format conversion (item-based)
            for (String key : config.getKeys(false)) {
                ConfigurationSection section = config.getConfigurationSection(key);
                if (section == null) {
                    continue;
                }
                
                String itemString = section.getString("item");
                if (itemString == null) {
                    continue;
                }
                
                ItemStack item = Util.deserialize(itemString);
                if (item == null) {
                    continue;
                }
                
                double probability = section.getDouble("probability", 0.1);
                
                // Convert to new format using item key
                String itemKey = lotteryPool.generateItemKey(item);
                lotteryPool.setItemProbability(itemKey, probability);
            }
            
            return lotteryPool;
        } catch (Exception e) {
            return new SimpleLotteryPool();
        }
    }
    
    /**
     * Creates a ShopLotteryDrawLog for logging lottery transactions.
     *
     * @param player The player who drew from the lottery.
     * @param shop The shop where the lottery draw occurred.
     * @param wonItem The item won by the player.
     * @param price The price paid for the draw.
     * @return A new ShopLotteryDrawLog instance.
     */
    @NotNull
    public static com.ghostchu.quickshop.util.logging.container.ShopLotteryDrawLog createLotteryDrawLog(@NotNull QUser player, @NotNull Shop shop, @NotNull ItemStack wonItem, double price) {
        return new com.ghostchu.quickshop.util.logging.container.ShopLotteryDrawLog(
                player,
                shop.saveToInfoStorage(),
                getItemName(wonItem),
                wonItem.getAmount(),
                price
        );
    }
}
