package com.aaltomsp.androidlights.app;

public class Light {
    private final static String LIGHT_SEPARATOR = "_1/3#3/7_";

    private int id;
    private int hue;
    private int saturation;
    private int brightness;
    private boolean on;
    private String name;

    public Light(int id, String name, boolean on, int brightness, int hue, int saturation) {
        this.id = id;
        this.name = name;
        this.on = on;
        this.brightness = brightness;
        this.hue = hue;
        this.saturation = saturation;
    }

    public int getSaturation() {
        return saturation;
    }

    public void setSaturation(int saturation) {
        this.saturation = saturation;
    }

    public int getHue() {
        return hue;
    }

    public void setHue(int hue) {
        this.hue = hue;
    }

    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Light clone() {
        return new Light(id, name, on, brightness, hue, saturation);
    }

    @Override
    public String toString() {
        String string = "Light[";
        string += "id=" + id;
        string += ", name=" + name;
        string += ", on=" + on;
        string += ", brightness=" + brightness;
        string += ", hue=" + hue;
        string += ", saturation=" + saturation;
        string += "]";
        return string;
    }

    public static Light fromString(String string) {
        string = string.replace("Light[" , "");
        string = string.replace("]" , "");
        String[] split = string.split(", ");

        int _id = Integer.valueOf(split[0].replace("id=", ""));
        String _name = split[1].replace("name=", "");
        boolean _on = Boolean.valueOf(split[2].replace("on=", ""));
        int _bri = Integer.valueOf(split[3].replace("brightness=", ""));
        int _hue = Integer.valueOf(split[4].replace("hue=", ""));
        int _sat = Integer.valueOf(split[5].replace("saturation=", ""));

        return new Light(_id, _name, _on, _bri, _hue, _sat);
    }

    public static String serialize(Light[] lights) {
        if (lights == null) {
            return "";
        }
        String lightsString = "";
        for (int i = 0; i < lights.length; i++) {
            Light light = lights[i];
            if (i > 0) {
                lightsString += LIGHT_SEPARATOR;
            }
            lightsString += light.toString();
        }
        return lightsString;
    }

    public static Light[] deserialize(String string) {
        if (string == null || "".equals(string)) {
            return new Light[0];
        }
        String[] split = string.split(LIGHT_SEPARATOR);
        Light[] lights = new Light[split.length];
        for (int i = 0; i < lights.length; i++) {
            lights[i] = fromString(split[i]);
        }
        return lights;
    }
}
