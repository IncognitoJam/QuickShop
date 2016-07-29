package com.pb.spigot.quickshop.watcher;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by camer on 23/07/2016.
 */
public class Java {

private ArrayList<UUID> redTeam = new ArrayList<>();
private ArrayList<UUID> blueTeam = new ArrayList<>();

/**
 * Add a player to the red team array
 * @param player the player to add to the team
 */
private void addPlayerToRed(Player player) {
    redTeam.add(player.getUniqueId());
}

/**
 * Add a player to the blue team array
 * @param player the player to add to the team
 */
private void addPlayerToBlue(Player player) {
    blueTeam.add(player.getUniqueId());
}

@EventHandler
public void onPlayerAttackEntityEvent(EntityDamageByEntityEvent e) {

    // This is an event where two players are attacking each other
    if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {

        if (redTeam.contains(e.getDamager().getUniqueId()) && redTeam.contains(e.getEntity().getUniqueId())) {
            // Both players are on the red team
            e.setCancelled(true);
        } else if (blueTeam.contains(e.getDamager().getUniqueId()) && blueTeam.contains(e.getEntity().getUniqueId())) {
            // Both players are on the blue team
            e.setCancelled(true);
        }

    }
}

}
