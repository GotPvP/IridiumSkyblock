package com.iridium.iridiumskyblock.gui;

import com.iridium.iridiumcore.Item;
import com.iridium.iridiumcore.dependencies.xseries.XMaterial;
import com.iridium.iridiumcore.utils.InventoryUtils;
import com.iridium.iridiumcore.utils.ItemStackUtils;
import com.iridium.iridiumcore.utils.Placeholder;
import com.iridium.iridiumcore.utils.StringUtils;
import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.bank.BankItem;
import com.iridium.iridiumskyblock.bank.MobCoinsBankItem;
import com.iridium.iridiumskyblock.configs.Configuration;
import com.iridium.iridiumskyblock.configs.Schematics;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.database.IslandBank;
import com.iridium.iridiumskyblock.database.User;
import com.iridium.iridiumskyblock.managers.CooldownProvider;
import com.iridium.iridiumskyblock.utils.PlayerUtils;
import com.opblocks.utils.SignContainer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * GUI to confirm the regen of an island
 */
public class IslandRegenConfirmGUI extends IslandGUI {

    private final Map.Entry<String, Schematics.SchematicConfig> schematicConfig;
    private final CooldownProvider<CommandSender> cooldownProvider;

    /**
     * The default constructor.
     *
     * @param island The Island this GUI belongs to
     */
    public IslandRegenConfirmGUI(@NotNull Island island, Inventory previousInventory, Map.Entry<String, Schematics.SchematicConfig> schematicConfig, CooldownProvider<CommandSender> cooldownProvider) {
        super(IridiumSkyblock.getInstance().getInventories().islandRegenConfirmGUI, previousInventory, island);
        this.schematicConfig = schematicConfig;
        this.cooldownProvider = cooldownProvider;
    }

    @Override
    public void addContent(Inventory inventory) {
        inventory.clear();
        InventoryUtils.fillInventory(inventory, getNoItemGUI().background);

        inventory.setItem(12, ItemStackUtils.makeItem(new Item(XMaterial.LIME_TERRACOTTA, 0, 1, "&a&lConfirm", List.of("", "&c&lWarning: &7This will wipe your island!", "&7you will lose all your progress!"))));
        inventory.setItem(14, ItemStackUtils.makeItem(new Item(XMaterial.RED_TERRACOTTA, 0, 1, "&c&lDeny", Collections.emptyList())));

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
        Player player = (Player) event.getWhoClicked();

        if (event.getSlot() == 12) {
            User user = IridiumSkyblock.getInstance().getUserManager().getUser(player);
            Optional<Island> island = user.getIsland();
            Configuration.IslandRegenSettings regenSettings = IridiumSkyblock.getInstance().getConfiguration().regenSettings;
            if (PlayerUtils.pay(player, island.get(), regenSettings.crystalPrice, regenSettings.moneyPrice, regenSettings.mobcoinPrice)) {
                player.sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getMessages().regeneratingIsland.replace("%prefix%", IridiumSkyblock.getInstance().getConfiguration().prefix)));
                IridiumSkyblock.getInstance().getIslandManager().regenerateIsland(island.get(), user, schematicConfig.getValue());
                cooldownProvider.applyCooldown(player);
                player.closeInventory();
            } else {
                player.sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getMessages().cannotAfford.replace("%prefix%", IridiumSkyblock.getInstance().getConfiguration().prefix)));
            }
        } else if (event.getSlot() == 14) {
            player.closeInventory();
        }
    }
}
