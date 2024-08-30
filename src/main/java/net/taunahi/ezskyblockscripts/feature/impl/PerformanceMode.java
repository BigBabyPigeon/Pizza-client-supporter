package net.taunahi.ezskyblockscripts.feature.impl;

import net.taunahi.ezskyblockscripts.config.TaunahiConfig;
import net.taunahi.ezskyblockscripts.feature.IFeature;
import net.minecraft.client.Minecraft;

public class PerformanceMode implements IFeature {
    private final Minecraft mc = Minecraft.getMinecraft();
    private static PerformanceMode instance;

    public static PerformanceMode getInstance() {
        if (instance == null) {
            instance = new PerformanceMode();
        }
        return instance;
    }

    private int renderDistanceBefore = 0;
    private int maxFpsBefore = 0;

    private boolean enabled = false;

    @Override
    public String getName() {
        return "Performance Mode";
    }

    @Override
    public boolean isRunning() {
        return enabled;
    }

    @Override
    public boolean shouldPauseMacroExecution() {
        return false;
    }

    @Override
    public boolean shouldStartAtMacroStart() {
        return isToggled();
    }

    @Override
    public void start() {
        enabled = true;
        renderDistanceBefore = mc.gameSettings.renderDistanceChunks;
        maxFpsBefore = mc.gameSettings.limitFramerate;
        mc.gameSettings.renderDistanceChunks = 1;
        mc.gameSettings.limitFramerate = TaunahiConfig.performanceModeMaxFPS;
        mc.addScheduledTask(() -> mc.renderGlobal.loadRenderers());
    }

    @Override
    public void stop() {
        enabled = false;
        mc.gameSettings.renderDistanceChunks = renderDistanceBefore;
        mc.gameSettings.limitFramerate = maxFpsBefore;
        renderDistanceBefore = 0;
        maxFpsBefore = 0;
        mc.addScheduledTask(() -> mc.renderGlobal.loadRenderers());
    }

    @Override
    public void resetStatesAfterMacroDisabled() {

    }

    @Override
    public boolean isToggled() {
        return TaunahiConfig.performanceMode;
    }

    @Override
    public boolean shouldCheckForFailsafes() {
        return false;
    }
}
