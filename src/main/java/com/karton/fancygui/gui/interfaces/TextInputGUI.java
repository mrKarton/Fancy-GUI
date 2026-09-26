package com.karton.fancygui.gui.interfaces;

public interface TextInputGUI extends SlotScreenGUI {
    void setOnInputCallback(Runnable callback);
    String getInput();

    void setSubmitCallback(Runnable func);
    void setCancelCallback(Runnable func);
}
