//package net.attarax.arbcraft.items;
//
//import net.minecraft.core.registries.Registries;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.tags.BlockTags;
//import net.minecraft.tags.TagKey;
//import net.minecraft.world.item.Item;
//import net.minecraft.world.item.PickaxeItem;
//import net.minecraft.world.item.Tier;
//import net.minecraft.world.item.crafting.Ingredient;
//import net.minecraft.world.level.block.Block;
//
//public class PickaxeOfHolyMotal extends PickaxeItem {
//
//    private static final Tier TOOL_MATERIAL = new Tier() {
//        @Override
//        public int getUses() {
//            return 36000;
//        }
//
//        @Override
//        public float getSpeed() {
//            return 18f;
//        }
//
//        @Override
//        public float getAttackDamageBonus() {
//            return 0;
//        }
//
//        @Override
//        public TagKey<Block> getIncorrectBlocksForDrops() {
//            return BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
//        }
//
//        @Override
//        public int getEnchantmentValue() {
//            return 15;
//        }
//
//        @Override
//        public Ingredient getRepairIngredient() {
//            return Ingredient.of(TagKey.create(Registries.ITEM, ResourceLocation.parse("arbcraft:pickaxe_of_holy_motal_repair_items")));
//        }
//    };
//
//    public PickaxeOfHolyMotal(Item.Properties properties) {
//        super(TOOL_MATERIAL, properties.fireResistant().attributes(PickaxeItem.createAttributes(TOOL_MATERIAL, 11f, -1.7f)));
//    }
//}