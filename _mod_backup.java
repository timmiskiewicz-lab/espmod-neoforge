package com.twojnick.blockhighlighter.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.twojnick.blockhighlighter.BlockHighlighter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

public class HighlighterScreen extends Screen {
    private EditBox searchBox;
    private BlockList blockList;
    private int renderDistance = 15; // Domyślny dystans z obrazka

    public HighlighterScreen() {
        super(Component.literal("Block Highlighter"));
    }

    @Override
    protected void init() {
        super.init();
        
        int rightPanelWidth = 120;
        int rightPanelX = this.width - rightPanelWidth - 10;
        int listWidth = this.width - rightPanelWidth - 30;

        // 1. Pasek wyszukiwania (Lewa góra)
        this.searchBox = new EditBox(this.font, 10, 10, listWidth, 20, Component.literal("Search"));
        this.searchBox.setResponder(this::updateSearch);
        this.addRenderableWidget(this.searchBox);

        // 2. Przewijana lista bloków (Lewa strona)
        this.blockList = new BlockList(this.minecraft, listWidth, this.height - 50, 40, 36);
        this.blockList.setX(10);
        this.addRenderableWidget(this.blockList);

        // 3. Prawy panel (Tools)
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
        btnY += 34; // Większy odstęp

        this.addRenderableWidget(Button.builder(Component.literal("Distance: " + renderDistance), b -> {
            renderDistance = (renderDistance == 15) ? 30 : 15; // Proste przełączanie dla testu
            b.setMessage(Component.literal("Distance: " + renderDistance));
        }).bounds(rightPanelX, btnY, rightPanelWidth, 20).build());
        btnY += 24;

        // Dolne przyciski
        this.addRenderableWidget(Button.builder(Component.literal("Help"), b -> {})
                .bounds(rightPanelX, btnY, rightPanelWidth / 2 - 2, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Close"), b -> this.onClose())
                .bounds(rightPanelX + rightPanelWidth / 2 + 2, btnY, rightPanelWidth / 2 - 2, 20).build());

        updateSearch(""); // Wypełnij listę na start
    }

    private void updateSearch(String query) {
        this.blockList.clearEntries();
        for (Block block : BuiltInRegistries.BLOCK) {
            String blockName = BuiltInRegistries.BLOCK.getKey(block).getPath();
            if (blockName.contains(query.toLowerCase())) {
                // Dla optymalizacji pokazujemy np. tylko te wybrane lub pierwsze 100
                this.blockList.addEntry(new BlockListEntry(block, this.blockList));
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        
        // Rysowanie obramowań panelu bocznego i tła
        int rightPanelX = this.width - 135;
        guiGraphics.fill(rightPanelX, 10, this.width - 5, this.height - 10, 0x88000000); // Ciemne tło prawego panelu
        guiGraphics.drawString(this.font, "Tools", rightPanelX + 10, 20, 0xFFFF00, false); // Żółty napis Tools

        // Podpowiedź na dole
        guiGraphics.drawString(this.font, "Click to enable / disable", 10, this.height - 30, 0xAAAAAA, false);
        guiGraphics.drawString(this.font, "Hold shift and click to edit.", 10, this.height - 18, 0xAAAAAA, false);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    // --- KLASY WEWNĘTRZNE DO PRZEWIJANEJ LISTY ---

    class BlockList extends ObjectSelectionList<BlockListEntry> {
        public BlockList(Minecraft minecraft, int width, int height, int y0, int itemHeight) {
            super(minecraft, width, height, y0, itemHeight);
        }

        public void clearEntries() {
            this.clear();
        }

        public void addEntry(BlockListEntry entry) {
            this.addEntry((BlockListEntry) entry); // Rzutowanie wymagane przez niektóre wersje MDK
        }
        
        @Override
        public int getRowWidth() {
            return this.width - 20; // Szerokość wiersza
        }
        
        @Override
        protected int getScrollbarPosition() {
            return this.width - 5;
        }
    }

    class BlockListEntry extends ObjectSelectionList.Entry<BlockListEntry> {
        private final Block block;
        private final BlockList parentList;

        public BlockListEntry(Block block, BlockList parentList) {
            this.block = block;
            this.parentList = parentList;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float partialTick) {
            // 1. Rysowanie ikony bloku (wymaga itemu)
            ItemStack stack = new ItemStack(block.asItem());
            guiGraphics.renderItem(stack, left + 5, top + 10);

            // 2. Rysowanie nazwy bloku
            String name = BuiltInRegistries.BLOCK.getKey(block).getPath();
            // Formatyzacja nazwy (np. deepslate_gold_ore -> Deepslate Gold Ore)
            name = name.replace("_", " ").substring(0, 1).toUpperCase() + name.replace("_", " ").substring(1);
            guiGraphics.drawString(Minecraft.getInstance().font, name, left + 30, top + 5, 0xFFFFFF, false);

            // 3. Status Enabled / Disabled
            boolean isEnabled = BlockHighlighter.SELECTED_BLOCKS.getOrDefault(block, false);
            String status = isEnabled ? "Enabled" : "Disabled";
            int statusColor = isEnabled ? 0x00FF00 : 0xFF0000; // Zielony lub Czerwony
            guiGraphics.drawString(Minecraft.getInstance().font, status, left + 30, top + 18, statusColor, false);

            // 4. Kółko z kolorem (mockup z prawej strony wiersza)
            int circleX = left + width - 30;
            int circleY = top + 10;
            guiGraphics.fill(circleX, circleY, circleX + 16, circleY + 16, 0xFF444444); // Ramka
            guiGraphics.fill(circleX + 1, circleY + 1, circleX + 15, circleY + 15, isEnabled ? 0xFFFFFF00 : 0xFF555555); // Środek
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            // Przełączanie po kliknięciu w wiersz
            boolean current = BlockHighlighter.SELECTED_BLOCKS.getOrDefault(block, false);
            BlockHighlighter.SELECTED_BLOCKS.put(block, !current);
            return true;
        }
    }
}