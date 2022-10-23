package io.wispforest.owo.mixin.itemgroup;

import net.minecraft.item.ItemGroup;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemGroup.class)
public interface ItemGroupAccessor {

    @Mutable
    @Accessor("displayName")
    void owo$setDisplayName(Text displayName);

}