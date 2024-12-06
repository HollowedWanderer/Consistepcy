package com.example.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class DisableSprintingMixin extends Entity {

    public DisableSprintingMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "setSprinting", at = @At("HEAD"), cancellable = true)
    private void disableSprinting(boolean sprinting, CallbackInfo ci) {
        if ((LivingEntity) (Object) this instanceof PlayerEntity player && !player.getAbilities().flying && !player.isSubmergedInWater()) {
            // Prevent sprinting from being enabled
            if (sprinting) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void disableSprintingOnLanding(CallbackInfo ci) {
        if ((LivingEntity) (Object) this instanceof PlayerEntity player) {
            // If the player is on the ground and sprinting, disable sprinting
            if (player.isOnGround() && player.isSprinting()) {
                player.setSprinting(false);
            }
        }
    }
}
