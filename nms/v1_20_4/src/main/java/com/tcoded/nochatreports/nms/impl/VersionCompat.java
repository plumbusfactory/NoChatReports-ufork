package com.tcoded.nochatreports.nms.impl;

import com.tcoded.nochatreports.nms.NmsProvider;
import com.tcoded.nochatreports.nms.channel.GlobalPacketHandler;
import com.tcoded.nochatreports.nms.impl.listener.ClientboundServerDataPacketListener;
import com.tcoded.nochatreports.nms.impl.listener.ClientboundServerStatusListener;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Properties;

/**
 * Minecraft 1.20.4 specifics for the shared NMS implementation.
 *
 *   - Versioned CraftBukkit package
 *   - Chat type must be resolved through the registry
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
        return new ClientboundPlayerChatPacket(new FriendlyByteBuf(byteBuf));
    }

    public static Component unsignedContent(ClientboundPlayerChatPacket packet) {
        return packet.unsignedContent();
    }

    public static Component decorate(ClientboundPlayerChatPacket packet, Component content) {
        return packet.chatType().resolve(MinecraftServer.getServer().registryAccess()).orElseThrow().decorate(content);
    }

    public static void registerListeners(NmsProvider<?> nms, GlobalPacketHandler handler) {
        handler.addListener(new ClientboundServerDataPacketListener(nms));
        handler.addListener(new ClientboundServerStatusListener(nms));
    }

}
