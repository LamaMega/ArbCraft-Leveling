package net.attarax.arbcraft.network;

import net.attarax.arbcraft.blockentity.ExperienceConverterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record WithdrawExperiencePacket(BlockPos pos, int amount) implements CustomPacketPayload {

    public static final Type<WithdrawExperiencePacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("arbcraft", "withdraw_experience"));

    public static final StreamCodec<FriendlyByteBuf, WithdrawExperiencePacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    WithdrawExperiencePacket::pos,
                    ByteBufCodecs.VAR_INT,
                    WithdrawExperiencePacket::amount,
                    WithdrawExperiencePacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(WithdrawExperiencePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                BlockEntity be = player.level().getBlockEntity(packet.pos);
                if (be instanceof ExperienceConverterBlockEntity converter) {
                    if (packet.amount == -1) {
                        // Withdraw all
                        converter.withdrawAllExperience(player);
                    } else {
                        // Withdraw specific amount
                        converter.withdrawExperience(player, packet.amount);
                    }
                }
            }
        });
    }
}