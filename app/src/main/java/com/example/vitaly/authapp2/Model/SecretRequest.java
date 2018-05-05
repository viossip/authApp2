package com.example.vitaly.authapp2.Model;

import android.util.Log;
import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class SecretRequest {
    OkHttpClient client = new OkHttpClient();
    String user, password, id;

    public SecretRequest(String user, String password, String id) {
        this.user = user;
        this.password = password;
        this.id = id;
    }

    public String doWork() {

        String url = "http://authapp2-authapp2.7e14.starter-us-west-2.openshiftapps.com/";

        HttpUrl.Builder httpBuider = HttpUrl.parse(url).newBuilder();

        httpBuider.addQueryParameter("id", id);
        httpBuider.addQueryParameter("username", user);
        httpBuider.addQueryParameter("password", password);

        Request request = new Request.Builder().url(httpBuider.build()).build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
            if(response.code() == 200)
                return response.body().string();

        } catch (IOException e) {
            Log.e("ERROR", "error in getting response get request with query string okhttp");
        }
        return null;
    }
}