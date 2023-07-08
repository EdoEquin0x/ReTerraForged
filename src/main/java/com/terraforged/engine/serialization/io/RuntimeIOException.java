/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.serialization.io;

import java.io.IOException;

public class RuntimeIOException extends RuntimeException {
    private static final long serialVersionUID = -903430658978479697L;
	private final IOException cause;

    public RuntimeIOException(IOException cause) {
        this.cause = cause;
    }

    @Override
    public IOException getCause() {
        return this.cause;
    }
}

