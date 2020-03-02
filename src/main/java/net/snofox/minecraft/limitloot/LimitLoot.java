package net.snofox.minecraft.limitloot;

import me.crispy1989.CraftRegions.CraftRegions;
import me.crispy1989.CraftRegions.CuboidRegion;
import me.crispy1989.CraftRegions.Region;
import me.crispy1989.CraftRegions.RegionManager;
import me.crispy1989.CraftRegions.RegionProperties;
import me.crispy1989.CraftRegions.RegionProperty;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class LimitLoot extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new LootListener(this), this);
        initCraftRegions();
        getLogger().info("Loot is now limited");
    }

    private void initCraftRegions() {
        final Plugin craftRegions = getServer().getPluginManager().getPlugin("CraftRegions");
        if(craftRegions == null) {
            getLogger().info("CraftRegions not found. No message will appear when passing the border");
            return;
        }
        final RegionManager regionManager = ((CraftRegions)craftRegions).getManager();
        for(final World world : getServer().getWorlds()) initWorldRegion(world, regionManager);

    }

    private void initWorldRegion(final World world, final RegionManager regionManager) {
        int radius = getConfig().getInt("radius");
        switch(world.getEnvironment()) {
            case NETHER:
                radius = radius/8;
            case THE_END:
                radius = getConfig().getInt("endRadius", radius);
        }
        final CuboidRegion cuboid = new CuboidRegion(-radius, 0, -radius,
                radius, world.getMaxHeight(), radius);
        try {
            final Region region = regionManager.addRegion("limitloot_" + world.getName(), world.getName(), false, cuboid);
            final RegionProperties regionProps = region.properties();
            final RegionProperty enterMessage = new RegionProperty(getConfig().getString("exitMessage"), false);
            regionProps.set("exitMessage", enterMessage);
        } catch(Exception e) {
            getLogger().warning("Failed to add region for " + world.getName() + ": " + e.getMessage());
        }
    }

    public boolean shouldAllowDrops(final Location location) {
        final int x = Math.abs(location.getBlockX());
        final int z = Math.abs(location.getBlockZ());
        final int radius = getConfig().getInt("radius");
        return x <= radius && z <= radius;
    }

    public Material getReplacement(final Block block) {
        final String replacementStr = getConfig().getString("ores." + block.getType());
        if(replacementStr == null) return null;
        return Material.getMaterial(replacementStr);
    }
}
