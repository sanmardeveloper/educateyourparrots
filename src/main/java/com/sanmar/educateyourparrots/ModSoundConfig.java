package com.sanmar.educateyourparrots;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ModSoundConfig {
    public static double parrotsVolume = 0.6;

    private static final File CONFIG_FILE = new File(
            FabricLoader.getInstance().getConfigDir().toFile(),
            "educateyourparrots.json"
    );

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            JsonObject json = new JsonObject();
            json.addProperty("parrotsVolume", parrotsVolume);

            GSON.toJson(json, writer);
        } catch (IOException e) {
            System.err.println("[EducateYourParrots] Не удалось сохранить файл конфигурации!");
            e.printStackTrace();
        }
    }

    public static void load() {
        if (!CONFIG_FILE.exists()) {
            save();
            return;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

            if (json.has("parrotsVolume")) {
                parrotsVolume = json.get("parrotsVolume").getAsDouble();
            }
        } catch (Exception e) {
            System.err.println("[EducateYourParrots] Cannot load saved value using default.");
            e.printStackTrace();
        }
    }
}
