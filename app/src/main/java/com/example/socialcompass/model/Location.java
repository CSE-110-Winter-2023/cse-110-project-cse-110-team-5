package com.example.socialcompass.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.socialcompass.adapters.TimeStampAdapter;
import com.google.gson.Gson;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

@Entity(tableName = "location_items")
public class Location {

    public Location(
            @NonNull String publicCode,
            @NonNull String privateCode,
            @NonNull String label,
            @NonNull float latitude,
            @NonNull float longitude,
            @NonNull boolean listedPublicly,
            @NonNull long createdAt,
            @NonNull long updatedAt
    ) {
        this.publicCode = publicCode;
        this.privateCode = privateCode;
        this.label = label;
        this.latitude = latitude;
        this.longitude = longitude;
        this.listedPublicly = listedPublicly;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @NonNull
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "public_code")
    @SerializedName("public_code")
    public String publicCode;

    @NonNull
    @SerializedName("private_code")
    @ColumnInfo(name = "private_code")
    public String privateCode;

    @NonNull
    @SerializedName("label")
    @ColumnInfo(name = "label")
    public String label;

    @NonNull
    @SerializedName("latitude")
    @ColumnInfo(name = "latitude")
    public float latitude;

    @NonNull
    @SerializedName("longitude")
    @ColumnInfo(name = "longitude")
    public float longitude;

    @NonNull
    @SerializedName("is_listed_publicly")
    @ColumnInfo(name = "is_listed_publicly")
    public boolean listedPublicly = true;

    @NonNull
    @JsonAdapter(TimeStampAdapter.class)
    @SerializedName("created_at")
    @ColumnInfo(name = "created_at")
    public long createdAt = 0;

    @NonNull
    @JsonAdapter(TimeStampAdapter.class)
    @SerializedName("updated_at")
    @ColumnInfo(name = "updated_at")
    public long updatedAt = 0;

    public static Location fromJSON(String json) {
        return new Gson().fromJson(json, Location.class);
    }

    public String toJSON() {
        return new Gson().toJson(this);
    }
}
