package com.tcoded.nochatreports.nms.impl;

import com.tcoded.nochatreports.nms.NmsProvider;
import com.tcoded.nochatreports.nms.channel.GlobalPacketHandler;
import com.tcoded.nochatreports.nms.impl.listener.ClientboundLoginListener;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Properties;

/**
 * Minecraft 26.1.2 specifics for the shared NMS implementation.
 */
public final class VersionCompat {

    public static final Class<?> CONNECTION_OWNER = ServerCommonPacketListenerImpl.class;

    private VersionCompat() {
    }

    public static DedicatedServerProperties reloadProperties(NmsProvider<?> nms, DedicatedServer server, DedicatedServerProperties config, Properties newProps) {
        if (nms.getConfig().isPaper()) {
            return config.reload(server.registryAccess(), newProps, server.options);
        }

        try {
            return NmsProviderImpl.reloadPropertiesReflectively(server, config, newProps);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ServerPlayer getNmsPlayer(NmsProvider<?> nms, Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        if (nms.getConfig().isPaper()) return (ServerPlayer) craftPlayer.getHandleRaw();
        else return craftPlayer.getHandle();
    }

    public static void send(ServerPlayer player, Packet<?> packet) {
        player.connection.send(packet);
    }

    public static ClientboundPlayerChatPacket decodePlayerChat(ByteBuf byteBuf) {
        return ClientboundPlayerChatPacket.STREAM_CODEC.decode(new RegistryFriendlyByteBuf(byteBuf, MinecraftServer.getServer().registryAccess()));
    }

    public static Component unsignedContent(ClientboundPlayerChatPacket packet) {
        return packet.unsignedContent();
    }

    public static Component decorate(ClientboundPlayerChatPacket packet, Component content) {
        return packet.chatType().decorate(content);
    }

    public static void registerListeners(NmsProvider<?> nms, GlobalPacketHandler handler) {
        handler.addListener(new ClientboundLoginListener(nms));
    }

    public static ClientboundLoginPacket withEnforcesSecureChat(ClientboundLoginPacket packet, boolean enforcesSecureChat) {
        return new ClientboundLoginPacket(
                packet.playerId(),
                packet.hardcore(),
                packet.levels(),
                packet.maxPlayers(),
                packet.chunkRadius(),
                packet.simulationDistance(),
                packet.reducedDebugInfo(),
                packet.showDeathScreen(),
                packet.doLimitedCrafting(),
                packet.commonPlayerSpawnInfo(),
                enforcesSecureChat
        );
    }

}
