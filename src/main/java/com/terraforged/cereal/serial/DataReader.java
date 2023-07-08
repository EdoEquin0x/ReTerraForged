/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.cereal.serial;

import java.io.IOException;
import java.io.Reader;

import com.terraforged.cereal.CerealSpec;
import com.terraforged.cereal.value.DataList;
import com.terraforged.cereal.value.DataObject;
import com.terraforged.cereal.value.DataValue;

public class DataReader implements AutoCloseable {
    private final Reader reader;
    private final CerealSpec spec;
    private final DataBuffer buffer = new DataBuffer();
    private char c = (char)65535;

    public DataReader(Reader reader) {
        this(reader, CerealSpec.STANDARD);
    }

    public DataReader(Reader reader, CerealSpec spec) {
        this.reader = reader;
        this.spec = spec;
    }

    private boolean next() throws IOException {
        int i = this.reader.read();
        if (i == -1) {
            return false;
        }
        this.c = (char)i;
        return true;
    }

    private void skipSpace() throws IOException {
        while (Character.isWhitespace(this.c)) {
            if (this.next()) continue;
            throw new IOException("Unexpected end");
        }
    }

    public DataValue read() throws IOException {
        if (this.next()) {
            return this.readValue();
        }
        return DataValue.NULL;
    }

    private DataValue readValue() throws IOException {
        this.skipSpace();
        if (this.c == '{' && this.next()) {
            return this.readObject("");
        }
        if (this.c == '[' && this.next()) {
            return this.readList();
        }
        Object value = this.readPrimitive();
        if (value instanceof String) {
            this.skipSpace();
            if (this.c == '{' && this.next()) {
                return this.readObject(value.toString());
            }
        }
        return DataValue.of(value);
    }

    private DataValue readObject(String type) throws IOException {
        DataObject data = new DataObject(type);
        while (true) {
            this.skipSpace();
            if (this.c == '}') break;
            String key = this.readKey();
            DataValue value = this.readValue();
            data.add(key, value);
        }
        this.next();
        return data;
    }

    private DataValue readList() throws IOException {
        DataList list = new DataList();
        while (true) {
            this.skipSpace();
            if (this.c == ']') break;
            list.add(this.readValue());
        }
        this.next();
        return list;
    }

    private String readKey() throws IOException {
        this.skipSpace();
        this.buffer.reset();
        this.buffer.append(this.c);
        while (true) {
            if (!this.next()) {
                throw new IOException("Unexpected end: " + this.buffer.toString());
            }
            if (!Character.isLetterOrDigit(this.c) && this.c != '_') {
                if (this.c != ':') break;
                this.next();
                break;
            }
            this.buffer.append(this.c);
        }
        return this.buffer.toString();
    }

    private Object readPrimitive() throws IOException {
        if (this.c == this.spec.escapeChar) {
            return this.readEscapedString();
        }
        this.buffer.reset();
        this.buffer.append(this.c);
        while (true) {
            if (!this.next()) {
                throw new IOException("Unexpected end of string: " + this.buffer.toString());
            }
            if (!Character.isLetterOrDigit(this.c) && this.c != '.' && this.c != '-' && this.c != '_') break;
            this.buffer.append(this.c);
        }
        return this.buffer.getValue();
    }

    private String readEscapedString() throws IOException {
        this.buffer.reset();
        while (true) {
            if (!this.next()) {
                throw new IOException("Unexpected end of string: " + this.buffer.toString());
            }
            if (this.c == this.spec.escapeChar) break;
            this.buffer.append(this.c);
        }
        this.next();
        return this.buffer.toString();
    }

    @Override
    public void close() throws Exception {
        this.reader.close();
    }
}

