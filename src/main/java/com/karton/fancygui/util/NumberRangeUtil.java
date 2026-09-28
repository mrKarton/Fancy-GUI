package com.karton.fancygui.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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
