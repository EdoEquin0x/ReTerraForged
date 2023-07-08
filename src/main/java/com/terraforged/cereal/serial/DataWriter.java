/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.cereal.serial;

import java.io.IOException;
import java.io.Writer;

import com.terraforged.cereal.CerealSpec;
import com.terraforged.cereal.value.DataValue;

public class DataWriter implements AutoCloseable {
    private final Writer writer;
    private final CerealSpec spec;
    private int indents = 0;
    private boolean newLine = false;

    public DataWriter(Writer writer) {
        this(writer, CerealSpec.STANDARD);
    }

    public DataWriter(Writer writer, CerealSpec spec) {
        this.writer = writer;
        this.spec = spec;
    }

    public void write(DataValue value) throws IOException {
        value.appendTo(this);
    }

    public DataWriter beginObj() throws IOException {
        this.newLine();
        this.append('{');
        this.newLine = true;
        ++this.indents;
        return this;
    }

    public DataWriter endObj() throws IOException {
        --this.indents;
        this.newLine();
        this.append('}');
        this.newLine = true;
        return this;
    }

    public DataWriter beginList() throws IOException {
        this.newLine();
        this.append('[');
        this.newLine = true;
        ++this.indents;
        return this;
    }

    public DataWriter endList() throws IOException {
        --this.indents;
        this.newLine();
        this.append(']');
        this.newLine = true;
        return this;
    }

    public DataWriter name(String name) throws IOException {
        this.newLine();
        this.append(name);
        this.append(this.spec.delimiter);
        this.append(this.spec.separator);
        return this;
    }

    public DataWriter type(String name) throws IOException {
        if (!name.isEmpty()) {
            this.newLine();
            this.append(name);
            this.append(this.spec.separator);
        }
        return this;
    }

    public DataWriter value(Object value) throws IOException {
        if (value instanceof String && DataWriter.escape(value.toString())) {
            this.append(this.spec.escapeChar);
            this.append(value.toString());
            this.append(this.spec.escapeChar);
        } else {
            this.append(value.toString());
        }
        this.newLine = true;
        return this;
    }

    public DataWriter value(DataValue value) throws IOException {
        value.appendTo(this);
        return this;
    }

    private void append(char c) throws IOException {
        if (c != '\u0000') {
            this.writer.append(c);
        }
    }

    private void append(String string) throws IOException {
        if (string.length() > 0) {
            this.writer.append(string);
        }
    }

    private void newLine() throws IOException {
        if (this.newLine && !this.spec.indent.isEmpty()) {
            this.append('\n');
            this.newLine = false;
            this.indent();
        }
    }

    private void indent() throws IOException {
        if (!this.spec.indent.isEmpty()) {
            for (int i = 0; i < this.indents; ++i) {
                this.append(this.spec.indent);
            }
        }
    }

    private static boolean escape(String in) {
        for (int i = 0; i < in.length(); ++i) {
            char c = in.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '_') continue;
            return true;
        }
        return false;
    }

    @Override
    public void close() throws Exception {
        this.writer.close();
    }
}

