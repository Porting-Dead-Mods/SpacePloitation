package com.portingdeadmods.spaceploitation.networking;

import com.portingdeadmods.spaceploitation.Spaceploitation;
import com.portingdeadmods.spaceploitation.data.PlanetComponent;
import com.portingdeadmods.spaceploitation.registries.MJDataComponents;
import com.portingdeadmods.spaceploitation.registries.MJItems;
import com.portingdeadmods.spaceploitation.registries.MJPlanetCards;
import com.portingdeadmods.portingdeadlibs.PortingDeadLibsClient;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.Optional;

public final class MJNetworking {

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Spaceploitation.MODID);
        
        registrar.playToClient(
                SyncPlanetCardsPayload.TYPE,
                SyncPlanetCardsPayload.STREAM_CODEC,
                MJNetworking::handleSyncPlanetCards
        );
        registrar.playToServer(
                UpgradeWidgetOpenClosePayload.TYPE,
                UpgradeWidgetOpenClosePayload.STREAM_CODEC,
                UpgradeWidgetOpenClosePayload::handle
        );
        registrar.playToServer(
                UpgradeWidgetSetSlotPositionsPayload.TYPE,
                UpgradeWidgetSetSlotPositionsPayload.STREAM_CODEC,
                UpgradeWidgetSetSlotPositionsPayload::handle
        );
        registrar.playToServer(
                PlanetCardWidgetSetSlotPositionPayload.TYPE,
                PlanetCardWidgetSetSlotPositionPayload.STREAM_CODEC,
                PlanetCardWidgetSetSlotPositionPayload::handle
        );
    }

    private static void handleSyncPlanetCards(SyncPlanetCardsPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            MJPlanetCards.clearStacks();
            
            payload.planetTypes().forEach(planetType -> {
                Item itemToUse = planetType.tint().isPresent() ? MJItems.TINTED_PLANET_CARD.get() : MJItems.PLANET_CARD.get();
                ItemStack stack = new ItemStack(itemToUse);
                stack.set(MJDataComponents.PLANET, new PlanetComponent(Optional.of(planetType), false, planetType.isBlackHole()));
                MJPlanetCards.addStack(stack);
            });
        });
    }

    public static void sendToServer(CustomPacketPayload packet) {
        PacketDistributor.sendToServer(packet);
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload packet) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    public static void sendToAllPlayers(CustomPacketPayload packet) {
        PacketDistributor.sendToAllPlayers(packet);
    }
}
