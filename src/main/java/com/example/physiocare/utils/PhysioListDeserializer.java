package com.example.physiocare.utils;

import com.example.physiocare.models.physio.Physio;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PhysioListDeserializer implements JsonDeserializer<List<Physio>> {
    @Override
    public List<Physio> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        List<Physio> physios = new ArrayList<>();
        if (json.isJsonArray()) {
            for (JsonElement element : json.getAsJsonArray()) {
                physios.add(context.deserialize(element, Physio.class));
            }
        } else if (json.isJsonObject()) {
            physios.add(context.deserialize(json, Physio.class));
        } else {
            throw new JsonParseException("Unexpected JSON type for physios");
        }
        return physios;
    }
}