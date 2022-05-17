package com.iridium.iridiumskyblock.gui;

import com.iridium.iridiumcore.dependencies.xseries.XMaterial;
import com.iridium.iridiumcore.utils.StringUtils;
import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.api.IridiumSkyblockAPI;
import com.iridium.iridiumskyblock.configs.BlockValues.ValuableBlock;
import com.opblocks.inventory.BaseContainer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.EnderChest;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

/**
 * GUI which shows the value of valuable blocks.
 *
 * @see ValuableBlock
 */
public class MythicalChestGUI extends BaseContainer {

    private final EnderChest mythicalChest;
    private final int storage;
    private boolean shouldSave = true;
    /**
     * The default constructor.
     *
     * @param mythicalChest the phsyical block
     */
    public MythicalChestGUI(EnderChest mythicalChest, Player player, int storage) {
        super(6, "Mythical Chest");
        this.mythicalChest = mythicalChest;
        this.storage = storage;

        if(IridiumSkyblockAPI.getInstance().getOpenMythicalChests().contains(getMythicalChestId())) {
            player.sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getConfiguration().prefix + " &cYou can't have multiple people opening the chest!"));
            shouldSave = false;
            Bukkit.getScheduler().runTaskLater(IridiumSkyblock.getInstance(), (@NotNull Runnable) player::closeInventory, 2);
            return;
        } else {
            IridiumSkyblockAPI.getInstance().getOpenMythicalChests().add(getMythicalChestId());
        }

        if(mythicalChest.getPersistentDataContainer().has(new NamespacedKey(IridiumSkyblock.getInstance(), "mythicalChestStorage-" + storage), PersistentDataType.STRING)) {
            IridiumSkyblockAPI.inventoryDeserializeAndApply(getInventory(), mythicalChest.getPersistentDataContainer().get(new NamespacedKey(IridiumSkyblock.getInstance(), "mythicalChestStorage-" + storage), PersistentDataType.STRING));
        }
    }


    @Override
    public void onClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getClickedInventory();
        if(inventory == event.getView().getBottomInventory()) {
            if (event.getCurrentItem() != null && !IridiumSkyblock.getInstance().getBlockValues().blockValues.containsKey(XMaterial.matchXMaterial(event.getCurrentItem().getType()))) {
                event.setCancelled(true);
                player.sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getConfiguration().prefix + " &7You can't put that block in there!"));
            }
        }
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        if(!shouldSave) return;

        mythicalChest.getPersistentDataContainer().set(new NamespacedKey(IridiumSkyblock.getInstance(), "mythicalChestStorage-" + storage), PersistentDataType.STRING, IridiumSkyblockAPI.inventorySerialize(getInventory()));
        int value = 0;
        for(ItemStack itemStack : getInventory().getContents()) {
            if(itemStack != null && IridiumSkyblock.getInstance().getBlockValues().blockValues.containsKey(XMaterial.matchXMaterial(itemStack.getType()))) {
                value += IridiumSkyblock.getInstance().getBlockValues().blockValues.get(XMaterial.matchXMaterial(itemStack.getType())).value * itemStack.getAmount();
            }
        }

        mythicalChest.getPersistentDataContainer().set(new NamespacedKey(IridiumSkyblock.getInstance(), "mythicalChestValue-" + storage), PersistentDataType.INTEGER, value);
        mythicalChest.update();
        IridiumSkyblockAPI.getInstance().getIslandViaLocation(mythicalChest.getLocation()).ifPresent(island -> {
            if(!IridiumSkyblock.getInstance().getIslandsToRecalulate().contains(island)) {
                IridiumSkyblock.getInstance().getIslandsToRecalulate().add(island);
            }
        });

        Bukkit.getScheduler().runTaskLater(IridiumSkyblock.getInstance(), () -> IridiumSkyblockAPI.getInstance().getOpenMythicalChests().remove(getMythicalChestId()), 20 * 2);
    }

    public EnderChest getMythicalChest() {
        return mythicalChest;
    }

    private String getMythicalChestId() {
        return mythicalChest.getLocation().toString() + "-" + storage;
    }
}
