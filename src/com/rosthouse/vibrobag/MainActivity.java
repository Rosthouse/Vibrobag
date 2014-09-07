package com.rosthouse.vibrobag;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainActivity extends Activity {

    private BluetoothAdapter blthAdapter;
    private BluetoothSocket socket;
    private UUID uuid = UUID.randomUUID();
    private BroadcastReceiver broadcastMessageReceiver;
    private final static int REQUEST_ENABLE_BT = 42;
    private ArrayAdapter<BluetoothDevice> mArrayAdapter;
    private final BroadcastReceiver searchDevicesReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mArrayAdapter.add(device);
            }
        }
    };
    private BluetoothAdapter.LeScanCallback mLeScanCallback
            = new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                        byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//               mLeDeviceListAdapter.addDevice(device);
//               mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        blthAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        if (blthAdapter == null || !blthAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        setContentView(R.layout.main);
        broadcastMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = intent.getStringExtra("message");
                Log.d("receiver", "Got message: " + message);
            }
        };
        ListView devicesList = (ListView) findViewById(R.id.found_devices_list);
        mArrayAdapter = new ArrayAdapter<BluetoothDevice>(MainActivity.this, android.R.layout.simple_list_item_1);
        devicesList.setAdapter(mArrayAdapter);
        devicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                AsyncTask<Integer, Void, Void> connectTask = new AsyncTask<Integer, Void, Void>() {
                    @Override
                    protected Void doInBackground(Integer... params) {
                        try {
                            BluetoothDevice device = mArrayAdapter.getItem(params[0]);
                            socket = device.createRfcommSocketToServiceRecord(uuid);
                            socket.connect();
                        } catch (IOException e) {
                            Log.d("BLUETOOTH_CLIENT", e.getMessage());
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
//                        switchViews();
                    }
                };
                connectTask.execute(position);
            }
        });
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastMessageReceiver, new IntentFilter("custom-event-name"));
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(broadcastMessageReceiver);
        unregisterReceiver(searchDevicesReceiver);
    }

    public void discoverBag(View view) {

        /*try {
         Set<BluetoothDevice> pairedDevices = blthAdapter.getBondedDevices();
         if (pairedDevices.size() > 0) {
         for (BluetoothDevice device : pairedDevices) {
         mArrayAdapter.add(device);
         }
         } else {
         IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
         registerReceiver(searchDevicesReceiver, filter); // Don't forget to unregister during onDestroy
         }
         } catch (Exception e) {
         System.out.println(e.getMessage());
         }*/
    }

    public void sendMessage(View view) {
        Context context = getApplicationContext();
        CharSequence text = "Hello toast!";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
    private static int DISCOVERY_REQUEST = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DISCOVERY_REQUEST) {
            boolean isDiscoverable = resultCode > 0;
            if (isDiscoverable) {
                String name = "bluetoothserver";
                try {
                    final BluetoothServerSocket btserver = blthAdapter.listenUsingRfcommWithServiceRecord(name, uuid);
                    AsyncTask<Integer, Void, BluetoothSocket> acceptThread = new AsyncTask<Integer, Void, BluetoothSocket>() {
                        @Override
                        protected BluetoothSocket doInBackground(Integer... params) {

                            try {
                                BluetoothSocket socket = btserver.accept(params[0] * 1000);
                                return socket;
                            } catch (IOException e) {
                                Log.d("BLUETOOTH", e.getMessage());
                            }

                            return null;
                        }

                        @Override
                        protected void onPostExecute(BluetoothSocket result) {
                            if (result != null) {
//                                switchUI();
                                socket = result;
                                try {
                                    socket.connect();
                                } catch (IOException ex) {
                                    Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }
                    };
                    acceptThread.execute(resultCode);
                } catch (IOException e) {
                    Log.d("BLUETOOTH", e.getMessage());
                }
            }
        }
    }

}
