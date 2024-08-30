package net.taunahi.ezskyblockscripts.feature.impl;

import net.taunahi.ezskyblockscripts.config.TaunahiConfig;
import net.taunahi.ezskyblockscripts.feature.IFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MouseHelper;
import org.lwjgl.input.Mouse;

public class UngrabMouse implements IFeature {
    private final Minecraft mc = Minecraft.getMinecraft();
    private static UngrabMouse instance;

    public static UngrabMouse getInstance() {
        if (instance == null) {
            instance = new UngrabMouse();
        }
        return instance;
    }

    private boolean mouseUngrabbed;
    private MouseHelper oldMouseHelper;

    public void ungrabMouse() {
        if (!Mouse.isGrabbed() || mouseUngrabbed) return;
        mc.gameSettings.pauseOnLostFocus = false;
        oldMouseHelper = mc.mouseHelper;
        oldMouseHelper.ungrabMouseCursor();
        mc.inGameHasFocus = true;
        mc.mouseHelper = new MouseHelper() {
            @Override
            public void mouseXYChange() {
            }

            @Override
            public void grabMouseCursor() {
            }

            @Override
            public void ungrabMouseCursor() {
            }
        };
        mouseUngrabbed = true;
    }

    public void regrabMouse() {
        regrabMouse(false);
    }

    public void regrabMouse(boolean force) {
        if (!mouseUngrabbed && !force) return;
        mc.mouseHelper = oldMouseHelper;
        if (mc.currentScreen == null || force) {
            mc.mouseHelper.grabMouseCursor();
        }
        mouseUngrabbed = false;
    }

    @Override
    public String getName() {
        return "Ungrab Mouse";
    }

    @Override
    public boolean isRunning() {
        return false;
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
        ungrabMouse();
    }

    @Override
    public void stop() {
        regrabMouse();
    }

    @Override
    public void resetStatesAfterMacroDisabled() {

    }

    @Override
    public boolean isToggled() {
        return TaunahiConfig.autoUngrabMouse;
    }

    @Override
    public boolean shouldCheckForFailsafes() {
        return false;
    }
}
