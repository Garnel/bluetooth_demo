package com.maoguangming.bluetoothdemo;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Dialog;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements View.OnClickListener {

        private static final int REQUEST_ENABLE_BT = 1000;
        private Button searchButton;
        private BluetoothAdapter mBluetoothAdapter;
        private ArrayList<BluetoothDevice> mDevicesAvailable;
        private Dialog deviceList;

        public PlaceholderFragment() {
            mDevicesAvailable = new ArrayList<BluetoothDevice>();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            searchButton = (Button) view.findViewById(R.id.start_search);
            if (searchButton != null) {
                searchButton.setOnClickListener(this);
            }
        }


        @Override
        public void onClick(View view) {
            if (view == null) {
                return;
            }

            switch (view.getId()) {
                case R.id.start_search:
                    startSearch();
                    break;
                default:
                    break;
            }
        }

        /**
         * Start search
         */
        private void startSearch() {
             mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null) {
                Toast.makeText(getActivity(), R.string.not_support_bluetooth, Toast.LENGTH_LONG).show();
            }

            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == REQUEST_ENABLE_BT) {
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(getActivity(), R.string.bluetooth_not_enabled, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (resultCode == RESULT_OK) {
                    Toast.makeText(getActivity(), R.string.bluetooth_enabled, Toast.LENGTH_SHORT).show();
                }
            }
        }

        private void findDevices() {

        }

        private void queryPaired() {
            if (mBluetoothAdapter == null) {
                return;
            }

            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            // If there are paired devices
            if (pairedDevices.size() > 0) {
                // Loop through paired devices
                for (BluetoothDevice device : pairedDevices) {
                    mDevicesAvailable.add(device);
                }
            }
        }
    }
}
