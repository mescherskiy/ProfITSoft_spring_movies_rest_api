package com.example.springrest.service.utils;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public class GenreAdapter implements JsonDeserializer<Set<String>>, JsonSerializer<Set<String>> {

    @Override
    public Set<String> deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        Set<String> genre = new HashSet<>();
        if (!json.isJsonArray()) {
            return null;
        }
        JsonArray jsonArray = json.getAsJsonArray();
        for (JsonElement element : jsonArray) {
            genre.add(element.getAsString());
        }
        return genre;
    }

    @Override
    public JsonElement serialize(Set<String> genres, Type type, JsonSerializationContext context) {
        JsonArray jsonArray = new JsonArray();
        for (String genre : genres) {
            jsonArray.add(genre);
        }
        return jsonArray;
    }
}
