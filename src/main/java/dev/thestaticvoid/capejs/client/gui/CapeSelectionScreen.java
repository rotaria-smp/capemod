package dev.thestaticvoid.capejs.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.thestaticvoid.capejs.client.ClientCapeStorage;
import dev.thestaticvoid.capejs.client.CapeTextureManager;
import dev.thestaticvoid.capejs.core.CapeManager;
import dev.thestaticvoid.capejs.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class CapeSelectionScreen extends Screen {
    private static final int CAPE_BUTTON_WIDTH = 100;
    private static final int CAPE_BUTTON_HEIGHT = 20;
    private static final int CAPE_LIST_PADDING = 5;

    private final Screen lastScreen;
    private List<String> unlockedCapes;
    private String selectedCape;
    private String currentEquippedCape;
    private Button equipButton;
    private Button unequipButton;
    private CapeEntityRenderer capeRenderer;

    public CapeSelectionScreen(Screen lastScreen) {
        super(Component.literal("Cape Selection"));
        this.lastScreen = lastScreen;
        this.unlockedCapes = new ArrayList<>();
        loadUnlockedCapes();
    }

    private void loadUnlockedCapes() {
        // Load from client-side storage instead of NBT
        // (NBT is server-side only and doesn't sync to client automatically)
        System.out.println("[CapeGUI] ===== Loading Capes =====");

        unlockedCapes.clear();
        unlockedCapes.addAll(ClientCapeStorage.getUnlockedCapes());

        currentEquippedCape = ClientCapeStorage.getEquippedCape();
        selectedCape = currentEquippedCape.isEmpty() ? (unlockedCapes.isEmpty() ? null : unlockedCapes.get(0)) : currentEquippedCape;

        System.out.println("[CapeGUI] Loaded " + unlockedCapes.size() + " capes from client storage");
        for (String cape : unlockedCapes) {
            System.out.println("[CapeGUI]   - " + cape);
        }
        System.out.println("[CapeGUI] Currently equipped: '" + currentEquippedCape + "'");
        System.out.println("[CapeGUI] =========================");
    }

    @Override
    protected void init() {
        super.init();

        System.out.println("[CapeGUI] Initializing GUI with " + unlockedCapes.size() + " capes");

        // Initialize cape renderer in the center
        int rendererX = 180;
        int rendererY = 40;
        int rendererWidth = this.width - 380;
        int rendererHeight = this.height - 100;
        this.capeRenderer = new CapeEntityRenderer(rendererX, rendererY, rendererWidth, rendererHeight);

        // Buttons on the right side
        int buttonX = this.width - 150;
        int buttonY = 40;

        // Equip button
        this.equipButton = Button.builder(
                        Component.literal("Equip Cape"),
                        button -> equipSelectedCape()
                )
                .bounds(buttonX, buttonY, 130, 20)
                .build();
        this.addRenderableWidget(equipButton);

        // Unequip button
        this.unequipButton = Button.builder(
                        Component.literal("Unequip Cape"),
                        button -> unequipCurrentCape()
                )
                .bounds(buttonX, buttonY + 25, 130, 20)
                .build();
        this.addRenderableWidget(unequipButton);

//        // Debug: Add test cape button
//        this.addRenderableWidget(Button.builder(
//                        Component.literal("Add Test Cape"),
//                        button -> addTestCape()
//                )
//                .bounds(buttonX, buttonY + 50, 130, 20)
//                .build());

        // Add refresh button
        this.addRenderableWidget(Button.builder(
                        Component.literal("Refresh"),
                        button -> {
                            minecraft.setScreen(new CapeSelectionScreen(lastScreen));
                        }
                )
                .bounds(buttonX, buttonY + 80, 130, 20)
                .build());

        // Close button
        this.addRenderableWidget(Button.builder(
                        Component.literal("Done"),
                        button -> this.minecraft.setScreen(lastScreen)
                )
                .bounds(this.width / 2 - 50, this.height - 25, 100, 20)
                .build());

        updateButtonStates();

        System.out.println("[CapeGUI] Total renderables: " + this.renderables.size());
    }
//
//    private void addTestCape() {
//        if (minecraft.player != null) {
//            String testCape = "test_cape";
//
//            if (!ClientCapeStorage.isCapeUnlocked(testCape)) {
//                ClientCapeStorage.unlockCape(testCape);
//                System.out.println("[CapeGUI] Added test_cape to ClientCapeStorage");
//
//                // Reload the GUI
//                minecraft.setScreen(new CapeSelectionScreen(lastScreen));
//            } else {
//                System.out.println("[CapeGUI] test_cape already unlocked in ClientCapeStorage");
//            }
//        }
//    }

    private void equipSelectedCape() {
        if (selectedCape != null && !selectedCape.equals(currentEquippedCape)) {
            // Add to CapeRegistry first so the mixin can find it
            ResourceLocation capeResource = dev.thestaticvoid.capejs.CapeJS.id(dev.thestaticvoid.capejs.CapeRegistry.locationString(selectedCape));
            dev.thestaticvoid.capejs.CapeRegistry.addCapeToMap(minecraft.player.getUUID().toString(), capeResource);
            System.out.println("[CapeGUI] Added cape to CapeRegistry: " + capeResource);
            unequipCurrentCape();
            // Send packet to server
            NetworkHandler.CapeData payload = new NetworkHandler.CapeData(
                    minecraft.player.getUUID().toString(),
                    selectedCape,
                    false
            );
            PacketDistributor.sendToServer(payload);

            // Update local state
            currentEquippedCape = selectedCape;
            ClientCapeStorage.setEquippedCape(selectedCape);
            CapeManager.register(minecraft.player.getUUID(), selectedCape);

            updateButtonStates();

            System.out.println("[CapeGUI] Equipped cape: " + selectedCape);
        }
    }

    private void unequipCurrentCape() {
        if (currentEquippedCape != null && !currentEquippedCape.isEmpty()) {
            // Remove from CapeRegistry
            dev.thestaticvoid.capejs.CapeRegistry.removeCapeFromMap(minecraft.player.getUUID().toString());
            System.out.println("[CapeGUI] Removed cape from CapeRegistry");

            // Send packet to server
            NetworkHandler.CapeData payload = new NetworkHandler.CapeData(
                    minecraft.player.getUUID().toString(),
                    currentEquippedCape,
                    true
            );
            PacketDistributor.sendToServer(payload);

            // Update local state
            ClientCapeStorage.setEquippedCape("");
            CapeManager.unregister(minecraft.player.getUUID());
            currentEquippedCape = "";

            updateButtonStates();

            System.out.println("[CapeGUI] Unequipped cape");
        }
    }

    private void updateButtonStates() {
        equipButton.active = selectedCape != null && !selectedCape.equals(currentEquippedCape);
        unequipButton.active = currentEquippedCape != null && !currentEquippedCape.isEmpty();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Check if cape list has changed (someone might have run /givecape)
        int currentCapeCount = ClientCapeStorage.getUnlockedCapeCount();
        if (currentCapeCount != unlockedCapes.size()) {
            System.out.println("[CapeGUI] Detected cape list change (" + unlockedCapes.size() + " -> " + currentCapeCount + "), reloading...");
            loadUnlockedCapes();
        }

        // Render a solid background for the entire screen first
        guiGraphics.fill(0, 0, this.width, this.height, 0xC0101010);

        // Render title
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFF);

        // Draw the cape list with visual previews on the left
        int listX = 10;
        int listY = 30;
        int listWidth = 140;
        int listHeight = this.height - 80;

        guiGraphics.fill(listX, listY, listX + listWidth, listY + listHeight, 0xDD000000);
        guiGraphics.drawString(this.font, "Available Capes:", listX + 5, listY + 5, 0xFFFFFF);

        // Draw each cape as a small preview in the list
        int previewY = listY + 20;
        int previewHeight = 40;
        int spacing = 3;

        for (int i = 0; i < unlockedCapes.size(); i++) {
            String cape = unlockedCapes.get(i);
            boolean isSelected = cape.equals(selectedCape);
            boolean isEquipped = cape.equals(currentEquippedCape);

            // Draw selection border
            int borderColor = isSelected ? 0xFF4444FF : (isEquipped ? 0xFF44FF44 : 0xFF333333);
            guiGraphics.fill(listX + 5, previewY, listX + listWidth - 5, previewY + previewHeight, borderColor);

            // Draw inner background
            guiGraphics.fill(listX + 7, previewY + 2, listX + listWidth - 7, previewY + previewHeight - 2, 0xFF1A1A1A);

            // Draw cape name
            guiGraphics.drawString(this.font, cape, listX + 10, previewY + 8, 0xFFFFFF);
            if (isEquipped) {
                guiGraphics.drawString(this.font, "(Equipped)", listX + 10, previewY + 20, 0x44FF44);
            }

            previewY += previewHeight + spacing;
        }

        // Render widgets (buttons on the right)
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // Render 3D cape preview after widgets
        if (capeRenderer != null) {
            capeRenderer.render(guiGraphics, mouseX, mouseY, partialTick, selectedCape);
        }

        // Render info text at bottom
        if (unlockedCapes.isEmpty()) {
            guiGraphics.drawString(this.font, "§cNo capes unlocked! Click 'Add Test Cape' button.", 10, this.height - 15, 0xFF5555);
        } else {
            guiGraphics.drawString(this.font, "Total capes: " + unlockedCapes.size(), 10, this.height - 15, 0xAAAAAA);
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Override to prevent default blur rendering
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Check if clicking on cape preview list
        int listX = 10;
        int listY = 30;
        int listWidth = 140;
        int previewY = listY + 20;
        int previewHeight = 40;
        int spacing = 3;

        for (String cape : unlockedCapes) {
            if (mouseX >= listX + 5 && mouseX <= listX + listWidth - 5 &&
                    mouseY >= previewY && mouseY < previewY + previewHeight) {
                selectedCape = cape;
                System.out.println("[CapeGUI] Cape selected via click: " + cape);
                updateButtonStates();
                return true;
            }
            previewY += previewHeight + spacing;
        }

        // Check if clicking on cape renderer for rotation
        if (capeRenderer != null && capeRenderer.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (capeRenderer != null && capeRenderer.mouseReleased(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (capeRenderer != null && capeRenderer.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    // Inner class for cape list widget
    private class CapeListWidget extends AbstractWidget {
        private final List<CapeButton> capeButtons;
        private int scrollOffset = 0;
        private final int itemHeight;

        public CapeListWidget(Minecraft mc, int width, int height, int y, int bottom, int itemHeight) {
            super(10, y, width, bottom - y, Component.empty());
            this.itemHeight = itemHeight;
            this.capeButtons = new ArrayList<>();

            System.out.println("[CapeListWidget] Creating widget with " + unlockedCapes.size() + " unlocked capes");

            for (String cape : unlockedCapes) {
                capeButtons.add(new CapeButton(cape));
                System.out.println("[CapeListWidget] Added button for cape: " + cape);
            }

            System.out.println("[CapeListWidget] Total buttons created: " + capeButtons.size());
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            // Draw background
            guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xDD000000);

            // Debug: Always draw something to confirm this is rendering
            Minecraft mc = Minecraft.getInstance();
            guiGraphics.drawString(mc.font, "Cape List (" + capeButtons.size() + ")", this.getX() + 5, this.getY() + 5, 0xFFFFFF);

            // Draw cape buttons
            int y = this.getY() + CAPE_LIST_PADDING + 15 - scrollOffset; // +15 to account for debug text
            for (CapeButton button : capeButtons) {
                if (y + itemHeight > this.getY() && y < this.getY() + this.height) {
                    button.setPosition(this.getX() + CAPE_LIST_PADDING, y);
                    button.render(guiGraphics, mouseX, mouseY, partialTick);
                }
                y += itemHeight;
            }

            // Debug: show message if no capes
            if (capeButtons.isEmpty()) {
                guiGraphics.drawString(mc.font, "No capes", this.getX() + 10, this.getY() + 25, 0xFF5555);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (!this.isMouseOver(mouseX, mouseY)) {
                return false;
            }

            int y = this.getY() + CAPE_LIST_PADDING - scrollOffset;
            for (CapeButton capeBtn : capeButtons) {
                if (mouseY >= y && mouseY < y + itemHeight) {
                    selectedCape = capeBtn.capeId;
                    updateButtonStates();
                    return true;
                }
                y += itemHeight;
            }

            return false;
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
            if (this.isMouseOver(mouseX, mouseY)) {
                scrollOffset = Math.max(0, Math.min(scrollOffset - (int)(scrollY * 10),
                        Math.max(0, capeButtons.size() * itemHeight - this.height + CAPE_LIST_PADDING * 2)));
                return true;
            }
            return false;
        }

        @Override
        protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput output) {
            // Narration for accessibility
        }
    }

    // Inner class for individual cape button
    private class CapeButton {
        private final String capeId;
        private int x, y;

        public CapeButton(String capeId) {
            this.capeId = capeId;
        }

        public void setPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            boolean isSelected = capeId.equals(selectedCape);
            boolean isEquipped = capeId.equals(currentEquippedCape);
            boolean isHovered = mouseX >= x && mouseX < x + CAPE_BUTTON_WIDTH &&
                    mouseY >= y && mouseY < y + CAPE_BUTTON_HEIGHT;

            // Background color
            int color = isSelected ? 0xAA4444FF : (isHovered ? 0xAA888888 : 0xAA333333);
            guiGraphics.fill(x, y, x + CAPE_BUTTON_WIDTH, y + CAPE_BUTTON_HEIGHT, color);

            // Border for equipped cape
            if (isEquipped) {
                guiGraphics.fill(x, y, x + CAPE_BUTTON_WIDTH, y + 1, 0xFF00FF00);
                guiGraphics.fill(x, y + CAPE_BUTTON_HEIGHT - 1, x + CAPE_BUTTON_WIDTH, y + CAPE_BUTTON_HEIGHT, 0xFF00FF00);
                guiGraphics.fill(x, y, x + 1, y + CAPE_BUTTON_HEIGHT, 0xFF00FF00);
                guiGraphics.fill(x + CAPE_BUTTON_WIDTH - 1, y, x + CAPE_BUTTON_WIDTH, y + CAPE_BUTTON_HEIGHT, 0xFF00FF00);
            }

            // Cape name
            String displayName = capeId.length() > 12 ? capeId.substring(0, 12) + "..." : capeId;
            guiGraphics.drawString(font, displayName, x + 5, y + 6, 0xFFFFFF);
        }
    }
}