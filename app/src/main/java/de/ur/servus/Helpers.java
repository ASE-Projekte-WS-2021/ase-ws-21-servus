package de.ur.servus;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.widget.Button;

import de.ur.servus.core.BackendHandler;
import de.ur.servus.core.FirestoreBackendHandler;

// TODO better name/structuring of this stuff
public class Helpers {

    /**
     * Informs the backend about the attendance of an Event and saves its id in the SharedPreferences.
     * @param eventId The ID of the Event to attend to.
     */
    public static void attendEvent(Activity activity, String eventId) {
        // call backend to increment attendant counter
        // trigger subscription to Event (and remove any old subscriptions to specific Events), after first call to backend is finished
        // save eventId in SharedPreferences as currentEventId

        /*

        SharedPreferences sharedPreferences = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        final String SUBSCRIBED_TO_EVENT = "subscribedToEvent";

        Button attend_withdraw_meetup = activity.findViewById(R.id.event_details_button);

        BackendHandler bh_attend_withdraw = FirestoreBackendHandler.getInstance();
        bh_attend_withdraw.incrementEventAttendants(eventId);

        attend_withdraw_meetup.setText(R.string.event_details_button_withdraw);
        attend_withdraw_meetup.setBackgroundResource(R.drawable.style_btn_roundedcorners_clicked);
        attend_withdraw_meetup.setTextColor(activity.getResources().getColor(R.color.servus_pink, activity.getTheme()));

        editor.putString(SUBSCRIBED_TO_EVENT, eventId);
        editor.apply();

        */
    }

    /**
     * Informs the backend about the leave of an Event and clears the SharedPreferences.
     */
    public static void leaveEvent(Activity activity, String eventId) {
        // check, if currently in event
        // call backend to decrement attendant counter
        // remove any old subscriptions to specific Events
        // set currentEventId in SharedPreferences to a null value

        /*

        SharedPreferences sharedPreferences = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        final String SUBSCRIBED_TO_EVENT = "subscribedToEvent";

        Button attend_withdraw_meetup = activity.findViewById(R.id.event_details_button);

        BackendHandler bh_attend_withdraw = FirestoreBackendHandler.getInstance();

        bh_attend_withdraw.decrementEventAttendants(eventId);

        attend_withdraw_meetup.setText(R.string.event_details_button_attend);
        attend_withdraw_meetup.setBackgroundResource(R.drawable.style_btn_roundedcorners);
        attend_withdraw_meetup.setTextColor(activity.getResources().getColor(R.color.servus_white, activity.getTheme()));

        editor.putString(SUBSCRIBED_TO_EVENT, "none");
        editor.apply();

        */
    }
}
