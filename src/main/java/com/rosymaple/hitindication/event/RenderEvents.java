package com.rosymaple.hitindication.event;

import com.rosymaple.hitindication.HitIndication;
import com.rosymaple.hitindication.capability.latesthits.ClientLatestHits;
import com.rosymaple.hitindication.capability.latesthits.Hit;
import com.rosymaple.hitindication.capability.latesthits.Indicator;
import com.rosymaple.hitindication.config.HitIndicatorConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;

import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;


@Mod.EventBusSubscriber(modid = HitIndication.MODID)
public class RenderEvents {
    private static final ResourceLocation INDICATOR_RED = new ResourceLocation(HitIndication.MODID, "textures/hit/indicator_red.png");
    private static final ResourceLocation INDICATOR_BLUE = new ResourceLocation(HitIndication.MODID, "textures/hit/indicator_blue.png");
    private static final ResourceLocation INDICATOR_GREEN = new ResourceLocation(HitIndication.MODID, "textures/hit/indicator_green.png");
    private static final ResourceLocation INDICATOR_YELLOW = new ResourceLocation(HitIndication.MODID, "textures/hit/indicator_yellow.png");


    @SubscribeEvent
    public static void onRender(RenderGameOverlayEvent.Post event) {
        if(event.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE)
            return;

        Minecraft mc = Minecraft.getMinecraft();
        TextureManager textureManager = mc.getTextureManager();

        ScaledResolution scaledResolution = new ScaledResolution(mc);
        int screenMiddleX = scaledResolution.getScaledWidth() / 2;
        int screenMiddleY = scaledResolution.getScaledHeight() / 2;

        Vector2d lookVec = getLookVec(mc.player);
        Vector2d playerPos = new Vector2d(mc.player.posX, mc.player.posZ);
        for(Hit hit : ClientLatestHits.latestHits) {
            drawIndicator(hit, textureManager, screenMiddleX, screenMiddleY, playerPos, lookVec);
        }
    }

    private static void drawIndicator(Hit hit, TextureManager textureManager, int screenMiddleX, int screenMiddleY, Vector2d playerPos, Vector2d lookVec) {
        Vector3d sourceVec3d = hit.getLocation();
        Vector2d diff = new Vector2d(sourceVec3d.x - playerPos.x, sourceVec3d.z - playerPos.y);
        double angleBetween = angleBetween(lookVec, diff);
        float opacity = hit.getLifeTime() >= 25
                ? HitIndicatorConfig.IndicatorOpacity
                : HitIndicatorConfig.IndicatorOpacity * hit.getLifeTime() / 25.0f;
        opacity /= 100.0f;

        bindIndicatorTexture(textureManager, hit.getIndicator());

        GL11.glPushMatrix();
        GL11.glColor4f(1, 1, 1, opacity);
        GL11.glTranslatef(screenMiddleX, screenMiddleY, 0);
        GL11.glRotatef((float)angleBetween, 0, 0, 1);
        GL11.glTranslatef(-screenMiddleX, -screenMiddleY, 0);
        Gui.drawModalRectWithCustomSizedTexture(screenMiddleX - 21, screenMiddleY - 37 , 0, 0, 42, 13, 42, 13);
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glPopMatrix();
    }

    private static void bindIndicatorTexture(TextureManager textureManager, Indicator type) {
        switch(type) {
            case BLUE: textureManager.bindTexture(INDICATOR_BLUE);  break;
            default: textureManager.bindTexture(INDICATOR_RED);  break;
        }
    }

    private static double angleBetween(Vector2d first, Vector2d second) {
        double dot = first.x * second.x + first.y * second.y;
        double cross = first.x * second.y - second.x * first.y;
        double res = Math.atan2(cross, dot) * 180 / Math.PI;

        return res;
    }

    private static Vector2d getLookVec(EntityPlayerSP player) {
        return new Vector2d(-Math.sin(-player.rotationYaw * Math.PI / 180.0 - Math.PI), -Math.cos(-player.rotationYaw * Math.PI / 180.0 - Math.PI));
    }
}