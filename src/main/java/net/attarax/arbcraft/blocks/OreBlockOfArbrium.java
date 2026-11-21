package net.attarax.arbcraft.blocks;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

public class OreBlockOfArbrium extends Block {
    public OreBlockOfArbrium(BlockBehaviour.Properties properties) {
        super(properties.sound(SoundType.STONE).strength(10.1f, 100f)
                .lightLevel(s -> 5).requiresCorrectToolForDrops()
                .hasPostProcess((bs, br, bp) -> true)
                .emissiveRendering((bs, br, bp) -> true));
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 15;
    }
}