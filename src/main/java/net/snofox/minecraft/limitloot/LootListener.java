package net.snofox.minecraft.limitloot;

import net.snofox.minecraft.snolib.numbers.RandomUtil;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.Lootable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by Josh on 2020-02-06
 */
public class LootListener implements Listener {

    private final LimitLoot module;

    public LootListener(final LimitLoot limitLoot) {
        this.module = limitLoot;
    }

    @EventHandler(ignoreCancelled = true)
    public void onVehicleDestroy(final VehicleDestroyEvent ev) {
        final Vehicle vehicle = ev.getVehicle();
        if(module.shouldAllowDrops(vehicle.getLocation())) return;
        if(!(vehicle instanceof Lootable)) return;
        if(((Lootable) vehicle).getLootTable() == null) return;
        ((Lootable) vehicle).setLootTable(null);
        if(vehicle instanceof InventoryHolder) fillLoot(((InventoryHolder) vehicle).getInventory());
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDeath(final EntityDeathEvent ev) {
        if(module.shouldAllowDrops(ev.getEntity().getLocation())) return;
        if(ev.getEntity() instanceof Lootable)  ((Lootable) ev.getEntity()).setLootTable(null);
        ev.setDroppedExp(0);
        ev.getDrops().clear();
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityBreed(final EntityBreedEvent ev) {
        if(module.shouldAllowDrops(ev.getMother().getLocation())) return;
        ev.setExperience(0);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent ev) {
        if(module.shouldAllowDrops(ev.getBlock().getLocation())) return;
        final Material replacementMat = module.getReplacement(ev.getBlock());
        if(replacementMat != null) {
            ev.setExpToDrop(0);
            ev.setDropItems(false);
            ev.getBlock().getWorld().dropItemNaturally(ev.getBlock().getLocation(), new ItemStack(replacementMat, 1));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(final EntityExplodeEvent ev) {
        for(final Block block : ev.blockList()) {
            final BlockState state = block.getState();
            if(!module.shouldAllowDrops(block.getLocation()) && state instanceof Lootable) handleBlock(state);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(final BlockExplodeEvent ev) {
        for(final Block block : ev.blockList()) {
            final BlockState state = block.getState();
            if(!module.shouldAllowDrops(block.getLocation()) && state instanceof Lootable) handleBlock(state);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(final PlayerInteractEvent ev) {
        if(ev.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) return;
        if(ev.getClickedBlock() == null) return;
        if(module.shouldAllowDrops(ev.getClickedBlock().getLocation())) return;
        final BlockState state = ev.getClickedBlock().getState();
        handleBlock(state);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteractEntity(final PlayerInteractEntityEvent ev) {
        if(ev.getPlayer().getGameMode().equals(GameMode.SPECTATOR)) return;
        if(module.shouldAllowDrops(ev.getRightClicked().getLocation())) return;
        if(ev.getRightClicked() instanceof Lootable) {
            Lootable lootable = (Lootable)ev.getRightClicked();
            if(lootable.getLootTable() == null) return;
            lootable.setLootTable(null);
        }
    }

    private void handleBlock(final BlockState state) {
        if(!(state instanceof Lootable)) return;
        if(((Lootable) state).getLootTable() == null) return;
        ((Lootable)state).setLootTable(null);
        if(state instanceof Container) {
            fillLoot(((Container) state).getSnapshotInventory());
        }
        state.update();
    }

    private void fillLoot(final Inventory inventory) {
        final int invSize = inventory.getStorageContents().length;
        final ArrayList<ItemStack> shuffledInv = new ArrayList<>(invSize);
        final Random random = RandomUtil.getRandom();
        final int numItems = Math.max(1, random.nextInt(invSize));
        for(int x = 0; x < numItems; ++x)
            shuffledInv.add(new ItemStack(Material.COBBLESTONE, Math.max(1, random.nextInt(32))));
        for(int x = shuffledInv.size(); x < invSize; ++x) shuffledInv.add(null);
        Collections.shuffle(shuffledInv);
        ItemStack[] invArray = new ItemStack[invSize];
        invArray = shuffledInv.toArray(invArray);
        inventory.setStorageContents(invArray);
    }
}
