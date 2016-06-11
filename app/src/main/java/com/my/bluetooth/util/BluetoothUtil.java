package com.my.bluetooth.util;

import android.app.AlertDialog;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;


import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by yiming on 2016/6/8 0008.
 */
public class BluetoothUtil {
    private String TAG = "Blue";

    private static BluetoothUtil bluetoothUtil;
    public static void init(Context con){
        bluetoothUtil = new BluetoothUtil(con);
    }
    public static BluetoothUtil getInstance(){
        return bluetoothUtil;
    }

    public static boolean HAS_BLUETOOTH = false;//"找不到蓝牙设备"
    public static boolean BLUETOOTH_OPEN = false;//"等待用户开启蓝牙设备"
    public static boolean CLIENT_CONNECT = false;//客户端连接成功

    public static final String ACTION_BLUETOOTH_CLIENT_OPEN = "com.my.bluetooth.utils.BluetoothUtil.bluetooth_client_open";
    public static final String ACTION_BLUETOOTH_CLIENT_ERROR = "com.my.bluetooth.utils.BluetoothUtil.bluetooth_client_error";
    public static final String ACTION_RECEIVE_MESSAGE = "com.my.bluetooth.utils.BluetoothUtil.receive_message";
    public static final String  ACTION_SERVER_OPEN = "com.my.bluetooth.utils.BluetoothUtil.open";
    private Context context;

    //Bluetooth
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;

    public Receive ReceiveData;
    private ClientThread clientThread;
    private BluetoothSocket bluetoothSocket;
    private BluetoothServerSocket bluetoothServerSocket;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    /**
     * 初始化,做一次就好
     */
    public BluetoothUtil(Context con){
        context = con;

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter==null){
            //这台机器上没有蓝牙设备
            return;
        }
        HAS_BLUETOOTH = true;

        if(!bluetoothAdapter.isEnabled()){
            //蓝牙设备没有开启
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            new AlertDialog.Builder(con).setTitle("是否开启蓝牙？")
                    .setNegativeButton("取消",new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .setPositiveButton("开启",new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            bluetoothAdapter.enable();
                            if(bluetoothAdapter.isEnabled()){
                                BLUETOOTH_OPEN = true;
                            }
                            dialogInterface.dismiss();
                        }
                    }).create().show();
        }

    }

    /**
     * 检测到可连接的蓝牙设备
     */
    public void startSearch(){
        //bluetoothDevices = bluetoothAdapter.getBondedDevices();//得到最近配对的设备
        bluetoothAdapter.startDiscovery();
    }


    /**
     * 蓝牙客户端连接
     */
    public void connectBluetoothDevice(BluetoothDevice device){
        bluetoothDevice = device;
        if(bluetoothDevice == null){
            return;
        }

        clientThread = new ClientThread();
        clientThread.start();
    }

    /**
     * 关闭蓝牙客户端连接
     */
    public void finishClient(){
        CLIENT_CONNECT = false;
        if(clientThread!=null){
            clientThread.interrupt();
            clientThread = null;
        }
    }

    private class ServerThread extends Thread{
        @Override
        public void run(){
             try {

                 bluetoothServerSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord("server",MY_UUID);Log.i("bluetooth","server open");
                Intent intent = new Intent();
                intent.setAction(ACTION_SERVER_OPEN);
                context.sendBroadcast(intent);

                bluetoothSocket = bluetoothServerSocket.accept();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ClientThread extends Thread{
        @Override
        public void run(){
            try {
                bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            bluetoothAdapter.cancelDiscovery();
            try {
                bluetoothSocket.connect();
                CLIENT_CONNECT = true;Log.i(TAG,"connect succeed");

                Intent intent = new Intent();
                intent.setAction(ACTION_BLUETOOTH_CLIENT_OPEN);
                context.sendBroadcast(intent);

            } catch (IOException e) {
                Log.i(TAG,"蓝牙连接失败!"+e.toString());
                Intent intent = new Intent();
                intent.setAction(ACTION_BLUETOOTH_CLIENT_ERROR);
                context.sendBroadcast(intent);

                e.printStackTrace();
                try {
                    bluetoothSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * 手机通过蓝牙发送消息
     */
    public void SendMessage(char[] outputMessage){
        if(bluetoothSocket==null){
            Log.i(TAG,"蓝牙设备未连接，发送失败");
            //logs = "蓝牙设备未连接，发送失败";
            return;
        }

        //利用 BluetoothSocket获取输出流进行输出
        try {
            OutputStream outputStream = bluetoothSocket.getOutputStream();
            for(int k=0; k < outputMessage.length; k++){
                new DataOutputStream(outputStream).writeByte(outputMessage[k]);
            }
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void InitReceive(){
        ReceiveData = new Receive();
        ReceiveData.start();
    }

    public void finishReceive(){
        if(ReceiveData != null) {
            ReceiveData.interrupt();
            ReceiveData = null;
        }
    }
    /**
     * 读取数据
     */
    private class Receive extends Thread{
        public void run(){
            byte[] buffer = new byte[1024];
            int bytes;
            InputStream inputStream = null;

            try{
                inputStream = bluetoothSocket.getInputStream();
            }catch (Exception e){
                e.printStackTrace();
                return;
            }
            byte[] buffer_data = new byte[10000];
            int pos = 0;
            while (true){
                try {
                    if((bytes=inputStream.read(buffer))>0){
                        for(int i=0;i<bytes;++i){
                            buffer_data[pos++]=buffer[i];
                        }
                        //String s = new String(buffer_data);
                        String s = bytesToHex(buffer_data);
                        //输出s
                        Intent intent = new Intent();
                        intent.setAction(ACTION_RECEIVE_MESSAGE);
                        intent.putExtra("receiveMsg",s);
                        context.sendBroadcast(intent);
                    } else {
                        buffer_data[pos++] = 0x0A;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
