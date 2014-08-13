package com.maoguangming.bluetoothdemo;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends Activity {

    private static final String TAG = "MainAcrivity";

    private static final int REQUEST_ENABLE_BT = 1000;
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<BluetoothDevice> mDevicesAvailable = new ArrayList<BluetoothDevice>();
    private ArrayList<String> mDevicesNameList = new ArrayList<String>();
    private Dialog deviceListDialog;
    private ArrayAdapter mDeviceListAdapter;

    private interface DataReceiveLisnter {
        public void onDataReceive(SensorData data);
    }

    private DataReceiveLisnter mReceiveListener;

    private UUID DATA_UUID;
    private static final int MESSAGE_READ = 1;

    static class DataReceiveHandler extends Handler {
        WeakReference<MainActivity> mActivity;

        public DataReceiveHandler (MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            switch (msg.what) {
                case MESSAGE_READ:
                    int bytes = msg.arg1;
                    byte[] buf = (byte[]) msg.obj;
                    activity.handleData(bytes, buf);
                    break;
            }
        }
    }

    DataReceiveHandler mHandler = new DataReceiveHandler(this);

    ConnectThread connectThread;
    ConnectedThread readThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            ResultFragment fragment = new ResultFragment();
            getFragmentManager().beginTransaction().add(R.id.container, fragment).commit();
            mReceiveListener = fragment;
        }

        // UUID for receive data: 0xFFE5
        DATA_UUID = UUID.nameUUIDFromBytes(new byte[]{(byte)0xFF, (byte)0xE5});
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
        // automatically handle clicks on the Home/Up button, so longfindViewById
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.action_search) {
            // start searching Bluetooth devices
            startSearch();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mReceiver);

        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }

        if (connectThread != null) {
            connectThread.cancel();
        }

        if (readThread != null) {
            readThread.cancel();
            readThread.stopRead();
        }
    }

    /**
     * Start search
     */
    private void startSearch() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.not_support_bluetooth, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, R.string.bluetooth_not_enabled, Toast.LENGTH_SHORT).show();
                return;
            }

            if (resultCode == RESULT_OK) {
                Toast.makeText(this, R.string.bluetooth_enabled, Toast.LENGTH_SHORT).show();
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
                if (!mDevicesAvailable.contains(device)) {
                    mDevicesAvailable.add(device);
                    mDevicesNameList.add(device.getName());
                }

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
                Toast.makeText(MainActivity.this, getString(R.string.connecting_to) + " " +
                        device.getName(), Toast.LENGTH_SHORT).show();

                if (deviceListDialog.isShowing()) {
                    deviceListDialog.dismiss();
                }

                mBluetoothAdapter.cancelDiscovery();

                // start to connect
                connectThread = new ConnectThread(device);
                connectThread.start();
            }
        }
    };

    /**
     * Handle data received
     * @param bytes size of data received
     * @param data  data received
     */
    private void handleData(int bytes, byte[] data) {
        Log.d(TAG, "Received " + bytes + " bytes: " + new String(data));
        SensorData sensorData;
        // TODO handle data and init sensorData
        // TODO JUST FOR TEST
        sensorData = new SensorData(1,1,1,1,1,1,1);
        Toast.makeText(this, "Received " + bytes + " bytes: " + new String(data), Toast.LENGTH_SHORT)
                .show();

        // update
        mReceiveListener.onDataReceive(sensorData);
    }

    /**
     * found Bluetooth devices
     */
    private void findDevices() {
        deviceListDialog = new Dialog(this);

        deviceListDialog.setContentView(R.layout.device_list);
        deviceListDialog.setTitle(R.string.search_bluetooth_devices);

        ListView mDeviceList = (ListView) deviceListDialog.findViewById(android.R.id.list);
        if (mDeviceList != null) {
            mDeviceListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                    mDevicesNameList);
            mDeviceList.setAdapter(mDeviceListAdapter);
            mDeviceList.setOnItemClickListener(connectDevice);
        }
        deviceListDialog.show();

        queryPaired();

        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

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

    /**
     * start a read thread
     * @param socket socket use to read
     */
    private void manageConnectedSocket(BluetoothSocket socket) {
        readThread = new ConnectedThread(socket);
        readThread.start();
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(DATA_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                connectException.printStackTrace();

                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    closeException.printStackTrace();
                }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            manageConnectedSocket(mmSocket);
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        private boolean exit = false;
        public void stopRead() {
            exit = true;
        }

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                if (exit) {
                    return;
                }

                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class ResultFragment extends Fragment implements DataReceiveLisnter {

        private TableLayout tblResult;
        private TextView tvNoData;

        private TextView tvTemperature;
        private TextView tvPressure;
        private TextView tvWind;
        private TextView tvAcceleration;

        private SensorData data;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_result, container, false);
        }


        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            tvNoData = (TextView) view.findViewById(R.id.no_data);
            tblResult = (TableLayout) view.findViewById(R.id.result_table);

            tvTemperature = (TextView) view.findViewById(R.id.temperature);
            tvPressure = (TextView) view.findViewById(R.id.pressure);
            tvWind = (TextView) view.findViewById(R.id.wind);
            tvAcceleration = (TextView) view.findViewById(R.id.acceleration);
        }

        @Override
        public void onStart() {
            super.onStart();
        }

        private void updateResult() {
            if (data == null) {
                return;
            }

            if (tvNoData != null && tvNoData.getVisibility() == View.VISIBLE) {
                tvNoData.setVisibility(View.INVISIBLE);
            }

            if (tblResult != null && tblResult.getVisibility() != View.VISIBLE) {
                tblResult.setVisibility(View.VISIBLE);
            }

            if (tvTemperature != null) {
                tvTemperature.setText(String.format(Locale.getDefault(), "%.5f", data.getTemperature()));
            }

            if (tvPressure != null) {
                tvTemperature.setText(String.format(Locale.getDefault(), "%.5f", data.getPressure()));
            }

            if (tvWind != null) {
                tvWind.setText(String.format(Locale.getDefault(), "(%.5f, %.5f)",
                        data.getWindFront(), data.getWindSide()));
            }

            if (tvAcceleration != null) {
                tvAcceleration.setText(String.format(Locale.getDefault(), "(%.5f, %.5f, %.5f)",
                        data.getAccelerationX(), data.getAccelerationY(), data.getAccelerationZ()));
            }
        }

        @Override
        public void onDataReceive(SensorData data) {
            this.data = data;
            if (data != null) {
                updateResult();
            }
        }
    }
}
