package com.industrialcivilization.core;

import java.util.Collections;
import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public final class CommandIndustrialStatus extends CommandBase {
    @Override public String getName() { return "ic_status"; }
    @Override public String getUsage(ICommandSender sender) { return "/ic_status"; }
    @Override public int getRequiredPermissionLevel() { return 0; }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayer player = getCommandSenderAsPlayer(sender);
        player.sendMessage(new TextComponentString("Industrial Civilization telemetry"));
        player.sendMessage(new TextComponentString("Play time: "
            + ProgressionState.counter(player, "active_ticks") / 72000.0 + "h; manual crafts: "
            + ProgressionState.counter(player, "manual_crafts") + "; blocks mined: "
            + ProgressionState.counter(player, "blocks_mined_manual")));
        player.sendMessage(new TextComponentString("Dimension transfers: "
            + ProgressionState.counter(player, "dimension_transfers") + "; machine artifacts synchronized: "
            + ProgressionState.counter(player, "artifacts_recorded")));
        player.sendMessage(new TextComponentString("Moon archive: "
            + ProgressionState.has(player, "orbital_research_archive") + "; Mars authorization: "
            + ProgressionState.has(player, "mars_mission_authorization") + "; AI Age: "
            + ProgressionState.has(player, "ai_age")));
    }
}
