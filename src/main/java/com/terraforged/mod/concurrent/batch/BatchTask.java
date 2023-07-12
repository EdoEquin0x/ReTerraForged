/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.mod.concurrent.batch;

public interface BatchTask extends Runnable {
    public static final Notifier NONE = () -> {};

    public void setNotifier(Notifier var1);

    public static interface Notifier {
        public void markDone();
    }
}

