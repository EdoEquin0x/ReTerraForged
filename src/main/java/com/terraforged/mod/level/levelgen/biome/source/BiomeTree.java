package com.terraforged.mod.level.levelgen.biome.source;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraforged.mod.level.levelgen.climate.ClimateSample;

import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;

public class BiomeTree {
	public static final int PARAM_COUNT = 5;

	public static BiomeTree.ParameterPoint parameters(float temperature, float moisture, float continentalness, float biome, float river) {
		return new BiomeTree.ParameterPoint(BiomeTree.Parameter.point(temperature), BiomeTree.Parameter.point(moisture), BiomeTree.Parameter.point(continentalness), BiomeTree.Parameter.point(biome), BiomeTree.Parameter.point(river));
	}

	public static BiomeTree.ParameterPoint parameters(BiomeTree.Parameter temperature, BiomeTree.Parameter moisture, BiomeTree.Parameter continentalness, BiomeTree.Parameter biome, BiomeTree.Parameter river) {
		return new BiomeTree.ParameterPoint(temperature, moisture, continentalness, biome, river);
	}

	interface DistanceMetric<T> {
		float distance(BiomeTree.RTree.Node<T> node, float[] params);
	}

	public static record Parameter(float min, float max) {
		public static final Codec<BiomeTree.Parameter> CODEC = ExtraCodecs.intervalCodec(
			Codec.floatRange(0.0F, 1.0F),
			"min", "max", (min, max) -> {
				return min.compareTo(max) > 0 ? DataResult.error(() -> {
					return "Cannon construct interval, min > max (" + min + " > " + max + ")";
				}) : DataResult.success(new BiomeTree.Parameter(min, max));
			}, (param) -> {
				return param.min();
			}, (max) -> {
				return max.max();
			}
		);

		public static BiomeTree.Parameter point(float value) {
			return span(value, value);
		}

		public static BiomeTree.Parameter span(float min, float max) {
			if (min > max) {
				throw new IllegalArgumentException("min > max: " + min + " " + max);
			} else {
				return new BiomeTree.Parameter(min, max);
			}
		}

		public static BiomeTree.Parameter span(BiomeTree.Parameter p1, BiomeTree.Parameter p2) {
			if (p1.min() > p2.max()) {
				throw new IllegalArgumentException("min > max: " + p1 + " " + p2);
			} else {
				return new BiomeTree.Parameter(p1.min(), p2.max());
			}
		}

		@Override
		public String toString() {
			return this.min == this.max ? String.format(Locale.ROOT, "%d", this.min)
					: String.format(Locale.ROOT, "[%d-%d]", this.min, this.max);
		}

		public float distance(float point) {
			float i = point - this.max;
			float j = this.min - point;
			return i > 0L ? i : Math.max(j, 0L);
		}
		
		private BiomeTree.Parameter span(@Nullable BiomeTree.Parameter parameter) {
			return parameter == null ? this : new BiomeTree.Parameter(Math.min(this.min, parameter.min()), Math.max(this.max, parameter.max()));
		}
	}

	public static class ParameterList<T> {
		private final List<Pair<BiomeTree.ParameterPoint, T>> values;
		private final BiomeTree.RTree<T> index;

		public static <T> Codec<BiomeTree.ParameterList<T>> codec(MapCodec<T> codec) {
			return ExtraCodecs.nonEmptyList(RecordCodecBuilder.<Pair<BiomeTree.ParameterPoint, T>>create((instance) -> {
				return instance.group(BiomeTree.ParameterPoint.CODEC.fieldOf("parameters").forGetter(Pair::getFirst), codec.forGetter(Pair::getSecond)).apply(instance, Pair::of);
			}).listOf()).xmap(BiomeTree.ParameterList::new, BiomeTree.ParameterList::values);
		}

		public ParameterList(List<Pair<BiomeTree.ParameterPoint, T>> values) {
			this.values = values;
			this.index = BiomeTree.RTree.create(values);
		}

		public List<Pair<BiomeTree.ParameterPoint, T>> values() {
			return this.values;
		}

		private final DistanceMetric<T> metric = BiomeTree.RTree.Node::distance;
		public T findValue(ClimateSample sample) {
			return this.index.search(sample, this.metric);
		}
	}

	public static record ParameterPoint(BiomeTree.Parameter temperature, BiomeTree.Parameter moisture, BiomeTree.Parameter continentalness, BiomeTree.Parameter biome, BiomeTree.Parameter river) {
		public static final Codec<BiomeTree.ParameterPoint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BiomeTree.Parameter.CODEC.fieldOf("temperature").forGetter((point) -> {
				return point.temperature;
			}), BiomeTree.Parameter.CODEC.fieldOf("moisture").forGetter((point) -> {
				return point.moisture;
			}), BiomeTree.Parameter.CODEC.fieldOf("continentalness").forGetter((point) -> {
				return point.continentalness;
			}), BiomeTree.Parameter.CODEC.fieldOf("biome").forGetter((point) -> {
				return point.biome;
			}), BiomeTree.Parameter.CODEC.fieldOf("river").forGetter((point) -> {
				return point.river;
			})).apply(instance, BiomeTree.ParameterPoint::new));

		float fitness(ClimateSample sample) {
			return Mth.square(this.temperature.distance(sample.temperature))
					+ Mth.square(this.moisture.distance(sample.moisture))
					+ Mth.square(this.continentalness.distance(sample.continentNoise))
					+ Mth.square(this.biome.distance(sample.biomeNoise))
					+ Mth.square(this.river.distance(sample.riverNoise));
		}

		protected List<BiomeTree.Parameter> parameterSpace() {
			return ImmutableList.of(this.temperature, this.moisture, this.continentalness, this.biome, this.river);
		}
	}

	protected static final class RTree<T> {
		private final BiomeTree.RTree.Node<T> root;
		private final ThreadLocal<BiomeTree.RTree.Leaf<T>> lastResult = new ThreadLocal<>();

		private RTree(BiomeTree.RTree.Node<T> root) {
			this.root = root;
		}

		public static <T> BiomeTree.RTree<T> create(List<Pair<BiomeTree.ParameterPoint, T>> points) {
			if (points.isEmpty()) {
				throw new IllegalArgumentException("Need at least one value to build the search tree.");
			} else {
				int i = points.get(0).getFirst().parameterSpace().size();
				if (i != PARAM_COUNT) {
					throw new IllegalStateException("Expecting parameter space to be " + PARAM_COUNT + ", got " + i);
				} else {
					List<BiomeTree.RTree.Leaf<T>> list = points.stream().map((point) -> {
						return new BiomeTree.RTree.Leaf<T>(point.getFirst(), point.getSecond());
					}).collect(Collectors.toCollection(ArrayList::new));
					return new BiomeTree.RTree<>(build(i, list));
				}
			}
		}

		private static <T> BiomeTree.RTree.Node<T> build(int count, List<? extends BiomeTree.RTree.Node<T>> nodes) {
			if (nodes.isEmpty()) {
				throw new IllegalStateException("Need at least one child to build a node");
			} else if (nodes.size() == 1) {
				return nodes.get(0);
			} else if (nodes.size() <= PARAM_COUNT) {
				nodes.sort(Comparator.comparingLong((node) -> {
					long i1 = 0L;

					for (int j1 = 0; j1 < count; ++j1) {
						BiomeTree.Parameter climate$parameter = node.parameterSpace[j1];
						i1 += Math.abs((climate$parameter.min() + climate$parameter.max()) / 2L);
					}

					return i1;
				}));
				return new BiomeTree.RTree.SubTree<>(nodes);
			} else {
				long i = Long.MAX_VALUE;
				int j = -1;
				List<BiomeTree.RTree.SubTree<T>> list = null;

				for (int k = 0; k < count; ++k) {
					sort(nodes, count, k, false);
					List<BiomeTree.RTree.SubTree<T>> list1 = bucketize(nodes);
					long l = 0L;

					for (BiomeTree.RTree.SubTree<T> subtree : list1) {
						l += cost(subtree.parameterSpace);
					}

					if (i > l) {
						i = l;
						j = k;
						list = list1;
					}
				}

				sort(list, count, j, true);
				return new BiomeTree.RTree.SubTree<>(list.stream().map((tree) -> {
					return build(count, Arrays.asList(tree.children));
				}).collect(Collectors.toList()));
			}
		}

		private static <T> void sort(List<? extends BiomeTree.RTree.Node<T>> nodes, int count, int paramIndex, boolean abs) {
			Comparator<BiomeTree.RTree.Node<T>> comparator = comparator(paramIndex, abs);

			for (int i = 1; i < count; ++i) {
				comparator = comparator.thenComparing(comparator((paramIndex + i) % count, abs));
			}

			nodes.sort(comparator);
		}

		private static <T> Comparator<BiomeTree.RTree.Node<T>> comparator(int paramIndex, boolean abs) {
			return Comparator.comparingDouble((node) -> {
				BiomeTree.Parameter climate$parameter = node.parameterSpace[paramIndex];
				float i = (climate$parameter.min() + climate$parameter.max()) / 2F;
				return abs ? Math.abs(i) : i;
			});
		}

		private static <T> List<BiomeTree.RTree.SubTree<T>> bucketize(List<? extends BiomeTree.RTree.Node<T>> nodes) {
			List<BiomeTree.RTree.SubTree<T>> list = Lists.newArrayList();
			List<BiomeTree.RTree.Node<T>> list1 = Lists.newArrayList();
			int i = (int) Math.pow(PARAM_COUNT, Math.floor(Math.log((double) nodes.size() - 0.01D) / Math.log(PARAM_COUNT)));

			for (BiomeTree.RTree.Node<T> node : nodes) {
				list1.add(node);
				if (list1.size() >= i) {
					list.add(new BiomeTree.RTree.SubTree<>(list1));
					list1 = Lists.newArrayList();
				}
			}

			if (!list1.isEmpty()) {
				list.add(new BiomeTree.RTree.SubTree<>(list1));
			}

			return list;
		}

		private static long cost(BiomeTree.Parameter[] params) {
			long i = 0L;

			for (BiomeTree.Parameter climate$parameter : params) {
				i += Math.abs(climate$parameter.max() - climate$parameter.min());
			}

			return i;
		}

		static <T> List<BiomeTree.Parameter> buildParameterSpace(List<? extends BiomeTree.RTree.Node<T>> nodes) {
			if (nodes.isEmpty()) {
				throw new IllegalArgumentException("SubTree needs at least one child");
			} else {
				List<BiomeTree.Parameter> list = Lists.newArrayList();

				for (int j = 0; j < PARAM_COUNT; ++j) {
					list.add((BiomeTree.Parameter) null);
				}

				for (BiomeTree.RTree.Node<T> node : nodes) {
					for (int k = 0; k < PARAM_COUNT; ++k) {
						list.set(k, node.parameterSpace[k].span(list.get(k)));
					}
				}

				return list;
			}
		}

		public T search(ClimateSample point, BiomeTree.DistanceMetric<T> metric) {
			float[] along = toParameterArray(point);
			BiomeTree.RTree.Leaf<T> leaf = this.root.search(along, this.lastResult.get(), metric);
			this.lastResult.set(leaf);
			return leaf.value;
		}

		static final class Leaf<T> extends BiomeTree.RTree.Node<T> {
			final T value;

			Leaf(BiomeTree.ParameterPoint point, T value) {
				super(point.parameterSpace());
				this.value = value;
			}

			protected BiomeTree.RTree.Leaf<T> search(float[] params, @Nullable BiomeTree.RTree.Leaf<T> leaf, BiomeTree.DistanceMetric<T> metric) {
				return this;
			}
		}

		abstract static class Node<T> {
			protected final BiomeTree.Parameter[] parameterSpace;

			protected Node(List<BiomeTree.Parameter> paramSpace) {
				this.parameterSpace = paramSpace.toArray(new BiomeTree.Parameter[0]);
			}

			protected abstract BiomeTree.RTree.Leaf<T> search(float[] params, @Nullable BiomeTree.RTree.Leaf<T> leaf, BiomeTree.DistanceMetric<T> metric);

			protected float distance(float[] params) {
				float i = 0L;

				for (int j = 0; j < PARAM_COUNT; ++j) {
					i += Mth.square(this.parameterSpace[j].distance(params[j]));
				}

				return i;
			}

			public String toString() {
				return Arrays.toString((Object[]) this.parameterSpace);
			}
		}

		static final class SubTree<T> extends BiomeTree.RTree.Node<T> {
			final BiomeTree.RTree.Node<T>[] children;

			protected SubTree(List<? extends BiomeTree.RTree.Node<T>> nodes) {
				this(BiomeTree.RTree.buildParameterSpace(nodes), nodes);
			}

			@SuppressWarnings("unchecked")
			protected SubTree(List<BiomeTree.Parameter> params, List<? extends BiomeTree.RTree.Node<T>> nodes) {
				super(params);
				this.children = nodes.toArray(new BiomeTree.RTree.Node[0]);
			}

			protected BiomeTree.RTree.Leaf<T> search(float[] params, @Nullable BiomeTree.RTree.Leaf<T> leaf, BiomeTree.DistanceMetric<T> metric) {
				float i = leaf == null ? Float.MAX_VALUE : metric.distance(leaf, params);
				BiomeTree.RTree.Leaf<T> found = leaf;

				for (BiomeTree.RTree.Node<T> node : this.children) {
					float j = metric.distance(node, params);
					if (i > j) {
						BiomeTree.RTree.Leaf<T> leaf1 = node.search(params, found, metric);
						float k = node == leaf1 ? j : metric.distance(leaf1, params);
						if (i > k) {
							i = k;
							found = leaf1;
						}
					}
				}

				return found;
			}
		}
	}
	
	public static float[] toParameterArray(ClimateSample sample) {
		sample.params[0] = sample.temperature;
		sample.params[1] = sample.moisture;
		sample.params[2] = sample.continentNoise;
		sample.params[3] = sample.biomeNoise;
		sample.params[4] = sample.riverNoise;
		return sample.params;
	}
}