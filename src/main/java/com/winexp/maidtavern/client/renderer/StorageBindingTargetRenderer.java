package com.winexp.maidtavern.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.winexp.maidtavern.item.MaidTavernItems;
import com.winexp.maidtavern.maid.brewing.StorageBinding;
import com.winexp.maidtavern.util.RenderUtil;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@EventBusSubscriber(value = Dist.CLIENT)
public class StorageBindingTargetRenderer {
    private static final List<CompiledCube> cubes = new ArrayList<>();
    private static StorageBinding.Type prevType;
    private static StorageBinding prevBinding;

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        RenderLevelStageEvent.Stage stage = event.getStage();
        if (cubes.isEmpty()) return;
        if (stage == RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player == null) return;
            if (mc.options.hideGui || player.isSpectator()) return;
            Camera camera = event.getCamera();
            Vec3 cameraPos = camera.getPosition();
            PoseStack poseStack = event.getPoseStack();
            MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
            VertexConsumer faceConsumer = buffer.getBuffer(MaidTavernRenderTypes.HILIGHT_CUBE);
            for (CompiledCube cube : cubes) {
                Vec3 cubePos = cube.pos.subtract(cameraPos);
                float size = cube.size;
                int color = cube.color;
                float red = FastColor.ARGB32.red(color) / 255f;
                float green = FastColor.ARGB32.green(color) / 255f;
                float blue = FastColor.ARGB32.blue(color) / 255f;
                float alpha = FastColor.ARGB32.alpha(color) / 255f;
                RenderUtil.renderCube(poseStack, faceConsumer, cubePos, size, red, green, blue, alpha);
            }
            RenderSystem.disableDepthTest();
            buffer.endBatch(MaidTavernRenderTypes.HILIGHT_CUBE);
            VertexConsumer outlineConsumer = buffer.getBuffer(MaidTavernRenderTypes.HILIGHT_CUBE_OUTLINE);
            for (CompiledCube cube : cubes) {
                Vec3 cubePos = cube.pos.subtract(cameraPos);
                float size = cube.size;
                int color = cube.outlineColor;
                float red = FastColor.ARGB32.red(color) / 255f;
                float green = FastColor.ARGB32.green(color) / 255f;
                float blue = FastColor.ARGB32.blue(color) / 255f;
                float alpha = FastColor.ARGB32.alpha(color) / 255f;
                RenderUtil.renderCubeOutline(poseStack, outlineConsumer, cubePos, size, red, green, blue, alpha);
            }
            RenderSystem.disableDepthTest();
            buffer.endBatch(MaidTavernRenderTypes.HILIGHT_CUBE_OUTLINE);
        }
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        Level level = mc.level;
        if (player == null || level == null) return;
        ItemStack stack = player.getMainHandItem();
        StorageBinding binding = stack.get(MaidTavernItems.STORAGE_BINDING_DATA);
        StorageBinding.Type type = stack.get(MaidTavernItems.STORAGE_BINDING_TYPE_DATA);
        if (Objects.equals(prevBinding, binding) && prevType == type) return;
        cubes.clear();
        prevBinding = binding;
        prevType = type;
        if (binding == null || type == null) return;
        int color = switch (type) {
            case StorageBinding.Type.INGREDIENTS -> FastColor.ARGB32.color(210, 30, 30);
            case StorageBinding.Type.RESULTS -> FastColor.ARGB32.color(30, 210, 30);
            case StorageBinding.Type.BYPRODUCTS -> FastColor.ARGB32.color(30, 30, 210);
        };
        for (BlockPos pos : binding.get(type)) {
            cubes.add(new CompiledCube(pos.getCenter(), 1f,
                    FastColor.ARGB32.color(70, color), color));
        }
    }

    public record CompiledCube(Vec3 pos, float size, int color, int outlineColor) {
    }
}
