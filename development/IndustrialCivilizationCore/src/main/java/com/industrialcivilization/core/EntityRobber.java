package com.industrialcivilization.core;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIBreakDoor;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** A network-registered human robber with no undead identity or audio. */
public final class EntityRobber extends EntityMob {
    public static final double BASE_MOVEMENT_SPEED = 0.20D;

    public EntityRobber(World world) {
        super(world);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)
            .setBaseValue(BASE_MOVEMENT_SPEED);
    }

    @Override
    protected void initEntityAI() {
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(1, new EntityAIBreakDoor(this));
        tasks.addTask(2, new EntityAIAttackMelee(this, 0.95D, false));
        tasks.addTask(5, new EntityAIWanderAvoidWater(this, 0.85D));
        tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 12.0F));
        tasks.addTask(7, new EntityAILookIdle(this));
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
    }

    @Nullable
    @Override protected SoundEvent getAmbientSound() { return null; }
    @Override protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_PLAYER_HURT;
    }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.ENTITY_PLAYER_DEATH; }
    @Override protected void playStepSound(BlockPos pos, Block block) {
        playSound(SoundEvents.BLOCK_CLOTH_STEP, 0.15F, 1.0F);
    }

    /** Clear the persistence marker written by builds that converted every zombie. */
    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        compound.setBoolean("PersistenceRequired", false);
        super.readEntityFromNBT(compound);
    }
}
