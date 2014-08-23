package ancorage.kamosoft.com.safeancorage;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.util.Date;

public class LocateService extends Service
        implements GooglePlayServicesClient.ConnectionCallbacks,
        LocationListener,
        GooglePlayServicesClient.OnConnectionFailedListener, Constants {

    private LocationClient mLocationClient;
    private LocationRequest sLocationRequest = LocationRequest.create()
            .setInterval(8000)
            .setFastestInterval(4000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    private LatLng mAnchorLatLng;

    @Override
    public void onCreate() {
        super.onCreate();
        mLocationClient = new LocationClient(this, this, this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        play();
        mLocationClient.connect();
        return (START_NOT_STICKY);
    }

    @Override
    public void onDestroy() {
        Log.w(TAG, "stop service");
        mLocationClient.disconnect();
        stopForeground(true);
        mAnchorLatLng = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationClient.requestLocationUpdates(sLocationRequest, this);
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (mAnchorLatLng == null) {
            // First pass
            mAnchorLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        }

        double d = SphericalUtil.computeDistanceBetween(mAnchorLatLng,
                new LatLng(location.getLatitude(), location.getLongitude()));

        SharedPreferences prefs = getSharedPreferences(SHARED_PREF, 0);
        if (d > prefs.getInt(PREFS_RADIUS, 5)) {
            // Distance reached -> Alert!
            sendNotification();
        }
    }

    private void sendNotification() {

        Intent intent = new Intent(this, MainActivity.class);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentText(getString(R.string.anchorage_alert))
//                .setContentText("Camille a vu quelquechose...")
                .setContentIntent(contentIntent)
                .setWhen(new Date().getTime())
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL);

        Notification notification = builder
                .build();

        NotificationManager notifManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.notify(1, notification);
    }

    private void play() {
        Log.w(TAG, "start service");

        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);

        NotificationCompat.Builder notif = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentText(getString(R.string.anchorage_ready))
//                .setContentText("Camille a vu quelquechose...")
                .setContentIntent(pi)
                .setWhen(new Date().getTime())
                .setAutoCancel(true);

//            note.setLatestEventInfo(this, "Fake Player",
//                    "Now Playing: \"Ummmm, Nothing\"",
//                    pi);
//            note.flags |= Notification.FLAG_NO_CLEAR;

        startForeground(1337, notif.build());
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed");
        stopSelf();
    }
}