package com.iridium.iridiumskyblock.commands;

import com.iridium.iridiumcore.utils.StringUtils;
import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.LogAction;
import com.iridium.iridiumskyblock.PermissionType;
import com.iridium.iridiumskyblock.api.IridiumSkyblockAPI;
import com.iridium.iridiumskyblock.api.UserKickEvent;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.database.IslandLog;
import com.iridium.iridiumskyblock.database.User;
import com.iridium.iridiumskyblock.utils.PlayerUtils;
import com.opblocks.overflowbackpacks.OverflowAPI;
import com.opblocks.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AdminCommand extends Command {

    public AdminCommand() {
        super(Collections.singletonList("admin"), "Commands for admins", "iridiumskyblock.admin", false, Duration.ZERO);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if(sender.isOp()) {
            if(args.length > 1) {
                if(args[1].equalsIgnoreCase("help")) {
                    sendHelp(sender);
                } else if(args[1].equalsIgnoreCase("size")) {
                    if(args.length > 3) {
                        Player target = Bukkit.getPlayer(args[2]);
                        if(target != null) {
                            int size = Integer.parseInt(args[3]);
                            Optional<Island> island = IridiumSkyblockAPI.getInstance().getUser(target).getIsland();
                            island.ifPresent(value -> {
                                value.setSizeAddon(size);
                                sender.sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getConfiguration().prefix + " &b" + target.getName() + "'s &7island size is now set to " + value.getSizeAddon() + " &c(Not including upgrades)"));
                                value.getMembers().stream().map(islandUser -> Bukkit.getPlayer(islandUser.getUuid())).filter(Objects::nonNull)
                                        .forEach(islandPlayer -> islandPlayer.sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getConfiguration().prefix + " &7Your island's size has been changed to &b" + value.getSizeAddon() + "&7! &c(Not including upgrades)")));
                            });
                            return true;
                        }
                        sender.sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getConfiguration().prefix + " &cInvalid player!"));
                        return true;
                    } else if(args.length > 2) {
                        Player target = Bukkit.getPlayer(args[2]);
                        if(target != null) {
                            Optional<Island> island = IridiumSkyblockAPI.getInstance().getUser(target).getIsland();
                            if (island.isPresent()) {
                                sender.sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getConfiguration().prefix + " &b" + target.getName() + "'s &7island size is set to " + island.get().getSizeAddon() + " &c(Not including upgrades)"));
                            } else {
                                sender.sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getConfiguration().prefix + " &cNo island found!"));
                            }
                            return true;
                        }
                        sender.sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getConfiguration().prefix + " &cInvalid player!"));
                        return true;
                    }
                    sender.sendMessage(StringUtils.color("&8=========== &b&lIsland Admin Help &r&8==========="));
                    sender.sendMessage(StringUtils.color("&b/is admin size <player>&f: &7See the size of a player's island."));
                    sender.sendMessage(StringUtils.color("&b/is admin size <player> <amount>&f: &7Set the size of a player's island. This size is on top of their upgrade"));
                    return true;
                } else if(args[1].equalsIgnoreCase("mythicalchest")) {
                    Player target = Bukkit.getPlayer(args[2]);
                    int level = 1;
                    if(args.length > 3) {
                        level = Integer.parseInt(args[3]);
                    }
                    if(target != null) {
                        OverflowAPI.add(target, ItemBuilder.of(Material.ENDER_CHEST).name("&d&lMythical Chest").lore("", " &d• &fLevel &d" + level, "", "&7A mysterious chest that adds", "&7island levels based on how", "&7many blocks you put into it!").persistent(IridiumSkyblockAPI.getInstance().getMythicalChestKey(), PersistentDataType.INTEGER, level).build());
                        return true;
                    }
                    sender.sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getConfiguration().prefix + " &cInvalid player!"));
                    return true;
                } else if(args[1].equalsIgnoreCase("recalculate")) {
                    if(sender instanceof Player player) {
                        IridiumSkyblockAPI.getInstance().getIslandViaLocation(player.getLocation()).ifPresent(island -> {
                            IridiumSkyblock.getInstance().getIslandManager().recalculateIsland(island);
                            sender.sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getConfiguration().prefix + " &7Island Recalculated!"));
                        });
                    }
                    return true;
                } else if(args[1].equalsIgnoreCase("kick")) {
                    if(sender instanceof Player player) {
                        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(args[2]);

                        User user = IridiumSkyblock.getInstance().getUserManager().getUser(player);
                        User targetUser = IridiumSkyblock.getInstance().getUserManager().getUser(targetPlayer);
                        Island island = targetUser.getIsland().get();

                        UserKickEvent userKickEvent = new UserKickEvent(island, targetUser, user);
                        Bukkit.getPluginManager().callEvent(userKickEvent);
                        if (userKickEvent.isCancelled()) return false;

                        if (targetPlayer instanceof Player) {
                            ((Player) targetPlayer).sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getMessages().youHaveBeenKicked.replace("%player%", player.getName()).replace("%prefix%", IridiumSkyblock.getInstance().getConfiguration().prefix)));
                            PlayerUtils.teleportSpawn((Player) targetPlayer);
                        }

                        targetUser.setIsland(null);

                        // Send a message to all other members
                        for (User member : island.getMembers()) {
                            Player islandMember = Bukkit.getPlayer(member.getUuid());
                            if (islandMember != null) {
                                if (!islandMember.equals(player)) {
                                    islandMember.sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getMessages().kickedPlayer.replace("%kicker%", player.getName()).replace("%player%", targetUser.getName()).replace("%prefix%", IridiumSkyblock.getInstance().getConfiguration().prefix)));
                                } else {
                                    islandMember.sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getMessages().youKickedPlayer.replace("%player%", targetUser.getName()).replace("%prefix%", IridiumSkyblock.getInstance().getConfiguration().prefix)));
                                }
                            }
                        }

                        IslandLog islandLog = new IslandLog(island, LogAction.USER_KICKED, user, targetUser, 0, "");
                        IridiumSkyblock.getInstance().getDatabaseManager().getIslandLogTableManager().addEntry(islandLog);
                    }
                }
            } else {
                sendHelp(sender);
            }
        }

        return true;
    }

    public void sendHelp(CommandSender sender) {
        sender.sendMessage(StringUtils.color("&8=========== &b&lIsland Admin Help &r&8==========="));
        sender.sendMessage(StringUtils.color("&b/is admin help&f: &7Display admin commands"));
        sender.sendMessage(StringUtils.color("&b/is admin size&f: &7View/Change the size of an island"));
        sender.sendMessage(StringUtils.color("&b/is admin mythicalchest <player>&f: &7Give a mythical chest"));
        sender.sendMessage(StringUtils.color("&b/is admin recalculate &f: &7Recalculate the island you're on"));
        sender.sendMessage(StringUtils.color("&b/is admin kick &f: &7Kick a player"));
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, org.bukkit.command.Command command, String label, String[] args) {
        // We currently don't want to tab-completion here
        // Return a new List, so it isn't a list of online players
        return Collections.emptyList();
    }

}
