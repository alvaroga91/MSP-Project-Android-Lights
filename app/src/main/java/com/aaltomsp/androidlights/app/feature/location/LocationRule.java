package com.aaltomsp.androidlights.app.feature.location;

public class LocationRule {

    public enum LocationRuleActivationEvent {
        Approaching,
        Leaving
    }

    private int lightId;
    private boolean on;
    private LocationRuleActivationEvent activationEvent;
    private int hue;
    private int saturation;
    private int brightness;

    public LocationRule(int lightId) {
        this.lightId = lightId;
    }

    public LocationRule(int lightId, LocationRuleActivationEvent activationEvent) {
        this.lightId = lightId;
        this.activationEvent = activationEvent;
    }

    public int getLightId() {
        return lightId;
    }

    public void setLightId(int lightId) {
        this.lightId = lightId;
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    public LocationRuleActivationEvent getActivationEvent() {
        return activationEvent;
    }

    public void setActivationEvent(LocationRuleActivationEvent activationEvent) {
        this.activationEvent = activationEvent;
    }

    public int getHue() {
        return hue;
    }

    public void setHue(int hue) {
        this.hue = hue;
    }

    public int getSaturation() {
        return saturation;
    }

    public void setSaturation(int saturation) {
        this.saturation = saturation;
    }

    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }
}
