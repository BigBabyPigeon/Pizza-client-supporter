package net.taunahi.ezskyblockscripts.command;

import net.taunahi.ezskyblockscripts.config.TaunahiConfig;
import net.taunahi.ezskyblockscripts.util.LogUtils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RewarpCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "fhrewarp";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/fhrewarp [add|remove|removeall]";
    }

    @Override
    public List<String> getCommandAliases() {
        return new ArrayList<>();
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length != 1) {
            LogUtils.sendError("Invalid arguments. Use " + getCommandUsage(sender));
            return;
        }

        switch (args[0]) {
            case "add": {
                TaunahiConfig.addRewarp();
                break;
            }
            case "remove": {
                TaunahiConfig.removeRewarp();
                break;
            }
            case "removeall": {
                TaunahiConfig.removeAllRewarps();
                break;
            }
            default: {
                LogUtils.sendError("Invalid argument. Use " + getCommandUsage(sender));
                break;
            }
        }
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @Override
    public int compareTo(@NotNull ICommand o) {
        return 0;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return -1;
    }
}
