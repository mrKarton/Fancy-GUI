package com.karton.fancygui.util;

import java.util.ArrayList;

public class NumberRangeUtil {
    public static ArrayList<Integer> getFulRange(NumberRange[] numberRangex) {
        ArrayList<Integer> list = new ArrayList<>();

        for (NumberRange range : numberRangex) {
            for (int num : range.getRangeArray()) {
                list.add(num);
            }
        }

        return list;
    }
}
