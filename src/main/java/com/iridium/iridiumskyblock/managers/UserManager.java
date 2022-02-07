package com.iridium.iridiumskyblock.managers;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.database.User;
import com.iridium.iridiumskyblock.utils.BlockPosition;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Class which handles users.
 */
public class UserManager {

    private static LoadingCache<UUID, Optional<User>> userCache = CacheBuilder.newBuilder().expireAfterAccess(15, TimeUnit.MINUTES).build(
            new CacheLoader<>() {
                @Override
                public Optional<User> load(@NotNull UUID uuid) {
                    return IridiumSkyblock.getInstance().getDatabaseManager().getUserTableManager().getUser(uuid);
                }
            });

    /**
     * Gets a {@link User}'s info. Creates one if he doesn't exist.
     *
     * @param offlinePlayer The player who's data should be fetched
     * @return The user data
     */
    public @NotNull User getUser(@NotNull OfflinePlayer offlinePlayer) {
        Optional<User> userOptional = getUserByUUID(offlinePlayer.getUniqueId());
        if (userOptional.isPresent()) {
            return userOptional.get();
        } else {
            Optional<String> name = Optional.ofNullable(offlinePlayer.getName());
            User user = new User(offlinePlayer.getUniqueId(), name.orElse(""));
            IridiumSkyblock.getInstance().getDatabaseManager().getUserTableManager().addEntry(user);
            return user;
        }
    }

    /**
     * Finds an User by his {@link UUID}.
     *
     * @param uuid The uuid of the onlyForPlayers
     * @return the User class of the onlyForPlayers
     */
    public Optional<User> getUserByUUID(@NotNull UUID uuid) {
        return IridiumSkyblock.getInstance().getDatabaseManager().getUserTableManager().getUser(uuid);
    }

}
