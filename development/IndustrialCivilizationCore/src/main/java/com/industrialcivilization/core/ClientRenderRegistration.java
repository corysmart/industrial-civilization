package com.industrialcivilization.core;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderVillager;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Faction-readable NPC skins without adding heavyweight custom entity models. */
@SideOnly(Side.CLIENT)
public final class ClientRenderRegistration {
    public static void register() {
        RenderingRegistry.registerEntityRenderingHandler(EntityVillager.class,
            (RenderManager manager) -> new FactionVillagerRenderer(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityRobber.class,
            (RenderManager manager) -> new RobberRenderer(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityMilitiaPatrol.class,
            (RenderManager manager) -> new MilitiaRenderer(manager));
    }

    private static final class FactionVillagerRenderer extends RenderVillager {
        FactionVillagerRenderer(RenderManager manager) { super(manager); }
        @Override protected ResourceLocation getEntityTexture(EntityVillager entity) {
            String faction = entity.getEntityData().getString("IndustrialFaction");
            if (faction.isEmpty()) return super.getEntityTexture(entity);
            return texture(faction);
        }
    }

    private static final class RobberRenderer extends RenderBiped<EntityRobber> {
        RobberRenderer(RenderManager manager) { super(manager, new ModelBiped(), 0.5F); }
        @Override protected ResourceLocation getEntityTexture(EntityRobber entity) {
            return texture("ashline_raiders");
        }
    }

    private static final class MilitiaRenderer extends RenderBiped<EntityMilitiaPatrol> {
        MilitiaRenderer(RenderManager manager) { super(manager, new ModelBiped(), 0.5F); }
        @Override protected ResourceLocation getEntityTexture(EntityMilitiaPatrol entity) {
            return texture("territorial_militia");
        }
    }

    private static ResourceLocation texture(String id) {
        return new ResourceLocation(IndustrialCivilizationCore.MODID, "textures/entity/factions/" + id + ".png");
    }
    private ClientRenderRegistration() {}
}
