package com.untamedears.realisticbiomes.growth;

import com.untamedears.realisticbiomes.model.Plant;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.util.RandomSourceWrapper;

/**
 * We need to differentiate fungus from other types of saplings thanks
 * to the peculiarities with how Spigot handles tree generation. If you
 * call {@code world.generateTree()} with the {@link TreeType}, it will
 * generate the tree as if it were populating a newly generated chunk.
 * Therefore we need to bypass that and pretend the sapling is being
 * bone mealed so ensure a singular tree is generated.
 */
public class FungusGrower extends AgeableGrower {

    private final RandomSource randomSource = new RandomSourceWrapper(new Random());

    public FungusGrower(final Material material) {
        super(material, 1, 1);
    }

    @Override
    public int getStage(final Plant plant) {
        final Block block = plant.getLocation().getBlock();
        if (block.getType() != this.material) {
            return -1;
        }
        return 0;
    }

    @Override
    public boolean setStage(final Plant plant, final int stage) {
        if (stage < 1) {
            return true;
        }
        final Block block = plant.getLocation().getBlock();
        final Material material = block.getType();
        final ResourceKey<ConfiguredFeature<?, ?>> growth =
            material == Material.CRIMSON_FUNGUS ? TreeFeatures.CRIMSON_FUNGUS :
                material == Material.WARPED_FUNGUS ? TreeFeatures.WARPED_FUNGUS :
                    material == Material.FLOWERING_AZALEA ? TreeFeatures.AZALEA_TREE :
                        null;
        if (growth == null) {
            return true;
        }
        final ServerLevel world = ((CraftWorld) block.getWorld()).getHandle();
        final BlockPos position = new BlockPos(block.getX(), block.getY(), block.getZ());
        //Taken from CraftWorld.generateTree()
        Holder<ConfiguredFeature<?, ?>> growthHolder = world.registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE).get(growth).orElse(null);

        if (growthHolder == null) return false;

        if (!growthHolder.value().place(world, world.getChunkSource().getGenerator(), this.randomSource, position)) {
            block.setType(material);
        }
        return true;
    }

    @Override
    public boolean deleteOnFullGrowth() {
        return true;
    }

}
