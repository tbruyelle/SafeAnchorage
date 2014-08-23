package ancorage.kamosoft.com.safeancorage;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.util.Date;


public class MainActivity extends Activity
        implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private Button mAnchorBtn;
    private EditText mAlertRadius;
    private TextView mAnchorLocationTxt;
    private TextView mCurrentLocationTxt;
    private TextView mDistanceTxt;

    private LocationClient mLocationClient;
    private LocationRequest sLocationRequest = LocationRequest.create()
            .setInterval(8000)
            .setFastestInterval(4000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    private LatLng mAnchorLatLng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);

        mAnchorBtn = (Button) findViewById(R.id.anchor);
        // TODO Suggest 20m,30m,40m,50m,60m via a spinner
        mAlertRadius = (EditText) findViewById(R.id.alert_radius);

        mAnchorLocationTxt = (TextView) findViewById(R.id.anchor_location);
        mCurrentLocationTxt = (TextView) findViewById(R.id.current_location);
        mDistanceTxt = (TextView) findViewById(R.id.distance);

        mAnchorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(mAlertRadius.getText())) {
                    Toast.makeText(MainActivity.this, R.string.please_fill_lengths, Toast.LENGTH_SHORT).show();
                    return;
                }
                mLocationClient = new LocationClient(MainActivity.this, MainActivity.this, MainActivity.this);
                mLocationClient.connect();
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        anchorEnable(mLocationClient.getLastLocation());
        mLocationClient.requestLocationUpdates(sLocationRequest, MainActivity.this);
    }

    private void anchorEnable(Location location) {
        mAnchorLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        mAnchorLocationTxt.setText(mAnchorLatLng.latitude + ", " + mAnchorLatLng.longitude);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mAnchorLatLng != null) {
            mCurrentLocationTxt.setText(location.getLatitude() + ", " + location.getLongitude());
            double d = SphericalUtil.computeDistanceBetween(mAnchorLatLng,
                    new LatLng(location.getLatitude(), location.getLongitude()));
            mDistanceTxt.setText(d + "m");
            if (d > Integer.valueOf(mAlertRadius.getText().toString())) {
                sendNotification();
            }
        }
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification() {

        Intent intent = new Intent(this, MainActivity.class);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Anchorage radius reached !")
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

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "Unable to connect to Google Play Services", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
