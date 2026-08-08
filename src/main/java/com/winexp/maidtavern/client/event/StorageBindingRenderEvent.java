package com.winexp.maidtavern.client.event;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Pair;
import com.winexp.maidtavern.item.MaidTavernItems;
import com.winexp.maidtavern.maid.brew.StorageBinding;
import com.winexp.maidtavern.util.RenderUtil;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
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

import java.util.*;

@EventBusSubscriber(value = Dist.CLIENT)
public class StorageBindingRenderEvent {
    private static final RenderType TRIANGLES = RenderType.create(
            "storage_binding_triangles",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.TRIANGLES,
            1536,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderType.POSITION_COLOR_SHADER)
                    .setTransparencyState(RenderType.NO_TRANSPARENCY)
                    .setCullState(RenderType.NO_CULL)
                    .setDepthTestState(RenderType.NO_DEPTH_TEST)
                    .setWriteMaskState(RenderType.COLOR_WRITE)
                    .createCompositeState(false)
    );

    private static final List<Pair<Vec3, Integer>> cubes = new ArrayList<>();

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player == null) return;
            Camera camera = event.getCamera();
            Vec3 cameraPos = camera.getPosition();
            PoseStack poseStack = event.getPoseStack();
            VertexConsumer consumer = mc.renderBuffers().bufferSource().getBuffer(TRIANGLES);
            for (Pair<Vec3, Integer> cube : cubes) {
                Vec3 cubePos = cube.getFirst().subtract(cameraPos);
                int color = cube.getSecond();
                RenderUtil.renderCube(poseStack, consumer, cubePos, 0.4f,
                        FastColor.ARGB32.red(color) / 255f,
                        FastColor.ARGB32.green(color) / 255f,
                        FastColor.ARGB32.blue(color) / 255f,
                        FastColor.ARGB32.alpha(color) / 255f
                );
            }
        }
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        cubes.clear();
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        Level level = mc.level;
        if (player == null || level == null) return;
        ItemStack stack = player.getMainHandItem();
        StorageBinding binding = stack.get(MaidTavernItems.STORAGE_BINDING_DATA);
        if (binding == null) return;
        StorageBinding.Type type = stack.getOrDefault(MaidTavernItems.STORAGE_BINDING_TYPE_DATA, StorageBinding.Type.INGREDIENTS);
        int color = switch (type) {
            case StorageBinding.Type.INGREDIENTS -> FastColor.ARGB32.color(210, 0, 0);
            case StorageBinding.Type.RESULTS -> FastColor.ARGB32.color(0, 210, 0);
            case StorageBinding.Type.BYPRODUCTS -> FastColor.ARGB32.color(0, 0, 210);
        };
        for (BlockPos pos : binding.get(type)) {
            cubes.add(new Pair<>(pos.getCenter(), color));
        }
    }
}
