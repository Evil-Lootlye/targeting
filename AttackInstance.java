package vanillaamplified.combat;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;

public class AttackInstance {
	
	public long timeStarted = 0;
	public Player attacker = null;
	public LivingEntity attacked = null;
	
	public ItemStack attackerWeapon = null;
	public ItemStack attackerWeaponO = null;
	public Location attackerLocation = null;
	public Location attackerEyeLocation = null;
	
	public ItemStack attackedHolding = null;
	public Location attackedLocation = null;
	public Location attackedEyeLocation = null;
	
	EntityDamageByEntityEvent e1 = null;
	PlayerInteractEvent e2 = null;
	
	double damage = 0;
	// 1==head 2==torso 3==legs
	double heightHit = -1;
	int bodyPartHit = -1;
	BoundingBox bb = null;
	int clicks = 0;

	public AttackInstance(Player attack, LivingEntity block) {
		timeStarted = System.currentTimeMillis();
		attacker = attack;
		attacked = block;
		
		if(attack != null) {
			attackerWeapon = attack.getInventory().getItemInMainHand().clone();
			attackerWeaponO = attack.getInventory().getItemInMainHand();
			attackerLocation = attack.getLocation().clone();
			attackerEyeLocation = attack.getEyeLocation().clone();
		}
		if(block != null) {
			if(attacked instanceof Player) {
				attackedHolding = ((Player) block).getInventory().getItemInMainHand().clone();
			}
			attackedLocation = block.getLocation().clone();
			attackedEyeLocation = block.getEyeLocation().clone();
			bb = attacked.getBoundingBox().clone();
		}
	}

}
