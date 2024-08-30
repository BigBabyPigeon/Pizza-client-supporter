package net.taunahi.ezskyblockscripts.config;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.Number;
import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.core.OneKeyBind;
import cc.polyfrost.oneconfig.config.data.*;
import net.taunahi.ezskyblockscripts.Taunahi;
import net.taunahi.ezskyblockscripts.config.page.AutoSellNPCItemsPage;
import net.taunahi.ezskyblockscripts.config.page.CustomFailsafeMessagesPage;
import net.taunahi.ezskyblockscripts.config.page.FailsafeNotificationsPage;
import net.taunahi.ezskyblockscripts.config.struct.Rewarp;
import net.taunahi.ezskyblockscripts.failsafe.FailsafeManager;
import net.taunahi.ezskyblockscripts.feature.impl.*;
import net.taunahi.ezskyblockscripts.handler.GameStateHandler;
import net.taunahi.ezskyblockscripts.handler.MacroHandler;
import net.taunahi.ezskyblockscripts.hud.DebugHUD;
import net.taunahi.ezskyblockscripts.hud.ProfitCalculatorHUD;
import net.taunahi.ezskyblockscripts.hud.StatusHUD;
import net.taunahi.ezskyblockscripts.util.BlockUtils;
import net.taunahi.ezskyblockscripts.util.LogUtils;
import net.taunahi.ezskyblockscripts.util.PlayerUtils;
import net.taunahi.ezskyblockscripts.util.helper.AudioManager;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Loader;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

// THIS IS RAT - CatalizCS
@SuppressWarnings({"unused", "DefaultAnnotationParam"})
public class TaunahiConfig extends Config {
    private transient static final Minecraft mc = Minecraft.getMinecraft();
    private transient static final String GENERAL = "General";
    private transient static final String MISCELLANEOUS = "Miscellaneous";
    private transient static final String FAILSAFE = "Failsafe";
    private transient static final String SCHEDULER = "Scheduler";
    private transient static final String JACOBS_CONTEST = "Jacob's Contest";
    private transient static final String VISITORS_MACRO = "Visitors Macro";
    private transient static final String PESTS_DESTROYER = "Pests Destroyer";
    private transient static final String DISCORD_INTEGRATION = "Discord Integration";
    private transient static final String DELAYS = "Delays";
    private transient static final String HUD = "HUD";
    private transient static final String DEBUG = "Debug";
    private transient static final String EXPERIMENTAL = "Experimental";

    private transient static final File configRewarpFile = new File("taunahi_rewarp.json");


    public static List<Rewarp> rewarpList = new ArrayList<>();

    //<editor-fold desc="PROXY">
    public static boolean proxyEnabled = false;
    public static String proxyAddress = "";
    public static String proxyUsername = "";
    public static String proxyPassword = "";
    public static Proxy.ProxyType proxyType = Proxy.ProxyType.HTTP;
    //</editor-fold>

    //<editor-fold desc="GENERAL">
    @Dropdown(
            name = "Macro Type", category = GENERAL,
            description = "Farm Types",
            options = {
                    "S Shape / Vertical - Crops (Wheat, Carrot, Potato, NW)", // 0
                    "S Shape - Pumpkin/Melon", // 1
                    "S Shape - Pumpkin/Melon Melongkingde", // 2
                    "S Shape - Pumpkin/Melon Default Plot", // 3
                    "S Shape - Sugar Cane", // 4
                    "S Shape - Cactus", // 5
                    "S Shape - Cactus SunTzu Black Cat", // 6
                    "S Shape - Cocoa Beans", // 7
                    "S Shape - Cocoa Beans (Left/Right)", // 8
                    "S Shape - Mushroom (45°)", // 9
                    "S Shape - Mushroom (30° with rotations)", // 10
                    "S Shape - Mushroom SDS" // 11
            }, size = 2
    )
    public static int macroType = 0;

    @Switch(
            name = "Always hold W while farming", category = GENERAL,
            description = "Always hold W while farming"
    )
    public static boolean alwaysHoldW = false;

    //<editor-fold desc="Rotation">
    @Switch(
            name = "Rotate After Warped", category = GENERAL, subcategory = "Rotation",
            description = "Rotates the player after re-warping", size = 1
    )
    public static boolean rotateAfterWarped = false;
    @Switch(
            name = "Rotate After Drop", category = GENERAL, subcategory = "Rotation",
            description = "Rotates after the player falls down", size = 1
    )
    public static boolean rotateAfterDrop = false;
    @Switch(
            name = "Don't fix micro rotations after warp", category = GENERAL, subcategory = "Rotation",
            description = "The macro doesn't do micro-rotations after rewarp if the current yaw and target yaw are the same", size = 2
    )
    public static boolean dontFixAfterWarping = false;
    @Switch(
            name = "Custom Pitch", category = GENERAL, subcategory = "Rotation",
            description = "Set pitch to custom level after starting the macro"
    )
    public static boolean customPitch = false;
    @Number(
            name = "Custom Pitch Level", category = GENERAL, subcategory = "Rotation",
            description = "Set custom pitch level after starting the macro",
            min = -90.0F, max = 90.0F
    )
    public static float customPitchLevel = 0;

    @Switch(
            name = "Custom Yaw", category = GENERAL, subcategory = "Rotation",
            description = "Set yaw to custom level after starting the macro"
    )
    public static boolean customYaw = false;

    @Number(
            name = "Custom Yaw Level", category = GENERAL, subcategory = "Rotation",
            description = "Set custom yaw level after starting the macro",
            min = -180.0F, max = 180.0F
    )
    public static float customYawLevel = 0;
    //</editor-fold>

    //<editor-fold desc="Rewarp">
    @Switch(
            name = "Highlight rewarp points", category = GENERAL, subcategory = "Rewarp",
            description = "Highlights all rewarp points you have added",
            size = OptionSize.DUAL
    )
    public static boolean highlightRewarp = true;
    @Info(
            text = "Don't forget to add rewarp points!",
            type = InfoType.WARNING,
            category = GENERAL,
            subcategory = "Rewarp"
    )
    public static boolean rewarpWarning;

    @Button(
            name = "Add Rewarp", category = GENERAL, subcategory = "Rewarp",
            description = "Adds a rewarp position",
            text = "Add Rewarp"
    )
    Runnable _addRewarp = TaunahiConfig::addRewarp;
    @Button(
            name = "Remove Rewarp", category = GENERAL, subcategory = "Rewarp",
            description = "Removes a rewarp position",
            text = "Remove Rewarp"
    )
    Runnable _removeRewarp = TaunahiConfig::removeRewarp;
    @Button(
            name = "Remove All Rewarps", category = GENERAL, subcategory = "Rewarp",
            description = "Removes all rewarp positions",
            text = "Remove All Rewarps"
    )
    Runnable _removeAllRewarps = TaunahiConfig::removeAllRewarps;
    //</editor-fold>

    //<editor-fold desc="Spawn">
    @Number(
            name = "SpawnPos X", category = GENERAL, subcategory = "Spawn Position",
            description = "The X coordinate of the spawn",
            min = -30000000, max = 30000000

    )
    public static int spawnPosX = 0;
    @Number(
            name = "SpawnPos Y", category = GENERAL, subcategory = "Spawn Position",
            description = "The Y coordinate of the spawn",
            min = -30000000, max = 30000000
    )
    public static int spawnPosY = 0;
    @Number(
            name = "SpawnPos Z", category = GENERAL, subcategory = "Spawn Position",
            description = "The Z coordinate of the spawn",
            min = -30000000, max = 30000000
    )
    public static int spawnPosZ = 0;

    @Button(
            name = "Set SpawnPos", category = GENERAL, subcategory = "Spawn Position",
            description = "Sets the spawn position to your current position",
            text = "Set SpawnPos"
    )
    Runnable _setSpawnPos = PlayerUtils::setSpawnLocation;
    @Button(
            name = "Reset SpawnPos", category = GENERAL, subcategory = "Spawn Position",
            description = "Resets the spawn position",
            text = "Reset SpawnPos"
    )
    Runnable _resetSpawnPos = () -> {
        spawnPosX = 0;
        spawnPosY = 0;
        spawnPosZ = 0;
        save();
        LogUtils.sendSuccess("Spawn position has been reset!");
    };

    @Switch(
            name = "Draw spawn location", category = GENERAL, subcategory = "Drawings",
            description = "Draws the spawn location"
    )
    public static boolean drawSpawnLocation = true;
    //</editor-fold>

    //</editor-fold>

    //<editor-fold desc="MISC">
    //<editor-fold desc="Keybinds">
    @KeyBind(
            name = "Toggle Farm Helper", category = MISCELLANEOUS, subcategory = "Keybinds",
            description = "Toggles the macro on/off", size = 2
    )
    public static OneKeyBind toggleMacro = new OneKeyBind(Keyboard.KEY_GRAVE);
    @KeyBind(
            name = "Open GUI", category = MISCELLANEOUS, subcategory = "Keybinds",
            description = "Opens Farm Helper configuration menu", size = 2
    )

    public static OneKeyBind openGuiKeybind = new OneKeyBind(Keyboard.KEY_F);
    @KeyBind(
            name = "Freelook", category = MISCELLANEOUS, subcategory = "Keybinds",
            description = "Locks rotation, lets you freely look", size = 2
    )
    public static OneKeyBind freelookKeybind = new OneKeyBind(Keyboard.KEY_L);

    @Info(
            text = "Freelook doesn't work properly with Oringo!", type = InfoType.WARNING,
            category = MISCELLANEOUS, subcategory = "Keybinds"
    )
    private int freelookWarning;
    //</editor-fold>

    //<editor-fold desc="Plot Cleaning Helper">
    @KeyBind(
            name = "Plot Cleaning Helper", category = MISCELLANEOUS, subcategory = "Plot Cleaning Helper",
            description = "Toggles the plot cleaning helper on/off", size = 2
    )
    public static OneKeyBind plotCleaningHelperKeybind = new OneKeyBind(Keyboard.KEY_P);
    @Switch(
            name = "Automatically choose a tool to destroy the block", category = MISCELLANEOUS, subcategory = "Plot Cleaning Helper",
            description = "Automatically chooses the best tool to destroy the block"
    )
    public static boolean autoChooseTool = false;
    //</editor-fold>

    //<editor-fold desc="Miscellaneous">
    @DualOption(
            name = "AutoUpdater Version Type", category = MISCELLANEOUS, subcategory = "Miscellaneous",
            description = "The version type to use",
            left = "Release",
            right = "Pre-release",
            size = 2
    )
    public static boolean autoUpdaterDownloadBetaVersions = false;
    @Switch(
            name = "Performance Mode", category = MISCELLANEOUS, subcategory = "Miscellaneous",
            description = "Set render distance to 2, set max fps to 15 and doesn't render crops"
    )
    public static boolean performanceMode = false;
    @Number(
            name = "Max FPS", category = MISCELLANEOUS, subcategory = "Miscellaneous",
            description = "The maximum FPS to set when performance mode is enabled",
            min = 10, max = 60
    )
    public static int performanceModeMaxFPS = 20;
    @Switch(
            name = "Mute The Game", category = MISCELLANEOUS, subcategory = "Miscellaneous",
            description = "Mutes the game while farming"
    )
    public static boolean muteTheGame = false;
    @Switch(
            name = "Auto Cookie", category = MISCELLANEOUS, subcategory = "Miscellaneous",
            description = "Automatically purchases and consumes a booster cookie"
    )
    public static boolean autoCookie = false;
    @Switch(
            name = "Hold left click when changing row", category = MISCELLANEOUS, subcategory = "Miscellaneous",
            description = "Hold left click when change row"
    )
    public static boolean holdLeftClickWhenChangingRow = true;

    @Switch(
            name = "Auto Ungrab Mouse", category = MISCELLANEOUS, subcategory = "Miscellaneous",
            description = "Automatically ungrabs your mouse, so you can safely alt-tab"
    )
    public static boolean autoUngrabMouse = true;
    //</editor-fold>

    //<editor-fold desc="God Pot">
    @Switch(
            name = "Auto God Pot", category = MISCELLANEOUS, subcategory = "God Pot",
            description = "Automatically purchases and consumes a God Pot", size = 2
    )
    public static boolean autoGodPot = false;

    @Switch(
            name = "Get God Pot from Backpack", category = MISCELLANEOUS, subcategory = "God Pot", size = 2
    )
    public static boolean autoGodPotFromBackpack = true;

    @DualOption(
            name = "Storage Type", category = MISCELLANEOUS, subcategory = "God Pot",
            description = "The storage type to get god pots from",
            left = "Backpack",
            right = "Ender Chest"
    )
    public static boolean autoGodPotStorageType = true;

    @Number(
            name = "Backpack Number", category = MISCELLANEOUS, subcategory = "God Pot",
            description = "The backpack number, that contains god pots",
            min = 1, max = 18
    )
    public static int autoGodPotBackpackNumber = 1;

    @Switch(
            name = "Buy God Pot using Bits", category = MISCELLANEOUS, subcategory = "God Pot"
    )
    public static boolean autoGodPotFromBits = false;

    @Switch(
            name = "Get God Pot from Auction House", category = MISCELLANEOUS, subcategory = "God Pot",
            description = "If the user doesn't have a cookie, it will go to the hub and buy from AH"
    )
    public static boolean autoGodPotFromAH = false;

    @Info(
            text = "Priority getting God Pot is: Backpack -> Bits -> AH",
            type = InfoType.INFO, size = 2, category = MISCELLANEOUS, subcategory = "God Pot"
    )
    private static int godPotInfo;

    //</editor-fold>

    //<editor-fold desc="Auto Sell">
    @Info(
            text = "Click ESC during Auto Sell, to stop it and pause for the next 15 minutes",
            category = MISCELLANEOUS, subcategory = "Auto Sell", type = InfoType.INFO, size = 2
    )
    public static boolean autoSellInfo;

    @Switch(
            name = "Enable Auto Sell", category = MISCELLANEOUS, subcategory = "Auto Sell",
            description = "Enables auto sell"
    )
    public static boolean enableAutoSell = false;

    @DualOption(
            name = "Market type", category = MISCELLANEOUS, subcategory = "Auto Sell",
            description = "The market type to sell crops to",
            left = "BZ",
            right = "NPC"
    )
    public static boolean autoSellMarketType = false;

    @Switch(
            name = "Sell Items In Sacks", category = MISCELLANEOUS, subcategory = "Auto Sell",
            description = "Sells items in your sacks and inventory"
    )
    public static boolean autoSellSacks = false;

    @DualOption(
            name = "Sacks placement",
            category = MISCELLANEOUS, subcategory = "Auto Sell",
            description = "The sacks placement",
            left = "Inventory",
            right = "Sack of sacks"
    )
    public static boolean autoSellSacksPlacement = true;

    @Number(
            name = "Inventory Full Time", category = MISCELLANEOUS, subcategory = "Auto Sell",
            description = "The time to wait to test if inventory fullness ratio is still the same (or higher)",
            min = 1, max = 20
    )
    public static int inventoryFullTime = 6;

    @Number(
            name = "Inventory Full Ratio", category = MISCELLANEOUS, subcategory = "Auto Sell",
            description = "After reaching this ratio, the macro will start counting from 0 to Inventory Full Time. If the fullness ratio is still the same (or higher) after the time has passed, it will start selling items.",
            min = 1, max = 100
    )
    public static int inventoryFullRatio = 65;

    @Button(
            name = "Sell Inventory Now", category = MISCELLANEOUS, subcategory = "Auto Sell",
            description = "Sells crops in your inventory",
            text = "Sell Inventory Now"
    )
    Runnable autoSellFunction = () -> {
        PlayerUtils.closeScreen();
        AutoSell.getInstance().enable(true);
    };

    @Page(
            name = "Customize items sold to NPC", category = MISCELLANEOUS, subcategory = "Auto Sell", location = PageLocation.BOTTOM,
            description = "Click here to customize items that are sold to NPC automatically"
    )
    public AutoSellNPCItemsPage autoSellNPCItemsPage = new AutoSellNPCItemsPage();
    //</editor-fold>

    //<editor-fold desc="Pest Repellant">
    @Switch(
            name = "Auto Pest Repellent", category = MISCELLANEOUS, subcategory = "Pest Repellent",
            description = "Automatically uses pest repellent when it's not active"
    )
    public static boolean autoPestRepellent = false;

    @DualOption(
            name = "Pest Repellent Type", category = MISCELLANEOUS, subcategory = "Pest Repellent",
            description = "The pest repellent type to use",
            left = "Pest Repellent",
            right = "Pest Repellent MAX"
    )
    public static boolean pestRepellentType = true;

    @Button(
            name = "Reset Failsafe", category = MISCELLANEOUS, subcategory = "Pest Repellent",
            text = "Click Here",
            description = "Resets the failsafe timer for repellent"
    )
    Runnable resetFailsafe = () -> {
        AutoRepellent.repellentFailsafeClock.schedule(0);
    };
    //</editor-fold>

    //<editor-fold desc="Pet Swapper">
    @Switch(
            name = "Swap pet during Jacob's contest", category = MISCELLANEOUS, subcategory = "Pet Swapper",
            description = "Swaps pet to the selected pet during Jacob's contest. Selects the first one from the pet list."
    )
    public static boolean enablePetSwapper = false;

    @Slider(
            name = "Pet Swap Delay", category = MISCELLANEOUS, subcategory = "Pet Swapper",
            description = "The delay between clicking GUI during swapping the pet (in milliseconds)",
            min = 200, max = 3000
    )
    public static int petSwapperDelay = 1000;
    @Text(
            name = "Pet Name", placeholder = "Type your pet name here",
            category = MISCELLANEOUS, subcategory = "Pet Swapper"
    )
    public static String petSwapperName = null;
    //</editor-fold>

    //<editor-fold desc="Crop Utils">
    @Switch(
            name = "Increase Cocoa Hitboxes", category = MISCELLANEOUS, subcategory = "Crop Utils",
            description = "Allows you to farm cocoa beans more efficiently at higher speeds by making the hitboxes bigger"
    )
    public static boolean increasedCocoaBeans = true;

    @Switch(
            name = "Increase Crop Hitboxes", category = MISCELLANEOUS, subcategory = "Crop Utils",
            description = "Allows you to farm crops more efficient by making the hitboxes bigger"
    )
    public static boolean increasedCrops = true;

    @Switch(
            name = "Increase Nether Wart Hitboxes", category = MISCELLANEOUS, subcategory = "Crop Utils",
            description = "Allows you to farm nether warts more efficiently at higher speeds by making the hitboxes bigger"
    )
    public static boolean increasedNetherWarts = true;

    @Switch(
            name = "Increase Mushroom Hitboxes", category = MISCELLANEOUS, subcategory = "Crop Utils",
            description = "Allows you to farm mushrooms more efficiently at higher speeds by making the hitboxes bigger"
    )
    public static boolean increasedMushrooms = true;

    @Switch(
            name = "Pingless Cactus", category = MISCELLANEOUS, subcategory = "Crop Utils",
            description = "Allows you to farm cactus more efficiently at higher speeds by making the cactus pingless"
    )
    public static boolean pinglessCactus = true;
    //</editor-fold>

    //<editor-fold desc="Analytics">
    @Switch(
            name = "Send analytic data", category = MISCELLANEOUS, subcategory = "Analytics",
            description = "Sends analytic data to the server to improve the macro and learn how to detect staff checks"
    )
    public static boolean sendAnalyticData = true;
    //</editor-fold>

    //<editor-fold desc="Auto Sprayonator">
    @Switch(
            name = "Auto Sprayonator", category = MISCELLANEOUS, subcategory = "AutoSprayonator"
    )
    public static boolean autoSprayonatorEnable = false;

    @Dropdown(
            name = "Type", category = MISCELLANEOUS, subcategory = "AutoSprayonator",
            description = "Item to spray plot with",
            options = {
                    "Compost (Earthworm & Mosquito)",
                    "Honey Jar (Moth & Cricket)",
                    "Dung (Beetle & Fly)",
                    "Plant Matter (Locust & Slug)",
                    "Tasty Cheese (Rat & Mite)"
            }, size = 5
    )
    public static int sprayonatorType;

    @Getter
    public enum SPRAYONATOR_ITEM {
        COMPOST("Compost"),
        HONEY_JAR("Honey Jar"),
        DUNG("Dung"),
        PLANT_MATTER("Plant Matter"),
        TASTY_CHEESE("Tasty Cheese"),
        NONE("NONE");

        final String itemName;

        SPRAYONATOR_ITEM(final String item_name) {
            this.itemName = item_name;
        }
    }

    @Switch(
            name = "Inventory Only", category = MISCELLANEOUS, subcategory = "Sprayonator"
    )
    public static boolean sprayonatorItemInventoryOnly;

    @Slider(
            name = "Sprayonator Slot", category = MISCELLANEOUS, subcategory = "AutoSprayonator",
            min = 1, max = 8,
            step = 1,
            description = "Slot to move sprayonator to"
    )
    public static int autoSprayonatorSlot = 1;

    @Slider(
            name = "Additional Delay", category = MISCELLANEOUS, subcategory = "AutoSprayonator",
            description = "Additional delay between actions (in milliseconds)",
            min = 0, max = 5000, step = 1
    )
    public static int autoSprayonatorAdditionalDelay = 500;

    @Switch(
            name = "Auto Buy item from Bazaar", category = MISCELLANEOUS, subcategory = "AutoSprayonator",
            description = "Auto buy necessary sprayonator item from bazaar if none is in the inventory"
    )
    public static boolean autoSprayonatorAutoBuyItem = false;

    @Number(
            name = "Buy Amount", category = MISCELLANEOUS, subcategory = "AutoSprayonator",
            description = "Amount of item to buy from bazaar",
            min = 1, max = 64
    )
    public static int autoSprayonatorAutoBuyAmount = 1;

    @Button(
            name = "Reset Plots", category = MISCELLANEOUS, subcategory = "AutoSprayonator",
            text = "Click Here",
            description = "Resets the cached data for sprayonator"
    )
    Runnable _autoSprayonatorResetPlots = () -> {
        AutoSprayonator.getInstance().resetPlots();
    };

    //</editor-fold>

    //</editor-fold>

    //<editor-fold desc="FAILSAFES">
    //<editor-fold desc="Failsafe Misc">
    @Switch(
            name = "Pop-up Notification", category = FAILSAFE, subcategory = "Miscellaneous",
            description = "Enable pop-up notification"
    )
    public static boolean popUpNotification = true;
    @Switch(
            name = "Auto alt-tab when failsafe triggered", category = FAILSAFE, subcategory = "Miscellaneous",
            description = "Automatically alt-tabs to the game when the dark times come"
    )
    public static boolean autoAltTab = true;
    @Switch(
            name = "Try to use jumping and flying in failsafes reactions", category = FAILSAFE, subcategory = "Miscellaneous",
            description = "Tries to use jumping and flying in failsafes reactions"
    )
    public static boolean tryToUseJumpingAndFlying = true;
    @Slider(
            name = "Failsafe Stop Delay", category = FAILSAFE, subcategory = "Miscellaneous",
            description = "The delay to stop the macro after failsafe has been triggered (in milliseconds)",
            min = 1_000, max = 7_500
    )
    public static int failsafeStopDelay = 2_000;
    @Switch(
            name = "Auto TP back on World Change", category = FAILSAFE, subcategory = "Miscellaneous",
            description = "Automatically warps back to the garden on server reboot, server update, etc"
    )
    public static boolean autoTPOnWorldChange = true;
    @Switch(
            name = "Auto Evacuate on World update", category = FAILSAFE, subcategory = "Miscellaneous",
            description = "Automatically evacuates the island on server reboot, server update, etc"
    )
    public static boolean autoEvacuateOnWorldUpdate = true;
    @Switch(
            name = "Auto reconnect on disconnect", category = FAILSAFE, subcategory = "Miscellaneous",
            description = "Automatically reconnects to the server when disconnected"
    )
    public static boolean autoReconnect = true;
    @Switch(
            name = "Pause the macro when a guest arrives", category = FAILSAFE, subcategory = "Miscellaneous",
            description = "Pauses the macro when a guest arrives"
    )
    public static boolean pauseWhenGuestArrives = false;
    @Slider(
            name = "Teleport Check Lag Sensitivity", category = FAILSAFE, subcategory = "Miscellaneous",
            description = "Variation in distance between expected and actual positions when lagging",
            min = 0, max = 2
    )
    public static float teleportCheckLagSensitivity = 0.5f;
    @Slider(
            name = "Rotation Check Sensitivity", category = FAILSAFE, subcategory = "Miscellaneous",
            description = "The sensitivity of the rotation check; the lower the sensitivity, the more accurate the check is, but it will also increase the chance of getting false positives.",
            min = 1, max = 10
    )
    public static float rotationCheckSensitivity = 2;
    @Slider(
            name = "Teleport Check Sensitivity", category = FAILSAFE, subcategory = "Miscellaneous",
            description = "The minimum distance between the previous and teleported position to trigger failsafe",
            min = 0.5f, max = 20f
    )
    public static float teleportCheckSensitivity = 4;

    @Switch(
            name = "Average BPS Drop check", category = FAILSAFE, subcategory = "Miscellaneous",
            description = "Checks for average BPS drop"
    )
    public static boolean averageBPSDropCheck = true;

    @Slider(
            name = "Average BPS Drop %", category = FAILSAFE, subcategory = "Miscellaneous",
            description = "The minimum BPS drop to trigger failsafe",
            min = 2, max = 50
    )
    public static int averageBPSDrop = 15;

    @Button(
            name = "Test failsafe", category = FAILSAFE, subcategory = "Miscellaneous",
            description = "Tests failsafe",
            text = "Test failsafe"
    )
    Runnable _testFailsafe = () -> {
        if (!MacroHandler.getInstance().isMacroToggled()) {
            LogUtils.sendError("You need to start the macro first!");
            return;
        }
        LogUtils.sendWarning("Testing failsafe...");
        PlayerUtils.closeScreen();
        if (testFailsafeTypeSelected == 0)
            FailsafeManager.getInstance().possibleDetection(FailsafeManager.getInstance().failsafes.get(testFailsafeTypeSelected));
        else
            FailsafeManager.getInstance().possibleDetection(FailsafeManager.getInstance().failsafes.get(testFailsafeTypeSelected + 2));
    };

    @Dropdown(
            name = "Test Failsafe Type", category = FAILSAFE, subcategory = "Miscellaneous",
            description = "The failsafe type to test",
            options = {
                    "Banwave",
//                    "Bedrock Cage Check",
//                    "Dirt Check",
                    "Disconnect",
                    "Evacuate",
                    "Guest Visit",
                    "Item Change Check",
                    "Jacob",
                    "Lower Average Bps",
                    "Rotation Check",
                    "Teleport Check",
                    "World Change Check"
            }
    )
    public static int testFailsafeTypeSelected = 0;

    //</editor-fold>

    //<editor-fold desc="Failsafes conf page">
    @Page(
            name = "Failsafe Notifications", category = FAILSAFE, subcategory = "Failsafe Notifications", location = PageLocation.BOTTOM,
            description = "Click here to customize failsafe notifications"
    )
    public FailsafeNotificationsPage failsafeNotificationsPage = new FailsafeNotificationsPage();
    //</editor-fold>

    //<editor-fold desc="Desync">
    @Switch(
            name = "Check Desync", category = FAILSAFE, subcategory = "Desync",
            description = "If client desynchronization is detected, it activates a failsafe. Turn this off if the network is weak or if it happens frequently."
    )
    public static boolean checkDesync = true;
    @Slider(
            name = "Pause for X milliseconds after desync triggered", category = FAILSAFE, subcategory = "Desync",
            description = "The delay to pause after desync triggered (in milliseconds)",
            min = 3_000, max = 10_000
    )
    public static int desyncPauseDelay = 5_000;
    //</editor-fold>

    //<editor-fold desc="Failsafe Trigger Sound">
    @Switch(
            name = "Enable Failsafe Trigger Sound", category = FAILSAFE, subcategory = "Failsafe Trigger Sound", size = OptionSize.DUAL,
            description = "Makes a sound when a failsafe has been triggered"
    )
    public static boolean enableFailsafeSound = true;
    @DualOption(
            name = "Failsafe Sound Type", category = FAILSAFE, subcategory = "Failsafe Trigger Sound",
            description = "The failsafe sound type to play when a failsafe has been triggered",
            left = "Minecraft",
            right = "Custom"
    )
    public static boolean failsafeSoundType = false;
    @Dropdown(
            name = "Minecraft Sound", category = FAILSAFE, subcategory = "Failsafe Trigger Sound",
            description = "The Minecraft sound to play when a failsafe has been triggered",
            options = {
                    "Ping", // 0
                    "Anvil" // 1
            }
    )
    public static int failsafeMcSoundSelected = 1;

    @Dropdown(
            name = "Custom Sound", category = FAILSAFE, subcategory = "Failsafe Trigger Sound",
            description = "The custom sound to play when a failsafe has been triggered",
            options = {
                    "Custom", // 0
                    "Voice", // 1
                    "Metal Pipe", // 2
                    "AAAAAAAAAA", // 3
                    "Loud Buzz", // 4
            }
    )
    public static int failsafeSoundSelected = 1;
    @Number(
            name = "Number of times to play custom sound", category = FAILSAFE, subcategory = "Failsafe Trigger Sound",
            description = "The number of times to play custom sound when a failsafe has been triggered",
            min = 1, max = 10
    )
    public static int failsafeSoundTimes = 13;
    @Info(
            text = "If you want to use your own WAV file, rename it to 'taunahi_sound.wav' and put it in your Minecraft directory.",
            type = InfoType.WARNING,
            category = FAILSAFE,
            subcategory = "Failsafe Trigger Sound",
            size = 2
    )
    public static boolean customFailsafeSoundWarning;
    @Slider(
            name = "Failsafe Sound Volume (in %)", category = FAILSAFE, subcategory = "Failsafe Trigger Sound",
            description = "The volume of the failsafe sound",
            min = 0, max = 100
    )
    public static float failsafeSoundVolume = 50.0f;
    @Switch(
            name = "Max out Master category sounds while pinging", category = FAILSAFE, subcategory = "Failsafe Trigger Sound",
            description = "Maxes out the sounds while failsafe"
    )
    public static boolean maxOutMinecraftSounds = false;

    @Button(
            name = "", category = FAILSAFE, subcategory = "Failsafe Trigger Sound",
            description = "Plays the selected sound",
            text = "Play"
    )
    Runnable _playFailsafeSoundButton = () -> AudioManager.getInstance().playSound();
    @Button(
            name = "", category = FAILSAFE, subcategory = "Failsafe Trigger Sound",
            description = "Stops playing the selected sound",
            text = "Stop"
    )
    Runnable _stopFailsafeSoundButton = () -> AudioManager.getInstance().resetSound();

    //</editor-fold>

    //<editor-fold desc="Restart after failsafe">
    @Switch(
            name = "Enable Restart After FailSafe", category = FAILSAFE, subcategory = "Restart After FailSafe",
            description = "Restarts the macro after a while when a failsafe has been triggered"
    )
    public static boolean enableRestartAfterFailSafe = true;
    @Slider(
            name = "Restart Delay", category = FAILSAFE, subcategory = "Restart After FailSafe",
            description = "The delay to restart after failsafe (in minutes)",
            min = 0, max = 60
    )
    public static int restartAfterFailSafeDelay = 5;

    @Switch(
            name = "Always teleport to /warp garden after the failsafe",
            category = FAILSAFE, subcategory = "Restart After FailSafe",
            description = "Always teleports to /warp garden after the failsafe"
    )
    public static boolean alwaysTeleportToGarden = false;

    //</editor-fold>

    //<editor-fold desc="Banwave">
    @Switch(
            name = "Enable Banwave Checker", category = FAILSAFE, subcategory = "Banwave Checker",
            description = "Checks for banwave and shows you the number of players banned in the last 15 minutes",
            size = 2
    )
    public static boolean banwaveCheckerEnabled = true;
    @Switch(
            name = "Leave/pause during banwave", category = FAILSAFE, subcategory = "Banwave Checker",
            description = "Automatically disconnects from the server or pauses the macro when a banwave is detected"
    )
    public static boolean enableLeavePauseOnBanwave = false;
    @DualOption(
            name = "Banwave Action", category = FAILSAFE, subcategory = "Banwave Checker",
            description = "The action taken when banwave detected",
            left = "Leave",
            right = "Pause"
    )
    public static boolean banwaveAction = false;
    @Dropdown(
            name = "Base Threshold on", category = FAILSAFE, subcategory = "Banwave Checker",
            options = {"Global bans", "Taunahi bans", "Both"}, size = 2
    )
    public static int banwaveThresholdType = 0;
    @Slider(
            name = "Banwave Disconnect Threshold", category = FAILSAFE, subcategory = "Banwave Checker",
            description = "The threshold to disconnect from the server on banwave",
            min = 1, max = 100
    )
    public static int banwaveThreshold = 50;
    @Number(
            name = "Delay Before Reconnecting", category = FAILSAFE, subcategory = "Banwave Checker",
            description = "The delay before reconnecting after leaving on banwave (in seconds)",
            min = 1, max = 20, size = 2
    )
    public static int delayBeforeReconnecting = 5;
    @Switch(
            name = "Don't leave during Jacob's Contest", category = FAILSAFE, subcategory = "Banwave Checker",
            description = "Prevents the macro from leaving during Jacob's Contest even when banwave detected"
    )
    public static boolean banwaveDontLeaveDuringJacobsContest = true;
    //</editor-fold>

    //<editor-fold desc="Anti Stuck">
    @Switch(
            name = "Enable Anti Stuck", category = FAILSAFE, subcategory = "Anti Stuck",
            description = "Prevents the macro from getting stuck in the same position"
    )
    public static boolean enableAntiStuck = true;
    //</editor-fold>

    //<editor-fold desc="Failsafe Messages">
    @Switch(
            name = "Send Chat Message During Failsafe", category = FAILSAFE, subcategory = "Failsafe Messages",
            description = "Sends a chat message when a failsafe has been triggered"
    )
    public static boolean sendFailsafeMessage = true;
    @Page(
            name = "Custom Failsafe Messages", category = FAILSAFE, subcategory = "Failsafe Messages", location = PageLocation.BOTTOM,
            description = "Click here to edit custom failsafe messages"
    )
    public static CustomFailsafeMessagesPage customFailsafeMessagesPage = new CustomFailsafeMessagesPage();
    //</editor-fold>
    //</editor-fold>

    //<editor-fold desc="SCHEDULER">
    //<editor-fold desc="Scheduler">
    @Switch(
            name = "Enable Scheduler", category = SCHEDULER, subcategory = "Scheduler", size = OptionSize.DUAL,
            description = "Farms for X amount of minutes then takes a break for X amount of minutes"
    )
    public static boolean enableScheduler = false;
    @Slider(
            name = "Farming time (in minutes)", category = SCHEDULER, subcategory = "Scheduler",
            description = "How long to farm",
            min = 1, max = 300, step = 1
    )
    public static int schedulerFarmingTime = 30;
    @Slider(
            name = "Farming time randomness (in minutes)", category = SCHEDULER, subcategory = "Scheduler",
            description = "How much randomness to add to the farming time",
            min = 0, max = 15, step = 1
    )
    public static int schedulerFarmingTimeRandomness = 0;
    @Slider(
            name = "Break time (in minutes)", category = SCHEDULER, subcategory = "Scheduler",
            description = "How long to take a break",
            min = 1, max = 120, step = 1
    )
    public static int schedulerBreakTime = 5;
    @Slider(
            name = "Break time randomness (in minutes)", category = SCHEDULER, subcategory = "Scheduler",
            description = "How much randomness to add to the break time",
            min = 0, max = 15, step = 1
    )
    public static int schedulerBreakTimeRandomness = 0;
    @Switch(
            name = "Pause the scheduler during Jacob's Contest", category = SCHEDULER, subcategory = "Scheduler",
            description = "Pauses and delays the scheduler during Jacob's Contest"
    )
    public static boolean pauseSchedulerDuringJacobsContest = true;
    @Switch(
            name = "Open inventory on scheduler breaks", category = SCHEDULER, subcategory = "Scheduler",
            description = "Opens inventory on scheduler breaks"
    )
    public static boolean openInventoryOnSchedulerBreaks = true;
    //</editor-fold>

    //<editor-fold desc="Leave timer">
    @Switch(
            name = "Enable leave timer", category = SCHEDULER, subcategory = "Leave Timer",
            description = "Leaves the server after the timer has ended"
    )
    public static boolean leaveTimer = false;
    @Slider(
            name = "Leave time", category = SCHEDULER, subcategory = "Leave Timer",
            description = "The time to leave the server (in minutes)",
            min = 15, max = 720, step = 5
    )
    public static int leaveTime = 60;
    //</editor-fold>
    //</editor-fold>

    //<editor-fold desc="JACOB'S CONTEST">
    @Switch(
            name = "Enable Jacob Failsafes", category = JACOBS_CONTEST, subcategory = "Jacob's Contest",
            description = "Stops farming once a crop threshold has been met"
    )
    public static boolean enableJacobFailsafes = false;
    @DualOption(
            name = "Jacob Failsafe Action", category = JACOBS_CONTEST, subcategory = "Jacob's Contest",
            description = "The action to take when a failsafe has been triggered",
            left = "Leave",
            right = "Pause"
    )
    public static boolean jacobFailsafeAction = true;
    @Slider(
            name = "Nether Wart Cap", category = JACOBS_CONTEST, subcategory = "Jacob's Contest",
            description = "The nether wart cap",
            min = 10000, max = 2000000, step = 10000
    )
    public static int jacobNetherWartCap = 800000;
    @Slider(
            name = "Potato Cap", category = JACOBS_CONTEST, subcategory = "Jacob's Contest",
            description = "The potato cap",
            min = 10000, max = 2000000, step = 10000
    )
    public static int jacobPotatoCap = 830000;
    @Slider(
            name = "Carrot Cap", category = JACOBS_CONTEST, subcategory = "Jacob's Contest",
            description = "The carrot cap",
            min = 10000, max = 2000000, step = 10000
    )
    public static int jacobCarrotCap = 860000;
    @Slider(
            name = "Wheat Cap", category = JACOBS_CONTEST, subcategory = "Jacob's Contest",
            description = "The wheat cap",
            min = 10000, max = 2000000, step = 10000
    )
    public static int jacobWheatCap = 265000;
    @Slider(
            name = "Sugar Cane Cap", category = JACOBS_CONTEST, subcategory = "Jacob's Contest",
            description = "The sugar cane cap",
            min = 10000, max = 2000000, step = 10000
    )
    public static int jacobSugarCaneCap = 575000;
    @Slider(
            name = "Mushroom Cap", category = JACOBS_CONTEST, subcategory = "Jacob's Contest",
            description = "The mushroom cap",
            min = 10000, max = 2000000, step = 10000
    )
    public static int jacobMushroomCap = 250000;
    @Slider(
            name = "Melon Cap", category = JACOBS_CONTEST, subcategory = "Jacob's Contest",
            description = "The melon cap",
            min = 10000, max = 2000000, step = 10000
    )
    public static int jacobMelonCap = 1234000;

    @Slider(
            name = "Pumpkin Cap", category = JACOBS_CONTEST, subcategory = "Jacob's Contest",
            description = "The pumpkin cap",
            min = 10000, max = 2000000, step = 10000
    )
    public static int jacobPumpkinCap = 240000;

    @Slider(
            name = "Cocoa Beans Cap", category = JACOBS_CONTEST, subcategory = "Jacob's Contest",
            description = "The cocoa beans cap",
            min = 10000, max = 2000000, step = 10000
    )
    public static int jacobCocoaBeansCap = 725000;
    @Slider(
            name = "Cactus Cap", category = JACOBS_CONTEST, subcategory = "Jacob's Contest",
            description = "The cactus cap",
            min = 10000, max = 2000000, step = 10000
    )
    public static int jacobCactusCap = 470000;

    //</editor-fold>

    //<editor-fold desc="VISITORS">
    //<editor-fold desc="Visitors Main">
    @Info(
            text = "Visitors Macro tends to move your mouse because of opening GUIs frequently. Be aware of that.",
            type = InfoType.WARNING,
            category = VISITORS_MACRO,
            subcategory = "Visitors Macro",
            size = 2
    )
    public static boolean visitorsMacroWarning2;

    @Info(
            text = "Cookie buff is required!",
            type = InfoType.ERROR,
            category = VISITORS_MACRO,
            subcategory = "Visitors Macro"
    )
    public static boolean infoCookieBuffRequired;

    @Switch(
            name = "Enable visitors macro", category = VISITORS_MACRO, subcategory = "Visitors Macro",
            description = "Enables visitors macro"
    )
    public static boolean visitorsMacro = false;
    @Slider(
            name = "Minimum Visitors to start the macro", category = VISITORS_MACRO, subcategory = "Visitors Macro",
            description = "The minimum amount of visitors to start the macro",
            min = 1, max = 5
    )
    public static int visitorsMacroMinVisitors = 5;
    @Switch(
            name = "Autosell before serving visitors", category = VISITORS_MACRO, subcategory = "Visitors Macro",
            description = "Automatically sells crops before serving visitors"
    )
    public static boolean visitorsMacroAutosellBeforeServing = false;
    @Switch(
            name = "Pause the visitors macro during Jacob's contests", category = VISITORS_MACRO, subcategory = "Visitors Macro",
            description = "Pauses the visitors macro during Jacob's contests"
    )
    public static boolean pauseVisitorsMacroDuringJacobsContest = true;

    @Switch(
            name = "Use Path finder in Visitors macro between serving visitors", category = VISITORS_MACRO, subcategory = "Visitors Macro",
            description = "Uses path finder between serving visitors"
    )
    public static boolean visitorsMacroUsePathFinder = false;

    @Slider(
            name = "The minimum amount of coins to start the macro (in thousands)", category = VISITORS_MACRO, subcategory = "Visitors Macro",
            description = "The minimum amount of coins you need to have in your purse to start the visitors macro",
            min = 1_000, max = 20_000
    )
    public static int visitorsMacroMinMoney = 2_000;
    @Info(
            text = "If you put your compactors in the hotbar, they will be temporarily disabled.",
            type = InfoType.WARNING,
            category = VISITORS_MACRO,
            subcategory = "Visitors Macro",
            size = 2
    )
    public static boolean infoCompactors;

    @Slider(
            name = "Max Spend Limit (in Thousands Per Purchase)", category = VISITORS_MACRO, subcategory = "Visitors Macro",
            min = 10, max = 2000, step = 1
    )
    public static int visitorsMacroMaxSpendLimit = 700;

    @Button(
            name = "Start the macro manually", category = VISITORS_MACRO, subcategory = "Visitors Macro",
            description = "Triggers the visitors macro",
            text = "Trigger now"
    )
    public static Runnable triggerVisitorsMacro = () -> {
        if (!VisitorsMacro.getInstance().isInBarn()) {
            LogUtils.sendError("[Visitors Macro] You need to be in the barn to start the macro!");
            return;
        }
        VisitorsMacro.getInstance().setManuallyStarted(true);
        VisitorsMacro.getInstance().start();
    };
    //</editor-fold>

    //<editor-fold desc="Rarity">
    @Dropdown(
            name = "Uncommon", category = VISITORS_MACRO, subcategory = "Rarity",
            description = "The action taken when an uncommon visitor arrives",
            options = {"Accept", "Accept if profitable only", "Decline"},
            size = 2
    )
    public static int visitorsActionUncommon = 0;
    @Dropdown(
            name = "Rare", category = VISITORS_MACRO, subcategory = "Rarity",
            description = "The action taken when a rare visitor arrives",
            options = {"Accept", "Accept if profitable only", "Decline"},
            size = 2
    )
    public static int visitorsActionRare = 0;
    @Dropdown(
            name = "Legendary", category = VISITORS_MACRO, subcategory = "Rarity",
            description = "The action taken when a legendary visitor arrives",
            options = {"Accept", "Accept if profitable only", "Decline"},
            size = 2
    )
    public static int visitorsActionLegendary = 0;
    @Dropdown(
            name = "Mythic", category = VISITORS_MACRO, subcategory = "Rarity",
            description = "The action taken when a mythic visitor arrives",
            options = {"Accept", "Accept if profitable only", "Decline"},
            size = 2
    )
    public static int visitorsActionMythic = 0;
    @Dropdown(
            name = "Special", category = VISITORS_MACRO, subcategory = "Rarity",
            description = "The action taken when a special visitor arrives",
            options = {"Accept", "Accept if profitable only", "Decline"},
            size = 2
    )
    public static int visitorsActionSpecial = 0;
    //</editor-fold>
    //</editor-fold>

    //<editor-fold desc="PESTS DESTROYER">
    //<editor-fold desc="Infos">
    @Info(
            text = "Make sure to enable Hypixel's Particles (/pq low), low is the minimum to make it work",
            type = InfoType.WARNING,
            category = PESTS_DESTROYER,
            size = 2
    )
    public static boolean pestsDestroyerWarning;

    @Info(
            text = "Pests Destroyer will trigger only at rewarp or spawn location after reaching the threshold!",
            type = InfoType.INFO,
            category = PESTS_DESTROYER,
            size = 2
    )
    public static boolean pestsDestroyerInfo;
    //</editor-fold>

    //<editor-fold desc="Pests Destroyer Main">
    @Switch(
            name = "Enable Pests Destroyer (USE AT YOUR OWN RISK)", category = PESTS_DESTROYER, subcategory = "Pests Destroyer",
            description = "Destroys pests"
    )
    public static boolean enablePestsDestroyer = false;
    @Slider(
            name = "Start killing pests at X pests", category = PESTS_DESTROYER, subcategory = "Pests Destroyer",
            description = "The amount of pests to start killing pests",
            min = 1, max = 8
    )
    public static int startKillingPestsAt = 3;
    @Slider(
            name = "Additional GUI Delay (ms)", category = PESTS_DESTROYER, subcategory = "Pests Destroyer",
            description = "Extra time to wait between clicks. By default it's 500-1000 ms.",
            min = 0, max = 5000
    )
    public static int pestAdditionalGUIDelay = 0;

    @Switch(
            name = "Sprint while flying", category = PESTS_DESTROYER, subcategory = "Pests Destroyer",
            description = "Sprints while flying"
    )
    public static boolean sprintWhileFlying = false;

    @Switch(
            name = "Pause the Pests Destroyer during Jacob's contests", category = PESTS_DESTROYER, subcategory = "Pests Destroyer",
            description = "Pauses the Pests Destroyer during Jacob's contests",
            size = 2
    )
    public static boolean pausePestsDestroyerDuringJacobsContest = true;

    @Button(
            name = "Trigger now Pests Destroyer", category = PESTS_DESTROYER, subcategory = "Pests Destroyer",
            description = "Triggers the pests destroyer manually",
            text = "Trigger now"
    )
    public static void triggerManuallyPestsDestroyer() {
        if (PestsDestroyer.getInstance().canEnableMacro(true)) {
            PestsDestroyer.getInstance().start();
        }
    }

    @KeyBind(
            name = "Enable Pests Destroyer", category = PESTS_DESTROYER, subcategory = "Pests Destroyer",
            description = "Enables the pests destroyer",
            size = 2
    )
    public static OneKeyBind enablePestsDestroyerKeyBind = new OneKeyBind(Keyboard.KEY_NONE);

    //</editor-fold>

    //<editor-fold desc="Drawings">

    @Switch(
            name = "Pests ESP", category = PESTS_DESTROYER, subcategory = "Drawings",
            description = "Draws a box around pests"
    )
    public static boolean pestsESP = true;
    @Color(
            name = "ESP Color", category = PESTS_DESTROYER, subcategory = "Drawings",
            description = "The color of the pests ESP"
    )
    public static OneColor pestsESPColor = new OneColor(0, 255, 217, 171);
    @Switch(
            name = "Tracers to Pests", category = PESTS_DESTROYER, subcategory = "Drawings",
            description = "Draws a line to pests"
    )
    public static boolean pestsTracers = true;
    @Color(
            name = "Tracers Color", category = PESTS_DESTROYER, subcategory = "Drawings",
            description = "The color of the pests tracers"
    )
    public static OneColor pestsTracersColor = new OneColor(0, 255, 217, 171);
    @Switch(
            name = "Highlight borders of Plot with pests", category = PESTS_DESTROYER, subcategory = "Drawings",
            description = "Highlights the borders of the plot with pests"
    )
    public static boolean highlightPlotWithPests = true;
    @Color(
            name = "Plot Highlight Color", category = PESTS_DESTROYER, subcategory = "Drawings",
            description = "The color of the plot highlight"
    )
    public static OneColor plotHighlightColor = new OneColor(0, 255, 217, 40);
    //</editor-fold>

    //<editor-fold desc="Logs">
    @Switch(
            name = "Send Webhook log if pests detection number has been exceeded", category = PESTS_DESTROYER, subcategory = "Logs",
            description = "Sends a webhook log if pests detection number has been exceeded"
    )
    public static boolean sendWebhookLogIfPestsDetectionNumberExceeded = true;
    @Switch(
            name = "Ping @everyone", category = PESTS_DESTROYER, subcategory = "Logs",
            description = "Pings @everyone on pests detection number exceeded"
    )
    public static boolean pingEveryoneOnPestsDetectionNumberExceeded = false;
    @Switch(
            name = "Send notification if pests detection number has been exceeded", category = PESTS_DESTROYER, subcategory = "Logs",
            description = "Sends a notification if pests detection number has been exceeded"
    )
    public static boolean sendNotificationIfPestsDetectionNumberExceeded = true;
    //</editor-fold>
    //</editor-fold>

    //<editor-fold desc="DISCORD INTEGRATION">
    //<editor-fold desc="Webhook Discord">
    @Switch(
            name = "Enable Webhook Messages", category = DISCORD_INTEGRATION, subcategory = "Discord Webhook",
            description = "Allows to send messages via Discord webhooks"
    )
    public static boolean enableWebHook = false;
    @Switch(
            name = "Send Logs", category = DISCORD_INTEGRATION, subcategory = "Discord Webhook",
            description = "Sends all messages about the macro, staff checks, etc"
    )
    public static boolean sendLogs = false;
    @Switch(
            name = "Send Status Updates", category = DISCORD_INTEGRATION, subcategory = "Discord Webhook",
            description = "Sends messages about the macro, such as profits, harvesting crops, etc"
    )
    public static boolean sendStatusUpdates = false;
    @Number(
            name = "Status Update Interval (in minutes)", category = DISCORD_INTEGRATION, subcategory = "Discord Webhook",
            description = "The interval between sending messages about status updates",
            min = 1, max = 60
    )
    public static int statusUpdateInterval = 5;
    @Switch(
            name = "Send Visitors Macro Logs", category = DISCORD_INTEGRATION, subcategory = "Discord Webhook",
            description = "Sends messages about the visitors macro, such as which visitor got rejected or accepted and with what items"
    )
    public static boolean sendVisitorsMacroLogs = true;
    @Switch(
            name = "Ping everyone on Visitors Macro Logs", category = DISCORD_INTEGRATION, subcategory = "Discord Webhook",
            description = "Pings everyone on Visitors Macro Logs"
    )
    public static boolean pingEveryoneOnVisitorsMacroLogs = false;
    @Text(
            name = "WebHook URL", category = DISCORD_INTEGRATION, subcategory = "Discord Webhook",
            description = "The URL to use for the webhook",
            placeholder = "https://discord.com/api/webhooks/...",
            secure = true
    )
    public static String webHookURL = "";
    //</editor-fold>

    //<editor-fold desc="Remote Control">
    @Switch(
            name = "Enable Remote Control", category = DISCORD_INTEGRATION, subcategory = "Remote Control",
            description = "Enables remote control via Discord messages"
    )
    public static boolean enableRemoteControl = false;
    @Text(
            name = "Discord Remote Control Bot Token",
            category = DISCORD_INTEGRATION, subcategory = "Remote Control",
            description = "The bot token to use for remote control",
            secure = true
    )
    public static String discordRemoteControlToken;
    @Text(
            name = "Discord Remote Control Address",
            category = DISCORD_INTEGRATION, subcategory = "Remote Control",
            description = "The address to use for remote control. If you are unsure what to put there, leave \"localhost\".",
            placeholder = "localhost"
    )
    public static String discordRemoteControlAddress = "localhost";

    @Number(
            name = "Remote Control Port", category = DISCORD_INTEGRATION, subcategory = "Remote Control",
            description = "The port to use for remote control. Change this if you have port conflicts.",
            min = 1, max = 65535
    )
    public static int remoteControlPort = 21370;

    @Info(
            text = "If you want to use the remote control feature, you need to put Farm Helper JDA Dependency inside your mods folder.",
            type = InfoType.ERROR,
            category = DISCORD_INTEGRATION,
            subcategory = "Remote Control",
            size = 2
    )
    public static boolean infoRemoteControl;
    //</editor-fold>
    //</editor-fold>

    //<editor-fold desc="DELAYS">
    //<editor-fold desc="Changing Rows">
    @Slider(
            name = "Time between changing rows", category = DELAYS, subcategory = "Changing rows",
            description = "The minimum time to wait before changing rows (in milliseconds)",
            min = 70, max = 2000
    )
    public static float timeBetweenChangingRows = 400f;
    @Slider(
            name = "Additional random time between changing rows", category = DELAYS, subcategory = "Changing rows",
            description = "The maximum time to wait before changing rows (in milliseconds)",
            min = 0, max = 2000
    )
    public static float randomTimeBetweenChangingRows = 200f;
    //</editor-fold>

    //<editor-fold desc="Rotation Time">
    @Slider(
            name = "Rotation Time", category = DELAYS, subcategory = "Rotations",
            description = "The time it takes to rotate the player",
            min = 200f, max = 2000f
    )
    public static float rotationTime = 500f;
    @Slider(
            name = "Additional random Rotation Time", category = DELAYS, subcategory = "Rotations",
            description = "The maximum random time added to the delay time it takes to rotate the player (in seconds)",
            min = 0f, max = 2000f
    )
    public static float rotationTimeRandomness = 300;
    //</editor-fold>

    //<editor-fold desc="Pests Destroyer Time">
    @Slider(
            name = "Pests Destroyer Small Distance Rotation Time", category = DELAYS, subcategory = "Pests Destroyer",
            description = "The time it takes to rotate the player",
            min = 50f, max = 750
    )
    public static float pestsKillerRotationTimeSmallDistance = 200f;
    @Slider(
            name = "Additional random Pests Destroyer Small Distance Rotation Time", category = DELAYS, subcategory = "Pests Destroyer",
            description = "The maximum random time added to the delay time it takes to rotate the player (in seconds)",
            min = 0f, max = 750
    )
    public static float pestsKillerRotationTimeRandomnessSmallDistance = 150;

    @Slider(
            name = "Pests Destroyer Medium Distance Rotation Time", category = DELAYS, subcategory = "Pests Destroyer",
            description = "The time it takes to rotate the player",
            min = 50f, max = 750
    )
    public static float pestsKillerRotationTimeMediumDistance = 300f;
    @Slider(
            name = "Additional random Pests Destroyer Medium Distance Rotation Time", category = DELAYS, subcategory = "Pests Destroyer",
            description = "The maximum random time added to the delay time it takes to rotate the player (in seconds)",
            min = 0f, max = 750
    )
    public static float pestsKillerRotationTimeRandomnessMediumDistance = 120;

    @Slider(
            name = "Pests Destroyer Stuck Time (in minutes)", category = DELAYS, subcategory = "Pests Destroyer",
            description = "Pests Destroyer Stuck Time (in minutes) for single pest",
            min = 1, max = 7
    )
    public static float pestsKillerStuckTime = 3;
    @Slider(
            name = "Pests Destroyer Ticks of not seeing pest.", category = DELAYS, subcategory = "Pests Destroyer",
            description = "Pests Destroyer Ticks of not seeing pest while attacking (1 tick == 50ms) to trigger Escape to Hub. 0 to disable",
            min = 20, max = 200
    )
    public static int pestsKillerTicksOfNotSeeingPestWhileAttacking = 100;
    //</editor-fold>

    //<editor-fold desc="Gui Delay">
    @Slider(
            name = "GUI Delay", category = DELAYS, subcategory = "GUI Delays",
            description = "The delay between clicking during GUI macros (in milliseconds)",
            min = 250f, max = 2000f
    )
    public static float macroGuiDelay = 400f;
    @Slider(
            name = "Additional random GUI Delay", category = DELAYS, subcategory = "GUI Delays",
            description = "The maximum random time added to the delay time between clicking during GUI macros (in milliseconds)",
            min = 0f, max = 2000f
    )
    public static float macroGuiDelayRandomness = 350f;
    //</editor-fold>

    //<editor-fold desc="Plot Cleaning Time">
    @Slider(
            name = "Plot Cleaning Helper Rotation Time", category = DELAYS, subcategory = "Plot Cleaning Helper",
            description = "The time it takes to rotate the player",
            min = 20f, max = 500f
    )
    public static float plotCleaningHelperRotationTime = 50;
    @Slider(
            name = "Additional random Plot Cleaning Helper Rotation Time", category = DELAYS, subcategory = "Plot Cleaning Helper",
            description = "The maximum random time added to the delay time it takes to rotate the player (in seconds)",
            min = 0f, max = 500f
    )

    public static float plotCleaningHelperRotationTimeRandomness = 50;
    //</editor-fold>

    //<editor-fold desc="Rewarp Time">
    @Slider(
            name = "Rewarp Delay", category = DELAYS, subcategory = "Rewarp",
            description = "The delay between rewarping (in milliseconds)",
            min = 250f, max = 2000f
    )
    public static float rewarpDelay = 400f;
    @Slider(
            name = "Additional random Rewarp Delay", category = DELAYS, subcategory = "Rewarp",
            description = "The maximum random time added to the delay time between rewarping (in milliseconds)",
            min = 0f, max = 2000f
    )
    public static float rewarpDelayRandomness = 350f;
    //</editor-fold>
    //</editor-fold>

    //<editor-fold desc="HUD">
    @HUD(
            name = "Status HUD", category = HUD
    )
    public static StatusHUD statusHUD = new StatusHUD();
    @HUD(
            name = "Profit Calculator HUD", category = HUD, subcategory = " "
    )
    public static ProfitCalculatorHUD profitHUD = new ProfitCalculatorHUD();
    //</editor-fold>

    //<editor-fold desc="DEBUG">
    //<editor-fold desc="Debug">
    @KeyBind(
            name = "Debug Keybind", category = DEBUG, subcategory = "Debug"
    )
    public static OneKeyBind debugKeybind = new OneKeyBind(Keyboard.KEY_NONE);
    //    @KeyBind(
//            name = "Debug Keybind 2", category = DEBUG
//    )
//    public static OneKeyBind debugKeybind2 = new OneKeyBind(Keyboard.KEY_H);
//    @KeyBind(
//            name = "Debug Keybind 3", category = DEBUG
//    )
//    public static OneKeyBind debugKeybind3 = new OneKeyBind(Keyboard.KEY_J);
    @Switch(
            name = "Debug Mode", category = DEBUG, subcategory = "Debug",
            description = "Prints to chat what the bot is currently executing. Useful if you are having issues."
    )
    public static boolean debugMode = false;
    @Switch(
            name = "Hide Logs (Not Recommended)", category = DEBUG, subcategory = "Debug",
            description = "Hides all logs from the console. Not recommended."
    )
    public static boolean hideLogs = false;
    @Switch(
            name = "Show rotation debug messages", category = DEBUG, subcategory = "Debug",
            description = "Shows rotation debug messages"
    )
    public static boolean showRotationDebugMessages = false;
    //</editor-fold>

    //<editor-fold desc="Debug Hud">
    @HUD(
            name = "Debug HUD", category = DEBUG, subcategory = " "
    )
    public static DebugHUD debugHUD = new DebugHUD();
    //</editor-fold>
    //</editor-fold>

    //<editor-fold desc="EXPERIMENTAL">
    //<editor-fold desc="Fastbreak">
    @Switch(
            name = "Enable Fast Break (DANGEROUS)", category = EXPERIMENTAL, subcategory = "Fast Break",
            description = "Fast Break is very risky and using it will most likely result in a ban. Proceed with caution."
    )
    public static boolean fastBreak = false;

    @Info(
            text = "Fast Break will most likely ban you. Use at your own risk.",
            type = InfoType.ERROR,
            category = EXPERIMENTAL,
            subcategory = "Fast Break"
    )
    public static boolean fastBreakWarning;
    @Slider(
            name = "Fast Break Speed", category = EXPERIMENTAL, subcategory = "Fast Break",
            description = "Fast Break speed",
            min = 1, max = 3
    )
    public static int fastBreakSpeed = 1;
    @Switch(
            name = "Disable Fast Break during banwave", category = EXPERIMENTAL, subcategory = "Fast Break",
            description = "Disables Fast Break during banwave"
    )
    public static boolean disableFastBreakDuringBanWave = true;
    @Switch(
            name = "Disable Fast Break during Jacob's contest", category = EXPERIMENTAL, subcategory = "Fast Break",
            description = "Disables Fast Break during Jacob's contest"
    )
    public static boolean disableFastBreakDuringJacobsContest = true;
    //</editor-fold>

    //<editor-fold desc="Auto Switch">
    @Switch(
            name = "Automatically switch recognized crop", category = EXPERIMENTAL, subcategory = "Auto Switch",
            description = "Macro will be recognizing farming crop, which will lead to auto switching tool to the best one"
    )
    public static boolean autoSwitchTool = true;
    //</editor-fold>

    //<editor-fold desc="Fly Path Finder">
    @Slider(
            name = "Allowed Overshoot Threshold", category = EXPERIMENTAL, subcategory = "Flight",
            description = "The minimum distance from the block at which the fly path finder would allow overshooting",
            min = 0.05f, max = 0.4f
    )
    public static float flightAllowedOvershootThreshold = 0.1f;
    @Slider(
            name = "Max stuck time without motion (in ticks)", category = EXPERIMENTAL, subcategory = "Flight",
            description = "The maximum time to wait before unstucking (in ticks)",
            min = 30, max = 150
    )
    public static int flightMaxStuckTimeWithoutMotion = 40;
    @Slider(
            name = "Max stuck time with motion (in ticks)", category = EXPERIMENTAL, subcategory = "Flight",
            description = "The maximum time to wait before unstucking (in ticks)",
            min = 30, max = 150
    )
    public static int flightMaxStuckTimeWithMotion = 100;
    @Slider(
            name = "Deceleration offset", category = EXPERIMENTAL, subcategory = "Flight",
            description = "",
            min = 0, max = 15
    )
    public static int flightDecelerationOffset = 5;
    @Slider(
            name = "Maximum stuck distance threshold", category = EXPERIMENTAL, subcategory = "Flight",
            description = "The maximum distance threshold before unstucking (Vec3)",
            min = 0.3f, max = 1.5f
    )
    public static float flightMaximumStuckDistanceThreshold = 0.75f;
    @Switch(
            name = "Lock rotation to multipliers of 45 degrees", category = EXPERIMENTAL, subcategory = "Flight",
            description = "Locks the rotation to multipliers of 45 degrees"
    )
    public static boolean flightLockRotationToMultipliersOf45Degrees = false;
    //</editor-fold>
    //</editor-fold>

    @Number(name = "Config Version", category = EXPERIMENTAL, subcategory = "Experimental", min = 0, max = 1337)
    public static int configVersion = 2;
    @Switch(
            name = "Shown Welcome GUI", category = EXPERIMENTAL, subcategory = "Experimental"
    )
    public static boolean shownWelcomeGUI = false;

    public TaunahiConfig() {
        super(new Mod("Farm Helper", ModType.HYPIXEL, "/taunahi/icon-mod/icon.png"), "/taunahi/config.json");
        initialize();

        this.addDependency("macroType", "Macro Type", () -> !MacroHandler.getInstance().isMacroToggled());

        this.addDependency("customPitchLevel", "customPitch");
        this.addDependency("customYawLevel", "customYaw");

        this.addDependency("inventoryFullTime", "enableAutoSell");
        this.addDependency("autoSellMarketType", "enableAutoSell");
        this.addDependency("autoSellSacks", "enableAutoSell");
        this.addDependency("autoSellSacksPlacement", "enableAutoSell");
        this.addDependency("autoSellFunction", "enableAutoSell");

        this.addDependency("petSwapperDelay", "enablePetSwapper");
        this.addDependency("petSwapperName", "enablePetSwapper");

        this.addDependency("autoUngrabMouse", "This feature doesn't work properly on Mac OS!", () -> !Minecraft.isRunningOnMac);

        this.addDependency("desyncPauseDelay", "checkDesync");
        this.addDependency("failsafeSoundType", "Play Button", () -> enableFailsafeSound && !AudioManager.getInstance().isSoundPlaying());
        this.addDependency("_playFailsafeSoundButton", "enableFailsafeSound");
        this.addDependency("_stopFailsafeSoundButton", "enableFailsafeSound");
        this.hideIf("_playFailsafeSoundButton", () -> AudioManager.getInstance().isSoundPlaying());
        this.hideIf("_stopFailsafeSoundButton", () -> !AudioManager.getInstance().isSoundPlaying());
        this.addDependency("failsafeMcSoundSelected", "Minecraft Sound", () -> !failsafeSoundType && enableFailsafeSound);
        this.addDependency("failsafeSoundSelected", "Custom Sound", () -> failsafeSoundType && enableFailsafeSound);
        this.addDependency("failsafeSoundVolume", "Custom Sound", () -> failsafeSoundType && enableFailsafeSound);
        this.addDependency("maxOutMinecraftSounds", "Minecraft Sound", () -> !failsafeSoundType && enableFailsafeSound);
        this.hideIf("customFailsafeSoundWarning", () -> !failsafeSoundType || !enableFailsafeSound || failsafeSoundSelected != 0);
        this.addDependency("restartAfterFailSafeDelay", "enableRestartAfterFailSafe");
        this.addDependency("alwaysTeleportToGarden", "enableRestartAfterFailSafe");

        this.addDependency("schedulerFarmingTime", "enableScheduler");
        this.addDependency("schedulerFarmingTimeRandomness", "enableScheduler");
        this.addDependency("schedulerBreakTime", "enableScheduler");
        this.addDependency("schedulerBreakTimeRandomness", "enableScheduler");
        this.addDependency("pauseSchedulerDuringJacobsContest", "enableScheduler");

        this.addDependency("jacobNetherWartCap", "enableJacobFailsafes");
        this.addDependency("jacobPotatoCap", "enableJacobFailsafes");
        this.addDependency("jacobCarrotCap", "enableJacobFailsafes");
        this.addDependency("jacobWheatCap", "enableJacobFailsafes");
        this.addDependency("jacobSugarCaneCap", "enableJacobFailsafes");
        this.addDependency("jacobMushroomCap", "enableJacobFailsafes");
        this.addDependency("jacobMelonCap", "enableJacobFailsafes");
        this.addDependency("jacobPumpkinCap", "enableJacobFailsafes");
        this.addDependency("jacobCocoaBeansCap", "enableJacobFailsafes");
        this.addDependency("jacobCactusCap", "enableJacobFailsafes");
        this.addDependency("jacobFailsafeAction", "enableJacobFailsafes");

        this.addDependency("pauseVisitorsMacroDuringJacobsContest", "visitorsMacro");
        this.addDependency("visitorsMacroUsePathFinder", "visitorsMacro");
        this.addDependency("triggerVisitorsMacro", "visitorsMacro");
        this.addDependency("visitorsMacroPriceManipulationMultiplier", "visitorsMacro");
        this.addDependency("visitorsMacroMinVisitors", "visitorsMacro");
        this.addDependency("visitorsMacroAutosellBeforeServing", "visitorsMacro");
        this.addDependency("visitorsMacroMinMoney", "visitorsMacro");
        this.addDependency("visitorsMacroMaxSpendLimit", "visitorsMacro");

        this.addDependency("sendVisitorsMacroLogs", "visitorsMacro");
        this.addDependency("sendVisitorsMacroLogs", "enableWebHook");
        this.addDependency("pingEveryoneOnVisitorsMacroLogs", "visitorsMacro");
        this.addDependency("pingEveryoneOnVisitorsMacroLogs", "sendVisitorsMacroLogs");
        this.addDependency("pingEveryoneOnVisitorsMacroLogs", "enableWebHook");

        this.addDependency("startKillingPestsAt", "enablePestsDestroyer");
        this.addDependency("pestAdditionalGUIDelay", "enablePestsDestroyer");
        this.addDependency("sprintWhileFlying", "enablePestsDestroyer");
        this.addDependency("pausePestsDestroyerDuringJacobsContest", "enablePestsDestroyer");


        this.hideIf("infoCookieBuffRequired", () -> GameStateHandler.getInstance().inGarden() || GameStateHandler.getInstance().getCookieBuffState() == GameStateHandler.BuffState.NOT_ACTIVE);

        this.addDependency("sendLogs", "enableWebHook");
        this.addDependency("sendStatusUpdates", "enableWebHook");
        this.addDependency("statusUpdateInterval", "enableWebHook");
        this.addDependency("webHookURL", "enableWebHook");
        this.addDependency("enableRemoteControl", "Enable Remote Control", () -> Loader.isModLoaded("taunahijdadependency"));
        this.addDependency("discordRemoteControlAddress", "enableRemoteControl");
        this.addDependency("remoteControlPort", "enableRemoteControl");


        this.hideIf("infoRemoteControl", () -> Loader.isModLoaded("taunahijdadependency"));
        this.hideIf("failsafeSoundTimes", () -> true);

        this.addDependency("debugMode", "Debug Mode", () -> !hideLogs);
        this.addDependency("hideLogs", "Hide Logs (Not Recommended)", () -> !debugMode);

        this.addDependency("fastBreakSpeed", "fastBreak");
        this.addDependency("disableFastBreakDuringBanWave", "fastBreak");
        this.addDependency("disableFastBreakDuringJacobsContest", "fastBreak");

        this.addDependency("autoGodPotFromBackpack", "autoGodPot");
        this.addDependency("autoGodPotFromBits", "autoGodPot");
        this.addDependency("autoGodPotFromAH", "autoGodPot");

        this.hideIf("autoGodPotBackpackNumber", () -> !autoGodPotFromBackpack);
        this.hideIf("autoGodPotStorageType", () -> !autoGodPotFromBackpack);

        this.addDependency("banwaveAction", "enableLeavePauseOnBanwave");
        this.addDependency("banwaveThreshold", "enableLeavePauseOnBanwave");
        this.addDependency("banwaveThresholdType", "enableLeavePauseOnBanwave");
        this.addDependency("delayBeforeReconnecting", "enableLeavePauseOnBanwave");
        this.addDependency("banwaveDontLeaveDuringJacobsContest", "enableLeavePauseOnBanwave");

        this.addDependency("sendWebhookLogIfPestsDetectionNumberExceeded", "enableWebHook");
        this.addDependency("pingEveryoneOnPestsDetectionNumberExceeded", "sendWebhookLogIfPestsDetectionNumberExceeded");
        this.addDependency("pingEveryoneOnPestsDetectionNumberExceeded", "enableWebHook");

        this.addDependency("pestRepellentType", "autoPestRepellent");

        this.addDependency("averageBPSDrop", "averageBPSDropCheck");

        this.addDependency("leaveTime", "leaveTimer");

        this.hideIf("shownWelcomeGUI", () -> true);

        this.hideIf("configVersion", () -> true);

        registerKeyBind(openGuiKeybind, this::openGui);
        registerKeyBind(toggleMacro, () -> MacroHandler.getInstance().toggleMacro());
//        registerKeyBind(debugKeybind, () -> {
//        });
        registerKeyBind(freelookKeybind, () -> Freelook.getInstance().toggle());
        registerKeyBind(plotCleaningHelperKeybind, () -> PlotCleaningHelper.getInstance().toggle());
        registerKeyBind(enablePestsDestroyerKeyBind, () -> {
            if (PestsDestroyer.getInstance().canEnableMacro(true)) {
                PestsDestroyer.getInstance().start();
            }
        });
//        registerKeyBind(debugKeybind2, () -> {
//            MovingObjectPosition objectMouseOver = Minecraft.getMinecraft().objectMouseOver;
//            if (objectMouseOver != null && objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
//                BlockPos blockPos = objectMouseOver.getBlockPos();
//                BlockPos oppositeSide = blockPos.offset(objectMouseOver.sideHit);
//                LogUtils.sendDebug("Block: " + oppositeSide);
//                FlyPathfinder.getInstance().setGoal(new GoalBlock(oppositeSide));
//            }
//        });
//        registerKeyBind(debugKeybind3, () -> {
//                    FlyPathfinder.getInstance().getPathTo(FlyPathfinder.getInstance().getGoal());
//                });
        save();
    }

    public static void addRewarp() {
        if (TaunahiConfig.rewarpList.stream().anyMatch(rewarp -> rewarp.isTheSameAs(BlockUtils.getRelativeBlockPos(0, 0, 0)))) {
            LogUtils.sendError("Rewarp location has already been set!");
            return;
        }
        Rewarp newRewarp = new Rewarp(BlockUtils.getRelativeBlockPos(0, 0, 0));
        rewarpList.add(newRewarp);
        LogUtils.sendSuccess("Added rewarp: " + newRewarp);
        saveRewarpConfig();
    }

    public static void removeRewarp() {
        Rewarp closest = null;
        if (rewarpList.isEmpty()) {
            LogUtils.sendError("No rewarp locations set!");
            return;
        }
        double closestDistance = Double.MAX_VALUE;
        for (Rewarp rewarp : rewarpList) {
            double distance = rewarp.getDistance(BlockUtils.getRelativeBlockPos(0, 0, 0));
            if (distance < closestDistance) {
                closest = rewarp;
                closestDistance = distance;
            }
        }
        if (closest != null) {
            rewarpList.remove(closest);
            LogUtils.sendSuccess("Removed the closest rewarp: " + closest);
            saveRewarpConfig();
        }
    }

    public static void removeAllRewarps() {
        rewarpList.clear();
        LogUtils.sendSuccess("Removed all saved rewarp positions");
        saveRewarpConfig();
    }

    public static void saveRewarpConfig() {
        try {
            if (!configRewarpFile.exists())
                Files.createFile(configRewarpFile.toPath());

            Files.write(configRewarpFile.toPath(), Taunahi.gson.toJson(rewarpList).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static MacroEnum getMacro() {
        return MacroEnum.values()[macroType];
    }

    public static long getRandomTimeBetweenChangingRows() {
        return (long) (timeBetweenChangingRows + (float) Math.random() * randomTimeBetweenChangingRows);
    }

    public static long getMaxTimeBetweenChangingRows() {
        return (long) (timeBetweenChangingRows + randomTimeBetweenChangingRows);
    }

    public static long getRandomRotationTime() {
        return (long) (rotationTime + (float) Math.random() * rotationTimeRandomness);
    }

    public static long getRandomPestsKillerRotationTimeSmallDistance() {
        return (long) (pestsKillerRotationTimeSmallDistance + (float) Math.random() * pestsKillerRotationTimeRandomnessSmallDistance);
    }

    public static long getRandomPestsKillerRotationTimeMediumDistance() {
        return (long) (pestsKillerRotationTimeMediumDistance + (float) Math.random() * pestsKillerRotationTimeRandomnessMediumDistance);
    }

    public static long getRandomGUIMacroDelay() {
        return (long) (macroGuiDelay + (float) Math.random() * macroGuiDelayRandomness);
    }

    public static long getRandomPlotCleaningHelperRotationTime() {
        return (long) (plotCleaningHelperRotationTime + (float) Math.random() * plotCleaningHelperRotationTimeRandomness);
    }

    public static long getRandomRewarpDelay() {
        return (long) (rewarpDelay + (float) Math.random() * rewarpDelayRandomness);
    }

    public String getJson() {
        String json = gson.toJson(this);
        if (json == null || json.equals("{}")) {
            json = nonProfileSpecificGson.toJson(this);
        }
        return json;
    }

    public enum MacroEnum {
        S_V_NORMAL_TYPE,
        S_PUMPKIN_MELON,
        S_PUMPKIN_MELON_MELONGKINGDE,
        S_PUMPKIN_MELON_DEFAULT_PLOT,
        S_SUGAR_CANE,
        S_CACTUS,
        S_CACTUS_SUNTZU,
        S_COCOA_BEANS,
        S_COCOA_BEANS_LEFT_RIGHT,
        S_MUSHROOM,
        S_MUSHROOM_ROTATE,
        S_MUSHROOM_SDS
    }

    public enum CropEnum {
        NONE,
        CARROT,
        NETHER_WART,
        POTATO,
        WHEAT,
        SUGAR_CANE,
        MELON,
        PUMPKIN,
        PUMPKIN_MELON_UNKNOWN,
        CACTUS,
        COCOA_BEANS,
        MUSHROOM,
        MUSHROOM_ROTATE,
    }
}
