package com.industrialcivilization.core;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Minimal server-authoritative synchronization for the pause-menu directory. */
public final class FactionNetwork {
    private static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("ic_factions");
    public static String clientMembership = "";
    public static final int[] clientReputation = new int[FactionSystem.DEFINITIONS.length];
    public static final boolean[] clientKnown = new boolean[FactionSystem.DEFINITIONS.length];
    public static final boolean[] clientEligible = new boolean[FactionSystem.DEFINITIONS.length];

    public static void init() {
        CHANNEL.registerMessage(RequestHandler.class, Request.class, 0, Side.SERVER);
        CHANNEL.registerMessage(SnapshotHandler.class, Snapshot.class, 1, Side.CLIENT);
    }

    @SideOnly(Side.CLIENT)
    public static void requestSnapshot() {
        CHANNEL.sendToServer(new Request());
    }

    public static void sendSnapshot(EntityPlayerMP player) {
        Snapshot snapshot = new Snapshot();
        snapshot.membership = FactionSystem.membership(player);
        snapshot.reputation = new int[FactionSystem.DEFINITIONS.length];
        snapshot.known = new boolean[FactionSystem.DEFINITIONS.length];
        snapshot.eligible = new boolean[FactionSystem.DEFINITIONS.length];
        for (int index = 0; index < FactionSystem.DEFINITIONS.length; index++) {
            String faction = FactionSystem.DEFINITIONS[index].id;
            snapshot.reputation[index] = FactionSystem.reputation(player, faction);
            snapshot.known[index] = FactionSystem.known(player, faction);
            snapshot.eligible[index] = FactionSystem.eligible(player, faction);
        }
        CHANNEL.sendTo(snapshot, player);
    }

    public static final class Request implements IMessage {
        @Override public void fromBytes(ByteBuf buffer) {}
        @Override public void toBytes(ByteBuf buffer) {}
    }

    public static final class RequestHandler implements IMessageHandler<Request, IMessage> {
        @Override
        public IMessage onMessage(Request message, MessageContext context) {
            EntityPlayerMP player = context.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> sendSnapshot(player));
            return null;
        }
    }

    public static final class Snapshot implements IMessage {
        String membership = "";
        int[] reputation = new int[0];
        boolean[] known = new boolean[0];
        boolean[] eligible = new boolean[0];

        @Override
        public void fromBytes(ByteBuf buffer) {
            membership = ByteBufUtils.readUTF8String(buffer);
            int count = buffer.readUnsignedByte();
            reputation = new int[count];
            known = new boolean[count];
            eligible = new boolean[count];
            for (int index = 0; index < count; index++) {
                reputation[index] = buffer.readInt();
                known[index] = buffer.readBoolean();
                eligible[index] = buffer.readBoolean();
            }
        }

        @Override
        public void toBytes(ByteBuf buffer) {
            ByteBufUtils.writeUTF8String(buffer, membership == null ? "" : membership);
            buffer.writeByte(reputation.length);
            for (int index = 0; index < reputation.length; index++) {
                buffer.writeInt(reputation[index]);
                buffer.writeBoolean(known[index]);
                buffer.writeBoolean(eligible[index]);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static final class SnapshotHandler implements IMessageHandler<Snapshot, IMessage> {
        @Override
        public IMessage onMessage(Snapshot message, MessageContext context) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                clientMembership = message.membership;
                int count = Math.min(clientReputation.length, message.reputation.length);
                for (int index = 0; index < count; index++) {
                    clientReputation[index] = message.reputation[index];
                    clientKnown[index] = message.known[index];
                    clientEligible[index] = message.eligible[index];
                }
            });
            return null;
        }
    }

    private FactionNetwork() {}
}
