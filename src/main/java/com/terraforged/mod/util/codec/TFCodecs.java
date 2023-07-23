/*
 * MIT License
 *
 * Copyright (c) 2021 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.mod.util.codec;

import java.util.function.Function;
import java.util.function.IntFunction;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.GameData;

public class TFCodecs {

	public static <V> Codec<V[]> forArray(Codec<V> elementCodec, IntFunction<V[]> generator) {
		return Codec.list(elementCodec).xmap((v) -> {
			return v.toArray(generator);
		}, ImmutableList::copyOf);
	}

	public static <T extends Enum<T>> Codec<T> forEnum(Function<String, T> enumLookup) {
		return Codec.STRING.xmap(String::toUpperCase, String::toLowerCase).xmap(enumLookup::apply, Enum::name);
	}

	public static <A> Codec<A> forRegistry(ResourceKey<Registry<Codec<? extends A>>> key, Function<A, Codec<? extends A>> codec) {
		return new Codec<A>() {
			private final Lazy<Codec<A>> delegate = Lazy.of(() -> {
				return GameData.getWrapper(key, Lifecycle.stable()).byNameCodec().dispatch(codec, Function.identity());
			});
			
			@Override
			public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
				return this.delegate.get().encode(input, ops, prefix);
			}

			@Override
			public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
				return this.delegate.get().decode(ops, input);
			}
		};
	}
	
	public static <A> Codec<A> forError(String error) {
		return new Codec<>() {

			@Override
			public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
				return DataResult.error(() -> error);
			}

			@Override
			public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
				return DataResult.error(() -> error);
			}			
		};
	}
}
