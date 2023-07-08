/*
 * Decompiled with CFR 0.150.
 */
package com.terraforged.engine.util.metric;

import com.terraforged.engine.concurrent.cache.SafeCloseable;
import com.terraforged.engine.concurrent.pool.ObjectPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Metric {
    private final AtomicLong hits = new AtomicLong();
    private final AtomicLong nanos = new AtomicLong();
    private final ObjectPool<Timer> pool = new ObjectPool<Timer>(4, () -> new Timer());

    public long hits() {
        return this.hits.get();
    }

    public long nanos() {
        return this.nanos.get();
    }

    public String average() {
        long hits = this.hits();
        double milli = TimeUnit.NANOSECONDS.toMillis(this.nanos());
        double average = milli / (double)hits;
        return String.format("Average: %.3f", average);
    }

    public Timer timer() {
        return this.pool.get().get().punchIn();
    }

    public class Timer implements SafeCloseable {
        private long start = -1L;

        public Timer punchIn() {
            this.start = System.nanoTime();
            return this;
        }

        public Timer punchOut() {
            if (this.start > -1L) {
                long duration = System.nanoTime() - this.start;
                Metric.this.nanos.addAndGet(duration);
                Metric.this.hits.incrementAndGet();
                this.start = -1L;
            }
            return this;
        }

        @Override
        public void close() {
            this.punchOut();
        }
    }
}

