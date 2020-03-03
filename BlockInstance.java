package vanillaamplified.combat;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BlockInstance {

	public long blockTime = 0;
	public int blockAmmount = 0;
	public Player blocker = null;
	
	public ItemStack blockerHolding = null;
	public Location blockerLocation = null;

	public BlockInstance(Player player) {
		blockTime = System.currentTimeMillis();
		blocker = player;
		
		if(player != null) {
			blockerHolding = player.getInventory().getItemInMainHand().clone();
			blockerLocation = player.getLocation().clone();
		}
	}
	
	public void update() {
		if(blocker != null) {
			blockerHolding = blocker.getInventory().getItemInMainHand().clone();
			blockerLocation = blocker.getLocation().clone();
		}
	}
	
}
