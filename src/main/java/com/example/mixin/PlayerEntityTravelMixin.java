package com.example.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityTravelMixin extends LivingEntity {
    @Shadow public abstract PlayerAbilities getAbilities();

    @Shadow public abstract boolean isSwimming();

    @Unique
    private static final float BACKWARD_SPEED = 0.06f; // Default backward speed

    protected PlayerEntityTravelMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void modifyTravel(Vec3d movementInput, CallbackInfo ci) {
        if (!this.getAbilities().flying && !this.isSwimming()) {
            // Modify movement logic only if not flying or in a vehicle
            Vec3d adjustedInput = movementInput;

            if (movementInput.z < 0) {
                // Reduce backward movement speed
                adjustedInput = new Vec3d(movementInput.x, movementInput.y, movementInput.z * BACKWARD_SPEED / 0.1f);
            }

            super.travel(adjustedInput); // Call travel with adjusted input
            ci.cancel(); // Prevent original travel logic from executing
        }
    }

    @Inject(method = "getMovementSpeed", at = @At("HEAD"), cancellable = true)
    public void getMovementSpeed(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue((float)this.getAttributeValue(EntityAttributes.MOVEMENT_SPEED) * 1.7F);
    }

    @Inject(method = "getOffGroundSpeed", at = @At("HEAD"), cancellable = true)
    private void modifyAirMovementSpeed(CallbackInfoReturnable<Float> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        // Check if the player is flying; if so, leave the default functionality
        if (player.getAbilities().flying) {
            return; // Do not modify the flying behavior
        }

        // When the player is airborne and not flying, always apply sprinting speed
        if (!player.isOnGround() && !player.hasVehicle()) {
            float sprintSpeedMultiplier = 0.05F; // Vanilla sprinting air speed
            cir.setReturnValue(sprintSpeedMultiplier); // Apply sprinting speed
        }
    }
}
