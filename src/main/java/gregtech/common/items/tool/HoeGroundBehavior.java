package gregtech.common.items.tool;

import com.google.common.collect.ImmutableSet;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.items.toolitem.aoe.AoESymmetrical;
import gregtech.api.items.toolitem.behavior.IToolBehavior;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public class HoeGroundBehavior implements IToolBehavior {

    @Nonnull
    @Override
    public EnumActionResult onItemUse(@Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (facing == EnumFacing.DOWN) return EnumActionResult.PASS;

        ItemStack stack = player.getHeldItem(hand);
        AoESymmetrical aoeDefinition = ToolHelper.getAoEDefinition(stack);

        Set<BlockPos> blocks;
        // only attempt to till if the center block is tillable
        if (world.isAirBlock(pos.up()) && isBlockTillable(stack, world, player, pos, null)) {
            if (aoeDefinition == AoESymmetrical.none()) {
                blocks = ImmutableSet.of(pos);
            } else {
                Vec3d lookPos = player.getPositionEyes(1F);
                Vec3d rotation = player.getLook(1);
                Vec3d realLookPos = lookPos.add(rotation.x * 5, rotation.y * 5, rotation.z * 5);
                RayTraceResult rayTraceResult = world.rayTraceBlocks(lookPos, realLookPos);

                if (rayTraceResult == null) return EnumActionResult.PASS;
                if (rayTraceResult.typeOfHit != RayTraceResult.Type.BLOCK) return EnumActionResult.PASS;
                if (rayTraceResult.sideHit == null) return EnumActionResult.PASS;

                blocks = getTillableBlocks(stack, aoeDefinition, world, player, rayTraceResult);
                blocks.add(rayTraceResult.getBlockPos());
            }
        } else return EnumActionResult.PASS;

        boolean tilled = false;
        for (BlockPos blockPos : blocks) {
            int hook = ForgeEventFactory.onHoeUse(stack, player, world, blockPos);
            if (hook != 0) return hook > 0 ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;

            IBlockState state = world.getBlockState(blockPos);
            Block block = state.getBlock();
            if (block == Blocks.GRASS || block == Blocks.GRASS_PATH) {
                tillGround(world, player, stack, blockPos, Blocks.FARMLAND.getDefaultState());
                tilled = true;
            } else if (block == Blocks.DIRT) {
                switch (state.getValue(BlockDirt.VARIANT)) {
                    case DIRT: {
                        tillGround(world, player, stack, blockPos, Blocks.FARMLAND.getDefaultState());
                        tilled = true;
                        break;
                    }
                    case COARSE_DIRT: {
                        tillGround(world, player, stack, blockPos, Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.DIRT));
                        tilled = true;
                        break;
                    }
                }
            }
        }

        if (tilled) {
            world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ITEM_HOE_TILL, SoundCategory.PLAYERS, 1.0F, 1.0F);
            player.swingArm(hand);
            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }

    public static Set<BlockPos> getTillableBlocks(ItemStack stack, AoESymmetrical aoeDefinition, World world, EntityPlayer player, RayTraceResult rayTraceResult) {
        return ToolHelper.iterateAoE(stack, aoeDefinition, world, player, rayTraceResult, HoeGroundBehavior::isBlockTillable);
    }

    private static boolean isBlockTillable(ItemStack stack, World world, EntityPlayer player, BlockPos pos, @Nullable BlockPos hitBlockPos) {
        if (world.isAirBlock(pos.up())) {
            Block block = world.getBlockState(pos).getBlock();
            return block == Blocks.GRASS || block == Blocks.GRASS_PATH || block == Blocks.DIRT;
        }
        return false;
    }

    private static void tillGround(@Nonnull World world, EntityPlayer player, ItemStack stack, BlockPos pos, IBlockState state) {
        world.setBlockState(pos, state, 11);
        if (!player.isCreative()) {
            ToolHelper.damageItem(stack, player);
        }
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World world, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flag) {
        tooltip.add(" " + I18n.format("item.gt.tool.behavior.ground_tilling"));
    }
}
