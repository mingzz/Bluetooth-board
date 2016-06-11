package com.my.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import com.my.bluetooth.util.BluetoothUtil;
import com.my.bluetooth.util.DataUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/*
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
*/
public class MainActivity extends Activity {


    //bluetooth
    private Switch switchClient;
    private LinearLayout layoutConnectBluetooth;
    private ListView listViewBluetoothDevices;
    private Button buttonReScanDevices;
    private ProgressBar progressBar;
    private ArrayAdapter<String> arrayAdapterDevices;

    private BluetoothUtil bluetoothUtil;
    private Map<String,BluetoothDevice> deviceMap = new HashMap<String, BluetoothDevice>();
    private List<String> deviceNames = new ArrayList<String>();
    private Button buttonTour;
    private ImageView imgView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initListener();

        initBluetooth();


    }

    private void initView(){
        switchClient = (Switch)findViewById(R.id.switchClient);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        layoutConnectBluetooth = (LinearLayout)findViewById(R.id.layoutConnectBluetooth);
        listViewBluetoothDevices = (ListView)findViewById(R.id.listViewBluetoothDevices);
        buttonReScanDevices = (Button)findViewById(R.id.buttonReScanDevices);
        buttonTour = (Button)findViewById(R.id.buttonTour);
        layoutConnectBluetooth.setVisibility(View.GONE);
        imgView = (ImageView) findViewById(R.id.sjtu);
        imgView.setImageResource(R.drawable.sjtu);
    }

    private void initListener(){
        switchClient.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    layoutConnectBluetooth.setVisibility(View.VISIBLE);
                    startClient();//开启客户端
                }else{
                    layoutConnectBluetooth.setVisibility(View.GONE);
                    bluetoothUtil.finishClient();
                    progressBar.setVisibility(View.GONE);
                    switchClient.setText("点击开启客户端");
                }
            }
        });


        buttonReScanDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startClient();
                buttonReScanDevices.setVisibility(View.GONE);
            }
        });

        buttonTour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bluetoothUtil.CLIENT_CONNECT ){
                    //作为客户端或者服务器连接上了
                    Intent intent = new Intent(MainActivity.this, SubActivity.class);
                    startActivity(intent);
                    //finish();
                }else{
                    Toast.makeText(MainActivity.this,"蓝牙设备未连接",Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    @Override
    public void onDestroy(){
        unregisterReceiver(bluetoothReceiver);
        super.onDestroy();
    }


    /**
     * 蓝牙扫描广播
     */
    private BroadcastReceiver bluetoothReceiver =new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice deviceTmp = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                deviceMap.put(deviceTmp.getName(),deviceTmp);
                deviceNames.add(deviceTmp.getName());
                //arrayAdapterDevices.notifyDataSetChanged();
                arrayAdapterDevices = new ArrayAdapter<>(MainActivity.this,android.R.layout.simple_list_item_1,deviceNames);
                listViewBluetoothDevices.setAdapter(arrayAdapterDevices);

                switchClient.setText("找到以下设备，点击连接");
                buttonReScanDevices.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }else if(intent.getAction().equals(BluetoothUtil.ACTION_BLUETOOTH_CLIENT_OPEN)){
                //客户端成功连接一台设备

                switchClient.setText("蓝牙连接成功！");
                progressBar.setVisibility(View.GONE);
            }else if(intent.getAction().equals(BluetoothUtil.ACTION_BLUETOOTH_CLIENT_ERROR)){

                switchClient.setText("蓝牙连接超时，请重试");
                buttonReScanDevices.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        }
    };

    private void initBluetooth(){
        BluetoothUtil.init(this);
        bluetoothUtil = BluetoothUtil.getInstance();
        arrayAdapterDevices = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,deviceNames);
        listViewBluetoothDevices.setAdapter(arrayAdapterDevices);

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothUtil.ACTION_BLUETOOTH_CLIENT_OPEN);
        intentFilter.addAction(BluetoothUtil.ACTION_BLUETOOTH_CLIENT_ERROR);
        registerReceiver(bluetoothReceiver, intentFilter);

        //点击列表项建立连接
        listViewBluetoothDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                bluetoothUtil.connectBluetoothDevice(deviceMap.get(deviceNames.get(i)));
                DataUtil.connectDeviceName = deviceNames.get(i);
                progressBar.setVisibility(View.VISIBLE);
            }
        });
    }

    private void startClient(){
        switchClient.setText("正在搜寻服务端。。。");
        progressBar.setVisibility(View.VISIBLE);
        deviceMap = new HashMap<String, BluetoothDevice>();
        deviceNames = new ArrayList<String>();
        arrayAdapterDevices = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,deviceNames);
        listViewBluetoothDevices.setAdapter(arrayAdapterDevices);
        bluetoothUtil.startSearch();
    }

}
