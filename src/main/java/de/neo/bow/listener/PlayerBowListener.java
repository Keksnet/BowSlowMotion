package de.neo.bow.listener;

import de.neo.bow.BowMotionMain;
import de.neo.bow.obj.BowPlayer;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.UUID;

public class PlayerBowListener implements Listener {

    private final HashMap<UUID, BowPlayer> player;

    public PlayerBowListener() {
        this.player = new HashMap<>();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        BowPlayer bowPlayer = getBowPlayer(p);
        if(p.hasPermission("bow.slowmotion.use")) {
            Location loc = p.getLocation();
            loc.setY(loc.getY() - 1.0);
            if(loc.getBlock().getType().isSolid()) return;
            if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if(bowPlayer.validateSlowMotion()) {
                    bowPlayer.activateNcpIgnore();
                    bowPlayer.clearCounter();
                    bowPlayer.setSlowMotion(true);
                    bowPlayer.setSlowMotionVelocity();
                }
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        BowMotionMain main = BowMotionMain.getInstance();
        Player p = e.getPlayer();
        BowPlayer bowPlayer = getBowPlayer(p);
        if(bowPlayer.isInSlowMotion() && bowPlayer.validateSlowMotion()) {
            if(p.getFoodLevel() <= main.getConfig().getInt("options.min_food_level")) {
                bowPlayer.setSlowMotion(false);
                bowPlayer.clearCounter();
                bowPlayer.deactivateNcpIgnore();
                return;
            }
            bowPlayer.setSlowMotionVelocity();
            bowPlayer.tickFood();
            if(bowPlayer.isOnGround()) {
                bowPlayer.setSlowMotion(false);
                bowPlayer.clearCounter();
                bowPlayer.deactivateNcpIgnore();
                bowPlayer.deactivateFalling();
            }
        }else if(bowPlayer.isFalling()) {
            if(bowPlayer.isOnGround()) {
                bowPlayer.deactivateFalling();
            }
        }
    }

    @EventHandler
    public void onShot(EntityShootBowEvent e) {
        if(e.getEntityType() == EntityType.PLAYER) {
            Player p = (Player) e.getEntity();
            BowPlayer bowPlayer = getBowPlayer(p);
            bowPlayer.setSlowMotion(false);
            bowPlayer.clearCounter();
            bowPlayer.deactivateNcpIgnore();
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e) {
        BowMotionMain main = BowMotionMain.getInstance();
        if(e.getEntity() instanceof Player p) {
            BowPlayer bowPlayer = getBowPlayer(p);
            if(e.getCause() == EntityDamageEvent.DamageCause.FALL) {
                if(bowPlayer.isFalling()) {
                    if(p.hasPermission("bow.falldamage.ignore")) {
                        e.setCancelled(true);
                    }else {
                        double multiplier = main.getConfig().getDouble("options.damage_multiplier");
                        e.setDamage(e.getDamage() * multiplier);
                    }
                }
                bowPlayer.deactivateFalling();
            }
        }
    }

    private BowPlayer getBowPlayer(Player p) {
        BowPlayer bowPlayer = this.player.get(p.getUniqueId());
        if(bowPlayer == null) {
            bowPlayer = new BowPlayer(p.getUniqueId());
            this.player.put(p.getUniqueId(), bowPlayer);
        }
        return bowPlayer;
    }

}
