package com.karton.fancygui.client.gui;

import com.karton.fancygui.gui.menu.FancyMenu;
import com.karton.fancygui.network.ElementIds;
import com.karton.fancygui.network.ScreenActionType;
import com.karton.fancygui.network.ScreenElementData;
import com.karton.fancygui.network.ScreenElementType;
import com.karton.fancygui.network.ScreenStatePayload;
import com.karton.fancygui.network.ScreenType;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Native/modded FancyGUI slot screen.
 *
 * Важное отличие от ChestMenu: FancyMenu содержит только те top-level Slot,
 * которые были реально объявлены сервером через NumberRange[].
 *
 * При этом мышь/drag/double-click/hotbar/shift-click обрабатывает ванильный
 * AbstractContainerScreen + AbstractContainerMenu. Мы заменяем только transport:
 * вместо vanilla container packet отправляем ScreenActionPayload.
 */
public class SlotScreen
        extends AbstractContainerScreen<FancyMenu>
        implements FancyScreen {

    protected static final int GUI_WIDTH = 176;
    protected static final int SLOT_SIZE = 18;
    protected static final int SLOT_START_X = 8;

    private static final Identifier GENERIC_54_TEXTURE =
            Identifier.withDefaultNamespace(
                    "textures/gui/container/generic_54.png"
            );

    private static final Identifier SLOT_SPRITE =
            Identifier.withDefaultNamespace(
                    "container/slot"
            );

    private static final Identifier SLOT_HIGHLIGHT_FRONT_SPRITE =
            Identifier.withDefaultNamespace(
                    "container/slot_highlight_front"
            );

    private final int screenId;
    protected final int rows;
    protected final int slotStartY;

    protected Component displayTitle;

    /** Buttons являются widgets, а НЕ Slot. */
    private final Map<Integer, ScreenElementData> buttons =
            new LinkedHashMap<>();

    private final Map<Integer, FancyButtonWidget> buttonWidgets =
            new LinkedHashMap<>();

    private boolean closingFromServer;

    public SlotScreen(
            Inventory inventory,
            ScreenStatePayload state
    ) {
        this(
                inventory,
                state,
                slotStartYFor(state)
        );
    }

    private SlotScreen(
            Inventory inventory,
            ScreenStatePayload state,
            int slotStartY
    ) {
        super(
                FancyMenu.createClient(
                        state.screenId(),
                        inventory,
                        state.rows(),
                        slotStartY,
                        collectInitialGuiStacks(state)
                ),
                inventory,
                Component.literal(state.title()),
                GUI_WIDTH,
                calculateImageHeight(
                        state.rows(),
                        slotStartY
                )
        );

        this.screenId = state.screenId();
        this.rows = state.rows();
        this.slotStartY = slotStartY;
        this.displayTitle = Component.literal(state.title());

        applyState(state);
    }

    private static int slotStartYFor(
            ScreenStatePayload state
    ) {
        return state.screenType() == ScreenType.TEXT_INPUT
                ? 46
                : 18;
    }

    private static int calculateImageHeight(
            int rows,
            int slotStartY
    ) {
        return 114
                + rows * SLOT_SIZE
                + (slotStartY - 18);
    }

    private static Map<Integer, ItemStack> collectInitialGuiStacks(
            ScreenStatePayload state
    ) {
        Map<Integer, ItemStack> result =
                new LinkedHashMap<>();

        for (ScreenElementData element : state.elements()) {
            if (element.type() != ScreenElementType.SLOT) {
                continue;
            }

            result.put(
                    element.gridIndex(),
                    element.item().copy()
            );
        }

        return result;
    }

    @Override
    public int getScreenId() {
        return screenId;
    }

    @Override
    public void applyState(
            ScreenStatePayload state
    ) {
        if (state.screenId() != screenId) {
            return;
        }

        this.displayTitle = Component.literal(state.title());

        /*
         * Sparse layout фиксирован на время жизни screen.
         * State меняет только ItemStack уже существующих Slot.
         */
        for (int gridIndex : menu.getGuiGridIndices()) {
            menu.setGuiStack(
                    gridIndex,
                    ItemStack.EMPTY
            );
        }

        buttons.clear();

        ItemStack carried = ItemStack.EMPTY;

        for (ScreenElementData element : state.elements()) {
            if (element.type() == ScreenElementType.SLOT) {
                if (menu.hasGuiGridIndex(element.gridIndex())) {
                    menu.setGuiStack(
                            element.gridIndex(),
                            element.item()
                    );
                }

                continue;
            }

            if (element.type() == ScreenElementType.BUTTON) {
                buttons.put(element.id(), element);
                continue;
            }

            if (element.type() == ScreenElementType.CARRIED_STACK) {
                carried = element.item().copy();
            }
        }

        menu.setCarried(carried);

        if (width > 0 && height > 0) {
            rebuildButtonWidgets();
        }
    }

    @Override
    protected void init() {
        super.init();
        rebuildButtonWidgets();
    }

    protected final int getFancySlotStartY() {
        return slotStartY;
    }

    protected final int getGridScreenX(
            int gridIndex
    ) {
        return leftPos
                + SLOT_START_X
                + (gridIndex % 9) * SLOT_SIZE;
    }

    protected final int getGridScreenY(
            int gridIndex
    ) {
        return topPos
                + slotStartY
                + (gridIndex / 9) * SLOT_SIZE;
    }

    // ============================================================
    // VANILLA BACKGROUND
    // ============================================================

    @Override
    public void extractBackground(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        super.extractBackground(
                graphics,
                mouseX,
                mouseY,
                delta
        );

        int lowerSectionY =
                slotStartY - 1 + rows * SLOT_SIZE;

        if (slotStartY == 18) {
            int upperHeight =
                    rows * SLOT_SIZE + 17;

            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    GENERIC_54_TEXTURE,
                    leftPos,
                    topPos,
                    0.0F,
                    0.0F,
                    GUI_WIDTH,
                    upperHeight,
                    256,
                    256
            );

            /*
             * generic_54 рисует полную сетку.
             * Удаляем только ячейки, для которых нет ни Slot, ни Button.
             */
            for (int gridIndex = 0; gridIndex < rows * 9; gridIndex++) {
                if (hasVisibleGridElement(gridIndex)) {
                    continue;
                }

                int x = getGridScreenX(gridIndex) - 1;
                int y = getGridScreenY(gridIndex) - 1;

                graphics.fill(
                        x,
                        y,
                        x + SLOT_SIZE,
                        y + SLOT_SIZE,
                        0xFFC6C6C6
                );
            }
        } else {
            /*
             * TextInputScreen: сохраняем vanilla header/background,
             * но между title и grid есть дополнительная область под EditBox.
             */
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    GENERIC_54_TEXTURE,
                    leftPos,
                    topPos,
                    0.0F,
                    0.0F,
                    GUI_WIDTH,
                    17,
                    256,
                    256
            );

            /*
             * Не заливаем всю ширину панели: это стирало 7px vanilla border
             * слева/справа и визуально делало TextInput-секцию шире остальных.
             * Центральную часть заполняем фоном, а края повторяем из
             * generic_54.png.
             */
            renderExtendedVanillaMiddle(
                    graphics,
                    topPos + 17,
                    topPos + lowerSectionY
            );

            /* Sparse top slots/button cells рисуем individually. */
            for (int gridIndex : menu.getGuiGridIndices()) {
                renderGridSlotBackground(
                        graphics,
                        gridIndex
                );
            }

            for (ScreenElementData button : buttons.values()) {
                if (
                        button.width() == SLOT_SIZE
                                && button.height() == SLOT_SIZE
                ) {
                    renderGridSlotBackground(
                            graphics,
                            button.gridIndex()
                    );
                }
            }
        }

        /* Vanilla player inventory section. */
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                GENERIC_54_TEXTURE,
                leftPos,
                topPos + lowerSectionY,
                0.0F,
                126.0F,
                GUI_WIDTH,
                96,
                256,
                256
        );
    }

    private void renderExtendedVanillaMiddle(
            GuiGraphicsExtractor graphics,
            int fromY,
            int toY
    ) {
        final int borderWidth = 7;
        final int sourceY = 17;
        final int tileHeight = 18;

        /* Vanilla interior. */
        graphics.fill(
                leftPos + borderWidth,
                fromY,
                leftPos + GUI_WIDTH - borderWidth,
                toY,
                0xFFC6C6C6
        );

        /*
         * Вертикальные края берём непосредственно из generic_54.png.
         * Так сохраняется ровно та же рамка, что у нижнего inventory section.
         */
        for (int y = fromY; y < toY; y += tileHeight) {
            int height = Math.min(tileHeight, toY - y);

            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    GENERIC_54_TEXTURE,
                    leftPos,
                    y,
                    0.0F,
                    (float) sourceY,
                    borderWidth,
                    height,
                    256,
                    256
            );

            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    GENERIC_54_TEXTURE,
                    leftPos + GUI_WIDTH - borderWidth,
                    y,
                    (float) (GUI_WIDTH - borderWidth),
                    (float) sourceY,
                    borderWidth,
                    height,
                    256,
                    256
            );
        }
    }

    private void renderGridSlotBackground(
            GuiGraphicsExtractor graphics,
            int gridIndex
    ) {
        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                SLOT_SPRITE,
                getGridScreenX(gridIndex) - 1,
                getGridScreenY(gridIndex) - 1,
                SLOT_SIZE,
                SLOT_SIZE
        );
    }

    private boolean hasVisibleGridElement(
            int gridIndex
    ) {
        if (menu.hasGuiGridIndex(gridIndex)) {
            return true;
        }

        for (ScreenElementData button : buttons.values()) {
            if (button.gridIndex() == gridIndex) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void extractLabels(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY
    ) {
        graphics.text(
                font,
                displayTitle,
                8,
                6,
                0xFF404040,
                false
        );

        graphics.text(
                font,
                playerInventoryTitle,
                8,
                imageHeight - 94,
                0xFF404040,
                false
        );
    }

    // ============================================================
    // BUTTONS
    // ============================================================

    private void rebuildButtonWidgets() {
        for (FancyButtonWidget widget : buttonWidgets.values()) {
            removeWidget(widget);
        }

        buttonWidgets.clear();

        for (ScreenElementData button : buttons.values()) {
            int x = getGridScreenX(button.gridIndex()) - 1;
            int y = getGridScreenY(button.gridIndex()) - 1;

            FancyButtonWidget widget =
                    new FancyButtonWidget(
                            button,
                            x,
                            y
                    );

            buttonWidgets.put(
                    button.id(),
                    widget
            );

            addRenderableWidget(widget);
        }
    }

    // ============================================================
    // VANILLA CONTAINER INPUT -> FANCYGUI NETWORK
    // ============================================================

    @Override
    protected void slotClicked(
            Slot slot,
            int slotId,
            int buttonNum,
            ContainerInput containerInput
    ) {
        if (minecraft == null || minecraft.player == null) {
            return;
        }

        /*
         * Client-side vanilla prediction.
         * Это тот же AbstractContainerMenu алгоритм, что используется vanilla.
         */
        menu.clicked(
                slotId,
                buttonNum,
                containerInput,
                minecraft.player
        );

        /*
         * НЕ вызываем super.slotClicked(): он пошлёт обычный vanilla
         * container packet, а FancyMenu не является зарегистрированным MenuType.
         */
        sendAction(
                slotId,
                ScreenActionType.SLOT_CLICK,
                buttonNum
                        + ":"
                        + containerInput.name()
        );
    }

    // ============================================================
    // CLOSE / LIFECYCLE
    // ============================================================

    @Override
    public void onClose() {
        if (!closingFromServer) {
            sendAction(
                    -1,
                    ScreenActionType.CLOSE
            );
        }

        /*
         * Не вызываем AbstractContainerScreen#onClose(), потому что тот закрывает
         * vanilla container. Наш server-side menu живёт в FancyScreenSession.
         */
        if (minecraft != null) {
            minecraft.setScreenAndShow(null);
        }
    }

    @Override
    public void closeFromServer() {
        closingFromServer = true;

        if (minecraft != null) {
            minecraft.setScreenAndShow(null);
        }
    }

    @Override
    public void removed() {
        /*
         * Server authoritative state вернёт carried stack игроку при закрытии.
         * Здесь просто очищаем локальный prediction и не вызываем vanilla
         * container teardown.
         */
        menu.setCarried(ItemStack.EMPTY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ============================================================

    private class FancyButtonWidget extends AbstractButton {

        private final ScreenElementData data;

        FancyButtonWidget(
                ScreenElementData data,
                int x,
                int y
        ) {
            super(
                    x,
                    y,
                    Math.max(1, data.width()),
                    Math.max(1, data.height()),
                    Component.literal(data.text())
            );

            this.data = data;
        }

        @Override
        public void onPress(
                InputWithModifiers input
        ) {
            sendAction(
                    data.id(),
                    ScreenActionType.BUTTON_CLICK
            );
        }

        @Override
        protected void extractContents(
                GuiGraphicsExtractor graphics,
                int mouseX,
                int mouseY,
                float delta
        ) {
            boolean slotSized =
                    getWidth() == SLOT_SIZE
                            && getHeight() == SLOT_SIZE;

            /*
             * У 18x18 button фон уже нарисован container background.
             * Для произвольных размеров используем vanilla button sprite.
             */
            if (!slotSized) {
                extractDefaultSprite(graphics);
            }

            ItemStack icon = data.item();

            if (!icon.isEmpty()) {
                int iconX =
                        getX() + (getWidth() - 16) / 2;

                int iconY =
                        getY() + (getHeight() - 16) / 2;

                graphics.fakeItem(
                        icon,
                        iconX,
                        iconY
                );

                graphics.itemDecorations(
                        font,
                        icon,
                        iconX,
                        iconY
                );
            }

            if (isHovered()) {
                if (slotSized) {
                    graphics.blitSprite(
                            RenderPipelines.GUI_TEXTURED,
                            SLOT_HIGHLIGHT_FRONT_SPRITE,
                            getX() - 3,
                            getY() - 3,
                            24,
                            24
                    );
                }

                List<Component> tooltip =
                        new ArrayList<>();

                if (!data.text().isEmpty()) {
                    tooltip.add(
                            Component.literal(data.text())
                    );
                }

                if (!data.secondaryText().isEmpty()) {
                    tooltip.add(
                            Component.literal(
                                    data.secondaryText()
                            )
                    );
                }

                if (!tooltip.isEmpty()) {
                    graphics.setComponentTooltipForNextFrame(
                            font,
                            tooltip,
                            mouseX,
                            mouseY
                    );
                }
            }
        }

        @Override
        protected void updateWidgetNarration(
                NarrationElementOutput output
        ) {
            defaultButtonNarrationText(output);
        }
    }
}
