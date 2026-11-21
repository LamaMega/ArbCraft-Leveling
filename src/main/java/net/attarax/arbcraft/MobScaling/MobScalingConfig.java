//package net.attarax.arbcraft.MobScaling;
//
//import net.neoforged.neoforge.common.ModConfigSpec;
//
//public class MobScalingConfig {
//    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
//
//    public static final ModConfigSpec.DoubleValue HP_MULTIPLIER = BUILDER
//            .comment("Base HP multiplier per experience level (default: 0.1 = 10%)")
//            .defineInRange("hpMultiplier", 0.01, 0.0, 10.0);
//
//    public static final ModConfigSpec.DoubleValue DAMAGE_MULTIPLIER = BUILDER
//            .comment("Base damage multiplier per experience level (default: 0.05 = 5%)")
//            .defineInRange("damageMultiplier", 0.005, 0.0, 10.0);
//
//    public static final ModConfigSpec.DoubleValue SPEED_MULTIPLIER = BUILDER
//            .comment("Base speed multiplier per experience level (default: 0.02 = 2%)")
//            .defineInRange("speedMultiplier", 0.005, 0.0, 1.0);
//
//    public static final ModConfigSpec.DoubleValue DAY_MULTIPLIER = BUILDER
//            .comment("Multiplier per day for all attributes (default: 0.01 = 1%)")
//            .defineInRange("dayMultiplier", 0.005, 0.0, 1.0);
//
//    public static final ModConfigSpec.DoubleValue MAX_SPEED_MULTIPLIER = BUILDER
//            .comment("Maximum speed multiplier to prevent mobs from becoming too fast (default: 2.0 = 200%)")
//            .defineInRange("maxSpeedMultiplier", 2.0, 1.0, 10.0);
//
//    public static final ModConfigSpec.DoubleValue EXP_DIVISOR = BUILDER
//            .comment("Divide total experience by this value for scaling calculations (default: 10)")
//            .defineInRange("expDivisor", 0.003, 0.000001, 1000);
//
//    public static final ModConfigSpec SPEC = BUILDER.build();
//}
