/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.util;

public class NameUtil {
    public static String toDisplayName(String input) {
        StringBuilder sb = new StringBuilder(input.length() + 8);
        for (int i = 0; i < input.length(); ++i) {
            char c = input.charAt(i);
            if (i == 0) {
                c = Character.toUpperCase(c);
            } else if (Character.isUpperCase(c)) {
                sb.append(' ');
            } else {
                if (c == '_') {
                    sb.append(' ');
                    continue;
                }
                if (input.charAt(i - 1) == '_' || input.charAt(i - 1) == ' ') {
                    c = Character.toUpperCase(c);
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }

    public static String toTranslationKey(String parent, String name) {
        StringBuilder sb = new StringBuilder(name.length() * 2);
        if (!parent.isEmpty()) {
            sb.append(parent).append('.');
        }
        for (int i = 0; i < name.length(); ++i) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append('_');
            }
            sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }

    public static String toDisplayNameKey(String name) {
        return "display.terraforged." + name;
    }

    public static String toTooltipKey(String name) {
        return "tooltip.terraforged." + name;
    }
}

