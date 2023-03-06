package com.example.socialcompass.model;

public class LocationBuilder {
    private String publicCode = null;
    private String privateCode = null;
    private String label = null;
    private float latitude = 0;
    private float longitude = 0;
    private boolean listedPublicly = true;
    private long createdAt = 0;
    private long updatedAt = 0;

    public LocationBuilder setPublicCode(String publicCode) {
        this.publicCode = publicCode;
        return this;
    }

    public LocationBuilder setPrivateCode(String privateCode) {
        this.privateCode = privateCode;
        return this;
    }

    public LocationBuilder setLabel(String label) {
        this.label = label;
        return this;
    }

    public LocationBuilder setListedPublicly(boolean listedPublicly) {
        this.listedPublicly = listedPublicly;
        return this;
    }

    public LocationBuilder setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public LocationBuilder setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public LocationBuilder setLatitude(float latitude) {
        this.latitude = latitude;
        return this;
    }

    public LocationBuilder setLongitude(float latitude) {
        this.latitude = latitude;
        return this;
    }

    /* will delete later; just here as a template to copy and paste :O
    public Location copyLocation(Location location) {
        return this.setPublicCode(location.publicCode)
                .setPrivateCode(location.privateCode)
                .setLabel(location.label)
                .setLatitude(location.latitude)
                .setLongitude(location.longitude)
                .setListedPublicly(location.listedPublicly)
                .setCreatedAt(location.createdAt)
                .setUpdatedAt(location.updatedAt)
                .build();
    }
     */

    public Location build() {
        return new Location(publicCode, privateCode, label, latitude, longitude, listedPublicly, createdAt, updatedAt);
    }
}


