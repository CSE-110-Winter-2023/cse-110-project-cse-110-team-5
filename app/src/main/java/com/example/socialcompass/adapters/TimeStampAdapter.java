package com.example.socialcompass.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.time.Instant;

/**
 * Used to convert created_at and updated_at long values into strings for
 * serialization
 */
public class TimeStampAdapter extends TypeAdapter<Long> {
    @Override
    public void write(JsonWriter out, Long value) throws java.io.IOException {
        var instant = Instant.ofEpochSecond(value);
        out.value(instant.toString());
    }

    @Override
    public Long read(JsonReader in) throws java.io.IOException {
        var instant = Instant.parse(in.nextString());
        return instant.getEpochSecond();
    }
}