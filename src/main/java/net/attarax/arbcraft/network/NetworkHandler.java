package net.attarax.arbcraft.network;

import net.attarax.arbcraft.blockentity.ExperienceConverterBlockEntity;
import net.attarax.arbcraft.menu.ExperienceConverterMenu;
import net.attarax.arbcraft.registry.ModRegistries;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkHandler {

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(ModRegistries.MOD_ID);

        registrar.playToServer(
                WithdrawExpPayload.TYPE,
                WithdrawExpPayload.STREAM_CODEC,
                NetworkHandler::handleWithdrawExp
        );

        registrar.playToServer(
                WithdrawAllExpPayload.TYPE,
                WithdrawAllExpPayload.STREAM_CODEC,
                NetworkHandler::handleWithdrawAllExp
        );

        registrar.playToServer(
                WithdrawExperiencePacket.TYPE,
                WithdrawExperiencePacket.STREAM_CODEC,
                WithdrawExperiencePacket::handle
        );
    }

    private static void handleWithdrawExp(WithdrawExpPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (player.containerMenu instanceof ExperienceConverterMenu menu) {
                    menu.getBlockEntity().withdrawExperience(player, payload.amount);
                }
            }
        });
    }

    private static void handleWithdrawAllExp(WithdrawAllExpPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (player.containerMenu instanceof ExperienceConverterMenu menu) {
                    menu.getBlockEntity().withdrawAllExperience(player);
                }
            }
        });
    }

    public record WithdrawExpPayload(int amount) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<WithdrawExpPayload> TYPE =
                new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ModRegistries.MOD_ID, "withdraw_exp"));

        public static final StreamCodec<ByteBuf, WithdrawExpPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT,
                        WithdrawExpPayload::amount,
                        WithdrawExpPayload::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record WithdrawAllExpPayload(boolean withdrawAll) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<WithdrawAllExpPayload> TYPE =
                new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ModRegistries.MOD_ID, "withdraw_all_exp"));

        public static final StreamCodec<ByteBuf, WithdrawAllExpPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.BOOL,
                        WithdrawAllExpPayload::withdrawAll,
                        WithdrawAllExpPayload::new
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

}