package de.ur.servus;

public interface DataBroadcastReciever<TData> {

    void onRecieve(TData data);

    void onError(String e);
}
