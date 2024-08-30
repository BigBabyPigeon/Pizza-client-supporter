package net.taunahi.ezskyblockscripts.config.page;

import cc.polyfrost.oneconfig.config.annotations.Text;

/*
    Credits to Yuro for this superb class
*/
public class CustomFailsafeMessagesPage {
    @Text(
            name = "Custom continue messages",
            description = "The messages to send to the chat when the failsafe has been triggered and you want to ask if you can continue (use '|' to split the messages)",
            placeholder = "Leave empty to use a random message",
            multiline = true
    )
    public static String customContinueMessages = "";
    @Text(
            name = "Rotation failsafe messages",
            description = "The messages to send to the chat when the rotation failsafe has been triggered (use '|' to split the messages)",
            placeholder = "Leave empty to use a random message",
            multiline = true
    )
    public static String customRotationMessages = "";
    @Text(
            name = "Teleportation failsafe messages",
            description = "The messages to send to the chat when the teleportation failsafe has been triggered (use '|' to split the messages)",
            placeholder = "Leave empty to use a random message",
            multiline = true
    )
    public static String customTeleportationMessages = "";

    @Text(
            name = "Bedrock failsafe messages",
            description = "The messages to send to the chat when the bedrock failsafe has been triggered (use '|' to split the messages)",
            placeholder = "Leave empty to use a random message",
            multiline = true
    )
    public static String customBedrockMessages = "";

    @Text(
            name = "Dirt failsafe messages",
            description = "The messages to send to the chat when the dirt failsafe has been triggered (use '|' to split the messages)",
            placeholder = "Leave empty to use a random message",
            multiline = true
    )
    public static String customDirtMessages = "";
}
