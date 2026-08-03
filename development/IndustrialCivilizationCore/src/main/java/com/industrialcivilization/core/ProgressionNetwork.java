package com.industrialcivilization.core;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Client notifications for major server-authoritative progression transitions. */
public final class ProgressionNetwork {
    private static final SimpleNetworkWrapper CHANNEL =
        NetworkRegistry.INSTANCE.newSimpleChannel("ic_progress");

    public static void init() {
        CHANNEL.registerMessage(CreditsHandler.class, ShowCredits.class, 0, Side.CLIENT);
        CHANNEL.registerMessage(SpaceAccessRequestHandler.class, SpaceAccessRequest.class, 1, Side.SERVER);
        CHANNEL.registerMessage(SpaceAccessHandler.class, SpaceAccess.class, 2, Side.CLIENT);
    }

    public static void showCredits(EntityPlayerMP player) {
        CHANNEL.sendTo(new ShowCredits(), player);
    }

    @SideOnly(Side.CLIENT)
    public static void requestSpaceAccess() {
        CHANNEL.sendToServer(new SpaceAccessRequest());
    }

    public static final class ShowCredits implements IMessage {
        @Override public void fromBytes(ByteBuf buffer) {}
        @Override public void toBytes(ByteBuf buffer) {}
    }

    public static final class SpaceAccessRequest implements IMessage {
        @Override public void fromBytes(ByteBuf buffer) {}
        @Override public void toBytes(ByteBuf buffer) {}
    }

    public static final class SpaceAccessRequestHandler
            implements IMessageHandler<SpaceAccessRequest, IMessage> {
        @Override
        public IMessage onMessage(SpaceAccessRequest message, MessageContext context) {
            EntityPlayerMP player = context.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                boolean moon = ProgressionState.has(player, "orbital_research_archive");
                boolean mars = ProgressionState.has(player, "lunar_quantum_component")
                    && ProgressionState.has(player, "mars_mission_authorization");
                CHANNEL.sendTo(new SpaceAccess(moon, mars), player);
            });
            return null;
        }
    }

    public static final class SpaceAccess implements IMessage {
        boolean moon;
        boolean mars;

        public SpaceAccess() {}
        SpaceAccess(boolean moon, boolean mars) {
            this.moon = moon;
            this.mars = mars;
        }

        @Override public void fromBytes(ByteBuf buffer) {
            moon = buffer.readBoolean();
            mars = buffer.readBoolean();
        }
        @Override public void toBytes(ByteBuf buffer) {
            buffer.writeBoolean(moon);
            buffer.writeBoolean(mars);
        }
    }

    @SideOnly(Side.CLIENT)
    public static final class CreditsHandler implements IMessageHandler<ShowCredits, IMessage> {
        @Override
        public IMessage onMessage(ShowCredits message, MessageContext context) {
            Minecraft.getMinecraft().addScheduledTask(() ->
                Minecraft.getMinecraft().displayGuiScreen(new GuiIndustrialCredits()));
            return null;
        }
    }

    @SideOnly(Side.CLIENT)
    public static final class SpaceAccessHandler implements IMessageHandler<SpaceAccess, IMessage> {
        @Override
        public IMessage onMessage(SpaceAccess message, MessageContext context) {
            Minecraft.getMinecraft().addScheduledTask(() ->
                IndustrialCivilizationCore.ClientRegistration.applySpaceAccess(
                    message.moon, message.mars));
            return null;
        }
    }

    private ProgressionNetwork() {}
}
