package io.wispforest.owo.mixin.ui;

import com.mojang.blaze3d.vertex.MatrixStack;
import com.mojang.math.Axis;
import io.wispforest.owo.util.pond.OwoEntityRenderDispatcherExtension;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Text;
import net.minecraft.world.entity.Entity;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin<T extends Entity> {

    @Shadow
    @Final
    protected EntityRenderDispatcher entityRenderDispatcher;

    @Inject(method = "renderNameTag", at = @At("HEAD"), cancellable = true)
    private void cancelLabel(T entity, Text text, MatrixStack matrices, MultiBufferSource vertexConsumers, int light, float tickDelta, CallbackInfo ci) {
        if (((OwoEntityRenderDispatcherExtension) this.entityRenderDispatcher).owo$showNametag()) return;
        ci.cancel();
    }

    @Inject(method = "renderNameTag", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/MatrixStack;rotate(Lorg/joml/Quaternionf;)V", shift = At.Shift.AFTER))
    private void adjustLabelRotation(T entity, Text text, MatrixStack matrices, MultiBufferSource vertexConsumers, int light, float tickDelta, CallbackInfo ci) {
        if (!((OwoEntityRenderDispatcherExtension) this.entityRenderDispatcher).owo$counterRotate()) return;

        matrices.rotate(new Quaternionf(this.entityRenderDispatcher.cameraOrientation()).invert());
        matrices.rotate(Axis.YP.rotationDegrees(180));
    }

}
