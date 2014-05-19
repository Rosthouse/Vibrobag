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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainActivity extends Activity {

    private BluetoothAdapter blthAdapter;
    private BluetoothSocket socket;
    private UUID uuid = UUID.randomUUID();
    private ArrayList<BluetoothDevice> foundDevices;
    private BroadcastReceiver broadcastMessageReceiver;
    private final static int REQUEST_ENABLE_BT = 42;
    private ArrayAdapter<String> mArrayAdapter;
    private final BroadcastReceiver searchDevicesReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                mArrayAdapter.notifyDataSetChanged();
            }

        }
    };

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        blthAdapter = BluetoothAdapter.getDefaultAdapter();
        mArrayAdapter = new ArrayAdapter<String>(this, R.id.found_devices);
//        blthAdapter = BluetoothAdapter.getDefaultAdapter();
        if (blthAdapter == null || !blthAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        setContentView(R.layout.main);
        broadcastMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Get extra data included in the Intent
                String message = intent.getStringExtra("message");
                Log.d("receiver", "Got message: " + message);
            }
        };
        ListView devicesList = (ListView) findViewById(R.id.found_devices);
//        devicesList.setOnClickListener(new View.OnClickListener() {
//
//            public void onClick(View v) {
//v.get
//            }
//        });

        LocalBroadcastManager
                .getInstance(this).registerReceiver(broadcastMessageReceiver,
                        new IntentFilter("custom-event-name"));
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(broadcastMessageReceiver);
        unregisterReceiver(searchDevicesReceiver);
    }

    public void discoverBag(View view) {

        Set<BluetoothDevice> pairedDevices = blthAdapter.getBondedDevices();
// If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
            mArrayAdapter.notifyDataSetChanged();
        } else {
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(searchDevicesReceiver, filter); // Don't forget to unregister during onDestroy
        }
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
