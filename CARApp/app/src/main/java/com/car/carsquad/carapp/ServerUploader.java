package com.car.carsquad.carapp;

/**
 * Created by joshfreilich on 12/1/18.
 */



        import android.content.Context;
        import android.widget.Toast;

        import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by joshfreilich on 12/01/18.
 */

public class ServerUploader {

    private RequestQueue mRequestQueue;
    private String mServerResponse;
    private Context context;

    public ServerUploader(Context context) {
        this.context = context;
        mRequestQueue = Volley.newRequestQueue(context);
    }

    public void addRequest(String url, final HashMap<String, String> params) {
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        mServerResponse = response;
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mServerResponse = "error: " + error.getMessage();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                return params;
            }
        };
        mRequestQueue.add(postRequest);
    }

    public void show() {
        Toast.makeText(context, mServerResponse, Toast.LENGTH_LONG).show();
    }
}
