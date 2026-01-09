package fr.ax_dev.universejobs.menu.config;

import fr.ax_dev.universejobs.UniverseJobs;
import fr.ax_dev.universejobs.reward.gui.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Crée les ItemStacks de récompense en utilisant RewardItemFormat
 * Supporte les custom items Nexo avec fallback Bukkit materials
 */
public class RewardItemBuilder {
    
    private final UniverseJobs plugin;
    private final RewardItemFormat format;
    
    public RewardItemBuilder(UniverseJobs plugin, RewardItemFormat format) {
        this.plugin = plugin;
        this.format = format;
    }
    
    /**
     * Crée un item de récompense complet
     * @param state État de la récompense (retrievable, blocked, retrieved)
     * @param rewardName Nom de la récompense
     * @param placeholders Map des placeholders personnalisés
     * @param player Joueur pour qui créer l'item
     * @return ItemStack de la récompense
     */
    public ItemStack createRewardItem(
            RewardItemFormat.RewardState state,
            String rewardName,
            Map<String, String> placeholders,
            Player player
    ) {
        // Créer la base de l'item avec Nexo ou fallback
        ItemBuilder builder = createBaseItem(state);
        
        if (builder == null) {
            return null;
        }
        
        builder.amount(1);
        
        // Définir le nom avec placeholders
        String displayName = format.getDisplayNameForState(state);
        displayName = displayName.replace("{reward_name}", rewardName);
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                displayName = displayName.replace(entry.getKey(), entry.getValue());
            }
        }
        builder.name(displayName);
        
        // Définir la lore avec placeholders
        List<String> lore = new ArrayList<>();
        List<String> templateLore = format.getLoreForState(state);
        if (templateLore != null) {
            for (String loreLine : templateLore) {
                // Remplacer les placeholders
                String processedLine = loreLine;
                if (placeholders != null) {
                    for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                        processedLine = processedLine.replace(entry.getKey(), entry.getValue());
                    }
                }
                lore.add(processedLine);
            }
        }
        builder.lore(lore);
        
        ItemStack item = builder.build();
        
        // Appliquer les enchantments et flags
        applyEnchantmentsAndFlags(item, state);
        
        return item;
    }
    
    /**
     * Crée l'item de base (Nexo ou fallback)
     */
    private ItemBuilder createBaseItem(RewardItemFormat.RewardState state) {
        // Essayer d'utiliser l'ID Nexo d'abord
        String nexoId = format.getNexoIdForState(state);
        ItemBuilder builder = ItemBuilder.fromMaterialName(plugin, nexoId);
        
        // Si Nexo ne fonctionne pas, utiliser le fallback
        if (builder == null) {
            Material fallbackMaterial = format.getMaterialForState(state);
            builder = new ItemBuilder(plugin, fallbackMaterial);
            
            plugin.getLogger().fine("Utilisation du fallback material pour " + state + ": " + fallbackMaterial);
        }
        
        return builder;
    }
    
    /**
     * Applique les enchantments et item flags
     */
    private void applyEnchantmentsAndFlags(ItemStack item, RewardItemFormat.RewardState state) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        
        boolean modified = false;
        
        // Ajouter les enchantments
        Map<String, Integer> enchantments = format.getEnchantementsForState(state);
        if (enchantments != null && !enchantments.isEmpty()) {
            for (Map.Entry<String, Integer> entry : enchantments.entrySet()) {
                try {
                    Enchantment enchantment = Enchantment.getByKey(
                        org.bukkit.NamespacedKey.minecraft(entry.getKey().toLowerCase())
                    );
                    if (enchantment != null) {
                        meta.addEnchant(enchantment, entry.getValue(), true);
                        modified = true;
                    }
                } catch (Exception ignored) {}
            }
        }
        
        // Ajouter les item flags
        List<String> flags = format.getItemFlags();
        if (flags != null && !flags.isEmpty()) {
            for (String flagName : flags) {
                try {
                    ItemFlag flag = ItemFlag.valueOf(flagName.toUpperCase());
                    meta.addItemFlags(flag);
                    modified = true;
                } catch (IllegalArgumentException ignored) {}
            }
        }
        
        if (modified) {
            item.setItemMeta(meta);
        }
    }
    
    /**
     * Crée un item de récompense simple (sans placeholders)
     */
    public ItemStack createRewardItem(
            RewardItemFormat.RewardState state,
            String rewardName,
            Player player
    ) {
        return createRewardItem(state, rewardName, null, player);
    }
    
    /**
     * Crée un item de récompense avec les valeurs par défaut
     */
    public ItemStack createSimpleRewardItem(RewardItemFormat.RewardState state) {
        ItemBuilder builder = createBaseItem(state);
        if (builder == null) return null;
        
        builder.amount(1);
        builder.name(format.getDisplayNameForState(state).replace("{reward_name}", "Reward"));
        
        List<String> lore = format.getLoreForState(state);
        if (lore != null && !lore.isEmpty()) {
            builder.lore(lore);
        }
        
        ItemStack item = builder.build();
        applyEnchantmentsAndFlags(item, state);
        
        return item;
    }
}