package net.taunahi.ezskyblockscripts.feature.impl;

import cc.polyfrost.oneconfig.utils.Multithreading;
import net.taunahi.ezskyblockscripts.config.TaunahiConfig;
import net.taunahi.ezskyblockscripts.feature.FeatureManager;
import net.taunahi.ezskyblockscripts.feature.IFeature;
import net.taunahi.ezskyblockscripts.handler.GameStateHandler;
import net.taunahi.ezskyblockscripts.handler.MacroHandler;
import net.taunahi.ezskyblockscripts.handler.RotationHandler;
import net.taunahi.ezskyblockscripts.util.InventoryUtils;
import net.taunahi.ezskyblockscripts.util.KeyBindUtils;
import net.taunahi.ezskyblockscripts.util.LogUtils;
import net.taunahi.ezskyblockscripts.util.PlayerUtils;
import net.taunahi.ezskyblockscripts.util.helper.Clock;
import net.taunahi.ezskyblockscripts.util.helper.Rotation;
import net.taunahi.ezskyblockscripts.util.helper.RotationConfiguration;
import net.taunahi.ezskyblockscripts.util.helper.SignUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.StringUtils;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class AutoGodPot implements IFeature {
    private static AutoGodPot instance;
    private final Minecraft mc = Minecraft.getMinecraft();
    @Getter
    private final Clock delayClock = new Clock();
    @Getter
    private final Clock stuckClock = new Clock();
    private final int STUCK_DELAY = (int) (7_500 + TaunahiConfig.macroGuiDelay + TaunahiConfig.macroGuiDelayRandomness);
    private final RotationHandler rotation = RotationHandler.getInstance();
    private final Vec3 ahLocation1 = new Vec3(-17.5, 72, -91.5);
    private final Vec3 ahLocation2 = new Vec3(-30.5, 73, -87.5);
    private final Vec3 bitsShopLocation1 = new Vec3(5.5, 72, -97.5);
    private final Vec3 bitsShopLocation2 = new Vec3(1.5, 72, -100.5);
    private final ArrayList<Integer> badItems = new ArrayList<>();
    private final AxisAlignedBB ahArea = new AxisAlignedBB(-31, 75, -87, -33, 70, -89);
    private boolean shouldTpToGarden = true;
    private boolean enabled = false;
    private boolean activating = false;
    @Getter
    private AhState ahState = AhState.NONE;
    @Getter
    private GodPotMode godPotMode = GodPotMode.NONE;
    @Getter
    private GoingToAHState goingToAHState = GoingToAHState.NONE;
    @Getter
    private ConsumePotState consumePotState = ConsumePotState.NONE;
    @Getter
    private MovePotState movePotState = MovePotState.SWAP_POT_TO_HOTBAR_PICKUP;
    private int hotbarSlot = -1;
    @Getter
    private BackpackState backpackState = BackpackState.NONE;
    @Getter
    private BitsShopState bitsShopState = BitsShopState.NONE;
    private Slot godPotItem;

    public static AutoGodPot getInstance() {
        if (instance == null) {
            instance = new AutoGodPot();
        }
        return instance;
    }

    @Override
    public String getName() {
        return "Auto God Pot";
    }

    @Override
    public boolean isRunning() {
        return enabled || activating;
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
        if (enabled) return;

        if (InventoryUtils.hasItemInInventory("God Pot")) {
            godPotMode = GodPotMode.FROM_INVENTORY;
            shouldTpToGarden = false;
        } else {
            if (TaunahiConfig.autoGodPotFromBackpack) {
                godPotMode = GodPotMode.FROM_BACKPACK;
                shouldTpToGarden = false;
            } else if (TaunahiConfig.autoGodPotFromBits) {
                godPotMode = GodPotMode.FROM_BITS_SHOP;
                shouldTpToGarden = true;
            } else if (TaunahiConfig.autoGodPotFromAH && GameStateHandler.getInstance().getCookieBuffState() == GameStateHandler.BuffState.ACTIVE) {
                godPotMode = GodPotMode.FROM_AH_COOKIE;
                shouldTpToGarden = false;
            } else if (TaunahiConfig.autoGodPotFromAH) {
                godPotMode = GodPotMode.FROM_AH_NO_COOKIE;
                shouldTpToGarden = true;
            } else {
                LogUtils.sendError("[Auto God Pot] You didn't activate any God Pot source! Disabling");
                TaunahiConfig.autoGodPot = false;
                return;
            }
        }
        enabled = true;
        KeyBindUtils.stopMovement();
        if (MacroHandler.getInstance().isMacroToggled()) {
            MacroHandler.getInstance().pauseMacro();
        }
        LogUtils.sendWarning("[Auto God Pot] Enabled!");
        stuckClock.schedule(STUCK_DELAY);
    }

    @Override
    public void stop() {
        if (!enabled) return;
        enabled = false;
        stuckClock.reset();
        delayClock.reset();
        rotation.reset();
        resetAHState();
        resetGoingToAHState();
        resetConsumePotState();
        resetBackpackState();
        resetBitsShopState();
        godPotMode = GodPotMode.NONE;
        KeyBindUtils.stopMovement();
        if (shouldTpToGarden)
            MacroHandler.getInstance().getCurrentMacro().ifPresent(cm -> cm.triggerWarpGarden(true, true));
        shouldTpToGarden = true;
        Multithreading.schedule(() -> {
            LogUtils.sendWarning("[Auto God Pot] Disabled!");
            if (MacroHandler.getInstance().isMacroToggled()) {
                MacroHandler.getInstance().resumeMacro();
            }
        }, 4_000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void resetStatesAfterMacroDisabled() {
    }

    @Override
    public boolean isToggled() {
        return TaunahiConfig.autoGodPot;
    }

    @Override
    public boolean shouldCheckForFailsafes() {
        return goingToAHState != GoingToAHState.NONE && bitsShopState != BitsShopState.NONE;
    }

    @SubscribeEvent
    public void onTickShouldEnable(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (!isToggled()) return;
        if (isRunning()) return;
        if (!MacroHandler.getInstance().isMacroToggled()) return;
        if (FeatureManager.getInstance().isAnyOtherFeatureEnabled(this)) return;
        if (!GameStateHandler.getInstance().inGarden()) return;
        if (GameStateHandler.getInstance().getServerClosingSeconds().isPresent()) return;

        if (GameStateHandler.getInstance().getLocation() != GameStateHandler.Location.LOBBY && GameStateHandler.getInstance().getGodPotState() == GameStateHandler.BuffState.NOT_ACTIVE) {
            if (!enabled && !activating) {
                LogUtils.sendWarning("[Auto God Pot] Your God Pot Buff is not active! Activating Auto God Pot in 1.5 seconds!");
                activating = true;
                KeyBindUtils.stopMovement();
                Multithreading.schedule(() -> {
                    if (GameStateHandler.getInstance().getGodPotState() == GameStateHandler.BuffState.NOT_ACTIVE) {
                        start();
                        activating = false;
                    }
                }, 1_500, TimeUnit.MILLISECONDS);
            }
        }
    }

    @SubscribeEvent
    public void onTickUpdate(TickEvent.ClientTickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (!isToggled()) return;
        if (!enabled) return;
        if (!MacroHandler.getInstance().isMacroToggled()) return;
        if (FeatureManager.getInstance().isAnyOtherFeatureEnabled(this)) return;

        if (GameStateHandler.getInstance().getLocation() == GameStateHandler.Location.TELEPORTING) {
            stuckClock.schedule(STUCK_DELAY);
            return;
        }

        if (delayClock.isScheduled() && !delayClock.passed()) return;

        switch (godPotMode) {

            case NONE:
                LogUtils.sendWarning("[Auto God Pot] You didn't activate any God Pot source! Disabling");
                stop();
                break;
            case FROM_AH_COOKIE:
                onAhState(false);
                break;
            case FROM_AH_NO_COOKIE:
                if (isInAHArea()) {
                    onAhState(true);
                } else {
                    onGoingToAHState();
                }
                break;
            case FROM_INVENTORY:
                onInventoryState();
                break;
            case FROM_BACKPACK:
                onBackpackState();
                break;
            case FROM_BITS_SHOP:
                onBitsShop();
                break;
        }
    }

    private void onAhState(boolean rightClick) {
        switch (ahState) {
            case NONE:
                KeyBindUtils.stopMovement();
                setGoingToAHState(GoingToAHState.NONE);
                setAhState(AhState.OPEN_AH);
                break;
            case OPEN_AH:
                if (mc.currentScreen != null) {
                    PlayerUtils.closeScreen();
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    break;
                }
                if (rightClick) {
                    if (rotation.isRotating()) return;
                    long randomTime = TaunahiConfig.getRandomRotationTime();
                    Optional<Entity> entity = mc.theWorld.loadedEntityList.stream().filter(e -> {
                        double distance = Math.sqrt(e.getDistanceSqToCenter(mc.thePlayer.getPosition()));
                        String name = StringUtils.stripControlCodes(e.getCustomNameTag());
                        return distance < 4.5 && name != null && name.equals("Auction Agent");
                    }).findFirst();
                    if (entity.isPresent()) {
                        Rotation rot = rotation.getRotation(entity.get());
                        rotation.easeTo(new RotationConfiguration(
                                new Rotation(rot.getYaw(), rot.getPitch()), randomTime, null
                        ));
                        delayClock.schedule(randomTime + 150);
                        Multithreading.schedule(() -> {
                            KeyBindUtils.rightClick();
                            setAhState(AhState.OPEN_BROWSER);
                        }, randomTime - 50, TimeUnit.MILLISECONDS);
                    } else {
                        LogUtils.sendError("[Auto God Pot] Could not find Auction House NPC!");
                        setAhState(AhState.OPEN_AH);
                        delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                        break;
                    }
                } else {
                    mc.thePlayer.sendChatMessage("/ah");
                }
                setAhState(AhState.OPEN_BROWSER);
                delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                break;
            case OPEN_BROWSER:
                if (mc.currentScreen == null) {
                    setAhState(AhState.OPEN_AH);
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    break;
                }
                if (!Objects.requireNonNull(InventoryUtils.getInventoryName()).contains("Auction House")) break;
                Slot ahBrowserItem = InventoryUtils.getSlotOfItemInContainer("Auctions Browser");
                if (ahBrowserItem == null) break;
                InventoryUtils.clickContainerSlot(ahBrowserItem.slotNumber, InventoryUtils.ClickType.LEFT, InventoryUtils.ClickMode.PICKUP);
                setAhState(AhState.CLICK_SEARCH);
                delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                break;
            case CLICK_SEARCH:
                if (mc.currentScreen == null) {
                    setAhState(AhState.OPEN_AH);
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    break;
                }
                if (Objects.requireNonNull(InventoryUtils.getInventoryName()).startsWith("Auctions: \"God Potion\"")) {
                    setAhState(AhState.SORT_ITEMS);
                    break;
                }
                if (checkIfWrongInventory("Auctions Browser") && !Objects.requireNonNull(InventoryUtils.getInventoryName()).startsWith("Auctions: \""))
                    break;
                Slot searchItem = InventoryUtils.getSlotOfItemInContainer("Search");
                if (searchItem == null) break;
                InventoryUtils.clickContainerSlot(searchItem.slotNumber, InventoryUtils.ClickType.LEFT, InventoryUtils.ClickMode.PICKUP);
                setAhState(AhState.SORT_ITEMS);
                delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                Multithreading.schedule(() -> SignUtils.setTextToWriteOnString("God Potion"), (long) (400 + Math.random() * 400), TimeUnit.MILLISECONDS);
                break;
            case SORT_ITEMS:
                if (mc.currentScreen == null) {
                    setAhState(AhState.OPEN_AH);
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    break;
                }
                if (checkIfWrongInventory("Auctions: \"God Potion\"")) break;
                Slot sortItem = InventoryUtils.getSlotOfItemInContainer("Sort");
                if (sortItem == null) break;
                if (!InventoryUtils.getItemLore(sortItem.getStack()).contains("▶ Lowest Price")) {
                    InventoryUtils.clickContainerSlot(sortItem.slotNumber, InventoryUtils.ClickType.LEFT, InventoryUtils.ClickMode.PICKUP);
                    setAhState(AhState.SORT_ITEMS);
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    break;
                }
                setAhState(AhState.CHECK_IF_BIN);
                delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                break;
            case CHECK_IF_BIN:
                if (mc.currentScreen == null) {
                    setAhState(AhState.OPEN_AH);
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    break;
                }
                if (checkIfWrongInventory("Auctions: \"God Potion\"")) break;
                Slot binItem = InventoryUtils.getSlotOfItemInContainer("BIN Filter");
                if (binItem == null) break;
                ItemStack binItemStack = binItem.getStack();
                if (!InventoryUtils.getItemLore(binItemStack).contains("▶ BIN Only")) {
                    InventoryUtils.clickContainerSlot(binItem.slotNumber, InventoryUtils.ClickType.LEFT, InventoryUtils.ClickMode.PICKUP);
                    setAhState(AhState.CHECK_IF_BIN);
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    break;
                }
                setAhState(AhState.SELECT_ITEM);
                break;
            case SELECT_ITEM:
                if (mc.currentScreen == null) {
                    setAhState(AhState.OPEN_AH);
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    break;
                }
                if (checkIfWrongInventory("Auctions: \"God Potion\"")) break;
                for (Slot slot : mc.thePlayer.openContainer.inventorySlots) {
                    if (!slot.getHasStack()) continue;

                    String itemName = StringUtils.stripControlCodes(slot.getStack().getDisplayName());
                    if (itemName.contains("God Potion") && !badItems.contains(slot.slotNumber)) {
                        ItemStack itemLore = slot.getStack();
                        if (InventoryUtils.getItemLore(itemLore).contains("Status: Sold!")) {
                            badItems.add(slot.slotNumber);
                            continue;
                        }
                        godPotItem = slot;
                        break;
                    }
                }
                if (godPotItem == null) {
                    LogUtils.sendError("[Auto God Pot] Could not find any God Pot in AH! Disabling Auto God Pot!");
                    TaunahiConfig.autoGodPot = false;
                    stop();
                    return;
                }

                InventoryUtils.clickContainerSlot(godPotItem.slotNumber, InventoryUtils.ClickType.LEFT, InventoryUtils.ClickMode.PICKUP);
                setAhState(AhState.BUY_ITEM);
                delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                break;
            case BUY_ITEM:
                if (mc.currentScreen == null) {
                    setAhState(AhState.OPEN_AH);
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    break;
                }
                if (checkIfWrongInventory("BIN Auction View")) break;
                Slot buyItem = InventoryUtils.getSlotOfItemInContainer("Buy Item Right Now");
                if (buyItem == null) {
                    if (InventoryUtils.getSlotOfItemInContainer("Collect Auction") != null) {
                        badItems.add(godPotItem.slotNumber);
                        Slot goBack = InventoryUtils.getSlotOfItemInContainer("Go Back");
                        if (goBack == null) break;
                        InventoryUtils.clickContainerSlot(goBack.slotNumber, InventoryUtils.ClickType.LEFT, InventoryUtils.ClickMode.PICKUP);
                        setAhState(AhState.SELECT_ITEM);
                        delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    }
                    break;
                }
                ItemStack buyItemLore = buyItem.getStack();
                if (InventoryUtils.getItemLore(buyItemLore).contains("Cannot afford bid!")) {
                    LogUtils.sendError("[Auto God Pot] Cannot afford bid!");
                    setAhState(AhState.CLOSE_AH);
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    break;
                }
                InventoryUtils.clickContainerSlot(buyItem.slotNumber, InventoryUtils.ClickType.LEFT, InventoryUtils.ClickMode.PICKUP);
                setAhState(AhState.CONFIRM_PURCHASE);
                delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                break;
            case CONFIRM_PURCHASE:
                if (mc.currentScreen == null) {
                    setAhState(AhState.OPEN_AH);
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    break;
                }
                if (checkIfWrongInventory("Confirm Purchase")) break;
                Slot confirmPurchase = InventoryUtils.getSlotOfItemInContainer("Confirm");
                if (confirmPurchase == null) break;
                InventoryUtils.clickContainerSlot(confirmPurchase.slotNumber, InventoryUtils.ClickType.LEFT, InventoryUtils.ClickMode.PICKUP);
                setAhState(AhState.WAIT_FOR_CONFIRMATION);
                delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                break;
            case WAIT_FOR_CONFIRMATION:
            case COLLECT_ITEM_WAIT_FOR_COLLECT:
                break;
            case COLLECT_ITEM_OPEN_AH:
                if (mc.currentScreen != null) {
                    PlayerUtils.closeScreen();
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    break;
                }
                mc.thePlayer.sendChatMessage("/ah");
                setAhState(AhState.COLLECT_ITEM_VIEW_BIDS);
                delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                break;
            case COLLECT_ITEM_VIEW_BIDS:
                if (mc.currentScreen == null) {
                    setAhState(AhState.COLLECT_ITEM_OPEN_AH);
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    break;
                }
                if (!Objects.requireNonNull(InventoryUtils.getInventoryName()).contains("Auction House")) break;
                Slot viewBids = InventoryUtils.getSlotOfItemInContainer("View Bids");
                if (viewBids == null) break;
                InventoryUtils.clickContainerSlot(viewBids.slotNumber, InventoryUtils.ClickType.LEFT, InventoryUtils.ClickMode.PICKUP);
                setAhState(AhState.COLLECT_ITEM_CLICK_ITEM);
                delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                break;
            case COLLECT_ITEM_CLICK_ITEM:
                if (mc.currentScreen == null) {
                    setAhState(AhState.COLLECT_ITEM_OPEN_AH);
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    break;
                }
                if (!Objects.requireNonNull(InventoryUtils.getInventoryName()).contains("Bids")) break;
                Slot godPotSlot = InventoryUtils.getSlotOfItemInContainer("God Potion");
                if (godPotSlot == null) break;
                InventoryUtils.clickContainerSlot(godPotSlot.slotNumber, InventoryUtils.ClickType.LEFT, InventoryUtils.ClickMode.PICKUP);
                setAhState(AhState.COLLECT_ITEM_CLICK_COLLECT);
                delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                break;
            case COLLECT_ITEM_CLICK_COLLECT:
                if (mc.currentScreen == null) {
                    setAhState(AhState.COLLECT_ITEM_OPEN_AH);
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    break;
                }
                if (!Objects.requireNonNull(InventoryUtils.getInventoryName()).contains("BIN Auction View")) break;
                Slot collectItem = InventoryUtils.getSlotOfItemInContainer("Collect Auction");
                if (collectItem == null) break;
                InventoryUtils.clickContainerSlot(collectItem.slotNumber, InventoryUtils.ClickType.LEFT, InventoryUtils.ClickMode.PICKUP);
                setAhState(AhState.COLLECT_ITEM_WAIT_FOR_COLLECT);
                delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                break;
            case CLOSE_AH:
                if (mc.currentScreen != null) {
                    PlayerUtils.closeScreen();
                }
                delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                setAhState(AhState.NONE);
                setGodPotMode(GodPotMode.FROM_INVENTORY);
                break;
        }
    }

    private void onGoingToAHState() {
        switch (goingToAHState) {
            case NONE:
                setGoingToAHState(GoingToAHState.TELEPORT_TO_HUB);
                delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                break;
            case TELEPORT_TO_HUB:
                if (mc.currentScreen != null) {
                    PlayerUtils.closeScreen();
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    break;
                }
                mc.thePlayer.sendChatMessage("/hub");
                setGoingToAHState(GoingToAHState.ROTATE_TO_AH_1);
                delayClock.schedule(5_000);
                break;
            case ROTATE_TO_AH_1:
                if (GameStateHandler.getInstance().getLocation() != GameStateHandler.Location.HUB) {
                    setGoingToAHState(GoingToAHState.TELEPORT_TO_HUB);
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    break;
                }
                KeyBindUtils.stopMovement();
                long randomTime = TaunahiConfig.getRandomRotationTime();
                Rotation rot = rotation.getRotation(ahLocation1);
                rotation.easeTo(
                        new RotationConfiguration(new Rotation(rot.getYaw(), rot.getPitch()), randomTime, null
                        ));
                delayClock.schedule(randomTime + 150);
                setGoingToAHState(GoingToAHState.GO_TO_AH_1);
                break;
            case GO_TO_AH_1:
                if (mc.currentScreen != null) {
                    KeyBindUtils.stopMovement();
                    setGoingToAHState(GoingToAHState.ROTATE_TO_AH_1);
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    break;
                }
                if (rotation.isRotating()) break;
                if (mc.thePlayer.getPositionVector().distanceTo(ahLocation1) < 1.5) {
                    KeyBindUtils.stopMovement();
                    setGoingToAHState(GoingToAHState.ROTATE_TO_AH_2);
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    break;
                }
                KeyBindUtils.holdThese(mc.gameSettings.keyBindSprint, mc.gameSettings.keyBindForward);
                stuckClock.schedule(STUCK_DELAY);
                break;
            case ROTATE_TO_AH_2:
                if (GameStateHandler.getInstance().getLocation() != GameStateHandler.Location.HUB) {
                    setGoingToAHState(GoingToAHState.TELEPORT_TO_HUB);
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    break;
                }
                KeyBindUtils.stopMovement();
                long randomTime2 = TaunahiConfig.getRandomRotationTime();
                Rotation rot2 = rotation.getRotation(ahLocation2);
                rotation.easeTo(
                        new RotationConfiguration(new Rotation(rot2.getYaw(), rot2.getPitch()), randomTime2, null
                        ));
                delayClock.schedule(randomTime2 + 150);
                setGoingToAHState(GoingToAHState.GO_TO_AH_2);
                break;
            case GO_TO_AH_2:
                if (mc.currentScreen != null) {
                    KeyBindUtils.stopMovement();
                    setGoingToAHState(GoingToAHState.ROTATE_TO_AH_2);
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    break;
                }
                if (rotation.isRotating()) break;
                if (mc.thePlayer.getPositionVector().distanceTo(ahLocation2) < 1.5) {
                    KeyBindUtils.stopMovement();
                    setGoingToAHState(GoingToAHState.NONE);
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    break;
                }
                KeyBindUtils.holdThese(mc.gameSettings.keyBindSprint, mc.gameSettings.keyBindForward);
                stuckClock.schedule(STUCK_DELAY);
                break;
        }
    }

    private void onInventoryState() {
        switch (consumePotState) {
            case NONE:
                if (InventoryUtils.hasItemInHotbar("God Potion")) {
                    setConsumePotState(ConsumePotState.SELECT_POT);
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                } else if (InventoryUtils.hasItemInInventory("God Potion")) {
                    setConsumePotState(ConsumePotState.MOVE_POT_TO_HOTBAR);
                    setMovePotState(MovePotState.SWAP_POT_TO_HOTBAR_PICKUP);
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                } else {
                    noGodPotInInventory();
                }
                break;
            case MOVE_POT_TO_HOTBAR:
                switch (movePotState) {
                    case SWAP_POT_TO_HOTBAR_PICKUP:
                        if (mc.currentScreen == null) {
                            InventoryUtils.openInventory();
                            delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                            break;
                        }
                        int potSlot = InventoryUtils.getSlotIdOfItemInInventory("God Potion");
                        if (potSlot == -1) {
                            LogUtils.sendError("Something went wrong while trying to get the slot of the cookie!");
                            stop();
                            break;
                        }
                        this.hotbarSlot = potSlot;
                        InventoryUtils.clickSlot(potSlot, InventoryUtils.ClickType.LEFT, InventoryUtils.ClickMode.PICKUP);
                        setMovePotState(MovePotState.SWAP_POT_TO_HOTBAR_PUT);
                        delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                        break;
                    case SWAP_POT_TO_HOTBAR_PUT:
                        if (mc.currentScreen == null) {
                            LogUtils.sendError("Something went wrong while trying to get the slot of the cookie!");
                            stop();
                            break;
                        }
                        Slot newSlot = InventoryUtils.getSlotOfId(43);
                        if (newSlot != null && newSlot.getHasStack()) {
                            setMovePotState(MovePotState.SWAP_POT_TO_HOTBAR_PUT_BACK);
                        } else {
                            setMovePotState(MovePotState.PUT_ITEM_BACK_PICKUP);
                            setConsumePotState(ConsumePotState.SELECT_POT);
                        }
                        InventoryUtils.clickSlot(43, InventoryUtils.ClickType.LEFT, InventoryUtils.ClickMode.PICKUP);
                        delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                        break;
                    case SWAP_POT_TO_HOTBAR_PUT_BACK:
                        if (mc.currentScreen == null) {
                            LogUtils.sendError("Something went wrong while trying to get the slot of the cookie!");
                            stop();
                            break;
                        }
                        InventoryUtils.clickSlot(this.hotbarSlot, InventoryUtils.ClickType.LEFT, InventoryUtils.ClickMode.PICKUP);
                        setMovePotState(MovePotState.PUT_ITEM_BACK_PICKUP);
                        setConsumePotState(ConsumePotState.SELECT_POT);
                        delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                        break;
                    case PUT_ITEM_BACK_PICKUP:
                        if (mc.currentScreen == null) {
                            InventoryUtils.openInventory();
                            delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                            break;
                        }
                        InventoryUtils.clickSlot(this.hotbarSlot, InventoryUtils.ClickType.LEFT, InventoryUtils.ClickMode.PICKUP);
                        setMovePotState(MovePotState.PUT_ITEM_BACK_PUT);
                        delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                        break;
                    case PUT_ITEM_BACK_PUT:
                        if (mc.currentScreen == null) {
                            LogUtils.sendError("Something went wrong while trying to get the slot of the cookie!");
                            stop();
                            break;
                        }
                        InventoryUtils.clickSlot(43, InventoryUtils.ClickType.LEFT, InventoryUtils.ClickMode.PICKUP);
                        delayClock.schedule(3_000);
                        Multithreading.schedule(this::stop, 1_500, TimeUnit.MILLISECONDS);
                        break;
                }
                break;
            case SELECT_POT:
                if (mc.currentScreen != null) {
                    PlayerUtils.closeScreen();
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    break;
                }
                int potSlot = InventoryUtils.getSlotIdOfItemInHotbar("God Potion");
                LogUtils.sendDebug("Pot Slot: " + potSlot);
                if (potSlot == -1 || potSlot > 8) {
                    LogUtils.sendError("Something went wrong while trying to get the slot of the cookie!");
                    stop();
                    break;
                }
                mc.thePlayer.inventory.currentItem = potSlot;
                setConsumePotState(ConsumePotState.RIGHT_CLICK_POT);
                delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                break;
            case RIGHT_CLICK_POT:
                if (mc.currentScreen != null) {
                    PlayerUtils.closeScreen();
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    break;
                }
                mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
                setConsumePotState(ConsumePotState.WAIT_FOR_CONSUME);
                delayClock.schedule(3_000);
                break;
            case WAIT_FOR_CONSUME:
                break;
        }
    }

    private void onBackpackState() {
        switch (backpackState) {
            case NONE:
                if (InventoryUtils.hasItemInHotbar("God Potion")) {
                    setGodPotMode(GodPotMode.FROM_INVENTORY);
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                } else {
                    setBackpackState(BackpackState.OPEN_STORAGE);
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                }
                break;
            case OPEN_STORAGE:
                if (mc.currentScreen != null) {
                    PlayerUtils.closeScreen();
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    break;
                }
                if (TaunahiConfig.autoGodPotStorageType) {
                    mc.thePlayer.sendChatMessage("/ec " + TaunahiConfig.autoGodPotBackpackNumber);
                } else {
                    mc.thePlayer.sendChatMessage("/bp " + TaunahiConfig.autoGodPotBackpackNumber);
                }
                setBackpackState(BackpackState.MOVE_POT_TO_INVENTORY);
                delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                break;
            case MOVE_POT_TO_INVENTORY:
                if (mc.currentScreen == null) {
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    setBackpackState(BackpackState.OPEN_STORAGE);
                    break;
                }
                if (InventoryUtils.getInventoryName() == null) break;

                for (Slot slot : mc.thePlayer.openContainer.inventorySlots) {
                    if (!slot.getHasStack()) continue;

                    String itemName = StringUtils.stripControlCodes(slot.getStack().getDisplayName());
                    if (itemName.contains("God Potion")) {
                        InventoryUtils.clickContainerSlot(slot.slotNumber, InventoryUtils.ClickType.LEFT, InventoryUtils.ClickMode.QUICK_MOVE);
                        setBackpackState(BackpackState.END);
                        delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                        return;
                    }
                }
                noGodPotInInventory();
                break;
            case END:
                if (mc.currentScreen != null) {
                    PlayerUtils.closeScreen();
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    break;
                }
                if (InventoryUtils.hasItemInHotbar("God Potion") || InventoryUtils.hasItemInInventory("God Potion")) {
                    setGodPotMode(GodPotMode.FROM_INVENTORY);
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                } else {
                    setBackpackState(BackpackState.OPEN_STORAGE);
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                }
                break;
        }
    }

    private boolean isInAHArea() {
        Vec3 playerPosition = mc.thePlayer.getPositionVector();
        return ahArea.isVecInside(playerPosition) && GameStateHandler.getInstance().getLocation() == GameStateHandler.Location.HUB;
    }

    private void noGodPotInInventory() {
        LogUtils.sendError("[Auto God Pot] Could not find any God Pot in Backpack! Going to buy one!");
        shouldTpToGarden = true;
        if (TaunahiConfig.autoGodPotFromBits) {
            setGodPotMode(GodPotMode.FROM_BITS_SHOP);
        } else if (TaunahiConfig.autoGodPotFromAH && GameStateHandler.getInstance().getCookieBuffState() == GameStateHandler.BuffState.NOT_ACTIVE) {
            setGodPotMode(GodPotMode.FROM_AH_NO_COOKIE);
        } else if (TaunahiConfig.autoGodPotFromAH && GameStateHandler.getInstance().getCookieBuffState() == GameStateHandler.BuffState.ACTIVE) {
            setGodPotMode(GodPotMode.FROM_AH_COOKIE);
        } else {
            LogUtils.sendError("[Auto God Pot] You didn't activate any God Pot source! Disabling");
            stop();
            TaunahiConfig.autoGodPot = false;
        }
    }

    private void onBitsShop() {
        switch (bitsShopState) {
            case NONE:
                if (GameStateHandler.getInstance().getBits() < 1_500) {
                    LogUtils.sendError("[Auto God Pot] You don't have enough bits to buy a God Pot! Disabling Auto God Pot!");
                    TaunahiConfig.autoGodPot = false;
                    stop();
                    return;
                }
                setBitsShopState(BitsShopState.TELEPORT_TO_HUB);
                delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                break;
            case TELEPORT_TO_HUB:
                if (mc.currentScreen != null) {
                    PlayerUtils.closeScreen();
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    break;
                }
                mc.thePlayer.sendChatMessage("/hub");
                setBitsShopState(BitsShopState.ROTATE_TO_BITS_SHOP_1);
                delayClock.schedule(5_000);
                break;
            case ROTATE_TO_BITS_SHOP_1:
                if (GameStateHandler.getInstance().getLocation() != GameStateHandler.Location.HUB) {
                    setBitsShopState(BitsShopState.TELEPORT_TO_HUB);
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    break;
                }
                KeyBindUtils.stopMovement();
                long randomTime = TaunahiConfig.getRandomRotationTime();
                Rotation rot = rotation.getRotation(bitsShopLocation1);
                rotation.easeTo(
                        new RotationConfiguration(new Rotation(rot.getYaw(), rot.getPitch()), randomTime, null
                        ));
                delayClock.schedule(randomTime + 150);
                setBitsShopState(BitsShopState.GO_TO_BITS_SHOP_1);
                break;
            case GO_TO_BITS_SHOP_1:
                if (mc.currentScreen != null) {
                    KeyBindUtils.stopMovement();
                    setBitsShopState(BitsShopState.ROTATE_TO_BITS_SHOP_1);
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    break;
                }
                if (rotation.isRotating()) break;
                if (mc.thePlayer.getPositionVector().distanceTo(bitsShopLocation1) < 1.5) {
                    KeyBindUtils.stopMovement();
                    setBitsShopState(BitsShopState.ROTATE_TO_BITS_SHOP_2);
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    break;
                }
                KeyBindUtils.holdThese(mc.gameSettings.keyBindSprint, mc.gameSettings.keyBindForward);
                stuckClock.schedule(STUCK_DELAY);
                break;
            case ROTATE_TO_BITS_SHOP_2:
                if (GameStateHandler.getInstance().getLocation() != GameStateHandler.Location.HUB) {
                    setBitsShopState(BitsShopState.TELEPORT_TO_HUB);
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    break;
                }
                KeyBindUtils.stopMovement();
                long randomTime2 = TaunahiConfig.getRandomRotationTime();
                Rotation rot2 = rotation.getRotation(bitsShopLocation2);
                rotation.easeTo(
                        new RotationConfiguration(new Rotation(rot2.getYaw(), rot2.getPitch()), randomTime2, null
                        ));
                delayClock.schedule(randomTime2 + 150);
                setBitsShopState(BitsShopState.GO_TO_BITS_SHOP_2);
                break;
            case GO_TO_BITS_SHOP_2:
                if (mc.currentScreen != null) {
                    KeyBindUtils.stopMovement();
                    setBitsShopState(BitsShopState.ROTATE_TO_BITS_SHOP_2);
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    break;
                }
                if (rotation.isRotating()) break;
                if (mc.thePlayer.getPositionVector().distanceTo(bitsShopLocation2) < 1.5) {
                    KeyBindUtils.stopMovement();
                    setBitsShopState(BitsShopState.ROTATE_TO_BITS_SHOP);
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    break;
                }
                KeyBindUtils.holdThese(mc.gameSettings.keyBindSprint, mc.gameSettings.keyBindForward);
                stuckClock.schedule(STUCK_DELAY);
                break;
            case ROTATE_TO_BITS_SHOP:
                if (GameStateHandler.getInstance().getLocation() != GameStateHandler.Location.HUB) {
                    setBitsShopState(BitsShopState.TELEPORT_TO_HUB);
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    break;
                }
                Optional<Entity> elizabeth = mc.theWorld.loadedEntityList.stream().filter(entity -> entity instanceof EntityArmorStand).filter(entity -> entity.getDisplayName().getUnformattedText().contains("Elizabeth")).findFirst();
                if (!elizabeth.isPresent()) {
                    LogUtils.sendError("[Auto God Pot] Could not find Elizabeth! Trying again!");
                    setBitsShopState(BitsShopState.TELEPORT_TO_HUB);
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    return;
                }
                KeyBindUtils.stopMovement();
                long randomTime3 = TaunahiConfig.getRandomRotationTime();
                Rotation rot3 = rotation.getRotation(elizabeth.get());
                rotation.easeTo(
                        new RotationConfiguration(new Rotation(rot3.getYaw(), rot3.getPitch()), randomTime3, null
                        ));
                delayClock.schedule(randomTime3 + 150);
                setBitsShopState(BitsShopState.OPEN_BITS_SHOP);
                break;
            case OPEN_BITS_SHOP:
                if (mc.currentScreen != null) {
                    PlayerUtils.closeScreen();
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    break;
                }
                KeyBindUtils.rightClick();
                setBitsShopState(BitsShopState.BITS_SHOP_TAB);
                delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                break;
            case BITS_SHOP_TAB:
                if (mc.currentScreen == null) {
                    setBitsShopState(BitsShopState.OPEN_BITS_SHOP);
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    break;
                }
                if (!Objects.requireNonNull(InventoryUtils.getInventoryName()).contains("Community Shop")) break;
                Slot bitsShopTab = InventoryUtils.getSlotOfItemInContainer("Bits Shop");
                if (bitsShopTab == null) break;
                ArrayList<String> lore = InventoryUtils.getItemLore(bitsShopTab.getStack());
                for (String line : lore) {
                    if (line.contains("Click to view")) {
                        InventoryUtils.clickContainerSlot(bitsShopTab.slotNumber, InventoryUtils.ClickType.LEFT, InventoryUtils.ClickMode.PICKUP);
                        setBitsShopState(BitsShopState.CHECK_CONFIRM);
                        delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                        return;
                    }
                }
                setBitsShopState(BitsShopState.CHECK_CONFIRM);
                delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                break;
            case CHECK_CONFIRM:
                if (mc.currentScreen == null) {
                    setBitsShopState(BitsShopState.OPEN_BITS_SHOP);
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    break;
                }
                if (!Objects.requireNonNull(InventoryUtils.getInventoryName()).contains("Community Shop")) break;
                Slot confirm = InventoryUtils.getSlotOfItemInContainer("Purchase Confirmation");
                if (confirm == null) break;
                ArrayList<String> lore2 = InventoryUtils.getItemLore(confirm.getStack());
                for (String line : lore2) {
                    if (line.contains("Confirmation: Enabled!")) {
                        InventoryUtils.clickContainerSlot(confirm.slotNumber, InventoryUtils.ClickType.LEFT, InventoryUtils.ClickMode.PICKUP);
                        setBitsShopState(BitsShopState.CLICK_GOD_POT);
                        delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                        return;
                    }
                }
                setBitsShopState(BitsShopState.CLICK_GOD_POT);
                delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                break;
            case CLICK_GOD_POT:
                if (mc.currentScreen == null) {
                    setBitsShopState(BitsShopState.OPEN_BITS_SHOP);
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    break;
                }
                if (!Objects.requireNonNull(InventoryUtils.getInventoryName()).contains("Community Shop")) break;
                Slot godPot = InventoryUtils.getSlotOfItemInContainer("God Potion");
                if (godPot == null) break;
                setBitsShopState(BitsShopState.WAITING_FOR_BUY);
                InventoryUtils.clickContainerSlot(godPot.slotNumber, InventoryUtils.ClickType.LEFT, InventoryUtils.ClickMode.PICKUP);
                break;
            case WAITING_FOR_BUY:
                break;
            case END:
                if (mc.currentScreen != null) {
                    PlayerUtils.closeScreen();
                    delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
                    break;
                }
                setBitsShopState(BitsShopState.NONE);
                setGodPotMode(GodPotMode.FROM_INVENTORY);
                break;
        }
    }

    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) {
        if (!isRunning()) return;
        if (event.type != 0) return;
        String message = StringUtils.stripControlCodes(event.message.getUnformattedText());
        if (ahState != AhState.NONE) {
            if (message.startsWith("You claimed God Potion from")) {
                setAhState(AhState.CLOSE_AH);
                delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
            } else if (message.startsWith("You purchased God Potion")) {
                setAhState(AhState.COLLECT_ITEM_OPEN_AH);
                delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
            } else if (message.contains("NOT_FOUND_OR_ALREADY_CLAIMED")
                    || message.contains("There was an error with the auction house!")) {
                badItems.add(godPotItem.slotNumber);
                setAhState(AhState.OPEN_AH);
                delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
            }
        }
        if (consumePotState == ConsumePotState.WAIT_FOR_CONSUME) {
            if (message.startsWith("GULP! The God Potion grants you powers for")) {
                if (this.hotbarSlot != -1) {
                    setMovePotState(MovePotState.PUT_ITEM_BACK_PICKUP);
                    setConsumePotState(ConsumePotState.MOVE_POT_TO_HOTBAR);
                } else {
                    Multithreading.schedule(this::stop, 1_500, TimeUnit.MILLISECONDS);
                }
                delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
            }
        }
        if (bitsShopState == BitsShopState.WAITING_FOR_BUY) {
            if (message.startsWith("You bought God Potion!")) {
                setBitsShopState(BitsShopState.END);
                delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
            }
        }
    }

    private void setAhState(AhState state) {
        ahState = state;
        LogUtils.sendDebug("[Auto God Pot] Buy state: " + state.name());
        stuckClock.schedule(STUCK_DELAY);
    }

    private void setGodPotMode(GodPotMode mode) {
        godPotMode = mode;
        LogUtils.sendDebug("[Auto God Pot] God Pot mode: " + mode.name());
        stuckClock.schedule(STUCK_DELAY);
    }

    private void setConsumePotState(ConsumePotState state) {
        consumePotState = state;
        LogUtils.sendDebug("[Auto God Pot] Consume Pot state: " + state.name());
        stuckClock.schedule(STUCK_DELAY);
    }

    private void setGoingToAHState(GoingToAHState state) {
        goingToAHState = state;
        LogUtils.sendDebug("[Auto God Pot] Going to AH state: " + state.name());
        stuckClock.schedule(STUCK_DELAY);
    }

    private void setMovePotState(MovePotState state) {
        movePotState = state;
        LogUtils.sendDebug("[Auto God Pot] Move Pot state: " + state.name());
        stuckClock.schedule(STUCK_DELAY);
    }

    private void setBackpackState(BackpackState state) {
        backpackState = state;
        LogUtils.sendDebug("[Auto God Pot] Backpack state: " + state.name());
        stuckClock.schedule(STUCK_DELAY);
    }

    private void setBitsShopState(BitsShopState state) {
        bitsShopState = state;
        LogUtils.sendDebug("[Auto God Pot] Bits Shop state: " + state.name());
        stuckClock.schedule(STUCK_DELAY);
    }

    private void resetAHState() {
        badItems.clear();
        godPotItem = null;
        ahState = AhState.NONE;
    }

    private void resetGoingToAHState() {
        goingToAHState = GoingToAHState.NONE;
    }

    private void resetConsumePotState() {
        consumePotState = ConsumePotState.NONE;
        hotbarSlot = -1;
    }

    private void resetBackpackState() {
        backpackState = BackpackState.NONE;
    }

    private void resetBitsShopState() {
        bitsShopState = BitsShopState.NONE;
    }

    private boolean checkIfWrongInventory(String inventoryNameStartsWith) {
        if (mc.currentScreen == null) {
            setAhState(AhState.OPEN_AH);
            delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
            return true;
        }
        if (InventoryUtils.getInventoryName() == null)
            return true;
        if (!InventoryUtils.getInventoryName().startsWith(inventoryNameStartsWith)) {
            LogUtils.sendError("[Auto God Pot] Opened wrong Auction Menu! Restarting...");
            delayClock.schedule(TaunahiConfig.getRandomGUIMacroDelay());
            return true;
        }
        return false;
    }

    enum AhState {
        NONE,
        OPEN_AH,
        OPEN_BROWSER,
        CLICK_SEARCH,
        SORT_ITEMS,
        CHECK_IF_BIN,
        SELECT_ITEM,
        BUY_ITEM,
        CONFIRM_PURCHASE,
        WAIT_FOR_CONFIRMATION,
        COLLECT_ITEM_OPEN_AH,
        COLLECT_ITEM_VIEW_BIDS,
        COLLECT_ITEM_CLICK_ITEM,
        COLLECT_ITEM_CLICK_COLLECT,
        COLLECT_ITEM_WAIT_FOR_COLLECT,
        CLOSE_AH,
    }

    enum GodPotMode {
        NONE,
        FROM_AH_COOKIE,
        FROM_AH_NO_COOKIE,
        FROM_INVENTORY,
        FROM_BACKPACK,
        FROM_BITS_SHOP
    }

    enum GoingToAHState {
        NONE,
        TELEPORT_TO_HUB,
        ROTATE_TO_AH_1,
        GO_TO_AH_1,
        ROTATE_TO_AH_2,
        GO_TO_AH_2,

    }

    enum ConsumePotState {
        NONE,
        MOVE_POT_TO_HOTBAR,
        SELECT_POT,
        RIGHT_CLICK_POT,
        WAIT_FOR_CONSUME
    }

    enum MovePotState {
        SWAP_POT_TO_HOTBAR_PICKUP,
        SWAP_POT_TO_HOTBAR_PUT,
        SWAP_POT_TO_HOTBAR_PUT_BACK,
        PUT_ITEM_BACK_PICKUP,
        PUT_ITEM_BACK_PUT
    }

    enum BackpackState {
        NONE,
        OPEN_STORAGE,
        MOVE_POT_TO_INVENTORY,
        END
    }

    enum BitsShopState {
        NONE,
        TELEPORT_TO_HUB,
        ROTATE_TO_BITS_SHOP_1,
        GO_TO_BITS_SHOP_1,
        ROTATE_TO_BITS_SHOP_2,
        GO_TO_BITS_SHOP_2,
        ROTATE_TO_BITS_SHOP,
        OPEN_BITS_SHOP,
        BITS_SHOP_TAB,
        CHECK_CONFIRM,
        CLICK_GOD_POT,
        WAITING_FOR_BUY,
        END
    }
}
