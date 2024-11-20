package dev.cammiescorner.fireworkfrenzy;

import org.jetbrains.annotations.Nullable;

import dev.cammiescorner.fireworkfrenzy.common.compat.FireworkFrenzyConfig;
import dev.cammiescorner.fireworkfrenzy.common.enchantments.AirStrikeEnchantment;
import dev.cammiescorner.fireworkfrenzy.common.enchantments.FixedFuseEnchantment;
import dev.cammiescorner.fireworkfrenzy.common.enchantments.TakeoffEnchantment;
import dev.cammiescorner.fireworkfrenzy.common.entities.DamageCloudEntity;
import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ModInitializer;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class FireworkFrenzy implements ModInitializer {
	public static final TrackedData<Boolean> BLAST_JUMPING = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	public static final TrackedData<Integer> TIME_ON_GROUND = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.INTEGER);
	public static final String MOD_ID = "fireworkfrenzy";

	public static Enchantment TAKEOFF;
	public static Enchantment AIR_STRIKE;
	public static Enchantment FIXED_FUSE;
	public static EntityType<DamageCloudEntity> DAMAGE_CLOUD;
	public static final RegistryKey<DamageType> DAMAGE_TYPE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, new Identifier(MOD_ID, "damage_cloud"));

	@Override
	public void onInitialize() {
		MidnightConfig.init(MOD_ID, FireworkFrenzyConfig.class);

		TAKEOFF = Registry.register(Registries.ENCHANTMENT, new Identifier(MOD_ID, "takeoff"), new TakeoffEnchantment());
		AIR_STRIKE = Registry.register(Registries.ENCHANTMENT, new Identifier(MOD_ID, "air_strike"), new AirStrikeEnchantment());
		FIXED_FUSE = Registry.register(Registries.ENCHANTMENT, new Identifier(MOD_ID, "fixed_fuse"), new FixedFuseEnchantment());
		DAMAGE_CLOUD = Registry.register(Registries.ENTITY_TYPE, new Identifier(MOD_ID, "potion_cloud"), EntityType.Builder.create(DamageCloudEntity::new, SpawnGroup.MISC).makeFireImmune().setDimensions(6F, 6F).build("damage_cloud"));
	}

	public static DamageSource damageCloud(DamageCloudEntity cloud, @Nullable Entity owner) {
		return new DamageSource(cloud.getWorld().getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(DAMAGE_TYPE), cloud, owner);
	}
}
