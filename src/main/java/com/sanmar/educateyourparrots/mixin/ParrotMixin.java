package com.sanmar.educateyourparrots.mixin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.sanmar.educateyourparrots.ModSoundConfig;
import com.sanmar.educateyourparrots.ParrotSoundMemory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.minecraft.entity.passive.ParrotEntity.getSoundPitch;

@Mixin(ParrotEntity.class)
public class ParrotMixin implements ParrotSoundMemory {


    @Unique
    private final List<SoundEvent> learnedSounds = new ArrayList<>();
    @Unique
    int soundInteractTimer = 0;

    @Override
    public List<SoundEvent> getLearnedSounds() {
        return this.learnedSounds;
    }

    @Override
    public boolean addLearnedSound(SoundEvent sound) {

        if (learnedSounds.contains(sound))
            return false;

        if (learnedSounds.size() >= MAX_SOUNDS)
            learnedSounds.removeFirst();

        learnedSounds.add(sound);
        return true;
    }

    @Override
    public int getSoundInteractTimer() {
        return this.soundInteractTimer;
    }



    @Inject(
            method = "imitateNearbyMob",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void imitateNearbyMob(World world, Entity parrot, CallbackInfoReturnable<Boolean> cir) {
        if (!parrot.isAlive() || parrot.isSilent()) {
            cir.setReturnValue(false);
            return;
        }

        if (world.random.nextInt(2) != 0) {
            cir.setReturnValue(false);
            return;
        }

        List<SoundEvent> sounds = ((ParrotSoundMemory) parrot).getLearnedSounds();

        if (sounds.isEmpty()) {
            cir.setReturnValue(false);
            return;
        }

        SoundEvent sound = sounds.get(world.random.nextInt(sounds.size()));
        parrot.playSound(sound, (float) ModSoundConfig.parrotsVolume, getSoundPitch(world.random));

        cir.setReturnValue(true);
    }

    @Inject(method = "interactMob", at = @At("HEAD"))
    private void speak(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (!player.isSneaking() || hand != Hand.MAIN_HAND || !player.getMainHandStack().isEmpty()) {
            return;
        }

        if (getSoundInteractTimer() < 600) {
            player.sendMessage(Text.translatable("educate_your_parrots.in_game_texts.parrot_cannot_make_sounds", 30 - (soundInteractTimer / 20)), true);
            return;
        }



        soundInteractTimer = 0;
        ParrotEntity parrot = (ParrotEntity)(Object)this;

        ParrotEntity.imitateNearbyMob(
                parrot.getEntityWorld(),
                parrot
        );
    }

    @Inject(method = "tickMovement", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        soundInteractTimer++;
    }


    @Inject(method = "writeCustomData", at = @At("TAIL"))
    private void writeCustomSounds(WriteView view, CallbackInfo ci) {
        view.put("LearnedSounds", SoundEvent.CODEC.listOf(), this.learnedSounds);

        view.put("SoundInteractTimer", Codec.INT, this.soundInteractTimer);
    }

    @Inject(method = "readCustomData", at = @At("TAIL"))
    private void readCustomSounds(ReadView view, CallbackInfo ci) {
        this.learnedSounds.clear();

        view.<List<SoundEvent>>read("LearnedSounds", SoundEvent.CODEC.listOf())
                .ifPresent(this.learnedSounds::addAll);

        this.soundInteractTimer = view.read("SoundInteractTimer", Codec.INT).orElse(1);
    }

}
