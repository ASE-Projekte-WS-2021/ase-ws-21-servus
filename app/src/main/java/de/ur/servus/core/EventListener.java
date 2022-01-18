package de.ur.servus.core;

public interface EventListener<TData> {
    void onEvent(TData data);
    void onError(Exception e);
}
