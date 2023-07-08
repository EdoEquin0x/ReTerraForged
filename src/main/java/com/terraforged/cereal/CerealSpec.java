/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.cereal;

public class CerealSpec {
    public static final char NONE = '\u0000';
    public static final CerealSpec STANDARD = new CerealSpec("  ", '\u0000', ' ', '\'');
    public final String indent;
    public final char delimiter;
    public final char separator;
    public final char escapeChar;

    public CerealSpec(String indent, char delimiter, char separator, char escapeChar) {
        this.indent = indent;
        this.delimiter = delimiter;
        this.escapeChar = escapeChar;
        this.separator = (char)(delimiter == '\u0000' ? 32 : (int)separator);
    }
}

