package com.iridium.iridiumskyblock.gui;

import com.iridium.iridiumcore.utils.InventoryUtils;
import com.iridium.iridiumcore.utils.StringUtils;
import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.api.IridiumSkyblockAPI;
import com.iridium.iridiumskyblock.bank.MobCoinsBankItem;
import com.iridium.iridiumskyblock.configs.BlockValues.ValuableBlock;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.database.IslandBank;
import com.iridium.iridiumskyblock.database.User;
import com.opblocks.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.block.EnderChest;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataType;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * GUI which shows the value of valuable blocks.
 *
 * @see ValuableBlock
 */
public class MythicalChestMainGUI extends GUI {

    private DecimalFormat DF = new DecimalFormat("###,###,###,###,###");
    private final EnderChest mythicalChest;
    private final List<Integer> slots = Arrays.asList(21, 22, 23, 30, 31, 32);
    /**
     * The default constructor.
     *
     * @param mythicalChest the phsyical block
     */
    public MythicalChestMainGUI(EnderChest mythicalChest) {
        super(IridiumSkyblock.getInstance().getInventories().mythicalChestGUI, null);
        this.mythicalChest = mythicalChest;
    }

    @Override
    public void addContent(Inventory inventory) {
        inventory.clear();

        InventoryUtils.fillInventory(inventory, getNoItemGUI().background);

        int level = mythicalChest.getPersistentDataContainer().getOrDefault(IridiumSkyblockAPI.getInstance().getMythicalChestKey(), PersistentDataType.INTEGER, 0);
        for(int i = 1; i <= 6; i++) {
            if(level >= i) {
                inventory.setItem(slots.get(i - 1), ItemBuilder.of(Material.ENDER_CHEST).name("&d&lMythical Chest &7(&dLevel " + (i) + "&7)").lore("", "&a&l(!) &aClick to open!").build());
            } else {
                inventory.setItem(slots.get(i - 1), ItemBuilder.of(Material.BARRIER).name("&c&lLocked").lore("", " &d• &fLevel &d" + (i), " &d• &fCost &e" + DF.format(getCost(i)) + " Mob Coins", "", (level + 1) == i ? "&a&l(!) &aClick to purchase upgrade!" : "&c&l(!) &cPurchase the previous upgrade first!").build());
            }
        }
    }

    private int getCost(int level) {
        return switch (level) {
            case 1 -> 150000;
            case 2 -> 250000;
            case 3 -> 400000;
            case 4 -> 600000;
            default -> 850000;
        };
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event) {

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
        if(slots.contains(event.getSlot())) {
            int level = mythicalChest.getPersistentDataContainer().getOrDefault(IridiumSkyblockAPI.getInstance().getMythicalChestKey(), PersistentDataType.INTEGER, 0);
            int clicked = slots.indexOf(event.getSlot()) + 1;

            if(level >= clicked) {
                player.openInventory(new MythicalChestGUI(mythicalChest, player, clicked).getInventory());
                return;
            } else if(level + 1 != clicked) {
                player.sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getConfiguration().prefix + " &cYou need to purchase the previous levels first!"));
                return;
            }

            int cost = getCost(clicked);
            MobCoinsBankItem mobcoinsBankItem = IridiumSkyblock.getInstance().getBankItems().mobcoinsBankItem;
            User user = IridiumSkyblock.getInstance().getUserManager().getUser(player);
            Optional<Island> island = user.getIsland();
            if (!island.isPresent()) {
                player.sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getMessages().noIsland.replace("%prefix%", IridiumSkyblock.getInstance().getConfiguration().prefix)));
                return;
            }

            IslandBank islandBank = IridiumSkyblock.getInstance().getIslandManager().getIslandBank(island.get(), mobcoinsBankItem);
            double mobcoins = islandBank.getNumber();
            if (mobcoins >= cost) {
                islandBank.setNumber(islandBank.getNumber() - cost);
                player.sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getConfiguration().prefix + " &7You successfully purchased &d&lMythical Chest &7(&dLevel " + clicked + "&7)"));
                mythicalChest.getPersistentDataContainer().set(IridiumSkyblockAPI.getInstance().getMythicalChestKey(), PersistentDataType.INTEGER, clicked);
                mythicalChest.update();
                player.openInventory(new MythicalChestMainGUI(mythicalChest).getInventory());
            } else {
                player.sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getConfiguration().prefix + " &7You don't have enough Mob Coins in your bank!"));
            }
        }
    }
}
