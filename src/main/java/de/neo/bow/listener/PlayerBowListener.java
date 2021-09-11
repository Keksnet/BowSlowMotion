package de.neo.bow.listener;

import de.neo.bow.BowMotionMain;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlayerBowListener implements Listener {

    private HashSet<UUID> slowMotion;
    private HashMap<UUID, Integer> counter;

    public PlayerBowListener() {
        this.slowMotion = new HashSet<>();
        this.counter = new HashMap<>();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        BowMotionMain main = BowMotionMain.getInstance();
        Player p = e.getPlayer();
        Location loc = p.getLocation();
        loc.setY(loc.getY() - 1.0);
        if(loc.getBlock().getType().isSolid()) return;
        if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if(p.getInventory().getItemInMainHand().getType() == Material.BOW) {
                if(containsArrow(p.getInventory())) {
                    if(p.getFoodLevel() > main.getConfig().getInt("options.min_food_level")) {
                        Vector vel = p.getVelocity();
                        calculateVelocity(vel);
                        p.setVelocity(vel);
                        this.slowMotion.add(p.getUniqueId());
                        this.counter.put(p.getUniqueId(), 0);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        BowMotionMain main = BowMotionMain.getInstance();
        Player p = e.getPlayer();
        if(this.slowMotion.contains(e.getPlayer().getUniqueId()) && p.getInventory().getItemInMainHand().getType() == Material.BOW) {
            if(p.getFoodLevel() <= main.getConfig().getInt("options.min_food_level")) {
                this.slowMotion.remove(p.getUniqueId());
                return;
            }
            Vector vel = p.getVelocity();
            calculateVelocity(vel);
            p.setVelocity(vel);
            if(this.counter.get(p.getUniqueId()) >= main.getConfig().getInt("options.food_decrease_rate")) {
                p.setFoodLevel(p.getFoodLevel() - 1);
                this.counter.put(p.getUniqueId(), 0);
            }else {
                this.counter.put(p.getUniqueId(), this.counter.get(p.getUniqueId()) + 1);
            }
            Location loc = p.getLocation();
            loc.setY(loc.getY() - 1.0);
            if(loc.getBlock().getType().isSolid()) {
                this.slowMotion.remove(p.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onShot(EntityShootBowEvent e) {
        if(e.getEntityType() == EntityType.PLAYER) {
            this.slowMotion.remove(e.getEntity().getUniqueId());
        }
    }

    private void calculateVelocity(Vector vel) {
        BowMotionMain main = BowMotionMain.getInstance();
        vel.setY(-main.getConfig().getDouble("options.fall_speed"));
    }

    private boolean containsArrow(Inventory inv) {
        AtomicBoolean flag = new AtomicBoolean(false);
        BowMotionMain.arrows.stream().forEach(arrowType -> {
            if(flag.get()) return;
            flag.set(inv.contains(arrowType));
        });
        return flag.get();
    }
}
