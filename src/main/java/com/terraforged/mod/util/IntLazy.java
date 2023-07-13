package com.terraforged.mod.util;

import java.util.function.IntFunction;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

public class IntLazy<T> implements Supplier<T> {
	private volatile Object lock;
	private volatile T instance;
	private volatile IntFunction<T> supplier;

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
		if (this.supplier != null) {
			synchronized (localLock) {
				if (this.supplier != null) {
					this.instance = this.supplier.apply(value);
					this.supplier = null;
					this.lock = null;
				}
			}
		}
		return this.instance;
	}
	
	public static <T> IntLazy<T> of(IntFunction<T> supplier) {
		return new IntLazy<>(supplier);
	}
}
