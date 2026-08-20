package com.winexp.maidtavern.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

import java.util.OptionalDouble;

public class MaidTavernRenderTypes extends RenderType {
    private MaidTavernRenderTypes(String pName, VertexFormat pFormat, VertexFormat.Mode pMode, int pBufferSize, boolean pAffectsCrumbling, boolean pSortOnUpload, Runnable pSetupState, Runnable pClearState) {
        super(pName, pFormat, pMode, pBufferSize, pAffectsCrumbling, pSortOnUpload, pSetupState, pClearState);
    }

    public static final RenderType HILIGHT_CUBE = RenderType.create(
            "hilight_cube",
            DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.QUADS,
            256,
            false, false,
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

    protected static final RenderStateShard.LineStateShard THIN_LINE = new RenderStateShard.LineStateShard(OptionalDouble.of(3.0));
    public static final RenderType HILIGHT_CUBE_OUTLINE = RenderType.create(
            "hilight_cube_outline",
            DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.LINES,
            256,
            false, false,
            RenderType.CompositeState.builder()
                    .setLineState(THIN_LINE)
                    .setShaderState(RenderType.RENDERTYPE_LINES_SHADER)
                    .setLayeringState(RenderType.VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderType.NO_CULL)
                    .setTextureState(RenderType.NO_TEXTURE)
                    .setLightmapState(RenderType.NO_LIGHTMAP)
                    .setDepthTestState(RenderType.NO_DEPTH_TEST)
                    .setWriteMaskState(RenderType.COLOR_WRITE)
                    .createCompositeState(false)
    );
}
