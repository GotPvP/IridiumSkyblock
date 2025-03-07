package com.iridium.iridiumskyblock.listeners;

import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.SettingType;
import com.iridium.iridiumskyblock.database.IslandSetting;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class EntitySpawnListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        IridiumSkyblock.getInstance().getIslandManager().getIslandViaLocation(event.getLocation()).ifPresent(island -> {
            IslandSetting mobSpawnSetting = IridiumSkyblock.getInstance().getIslandManager().getIslandSetting(island, SettingType.MOB_SPAWN);
            boolean is_living = event.getEntity() instanceof LivingEntity && event.getEntityType() != EntityType.ARMOR_STAND;
            if (is_living) {
                if (!mobSpawnSetting.getBooleanValue()) {
                    event.setCancelled(true);
                    return;
                }

                // ? why is living not checked
                event.getEntity().setMetadata("island_spawned", new FixedMetadataValue(IridiumSkyblock.getInstance(), island.getId()));
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (IridiumSkyblock.getInstance().getIslandManager().isIslandEnd(event.getEntity().getWorld())) {
            if (event.getEntityType().equals(EntityType.ENDER_DRAGON) && event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.DEFAULT)) {
                event.setCancelled(true);
            }
        }
    }

}
