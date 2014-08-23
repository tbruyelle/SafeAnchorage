package ancorage.kamosoft.com.safeancorage;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;


public class MainActivity extends Activity implements Constants {

    private Switch mAnchorBtn;
    private Spinner mAlertRadius;
    private TextView mAnchorLocationTxt;
    private TextView mCurrentLocationTxt;
    private TextView mDistanceTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);

        mAnchorBtn = (Switch) findViewById(R.id.anchor);
        mAlertRadius = (Spinner) findViewById(R.id.alert_radius);
        mAnchorLocationTxt = (TextView) findViewById(R.id.anchor_location);
        mCurrentLocationTxt = (TextView) findViewById(R.id.current_location);
        mDistanceTxt = (TextView) findViewById(R.id.distance);

        mAnchorBtn.setChecked(isMyServiceRunning(LocateService.class));

        mAnchorBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startService(new Intent(MainActivity.this, LocateService.class));
                } else {
                    stopService(new Intent(MainActivity.this, LocateService.class));
                }
            }
        });

        mAlertRadius.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences prefs = getSharedPreferences(Constants.SHARED_PREF, 0);
                prefs.edit().putInt(PREFS_RADIUS,
                        Integer.valueOf((String) mAlertRadius.getSelectedItem())).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
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
