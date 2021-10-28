package de.neo.bow.obj;

import de.neo.bow.BowMotionMain;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class BowPlayer {

    private final UUID uuid;
    private boolean slowMotion;
    private int counter;
    private boolean falling;

    public BowPlayer(UUID uuid) {
        if(uuid == null) throw new IllegalArgumentException("uuid can not be null");
        this.uuid = uuid;
        this.slowMotion = false;
        this.counter = 0;
        this.falling = false;
    }

    public void activateNcpIgnore() {
        BowMotionMain main = BowMotionMain.getInstance();
        if(main.isNcpSupportDisabled()) return;
        NCPExemptionManager.exemptPermanently(this.getUUID(), CheckType.MOVING_SURVIVALFLY);
    }

    public void deactivateNcpIgnore() {
        BowMotionMain main = BowMotionMain.getInstance();
        if(main.isNcpSupportDisabled()) return;
        NCPExemptionManager.unexempt(this.getUUID(), CheckType.MOVING_SURVIVALFLY);
    }

    public boolean validateSlowMotion() {
        BowMotionMain main = BowMotionMain.getInstance();
        Player p = Bukkit.getPlayer(this.getUUID());
        if(p == null) throw new RuntimeException(this.getUUID() + " is offline");
        return p.getInventory().getItemInMainHand().getType() == Material.BOW
                && p.getFoodLevel() > main.getConfig().getInt("options.min_food_level") && this.hasArrow();
    }

    public void setSlowMotionVelocity() {
        Player p = Bukkit.getPlayer(this.getUUID());
        if(p == null) throw new RuntimeException(this.getUUID() + " is offline");
        p.setVelocity(calculateVelocity());
    }

    public void tickFood() {
        BowMotionMain main = BowMotionMain.getInstance();
        Player p = Bukkit.getPlayer(this.getUUID());
        if(p == null) throw new RuntimeException(this.getUUID() + " is offline");
        if(this.getCounter() >= main.getConfig().getInt("options.food_decrease_rate")) {
            p.setFoodLevel(p.getFoodLevel() - 1);
            this.clearCounter();
        }else {
            this.increaseCounter();
        }
    }

    public boolean isOnGround() {
        Player p = Bukkit.getPlayer(this.getUUID());
        if(p == null) throw new RuntimeException(this.getUUID() + " is offline");
        Location loc = p.getLocation();
        loc.setY(loc.getY() - 1.0);
        return loc.getBlock().getType().isSolid();
    }

    private Vector calculateVelocity() {
        BowMotionMain main = BowMotionMain.getInstance();
        Player p = Bukkit.getPlayer(this.getUUID());
        if(p == null) throw new RuntimeException(this.getUUID() + " is offline");
        Vector vel = p.getVelocity();
        vel.setY(-main.getConfig().getDouble("options.fall_speed"));
        return vel;
    }

    private boolean hasArrow() {
        Player p = Bukkit.getPlayer(this.getUUID());
        if(p == null) throw new RuntimeException(this.getUUID() + " is offline");
        AtomicBoolean flag = new AtomicBoolean(false);
        BowMotionMain.arrows.forEach(arrowType -> {
            if(flag.get()) return;
            flag.set(p.getInventory().contains(arrowType));
        });
        return flag.get();
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public void setSlowMotion(boolean slowMotion) {
        this.slowMotion = slowMotion;
        this.falling = !slowMotion;
    }

    public boolean isInSlowMotion() {
        return this.slowMotion;
    }

    public void increaseCounter() {
        this.counter++;
    }

    public void clearCounter() {
        this.counter = 0;
    }

    public int getCounter() {
        return this.counter;
    }

    public void deactivateFalling() {
        this.falling = false;
    }

    public boolean isFalling() {
        return this.falling;
    }
}
