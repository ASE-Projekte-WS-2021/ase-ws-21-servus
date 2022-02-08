package de.ur.servus;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;

import de.ur.servus.core.NetworkUtil;

public class NetworkChangeReceiver extends BroadcastReceiver {

    LinearLayout error_message;
    Activity activity;

    // Necessary to handle manifest error warning
    NetworkChangeReceiver(){}

    NetworkChangeReceiver(Activity activity){
        this.activity = activity;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {

        int status = NetworkUtil.getConnectivityStatusString(context);

        if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
            if (status == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
                // Not connected to the internet
                error_message = activity.findViewById(R.id.container_404);
                if (error_message != null) {
                    error_message.setVisibility(View.VISIBLE);
                }
            } else {
                // Connected to the internet
                error_message = activity.findViewById(R.id.container_404);
                if (error_message != null) {
                    error_message.setVisibility(View.GONE);
                }
            }
        }
    }
}
