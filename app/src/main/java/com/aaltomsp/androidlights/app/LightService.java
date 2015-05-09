package com.aaltomsp.androidlights.app;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LightService {
    private static final String DEVELOPER_KEY = "f16e8d43e35ba8f374162fc3ea28bf7";
    private List<ExpandableLightAdapter> lightAdapters;

    public enum Action {
        GET_LIGHTS,
        FIND_NEW_LIGHTS,
        SET_STATE
    }

    private static LightService instance;
    private ArrayList<Light> lights;

    public static LightService getInstance() {
        if (instance == null) {
            instance = new LightService();
        }
        return instance;
    }

    private LightService() {
        lights = new ArrayList<Light>();
        lightAdapters = new ArrayList<ExpandableLightAdapter>();
    }

    public void refreshLights(SharedPreferences sharedPreferences) {
        Log.d(LightService.class.getSimpleName(), "Sending GET_LIGHTS request.");
        new HttpRequestTask(Action.GET_LIGHTS, null, null, sharedPreferences).execute();
    }

    public void findNewLights(SharedPreferences sharedPreferences) {
        Log.d(LightService.class.getSimpleName(), "Sending FIND_NEW_LIGHTS request.");
        new HttpRequestTask(Action.FIND_NEW_LIGHTS, null, null, sharedPreferences).execute();
    }

    public void setLightState(SharedPreferences sharedPreferences, int lightId, Bundle extras) {
        Log.d(LightService.class.getSimpleName(), "Sending SET_STATE request.");
        new HttpRequestTask(Action.SET_STATE, lightId, extras, sharedPreferences).execute();
    }

    public void addAdapterToBeNotifiedOnNewLights(ExpandableLightAdapter lightAdapter) {
        lightAdapters.add(lightAdapter);
    }

    public List<Light> getLights() {
        return lights;
    }

    public String getLightName(int id) {
        return getLightById(id).getName();
    }

    public String[] getLightNames() {
        return getLightNames(lights);
    }

    public String[] getLightNames(List<Light> lightList) {
        String[] lightNames = new String[lightList.size()];
        for (int i = 0; i < lightList.size(); i++) {
            Light light = lightList.get(i);
            if (light.getName() != null) {
                lightNames[i] = light.getName();
            } else {
                lightNames[i] = "Light " + light.getId();
            }
        }
        return lightNames;
    }

    public Bundle createExtras(Boolean on, Integer hue, Integer brightness, Integer saturation, String alert) {
        Bundle extras = new Bundle();
        extras.putBoolean("on", on);
        extras.putInt("brightness", brightness);
        extras.putInt("saturation", saturation);
        extras.putInt("hue", hue);
        extras.putString("alert", alert);
        return extras;
    }

    private Light getLightById(int lightId) {
        for (Light light : lights) {
            if (light.getId() == lightId) {
                return light;
            }
        }
        return null;
    }

    private void replaceLights(String response) {
        ArrayList<Light> tempLights = new ArrayList<Light>();
        try {
            JSONObject responseJSON = new JSONObject(response);
            for (int i = 1; i <= responseJSON.length(); i++) {
                JSONObject lightJSON = responseJSON.getJSONObject(i + "");
                JSONObject state = lightJSON.getJSONObject("state");
                boolean on = state.getBoolean("on");
                int brightness = state.getInt("bri");
                int hue = state.getInt("hue");
                int saturation = state.getInt("sat");
                String name = null;
                if (lightJSON.has("name")) {
                    name = lightJSON.getString("name");
                }
                Light light = new Light(i, name, on, brightness, hue, saturation);
                tempLights.add(light);
            }
        } catch (JSONException e) {
            Log.e(getClass().getSimpleName(), "Failed to parse response to GET_LIGHTS.");
            return;
        }
        lights = tempLights;
        for (ExpandableLightAdapter  lightAdapter : lightAdapters) {
            lightAdapter.notifyDataSetChanged();
        }
    }

    private void updateLight(String response, int lightId) {
        Light light = getLightById(lightId);
        String successKeyString = "/lights/" + lightId + "/state/";
        Boolean on = null;
        Integer brightness = null;
        Integer saturation = null;
        Integer hue = null;
        try {
            JSONArray updateJSON = new JSONArray(response);
            for (int i = 0; i < updateJSON.length(); i++) {
                JSONObject valueUpdateJSON = updateJSON.getJSONObject(i);
                if (valueUpdateJSON.has("success")) {
                    JSONObject successJSON = valueUpdateJSON.getJSONObject("success");
                    if (successJSON.has(successKeyString + "sat")) {
                        saturation = successJSON.getInt(successKeyString + "sat");
                    } else if (successJSON.has(successKeyString + "bri")) {
                        brightness = successJSON.getInt(successKeyString + "bri");
                    } else if (successJSON.has(successKeyString + "hue")) {
                        hue = successJSON.getInt(successKeyString + "hue");
                    } else if (successJSON.has(successKeyString + "on")) {
                        on = successJSON.getBoolean(successKeyString + "on");
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(getClass().getSimpleName(), "Failed to parse response to SET_STATE.");
            return;
        }
        if (on != null) {
            light.setOn(on);
        }
        if (brightness != null) {
            light.setBrightness(brightness);
        }
        if (hue != null) {
            light.setHue(hue);
        }
        if (saturation != null) {
            light.setSaturation(saturation);
        }
    }

    public class HttpRequestTask extends AsyncTask<Void, Void, Boolean> {
        private final long timeout;
        private final String url;
        private final HttpMethod method;
        private final Action action;
        private final Bundle extras;
        private final Integer lightId;
        private String response;
        private SharedPreferences sharedPreferences;

        /**
         * @param action  Action which decides what request to send
         * @param lightId Id specifying which light the request is for
         * @param extras  Parameters of a light when changed
         */
        public HttpRequestTask(Action action, Integer lightId, Bundle extras, SharedPreferences sharedPreferences) {
            if (action == null) {
                throw new IllegalArgumentException("Action must be given when creating HttpRequestTask.");
            }
            this.action = action;
            this.extras = extras;
            this.lightId = lightId;
            this.sharedPreferences = sharedPreferences;
            response = null;
            switch (action) {
                case GET_LIGHTS:
                    method = HttpMethod.GET;
                    timeout = 5 * 1000;
                    url = getBaseURL() + "lights";
                    break;
                case FIND_NEW_LIGHTS:
                    method = HttpMethod.POST;
                    timeout = (60 + 5) * 1000;
                    url = getBaseURL() + "lights/new";
                    break;
                case SET_STATE:
                    if (lightId == null) {
                        throw new IllegalArgumentException("Missing lightId.");
                    }
                    if (extras == null) {
                        throw new IllegalArgumentException("Missing json content.");
                    }
                    method = HttpMethod.PUT;
                    timeout = 5 * 1000;
                    url = getBaseURL() + "lights/" + lightId + "/state";
                    break;
                default:
                    throw new IllegalArgumentException("Invalid action.");
            }
        }

        private String getBaseURL() {
            return "http://" + sharedPreferences.getString("ip", "localhost") + ":" + sharedPreferences.getInt("port", 80) + "/api/" + DEVELOPER_KEY + "/";
        }

        /**
         * Forms the HTTP request, makes the call to the Lights API and then retrieves the response.
         *
         * @param url
         * @return The json response
         */
        private String sendRequest(String url) {
            DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
            HttpResponse httpResponse = null;

            switch (method) {
                case GET:
                    HttpGet httpGet = new HttpGet(url);
                    try {
                        httpResponse = httpClient.execute(httpGet);
                    } catch (IOException e) {
                        Log.e(getClass().getSimpleName(), "Failed to execute HTTP GET with url: " + url);
                        return null;
                    }
                    break;
                case POST:
                    try {
                        httpResponse = executeHttpPostOrPut(httpClient, new HttpPost(url));
                    } catch (Exception e) {
                        Log.e(getClass().getSimpleName(), "Failed to execute HTTP POST with url: " + url);
                        return null;
                    }
                    break;
                case PUT:
                    try {
                        httpResponse = executeHttpPostOrPut(httpClient, new HttpPut(url));
                    } catch (Exception e) {
                        Log.e(getClass().getSimpleName(), "Failed to execute HTTP PUT with url: " + url);
                        return null;
                    }
                    break;
                default:
                    return null;
            }

            InputStream inputStream = null;
            HttpEntity entity = httpResponse.getEntity();
            try {
                inputStream = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                StringBuilder jsonBuilder = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    jsonBuilder.append(line + "\n");
                }
                return jsonBuilder.toString();
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), "Failed to read response.");
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Exception e) {
                    }
                }
            }
            return null;
        }

        private HttpResponse executeHttpPostOrPut(HttpClient httpClient, HttpEntityEnclosingRequestBase httpPostOrPut) throws RuntimeException {
            httpPostOrPut.setHeader("Accept", "application/json");
            httpPostOrPut.setHeader("Content-type", "application/json");
            try {
                if (extras != null) {
                    StringEntity requestJSON = createRequestJSON();
                    if (requestJSON == null) {
                        throw new RuntimeException("Invalid HttpRequestTask data.");
                    }
                    httpPostOrPut.setEntity(requestJSON);
                }
                return httpClient.execute(httpPostOrPut);
            } catch (Exception e) {
                throw new RuntimeException("Failed to execute HTTP POST or PUT.");
            }
        }

        private StringEntity createRequestJSON() {
            JSONObject request = new JSONObject();
            try {
                request.put("on", extras.getBoolean("on"));
                request.put("bri", extras.getInt("brightness"));
                request.put("hue", extras.getInt("hue"));
                request.put("sat", extras.getInt("saturation"));
                request.put("alert", extras.getString("alert"));
            } catch (JSONException e) {
                Log.d(getClass().getSimpleName(), "Failed to generate HttpRequestTask for request due to invalid HttpRequestTask content.");
                return null;
            }
            StringEntity requestJSON = null;
            try {
                requestJSON = new StringEntity(request.toString());
            } catch (UnsupportedEncodingException e) {
                Log.e(getClass().getSimpleName(), "Failed to generate HttpRequestTask for request due to unsupported encoding.");
                return null;
            }
            requestJSON.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            return requestJSON;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            try {
                if (get(timeout, TimeUnit.MILLISECONDS)) {
                    switch (action) {
                        case GET_LIGHTS:
                            replaceLights(response);
                            break;
                        case FIND_NEW_LIGHTS:
                            refreshLights(sharedPreferences);
                            break;
                        case SET_STATE:
                            updateLight(response, lightId);
                            break;
                        default:
                            break;
                    }
                } else {
                    LightAPIResponseEventBroadcaster.getInstance().broadcast("No or bad response from server. Check app settings and your network connection.");
                }
            } catch (Exception e) {
                Log.i(getClass().getSimpleName(), "No response received after " + (timeout / 1000) + " seconds.");
                LightAPIResponseEventBroadcaster.getInstance().broadcast("Request timed out. Check app settings and your network connection.");
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            long startTimeMillis = System.currentTimeMillis();
            do {
                try{
                    response = sendRequest(url);
                } catch (Exception e) {
                    return false;
                }
                if (System.currentTimeMillis() - startTimeMillis >= timeout) {
                    return false;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            } while (response == null);
            return true;
        }
    }
}
