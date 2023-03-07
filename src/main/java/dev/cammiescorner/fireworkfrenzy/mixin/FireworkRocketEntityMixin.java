package dev.cammiescorner.fireworkfrenzy.mixin;

import dev.cammiescorner.fireworkfrenzy.FireworkFrenzy;
import dev.cammiescorner.fireworkfrenzy.entities.PotionCloudEntity;
import dev.cammiescorner.fireworkfrenzy.integration.FireworkFrenzyConfig;
import dev.cammiescorner.fireworkfrenzy.util.BlastJumper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;

@Mixin(FireworkRocketEntity.class)
public abstract class FireworkRocketEntityMixin extends ProjectileEntity implements FlyingItemEntity {
	@Shadow @Final private static TrackedData<ItemStack> ITEM;
	@Shadow protected abstract boolean hasExplosionEffects();
	@Shadow public abstract ItemStack getStack();

	@Shadow private @Nullable LivingEntity shooter;
	@Unique public LivingEntity directTarget;

	public FireworkRocketEntityMixin(EntityType<? extends ProjectileEntity> type, World world) { super(type, world); }

	@Inject(method = "explode", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
			ordinal = 1
	), locals = LocalCapture.CAPTURE_FAILSOFT)
	public void fireworkfrenzy$explodePostDamage(CallbackInfo info, float damage, ItemStack stack, NbtCompound tag, NbtList nbtList, double d, Vec3d vec, List<LivingEntity> list, Iterator<LivingEntity> iterator, LivingEntity target) {
		NbtCompound subNbt = dataTracker.get(ITEM).getSubNbt("Fireworks");

		if(FireworkFrenzyConfig.allowRocketJumping && hasExplosionEffects() && subNbt != null) {
			Box box = getBoundingBox().expand(d);
			float radius = (float) (box.getXLength() / 2);
			double multiplier = (subNbt.getList("Explosions", NbtElement.COMPOUND_TYPE).size() * 0.4	) * FireworkFrenzyConfig.rocketJumpMultiplier;
			DamageSource source = DamageSource.firework((FireworkRocketEntity) (Object) this, getOwner());

			if(!target.blockedByShield(source)) {
				Vec3d targetPos = target.getPos().add(0, MathHelper.clamp(getY() - target.getY(), 0, target.getHeight()), 0);
				Vec3d direction = targetPos.subtract(getPos());
				double distance = direction.length() - (getWidth() * 0.5) - (target.getWidth() * 0.5);
				double inverseDistance = MathHelper.clamp(1 - (distance / radius), 0, 1);
				float fireworkDamage = (target instanceof PlayerEntity ? FireworkFrenzyConfig.playerDamage : FireworkFrenzyConfig.mobDamage) * subNbt.getList("Explosions", NbtElement.COMPOUND_TYPE).size();

				if(target == getOwner() && EnchantmentHelper.getLevel(FireworkFrenzy.TAKEOFF, target.getEquippedStack(EquipmentSlot.FEET)) > 0)
					fireworkDamage = 0;
				if(EnchantmentHelper.getLevel(FireworkFrenzy.AIR_STRIKE, stack) > 0 && getOwner() instanceof BlastJumper jumper && jumper.isBlastJumping())
					fireworkDamage *= FireworkFrenzyConfig.airStrikeDamageMultiplier;

				if(target == directTarget)
					target.damage(source, fireworkDamage);
				else
					target.damage(source, (float) (fireworkDamage * inverseDistance));

				target.knockbackVelocity = 0F;
				target.setVelocity(target.getVelocity().getX(), Math.min(1D, Math.abs(target.getVelocity().getY())), target.getVelocity().getZ());
				target.setVelocity(target.getVelocity().add(direction).multiply(inverseDistance * (target == getOwner() ? multiplier : multiplier * FireworkFrenzyConfig.otherEntityKnockBack)));
				target.velocityModified = true;
			}
		}

		if(target instanceof BlastJumper jumper) {
			jumper.setTimeOnGround(0);
			jumper.setBlastJumping(true);
		}
	}

	@Inject(method = "explode", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILSOFT)
	private void fireworkfrenzy$spawnPotionCloud(CallbackInfo info) {
		ItemStack stack = dataTracker.get(ITEM);
		NbtCompound tag = stack.isEmpty() ? null : stack.getSubNbt("Fireworks");

		if(tag != null) {
			NbtList nbtList = tag.getList("Explosions", NbtElement.COMPOUND_TYPE);

			for(int i = 0; i < nbtList.size(); i++) {
				NbtCompound nbt = nbtList.getCompound(i);

				if(nbt.contains("Potion")) {
					Potion potion = Registries.POTION.get(new Identifier(nbt.getString("Potion")));
					PotionCloudEntity cloud = FireworkFrenzy.POTION_CLOUD.create(world);

					if(cloud != null) {
						cloud.setPotion(potion);
						cloud.setRadius(2.5F);
						cloud.setOwner(shooter);
						cloud.setDuration(200);
						cloud.setWaitTime(0);
						cloud.setPosition(getPos().add(0, -cloud.getRadius(), 0));
						world.spawnEntity(cloud);
					}
				}
			}
		}
	}

	@Inject(method = "onEntityHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/FireworkRocketEntity;explodeAndRemove()V"))
	public void fireworkfrenzy$directHit(EntityHitResult entityHitResult, CallbackInfo info) {
		if(entityHitResult.getEntity() instanceof LivingEntity target)
			directTarget = target;
	}

	@ModifyArg(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", ordinal = 0))
	public float fireworkfrenzy$selfDamage(DamageSource source, float amount) {
		return 0;
	}

	@ModifyArg(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", ordinal = 1))
	public float fireworkfrenzy$crossbowDamage(DamageSource source, float amount) {
		return 0;
	}
}
