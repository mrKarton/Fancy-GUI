package com.karton.fancygui.gui.server;

import com.karton.fancygui.FancyGUI;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;

public class font {
    public static final FontDescription FIRST_ROW_FONT = new FontDescription.Resource(
            FancyGUI.id("row_text/first")
    );

    public static final Style FIRST_ROW_STYLE = Style.EMPTY.withFont(FIRST_ROW_FONT);
}
