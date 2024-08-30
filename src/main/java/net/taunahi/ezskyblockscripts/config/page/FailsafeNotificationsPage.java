package net.taunahi.ezskyblockscripts.config.page;

import cc.polyfrost.oneconfig.config.annotations.Switch;

public class FailsafeNotificationsPage {
    @Switch(
            name = "Rotation Check Notifications",
            description = "Whether or not to send a notification when the rotation check failsafe is triggered.",
            category = "Failsafe Notifications"
    )
    public static boolean notifyOnRotationFailsafe = true;

    @Switch(
            name = "Teleportation Check Notifications",
            description = "Whether or not to send a notification when the teleportation check failsafe is triggered.",
            category = "Failsafe Notifications"
    )
    public static boolean notifyOnTeleportationFailsafe = true;

    @Switch(
            name = "Dirt Check Notifications",
            description = "Whether or not to send a notification when the dirt check failsafe is triggered.",
            category = "Failsafe Notifications"
    )
    public static boolean notifyOnDirtFailsafe = true;

    @Switch(
            name = "Item Change Check Notifications",
            description = "Whether or not to send a notification when the item change check failsafe is triggered.",
            category = "Failsafe Notifications"
    )
    public static boolean notifyOnItemChangeFailsafe = true;

    @Switch(
            name = "World Change Check Notifications",
            description = "Whether or not to send a notification when the world change check failsafe is triggered.",
            category = "Failsafe Notifications"
    )
    public static boolean notifyOnWorldChangeFailsafe = true;

    @Switch(
            name = "Bedrock Cage Check Notifications",
            description = "Whether or not to send a notification when the bedrock cage check failsafe is triggered.",
            category = "Failsafe Notifications"
    )
    public static boolean notifyOnBedrockCageFailsafe = true;

    @Switch(
            name = "Evacuate Notifications",
            description = "Whether or not to send a notification when the evacuate failsafe is triggered.",
            category = "Failsafe Notifications"
    )
    public static boolean notifyOnEvacuateFailsafe = false;

    @Switch(
            name = "Banwave Notifications",
            description = "Whether or not to send a notification when the banwave failsafe is triggered.",
            category = "Failsafe Notifications"
    )
    public static boolean notifyOnBanwaveFailsafe = false;

    @Switch(
            name = "Disconnect Notifications",
            description = "Whether or not to send a notification when the disconnect failsafe is triggered.",
            category = "Failsafe Notifications"
    )
    public static boolean notifyOnDisconnectFailsafe = true;

    @Switch(
            name = "Jacob Notifications",
            description = "Whether or not to send a notification when the Jacob failsafe is triggered.",
            category = "Failsafe Notifications"
    )
    public static boolean notifyOnJacobFailsafe = false;

    @Switch(
            name = "Test Notifications",
            description = "Whether or not to send a notification when the Test failsafe is triggered.",
            category = "Failsafe Notifications"
    )
    public static boolean notifyOnTestFailsafe = true;

    @Switch(
            name = "Lower Average BPS Notifications",
            description = "Whether or not to send a notification when the average BPS is lower than the specified value.",
            category = "Failsafe Notifications"
    )
    public static boolean notifyOnLowerAverageBPS = false;

    @Switch(
            name = "Guest Visit Notifications",
            description = "Whether or not to send a notification when a guest visits your island.",
            category = "Failsafe Notifications"
    )
    public static boolean notifyOnGuestVisit = true;

    @Switch(
            name = "Rotation Check Sound Alert",
            description = "Whether or not to play a sound when the rotation check failsafe is triggered.",
            category = "Failsafe Sound Alerts"
    )
    public static boolean alertOnRotationFailsafe = true;

    @Switch(
            name = "Teleportation Check Sound Alert",
            description = "Whether or not to play a sound when the teleportation check failsafe is triggered.",
            category = "Failsafe Sound Alerts"
    )
    public static boolean alertOnTeleportationFailsafe = true;

    @Switch(
            name = "Dirt Check Sound Alert",
            description = "Whether or not to play a sound when the dirt check failsafe is triggered.",
            category = "Failsafe Sound Alerts"
    )
    public static boolean alertOnDirtFailsafe = true;

    @Switch(
            name = "Item Change Check Sound Alert",
            description = "Whether or not to play a sound when the item change check failsafe is triggered.",
            category = "Failsafe Sound Alerts"
    )
    public static boolean alertOnItemChangeFailsafe = true;

    @Switch(
            name = "World Change Check Sound Alert",
            description = "Whether or not to play a sound when the world change check failsafe is triggered.",
            category = "Failsafe Sound Alerts"
    )
    public static boolean alertOnWorldChangeFailsafe = false;

    @Switch(
            name = "Bedrock Cage Check Sound Alert",
            description = "Whether or not to play a sound when the bedrock cage check failsafe is triggered.",
            category = "Failsafe Sound Alerts"
    )
    public static boolean alertOnBedrockCageFailsafe = true;

    @Switch(
            name = "Evacuate Alert",
            description = "Whether or not to play a sound when the evacuate failsafe is triggered.",
            category = "Failsafe Sound Alerts"
    )
    public static boolean alertOnEvacuateFailsafe = false;

    @Switch(
            name = "Banwave Alert",
            description = "Whether or not to play a sound when the banwave failsafe is triggered.",
            category = "Failsafe Sound Alerts"
    )
    public static boolean alertOnBanwaveFailsafe = false;

    @Switch(
            name = "Disconnect Alert",
            description = "Whether or not to play a sound when the disconnect failsafe is triggered.",
            category = "Failsafe Sound Alerts"
    )
    public static boolean alertOnDisconnectFailsafe = false;

    @Switch(
            name = "Jacob Alert",
            description = "Whether or not to play a sound when the Jacob failsafe is triggered.",
            category = "Failsafe Sound Alerts"
    )
    public static boolean alertOnJacobFailsafe = false;

    @Switch(
            name = "Test Alert",
            description = "Whether or not to play a sound when the Test failsafe is triggered.",
            category = "Failsafe Sound Alerts"
    )
    public static boolean alertOnTestFailsafe = true;

    @Switch(
            name = "Lower Average BPS Alert",
            description = "Whether or not to play a sound when the average BPS is lower than the specified value.",
            category = "Failsafe Sound Alerts"
    )
    public static boolean alertOnLowerAverageBPS = false;

    @Switch(
            name = "Guest Visit Alert",
            description = "Whether or not to play a sound when a guest visits your island.",
            category = "Failsafe Sound Alerts"
    )
    public static boolean alertOnGuestVisit = false;


    @Switch(
            name = "Rotation Check Tag Everyone",
            description = "Whether or not to tag everyone in the webhook message when the rotation check failsafe is triggered.",
            category = "Failsafe Tag Everyone"
    )
    public static boolean tagEveryoneOnRotationFailsafe = true;

    @Switch(
            name = "Teleportation Check Tag Everyone",
            description = "Whether or not to tag everyone in the webhook message when the teleportation check failsafe is triggered.",
            category = "Failsafe Tag Everyone"
    )
    public static boolean tagEveryoneOnTeleportationFailsafe = true;

    @Switch(
            name = "Dirt Check Tag Everyone",
            description = "Whether or not to tag everyone in the webhook message when the dirt check failsafe is triggered.",
            category = "Failsafe Tag Everyone"
    )
    public static boolean tagEveryoneOnDirtFailsafe = true;

    @Switch(
            name = "Item Change Check Tag Everyone",
            description = "Whether or not to tag everyone in the webhook message when the item change check failsafe is triggered.",
            category = "Failsafe Tag Everyone"
    )
    public static boolean tagEveryoneOnItemChangeFailsafe = true;

    @Switch(
            name = "World Change Check Tag Everyone",
            description = "Whether or not to tag everyone in the webhook message when the world change check failsafe is triggered.",
            category = "Failsafe Tag Everyone"
    )
    public static boolean tagEveryoneOnWorldChangeFailsafe = false;

    @Switch(
            name = "Bedrock Cage Check Tag Everyone",
            description = "Whether or not to tag everyone in the webhook message when the bedrock cage check failsafe is triggered.",
            category = "Failsafe Tag Everyone"
    )
    public static boolean tagEveryoneOnBedrockCageFailsafe = true;

    @Switch(
            name = "Evacuate Tag Everyone",
            description = "Whether or not to tag everyone in the webhook message when the evacuate failsafe is triggered.",
            category = "Failsafe Tag Everyone"
    )
    public static boolean tagEveryoneOnEvacuateFailsafe = false;

    @Switch(
            name = "Banwave Tag Everyone",
            description = "Whether or not to tag everyone in the webhook message when the banwave failsafe is triggered.",
            category = "Failsafe Tag Everyone"
    )
    public static boolean tagEveryoneOnBanwaveFailsafe = false;

    @Switch(
            name = "Disconnect Tag Everyone",
            description = "Whether or not to tag everyone in the webhook message when the disconnect failsafe is triggered.",
            category = "Failsafe Tag Everyone"
    )
    public static boolean tagEveryoneOnDisconnectFailsafe = false;

    @Switch(
            name = "Jacob Tag Everyone",
            description = "Whether or not to tag everyone in the webhook message when the Jacob failsafe is triggered.",
            category = "Failsafe Tag Everyone"
    )
    public static boolean tagEveryoneOnJacobFailsafe = false;

    @Switch(
            name = "Test Tag Everyone",
            description = "Whether or not to tag everyone in the webhook message when the Test failsafe is triggered.",
            category = "Failsafe Tag Everyone"
    )
    public static boolean tagEveryoneOnTestFailsafe = true;

    @Switch(
            name = "Lower Average BPS Tag Everyone",
            description = "Whether or not to tag everyone in the webhook message when the average BPS is lower than the specified value.",
            category = "Failsafe Tag Everyone"
    )
    public static boolean tagEveryoneOnLowerAverageBPS = false;

    @Switch(
            name = "Guest Visit Tag Everyone",
            description = "Whether or not to tag everyone in the webhook message when a guest visits your island.",
            category = "Failsafe Tag Everyone"
    )
    public static boolean tagEveryoneOnGuestVisit = false;

    @Switch(
            name = "Rotation Check Auto Alt-tab",
            description = "Whether or not to automatically alt-tab when the rotation check failsafe is triggered.",
            category = "Failsafe Auto Alt-tab"
    )
    public static boolean autoAltTabOnRotationFailsafe = true;

    @Switch(
            name = "Teleportation Check Auto Alt-tab",
            description = "Whether or not to automatically alt-tab when the teleportation check failsafe is triggered.",
            category = "Failsafe Auto Alt-tab"
    )
    public static boolean autoAltTabOnTeleportationFailsafe = true;

    @Switch(
            name = "Dirt Check Auto Alt-tab",
            description = "Whether or not to automatically alt-tab when the dirt check failsafe is triggered.",
            category = "Failsafe Auto Alt-tab"
    )
    public static boolean autoAltTabOnDirtFailsafe = true;

    @Switch(
            name = "Item Change Check Auto Alt-tab",
            description = "Whether or not to automatically alt-tab when the item change check failsafe is triggered.",
            category = "Failsafe Auto Alt-tab"
    )
    public static boolean autoAltTabOnItemChangeFailsafe = true;

    @Switch(
            name = "World Change Check Auto Alt-tab",
            description = "Whether or not to automatically alt-tab when the world change check failsafe is triggered.",
            category = "Failsafe Auto Alt-tab"
    )
    public static boolean autoAltTabOnWorldChangeFailsafe = false;

    @Switch(
            name = "Bedrock Cage Check Auto Alt-tab",
            description = "Whether or not to automatically alt-tab when the bedrock cage check failsafe is triggered.",
            category = "Failsafe Auto Alt-tab"
    )
    public static boolean autoAltTabOnBedrockCageFailsafe = true;

    @Switch(
            name = "Evacuate Auto Alt-tab",
            description = "Whether or not to automatically alt-tab when the evacuate failsafe is triggered.",
            category = "Failsafe Auto Alt-tab"
    )
    public static boolean autoAltTabOnEvacuateFailsafe = false;

    @Switch(
            name = "Banwave Auto Alt-tab",
            description = "Whether or not to automatically alt-tab when the banwave failsafe is triggered.",
            category = "Failsafe Auto Alt-tab"
    )
    public static boolean autoAltTabOnBanwaveFailsafe = false;

    @Switch(
            name = "Disconnect Auto Alt-tab",
            description = "Whether or not to automatically alt-tab when the disconnect failsafe is triggered.",
            category = "Failsafe Auto Alt-tab"
    )
    public static boolean autoAltTabOnDisconnectFailsafe = false;

    @Switch(
            name = "Jacob Auto Alt-tab",
            description = "Whether or not to automatically alt-tab when the Jacob failsafe is triggered.",
            category = "Failsafe Auto Alt-tab"
    )
    public static boolean autoAltTabOnJacobFailsafe = false;

    @Switch(
            name = "Test Auto Alt-tab",
            description = "Whether or not to automatically alt-tab when the Test failsafe is triggered.",
            category = "Failsafe Auto Alt-tab"
    )
    public static boolean autoAltTabOnTestFailsafe = true;

    @Switch(
            name = "Lower Average BPS Alt-tab",
            description = "Whether or not to automatically alt-tab when the average BPS is lower than the specified value.",
            category = "Failsafe Auto Alt-tab"
    )
    public static boolean autoAltTabOnLowerAverageBPS = false;
    @Switch(
            name = "Guest Visit Alt-tab",
            description = "Whether or not to automatically alt-tab when a guest visits your island.",
            category = "Failsafe Auto Alt-tab"
    )
    public static boolean autoAltTabOnGuestVisit = false;
}
