package com.winexp.maidtavern.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@EventBusSubscriber(value = Dist.CLIENT)
public class StorageBindingTargetRenderer {
    private static final RenderType QUADS = RenderType.create(
            "storage_binding_triangles",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            1536,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderType.POSITION_COLOR_SHADER)
                    .setLayeringState(RenderType.VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderType.NO_CULL)
                    .setTextureState(RenderType.NO_TEXTURE)
                    .setLightmapState(RenderType.NO_LIGHTMAP)
                    .setDepthTestState(RenderType.NO_DEPTH_TEST)
                    .setWriteMaskState(RenderType.COLOR_WRITE)
                    .createCompositeState(false)
    );

    private static final List<CompiledCube> cubes = new ArrayList<>();
    private static StorageBinding.Type prevType;
    private static StorageBinding prevBinding;

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        RenderLevelStageEvent.Stage stage = event.getStage();
        if ((stage == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES || stage == RenderLevelStageEvent.Stage.AFTER_WEATHER) && !cubes.isEmpty()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.options.hideGui) return;
            LocalPlayer player = mc.player;
            if (player == null) return;
            Camera camera = event.getCamera();
            Vec3 cameraPos = camera.getPosition();
            PoseStack poseStack = event.getPoseStack();
            VertexConsumer consumer = mc.renderBuffers().bufferSource().getBuffer(QUADS);
            for (CompiledCube cube : cubes) {
                Vec3 cubePos = cube.pos.subtract(cameraPos);
                float size = cube.size;
                int color = cube.color;
                RenderUtil.renderCube(poseStack, consumer, cubePos, size,
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
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        Level level = mc.level;
        if (player == null || level == null) return;
        ItemStack stack = player.getMainHandItem();
        StorageBinding binding = stack.get(MaidTavernItems.STORAGE_BINDING_DATA);
        StorageBinding.Type type = stack.getOrDefault(MaidTavernItems.STORAGE_BINDING_TYPE_DATA, StorageBinding.Type.INGREDIENTS);
        if (Objects.equals(prevBinding, binding) && prevType == type) return;
        cubes.clear();
        prevBinding = binding;
        prevType = type;
        if (binding == null) return;
        int color = switch (type) {
            case StorageBinding.Type.INGREDIENTS -> FastColor.ARGB32.color(210, 0, 0);
            case StorageBinding.Type.RESULTS -> FastColor.ARGB32.color(0, 210, 0);
            case StorageBinding.Type.BYPRODUCTS -> FastColor.ARGB32.color(0, 0, 210);
        };
        for (BlockPos pos : binding.get(type)) {
            cubes.add(new CompiledCube(pos.getCenter(), 0.4f, color));
        }
    }

    public record CompiledCube(Vec3 pos, float size, int color) {
    }
}
