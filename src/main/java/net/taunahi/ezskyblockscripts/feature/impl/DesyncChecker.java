package net.taunahi.ezskyblockscripts.feature.impl;

import cc.polyfrost.oneconfig.utils.Multithreading;
import net.taunahi.ezskyblockscripts.config.TaunahiConfig;
import net.taunahi.ezskyblockscripts.event.ClickedBlockEvent;
import net.taunahi.ezskyblockscripts.failsafe.FailsafeManager;
import net.taunahi.ezskyblockscripts.feature.IFeature;
import net.taunahi.ezskyblockscripts.handler.MacroHandler;
import net.taunahi.ezskyblockscripts.util.LogUtils;
import net.taunahi.ezskyblockscripts.util.helper.CircularFifoQueue;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockNetherWart;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DesyncChecker implements IFeature {
    private static DesyncChecker instance;
    private final Minecraft mc = Minecraft.getMinecraft();
    @Getter
    private final CircularFifoQueue<ClickedBlockEvent> clickedBlocks = new CircularFifoQueue<>(120);
    private boolean enabled = false;

    public static DesyncChecker getInstance() {
        if (instance == null) {
            instance = new DesyncChecker();
        }
        return instance;
    }

    @Override
    public String getName() {
        return "Desync Checker";
    }

    @Override
    public boolean isRunning() {
        return enabled;
    }

    @Override
    public boolean shouldPauseMacroExecution() {
        return true;
    }

    @Override
    public boolean shouldStartAtMacroStart() {
        return isToggled();
    }

    @Override
    public void start() {
        clickedBlocks.clear();
    }

    @Override
    public void stop() {
        clickedBlocks.clear();
    }

    @Override
    public void resetStatesAfterMacroDisabled() {
        enabled = false;
    }

    @Override
    public boolean isToggled() {
        return TaunahiConfig.checkDesync;
    }

    @Override
    public boolean shouldCheckForFailsafes() {
        return false;
    }

    @SubscribeEvent
    public void onClickedBlock(ClickedBlockEvent event) {
        if (!isToggled()) return;
        if (!MacroHandler.getInstance().isMacroToggled()) return;
        if (!isCrop(mc.theWorld.getBlockState(event.getPos()).getBlock())) return;
        if (FailsafeManager.getInstance().triggeredFailsafe.isPresent()) return;
        clickedBlocks.add(event);
        if (!clickedBlocks.isAtFullCapacity()) return;
        if (!checkIfDesync()) return;
        if (enabled) return;
        enabled = true;
        stop();
        LogUtils.sendWarning("[Desync Checker] Desync detected, pausing macro for " + Math.floor((double) TaunahiConfig.desyncPauseDelay / 1_000) + " seconds to prevent further desync.");
        MacroHandler.getInstance().pauseMacro();
        Multithreading.schedule(() -> {
            if (!MacroHandler.getInstance().isMacroToggled()) return;
            enabled = false;
            LogUtils.sendWarning("[Desync Checker] Desync should be over, resuming macro execution");
            MacroHandler.getInstance().resumeMacro();
        }, TaunahiConfig.desyncPauseDelay, TimeUnit.MILLISECONDS);
    }

    private boolean isCrop(Block block) {
        return block instanceof BlockNetherWart ||
                block instanceof BlockCrops ||
                block.equals(Blocks.melon_block) ||
                block.equals(Blocks.pumpkin) ||
                block.equals(Blocks.reeds) ||
                block.equals(Blocks.cactus) ||
                block.equals(Blocks.cocoa) ||
                block.equals(Blocks.brown_mushroom_block) ||
                block.equals(Blocks.red_mushroom_block);
    }

    private boolean checkIfDesync() {
        float RATIO = 0.75f;
        List<ClickedBlockEvent> list = new ArrayList<>(clickedBlocks);
        int count = 0;
        for (ClickedBlockEvent pos : list) {
            IBlockState state = mc.theWorld.getBlockState(pos.getPos());
            if (state == null) continue;

            switch (MacroHandler.getInstance().getCrop()) {
                case NETHER_WART:
                    if (state.getBlock() instanceof BlockNetherWart && state.getValue(BlockNetherWart.AGE) == 3)
                        count++;
                    break;
                case SUGAR_CANE:
                    if (state.getBlock().equals(Blocks.reeds)) count++;
                    break;
                case CACTUS:
                    if (state.getBlock().equals(Blocks.cactus)) count++;
                    break;
                case MELON:
                case PUMPKIN:
                    if (!state.getBlock().equals(Blocks.air)) count++;
                    break;
                case MUSHROOM:
                    if (state.getBlock().equals(Blocks.brown_mushroom_block) || state.getBlock().equals(Blocks.red_mushroom_block))
                        count++;
                    break;
                case COCOA_BEANS:
                    if (state.getBlock().equals(Blocks.cocoa) && state.getValue(BlockCocoa.AGE) == 2) count++;
                    break;
                case CARROT:
                case POTATO:
                case WHEAT:
                    if (state.getBlock() instanceof BlockCrops && state.getValue(BlockCrops.AGE) == 7) count++;
                    break;
                default:
                    // Unknown crop
            }
        }
        return count / (float) list.size() >= RATIO;
    }
}
