package com.sanmar.educateyourparrots;

import net.minecraft.sound.SoundEvent;
import java.util.List;

public interface ParrotSoundMemory {
    int MAX_SOUNDS = 64;
    List<SoundEvent> getLearnedSounds();
    boolean addLearnedSound(SoundEvent sound);

    int soundInteractTimer = 0;

    int getSoundInteractTimer();

}
