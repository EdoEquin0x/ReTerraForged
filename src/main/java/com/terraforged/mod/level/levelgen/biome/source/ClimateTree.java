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
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraforged.mod.level.levelgen.climate.Climate;
import com.terraforged.mod.level.levelgen.climate.ClimateSample;

import net.minecraft.core.Holder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;

public class ClimateTree {
	public static final int PARAM_COUNT = 5;

	public static ClimateTree.ParameterPoint parameters(Holder<Climate> climate, float temperature, float moisture, float continentalness, float height, float river) {
		return new ClimateTree.ParameterPoint(climate, ClimateTree.Parameter.point(temperature), ClimateTree.Parameter.point(moisture), ClimateTree.Parameter.point(continentalness), ClimateTree.Parameter.point(height), ClimateTree.Parameter.point(river));
	}

	public static ClimateTree.ParameterPoint parameters(Holder<Climate> climate, ClimateTree.Parameter temperature, ClimateTree.Parameter moisture, ClimateTree.Parameter continentalness, ClimateTree.Parameter height, ClimateTree.Parameter river) {
		return new ClimateTree.ParameterPoint(climate, temperature, moisture, continentalness, height, river);
	}

	public static record Parameter(float min, float max) {
		public static final Codec<ClimateTree.Parameter> CODEC = ExtraCodecs.intervalCodec(
			Codec.floatRange(0.0F, 1.0F),
			"min", "max", (min, max) -> {
				return min.compareTo(max) > 0 ? DataResult.error(() -> {
					return "Cannon construct interval, min > max (" + min + " > " + max + ")";
				}) : DataResult.success(new ClimateTree.Parameter(min, max));
			}, (param) -> {
				return param.min();
			}, (max) -> {
				return max.max();
			}
		);

		public static ClimateTree.Parameter point(float value) {
			return span(value, value);
		}

		public static ClimateTree.Parameter span(float min, float max) {
			if (min > max) {
				throw new IllegalArgumentException("min > max: " + min + " " + max);
			} else {
				return new ClimateTree.Parameter(min, max);
			}
		}
		
		public static ClimateTree.Parameter min(float min) {
			return span(min, 1.0F);
		}
		
		public static ClimateTree.Parameter max(float max) {
			return span(0.0F, max);
		}
		
		public static ClimateTree.Parameter any() {
			return span(0.0F, 1.0F);
		}

		public static ClimateTree.Parameter span(ClimateTree.Parameter p1, ClimateTree.Parameter p2) {
			if (p1.min() > p2.max()) {
				throw new IllegalArgumentException("min > max: " + p1 + " " + p2);
			} else {
				return new ClimateTree.Parameter(p1.min(), p2.max());
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
		
		private ClimateTree.Parameter span(@Nullable ClimateTree.Parameter parameter) {
			return parameter == null ? this : new ClimateTree.Parameter(Math.min(this.min, parameter.min()), Math.max(this.max, parameter.max()));
		}
	}

	public static class ParameterList {
		private final List<ClimateTree.ParameterPoint> values;
		private final ClimateTree.RTree index;

		public static final Codec<ParameterList> CODEC = ExtraCodecs.nonEmptyList(ClimateTree.ParameterPoint.CODEC.listOf()).xmap(ClimateTree.ParameterList::new, ClimateTree.ParameterList::values);

		public ParameterList(List<ClimateTree.ParameterPoint> values) {
			this.values = values;
			this.index = ClimateTree.RTree.create(values);
		}

		public List<ClimateTree.ParameterPoint> values() {
			return this.values;
		}

		public Holder<Climate> findValue(ClimateSample sample) {
			return this.index.search(sample);
		}
	}

	public static record ParameterPoint(Holder<Climate> climate, ClimateTree.Parameter temperature, ClimateTree.Parameter moisture, ClimateTree.Parameter continentalness, ClimateTree.Parameter height, ClimateTree.Parameter river) {
		public static final Codec<ClimateTree.ParameterPoint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Climate.CODEC.fieldOf("climate").forGetter(ParameterPoint::climate), 
			ClimateTree.Parameter.CODEC.fieldOf("temperature").forGetter(ParameterPoint::temperature), 
			ClimateTree.Parameter.CODEC.fieldOf("moisture").forGetter(ParameterPoint::moisture), 
			ClimateTree.Parameter.CODEC.fieldOf("continentalness").forGetter(ParameterPoint::continentalness), 
			ClimateTree.Parameter.CODEC.fieldOf("height").forGetter(ParameterPoint::height), 
			ClimateTree.Parameter.CODEC.fieldOf("river").forGetter(ParameterPoint::river)
		).apply(instance, ClimateTree.ParameterPoint::new));

		float fitness(ClimateSample sample) {
			return Mth.square(this.temperature.distance(sample.temperature))
					+ Mth.square(this.moisture.distance(sample.moisture))
					+ Mth.square(this.continentalness.distance(sample.continentNoise))
					+ Mth.square(this.height.distance(sample.heightNoise))
					+ Mth.square(this.river.distance(sample.riverNoise));
		}

		protected List<ClimateTree.Parameter> parameterSpace() {
			return ImmutableList.of(this.temperature, this.moisture, this.continentalness, this.height, this.river);
		}
	}

	protected static final class RTree {
		private final ClimateTree.RTree.Node root;
		private final ThreadLocal<ClimateTree.RTree.Leaf> lastResult = new ThreadLocal<>();

		private RTree(ClimateTree.RTree.Node root) {
			this.root = root;
		}

		public static ClimateTree.RTree create(List<ClimateTree.ParameterPoint> points) {
			if (points.isEmpty()) {
				throw new IllegalArgumentException("Need at least one value to build the search tree.");
			} else {
				int i = points.get(0).parameterSpace().size();
				if (i != PARAM_COUNT) {
					throw new IllegalStateException("Expecting parameter space to be " + PARAM_COUNT + ", got " + i);
				} else {
					List<ClimateTree.RTree.Leaf> list = points.stream().map((point) -> {
						return new ClimateTree.RTree.Leaf(point);
					}).collect(Collectors.toCollection(ArrayList::new));
					return new ClimateTree.RTree(build(i, list));
				}
			}
		}

		private static ClimateTree.RTree.Node build(int count, List<? extends ClimateTree.RTree.Node> nodes) {
			if (nodes.isEmpty()) {
				throw new IllegalStateException("Need at least one child to build a node");
			} else if (nodes.size() == 1) {
				return nodes.get(0);
			} else if (nodes.size() <= PARAM_COUNT) {
				nodes.sort(Comparator.comparingLong((node) -> {
					long i1 = 0L;

					for (int j1 = 0; j1 < count; ++j1) {
						ClimateTree.Parameter param = node.parameterSpace[j1];
						i1 += Math.abs((param.min() + param.max()) / 2L);
					}

					return i1;
				}));
				return new ClimateTree.RTree.SubTree(nodes);
			} else {
				long i = Long.MAX_VALUE;
				int j = -1;
				List<ClimateTree.RTree.SubTree> list = null;

				for (int k = 0; k < count; ++k) {
					sort(nodes, count, k, false);
					List<ClimateTree.RTree.SubTree> list1 = bucketize(nodes);
					long l = 0L;

					for (ClimateTree.RTree.SubTree subtree : list1) {
						l += cost(subtree.parameterSpace);
					}

					if (i > l) {
						i = l;
						j = k;
						list = list1;
					}
				}

				sort(list, count, j, true);
				return new ClimateTree.RTree.SubTree(list.stream().map((tree) -> {
					return build(count, Arrays.asList(tree.children));
				}).collect(Collectors.toList()));
			}
		}

		private static void sort(List<? extends ClimateTree.RTree.Node> nodes, int count, int paramIndex, boolean abs) {
			Comparator<ClimateTree.RTree.Node> comparator = comparator(paramIndex, abs);

			for (int i = 1; i < count; ++i) {
				comparator = comparator.thenComparing(comparator((paramIndex + i) % count, abs));
			}

			nodes.sort(comparator);
		}

		private static Comparator<ClimateTree.RTree.Node> comparator(int paramIndex, boolean abs) {
			return Comparator.comparingDouble((node) -> {
				ClimateTree.Parameter param = node.parameterSpace[paramIndex];
				float i = (param.min() + param.max()) / 2F;
				return abs ? Math.abs(i) : i;
			});
		}

		private static List<ClimateTree.RTree.SubTree> bucketize(List<? extends ClimateTree.RTree.Node> nodes) {
			List<ClimateTree.RTree.SubTree> list = Lists.newArrayList();
			List<ClimateTree.RTree.Node> list1 = Lists.newArrayList();
			int i = (int) Math.pow(PARAM_COUNT, Math.floor(Math.log((double) nodes.size() - 0.01D) / Math.log(PARAM_COUNT)));

			for (ClimateTree.RTree.Node node : nodes) {
				list1.add(node);
				if (list1.size() >= i) {
					list.add(new ClimateTree.RTree.SubTree(list1));
					list1 = Lists.newArrayList();
				}
			}

			if (!list1.isEmpty()) {
				list.add(new ClimateTree.RTree.SubTree(list1));
			}
			return list;
		}

		private static long cost(ClimateTree.Parameter[] params) {
			long i = 0L;

			for (ClimateTree.Parameter param : params) {
				i += Math.abs(param.max() - param.min());
			}

			return i;
		}

		static <T> List<ClimateTree.Parameter> buildParameterSpace(List<? extends ClimateTree.RTree.Node> nodes) {
			if (nodes.isEmpty()) {
				throw new IllegalArgumentException("SubTree needs at least one child");
			} else {
				List<ClimateTree.Parameter> list = Lists.newArrayList();

				for (int j = 0; j < PARAM_COUNT; ++j) {
					list.add((ClimateTree.Parameter) null);
				}

				for (ClimateTree.RTree.Node node : nodes) {
					for (int k = 0; k < PARAM_COUNT; ++k) {
						list.set(k, node.parameterSpace[k].span(list.get(k)));
					}
				}

				return list;
			}
		}

		public Holder<Climate> search(ClimateSample point) {
			float[] along = toParameterArray(point);
			ClimateTree.RTree.Leaf leaf = this.root.search(along, this.lastResult.get());
			this.lastResult.set(leaf);
			return leaf.value;
		}

		static final class Leaf extends ClimateTree.RTree.Node {
			final Holder<Climate> value;

			Leaf(ClimateTree.ParameterPoint point) {
				super(point.parameterSpace());
				this.value = point.climate();
			}

			protected ClimateTree.RTree.Leaf search(float[] params, @Nullable ClimateTree.RTree.Leaf leaf) {
				return this;
			}
		}

		abstract static class Node {
			protected final ClimateTree.Parameter[] parameterSpace;

			protected Node(List<ClimateTree.Parameter> paramSpace) {
				this.parameterSpace = paramSpace.toArray(new ClimateTree.Parameter[0]);
			}

			protected abstract ClimateTree.RTree.Leaf search(float[] params, @Nullable ClimateTree.RTree.Leaf leaf);

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

		static final class SubTree extends ClimateTree.RTree.Node {
			final ClimateTree.RTree.Node[] children;

			protected SubTree(List<? extends ClimateTree.RTree.Node> nodes) {
				this(ClimateTree.RTree.buildParameterSpace(nodes), nodes);
			}

			protected SubTree(List<ClimateTree.Parameter> params, List<? extends ClimateTree.RTree.Node> nodes) {
				super(params);
				this.children = nodes.toArray(new ClimateTree.RTree.Node[0]);
			}

			protected ClimateTree.RTree.Leaf search(float[] params, @Nullable ClimateTree.RTree.Leaf leaf) {
				float i = leaf == null ? Float.MAX_VALUE : leaf.distance(params);
				ClimateTree.RTree.Leaf found = leaf;

				for (ClimateTree.RTree.Node node : this.children) {
					float j = node.distance(params);
					if (i > j) {
						ClimateTree.RTree.Leaf leaf1 = node.search(params, found);
						float k = node == leaf1 ? j : leaf1.distance(params);
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
		sample.params[3] = sample.heightNoise;
		sample.params[4] = sample.riverNoise;
		return sample.params;
	}
}