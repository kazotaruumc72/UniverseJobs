package fr.ax_dev.universejobs.menu.config;

import fr.ax_dev.universejobs.UniverseJobs;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gère la configuration des items de récompenses avec support Nexo
 * Remplace le système de shulker boxes par des custom items Nexo
 */
public class RewardItemFormat {
    
    private final UniverseJobs plugin;
    
    // Configuration Nexo
    private String retrievableNexoId = "nexo:iconic_chest_open";
    private String blockedNexoId = "nexo:iconic_chest";
    private String retrievedNexoId = "nexo:iconic_chest_empty";
    
    // Fallback Bukkit materials
    private Material retrievableMaterial = Material.LIME_SHULKER_BOX;
    private Material blockedMaterial = Material.RED_SHULKER_BOX;
    private Material retrievedMaterial = Material.GRAY_SHULKER_BOX;
    
    // Display names
    private String retrievableDisplayName = "<#32CD32><bold>✓ {reward_name}</bold>";
    private String blockedDisplayName = "<#FF6B6B><bold>✗ {reward_name}</bold>";
    private String retrievedDisplayName = "<#808080><bold>✓ {reward_name}</bold>";
    
    // Lores
    private List<String> retrievableLore;
    private List<String> blockedLore;
    private List<String> retrievedLore;
    
    // Enchantments
    private Map<String, Integer> retrievableEnchantments = new HashMap<>();
    private Map<String, Integer> blockedEnchantments = new HashMap<>();
    private Map<String, Integer> retrievedEnchantments = new HashMap<>();
    
    // Item flags
    private List<String> itemFlags;
    
    public RewardItemFormat(UniverseJobs plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Charge la configuration depuis le fichier YAML du GUI
     */
    public void loadFromConfig(ConfigurationSection rewardItemSection) {
        if (rewardItemSection == null) {
            return;
        }
        
        // Charger les IDs Nexo
        ConfigurationSection nexoSection = rewardItemSection.getConfigurationSection("nexo-items");
        if (nexoSection != null) {
            retrievableNexoId = nexoSection.getString("retrievable", retrievableNexoId);
            blockedNexoId = nexoSection.getString("blocked", blockedNexoId);
            retrievedNexoId = nexoSection.getString("retrieved", retrievedNexoId);
            
            plugin.getLogger().info("IDs Nexo chargés:");
            plugin.getLogger().info("  - Retrievable: " + retrievableNexoId);
            plugin.getLogger().info("  - Blocked: " + blockedNexoId);
            plugin.getLogger().info("  - Retrieved: " + retrievedNexoId);
        }
        
        // Charger les fallback materials
        ConfigurationSection fallbackSection = rewardItemSection.getConfigurationSection("fallback-materials");
        if (fallbackSection != null) {
            retrievableMaterial = parseMaterial(fallbackSection.getString("retrievable", "LIME_SHULKER_BOX"), retrievableMaterial);
            blockedMaterial = parseMaterial(fallbackSection.getString("blocked", "RED_SHULKER_BOX"), blockedMaterial);
            retrievedMaterial = parseMaterial(fallbackSection.getString("retrieved", "GRAY_SHULKER_BOX"), retrievedMaterial);
        }
        
        // Charger les display names
        ConfigurationSection displaySection = rewardItemSection.getConfigurationSection("display-names");
        if (displaySection != null) {
            retrievableDisplayName = displaySection.getString("retrievable", retrievableDisplayName);
            blockedDisplayName = displaySection.getString("blocked", blockedDisplayName);
            retrievedDisplayName = displaySection.getString("retrieved", retrievedDisplayName);
        }
        
        // Charger les lores
        ConfigurationSection loreSection = rewardItemSection.getConfigurationSection("lore");
        if (loreSection != null) {
            retrievableLore = loreSection.getStringList("retrievable");
            blockedLore = loreSection.getStringList("blocked");
            retrievedLore = loreSection.getStringList("retrieved");
        }
        
        // Charger les enchantments
        ConfigurationSection enchantSection = rewardItemSection.getConfigurationSection("enchantments");
        if (enchantSection != null) {
            loadEnchantments(enchantSection.getStringList("retrievable"), retrievableEnchantments);
            loadEnchantments(enchantSection.getStringList("blocked"), blockedEnchantments);
            loadEnchantments(enchantSection.getStringList("retrieved"), retrievedEnchantments);
        }
        
        // Charger les item flags
        itemFlags = rewardItemSection.getStringList("item-flags");
    }
    
    /**
     * Charge les enchantments d'une liste de strings (format: "ENCHANT:LEVEL")
     */
    private void loadEnchantments(List<String> enchantList, Map<String, Integer> map) {
        for (String enchantStr : enchantList) {
            if (enchantStr.contains(":")) {
                String[] parts = enchantStr.split(":");
                try {
                    int level = Integer.parseInt(parts[1]);
                    map.put(parts[0], level);
                } catch (NumberFormatException ignored) {}
            }
        }
    }
    
    /**
     * Parse un Material de manière sûre
     */
    private Material parseMaterial(String materialName, Material fallback) {
        try {
            return Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Material invalide: " + materialName + ", utilisation du fallback: " + fallback);
            return fallback;
        }
    }
    
    /**
     * Obtient l'ID Nexo pour un état de récompense
     */
    public String getNexoIdForState(RewardState state) {
        return switch (state) {
            case RETRIEVABLE -> retrievableNexoId;
            case BLOCKED -> blockedNexoId;
            case RETRIEVED -> retrievedNexoId;
        };
    }
    
    /**
     * Obtient le Material fallback pour un état
     */
    public Material getMaterialForState(RewardState state) {
        return switch (state) {
            case RETRIEVABLE -> retrievableMaterial;
            case BLOCKED -> blockedMaterial;
            case RETRIEVED -> retrievedMaterial;
        };
    }
    
    /**
     * Obtient le display name pour un état
     */
    public String getDisplayNameForState(RewardState state) {
        return switch (state) {
            case RETRIEVABLE -> retrievableDisplayName;
            case BLOCKED -> blockedDisplayName;
            case RETRIEVED -> retrievedDisplayName;
        };
    }
    
    /**
     * Obtient la lore pour un état
     */
    public List<String> getLoreForState(RewardState state) {
        return switch (state) {
            case RETRIEVABLE -> retrievableLore;
            case BLOCKED -> blockedLore;
            case RETRIEVED -> retrievedLore;
        };
    }
    
    /**
     * Obtient les enchantments pour un état
     */
    public Map<String, Integer> getEnchantementsForState(RewardState state) {
        return switch (state) {
            case RETRIEVABLE -> retrievableEnchantments;
            case BLOCKED -> blockedEnchantments;
            case RETRIEVED -> retrievedEnchantments;
        };
    }
    
    // Getters
    public String getRetrievableNexoId() { return retrievableNexoId; }
    public String getBlockedNexoId() { return blockedNexoId; }
    public String getRetrievedNexoId() { return retrievedNexoId; }
    
    public Material getRetrievableMaterial() { return retrievableMaterial; }
    public Material getBlockedMaterial() { return blockedMaterial; }
    public Material getRetrievedMaterial() { return retrievedMaterial; }
    
    public List<String> getItemFlags() { return itemFlags; }
    
    /**
     * États possibles d'une récompense
     */
    public enum RewardState {
        RETRIEVABLE,  // La récompense peut être retirée
        BLOCKED,      // La récompense est bloquée
        RETRIEVED     // La récompense a été retirée
    }
}