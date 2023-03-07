package dev.cammiescorner.fireworkfrenzy;

import dev.cammiescorner.fireworkfrenzy.enchantments.AirStrikeEnchantment;
import dev.cammiescorner.fireworkfrenzy.enchantments.TakeoffEnchantment;
import dev.cammiescorner.fireworkfrenzy.entities.PotionCloudEntity;
import dev.cammiescorner.fireworkfrenzy.integration.FireworkFrenzyConfig;
import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class FireworkFrenzy implements ModInitializer {
	public static final TrackedData<Boolean> BLAST_JUMPING = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	public static final TrackedData<Integer> TIME_ON_GROUND = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.INTEGER);
	public static final String MOD_ID = "fireworkfrenzy";

	public static Enchantment TAKEOFF;
	public static Enchantment AIR_STRIKE;
	public static EntityType<PotionCloudEntity> POTION_CLOUD;

	@Override
	public void onInitialize() {
		MidnightConfig.init(MOD_ID, FireworkFrenzyConfig.class);

		TAKEOFF = Registry.register(Registries.ENCHANTMENT, new Identifier(MOD_ID, "takeoff"), new TakeoffEnchantment());
		AIR_STRIKE = Registry.register(Registries.ENCHANTMENT, new Identifier(MOD_ID, "air_strike"), new AirStrikeEnchantment());
		POTION_CLOUD = Registry.register(Registries.ENTITY_TYPE, new Identifier(MOD_ID, "potion_cloud"), FabricEntityTypeBuilder.create().entityFactory(PotionCloudEntity::new).fireImmune().dimensions(EntityDimensions.changing(6F, 6F)).build());
	}
}
