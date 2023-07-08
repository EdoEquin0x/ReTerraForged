/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.concurrent.batch;

public class BatchTimeoutException extends BatchTaskException {
	private static final long serialVersionUID = 7162681177345035645L;

	public BatchTimeoutException(String message) {
        super(message);
    }
}

