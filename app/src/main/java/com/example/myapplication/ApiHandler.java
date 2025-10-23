package com.example.myapplication;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class ApiHandler {
    private static ApiHandler instance;
    private RequestQueue requestQueue;
    private static Context ctx;

    private ApiHandler(Context context) {
        ctx = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized ApiHandler getInstance(Context context) {
        if (instance == null) {
            instance = new ApiHandler(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
