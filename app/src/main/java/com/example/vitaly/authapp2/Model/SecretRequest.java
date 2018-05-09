package com.example.vitaly.authapp2.Model;

import android.util.Log;
import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class SecretRequest {
    OkHttpClient c = new OkHttpClient();
    String user, password, id;

    public SecretRequest(String user, String password, String id) {
        this.user = user;
        this.password = password;
        this.id = id;
    }

    public String doWork() {

        String url = "http://authapp2-authapp2.7e14.starter-us-west-2.openshiftapps.com/";

        HttpUrl.Builder hb = HttpUrl.parse(url).newBuilder();

        hb.addQueryParameter("id", id);
        hb.addQueryParameter("username", user);
        hb.addQueryParameter("password", password);

        Request r = new Request.Builder().url(hb.build()).build();

        Response re = null;
        try {
            re = c.newCall(r).execute();
            if(re.code() == 200)
                return re.body().string();

        } catch (IOException e) {
            Log.e("ERROR", "Error in getting response");
        }
        return null;
    }
}