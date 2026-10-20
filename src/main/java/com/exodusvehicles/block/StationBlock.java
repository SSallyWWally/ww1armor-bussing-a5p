package com.exodusvehicles.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A low steel pad for vehicles to park on: a repair station mends them quickly, a fuel station fills the tank.
 * Lay a few side by side for big vehicles; any pad under the vehicle counts.
 */
public class StationBlock extends Block {
    public static final int REPAIR = 1, FUEL = 2;
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 2, 16);
    private final int kind;

    public StationBlock(Properties properties, int kind) {
        super(properties);
        this.kind = kind;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    /** Which stations (bit mask) are under this vehicle's footprint. */
    public static int under(Entity e) {
        Level level = e.level();
        AABB box = e.getBoundingBox();
        int y0 = Mth.floor(e.getY() - 0.3), y1 = Mth.floor(e.getY() + 0.2);
        int mask = 0;
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int x = Mth.floor(box.minX); x <= Mth.floor(box.maxX); x++) {
            for (int z = Mth.floor(box.minZ); z <= Mth.floor(box.maxZ); z++) {
                for (int y = y0; y <= y1; y++) {
                    if (level.getBlockState(p.set(x, y, z)).getBlock() instanceof StationBlock s) {
                        mask |= s.kind;
                    }
                }
            }
        }
        return mask;
    }
}
