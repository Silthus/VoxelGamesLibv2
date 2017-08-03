package com.voxelgameslib.voxelgameslib.game;

import com.google.gson.*;
import com.google.inject.Injector;
import com.voxelgameslib.voxelgameslib.phase.Phase;
import lombok.extern.java.Log;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.Type;
import java.util.logging.Level;

/**
 * TypeAdapter for the Game class, redirects gson to the right Game implementation
 */
@Log
@Singleton
public class GameTypeAdapter implements JsonDeserializer<Phase>, JsonSerializer<Phase> {

    @Inject
    private Injector injector;

    @Override
    public Phase deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            JsonObject jsonObject = json.getAsJsonObject();

            // default path
            String name = jsonObject.get("className").getAsString();

            Class clazz = Class.forName(name);
            Phase phase = context.deserialize(json, clazz);
            injector.injectMembers(phase);
            return phase;
        } catch (Exception e) {
            log.log(Level.WARNING, "Could not deserialize phase:\n" + json.toString(), e);
        }
        return null;
    }

    @Override
    public JsonElement serialize(Phase src, Type typeOfSrc, JsonSerializationContext context) {
        return context.serialize(src, src.getClass());
    }
}
