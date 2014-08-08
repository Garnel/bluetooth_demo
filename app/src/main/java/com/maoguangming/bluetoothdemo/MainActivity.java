package com.maoguangming.bluetoothdemo;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Dialog;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
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
        private ArrayList<String> mDevicesNameList;
        private Dialog deviceListDialog;
        private ListView mDeviceList;
        private ArrayAdapter mDeviceListAdapter;

        public PlaceholderFragment() {
            mDevicesAvailable = new ArrayList<BluetoothDevice>();
            mDevicesNameList = new ArrayList<String>();
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
        public void onDestroy() {
            super.onDestroy();

            if (mReceiver != null) {
                getActivity().unregisterReceiver(mReceiver);
            }

            if (mBluetoothAdapter != null) {
                mBluetoothAdapter.cancelDiscovery();
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
            } else {
                findDevices();
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
                    findDevices();
                }
            }
        }

        // Create a BroadcastReceiver for ACTION_FOUND
        private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Add the name and address to an array adapter to show in a ListView
                    mDevicesAvailable.add(device);
                    mDevicesNameList.add(device.getName());

                    if (mDeviceListAdapter != null) {
                        mDeviceListAdapter.notifyDataSetChanged();
                    }
                }
            }
        };

        AdapterView.OnItemClickListener connectDevice = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < mDevicesAvailable.size()) {
                    BluetoothDevice device = mDevicesAvailable.get(position);
                    Toast.makeText(getActivity(), getString(R.string.connecting_to) + " " +
                        device.getName(), Toast.LENGTH_LONG).show();

                    mBluetoothAdapter.cancelDiscovery();

                    Intent intent = new Intent(getActivity(), ConnectActivity.class);
                    if (intent != null) {
                        intent.putExtra("device", device);
                        startActivity(intent);
                    }
                }
            }
        };

        /**
         * found Bluetooth devices
         */
        private void findDevices() {
            deviceListDialog = new Dialog(getActivity());
            if (deviceListDialog == null) {
                return;
            }
            deviceListDialog.setContentView(R.layout.device_list);
            deviceListDialog.setTitle(R.string.search_bluetooth_devices);

            mDeviceList = (ListView) deviceListDialog.findViewById(android.R.id.list);
            if (mDeviceList != null) {
                mDeviceListAdapter = new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_list_item_1, mDevicesNameList);
                mDeviceList.setAdapter(mDeviceListAdapter);
                mDeviceList.setOnItemClickListener(connectDevice);
            }
            deviceListDialog.show();

            queryPaired();

            // Register the BroadcastReceiver
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            getActivity().registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

            if (mBluetoothAdapter != null) {
                mBluetoothAdapter.startDiscovery();
            }
        }

        /**
         * query devices already paired.
         */
        private void queryPaired() {
            if (mBluetoothAdapter == null || mDevicesAvailable == null) {
                return;
            }

            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            // If there are paired devices
            if (pairedDevices.size() > 0) {
                // Loop through paired devices
                for (BluetoothDevice device : pairedDevices) {
                    mDevicesAvailable.add(device);
                    mDevicesNameList.add(device.getName());
                }

                if (mDeviceListAdapter != null) {
                    mDeviceListAdapter.notifyDataSetChanged();
                }
            }
        }
    }
}
