package dev.satherov.utilityvest.client.screen;

import dev.satherov.utilityvest.client.input.UVKeybindManager;
import dev.satherov.utilityvest.common.capabilities.UVVestCapability;
import dev.satherov.utilityvest.common.item.UVVestItem;
import dev.satherov.utilityvest.common.item.UVVestReference;
import dev.satherov.utilityvest.config.UVConfig;
import dev.satherov.utilityvest.core.lang.UVLanguage;
import dev.satherov.utilityvest.network.UVNetworking;

import net.neoforged.neoforge.capabilities.Capabilities;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RadialMenuScreen extends Screen {
    
    private static final int INNER_RADIUS = 20;
    private static final int OUTER_RADIUS = 80;
    private static final int HOVER_EXTEND = 20;
    private static final int CENTER_DEAD_ZONE = 20;
    
    private final List<RadialMenuItem> menuItems = new ArrayList<>();
    private final int banks;
    private final ItemStack vest;
    private final UVVestReference vestReference;
    
    private int hoveredIndex = -1;
    private int row = 0;
    
    public RadialMenuScreen(ItemStack vest, UVVestReference vestReference, int banks, int lastRowOpen) {
        super(Component.literal("Radial Menu"));
        this.vest = vest;
        this.vestReference = vestReference;
        this.banks = banks;
        this.row = lastRowOpen;
        this.updateDisplay();
    }
    
    public void addMenuItem(ItemStack stack, Consumer<Boolean> action) {
        this.menuItems.add(new RadialMenuItem(stack, action));
    }
    
    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        this.drawRadialOverlay(graphics, centerX, centerY);
        
        super.render(graphics, mouseX, mouseY, partialTick);
        
        this.hoveredIndex = this.getHoveredSection(mouseX, mouseY, centerX, centerY);
        
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        
        final ItemStack barrier = Items.BARRIER.getDefaultInstance();
        final int xPos = centerX - 8;
        final int yPos = centerY - 8;
        graphics.renderItem(barrier, xPos, yPos);
        
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 200.0F);
        final String text = Integer.toString(this.row + 1);
        graphics.drawString(this.font, text, xPos + 17 - this.font.width(text), yPos + 9, 16777215, true);
        graphics.pose().popPose();
        
        if (this.hoveredIndex < 0) {
            Player player = Minecraft.getInstance().player;
            UVConfig.ToolTipDisplay display = UVConfig.RadialTooltip;
            boolean tooltip = player != null;
            if (display.equals(UVConfig.ToolTipDisplay.NEVER)) tooltip = false;
            if (display.equals(UVConfig.ToolTipDisplay.SHIFT) && !Screen.hasShiftDown()) tooltip = false;
            
            if (tooltip) {
                List<Component> lines = new ArrayList<>();
                lines.add(UVLanguage.TOOLTIP_MENU_INSERT.translate(
                        ComponentUtils.wrapInSquareBrackets(InputConstants.Type.MOUSE.getOrCreate(GLFW.GLFW_MOUSE_BUTTON_LEFT).getDisplayName().copy().withStyle(ChatFormatting.GOLD)),
                        player.getMainHandItem().getHoverName()
                ).withStyle(ChatFormatting.DARK_GRAY));
                lines.add(UVLanguage.TOOLTIP_MENU_INSERT.translate(
                        ComponentUtils.wrapInSquareBrackets(InputConstants.Type.MOUSE.getOrCreate(GLFW.GLFW_MOUSE_BUTTON_RIGHT).getDisplayName().copy().withStyle(ChatFormatting.GOLD)),
                        player.getOffhandItem().getHoverName()
                ).withStyle(ChatFormatting.DARK_GRAY));
                lines.add(UVLanguage.TOOLTIP_MENU_CYCLE.translate(
                        ComponentUtils.wrapInSquareBrackets(UVLanguage.INPUT_WHEEL_UP.translate().withStyle(ChatFormatting.GOLD)),
                        ComponentUtils.wrapInSquareBrackets(UVLanguage.INPUT_WHEEL_DOWN.translate().withStyle(ChatFormatting.GOLD))
                ).withStyle(ChatFormatting.DARK_GRAY));
                
                graphics.renderComponentTooltip(
                        this.font,
                        lines,
                        mouseX,
                        mouseY
                );
            }
        }
        
        for (int i = 0; i < this.menuItems.size(); i++) {
            boolean isHovered = i == this.hoveredIndex;
            this.renderSection(graphics, mouseX, mouseY, centerX, centerY, i, this.menuItems.size(), isHovered, this.menuItems.get(i).stack());
        }
        
        RenderSystem.disableBlend();
    }
    
    private void drawRadialOverlay(GuiGraphics graphics, int centerX, int centerY) {
        Matrix4f matrix = graphics.pose().last().pose();
        
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        
        buffer.addVertex(matrix, centerX, centerY, 0).setColor(0.0f, 0.0f, 0.0f, 0.0f);
        
        final int segments = 64;
        float radius = RadialMenuScreen.OUTER_RADIUS + RadialMenuScreen.HOVER_EXTEND;
        
        for (int i = 0; i <= segments; i++) {
            float angle = (float) (2.0 * Math.PI * i / segments);
            float x = centerX + (float) Math.cos(angle) * radius;
            float y = centerY + (float) Math.sin(angle) * radius;
            
            buffer.addVertex(matrix, x, y, 0).setColor(0.0f, 0.0f, 0.0f, 0.6f);
        }
        
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.disableBlend();
    }
    
    private void renderSection(GuiGraphics graphics, int mouseX, int mouseY, int centerX, int centerY, int index, int totalSections, boolean isHovered, ItemStack stack) {
        float anglePerSection = 360.0f / totalSections;
        float start = anglePerSection * index - 90;
        
        float midRad = (float) Math.toRadians(start + anglePerSection / 2);
        
        this.drawLines(graphics, centerX, centerY, start);
        
        int outerRadius = isHovered ? RadialMenuScreen.OUTER_RADIUS + RadialMenuScreen.HOVER_EXTEND : RadialMenuScreen.OUTER_RADIUS;
        
        float labelRadius = RadialMenuScreen.INNER_RADIUS + (outerRadius - RadialMenuScreen.INNER_RADIUS) * 0.6f;
        
        int x = (int) (centerX + (Math.cos(midRad) * labelRadius)) - 8;
        int y = (int) (centerY + (Math.sin(midRad) * labelRadius)) - 8;
        
        if (!stack.isEmpty()) {
            graphics.renderItem(stack, x, y);
            
            UVConfig.RadialStackCountDisplay display = UVConfig.RadialStackCount;
            
            if (display.equals(UVConfig.RadialStackCountDisplay.NEVER) && stack.getCount() == 1) {
                return;
            }
            
            if (display.equals(UVConfig.RadialStackCountDisplay.TOOLS_ONLY)
                    && (stack.has(DataComponents.TOOL) || stack.getItem() instanceof ArmorItem)) {
                return;
            }
            
            graphics.renderItemDecorations(this.font, stack, x, y, String.valueOf(stack.getCount()));
            
        }
        
        if (!isHovered) return;
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        
        UVConfig.ToolTipDisplay display = UVConfig.RadialTooltip;
        if (display.equals(UVConfig.ToolTipDisplay.NEVER)) return;
        if (display.equals(UVConfig.ToolTipDisplay.SHIFT) && !Screen.hasShiftDown()) return;
        
        List<Component> lines = new ArrayList<>();
        lines.add(UVLanguage.TOOLTIP_MENU_SWAP.translate(
                ComponentUtils.wrapInSquareBrackets(InputConstants.Type.MOUSE.getOrCreate(GLFW.GLFW_MOUSE_BUTTON_LEFT).getDisplayName().copy().withStyle(ChatFormatting.GOLD)),
                player.getMainHandItem().getHoverName()
        ).withStyle(ChatFormatting.DARK_GRAY));
        lines.add(UVLanguage.TOOLTIP_MENU_SWAP.translate(
                ComponentUtils.wrapInSquareBrackets(InputConstants.Type.MOUSE.getOrCreate(GLFW.GLFW_MOUSE_BUTTON_RIGHT).getDisplayName().copy().withStyle(ChatFormatting.GOLD)),
                player.getOffhandItem().getHoverName()
        ).withStyle(ChatFormatting.DARK_GRAY));
        lines.add(UVLanguage.TOOLTIP_MENU_CYCLE.translate(
                ComponentUtils.wrapInSquareBrackets(UVLanguage.INPUT_WHEEL_UP.translate().withStyle(ChatFormatting.GOLD)),
                ComponentUtils.wrapInSquareBrackets(UVLanguage.INPUT_WHEEL_DOWN.translate().withStyle(ChatFormatting.GOLD))
        ).withStyle(ChatFormatting.DARK_GRAY));
        
        graphics.renderComponentTooltip(
                this.font,
                lines,
                mouseX,
                mouseY
        );
    }
    
    private void drawLines(GuiGraphics graphics, float centerX, float centerY, float angle) {
        Matrix4f matrix = graphics.pose().last().pose();
        
        int argb = 0xFF878787;
        
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        
        float rad = (float) Math.toRadians(angle);
        float x1 = centerX + (float) (Math.cos(rad) * RadialMenuScreen.INNER_RADIUS);
        float y1 = centerY + (float) (Math.sin(rad) * RadialMenuScreen.INNER_RADIUS);
        float x2 = centerX + (float) (Math.cos(rad) * RadialMenuScreen.OUTER_RADIUS);
        float y2 = centerY + (float) (Math.sin(rad) * RadialMenuScreen.OUTER_RADIUS);
        
        buffer.addVertex(matrix, x1, y1, 0).setColor(argb);
        buffer.addVertex(matrix, x2, y2, 0).setColor(argb);
        
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth(2.5f);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.lineWidth(1.0f);
    }
    
    private int getHoveredSection(int mouseX, int mouseY, int centerX, int centerY) {
        float dx = mouseX - centerX;
        float dy = mouseY - centerY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        
        if (distance < RadialMenuScreen.CENTER_DEAD_ZONE || distance > RadialMenuScreen.OUTER_RADIUS + RadialMenuScreen.HOVER_EXTEND) {
            return -1;
        }
        
        if (this.menuItems.isEmpty()) {
            return -1;
        }
        
        float angle = (float) Math.toDegrees(Math.atan2(dy, dx)) + 90;
        if (angle < 0) angle += 360;
        
        float anglePerSection = 360.0f / this.menuItems.size();
        int section = (int) (angle / anglePerSection);
        
        return section % this.menuItems.size();
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.hoveredIndex < 0) {
            UVNetworking.doSwap(this.minecraft.player, button == 0, this.vestReference, this.vest, -1);
            this.updateDisplay();
            return true;
        }
        
        if (this.hoveredIndex > this.menuItems.size()) return super.mouseClicked(mouseX, mouseY, button);
        
        if (button == 0 || button == 1) {
            RadialMenuItem item = this.menuItems.get(this.hoveredIndex);
            item.action().accept(button == 0);
            this.updateDisplay();
            return true;
        }
        
        return true;
    }
    
    @Override
    public boolean mouseScrolled(double mx, double my, double dx, double dy) {
        int delta = (int) Math.signum(dy) * (UVConfig.InvertRadialScroll ? 1 : -1);
        this.row = Math.floorMod(this.row + delta, this.banks);
        this.updateDisplay();
        return true;
    }
    
    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (UVKeybindManager.RADIAL_KEY.matches(keyCode, scanCode)) {
            if (UVConfig.RememberRadialRow && this.vest.getItem() instanceof UVVestItem vestItem) {
                vestItem.setLastOpenRow(this.vest, this.row);
            }
            
            this.onClose();
        }
        return true;
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    private void updateDisplay() {
        this.menuItems.clear();
        
        var handler = this.vest.getCapability(Capabilities.ItemHandler.ITEM);
        if (!(handler instanceof UVVestCapability capability)) return;
        
        NonNullList<ItemStack> stacks = capability.getStorage();
        final int startIndex = this.row * 9;
        
        for (int i = 0; i < 9; i++) {
            if (startIndex + i < stacks.size()) {
                final int idx = startIndex + i;
                ItemStack stack = stacks.get(idx);
                this.addMenuItem(stack, dir -> {
                    UVNetworking.doSwap(this.minecraft.player, dir, this.vestReference, this.vest, idx);
                    this.updateDisplay();
                });
            }
        }
    }
    
    private record RadialMenuItem(ItemStack stack, Consumer<Boolean> action) { }
}
