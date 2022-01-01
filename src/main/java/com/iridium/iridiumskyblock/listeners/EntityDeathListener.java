package com.iridium.iridiumskyblock.listeners;

import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.api.IridiumSkyblockAPI;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.database.IslandBooster;
import com.iridium.iridiumskyblock.database.IslandUpgrade;
import com.iridium.iridiumskyblock.database.User;
import gyurix.outpost.OutpostAPI;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EntityDeathListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void monitorEntityDeath(EntityDeathEvent event) {
        if(event.getEntityType() == EntityType.PLAYER) return;
        if (!IridiumSkyblockAPI.getInstance().isIslandWorld(event.getEntity().getWorld())) return;

        Player player = event.getEntity().getKiller();
        if (player == null) return;

        User user = IridiumSkyblock.getInstance().getUserManager().getUser(player);
        Optional<Island> island = user.getIsland();
        island.ifPresent(value -> {
            IridiumSkyblock.getInstance().getMissionManager().handleMissionUpdates(value, "KILL", event.getEntityType().name(), 1);

            IslandBooster islandBooster = IridiumSkyblock.getInstance().getIslandManager().getIslandBooster(island.get(), "experience");
            if (islandBooster.isActive()) {
                event.setDroppedExp(event.getDroppedExp() * 2);
            }

            //mob drop multiplier in js script
        });
    }

}
