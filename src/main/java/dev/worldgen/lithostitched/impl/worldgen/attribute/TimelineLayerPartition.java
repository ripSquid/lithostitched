package dev.worldgen.lithostitched.impl.worldgen.attribute;

import net.minecraft.world.attribute.EnvironmentAttributeLayer;

import java.util.ArrayList;
import java.util.List;

/**
 * An environment attribute sampler's layers, split into the layers vanilla knows how to apply
 * ({@link #standardLayers()}) and Lithostitched's {@link PositionalTimeBasedLayer}s ({@link #timelineLayers()}).
 * <p>
 * Samplers are queried very often (every mob, every tick, during spawning), but their layer list never
 * changes after construction, so the split is computed once and reused instead of being rebuilt with streams
 * on every call.
 * <p>
 * Instances are immutable and only hold final fields, so they are safe to publish to other threads through a
 * plain (non-volatile) field. Worst case two threads compute the same partition concurrently.
 * <p>
 * AI Disclosure: this class and the matching change in EnvironmentAttributeSystemValueSamplerMixin were written
 * with AI assistance (Claude) and reviewed by a human. The rest of the mod is unchanged from upstream.
 */
public final class TimelineLayerPartition<Value> {
	private final List<EnvironmentAttributeLayer<Value>> source;
	private final int sourceSize;
	private final List<EnvironmentAttributeLayer<Value>> standardLayers;
	private final List<PositionalTimeBasedLayer<Value>> timelineLayers;

	private TimelineLayerPartition(
		List<EnvironmentAttributeLayer<Value>> source,
		List<EnvironmentAttributeLayer<Value>> standardLayers,
		List<PositionalTimeBasedLayer<Value>> timelineLayers
	) {
		this.source = source;
		this.sourceSize = source.size();
		this.standardLayers = standardLayers;
		this.timelineLayers = timelineLayers;
	}

	public static <Value> TimelineLayerPartition<Value> of(List<EnvironmentAttributeLayer<Value>> layers) {
		List<EnvironmentAttributeLayer<Value>> standard = null;
		List<PositionalTimeBasedLayer<Value>> timeline = null;

		for (int i = 0, size = layers.size(); i < size; i++) {
			EnvironmentAttributeLayer<Value> layer = layers.get(i);
			if (layer instanceof PositionalTimeBasedLayer<Value> timelineLayer) {
				if (timeline == null) {
					timeline = new ArrayList<>();
					// Everything seen so far was a standard layer.
					standard = new ArrayList<>(layers.subList(0, i));
				}
				timeline.add(timelineLayer);
			} else if (standard != null) {
				standard.add(layer);
			}
		}

		if (timeline == null) {
			// No timeline layers: nothing to filter out, so reuse the original list as-is.
			return new TimelineLayerPartition<>(layers, layers, List.of());
		}
		return new TimelineLayerPartition<>(layers, List.copyOf(standard), List.copyOf(timeline));
	}

	/**
	 * Whether this partition was computed from the given list in its current state. Layer lists are fixed once
	 * a sampler exists, so this is only a cheap guard against reusing a stale split.
	 */
	public boolean isFor(List<EnvironmentAttributeLayer<Value>> layers) {
		return this.source == layers && this.sourceSize == layers.size();
	}

	/** The layers without any {@link PositionalTimeBasedLayer}, in their original order. */
	public List<EnvironmentAttributeLayer<Value>> standardLayers() {
		return this.standardLayers;
	}

	/** Only the {@link PositionalTimeBasedLayer}s, in their original order. Empty when nothing uses biome timelines. */
	public List<PositionalTimeBasedLayer<Value>> timelineLayers() {
		return this.timelineLayers;
	}
}
