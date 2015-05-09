package com.aaltomsp.androidlights.app.feature.notification;

import com.aaltomsp.androidlights.app.Light;
/**
 * Created by Nick on 30/03/15.
 */
public class Notification {

    public static final String NO_EFFECT = "none";
    public static final String ONE_BREATHE_CYCLE = "select";
    public static final String LOOP_BREATHE_CYCLE = "lselect";
    public static final String FACEBOOK = "FACEBOOK";
    public static final String FACEBOOK_PKGNAME = "com.facebook.katana";
    public static final String GMAIL = "GMAIL";
    public static final String GMAIL_PKGNAME = "com.google.android.gm";
    public static final String TWITTER = "TWITTER";
    public static final String TWITTER_PKGNAME = "com.twitter.android";        //TODO: fill pkgnames
    public static final String INSTAGRAM = "INSTAGRAM";
    public static final String INSTAGRAM_PKGNAME = "com.instagram.android";
    public static final String WHATSAPP = "WHATSAPP";
    public static final String WHATSAPP_PKGNAME = "com.whatsapp";

    public String name;
    private Light light;
    private String app;
    private int brightness;
    private String frequency;
    private int hue;
    private int sat;
    private boolean isOn; //            !!-- "Notification On" != "Bulb On" --!!

    public Notification(String name, Light light, int brightness, String frequency, int hue, int sat) {
        this.name = name;
        this.brightness = brightness;
        this.frequency = frequency;
        this.hue = hue;
        this.sat = sat;
        this.light = light;
        this.isOn = true; //when Notification created, automatically set as ON

        switch (name) {
            case FACEBOOK:
                this.app = FACEBOOK_PKGNAME;
                break;
            case GMAIL:
                this.app = GMAIL_PKGNAME;
                break;
            case TWITTER:
                this.app = TWITTER_PKGNAME;
                break;
            case INSTAGRAM:
                this.app = INSTAGRAM_PKGNAME;
                break;
            case WHATSAPP:
                this.app = WHATSAPP_PKGNAME;
                break;
            default:
                this.app = null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Notification that = (Notification) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public void setOn(boolean isOn) {
        this.isOn = isOn;
    }

    public boolean isOn() {
        return isOn;
    }

    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(int brightness) { this.brightness = brightness; }

    public String getName() {
        return name;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String f) { this.frequency = f; }

    public int getHue() {
        return hue;
    }

    public void setHue(int h) { this.hue = h; }

    public int getSat() {
        return sat;
    }

    public void setSat(int s) { this.sat = s; }

    public Light getLight() {
        return light;
    }

    public String getPkgName() {
        return app;
    }
}
