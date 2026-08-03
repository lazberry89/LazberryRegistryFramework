package Framework.Utils;

import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.UUID;

public class EffectListener implements Listener {

    public EffectListener() {}

    @EventHandler
    public void EntityStunListener(EntityMoveEvent e) {
        LivingEntity le = e.getEntity();
        UUID uuid = le.getUniqueId();
        if (StunUtils.isStunned(uuid)) {
            e.setCancelled(true);
            le.sendActionBar(StunUtils.reasonIndicator(uuid, Lang.KOREAN));
            //TODO Roped effect
        }
    }

    @EventHandler
    public void PlayerStunListener(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();

        if (StunUtils.isStunned(uuid)) {
            Location from = e.getFrom();
            Location to = e.getTo();

            if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
                Location newTo = from.clone();
                newTo.setYaw(to.getYaw());
                newTo.setPitch(to.getPitch());
                e.setTo(newTo);
            }

            p.sendActionBar(StunUtils.reasonIndicator(uuid, Lang.KOREAN));
            //TODO Roped effect
        }
    }

	@EventHandler
	public void deStunWhenDead(EntityDeathEvent e) {
		LivingEntity victim = e.getEntity();
        var uuid = victim.getUniqueId();
		if (StunUtils.isStunned(uuid)) StunUtils.release(uuid);
	}

	@EventHandler
	public void deStunWhenPlayerDead(PlayerDeathEvent e) {
        Player victim = e.getPlayer();
        var uuid = victim.getUniqueId();
        if (StunUtils.isStunned(victim.getUniqueId())) StunUtils.release(uuid);
	}
}