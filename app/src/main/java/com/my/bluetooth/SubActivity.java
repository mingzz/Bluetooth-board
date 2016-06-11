package com.my.bluetooth;

/**
 * Created by yiming on 2016/6/10 0010.
 */
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.my.bluetooth.util.BluetoothUtil;

public class SubActivity extends Activity {
    //蓝牙
    private BluetoothUtil bluetoothUtil;

    //基础控件
    //private RelativeLayout layout1;//基础控件层
    private TextView textViewLogs;
    private ToggleButton LED;
    private Button SetBaud;
    private Button SendButton;
    private EditText SendText;

    private char[] LedOff = {0x0A,0x10,0x00 ,0x01 ,0x00 ,0x03 ,0x06 ,0x00 ,0x00 ,0x00 ,0x00 ,0x00 ,0x00 ,0xAD ,0xCE};
    private char[] LedOn = {0x0A ,0x10 ,0x00 ,0x01 ,0x00 ,0x03 ,0x06 ,0x00 ,0x00 ,0x00 ,0x02 ,0x00 ,0x05 ,0xCC ,0x0D};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);

        initView();
        initBlueTooth();
        initFunction();
        bluetoothUtil.InitReceive();
        textViewLogs.setText("初始化完毕~");
    }

    @Override
    public void onDestroy(){
        bluetoothUtil.finishReceive();
        super.onDestroy();
    }

    /**
     * 初始化界面控件显示
     */
    private void initView() {
        SetBaud = (Button) findViewById(R.id.SetBaud);
        LED = (ToggleButton) findViewById(R.id.LED);
        textViewLogs = (TextView) findViewById(R.id.textViewLogs);
        SendText = (EditText) findViewById(R.id.edit_text_out);
        SendButton = (Button) findViewById(R.id.button_send);
    }

    /**
     * 初始化蓝牙
     */
    private void initBlueTooth(){
        bluetoothUtil = BluetoothUtil.getInstance();

        IntentFilter intentFilter = new IntentFilter(BluetoothUtil.ACTION_RECEIVE_MESSAGE);
        registerReceiver(inputStreamReceiver,intentFilter);
    }

    private void initFunction(){
        SetBaud.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendMessage(LedOn);
            }
        });

        LED.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    sendMessage(LedOn);

                } else {
                    // The toggle is disabled
                    sendMessage(LedOff);
                }
            }
        });

        SendText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        SendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                //View view = getView();
                //if (null != view) {
                    TextView textView = (TextView) findViewById(R.id.edit_text_out);
                int len=textView.getText().length();
                    if(len>0) {
                        char[] message= new char [len+1];
                        for (int i = 0; i < textView.getText().length(); i++) {
                            message[i] = textView.getText().charAt(i);
                        }
                        sendMessage(message);
                    }
                //}
            }
        });
    }

    private TextView.OnEditorActionListener mWriteListener
            = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                int len=view.getText().length();
                if(len>0) {
                    char[] message= new char [len+1];
                    for (int i = 0; i < view.getText().length(); i++) {
                        message[i] = view.getText().charAt(i);
                    }
                    sendMessage(message);
                }
            }
            return true;
        }
    };

    private BroadcastReceiver inputStreamReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String s = intent.getStringExtra("receiveMsg");
            textViewLogs.setText("收到消息："+s);
        }
    };

    /**
     * 蓝牙通信，向别的设备发信息
     */
    private void sendMessage(char[] s){
        String str = "";
        for(int i = 0; i < s.length; ++i) {
            int ch = (int) s[i];
            String s4 = Integer.toHexString(ch);
            str = str + s4;
        }
        textViewLogs.setText(str);
        final char[] ss = s;
        new Thread(){
            public void run(){
                bluetoothUtil.SendMessage(ss);
            }
        }.start();
    }


}