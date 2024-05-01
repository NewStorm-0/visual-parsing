package com.chaldea.visualparsing;

public class ArrayHelper {
    public static <T> int findIndex(T[] array, T target) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(target)) {
                return i;
            }
        }
        // 如果元素不在数组中，则返回-1
        return -1;
    }
}
