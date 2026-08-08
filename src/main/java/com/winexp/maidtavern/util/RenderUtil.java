package com.winexp.maidtavern.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class RenderUtil {
    public static void renderCube(PoseStack poseStack, VertexConsumer consumer, Vec3 center, float size, float red, float green, float blue, float alpha) {
        float half = size * 0.5f;

        float minX = (float) center.x - half;
        float maxX = (float) center.x + half;
        float minY = (float) center.y - half;
        float maxY = (float) center.y + half;
        float minZ = (float) center.z - half;
        float maxZ = (float) center.z + half;

        PoseStack.Pose last = poseStack.last();
        Matrix4f pose = last.pose();

        addFace(
                consumer, pose,
                minX, minY, minZ,
                minX, minY, maxZ,
                minX, maxY, maxZ,
                minX, maxY, minZ,
                red, green, blue, alpha
        );
        addFace(
                consumer, pose,
                maxX, minY, maxZ,
                maxX, minY, minZ,
                maxX, maxY, minZ,
                maxX, maxY, maxZ,
                red, green, blue, alpha
        );

        addFace(
                consumer, pose,
                minX, minY, maxZ,
                minX, minY, minZ,
                maxX, minY, minZ,
                maxX, minY, maxZ,
                red, green, blue, alpha
        );
        addFace(
                consumer, pose,
                minX, maxY, minZ,
                minX, maxY, maxZ,
                maxX, maxY, maxZ,
                maxX, maxY, minZ,
                red, green, blue, alpha
        );

        addFace(
                consumer, pose,
                minX, minY, minZ,
                minX, maxY, minZ,
                maxX, maxY, minZ,
                maxX, minY, minZ,
                red, green, blue, alpha
        );
        addFace(
                consumer, pose,
                minX, maxY, maxZ,
                minX, minY, maxZ,
                maxX, minY, maxZ,
                maxX, maxY, maxZ,
                red, green, blue, alpha
        );
    }

    public static void addFace(
            VertexConsumer consumer, Matrix4f pose,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float x4, float y4, float z4,
            float red, float green, float blue, float alpha
    ) {
        addVertex(consumer, pose, x1, y1, z1, red, green, blue, alpha);
        addVertex(consumer, pose, x2, y2, z2, red, green, blue, alpha);
        addVertex(consumer, pose, x3, y3, z3, red, green, blue, alpha);

        addVertex(consumer, pose, x1, y1, z1, red, green, blue, alpha);
        addVertex(consumer, pose, x3, y3, z3, red, green, blue, alpha);
        addVertex(consumer, pose, x4, y4, z4, red, green, blue, alpha);
    }

    public static void addVertex(VertexConsumer consumer, Matrix4f matrix4f, float x, float y, float z, float red, float green, float blue, float alpha) {
        consumer.addVertex(matrix4f, x, y, z).setColor(red, green, blue, alpha);
    }
}
