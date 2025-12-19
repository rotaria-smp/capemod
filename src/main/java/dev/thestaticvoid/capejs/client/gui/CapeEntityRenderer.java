package dev.thestaticvoid.capejs.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * Renders a 3D cape entity (without a player model)
 */
public class CapeEntityRenderer {
    private final int x, y, width, height;
    private float rotationAngle = 180f; // Start at 180 to show front
    private boolean isDragging = false;
    private double lastMouseX;

    // Cape dimensions (standard Minecraft cape)
    private static final float CAPE_WIDTH = 10.0f;
    private static final float CAPE_HEIGHT = 16.0f;

    public CapeEntityRenderer(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, String capeId) {
        // Draw background panel
        guiGraphics.fill(x, y, x + width, y + height, 0x88000000);

        if (capeId == null || capeId.isEmpty()) {
            Minecraft mc = Minecraft.getInstance();
            String msg = "No cape selected";
            guiGraphics.drawCenteredString(mc.font, msg, x + width / 2, y + height / 2, 0xAAAAAA);
            return;
        }

        // Calculate center position
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        int scale = Math.min(width, height) / 2; // Made bigger (was /4)

        // Render the 3D cape
        renderCape3D(guiGraphics, centerX, centerY, scale, capeId);

        // Draw rotation hint
        Minecraft mc = Minecraft.getInstance();
        guiGraphics.drawString(mc.font, "Click and drag to rotate", centerX - 60, y + 10, 0xAAAAAA);
    }

    private void renderCape3D(GuiGraphics guiGraphics, int x, int y, int scale, String capeId) {
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();

        // Position
        poseStack.translate(x, y, 100.0);
        poseStack.scale(scale, scale, scale);

        // Rotation
        Quaternionf rotY = new Quaternionf().rotateY((float) Math.toRadians(rotationAngle));
        Quaternionf rotX = new Quaternionf().rotateX((float) Math.toRadians(20f)); // Slight tilt
        poseStack.mulPose(rotY);
        poseStack.mulPose(rotX);

        // Get cape texture
        ResourceLocation capeTexture = ResourceLocation.fromNamespaceAndPath(
                "capejs",
                "textures/capes/" + capeId + ".png"
        );

        // Render the cape geometry
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entitySolid(capeTexture));

        renderCapeGeometry(poseStack, vertexConsumer);

        bufferSource.endBatch();
        poseStack.popPose();
    }

    private void renderCapeGeometry(PoseStack poseStack, VertexConsumer vertexConsumer) {
        Matrix4f matrix = poseStack.last().pose();

        // Standard Minecraft cape texture layout:
        // Full texture: 64x32 pixels
        // Front face: (1, 1) to (11, 17) - that's 10 pixels wide, 16 pixels tall
        // Back face: (12, 1) to (22, 17) - also 10 pixels wide, 16 pixels tall

        // However, some custom capes might use simplified layouts
        // We'll use the standard Minecraft layout which should work for most capes

        float texWidth = 64f;
        float texHeight = 32f;

        // Front face of cape (main visible part)
        float frontU0 = 1f / texWidth;      // x=1
        float frontU1 = 11f / texWidth;     // x=11
        float frontV0 = 1f / texHeight;     // y=1
        float frontV1 = 17f / texHeight;    // y=17

        // Back face of cape
        float backU0 = 12f / texWidth;      // x=12
        float backU1 = 22f / texWidth;      // x=22
        float backV0 = 1f / texHeight;      // y=1
        float backV1 = 17f / texHeight;     // y=17

        // Cape physical dimensions
        float width = CAPE_WIDTH / 16f;   // 0.625 units
        float height = CAPE_HEIGHT / 16f;  // 1.0 units

        // Render front face
        renderQuad(matrix, vertexConsumer,
                -width/2, 0, 0.01f,       // Top left
                width/2, 0, 0.01f,       // Top right
                width/2, height, 0.01f,  // Bottom right
                -width/2, height, 0.01f,  // Bottom left
                frontU0, frontV0, frontU1, frontV1,
                0, 0, 1  // Normal pointing toward camera
        );

        // Render back face
        renderQuad(matrix, vertexConsumer,
                width/2, 0, -0.01f,       // Top right (reversed winding for back)
                -width/2, 0, -0.01f,       // Top left
                -width/2, height, -0.01f,  // Bottom left
                width/2, height, -0.01f,  // Bottom right
                backU0, backV0, backU1, backV1,
                0, 0, -1  // Normal pointing away from camera
        );
    }

    private void renderQuad(Matrix4f matrix, VertexConsumer consumer,
                            float x0, float y0, float z0,
                            float x1, float y1, float z1,
                            float x2, float y2, float z2,
                            float x3, float y3, float z3,
                            float u0, float v0, float u1, float v1,
                            float normalX, float normalY, float normalZ) {
        // Vertex 1 (top left)
        consumer.addVertex(matrix, x0, y0, z0)
                .setColor(255, 255, 255, 255)
                .setUv(u0, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0)
                .setNormal(normalX, normalY, normalZ);

        // Vertex 2 (top right)
        consumer.addVertex(matrix, x1, y1, z1)
                .setColor(255, 255, 255, 255)
                .setUv(u1, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0)
                .setNormal(normalX, normalY, normalZ);

        // Vertex 3 (bottom right)
        consumer.addVertex(matrix, x2, y2, z2)
                .setColor(255, 255, 255, 255)
                .setUv(u1, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0)
                .setNormal(normalX, normalY, normalZ);

        // Vertex 4 (bottom left)
        consumer.addVertex(matrix, x3, y3, z3)
                .setColor(255, 255, 255, 255)
                .setUv(u0, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0xF000F0)
                .setNormal(normalX, normalY, normalZ);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            isDragging = true;
            lastMouseX = mouseX;
            return true;
        }
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isDragging = false;
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDragging) {
            double delta = mouseX - lastMouseX;
            rotationAngle += (float) delta * 2f; // Faster rotation

            // Keep angle in 0-360 range
            while (rotationAngle >= 360f) rotationAngle -= 360f;
            while (rotationAngle < 0f) rotationAngle += 360f;

            lastMouseX = mouseX;
            return true;
        }
        return false;
    }
}