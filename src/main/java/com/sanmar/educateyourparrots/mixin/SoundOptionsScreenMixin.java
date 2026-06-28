package com.sanmar.educateyourparrots.mixin;

import com.sanmar.educateyourparrots.ModSoundConfig;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.option.SoundOptionsScreen;
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameOptionsScreen.class)
public class SoundOptionsScreenMixin {

    @Shadow protected OptionListWidget body;

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        if (!((Object) this instanceof SoundOptionsScreen)) {
            return;
        }

        SimpleOption<Double> customVolumeOption = new SimpleOption<>(
                "options.educateyourparrots.custom_volume",
                SimpleOption.emptyTooltip(),
                (text, value) -> {
                    if (value == 0.0) {
                        return text.copy().append(": ").append(Text.translatable("options.off"));
                    }
                    return text.copy().append(": " + (int)(value * 100.0) + "%");
                },
                SimpleOption.DoubleSliderCallbacks.INSTANCE,
                ModSoundConfig.parrotsVolume,
                value -> {
                    ModSoundConfig.parrotsVolume = value;
                    ModSoundConfig.save();
                }
        );

        if (this.body != null) {
            this.body.addSingleOptionEntry(customVolumeOption);
        }
    }
}
