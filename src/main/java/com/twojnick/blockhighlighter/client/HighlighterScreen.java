package com.twojnick.blockhighlighter.client;

import com.twojnick.blockhighlighter.BlockHighlighter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class HighlighterScreen extends Screen {
    private EditBox searchBox;
    private BlockList blockList;
    private int renderDistance = 15;

    public HighlighterScreen() {
        super(Component.literal("Block Highlighter"));
    }

    @Override
    protected void init() {
        super.init();

        int rightPanelWidth = 120;
        int rightPanelX = this.width - rightPanelWidth - 10;
        int listWidth = this.width - rightPanelWidth - 30;

        this.searchBox = new EditBox(this.font, 10, 10, listWidth, 20, Component.literal("Search"));
        this.searchBox.setResponder(this::updateSearch);
        this.addRenderableWidget(this.searchBox);

        this.blockList = new BlockList(this.minecraft, listWidth, this.height - 50, 40, 36);
        this.blockList.setX(10);
        this.addRenderableWidget(this.blockList);

        int btnY = 40;

        this.addRenderableWidget(Button.builder(Component.literal("Add Block"), b -> {})
                .bounds(rightPanelX, btnY, rightPanelWidth, 20).build());
        btnY += 24;

        this.addRenderableWidget(Button.builder(Component.literal("Add Block in hand"), b -> {
            if (this.minecraft.player != null) {
                ItemStack handItem = this.minecraft.player.getMainHandItem();
                Block block = Block.byItem(handItem.getItem());
                if (block != Blocks.AIR) {
                    BlockHighlighter.SELECTED_BLOCKS.put(block, true);
                    updateSearch(this.searchBox.getValue());
                }
            }
        }).bounds(rightPanelX, btnY, rightPanelWidth, 20).build());
        btnY += 24;

        this.addRenderableWidget(Button.builder(Component.literal("Add Looking at"), b -> {})
                .bounds(rightPanelX, btnY, rightPanelWidth, 20).build());
        btnY += 24;

        this.addRenderableWidget(Button.builder(Component.literal("Show Lava: true"), b -> {})
                .bounds(rightPanelX, btnY, rightPanelWidth, 20).build());
        btnY += 34;

        this.addRenderableWidget(Button.builder(Component.literal("Distance: " + renderDistance), b -> {
            renderDistance = (renderDistance == 15) ? 30 : 15;
            b.setMessage(Component.literal("Distance: " + renderDistance));
        }).bounds(rightPanelX, btnY, rightPanelWidth, 20).build());
        btnY += 24;

        this.addRenderableWidget(Button.builder(Component.literal("Help"), b -> {})
                .bounds(rightPanelX, btnY, rightPanelWidth / 2 - 2, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Close"), b -> this.onClose())
                .bounds(rightPanelX + rightPanelWidth / 2 + 2, btnY, rightPanelWidth / 2 - 2, 20).build());

        updateSearch("");
    }

    private void updateSearch(String query) {
        this.blockList.clearAll();
        String q = query == null ? "" : query.toLowerCase();
        for (Block block : BuiltInRegistries.BLOCK) {
            String blockName = BuiltInRegistries.BLOCK.getKey(block).getPath();
            if (blockName.contains(q)) {
                this.blockList.addBlockEntry(new BlockListEntry(block));
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Avoid the vanilla blurred background here because some client mod stacks
        // already apply blur once per frame (causing "Can only blur once per frame").
        guiGraphics.fill(0, 0, this.width, this.height, 0xAA000000);

        int rightPanelX = this.width - 135;
        guiGraphics.fill(rightPanelX, 10, this.width - 5, this.height - 10, 0x88000000);
        guiGraphics.drawString(this.font, "Tools", rightPanelX + 10, 20, 0xFFFF00, false);

        guiGraphics.drawString(this.font, "Click to enable / disable", 10, this.height - 30, 0xAAAAAA, false);
        guiGraphics.drawString(this.font, "Hold shift and click to edit.", 10, this.height - 18, 0xAAAAAA, false);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    class BlockList extends ObjectSelectionList<BlockListEntry> {
        public BlockList(Minecraft minecraft, int width, int height, int y0, int itemHeight) {
            super(minecraft, width, height, y0, itemHeight);
        }

        public void clearAll() {
            super.clearEntries();
        }

        public void addBlockEntry(BlockListEntry entry) {
            super.addEntry(entry);
        }

        @Override
        public int getRowWidth() {
            return this.width - 20;
        }
    }

    class BlockListEntry extends ObjectSelectionList.Entry<BlockListEntry> {
        private final Block block;

        public BlockListEntry(Block block) {
            this.block = block;
        }

        @Override
        public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean isHovering, float partialTick) {
            int left = this.getContentX();
            int top = this.getContentY();
            int width = this.getContentWidth();

            ItemStack stack = new ItemStack(block.asItem());
            guiGraphics.renderItem(stack, left + 5, top + 10);

            String name = BuiltInRegistries.BLOCK.getKey(block).getPath().replace("_", " ");
            if (!name.isEmpty()) {
                name = name.substring(0, 1).toUpperCase() + name.substring(1);
            }
            guiGraphics.drawString(Minecraft.getInstance().font, name, left + 30, top + 5, 0xFFFFFF, false);

            boolean isEnabled = BlockHighlighter.SELECTED_BLOCKS.getOrDefault(block, false);
            String status = isEnabled ? "Enabled" : "Disabled";
            int statusColor = isEnabled ? 0x00FF00 : 0xFF0000;
            guiGraphics.drawString(Minecraft.getInstance().font, status, left + 30, top + 18, statusColor, false);

            int circleX = left + width - 30;
            int circleY = top + 10;
            guiGraphics.fill(circleX, circleY, circleX + 16, circleY + 16, 0xFF444444);
            guiGraphics.fill(circleX + 1, circleY + 1, circleX + 15, circleY + 15, isEnabled ? 0xFFFFFF00 : 0xFF555555);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
            boolean current = BlockHighlighter.SELECTED_BLOCKS.getOrDefault(block, false);
            BlockHighlighter.SELECTED_BLOCKS.put(block, !current);
            return true;
        }

        @Override
        public Component getNarration() {
            return Component.literal(BuiltInRegistries.BLOCK.getKey(block).toString());
        }
    }
}

