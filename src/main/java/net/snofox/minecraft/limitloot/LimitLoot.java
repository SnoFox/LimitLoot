package net.snofox.minecraft.limitloot;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

public final class LimitLoot extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new LootListener(this), this);
        getLogger().info("Loot is now limited");
    }

    public boolean shouldAllowDrops(final Location location) {
        final int x = Math.abs(location.getBlockX());
        final int z = Math.abs(location.getBlockZ());
        final int radius = getConfig().getInt("radius", 1000);
        return x < radius && z < radius;
    }

    public Material getReplacement(final Block block) {
        final String replacementStr = getConfig().getString("ores." + block.getType());
        if(replacementStr == null) return null;
        return Material.getMaterial(replacementStr);
    }
}
