package com.terraforged.mod.util;

import java.util.function.IntFunction;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

@Deprecated(forRemoval = true)
public class IntLazy<T> implements Supplier<T> {
	private IntFunction<T> supplier;
	private Object lock;
	private volatile T instance;

	public IntLazy(IntFunction<T> supplier) {
		this.supplier = supplier;
		this.lock = new Object();
	}
	
	@Override
	public T get() {
		if(this.instance == null) {
			throw new IllegalStateException("Missing instance");
		}
		return this.instance;
	}

	@Nullable
	public final T apply(int value) {
		Object localLock = this.lock;
		synchronized (localLock) {
			this.instance = this.supplier.apply(value);
		}
		return this.instance;
	}
	
	public static <T> IntLazy<T> of(IntFunction<T> supplier) {
		return new IntLazy<>(supplier);
	}
}
