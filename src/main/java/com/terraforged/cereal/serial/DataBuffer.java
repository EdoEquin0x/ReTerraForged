/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.cereal.serial;

import java.util.Arrays;

public class DataBuffer {
    private char[] buffer = new char[5];
    private int index = -1;
    private boolean decimal = false;
    private boolean numeric = true;

    public void reset() {
        this.index = -1;
        this.numeric = true;
        this.decimal = false;
    }

    public void append(char c) {
        ++this.index;
        if (this.index >= this.buffer.length) {
            this.buffer = Arrays.copyOf(this.buffer, this.buffer.length * 2);
        }
        this.buffer[this.index] = c;
        if (this.numeric) {
            if (Character.isDigit(c)) {
                return;
            }
            if (c == '.' && !this.decimal && this.index > 0) {
                this.decimal = true;
                return;
            }
            if (c == '-' && this.index == 0) {
                return;
            }
            this.numeric = false;
        }
    }

    public Object getValue() {
        if (this.index == 4 && DataBuffer.matches(this.buffer, 4, "true")) {
            return true;
        }
        if (this.index == 5 && DataBuffer.matches(this.buffer, 5, "false")) {
            return false;
        }
        if (this.numeric) {
            if (this.decimal) {
                return DataBuffer.parseDouble(this.buffer, this.index + 1);
            }
            return DataBuffer.parseLong(this.buffer, this.index + 1);
        }
        return this.toString();
    }

    public String toString() {
        return new String(this.buffer, 0, this.index + 1);
    }

    public static boolean matches(char[] buffer, int length, String other) {
        if (length != other.length()) {
            return false;
        }
        for (int i = 0; i < length; ++i) {
            if (Character.toUpperCase(buffer[i]) == Character.toUpperCase(other.charAt(i))) continue;
            return false;
        }
        return true;
    }

    public static long parseLong(char[] buffer, int length) {
        long value = 0L;
        boolean negative = false;
        for (int i = 0; i < length; ++i) {
            char c = buffer[i];
            if (i == 0 && c == '-') {
                negative = true;
                continue;
            }
            value = value * 10L + (long)(c - 48);
        }
        return negative ? -value : value;
    }

    public static double parseDouble(char[] buffer, int length) {
        double value = 0.0;
        int decimalPlace = 0;
        boolean negative = false;
        for (int i = 0; i < length; ++i) {
            char c = buffer[i];
            if (i == 0 && c == '-') {
                negative = true;
                continue;
            }
            if (c == '.') {
                decimalPlace = 1;
                continue;
            }
            value = value * 10.0 + (double)(c - 48);
            if (decimalPlace <= 0) continue;
            decimalPlace *= 10;
        }
        if (decimalPlace > 0) {
            value /= (double)decimalPlace;
        }
        return negative ? -value : value;
    }
}

