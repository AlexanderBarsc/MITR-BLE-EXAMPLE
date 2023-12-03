package com.example.mitr_ble_example;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity {

    MyListAdapter myListAdapter;
    ListView myList;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    public MainActivity()
    {
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


        myList = (ListView)findViewById(R.id.ListViewBleDevices);
        myListAdapter = new MyListAdapter(this.getLayoutInflater());
        myList.setAdapter(myListAdapter);
        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDeviceParameters item = (BluetoothDeviceParameters)myListAdapter.getItem(position);
                Toast toast = Toast.makeText(getApplicationContext(), item.Name, Toast.LENGTH_SHORT);
                toast.show();
            }
        });



        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // code to be executed when button is clicked
                scanLeDevice();
            }
        });

    }

    public static class ViewHolder
    {
      TextView textView;
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
                        myListAdapter.addItem(new BluetoothDeviceParameters(device.getAddress(), device.getName()));
                       myListAdapter.notifyDataSetChanged();
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

    private class MyListAdapter extends BaseAdapter{

        public MyListAdapter(LayoutInflater inflater){
            super();
            mInflanter = inflater;
            myListItems = new ArrayList<>();
        }

        private LayoutInflater mInflanter;
        private ArrayList<BluetoothDeviceParameters> myListItems;

        public void addItem(BluetoothDeviceParameters item){
            myListItems.add(item);
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

        @Override
        public View getView(int position, View view, ViewGroup parent) {

           ViewHolder viewHolder;
            if(view == null){
                view = mInflanter.inflate(R.layout.activity_main, null);
                viewHolder = new ViewHolder();
                viewHolder.textView = (TextView) view.findViewById(R.id.list_item_text1);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder)view.getTag();
            }

            BluetoothDeviceParameters item = myListItems.get(position);
            viewHolder.textView.setText(item.Name);


            return view;
        }
    }
}