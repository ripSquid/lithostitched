package dev.worldgen.lithostitched.mixin.common.timeline;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.worldgen.lithostitched.impl.worldgen.attribute.PositionalTimeBasedLayer;
import dev.worldgen.lithostitched.impl.worldgen.attribute.TimelineLayerPartition;
import net.minecraft.world.attribute.*;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(EnvironmentAttributeSystem.ValueSampler.class)
public class EnvironmentAttributeSystemValueSamplerMixin<Value> {
	@Shadow
	@Final
	private List<EnvironmentAttributeLayer<Value>> layers;
	
	@Shadow
	private int cacheTickId;
	
	/**
	 * The layers split into standard and timeline layers. Computed lazily and reused: samplers are queried
	 * thousands of times per tick (e.g. once per mob while counting spawns) and their layers never change.
	 * The partition is immutable, so it is safe to publish through a plain field.
	 */
	@Unique
	private TimelineLayerPartition<Value> lithostitched$partition;
	
	@Unique
	private TimelineLayerPartition<Value> lithostitched$partition() {
		TimelineLayerPartition<Value> partition = this.lithostitched$partition;
		if (partition == null || !partition.isFor(this.layers)) {
			partition = TimelineLayerPartition.of(this.layers);
			this.lithostitched$partition = partition;
		}
		return partition;
	}
	
	@Definition(id = "layers", field = "Lnet/minecraft/world/attribute/EnvironmentAttributeSystem$ValueSampler;layers:Ljava/util/List;")
	@Expression("this.layers")
	@ModifyExpressionValue(
		method = {
			"computeValuePositional",
			"computeValueNotPositional"
		},
		at = @At("MIXINEXTRAS:EXPRESSION")
	)
	private List<EnvironmentAttributeLayer<Value>> removeBiomeTimelineLayers(List<EnvironmentAttributeLayer<Value>> layers) {
		return this.lithostitched$partition().standardLayers();
	}
	
	@WrapOperation(
		method = "computeValuePositional",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/attribute/EnvironmentAttribute;sanitizeValue(Ljava/lang/Object;)Ljava/lang/Object;"
		)
	)
	private Value applyBiomeTimelineLayers(EnvironmentAttribute<Value> attribute, Value result, Operation<Value> operation, @Local(ordinal = 0) Vec3 pos, @Local(ordinal = 0) SpatialAttributeInterpolator biomeInterpolator) {
		List<PositionalTimeBasedLayer<Value>> timelineLayers = this.lithostitched$partition().timelineLayers();
		for (int i = 0, size = timelineLayers.size(); i < size; i++) {
			result = timelineLayers.get(i).applyPositionalTimeBased(result, this.cacheTickId, pos, biomeInterpolator);
		}
		return operation.call(attribute, result);
	}
}
