package net.taunahi.ezskyblockscripts.failsafe;

import cc.polyfrost.oneconfig.utils.Multithreading;
import net.taunahi.ezskyblockscripts.config.TaunahiConfig;
import net.taunahi.ezskyblockscripts.event.ReceivePacketEvent;
import net.taunahi.ezskyblockscripts.failsafe.impl.*;
import net.taunahi.ezskyblockscripts.feature.FeatureManager;
import net.taunahi.ezskyblockscripts.feature.impl.Scheduler;
import net.taunahi.ezskyblockscripts.handler.MacroHandler;
import net.taunahi.ezskyblockscripts.handler.RotationHandler;
import net.taunahi.ezskyblockscripts.macro.AbstractMacro;
import net.taunahi.ezskyblockscripts.util.*;
import net.taunahi.ezskyblockscripts.util.helper.AudioManager;
import net.taunahi.ezskyblockscripts.util.helper.Clock;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.lwjgl.opengl.Display;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class FailsafeManager {
    private final Minecraft mc = Minecraft.getMinecraft();
    private static FailsafeManager instance;

    public static FailsafeManager getInstance() {
        if (instance == null) {
            instance = new FailsafeManager();
        }
        return instance;
    }
    public final List<Failsafe> failsafes = new ArrayList<>();
    public Optional<Failsafe> triggeredFailsafe = Optional.empty();
    @Getter
    public final ArrayList<Failsafe> emergencyQueue = new ArrayList<>();
    @Getter
    public final Clock chooseEmergencyDelay = new Clock();
    private final Clock onTickDelay = new Clock();
    @Getter
    private final Clock restartMacroAfterFailsafeDelay = new Clock();
    public final RotationHandler rotation = RotationHandler.getInstance();
    private boolean sendingFailsafeInfo = false;
    public boolean swapItemDuringRecording = false;
    private static final String[] FAILSAFE_MESSAGES = new String[]{
            "WHAT", "what?", "what", "what??", "what???", "wut?", "?", "what???",
            "yo huh", "yo huh?", "yo?", "ehhhhh??", "eh", "yo", "ahmm", "ehh", "LOL what", "lol :skull:", "bro wtf was that?",
            "lmao", "lmfao", "wtf is this", "wtf", "WTF", "wtf is this?", "wtf???", "tf", "tf?", "wth", "lmao what?", "????",
            "??", "???????", "???", "UMMM???", "umm", "ummm???", "damn wth", "Damn", "damn wtf", "damn", "hmmm", "hm", "sus",
            "hmm", "ok??", "ok?", "give me a rest", "again lol", "again??", "ok damn", "seriously?", "seriously????",
            "seriously", "really?", "really", "are you kidding me?", "are you serious?", "are you fr???", "not again",
            "give me a break", "youre kidding right?", "youre joking", "youre kidding me", "you must be joking", "seriously bro?",
            "cmon now", "cmon", "this is too much", "stop messing with me", "um what's going on?", "yo huh? did something happen?",
            "lol, what was that?", "ehhh what's happening here?", "bro, wtf was that move?", "wth just happened?", "again seriously?",
            "ok seriously what's up?", "are you kidding me right now?", "give me a break, what's going on?", "seriously bro?",
            "lmao, what was that move?", "damn wth just happened?", "ok, damn what's happening?", "you're joking right?",
            "cmon now whats happening?", "wtf seriously?", "really, again?", "are you serious right now?",
            "you must be joking, right?", "stop messing with me, seriously", "this is too much, come on!", "give me a rest",
            "hmmm, what's going on?", "um, seriously, what's up?", "ok, seriously, what's the deal?", "lmao what's going on here?",
            "damn, wtf is this?", "ok, seriously, really?", "what, again?", "wtf, what's happening?", "yo seriously what's up?",
            "hmmm, what was that?", "seriously, bro, really?", "again? are you kidding me?", "give me a break",
            "wtf is this???", "ok damn seriously?", "are you fr? what just happened?", "bro seriously what's wrong with you?",
            "you're kidding me, right?", "cmon this is too much!", "really, again? why??", "are you joking??",
            "um whats going on?", "ok seriously give me a rest", "what just happened lmao"};
    private static final String[] FAILSAFE_CONTINUE_MESSAGES = new String[]{
            "can i keep farming?", "lemme farm ok?", "leave me alone next time ok?", "leave me alone lol", "let me farm",
            "nice one admin", "hello admin???", "hello admin", "bro let me farm", "bro let me farm ok?", "bro let me farm pls",
            "dude leave me alone", "dude tf was that", "hey can i just do my thing?", "can i farm in peace please?",
            "let me farm bro seriously", "admin can i keep farming?", "hey admin leave me alone this time alright?",
            "admin seriously let me farm in peace", "admin bro let me farm okay?", "admin let me do my farming thing please",
            "can i keep doing my thing admin?", "bro seriously let me farm in peace", "admin nice one but can i still farm?",
            "hello admin can i keep farming?", "admin let me do my farm routine okay?", "admin seriously can i farm in peace?",
            "can i continue farming?", "admin let me farm in peace please", "can i keep farming admin? pretty please?",
            "admin seriously let me farm in peace ok?", "can i continue my farming admin?", "admin let me farm please and thank you",
            "leave me alone this time alright?", "seriously let me farm in peace", "bro let me farm okay?",
            "let me do my farming thing please", "can i keep doing my thing?", "nice one but can i still farm?",
            "hello can i keep farming?", "let me do my farm routine okay?", "seriously can i farm in peace?", "let me farm in peace please",
            "can i keep farming? pretty please?", "seriously let me farm in peace alright?", "can i continue my farming?",
            "let me farm please and thank you", "let me farm and dont interrupt me please", "let me farm dude seriously",
            "admin dude let me farm okay?", "dude seriously let me farm in peace", "dude let me farm okay?"};

    @Getter
    @Setter
    private boolean hadEmergency = false;

    public FailsafeManager() {
        failsafes.addAll(
                Arrays.asList(
                        BanwaveFailsafe.getInstance(),
                        BedrockCageFailsafe.getInstance(),
                        DirtFailsafe.getInstance(),
                        DisconnectFailsafe.getInstance(),
                        EvacuateFailsafe.getInstance(),
                        GuestVisitFailsafe.getInstance(),
                        ItemChangeFailsafe.getInstance(),
                        JacobFailsafe.getInstance(),
                        LowerAvgBpsFailsafe.getInstance(),
                        RotationFailsafe.getInstance(),
                        TeleportFailsafe.getInstance(),
                        WorldChangeFailsafe.getInstance()
                )
        );
    }

    public void stopFailsafes() {
        triggeredFailsafe = Optional.empty();
        emergencyQueue.clear();
        sendingFailsafeInfo = false;
        swapItemDuringRecording = false;
        chooseEmergencyDelay.reset();
        onTickDelay.reset();
        failsafes.forEach(Failsafe::resetStates);
    }

    public void resetAfterMacroDisable() {
        stopFailsafes();
        restartMacroAfterFailsafeDelay.reset();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onReceivedPacketDetection(ReceivePacketEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (!MacroHandler.getInstance().isMacroToggled()) return;
        if (triggeredFailsafe.isPresent()) {
            if (triggeredFailsafe.get().equals(BedrockCageFailsafe.getInstance())) {
                BedrockCageFailsafe.getInstance().onReceivedPacketDetection(event);
            }
            return;
        }
        if (FeatureManager.getInstance().shouldIgnoreFalseCheck()) return;

        failsafes.forEach(failsafe -> failsafe.onReceivedPacketDetection(event));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onTickDetection(TickEvent.ClientTickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (!MacroHandler.getInstance().isMacroToggled()) return;
        if (triggeredFailsafe.isPresent()) return;
        if (FeatureManager.getInstance().shouldIgnoreFalseCheck()) return;

        failsafes.forEach(failsafe -> failsafe.onTickDetection(event));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onChatDetection(ClientChatReceivedEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (event.type != 0) return;
        if (event.message == null) return;
        if (!MacroHandler.getInstance().isMacroToggled()) return;
        if (triggeredFailsafe.isPresent()) return;
        if (FeatureManager.getInstance().shouldIgnoreFalseCheck()) return;

        failsafes.forEach(failsafe -> failsafe.onChatDetection(event));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldUnloadDetection(WorldEvent.Unload event) {
        if (!MacroHandler.getInstance().isMacroToggled()) return;
        if (triggeredFailsafe.isPresent()) return;
        if (FeatureManager.getInstance().shouldIgnoreFalseCheck()) return;

        failsafes.forEach(failsafe -> failsafe.onWorldUnloadDetection(event));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onDisconnectDetection(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        if (!MacroHandler.getInstance().isMacroToggled()) return;
        if (triggeredFailsafe.isPresent()) return;
        if (FeatureManager.getInstance().shouldIgnoreFalseCheck()) return;

        failsafes.forEach(failsafe -> failsafe.onDisconnectDetection(event));
    }

    public void possibleDetection(Failsafe failsafe) {
        if (emergencyQueue.contains(failsafe)) return;

        MacroHandler.getInstance().getCurrentMacro().ifPresent(AbstractMacro::saveState);
        emergencyQueue.add(failsafe);
        if (!chooseEmergencyDelay.isScheduled())
            chooseEmergencyDelay.schedule(TaunahiConfig.failsafeStopDelay + (long) (Math.random() * 500));
        LogUtils.sendDebug("[Failsafe] Emergency added: " + failsafe.getType().name());
        LogUtils.sendWarning("[Failsafe] Probability of emergency: " + LogUtils.capitalize(failsafe.getType().name()));
        if (!sendingFailsafeInfo) {
            sendingFailsafeInfo = true;
            Multithreading.schedule(() -> {
                Failsafe tempFailsafe = getHighestPriorityEmergency();
                if (tempFailsafe.getType() == EmergencyType.NONE) {
                    // Should never happen, but yeh...
                    LogUtils.sendDebug("[Failsafe] No emergency chosen!");
                    stopFailsafes();
                    return;
                }

                if (TaunahiConfig.enableFailsafeSound && failsafe.shouldPlaySound()) {
                    AudioManager.getInstance().playSound();
                }

                if (TaunahiConfig.autoAltTab && failsafe.shouldAltTab()) {
                    FailsafeUtils.bringWindowToFront();
                }
                Multithreading.schedule(() -> {
                    if (TaunahiConfig.autoAltTab && failsafe.shouldAltTab() && !Display.isActive()) {
                        FailsafeUtils.bringWindowToFrontUsingRobot();
                        System.out.println("Bringing window to front using Robot because Winapi failed as usual.");
                    }
                }, 750, TimeUnit.MILLISECONDS);

                LogUtils.sendFailsafeMessage(tempFailsafe.getType().label, failsafe.shouldTagEveryone());
                if (failsafe.shouldSendNotification())
                    FailsafeUtils.getInstance().sendNotification(StringUtils.stripControlCodes(tempFailsafe.getType().label), TrayIcon.MessageType.WARNING);
            }, 800, TimeUnit.MILLISECONDS);
        }
    }

    private Failsafe getHighestPriorityEmergency() {
        Failsafe highestPriority = emergencyQueue.get(0);
        for (Failsafe emergencyType : emergencyQueue) {
            if (emergencyType.getPriority() < highestPriority.getPriority()) {
                highestPriority = emergencyType;
            }
        }
        return highestPriority;
    }

    @SubscribeEvent
    public void onTickChooseEmergency(TickEvent.ClientTickEvent event) {
        if (!MacroHandler.getInstance().isMacroToggled()) return;
        if (triggeredFailsafe.isPresent()) return;
        if (!chooseEmergencyDelay.isScheduled()) return;
        if (chooseEmergencyDelay.passed()) {
            triggeredFailsafe = Optional.of(getHighestPriorityEmergency());
            if (triggeredFailsafe.get().getType() == EmergencyType.NONE) {
                // Should never happen, but yeh...
                LogUtils.sendDebug("[Failsafe] No emergency chosen!");
                stopFailsafes();
                return;
            }
            emergencyQueue.clear();
            chooseEmergencyDelay.reset();
            hadEmergency = true;
            LogUtils.sendDebug("[Failsafe] Emergency chosen: " + StringUtils.stripControlCodes(triggeredFailsafe.get().getType().name()));
            FeatureManager.getInstance().disableCurrentlyRunning(null);
            if (!Scheduler.getInstance().isFarming()) {
                Scheduler.getInstance().farmingTime();
            }
        }
    }

    @SubscribeEvent
    public void onFailsafeTriggered(TickEvent.ClientTickEvent event) {
        if (!triggeredFailsafe.isPresent()) {
            return;
        }
        if (onTickDelay.isScheduled() && !onTickDelay.passed()) {
            return;
        }
        triggeredFailsafe.get().duringFailsafeTrigger();
    }

    @SubscribeEvent
    public void onTickRestartMacro(TickEvent.ClientTickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (!restartMacroAfterFailsafeDelay.isScheduled()) return;

        if (restartMacroAfterFailsafeDelay.passed()) {
            if (mc.currentScreen != null) {
                PlayerUtils.closeScreen();
            }
            if (TaunahiConfig.alwaysTeleportToGarden) {
                MacroHandler.getInstance().getCurrentMacro().ifPresent(AbstractMacro::triggerWarpGarden);
            }
            restartMacroAfterFailsafeDelay.reset();
            Multithreading.schedule(() -> {
                LogUtils.sendDebug("[Failsafe] Restarting the macro...");
                MacroHandler.getInstance().enableMacro();
                FailsafeManager.getInstance().setHadEmergency(false);
                FailsafeManager.getInstance().getRestartMacroAfterFailsafeDelay().reset();
            }, TaunahiConfig.alwaysTeleportToGarden ? 1_500 : 0, TimeUnit.MILLISECONDS);
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;

        if (chooseEmergencyDelay.isScheduled()) {
            String text = "Failsafe in: " + LogUtils.formatTime(chooseEmergencyDelay.getRemainingTime());
            RenderUtils.drawCenterTopText(text, event, Color.MAGENTA);
        } else if (restartMacroAfterFailsafeDelay.isScheduled()) {
            String text = "Restarting the macro in: " + LogUtils.formatTime(restartMacroAfterFailsafeDelay.getRemainingTime());
            RenderUtils.drawCenterTopText(text, event, Color.ORANGE);
        } else if (triggeredFailsafe.isPresent() && !triggeredFailsafe.get().equals(GuestVisitFailsafe.getInstance())) {
            ArrayList<String> textLines = new ArrayList<>();
            textLines.add("§6" + StringUtils.stripControlCodes(triggeredFailsafe.get().getType().name()).replace("_", " "));
            textLines.add("§c§lYOU ARE DURING STAFF CHECK!");
            textLines.add("§cPRESS §6" + TaunahiConfig.toggleMacro.getDisplay() + "§c TO DISABLE THE MACRO");
            textLines.add("§cDO §6§lNOT §cLEAVE! REACT!");
            RenderUtils.drawMultiLineText(textLines, event, Color.MAGENTA, 2f);
        }
    }

    public void restartMacroAfterDelay() {
        if (TaunahiConfig.enableRestartAfterFailSafe) {
            MacroHandler.getInstance().pauseMacro();
            Multithreading.schedule(() -> {
                InventoryUtils.openInventory();
                LogUtils.sendDebug("[Failsafe] Finished " + (triggeredFailsafe.map(failsafe -> (failsafe.getType().label + " ")).orElse("")) + "failsafe");
                if (TaunahiConfig.enableRestartAfterFailSafe) {
                    if (TaunahiConfig.restartAfterFailSafeDelay > 0) {
                        LogUtils.sendDebug("[Failsafe] Restarting the macro in " + TaunahiConfig.restartAfterFailSafeDelay + " minutes.");
                        restartMacroAfterFailsafeDelay.schedule(TaunahiConfig.restartAfterFailSafeDelay * 1_000L * 60L);
                    } else {
                        LogUtils.sendDebug("[Failsafe] Restarting the macro soon...");
                        restartMacroAfterFailsafeDelay.schedule(2000L + Math.random() * 1000L);
                    }
                }
            }, (long) (1_500 + Math.random() * 1_000), TimeUnit.MILLISECONDS);
        } else {
            MacroHandler.getInstance().disableMacro();
        }
    }

    public boolean firstCheckReturn() {
        if (mc.thePlayer == null || mc.theWorld == null) return true;
        if (!MacroHandler.getInstance().isMacroToggled()) return true;
        if (FailsafeManager.getInstance().triggeredFailsafe.isPresent()) return true;
        return FeatureManager.getInstance().shouldIgnoreFalseCheck();
    }

    public void scheduleDelay(long delay) {
        onTickDelay.schedule(delay);
    }

    public void scheduleRandomDelay(long minDelay, long maxAdditionalDelay) {
        onTickDelay.schedule(minDelay + Math.random() * maxAdditionalDelay);
    }

    public static String getRandomMessage(String[] messages) {
        if (messages.length > 1) {
            return messages[(int) (Math.random() * (messages.length - 1))];
        } else {
            return messages[0];
        }
    }

    public static String getRandomMessage() {
        return getRandomMessage(FAILSAFE_MESSAGES);
    }

    public static String getRandomContinueMessage() {
        return getRandomMessage(FAILSAFE_CONTINUE_MESSAGES);
    }

    public void selectNextItemSlot() {
        int nextSlot = mc.thePlayer.inventory.currentItem + 1;
        if (nextSlot > 7) {
            nextSlot = 0;
        }
        if (mc.thePlayer.inventory.currentItem != nextSlot) {
            mc.thePlayer.inventory.currentItem = nextSlot;
        }
    }

    public enum EmergencyType {
        NONE(""),
        ROTATION_CHECK("You've got§l ROTATED§r§d by staff member!"),
        TELEPORT_CHECK("You've got§l TELEPORTED§r§d by staff member!"),
        DIRT_CHECK("You've got§l DIRT CHECKED§r§d by staff member!"),
        ITEM_CHANGE_CHECK("Your §lITEM HAS CHANGED§r§d!"),
        WORLD_CHANGE_CHECK("Your §lWORLD HAS CHANGED§r§d!"),
        BEDROCK_CAGE_CHECK("You've got§l BEDROCK CAGED§r§d by staff member!"),
        EVACUATE("Server is restarting! Evacuate!"),
        BANWAVE("Banwave has been detected!"),
        DISCONNECT("You've been§l DISCONNECTED§r§d from the server!"),
        LOWER_AVERAGE_BPS("Your BPS is lower than average!"),
        JACOB("You've extended the §lJACOB COUNTER§r§d!"),
        GUEST_VISIT("You've got§l VISITED§r§d by "
                + (!GuestVisitFailsafe.getInstance().lastGuestName.isEmpty() ? GuestVisitFailsafe.getInstance().lastGuestName : "a guest") + "!");

        final String label;

        EmergencyType(String s) {
            label = s;
        }
    }
}