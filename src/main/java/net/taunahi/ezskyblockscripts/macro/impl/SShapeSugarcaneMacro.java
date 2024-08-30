package net.taunahi.ezskyblockscripts.macro.impl;

import net.taunahi.ezskyblockscripts.config.TaunahiConfig;
import net.taunahi.ezskyblockscripts.handler.GameStateHandler;
import net.taunahi.ezskyblockscripts.handler.MacroHandler;
import net.taunahi.ezskyblockscripts.macro.AbstractMacro;
import net.taunahi.ezskyblockscripts.util.AngleUtils;
import net.taunahi.ezskyblockscripts.util.BlockUtils;
import net.taunahi.ezskyblockscripts.util.KeyBindUtils;
import net.taunahi.ezskyblockscripts.util.LogUtils;
import net.taunahi.ezskyblockscripts.util.helper.Rotation;
import net.taunahi.ezskyblockscripts.util.helper.RotationConfiguration;

import java.util.Optional;

public class SShapeSugarcaneMacro extends AbstractMacro {

    public double rowStartX = 0;
    public double rowStartZ = 0;

    @Override
    public void updateState() {
        if (currentState == null)
            changeState(State.NONE);
        switch (currentState) {
            case S: {
                if (hasWall(0, -1, getYaw() - 45f) &&
                        hasWall(0, -1, getYaw() + 45f)) {

                    if (getNearestSideWall(getYaw() + 45, -1) == -999) {
                        changeState(State.A);
                    }
                    if (getNearestSideWall(getYaw() - 45, 1) == -999) {
                        changeState(State.D);
                    }
                }
                break;
            }
            case A:
            case D: {
                changeState(State.S);
                break;
            }
            case DROPPING: {
                LogUtils.sendDebug("On Ground: " + mc.thePlayer.onGround);
                if (mc.thePlayer.onGround && Math.abs(getLayerY() - mc.thePlayer.getPosition().getY()) > 1.5) {
                    if (TaunahiConfig.rotateAfterDrop && !getRotation().isRotating()) {
                        LogUtils.sendDebug("Rotating 180");
                        getRotation().reset();
                        setYaw(AngleUtils.getClosestDiagonal(getYaw() + 180));
                        setClosest90Deg(Optional.of(AngleUtils.getClosest(getYaw())));
                        getRotation().easeTo(
                                new RotationConfiguration(
                                        new Rotation(getYaw(), getPitch()),
                                        (long) (400 + Math.random() * 300), null
                                ).easeOutBack(true)
                        );
                    }
                    KeyBindUtils.stopMovement();
                    changeState(State.NONE);
                    setLayerY(mc.thePlayer.getPosition().getY());
                } else {
                    GameStateHandler.getInstance().scheduleNotMoving();
                }
                break;
            }
            case NONE: {
                changeState(calculateDirection());
                break;
            }
        }
    }

    @Override
    public void invokeState() {
        if (currentState == null) return;
        switch (currentState) {
            case NONE:
                break;
            case A:
                KeyBindUtils.holdThese(
                        mc.gameSettings.keyBindLeft,
                        mc.gameSettings.keyBindAttack
                );
                break;
            case D:
                KeyBindUtils.holdThese(
                        mc.gameSettings.keyBindRight,
                        mc.gameSettings.keyBindAttack
                );
                break;
            case S:
                KeyBindUtils.holdThese(
                        mc.gameSettings.keyBindBack,
                        mc.gameSettings.keyBindAttack
                );
                break;
            case DROPPING:
                if (mc.thePlayer.onGround && Math.abs(getLayerY() - mc.thePlayer.getPosition().getY()) <= 1.5) {
                    LogUtils.sendDebug("Dropping done, but didn't drop high enough to rotate!");
                    setLayerY(mc.thePlayer.getPosition().getY());
                    changeState(State.NONE);
                }
                break;
        }
    }

    @Override
    public void actionAfterTeleport() {
        setLayerY(mc.thePlayer.getPosition().getY());
        rowStartX = mc.thePlayer.posX;
        rowStartZ = mc.thePlayer.posZ;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (!TaunahiConfig.customPitch && !isRestoredState()) {
            setPitch((float) (Math.random() * 1) - 0.5f); // -0.5 to 0.5
        }
        if (!TaunahiConfig.customYaw && !isRestoredState()) {
            setYaw(AngleUtils.getClosestDiagonal());
            setClosest90Deg(Optional.of(AngleUtils.getClosest(getYaw())));
        }
        rowStartX = mc.thePlayer.posX;
        rowStartZ = mc.thePlayer.posZ;
        if (MacroHandler.getInstance().isTeleporting()) return;
        setRestoredState(false);
        getRotation().easeTo(
                new RotationConfiguration(
                        new Rotation(getYaw(), getPitch()),
                        TaunahiConfig.getRandomRotationTime(), null
                ).easeOutBack(true)
        );
    }

    @Override
    public State calculateDirection() {
        if (BlockUtils.isWater(BlockUtils.getRelativeBlock(2, -1, 1, getYaw() - 45f)) || BlockUtils.isWater(BlockUtils.getRelativeBlock(2, 0, 1, getYaw() - 45f))
                || BlockUtils.isWater(BlockUtils.getRelativeBlock(-1, -1, 1, getYaw() - 45f)) || BlockUtils.isWater(BlockUtils.getRelativeBlock(-1, 0, 1, getYaw() - 45f)))
            if (!(hasWall(0, 1, getYaw() - 45f) && hasWall(-1, 0, getYaw() - 45f)))
                return State.A;
            else if (BlockUtils.isWater(BlockUtils.getRelativeBlock(2, -1, 1, getYaw() + 45f)) || BlockUtils.isWater(BlockUtils.getRelativeBlock(2, 0, 1, getYaw() + 45f))
                    || BlockUtils.isWater(BlockUtils.getRelativeBlock(-1, -1, 1, getYaw() + 45f)) || BlockUtils.isWater(BlockUtils.getRelativeBlock(-1, 0, 1, getYaw() + 45f)))
                if (!(hasWall(0, 1, getYaw() + 45f) && hasWall(11, 0, getYaw() + 45f)))
                    return State.D;
        return State.S;
    }

    boolean hasWall(int rightOffset, int frontOffset, float yaw) {
        return !BlockUtils.canWalkThrough(BlockUtils.getRelativeBlockPos(rightOffset, 0, frontOffset, yaw));
    }

    int getNearestSideWall(float yaw, int dir) { // right = 1; left = -1
        for (int i = 0; i < 8; i++) {
            if (hasWall(i * dir, 0, yaw)) return i;
        }
        return -999;
    }
}
