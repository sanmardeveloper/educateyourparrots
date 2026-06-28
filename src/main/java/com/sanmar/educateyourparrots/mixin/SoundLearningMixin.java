package com.sanmar.educateyourparrots.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Set;

@Mixin(ServerWorld.class)
public class SoundLearningMixin {
    @Unique
    private static final Logger LOGGER =
            LoggerFactory.getLogger("educateyourparrots");
    @Unique
    private static final double LEARNING_RADIUS = 8.0;

    @Unique
    private static final Set<SoundCategory> IGNORED_CATEGORIES = Set.of(
            SoundCategory.WEATHER,
            SoundCategory.RECORDS,
            SoundCategory.PLAYERS,
            SoundCategory.MUSIC
    );

    @Inject(method = "playSound", at = @At("HEAD"))
    private void onPlaySound(Entity source, double x, double y, double z, RegistryEntry<SoundEvent> sound, SoundCategory category, float volume, float pitch, long seed, CallbackInfo ci) {
        if (sound == null
                || IGNORED_CATEGORIES.contains(category)
                || sound.value()
                .id()
                .getPath()
                .startsWith("entity.parrot")) {

            return;
        }

        ServerWorld world = (ServerWorld)(Object)this;

        teachParrotsToSound(world, sound.value(), x, y, z);
    }

    @Inject(method = "playSoundFromEntity", at = @At("HEAD"))
    private void onPlaySound(Entity source, Entity entity, RegistryEntry<SoundEvent> sound, SoundCategory category, float volume, float pitch, long seed, CallbackInfo ci) {
        if (sound == null
                || IGNORED_CATEGORIES.contains(category)
                || sound.value()
                .id()
                .getPath()
                .startsWith("entity.parrot")) {
            return;
        }

        ServerWorld world = (ServerWorld)(Object)this;

        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();

        teachParrotsToSound(world, sound.value(), x, y, z);
    }

    @Unique
    private static void teachParrotsToSound(ServerWorld world, SoundEvent sound, double x, double y, double z) {

        Box searchBox = getBox(x, y, z);

        List<ParrotEntity> nearbyParrots = world.getEntitiesByClass(
                ParrotEntity.class,
                searchBox,
                p -> true
        );

        //for (ParrotEntity parrot : nearbyParrots) {
        //    if (((ParrotSoundMemory) parrot).addLearnedSound(sound)) {
        //        logLearnedSounds(parrot.getName().getString(), world, x, y, z, sound);
        //    }

        //}
    }


    @Unique
    private static @NotNull Box getBox(double x, double y, double z) {
        return new Box(
                x - LEARNING_RADIUS, y - LEARNING_RADIUS, z - LEARNING_RADIUS,
                x + LEARNING_RADIUS, y + LEARNING_RADIUS, z + LEARNING_RADIUS
        );
    }

    @Unique
    private static void logLearnedSounds(
            String parrotName,
            ServerWorld world,
            double x,
            double y,
            double z,
            SoundEvent sound
    ) {
        LOGGER.info(
                "Parrot {} at {} {} {} in world - {} just learned {}",
                parrotName,
                (int) x,
                (int) y,
                (int) z,
                world.getRegistryKey().getValue().getPath(),
                sound.id()
        );
    }
}
