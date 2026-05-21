package com.blog.service;

public interface StreamCallback {
    void onStart();
    void onMessage(String message);
    void onComplete();
    void onError(Throwable t);
}
