package de.ur.servus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import de.ur.servus.core.BackendHandler;
import de.ur.servus.core.Event;
import de.ur.servus.core.EventListener;
import de.ur.servus.core.FirestoreBackendHandler;
import de.ur.servus.core.ListenerRegistration;

/**
 * The EventBroadcaster is used to receive updates about events.
 * We start listening for event updates from the database from here and listen to changes.
 */
public class EventBroadcaster {

    private static final String INTENT_EVENTS_RECEIVED = "de.ur.servus.EVENTS_RECEIVED";
    private static final String INTENT_EVENTS_RECEIVED_ERROR = "de.ur.servus.EVENTS_RECEIVED_ERROR";

    public final Context context;
    private @Nullable ListenerRegistration listenerRegistration;

    public EventBroadcaster(Context context) {
        this.context = context;
    }

    public void startListeningForEventUpdates() {
        stopListeningForEventUpdates();

        BackendHandler bh = new FirestoreBackendHandler();
        this.listenerRegistration = bh.subscribeEvents(new EventListener<>() {
            @Override
            public void onEvent(List<Event> events) {
                var intent = new Intent(EventBroadcaster.INTENT_EVENTS_RECEIVED);
                intent.putParcelableArrayListExtra("events", (ArrayList<Event>) events);

                LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
                localBroadcastManager.sendBroadcast(intent);
            }

            @Override
            public void onError(Exception e) {
                var intent = new Intent(EventBroadcaster.INTENT_EVENTS_RECEIVED_ERROR);
                intent.putExtra("message", e.getMessage());

                LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);
                localBroadcastManager.sendBroadcast(intent);
            }
        });
    }

    public void stopListeningForEventUpdates() {
        if(this.listenerRegistration != null){
            this.listenerRegistration.unsubscribe();
        }
    }

    public void startBroadcastReciever(DataBroadcastReciever<List<Event>> reciever) {
        this.registerSuccessReciever(reciever);
        this.registerErrorReciever(reciever);
    }

    private void registerSuccessReciever(DataBroadcastReciever<List<Event>> reciever){
        var broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                List<Event> events = intent.getParcelableArrayListExtra("events");
                reciever.onRecieve(events);
            }
        };

        this.registerLocalBroadcastForAction(EventBroadcaster.INTENT_EVENTS_RECEIVED, broadcastReceiver);
    }

    private void registerErrorReciever(DataBroadcastReciever<List<Event>> reciever) {
        var broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = intent.getStringExtra("message");
                reciever.onError(message);
            }
        };

        this.registerLocalBroadcastForAction(EventBroadcaster.INTENT_EVENTS_RECEIVED_ERROR, broadcastReceiver);
    }

    private void registerLocalBroadcastForAction(String action, BroadcastReceiver reciever) {
        var intentFilter = new IntentFilter();
        intentFilter.addAction(action);

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this.context);
        localBroadcastManager.registerReceiver(reciever, intentFilter);
    }
}
