package com.example.myapplication.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FeedResponse {
    // TODO-C2 (2) Implement your FeedResponse Bean here according to the response json
    @SerializedName("success")
    private boolean success;
    @SerializedName("feeds")
    private List<Feed> feeds;

    public boolean isSuccess() {
        return success;
    }

    public List<Feed> getFeeds() {
        return feeds;
    }
}
