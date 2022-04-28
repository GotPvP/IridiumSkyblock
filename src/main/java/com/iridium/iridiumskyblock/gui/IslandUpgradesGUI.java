package com.iridium.iridiumskyblock.gui;

import com.iridium.iridiumcore.Item;
import com.iridium.iridiumcore.utils.InventoryUtils;
import com.iridium.iridiumcore.utils.ItemStackUtils;
import com.iridium.iridiumcore.utils.Placeholder;
import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.Upgrade;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.upgrades.OresUpgrade;
import com.iridium.iridiumskyblock.upgrades.UpgradeData;
import org.apache.commons.lang.StringUtils;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * GUI which allows users to manage the Island Upgrades.
 */
public class IslandUpgradesGUI extends IslandGUI {

    /**
     * The default constructor.
     *
     * @param island The Island this GUI belongs to
     */
    public IslandUpgradesGUI(@NotNull Island island, Inventory previousInventory) {
        super(IridiumSkyblock.getInstance().getInventories().upgradesGUI, previousInventory, island);
    }

    @Override
    public void addContent(Inventory inventory) {
        inventory.clear();
        InventoryUtils.fillInventory(inventory, IridiumSkyblock.getInstance().getInventories().upgradesGUI.background);

        for (Map.Entry<String, Upgrade<?>> upgrade : IridiumSkyblock.getInstance().getUpgradesList().entrySet()) {
            Item item = upgrade.getValue().item;
            int level = IridiumSkyblock.getInstance().getIslandManager().getIslandUpgrade(getIsland(), upgrade.getKey()).getLevel();
            List<Placeholder> placeholderList = new ArrayList<>();
            placeholderList.add(new Placeholder("level", String.valueOf(level)));

            if (upgrade.getValue().upgrades.get(level) != null) {
                UpgradeData upgradeData = upgrade.getValue().upgrades.get(level);
                placeholderList.addAll(upgradeData.getPlaceholders());
            }
            if (upgrade.getValue().upgrades.get(level + 1) != null) {
                UpgradeData upgradeData = upgrade.getValue().upgrades.get(level + 1);
                placeholderList.add(new Placeholder("crystalscost", String.valueOf(upgradeData.crystals)));
                placeholderList.add(new Placeholder("vaultcost", String.valueOf(upgradeData.money)));
                placeholderList.add(new Placeholder("mobcoinscost", String.valueOf(upgradeData.mobcoins)));
            } else if (!upgrade.getValue().upgrades.containsKey(level + 1)) {
                placeholderList.add(new Placeholder("crystalscost", IridiumSkyblock.getInstance().getPlaceholders().crystalCost));
                placeholderList.add(new Placeholder("vaultcost", IridiumSkyblock.getInstance().getPlaceholders().vaultCost));
                placeholderList.add(new Placeholder("mobcoinscost", IridiumSkyblock.getInstance().getPlaceholders().mobcoinsCost));
            }

            if(upgrade.getKey().equals("generator")) {
                List<String> lore = new ArrayList<>(item.lore);
                OresUpgrade oresUpgrade = (OresUpgrade) upgrade.getValue().upgrades.get(level);
                if(upgrade.getValue().upgrades.containsKey(level + 1)) {
                    OresUpgrade nextOresUpgrade = (OresUpgrade) upgrade.getValue().upgrades.get(level + 1);
                    nextOresUpgrade.ores.forEach((material, chance) -> {
                        String materialName = StringUtils.capitaliseAllWords(material.name().toLowerCase().replace("_", " "));
                        int previousChance = oresUpgrade.ores.getOrDefault(material, 0);
                        lore.add(lore.size() - 2, "&b&l • &7" + materialName + " &b" + previousChance + "% &7-> &b" + chance + "%");
                    });
                } else {
                    oresUpgrade.ores.forEach((material, chance) -> {
                        String materialName = StringUtils.capitaliseAllWords(material.name().toLowerCase().replace("_", " "));
                        lore.add(lore.size() - 2, "&b&l • &7" + materialName + " &b" + chance + "%");
                    });
                }
                Item clone = new Item(item.material, item.amount, item.displayName, lore);
                inventory.setItem(item.slot, ItemStackUtils.makeItem(clone, placeholderList));
            } else {
                inventory.setItem(item.slot, ItemStackUtils.makeItem(item, placeholderList));
            }
        }

        if (IridiumSkyblock.getInstance().getConfiguration().backButtons && getPreviousInventory() != null) {
            inventory.setItem(inventory.getSize() + IridiumSkyblock.getInstance().getInventories().backButton.slot, ItemStackUtils.makeItem(IridiumSkyblock.getInstance().getInventories().backButton));
        }
    }

    /**
     * Called when there is a click in this GUI.
     * Cancelled automatically.
     *
     * @param event The InventoryClickEvent provided by Bukkit
     */
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        for (Map.Entry<String, Upgrade<?>> upgrade : IridiumSkyblock.getInstance().getUpgradesList().entrySet()) {
            if (event.getSlot() == upgrade.getValue().item.slot) {
                IridiumSkyblock.getInstance().getCommands().upgradesCommand.execute(event.getWhoClicked(), new String[]{"", upgrade.getKey()});
                addContent(event.getInventory());
            }
        }
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event) {

    }
}
