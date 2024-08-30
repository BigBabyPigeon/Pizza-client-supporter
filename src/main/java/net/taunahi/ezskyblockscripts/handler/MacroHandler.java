package net.taunahi.ezskyblockscripts.handler;

import baritone.api.BaritoneAPI;
import cc.polyfrost.oneconfig.utils.Multithreading;
import net.taunahi.ezskyblockscripts.config.TaunahiConfig;
import net.taunahi.ezskyblockscripts.config.struct.Rewarp;
import net.taunahi.ezskyblockscripts.event.ReceivePacketEvent;
import net.taunahi.ezskyblockscripts.failsafe.FailsafeManager;
import net.taunahi.ezskyblockscripts.failsafe.impl.LowerAvgBpsFailsafe;
import net.taunahi.ezskyblockscripts.failsafe.impl.WorldChangeFailsafe;
import net.taunahi.ezskyblockscripts.feature.FeatureManager;
import net.taunahi.ezskyblockscripts.feature.impl.*;
import net.taunahi.ezskyblockscripts.hud.ProfitCalculatorHUD;
import net.taunahi.ezskyblockscripts.macro.AbstractMacro;
import net.taunahi.ezskyblockscripts.macro.impl.*;
import net.taunahi.ezskyblockscripts.util.KeyBindUtils;
import net.taunahi.ezskyblockscripts.util.LogUtils;
import net.taunahi.ezskyblockscripts.util.PlayerUtils;
import net.taunahi.ezskyblockscripts.util.RenderUtils;
import net.taunahi.ezskyblockscripts.util.helper.AudioManager;
import net.taunahi.ezskyblockscripts.util.helper.FlyPathfinder;
import net.taunahi.ezskyblockscripts.util.helper.Timer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class MacroHandler {
    private static MacroHandler instance;
    private final Minecraft mc = Minecraft.getMinecraft();
    @Getter
    private final Timer macroingTimer = new Timer();
    @Getter
    private final Timer analyticsTimer = new Timer();
    @Getter
    @Setter
    private Optional<AbstractMacro> currentMacro = Optional.empty();
    @Getter
    @Setter
    private boolean isMacroToggled = false;
    @Getter
    @Setter
    private boolean startingUp = false;
    Runnable startCurrent = () -> {
        KeyBindUtils.stopMovement();
        if (isMacroToggled()) {
            currentMacro.ifPresent(AbstractMacro::onEnable);
        }
        startingUp = false;
    };
    @Getter
    @Setter
    private TaunahiConfig.CropEnum crop = TaunahiConfig.CropEnum.NONE;

    public static MacroHandler getInstance() {
        if (instance == null) {
            instance = new MacroHandler();
        }
        return instance;
    }

    public boolean isCurrentMacroEnabled() {
        return currentMacro.isPresent() && currentMacro.get().isEnabledAndNoFeature();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isCurrentMacroPaused() {
        return isMacroToggled() && currentMacro.isPresent() && currentMacro.get().isPaused();
    }

    public boolean isTeleporting() {
        return currentMacro.isPresent() && (currentMacro.get().getRewarpState() != AbstractMacro.RewarpState.NONE || !currentMacro.get().getAfterRewarpDelay().passed() );
    }

    public <T extends AbstractMacro> T getMacro() {
        switch (TaunahiConfig.getMacro()) {
            case S_V_NORMAL_TYPE:
            case S_PUMPKIN_MELON:
            case S_PUMPKIN_MELON_MELONGKINGDE:
            case S_CACTUS:
            case S_CACTUS_SUNTZU:
            case S_COCOA_BEANS_LEFT_RIGHT:
                return Macros.S_SHAPE_VERTICAL_CROP_MACRO.getMacro();
            case S_PUMPKIN_MELON_DEFAULT_PLOT:
                return Macros.S_SHAPE_MELON_PUMPKIN_DEFAULT_MACRO.getMacro();
            case S_SUGAR_CANE:
                return Macros.S_SHAPE_SUGARCANE_MACRO.getMacro();
            case S_COCOA_BEANS:
                return Macros.S_SHAPE_COCOA_BEAN_MACRO.getMacro();
            case S_MUSHROOM:
                return Macros.S_SHAPE_MUSHROOM_MACRO.getMacro();
            case S_MUSHROOM_ROTATE:
                return Macros.S_SHAPE_MUSHROOM_ROTATE_MACRO.getMacro();
            case S_MUSHROOM_SDS:
                return Macros.S_SHAPE_MUSHROOM_SDS.getMacro();
            default:
                throw new IllegalArgumentException("Invalid crop type: " + TaunahiConfig.macroType);
        }
    }

    public void setMacro(AbstractMacro macro) {
        setCurrentMacro(Optional.ofNullable(macro));
    }

    public void toggleMacro() {
        if (isMacroToggled()) {
            this.disableMacro();
        } else {
            if (FlyPathfinder.getInstance().isRunning()) {
                FlyPathfinder.getInstance().stop();
            } else {
                BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
            }
            if (VisitorsMacro.getInstance().isRunning()) {
                VisitorsMacro.getInstance().stop();
                return;
            }
            if (PestsDestroyer.getInstance().isRunning()) {
                PestsDestroyer.getInstance().stop();
                return;
            }
            if (VisitorsMacro.getInstance().isInBarn()) {
                if (VisitorsMacro.getInstance().isToggled()) {
                    VisitorsMacro.getInstance().setManuallyStarted(true);
                    VisitorsMacro.getInstance().start();
                } else {
                    LogUtils.sendError("You are in the barn and have Visitors Macro disabled!");
                }
                return;
            }
            if (FailsafeManager.getInstance().isHadEmergency()) {
                if (FailsafeManager.getInstance().triggeredFailsafe.isPresent()) {
                    FailsafeManager.getInstance().stopFailsafes();
                }
                FailsafeManager.getInstance().setHadEmergency(false);
                FailsafeManager.getInstance().getRestartMacroAfterFailsafeDelay().reset();
                LogUtils.sendWarning("Farm manually and DO NOT restart the macro too soon! The staff might still be spectating you for a while!");
                return;
            }
            this.enableMacro();
        }
    }

    public void enableMacro() {
        if (!GameStateHandler.getInstance().inGarden()) {
            LogUtils.sendError("You must be in the garden to start the macro!");
            return;
        }

        setMacro(getMacro());
        if (!currentMacro.isPresent()) {
            LogUtils.sendError("Invalid macro type: " + TaunahiConfig.macroType);
            return;
        }
        LogUtils.sendDebug("Selected macro: " + LogUtils.capitalize(currentMacro.get().getClass().getSimpleName()));
        PlayerUtils.closeScreen();
        LogUtils.sendSuccess("Macro enabled!");
        LogUtils.webhookLog("Macro enabled!");

        analyticsTimer.reset();
        Multithreading.schedule(() -> {
            if (!macroingTimer.isScheduled() || ProfitCalculatorHUD.resetStatsBetweenDisabling) {
                macroingTimer.schedule();
                macroingTimer.pause();
            }
        }, 300, TimeUnit.MILLISECONDS);

        if (mc.currentScreen != null) {
            PlayerUtils.closeScreen();
        }
        AudioManager.getInstance().setSoundBeforeChange(mc.gameSettings.getSoundLevel(SoundCategory.MASTER));

        FeatureManager.getInstance().enableAll();

        setMacroToggled(true);
        enableCurrentMacro();
        if (TaunahiConfig.sendAnalyticData) {
            try {
                BanInfoWS.getInstance().sendAnalyticsData(BanInfoWS.AnalyticsState.START_SESSION);
            } catch (Exception e) {
                LogUtils.sendDebug("Failed to send analytics data!");
                e.printStackTrace();
            } finally {
                analyticsTimer.schedule();
            }
        }
    }

    public void disableMacro() {
        setMacroToggled(false);
        LogUtils.sendSuccess("Macro disabled!");
        LogUtils.webhookLog("Macro disabled!");
        currentMacro.ifPresent(m -> {
            m.setSavedState(Optional.empty());
            m.getRotation().reset();
        });

        macroingTimer.pause();
        analyticsTimer.pause();

        if (TaunahiConfig.sendAnalyticData) {
            try {
                BanInfoWS.getInstance().sendAnalyticsData(BanInfoWS.AnalyticsState.END_SESSION);
            } catch (Exception e) {
                LogUtils.sendDebug("Failed to send analytics data!");
                e.printStackTrace();
            }
        }

        setCrop(TaunahiConfig.CropEnum.NONE);
        FeatureManager.getInstance().disableAll();
        FeatureManager.getInstance().resetAllStates();
        FailsafeManager.getInstance().resetAfterMacroDisable();
        if (UngrabMouse.getInstance().isToggled())
            UngrabMouse.getInstance().stop();
        disableCurrentMacro();
        setCurrentMacro(Optional.empty());
        if (FlyPathfinder.getInstance().isRunning()) {
            FlyPathfinder.getInstance().stop();
        } else {
            BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
        }
    }

    public void pauseMacro(boolean scheduler) {
        currentMacro.ifPresent(cm -> {
            KeyBindUtils.stopMovement();
            if (cm.isPaused()) return;
            cm.saveState();
            cm.onDisable();
            macroingTimer.pause();
            analyticsTimer.pause();
            LowerAvgBpsFailsafe.getInstance().endOfFailsafeTrigger();
            if (scheduler && Freelook.getInstance().isRunning()) {
                Freelook.getInstance().stop();
            }
            if (Scheduler.getInstance().isFarming())
                Scheduler.getInstance().pause();
        });
    }

    public void pauseMacro() {
        pauseMacro(false);
    }

    public void resumeMacro() {
        currentMacro.ifPresent(cm -> {
            if (!cm.isPaused()) return;
            mc.inGameHasFocus = true;
            cm.onEnable();
            PlayerUtils.getTool();
            macroingTimer.resume();
            analyticsTimer.resume();
            Scheduler.getInstance().resume();
        });
    }

    public void enableCurrentMacro() {
        if (currentMacro.isPresent() && !currentMacro.get().isEnabledAndNoFeature() && !startingUp) {
            mc.displayGuiScreen(null);
            mc.inGameHasFocus = true;
            mc.mouseHelper.grabMouseCursor();
            startingUp = true;
            PlayerUtils.itemChangedByStaff = false;
            PlayerUtils.changeItemEveryClock.reset();
            KeyBindUtils.holdThese(mc.thePlayer.capabilities.isFlying ? mc.gameSettings.keyBindSneak : null);
            Multithreading.schedule(() -> {
                startCurrent.run();
                macroingTimer.resume();
            }, 300, TimeUnit.MILLISECONDS);
        }
    }

    public void disableCurrentMacro() {
        currentMacro.ifPresent(AbstractMacro::onDisable);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END || !isMacroToggled()) {
            return;
        }
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (mc.currentScreen != null) {
            KeyBindUtils.stopMovement();
            return;
        }

        if (!GameStateHandler.getInstance().inGarden()) {
            if (!FeatureManager.getInstance().shouldIgnoreFalseCheck() && !FailsafeManager.getInstance().triggeredFailsafe.isPresent()) {
                FailsafeManager.getInstance().possibleDetection(WorldChangeFailsafe.getInstance());
            }
            return;
        }

        currentMacro.ifPresent(cm -> {
            if (isMacroToggled())
                cm.onTickCheckTeleport();
            if (!cm.isEnabledAndNoFeature()) return;
            cm.onTick();
        });
    }

    @SubscribeEvent
    public void onChatMessageReceived(ClientChatReceivedEvent event) {
        if (event.type == 0) {
            String message = event.message.getUnformattedText();
            if (!message.contains(":") && GameStateHandler.getInstance().inGarden()) {
                if (message.equals("Your spawn location has been set!")) {
                    PlayerUtils.setSpawnLocation();
                }
            }
        }

        if (!isMacroToggled()) {
            return;
        }

        currentMacro.ifPresent(m -> {
            if (!m.isEnabledAndNoFeature()) return;
            m.onChatMessageReceived(event.message.getUnformattedText());
        });
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (TaunahiConfig.highlightRewarp && TaunahiConfig.rewarpList != null && GameStateHandler.getInstance().inGarden()) {
            Color chroma = Color.getHSBColor((float) ((System.currentTimeMillis() / 10) % 2000) / 2000, 1, 1);
            Color chromaLowerAlpha = new Color(chroma.getRed(), chroma.getGreen(), chroma.getBlue(), 120);

            for (Rewarp rewarp : TaunahiConfig.rewarpList) {
                RenderUtils.drawBlockBox(new BlockPos(rewarp.x, rewarp.y, rewarp.z), chromaLowerAlpha);
            }
        }

        if (TaunahiConfig.drawSpawnLocation && PlayerUtils.isSpawnLocationSet() && GameStateHandler.getInstance().inGarden()) {
            BlockPos spawnLocation = new BlockPos(PlayerUtils.getSpawnLocation());
            RenderUtils.drawBlockBox(spawnLocation, new Color(Color.orange.getRed(), Color.orange.getGreen(), Color.orange.getBlue(), 80));
        }

        if (!isMacroToggled()) {
            return;
        }
        currentMacro.ifPresent(m -> {
            if (!m.isEnabledAndNoFeature()) return;
            m.onLastRender();
        });
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (!isMacroToggled()) {
            return;
        }
        currentMacro.ifPresent(m -> {
            if (!m.isEnabledAndNoFeature()) return;
            m.onOverlayRender(event);
        });
    }

    @SubscribeEvent
    public void onReceivePacket(ReceivePacketEvent event) {
        if (!isMacroToggled()) {
            return;
        }
        currentMacro.ifPresent(m -> {
            if (!m.isEnabledAndNoFeature()) return;
            m.onPacketReceived(event);
        });
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Unload event) {
        if (!isMacroToggled()) {
            return;
        }
        currentMacro.ifPresent(m -> {
            m.setCurrentState(AbstractMacro.State.NONE);
            m.getSavedState().ifPresent(ss -> ss.setState(AbstractMacro.State.NONE));
        });
    }

    @AllArgsConstructor
    public enum Macros {
        S_SHAPE_MUSHROOM_ROTATE_MACRO(SShapeMushroomRotateMacro.class),
        S_SHAPE_MUSHROOM_MACRO(SShapeMushroomMacro.class),
        S_SHAPE_COCOA_BEAN_MACRO(SShapeCocoaBeanMacro.class),
        S_SHAPE_SUGARCANE_MACRO(SShapeSugarcaneMacro.class),
        S_SHAPE_VERTICAL_CROP_MACRO(SShapeVerticalCropMacro.class),
        S_SHAPE_MELON_PUMPKIN_DEFAULT_MACRO(SShapeMelonPumpkinDefaultMacro.class),
        S_SHAPE_MUSHROOM_SDS(SShapeMushroomSDSMacro.class);

        private static final Map<Macros, AbstractMacro> macros = new HashMap<>();
        private final Class<? extends AbstractMacro> macroClass;

        public <T extends AbstractMacro> T getMacro() {
            if (!macros.containsKey(this)) {
                try {
                    macros.put(this, macroClass.newInstance());
                } catch (InstantiationException | IllegalAccessException e) {
                    LogUtils.sendError("Failed to instantiate macro: " + this.name());
                    e.printStackTrace();
                }
            }
            return (T) macros.get(this);
        }
    }
}
