package com.example.myapplication.bean;

import com.google.gson.annotations.SerializedName;

public class PostVideoResponse {

    // TODO-C2 (3) Implement your PostVideoResponse Bean here according to the response json
    @SerializedName("success")
    private boolean success;
    @SerializedName("url")
    private String url;

    public boolean isSuccess() {
        return success;
    }

    public String  getUrl() {
        return url;
    }
}

