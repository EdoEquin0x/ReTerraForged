/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.concurrent.batch;

public class BatchTaskException extends RuntimeException {
    private static final long serialVersionUID = -3761491805159677387L;

	public BatchTaskException(String message) {
        super(message);
    }

    public BatchTaskException(String message, Throwable cause) {
        super(message, cause);
    }
}

