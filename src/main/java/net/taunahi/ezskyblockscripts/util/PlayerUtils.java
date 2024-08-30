package net.taunahi.ezskyblockscripts.util;

import net.taunahi.ezskyblockscripts.Taunahi;
import net.taunahi.ezskyblockscripts.config.TaunahiConfig;
import net.taunahi.ezskyblockscripts.config.struct.Rewarp;
import net.taunahi.ezskyblockscripts.failsafe.FailsafeManager;
import net.taunahi.ezskyblockscripts.handler.MacroHandler;
import net.taunahi.ezskyblockscripts.util.helper.Clock;
import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class PlayerUtils {

    public static final Clock changeItemEveryClock = new Clock();
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static boolean itemChangedByStaff = false;

    public static boolean isInventoryEmpty(EntityPlayer player) {
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            if (player.inventory.getStackInSlot(i) != null) {
                return false;
            }
        }
        return true;
    }

    public static TaunahiConfig.CropEnum getFarmingCrop() {
        Pair<Block, BlockPos> closestCrop = null;
        boolean foundCropUnderMouse = false;
        if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            BlockPos pos = mc.objectMouseOver.getBlockPos();
            if (mc.theWorld.getBlockState(pos) == null) return TaunahiConfig.CropEnum.NONE;
            Block block = mc.theWorld.getBlockState(pos).getBlock();
            if (block instanceof BlockCrops || block instanceof BlockReed || block instanceof BlockCocoa || block instanceof BlockNetherWart || block instanceof BlockMelon || block instanceof BlockPumpkin || block instanceof BlockMushroom || block instanceof BlockCactus) {
                closestCrop = Pair.of(block, pos);
                foundCropUnderMouse = true;
            }
        }

        if (!foundCropUnderMouse) {
            for (int x = -3; x < 3; x++) {
                for (int y = -1; y < 5; y++) {
                    for (int z = -1; z < 3; z++) {
                        float yaw;
                        if (MacroHandler.getInstance().getCurrentMacro().isPresent()) {
                            yaw = MacroHandler.getInstance().getCurrentMacro().get().getClosest90Deg().orElse(AngleUtils.getClosest());
                        } else {
                            if (TaunahiConfig.getMacro() == TaunahiConfig.MacroEnum.S_MUSHROOM || TaunahiConfig.getMacro() == TaunahiConfig.MacroEnum.S_SUGAR_CANE) {
                                yaw = AngleUtils.getClosestDiagonal();
                            } else {
                                yaw = AngleUtils.getClosest();
                            }
                        }
                        BlockPos pos = BlockUtils.getRelativeBlockPos(x, y, z, yaw);
                        Block block = mc.theWorld.getBlockState(pos).getBlock();
                        if (!(block instanceof BlockCrops || block instanceof BlockReed || block instanceof BlockCocoa || block instanceof BlockNetherWart || block instanceof BlockMelon || block instanceof BlockPumpkin || block instanceof BlockMushroom || block instanceof BlockCactus))
                            continue;

                        if (closestCrop == null || mc.thePlayer.getPosition().distanceSq(pos.getX(), pos.getY(), pos.getZ()) < mc.thePlayer.getPosition().distanceSq(closestCrop.getRight().getX(), closestCrop.getRight().getY(), closestCrop.getRight().getZ())) {
                            closestCrop = Pair.of(block, pos);
                        }
                    }
                }
            }
        }

        if (closestCrop != null) {
            Block left = closestCrop.getLeft();
            if (left.equals(Blocks.wheat)) {
                return TaunahiConfig.CropEnum.WHEAT;
            } else if (left.equals(Blocks.carrots)) {
                return TaunahiConfig.CropEnum.CARROT;
            } else if (left.equals(Blocks.potatoes)) {
                return TaunahiConfig.CropEnum.POTATO;
            } else if (left.equals(Blocks.nether_wart)) {
                return TaunahiConfig.CropEnum.NETHER_WART;
            } else if (left.equals(Blocks.reeds)) {
                return TaunahiConfig.CropEnum.SUGAR_CANE;
            } else if (left.equals(Blocks.cocoa)) {
                return TaunahiConfig.CropEnum.COCOA_BEANS;
            } else if (left.equals(Blocks.melon_block)) {
                return TaunahiConfig.CropEnum.MELON;
            } else if (left.equals(Blocks.pumpkin)) {
                return TaunahiConfig.CropEnum.PUMPKIN;
            } else if (left.equals(Blocks.red_mushroom)) {
                return TaunahiConfig.CropEnum.MUSHROOM;
            } else if (left.equals(Blocks.brown_mushroom)) {
                return TaunahiConfig.CropEnum.MUSHROOM;
            } else if (left.equals(Blocks.cactus)) {
                return TaunahiConfig.CropEnum.CACTUS;
            }
        }
        LogUtils.sendError("Can't detect crop type!");
        return TaunahiConfig.CropEnum.NONE;
    }

    public static void getTool() {
        // Sometimes if staff changed your slot, you might not have the tool in your hand after the swap, so it won't be obvious that you're using a macro
        if (itemChangedByStaff) {
            LogUtils.sendDebug("Item changed by staff, not changing item");
            return;
        }

        if (changeItemEveryClock.isScheduled() && !changeItemEveryClock.passed()) {
            return;
        }

        changeItemEveryClock.schedule(1_500L);
        int id = PlayerUtils.getFarmingTool(MacroHandler.getInstance().getCrop(), true, false);
        if (id == -1) {
            LogUtils.sendDebug("No tool found!");
            return;
        }
        LogUtils.sendDebug("Tool id: " + id + " current item: " + mc.thePlayer.inventory.currentItem);
        if (id == mc.thePlayer.inventory.currentItem) return;
        mc.thePlayer.inventory.currentItem = id;
    }

    public static TaunahiConfig.CropEnum getCropBasedOnMouseOver() {
        if (mc.objectMouseOver == null || mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK)
            return TaunahiConfig.CropEnum.NONE;
        BlockPos pos = mc.objectMouseOver.getBlockPos();
        Block block = mc.theWorld.getBlockState(pos).getBlock();
        if (block.equals(Blocks.wheat)) {
            return TaunahiConfig.CropEnum.WHEAT;
        } else if (block.equals(Blocks.carrots)) {
            return TaunahiConfig.CropEnum.CARROT;
        } else if (block.equals(Blocks.potatoes)) {
            return TaunahiConfig.CropEnum.POTATO;
        } else if (block.equals(Blocks.nether_wart)) {
            return TaunahiConfig.CropEnum.NETHER_WART;
        } else if (block.equals(Blocks.reeds)) {
            return TaunahiConfig.CropEnum.SUGAR_CANE;
        } else if (block.equals(Blocks.cocoa)) {
            return TaunahiConfig.CropEnum.COCOA_BEANS;
        } else if (block.equals(Blocks.melon_block)) {
            return TaunahiConfig.CropEnum.MELON;
        } else if (block.equals(Blocks.pumpkin)) {
            return TaunahiConfig.CropEnum.PUMPKIN;
        } else if (block.equals(Blocks.red_mushroom)) {
            return TaunahiConfig.CropEnum.MUSHROOM;
        } else if (block.equals(Blocks.brown_mushroom)) {
            return TaunahiConfig.CropEnum.MUSHROOM;
        } else if (block.equals(Blocks.cactus)) {
            return TaunahiConfig.CropEnum.CACTUS;
        }
        return TaunahiConfig.CropEnum.NONE;
    }

    public static int getFarmingTool(TaunahiConfig.CropEnum crop, boolean withError, boolean anyHoe) {
        if (crop == null) return withError ? -1 : 0;
        for (int i = 36; i < 44; i++) {
            if (mc.thePlayer.inventoryContainer.inventorySlots.get(i).getStack() != null) {
                String name = mc.thePlayer.inventoryContainer.inventorySlots.get(i).getStack().getDisplayName();
                if (anyHoe) {
                    if (name.contains("Hoe") || name.contains("Dicer") || name.contains("Chopper") || name.contains("Fungi") || name.contains("Knife")) {
                        return i - 36;
                    }
                    continue;
                }
                switch (crop) {
                    case NETHER_WART:
                        if (name.contains("Newton")) {
                            return i - 36;
                        }
                        continue;
                    case CARROT:
                        if (name.contains("Gauss")) {
                            return i - 36;
                        }
                        continue;
                    case WHEAT:
                        if (name.contains("Euclid")) {
                            return i - 36;
                        }
                        continue;
                    case POTATO:
                        if (name.contains("Pythagorean")) {
                            return i - 36;
                        }
                        continue;
                    case SUGAR_CANE:
                        if (name.contains("Turing")) {
                            return i - 36;
                        }
                        continue;
                    case CACTUS:
                        if (name.contains("Knife")) {
                            return i - 36;
                        }
                        continue;
                    case MUSHROOM:
                        if (name.contains("Fungi")) {
                            return i - 36;
                        }
                        continue;
                    case PUMPKIN_MELON_UNKNOWN:
                    case MELON:
                    case PUMPKIN:
                        if (name.contains("Dicer")) {
                            return i - 36;
                        }
                        continue;
                    case COCOA_BEANS:
                        if (name.contains("Chopper")) {
                            return i - 36;
                        }
                }
            }
        }

        int gardeningHoe = InventoryUtils.getSlotIdOfItemInHotbar("Gardening Hoe");
        if (gardeningHoe != -1) {
            return gardeningHoe;
        }

        return withError ? -1 : 0;
    }

    public static boolean isRewarpLocationSet() {
        return !TaunahiConfig.rewarpList.isEmpty();
    }

    public static boolean isStandingOnRewarpLocation() {
        if (TaunahiConfig.rewarpList.isEmpty()) return false;
        Rewarp closest = null;
        double closestDistance = Double.MAX_VALUE;
        for (Rewarp rewarp : TaunahiConfig.rewarpList) {
            double distance = rewarp.getDistance(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ));
            if (distance < closestDistance) {
                closest = rewarp;
                closestDistance = distance;
            }
        }
        if (closest == null) return false;
        BlockPos playerPos = BlockUtils.getRelativeBlockPos(0, 0, 0);
        return playerPos.getX() == closest.getX() && playerPos.getY() == closest.getY() && playerPos.getZ() == closest.getZ();
    }

    public static boolean shouldPushBack() {
        if (FailsafeManager.getInstance().triggeredFailsafe.isPresent()) return false;
        float angle = AngleUtils.getClosest();
        double x = mc.thePlayer.posX % 1;
        double z = mc.thePlayer.posZ % 1;
        Block blockBehind = BlockUtils.getRelativeBlock(0, 0, -1);
        if (!(blockBehind.getMaterial().isSolid() || (blockBehind instanceof BlockSlab) || blockBehind.equals(Blocks.carpet) || (blockBehind instanceof BlockDoor)) || blockBehind.getMaterial().isLiquid())
            return false;
        if (angle == 0) {
            return (z > -0.65 && z < -0.1) || (z < 0.9 && z > 0.35);
        } else if (angle == 90) {
            return (x > -0.9 && x < -0.35) || (x < 0.65 && x > 0.1);
        } else if (angle == 180) {
            return (z > -0.9 && z < -0.35) || (z < 0.65 && z > 0.1);
        } else if (angle == 270) {
            return (x > -0.65 && x < -0.1) || (x < 0.9 && x > 0.35);
        }
        return false;
    }

    public static boolean shouldWalkForwards() {
        if (FailsafeManager.getInstance().triggeredFailsafe.isPresent()) return false;
        if (MacroHandler.getInstance().getCrop() == TaunahiConfig.CropEnum.CACTUS || (TaunahiConfig.getMacro() != TaunahiConfig.MacroEnum.S_PUMPKIN_MELON_MELONGKINGDE && (MacroHandler.getInstance().getCrop() == TaunahiConfig.CropEnum.PUMPKIN || MacroHandler.getInstance().getCrop() == TaunahiConfig.CropEnum.MELON)))
            return false;

        float angle = AngleUtils.getClosest();
        double x = mc.thePlayer.posX % 1;
        double z = mc.thePlayer.posZ % 1;
        float yaw;
        if (MacroHandler.getInstance().getCurrentMacro().isPresent() && MacroHandler.getInstance().getCurrentMacro().get().getClosest90Deg().isPresent()) {
            yaw = MacroHandler.getInstance().getCurrentMacro().get().getClosest90Deg().get();
        } else {
            yaw = mc.thePlayer.rotationYaw;
        }
        if (BlockUtils.canWalkThrough(BlockUtils.getRelativeBlockPos(0, 0, 1, yaw))) {
            return false;
        }
        if (angle == 0) {
            return (z > -0.9 && z < -0.35) || (z < 0.65 && z > 0.1);
        } else if (angle == 90) {
            return (x > -0.65 && x < -0.1) || (x < 0.9 && x > 0.35);
        } else if (angle == 180) {
            return (z > -0.65 && z < -0.1) || (z < 0.9 && z > 0.35);
        } else if (angle == 270) {
            return (x > -0.9 && x < -0.35) || (x < 0.65 && x > 0.1);
        }
        return false;
    }

    public static boolean isSpawnLocationSet() {
        return TaunahiConfig.spawnPosX != 0 || TaunahiConfig.spawnPosY != 0 || TaunahiConfig.spawnPosZ != 0;
    }

    public static boolean isStandingOnSpawnPoint() {
        BlockPos pos = BlockUtils.getRelativeBlockPos(0, 0, 0);
        return pos.getX() == TaunahiConfig.spawnPosX && pos.getY() == TaunahiConfig.spawnPosY && pos.getZ() == TaunahiConfig.spawnPosZ;
    }

    public static Vec3 getSpawnLocation() {
        return new Vec3(TaunahiConfig.spawnPosX + 0.5, TaunahiConfig.spawnPosY + 0.5, TaunahiConfig.spawnPosZ + 0.5);
    }

    public static void setSpawnLocation() {
        if (mc.thePlayer == null) return;
        BlockPos pos = BlockUtils.getRelativeBlockPos(0, 0, 0);
        TaunahiConfig.spawnPosX = pos.getX();
        TaunahiConfig.spawnPosY = pos.getY();
        TaunahiConfig.spawnPosZ = pos.getZ();
        Taunahi.config.save();
    }

    public static Entity getEntityCuttingOtherEntity(Entity e) {
        return getEntityCuttingOtherEntity(e, entity -> true);
    }

    public static Entity getEntityCuttingOtherEntity(Entity e, Predicate<Entity> predicate) {
        List<Entity> possible = mc.theWorld.getEntitiesInAABBexcluding(e, e.getEntityBoundingBox().expand(0.3D, 2.0D, 0.3D), a -> {
            boolean flag1 = (!a.isDead && !a.equals(mc.thePlayer));
            boolean flag2 = !(a instanceof net.minecraft.entity.projectile.EntityFireball);
            boolean flag3 = !(a instanceof net.minecraft.entity.projectile.EntityFishHook);
            boolean flag4 = predicate.test(a);
            return flag1 && flag2 && flag3 && flag4;
        });
        if (!possible.isEmpty())
            return Collections.min(possible, Comparator.comparing(e2 -> e2.getDistanceToEntity(e)));
        return null;
    }

    public static boolean isPlayerSuffocating() {
        AxisAlignedBB playerBB = mc.thePlayer.getEntityBoundingBox().expand(-0.15, -0.15, -0.15);
        List<AxisAlignedBB> collidingBoxes = mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, playerBB);
        return !collidingBoxes.isEmpty();
    }

    public static EnumFacing getHorizontalFacing(float yaw) {
        return EnumFacing.getHorizontal(MathHelper.floor_double((double) (yaw * 4.0F / 360.0F) + 0.5) & 3);
    }

    public static void closeScreen() {
        if (mc.currentScreen != null && mc.thePlayer != null) {
            mc.thePlayer.closeScreen();
        }
    }
}
