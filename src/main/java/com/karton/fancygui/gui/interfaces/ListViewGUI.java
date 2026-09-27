package com.karton.fancygui.gui.interfaces;

import net.minecraft.world.item.ItemStack;
import java.util.List;

public interface ListViewGUI extends SlotScreenGUI {
    void setDisplayItems(List<ItemStack> items);
    void setItemClickCallback(ListViewItemClickCallback callback);
    void setSearchCallback(Runnable callback);
    String getSearchInput();
}
