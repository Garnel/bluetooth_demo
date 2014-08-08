package com.maoguangming.bluetoothdemo;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.TextView;

import com.maoguangming.bluetoothdemo.R;

import org.w3c.dom.Text;

public class ConnectActivity extends Activity {

    private BluetoothDevice device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        Intent intent = getIntent();
        if (intent != null) {
            device = intent.getParcelableExtra("device");
        }

        if (device == null) {
            throw new RuntimeException("No device provided.");
        }

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new ResultFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.connect, menu);
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

    private interface DataReceiveLisnter {
        public void onDataReceive();
    }

    /**
     * Result fragment containing data from sensors.
     */
    public static class ResultFragment extends Fragment {

        private TextView tvTemperature;
        private TextView tvPressure;
        private TextView tvWind;
        private TextView tvAcceleration;

        public ResultFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_connect, container, false);
            return rootView;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            tvTemperature = (TextView) view.findViewById(R.id.temperature);
            tvPressure = (TextView) view.findViewById(R.id.pressure);
            tvWind = (TextView) view.findViewById(R.id.wind);
            tvAcceleration = (TextView) view.findViewById(R.id.acceleration);
        }

        @Override
        public void onStart() {
            super.onStart();


        }
    }
}
