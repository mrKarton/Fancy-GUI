package com.karton.fancygui.client.gui;

import com.karton.fancygui.network.ElementIds;
import com.karton.fancygui.network.ListViewClickPayload;
import com.karton.fancygui.network.ListViewContentPayload;
import com.karton.fancygui.network.ScreenActionType;
import com.karton.fancygui.network.ScreenElementData;
import com.karton.fancygui.network.ScreenElementType;
import com.karton.fancygui.network.ScreenStatePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import net.minecraft.client.input.PreeditEvent;

import java.util.ArrayList;
import java.util.List;

public class ListViewScreen extends SlotScreen {
    private static final int COLUMNS = 8;
    private static final int VISIBLE_ROWS = 4;
    private static final int LIST_Y = 36;
    private static final int BAR_X = 155;
    private static final int BAR_Y = LIST_Y;
    private static final int BAR_WIDTH = 12;
    private static final int BAR_HEIGHT = VISIBLE_ROWS * SLOT_SIZE;
    private static final int THUMB_HEIGHT = 15;

    private final List<ItemStack> items = new ArrayList<>();
    private long revision = -1;
    private float scrollOffs;
    private boolean draggingScrollbar;
    private EditBox searchBox;
    private ScreenElementData searchData;
    private boolean applyingServerValue;

    public ListViewScreen(ScreenStatePayload state) {
        super(state);
        readSearchData(state);
    }

    @Override
    public void applyState(ScreenStatePayload state) {
        if (state.screenId() != getScreenId()) return;
        super.applyState(state);
        readSearchData(state);
    }

    private void readSearchData(ScreenStatePayload state) {
        for (ScreenElementData element : state.elements()) {
            if (element.type() == ScreenElementType.TEXT_INPUT) {
                searchData = element;
                break;
            }
        }
        if (searchBox != null && searchData != null) {
            applyingServerValue = true;
            searchBox.setValue(searchData.text());
            applyingServerValue = false;
        }
    }

    public void applyListContents(ListViewContentPayload payload) {
        if (payload.screenId() != getScreenId() || payload.revision() <= revision) return;
        items.clear();
        for (ItemStack item : payload.items()) items.add(item.copy());
        revision = payload.revision();
        if (maxScrollRows() == 0) scrollOffs = 0;
    }

    @Override
    protected void init() {
        super.init();
        if (searchData == null) return;
        searchBox = new EditBox(font, leftPos + searchData.x(),
                topPos + searchData.y(), searchData.width(), searchData.height(),
                Component.literal("Search"));
        applyingServerValue = true;
        searchBox.setValue(searchData.text());
        applyingServerValue = false;
        searchBox.setHint(Component.literal(searchData.secondaryText()));
        searchBox.setResponder(value -> {
            if (!applyingServerValue) {
                scrollOffs = 0;
                sendAction(ElementIds.TEXT_INPUT, ScreenActionType.TEXT_CHANGED, value);
            }
        });
        addRenderableWidget(searchBox);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX,
                                   int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        int first = firstVisibleRow() * COLUMNS;
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                int x = leftPos + SLOT_START_X + col * SLOT_SIZE;
                int y = topPos + LIST_Y + row * SLOT_SIZE;
                int index = first + row * COLUMNS + col;
                renderSlotBackground(graphics, x, y);
                if (index >= items.size()) continue;
                ItemStack stack = items.get(index);
                renderStack(graphics, stack, x, y);
                if (isInside(mouseX, mouseY, x, y, SLOT_SIZE, SLOT_SIZE)) {
                    renderSlotHighlight(graphics, x, y);
                    if (!stack.isEmpty()) graphics.setTooltipForNextFrame(
                            font, stack, mouseX, mouseY);
                }
            }
        }
        int bx = leftPos + BAR_X;
        int by = topPos + BAR_Y;
        graphics.fill(bx, by, bx + BAR_WIDTH, by + BAR_HEIGHT, 0xFF575757);
        int thumbY = by + Math.round(scrollOffs * (BAR_HEIGHT - THUMB_HEIGHT));
        graphics.fill(bx + 1, thumbY, bx + BAR_WIDTH - 1,
                thumbY + THUMB_HEIGHT, maxScrollRows() > 0 ? 0xFFE0E0E0 : 0xFF999999);
    }

    private int maxScrollRows() {
        return Math.max(0, (items.size() + COLUMNS - 1) / COLUMNS - VISIBLE_ROWS);
    }

    private int firstVisibleRow() {
        return Math.round(scrollOffs * maxScrollRows());
    }

    private int itemIndexAt(double x, double y) {
        int dx = (int) Math.floor(x - leftPos - SLOT_START_X);
        int dy = (int) Math.floor(y - topPos - LIST_Y);
        if (dx < 0 || dy < 0 || dx >= COLUMNS * SLOT_SIZE
                || dy >= VISIBLE_ROWS * SLOT_SIZE) return -1;
        int index = (firstVisibleRow() + dy / SLOT_SIZE) * COLUMNS + dx / SLOT_SIZE;
        return index < items.size() ? index : -1;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        {
            int index = itemIndexAt(event.x(), event.y());
            if (index >= 0) {
                if (revision >= 0) ClientPlayNetworking.send(
                        new ListViewClickPayload(getScreenId(), revision, index));
                return true;
            }
            if (isInside(event.x(), event.y(), leftPos + BAR_X, topPos + BAR_Y,
                    BAR_WIDTH, BAR_HEIGHT)) {
                draggingScrollbar = maxScrollRows() > 0;
                scrollFromMouse(event.y());
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (draggingScrollbar) {
            scrollFromMouse(event.y());
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        draggingScrollbar = false;
        return super.mouseReleased(event);
    }

    private void scrollFromMouse(double y) {
        if (maxScrollRows() == 0) return;
        double fraction = (y - topPos - BAR_Y - THUMB_HEIGHT / 2.0)
                / (BAR_HEIGHT - THUMB_HEIGHT);
        scrollOffs = (float) Math.max(0, Math.min(1, fraction));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY,
                                 double horizontalAmount, double verticalAmount) {
        if (maxScrollRows() > 0 && isInside(mouseX, mouseY,
                leftPos + SLOT_START_X, topPos + LIST_Y,
                BAR_X + BAR_WIDTH - SLOT_START_X, BAR_HEIGHT)) {
            scrollOffs = (float) Math.max(0, Math.min(1,
                    scrollOffs - verticalAmount / maxScrollRows()));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (searchBox != null && searchBox.isFocused()) {
            if (event.isEscape()) return super.keyPressed(event);
            searchBox.keyPressed(event);
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (searchBox != null && searchBox.isFocused()) {
            return searchBox.charTyped(event);
        }
        return super.charTyped(event);
    }

    @Override
    public boolean preeditUpdated(@Nullable PreeditEvent event) {
        if (searchBox != null && searchBox.isFocused()) {
            return searchBox.preeditUpdated(event);
        }
        return super.preeditUpdated(event);
    }

    @Override
    public boolean isInputCaptured() {
        return searchBox != null && searchBox.capturesInput();
    }
}
