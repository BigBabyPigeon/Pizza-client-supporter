package net.taunahi.ezskyblockscripts.feature.impl;

import cc.polyfrost.oneconfig.utils.Multithreading;
import net.taunahi.ezskyblockscripts.config.TaunahiConfig;
import net.taunahi.ezskyblockscripts.config.page.AutoSellNPCItemsPage;
import net.taunahi.ezskyblockscripts.feature.FeatureManager;
import net.taunahi.ezskyblockscripts.feature.IFeature;
import net.taunahi.ezskyblockscripts.handler.GameStateHandler;
import net.taunahi.ezskyblockscripts.handler.MacroHandler;
import net.taunahi.ezskyblockscripts.util.InventoryUtils;
import net.taunahi.ezskyblockscripts.util.KeyBindUtils;
import net.taunahi.ezskyblockscripts.util.LogUtils;
import net.taunahi.ezskyblockscripts.util.PlayerUtils;
import net.taunahi.ezskyblockscripts.util.helper.Clock;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/*
    Credits to Yuro for this superb class
*/
public class AutoSell implements IFeature {
    private final Minecraft mc = Minecraft.getMinecraft();
    private static AutoSell instance;

    public static AutoSell getInstance() {
        if (instance == null) {
            instance = new AutoSell();
        }
        return instance;
    }

    private boolean enabled = false;

    @Getter
    private MarketType marketType = MarketType.NONE;
    @Getter
    private NPCState npcState = NPCState.NONE;

    public void setNpcState(NPCState npcState) {
        timeoutClock.schedule(30_000);
        this.npcState = npcState;
    }

    @Getter
    private BazaarState bazaarState = BazaarState.NONE;

    public void setBazaarState(BazaarState bazaarState) {
        timeoutClock.schedule(30_000);
        this.bazaarState = bazaarState;
    }

    @Getter
    private SacksState sacksState = SacksState.NONE;

    public void setSacksState(SacksState sacksState) {
        timeoutClock.schedule(30_000);
        this.sacksState = sacksState;
    }

    private boolean emptySacks = false;
    private boolean pickedUpItems = false;

    @Getter
    private final Clock delayClock = new Clock();
    @Getter
    private final Clock timeoutClock = new Clock();
    @Getter
    private final Clock inventoryFilledClock = new Clock();
    @Getter
    private final Clock dontEnableForClock = new Clock();

    @Override
    public String getName() {
        return "Auto Sell";
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
        return false;
    }

    @Override
    public void start() {
        this.enable(false);
    }

    @Override
    public void stop() {
        LogUtils.sendWarning("[Auto Sell] Disabling Auto Sell");
        enabled = false;
        emptySacks = false;
        pickedUpItems = false;
        marketType = MarketType.NONE;
        npcState = NPCState.NONE;
        bazaarState = BazaarState.NONE;
        sacksState = SacksState.NONE;
        delayClock.reset();
        timeoutClock.reset();
        inventoryFilledClock.reset();
        PlayerUtils.closeScreen();
        KeyBindUtils.stopMovement();
        if (MacroHandler.getInstance().isMacroToggled() && !VisitorsMacro.getInstance().isRunning()) {
            Multithreading.schedule(() -> MacroHandler.getInstance().resumeMacro(), 1000, TimeUnit.MILLISECONDS);
        }
        if (TaunahiConfig.enableScheduler && !VisitorsMacro.getInstance().isRunning())
            Scheduler.getInstance().resume();
    }

    @Override
    public void resetStatesAfterMacroDisabled() {
        dontEnableForClock.reset();
    }

    @Override
    public boolean isToggled() {
        return TaunahiConfig.enableAutoSell;
    }

    @Override
    public boolean shouldCheckForFailsafes() {
        return true;
    }

    public void enable(boolean manually) {
        if (enabled && !manually) return;
        if (GameStateHandler.getInstance().getCookieBuffState() != GameStateHandler.BuffState.ACTIVE) {
            LogUtils.sendError("[Auto Sell] You don't have cookie buff active!");
            return;
        }
        if (GameStateHandler.getInstance().getServerClosingSeconds().isPresent()) {
            LogUtils.sendError("[Auto Sell] Server is closing in " + GameStateHandler.getInstance().getServerClosingSeconds().get() + " seconds!");
            return;
        }
        PlayerUtils.closeScreen();
        LogUtils.sendWarning("[Auto Sell] Enabling Auto Sell");
        enabled = true;
        marketType = TaunahiConfig.autoSellMarketType ? MarketType.NPC : MarketType.BAZAAR;
        npcState = NPCState.NONE;
        bazaarState = BazaarState.NONE;
        sacksState = SacksState.NONE;
        delayClock.reset();
        timeoutClock.schedule(30_000);
        if (manually) {
            dontEnableForClock.reset();
        }
        KeyBindUtils.stopMovement();
        if (TaunahiConfig.enableScheduler)
            Scheduler.getInstance().pause();
        if (MacroHandler.getInstance().isMacroToggled()) {
            MacroHandler.getInstance().pauseMacro();
        }
    }


    @SubscribeEvent
    public void onTickShouldEnable(TickEvent.ClientTickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (!isToggled()) return;
        if (isRunning()) return;
        if (!MacroHandler.getInstance().isMacroToggled()) return;
        if (!GameStateHandler.getInstance().inGarden()) return;
        if (GameStateHandler.getInstance().getServerClosingSeconds().isPresent()) return;
        if (GameStateHandler.getInstance().getCookieBuffState() != GameStateHandler.BuffState.ACTIVE) return;
        if (FeatureManager.getInstance().isAnyOtherFeatureEnabled(this)) return;
        if (dontEnableForClock.isScheduled() && !dontEnableForClock.passed()) return;

        if (inventoryFilledClock.isScheduled() && inventoryFilledClock.passed()) {
            if (getInventoryFilledPercentage() >= TaunahiConfig.inventoryFullRatio / 100f) {
                start();
            } else {
                inventoryFilledClock.reset();
                LogUtils.sendDebug("[Auto Sell] Inventory is not full anymore, resetting inventoryFilledClock...");
            }
        } else if (!inventoryFilledClock.isScheduled()) {
            if (getInventoryFilledPercentage() >= TaunahiConfig.inventoryFullRatio / 100f) {
                inventoryFilledClock.schedule((long) (TaunahiConfig.inventoryFullTime * 1000f));
                LogUtils.sendDebug("[Auto Sell] Inventory is full, scheduling inventoryFilledClock...");
            }
        }
    }

    @SubscribeEvent
    public void onTickEnabled(TickEvent.ClientTickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (!isRunning()) return;
        if (!GameStateHandler.getInstance().inGarden()) return;
        if (GameStateHandler.getInstance().getServerClosingSeconds().isPresent()) {
            LogUtils.sendWarning("[Auto Sell] Server is closing in " + GameStateHandler.getInstance().getServerClosingSeconds().get() + " seconds, disabling Auto Sell");
            stop();
            return;
        }
        if (GameStateHandler.getInstance().getCookieBuffState() != GameStateHandler.BuffState.ACTIVE) {
            stop();
            return;
        }
        if (FeatureManager.getInstance().isAnyOtherFeatureEnabled(this, VisitorsMacro.getInstance())) return;

        if (delayClock.isScheduled() && !delayClock.passed()) return;

        switch (sacksState) {
            case NONE:
                break;
            case OPEN_MENU:
                if (mc.currentScreen == null) {
                    LogUtils.sendDebug("[Auto Sell] Opening the Sacks menu");
                    pickedUpItems = false;
                    if (TaunahiConfig.autoSellSacksPlacement)
                        mc.thePlayer.sendChatMessage("/sacks");
                    else
                        InventoryUtils.openInventory();
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    setSacksState(SacksState.SELECT_SACK);
                    return;
                }
                return;
            case SELECT_SACK:
                if (mc.currentScreen == null) {
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    setSacksState(SacksState.OPEN_MENU);
                    return;
                }
                if (TaunahiConfig.autoSellSacksPlacement) {
                    if (InventoryUtils.getInventoryName() != null && !InventoryUtils.getInventoryName().contains("Sacks")) {
                        LogUtils.sendDebug("[Auto Sell] Wrong menu detected!");
                        PlayerUtils.closeScreen();
                        delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                        setSacksState(SacksState.OPEN_MENU);
                        return;
                    } else if (InventoryUtils.getInventoryName() == null) {
                        return;
                    }
                } else {
                    if (!(mc.currentScreen instanceof GuiInventory)) {
                        return;
                    }
                }

                int closeSlot = InventoryUtils.getSlotIdOfItemInContainer("Close");
                if (closeSlot == -1) return;

                LogUtils.sendDebug("[Auto Sell] Detected the Sacks menu");
                int sacksSlot = InventoryUtils.getSlotIdOfItemInContainer("Enchanted Agronomy Sack");
                if (sacksSlot == -1) {
                    LogUtils.sendDebug("[Auto Sell] Couldn't find the \"Enchanted Agronomy Sack\" item in the Sacks menu, closing Sacks menu");
                    PlayerUtils.closeScreen();
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    setSacksState(SacksState.CLOSE_MENU);
                    emptySacks = true;
                    return;
                }

                LogUtils.sendDebug("[Auto Sell] Selecting Enchanted Agronomy Sack");
                InventoryUtils.clickContainerSlot(sacksSlot, InventoryUtils.ClickType.RIGHT, InventoryUtils.ClickMode.PICKUP);
                delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                setSacksState(SacksState.PICKUP);
                return;
            case PICKUP:
                if (mc.currentScreen == null) {
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    setSacksState(SacksState.OPEN_MENU);
                    return;
                }
                if (InventoryUtils.getInventoryName() != null && !InventoryUtils.getInventoryName().contains("Enchanted Agronomy Sack")) {
                    LogUtils.sendDebug("[Auto Sell] Wrong menu detected!");
                    PlayerUtils.closeScreen();
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    setSacksState(SacksState.OPEN_MENU);
                    return;
                } else if (InventoryUtils.getInventoryName() == null) {
                    return;
                }
                int closeSlot1 = InventoryUtils.getSlotIdOfItemInContainer("Go Back");
                if (closeSlot1 == -1) return;
                int pickUpAll = InventoryUtils.getSlotIdOfItemInContainer("Pickup All");
                if (pickUpAll != -1) {
                    if (mc.thePlayer.inventory.getFirstEmptyStack() != -1) {
                        LogUtils.sendDebug("[Auto Sell] Picking up all items from Enchanted Agronomy Sack");
                        pickedUpItems = true;
                        InventoryUtils.clickContainerSlot(pickUpAll, InventoryUtils.ClickType.LEFT, InventoryUtils.ClickMode.PICKUP);
                    } else {
                        LogUtils.sendDebug("[Auto Sell] Inventory is full, closing Sacks menu");
                        PlayerUtils.closeScreen();
                        delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                        setSacksState(SacksState.CLOSE_MENU);
                    }
                } else {
                    LogUtils.sendDebug("[Auto Sell] Couldn't find the \"Pickup All\" item in the Sacks menu, closing Sacks menu...");
                    PlayerUtils.closeScreen();
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    setSacksState(SacksState.CLOSE_MENU);
                    emptySacks = true;
                }
                delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                return;
            case CLOSE_MENU:
                LogUtils.sendDebug("[Auto Sell] Closing Sacks menu");
                if (mc.currentScreen != null)
                    PlayerUtils.closeScreen();
                delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                setSacksState(SacksState.NONE);
                if (TaunahiConfig.autoSellMarketType) {
                    marketType = MarketType.NPC;
                } else {
                    marketType = MarketType.BAZAAR;
                }
                if (emptySacks && !pickedUpItems) {
                    stop();
                }
                return;
        }

        switch (marketType) {
            case NONE:
                break;
            case BAZAAR:
                switch (bazaarState) {
                    case NONE:
                        setBazaarState(BazaarState.OPEN_MENU);
                        break;
                    case OPEN_MENU:
                        if (mc.currentScreen == null) {
                            LogUtils.sendDebug("[Auto Sell] Opening Bazaar menu");
                            mc.thePlayer.sendChatMessage("/bz");
                            delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                            setBazaarState(BazaarState.SELL_INV);
                            break;
                        }
                        break;
                    case SELL_INV:
                        if (mc.currentScreen == null) {
                            delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                            setBazaarState(BazaarState.OPEN_MENU);
                            break;
                        }
                        if (InventoryUtils.getInventoryName() != null && !InventoryUtils.getInventoryName().contains("Bazaar")) {
                            LogUtils.sendDebug("[Auto Sell] Wrong menu detected!");
                            PlayerUtils.closeScreen();
                            delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                            setBazaarState(BazaarState.OPEN_MENU);
                            break;
                        } else if (InventoryUtils.getInventoryName() == null) {
                            break;
                        }
                        LogUtils.sendDebug("[Auto Sell] Detected BZ menu");

                        int sellInventorySlot = InventoryUtils.getSlotIdOfItemInContainer("Sell Inventory Now");
                        int sellSacksSlot = InventoryUtils.getSlotIdOfItemInContainer("Sell Sacks Now");
                        if (sellInventorySlot == -1) {
                            break;
                        }
                        List<String> sellInventoryNowLore = InventoryUtils.getLoreOfItemInContainer(sellInventorySlot);
                        if (sellInventoryNowLore.stream().anyMatch(s -> s.contains("You don't have anything to"))) {
                            if (TaunahiConfig.autoSellSacks && sellSacksSlot != -1) {
                                InventoryUtils.clickContainerSlot(sellSacksSlot, InventoryUtils.ClickType.LEFT, InventoryUtils.ClickMode.PICKUP);
                                setBazaarState(BazaarState.SELL_INV_CONFIRM);
                            } else {
                                LogUtils.sendDebug("[Auto Sell] Nothing to sell, closing Bazaar menu");
                                PlayerUtils.closeScreen();
                                setBazaarState(BazaarState.CLOSE_MENU);
                            }
                            delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                        } else {
                            LogUtils.sendDebug("[Auto Sell] Selling inventory");
                            InventoryUtils.clickContainerSlot(sellInventorySlot, InventoryUtils.ClickType.LEFT, InventoryUtils.ClickMode.PICKUP);
                            delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                            setBazaarState(BazaarState.SELL_INV_CONFIRM);
                        }
                        break;
                    case SELL_INV_CONFIRM:
                        if (mc.currentScreen == null) return;
                        if (InventoryUtils.getInventoryName() != null && !InventoryUtils.getInventoryName().contains("Are you sure?")) {
                            LogUtils.sendError("[Auto Sell] Couldn't find the \"Are you sure?\" inventory, opening the menu again...");
                            PlayerUtils.closeScreen();
                            delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                            setBazaarState(BazaarState.OPEN_MENU);
                            break;
                        } else if (InventoryUtils.getInventoryName() == null) {
                            break;
                        }
                        LogUtils.sendDebug("[Auto Sell] Detected sell confirmation menu");
                        int confirmSlot = InventoryUtils.getSlotIdOfItemInContainer("Selling whole inventory");
                        if (confirmSlot == -1) {
                            LogUtils.sendError("[Auto Sell] Couldn't find the \"Selling whole inventory\" item in the Bazaar menu, opening the menu again...");
                            PlayerUtils.closeScreen();
                            delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                            setBazaarState(BazaarState.OPEN_MENU);
                            break;
                        }
                        InventoryUtils.clickContainerSlot(confirmSlot, InventoryUtils.ClickType.LEFT, InventoryUtils.ClickMode.PICKUP);
                        setBazaarState(BazaarState.SELL_INV);
                        delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                        break;
                    case CLOSE_MENU:
                        if (mc.currentScreen == null) {
                            LogUtils.sendDebug("[Auto Sell] Closing Bazaar menu");
                            setBazaarState(BazaarState.NONE);
                            if (hasShitItemsInInventory()) {
                                marketType = MarketType.NPC;
                            } else {
                                stop();
                            }
                        } else {
                            PlayerUtils.closeScreen();
                        }
                        delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                        break;
                }
                break;
            case NPC:
                switch (npcState) {
                    case NONE:
                        if (sacksState != SacksState.NONE) break;
                        boolean shouldSell = hasAnythingToSell();
                        if (!shouldSell && TaunahiConfig.autoSellSacks && !emptySacks) {
                            setSacksState(SacksState.OPEN_MENU);
                            if (mc.currentScreen != null) {
                                PlayerUtils.closeScreen();
                            }
                            delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                            break;
                        }
                        if (!shouldSell) {
                            LogUtils.sendDebug("[Auto Sell] Nothing to sell, disabling Auto Sell");
                            stop();
                            break;
                        }
                        setNpcState(NPCState.OPEN_MENU);
                        break;
                    case OPEN_MENU:
                        if (mc.currentScreen == null) {
                            LogUtils.sendDebug("[Auto Sell] Opening Trades menu");
                            mc.thePlayer.sendChatMessage("/trades");
                            delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                            setNpcState(NPCState.SELL);
                            break;
                        }
                        if (InventoryUtils.getInventoryName() == null) {
                            break;
                        }
                        if (InventoryUtils.getInventoryName().contains("Trades")) {
                            LogUtils.sendDebug("[Auto Sell] Detected trades menu");
                            setNpcState(NPCState.SELL);
                            break;
                        }

                        LogUtils.sendDebug("[Auto Sell] Detected wrong menu, closing it");
                        PlayerUtils.closeScreen();
                        delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                        setNpcState(NPCState.OPEN_MENU);
                        break;
                    case SELL:
                        if (mc.currentScreen == null) {
                            delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                            setNpcState(NPCState.OPEN_MENU);
                            break;
                        }
                        if (InventoryUtils.getInventoryName() != null && !InventoryUtils.getInventoryName().contains("Trades")) {
                            LogUtils.sendDebug("[Auto Sell] Wrong menu detected!");
                            PlayerUtils.closeScreen();
                            delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                            setNpcState(NPCState.OPEN_MENU);
                            break;
                        } else if (InventoryUtils.getInventoryName() == null) {
                            break;
                        }

                        LogUtils.sendDebug("[Auto Sell] Detected trades menu");
                        ContainerChest chest = (ContainerChest) mc.thePlayer.openContainer;
                        IInventory inv = chest.getLowerChestInventory();

                        for (Slot slot : chest.inventorySlots) {
                            if (slot == null || !slot.getHasStack() || slot.slotNumber < inv.getSizeInventory())
                                continue;
                            String name = StringUtils.stripControlCodes(slot.getStack().getDisplayName());
                            if (slot.getStack().getItem() instanceof ItemTool) continue;
                            if (!shouldSell(name)) continue;
                            LogUtils.sendDebug("[Auto Sell] Selling " + name);
                            InventoryUtils.clickSlotWithId(slot.slotNumber, InventoryUtils.ClickType.LEFT, InventoryUtils.ClickMode.PICKUP, chest.windowId);
                            delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                            return;
                        }

                        LogUtils.sendDebug("[Auto Sell] Nothing to sell, closing Trades menu");
                        PlayerUtils.closeScreen();
                        setNpcState(NPCState.CLOSE_MENU);
                        delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                        break;
                    case CLOSE_MENU:
                        if (mc.currentScreen == null) {
                            LogUtils.sendDebug("[Auto Sell] Closing Trades menu");
                            setNpcState(NPCState.NONE);
                            if (TaunahiConfig.autoSellSacks && !emptySacks) {
                                setSacksState(SacksState.OPEN_MENU);
                            } else {
                                stop();
                            }
                        } else {
                            PlayerUtils.closeScreen();
                        }
                        delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                        break;
                }
                break;
        }

        if (timeoutClock.isScheduled() && timeoutClock.passed()) {
            LogUtils.sendWarning("[Auto Sell] Timeout reached, disabling Auto Sell");
            stop();
        }
    }

    @SubscribeEvent
    public void onKeypress(InputEvent.KeyInputEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (!isRunning()) return;

        if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
            LogUtils.sendWarning("[Auto Sell] Stopping Auto Sell manually");
            stop();
        }
    }

    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) {
        if (!enabled) return;
        if (event.type != 0) return;
        String message = StringUtils.stripControlCodes(event.message.getUnformattedText());
        if (message.contains("You do not have the Sack of Sacks unlocked!")) {
            emptySacks = true;
            sacksState = SacksState.CLOSE_MENU;
        }
        if (message.startsWith("[Bazaar] No items could be matched")) {
            bazaarState = BazaarState.CLOSE_MENU;
        }
    }

    private boolean hasAnythingToSell() {
        for (int i = 0; i < mc.thePlayer.inventory.getSizeInventory(); i++) {
            if (i > 38) break;
            ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(i);
            if (itemStack == null) continue;
            String name = StringUtils.stripControlCodes(itemStack.getDisplayName());
            if (itemStack.getItem() instanceof ItemTool) continue;
            if (itemStack.getItem() instanceof ItemArmor) continue;
            if (name.equals("Basket of Seeds")) continue;
            if (shouldSell(name)) {
                LogUtils.sendDebug("[Auto Sell] Found " + name + " at slot " + i);
                return true;
            }
        }
        return false;
    }

    private boolean hasShitItemsInInventory() {
        for (int i = 9; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                String name = mc.thePlayer.inventoryContainer.getSlot(i).getStack().getDisplayName();
                if (shouldSellCustomItem(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean shouldSell(String name) {
        if (shouldSellCustomItem(name)) return true;
        return crops.stream().anyMatch(crop -> StringUtils.stripControlCodes(name).startsWith(crop));
    }

    private boolean shouldSellCustomItem(String name) {
        if (AutoSellNPCItemsPage.autoSellRunes && name.contains(" Rune") && !name.contains("Music")) return true;
        if (AutoSellNPCItemsPage.autoSellDeadBush && name.contains("Dead Bush")) return true;
        if (AutoSellNPCItemsPage.autoSellIronHoe && name.contains("Iron Hoe")) return true;
        if (!AutoSellNPCItemsPage.autoSellCustomItems.isEmpty()) {
            List<String> customItems = Arrays.asList(AutoSellNPCItemsPage.autoSellCustomItems.split("\\|"));
            System.out.println(customItems);
            return customItems.stream().anyMatch(item -> StringUtils.stripControlCodes(name.toLowerCase()).contains(item.toLowerCase()));
        }
        return false;
    }

    private final List<String> crops = Arrays.asList(
            "Brown Mushroom", "Enchanted Brown Mushroom", "Brown Mushroom Block", "Brown Enchanted Mushroom Block",
            "Red Mushroom", "Enchanted Red Mushroom", "Red Mushroom Block", "Red Enchanted Mushroom Block",
            "Nether Wart", "Enchanted Nether Wart", "Mutant Nether Wart",
            "Sugar Cane", "Enchanted Sugar", "Enchanted Sugar Cane",
            "Potato", "Enchanted Potato", "Enchanted Baked Potato",
            "Carrot", "Enchanted Carrot", "Enchanted Golden Carrot",
            "Seeds", "Enchanted Seeds", "Box of Seeds",
            "Wheat", "Enchanted Bread", "Hay Bale", "Enchanted Hay Bale", "Enchanted Wheat", "Tightly-Tied Hay Bale",
            "Pumpkin", "Enchanted Pumpkin", "Polished Pumpkin",
            "Melon", "Enchanted Melon", "Enchanted Melon Block",
            "Cocoa Beans", "Enchanted Cocoa Bean", "Enchanted Cookie",
            "Cactus", "Cactus Green", "Enchanted Cactus Green", "Enchanted Cactus",
            "Cropie", "Squash", "Fermento", "Stone");

    private float getInventoryFilledPercentage() {
        int filled = 0;
        for (int i = 8; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                filled++;
            }
        }
        return (float) filled / 36;
    }


    public enum MarketType {
        NONE,
        BAZAAR,
        NPC
    }

    public enum NPCState {
        NONE,
        OPEN_MENU,
        SELL,
        CLOSE_MENU
    }

    public enum BazaarState {
        NONE,
        OPEN_MENU,
        SELL_INV,
        SELL_INV_CONFIRM,
        CLOSE_MENU
    }

    public enum SacksState {
        NONE,
        OPEN_MENU,
        SELECT_SACK,
        PICKUP,
        CLOSE_MENU
    }
}
