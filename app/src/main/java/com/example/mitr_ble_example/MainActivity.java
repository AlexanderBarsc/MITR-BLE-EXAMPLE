package com.example.mitr_ble_example;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    MyListAdapter myListAdapter;
    ListView myList;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;

    private TextView measuredValue;

    BluetoothGattCharacteristic characteristic;

    private BluetoothGatt bluetoothGatt;

    private boolean isConnected;

    private Timer myTimer;

    public MainActivity()
    {
        isConnected = false;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final BluetoothManager bluetoothManager = (BluetoothManager)
                getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        // TODO - tohle nejak rozumne napsat
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }

        BluetoothGattCallback serverCallback = new BluetoothGattCallback() {

            @SuppressLint("MissingPermission")
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

                if (BluetoothProfile.STATE_CONNECTED == newState) {

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            myList.setVisibility(View.GONE);
                            Toast toast = Toast.makeText(getApplicationContext(), "Connected to device", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    });

                    isConnected = true;

                    gatt.discoverServices();

                } else if (BluetoothProfile.STATE_DISCONNECTED == newState)
                {
                    myList.setVisibility(View.INVISIBLE);
                    isConnected = false;

                }

                // super.onConnectionStateChange(gatt, status, newState);
            }

            @Override
            public void onCharacteristicRead(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value, int status) {

                String text = "Measured value: " + littleEndianByteArrayToInt(value);
                updateTextView(text);

            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {

                BluetoothGattService service = gatt.getService(UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b"));
                characteristic = service.getCharacteristic(UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8"));

            }


        };


        myList = (ListView)findViewById(R.id.ListViewBleDevices);
        measuredValue = (TextView)findViewById(R.id.measuredValue);
        measuredValue.setText("");
        myListAdapter = new MyListAdapter(this.getLayoutInflater());
        myList.setAdapter(myListAdapter);
        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice item = (BluetoothDevice)myListAdapter.getItem(position);

                bluetoothGatt = item.connectGatt(getApplicationContext(),false, serverCallback );
                @SuppressLint("MissingPermission") Toast toast = Toast.makeText(getApplicationContext(), "Connecting to device: " + item.getName(), Toast.LENGTH_SHORT);
                toast.show();
            }
        });


        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // code to be executed when button is clicked
                if(isConnected)
                {
                    scanLeDevice();
                }
                else
                {

                }

            }
        });


        myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                TimerMethod();
            }

        }, 0, 5000);

    }

    @SuppressLint("MissingPermission")
    private void TimerMethod()
    {
        //This method is called directly by the timer
        //and runs in the same thread as the timer.
          if(isConnected) {
                //We call the method that will work with the UI
                //through the runOnUiThread method.

              if(characteristic != null)
              {
                  bluetoothGatt.readCharacteristic(characteristic);
              }

            }

    }


    private void updateTextView(final String s) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tv= (TextView) findViewById(R.id.measuredValue);
                tv.setText(s);
            }
        });
    }


    public static class ViewHolder
    {
      TextView textView1;
      TextView textView2;
    }

    private void scanLeDevice() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 1);
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            bluetoothLeScanner.startScan(leScanCallback);
        }

        private LeDeviceListAdapter leDeviceListAdapter = new LeDeviceListAdapter();

// Device scan callback.
    private ScanCallback leScanCallback =
                new ScanCallback() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onScanResult(int callbackType, ScanResult result) {
                        BluetoothDevice device = result.getDevice();
                        myListAdapter.addItem(device);
                       bluetoothLeScanner.stopScan(leScanCallback);
                    }
                };

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // feature requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            });

    public static int littleEndianByteArrayToInt(byte[] byteArray) {

        // Convert the bytes to an int using bitwise operations for little-endian order
        int result = 0;
        for (int i = 3; i >= 0; i--) {
            result = (result << 8) | (byteArray[i] & 0xFF);
        }

        return result;
    }

    private class MyListAdapter extends BaseAdapter{

        public MyListAdapter(LayoutInflater inflater){
            super();
            mInflanter = inflater;
            myListItems = new ArrayList<>();
        }

        private LayoutInflater mInflanter;
        private ArrayList<BluetoothDevice> myListItems;

        public void addItem(BluetoothDevice item)
        {
            if (!isDeviceAlreadyAdded(item)) {
                myListItems.add(item);
                notifyDataSetChanged(); // Make sure to notify the adapter about the data change
            }
        }

        @Override
        public int getCount() {
            return myListItems.size();
        }

        @Override
        public Object getItem(int position) {
            return myListItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("MissingPermission")
        @Override
        public View getView(int position, View view, ViewGroup parent) {

           ViewHolder viewHolder;
            if(view == null){
                view = mInflanter.inflate(R.layout.list_item, null);
                viewHolder = new ViewHolder();
                viewHolder.textView1 = (TextView) view.findViewById(R.id.list_item_text1);
                viewHolder.textView2 = (TextView) view.findViewById(R.id.list_item_text2);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder)view.getTag();
            }

            BluetoothDevice item = myListItems.get(position);
            viewHolder.textView1.setText(item.getName());
            viewHolder.textView2.setText(item.getAddress());

            return view;
        }

        private boolean isDeviceAlreadyAdded(BluetoothDevice newDevice) {
            for (BluetoothDevice device : myListItems) {
                if (device.getAddress().equals(newDevice.getAddress())) {
                    return true; // Device already in the list
                }
            }
            return false; // Device not found in the list
        }


    }
}