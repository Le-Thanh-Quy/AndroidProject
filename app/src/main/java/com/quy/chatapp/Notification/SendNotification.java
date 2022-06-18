package com.quy.chatapp.Notification;

import android.content.Context;
import android.os.StrictMode;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SendNotification {
    private static String BASE_URL = "https://fcm.googleapis.com/fcm/send";
    private static String SERVER_KEY = "key=AAAAU7MZJ2Q:APA91bEdAuS5DpPDV9slZjwrALdmR3CFs6gQyOhYL6nvrQPTzxIllF5_-U0Muil6-4l4IFLN4gaekCx_ELaq5PzRMLddQPSOi1gsElw7bXZOM-VHI1hJXbyKIvDPkjVUOlaaxg7-lxkQ";

    public static void send(Context context, String token, String title, String message, String id, String type, String image, boolean isGroup) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        RequestQueue queue = Volley.newRequestQueue(context);
        try {
            JSONObject json = new JSONObject();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("title", title);
            jsonObject.put("body", message);
            jsonObject.put("id", id);
            jsonObject.put("type", type);
            jsonObject.put("image", image);
            jsonObject.put("isGroup", isGroup);
            json.put("to", token);
            json.put("data", jsonObject);
            json.put("priority", "high");


            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, BASE_URL, json, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    System.out.println("FCM " + response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("Content-Type", "application/json");
                    params.put("Authorization", SERVER_KEY);
                    return params;
                }
            };
            queue.add(jsonObjectRequest);
        } catch (JSONException e) {
            System.out.println("AAAAAAA" + e);
            e.printStackTrace();
        }
    }

}
