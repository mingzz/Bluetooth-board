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
import android.os.StrictMode;
import android.util.Log;
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
    private TextView textViewLogs;
    private ToggleButton LED;
    private Button Decrease;
    private Button Increase;
    private Button SetBaud;
    private Button SendButton;
    private EditText SendText;

    private char[] BreatheOff = {0x0A,0x10,0x00 ,0x01 ,0x00 ,0x02, 0x04 ,0x00 ,0x00 ,0x00 ,0x00 , 0x17, 0x47};
    private char[] BreatheOn = {0x0A, 0x10, 0x00, 0x01, 0x00, 0x02, 0x04, 0x00, 0x01, 0x00, 0x00, 0x46, 0x87};
    private char[] LedOff = {0x0A,0x10,0x00 ,0x01 ,0x00 ,0x03 ,0x06 ,0x00 ,0x00 ,0x00 ,0x00 ,0x00 ,0x00 ,0xAD ,0xCE};
    private char[] Lv1 = {0x0A, 0x10, 0x00, 0x01, 0x00, 0x03, 0x06, 0x00, 0x00, 0x00, 0x02, 0x00, 0x01, 0xCD, 0xCE};
    private char[] Lv2 = {0x0A ,0x10 ,0x00 ,0x01 ,0x00 ,0x03 ,0x06 ,0x00 ,0x00 ,0x00 ,0x02 ,0x00 ,0x05 ,0xCC ,0x0D};
    private char[] Lv3 = {0x0A, 0x10, 0x00, 0x01, 0x00, 0x03, 0x06, 0x00, 0x00, 0x00, 0x02, 0x00, 0x10, 0x0D, 0xC2};
    private char[] Lv4 = {0x0A ,0x10 ,0x00 ,0x01 ,0x00 ,0x03 ,0x06 ,0x00 ,0x00 ,0x00 ,0x02 ,0x00 ,0x20, 0x0D, 0xD6};

    private int newline = 0;
    private int showline = 0;
    private int lastlen = 0;
    private int LedLevel = 0;
    private String showMsg = "";

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
        Increase = (Button) findViewById(R.id.Increase);
        Decrease = (Button) findViewById(R.id.Decrease);
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

    /**
     * 初始化功能
     */
    private void initFunction(){
        SetBaud.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                newline++;
                sendMessage(BreatheOn);
            }
        });

        LED.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    newline++;
                    sendMessage(BreatheOff);

                } else {
                    // The toggle is disabled
                    newline++;
                    sendMessage(BreatheOn);
                }
            }
        });

        Increase.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                newline++;
                LedLevel = (LedLevel == 4) ? LedLevel : LedLevel+1;
                switch(LedLevel){
                    case 0:
                        break;
                    case 1:
                        sendMessage(Lv1);break;
                    case 2:
                        sendMessage(Lv2);break;
                    case 3:
                        sendMessage(Lv3);break;
                    case 4:
                        sendMessage(Lv4);break;
                    default:
                        break;
                }
            }
        });

        Decrease.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                newline++;
                LedLevel = (LedLevel == 0) ? LedLevel : LedLevel-1;
                switch(LedLevel){
                    case 0:
                        sendMessage(LedOff);break;
                    case 1:
                        sendMessage(Lv1);break;
                    case 2:
                        sendMessage(Lv2);break;
                    case 3:
                        sendMessage(Lv3);break;
                    default:
                        break;
                }
            }
        });

        SendText.setOnEditorActionListener(mWriteListener);


         //初始化按钮，发送左边框里的内容
        SendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                //View view = getView();
                //if (null != view) {
                TextView textView = (TextView) findViewById(R.id.edit_text_out);
                int len=textView.getText().length();
                Log.d(String.valueOf(len), "length is :");
                    if(len>0) {
                        char[] message= new char [len/2];
                        int sum = 0, count = 0, j = 0;
                        for (int i = 0; i < textView.getText().length(); i++) {
                            char tmp = textView.getText().charAt(i);
                            Log.d(String.valueOf(tmp), "tmp is :");
                            if(tmp <= '9' && tmp >= '0')
                                sum += (tmp - '0');
                            else if(tmp <= 'F' && tmp >= 'A')
                                    sum += tmp - 'A' + 10;
                                else
                                    sum += tmp - 'a' + 10;
                            if(count == 0) {
                                sum *= 16;
                                ++count;
                            } else {
                                message[j++] = (char)sum;
                                sum = 0;
                                count = 0;
                            }
                        }
                        newline++;
                        Log.d(String.valueOf(message), "message is :");
                        sendMessage(message);
                    }
                //}
            }
        });
    }

    /**
     * 设置发送框，可以用回车触发
     */
    private TextView.OnEditorActionListener mWriteListener
            = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                int len=view.getText().length();
                if(len>0) {
                    char[] message= new char [len/2];
                    int sum = 0, count = 0, j = 0;
                    for (int i = 0; i < view.getText().length(); i++) {
                        char tmp = view.getText().charAt(i);
                        Log.d(String.valueOf(tmp), "tmp is :");
                        if(tmp <= '9' && tmp >= '0')
                            sum += (tmp - '0');
                        else if(tmp <= 'F' && tmp >= 'A')
                                sum += tmp - 'A' + 10;
                            else
                                sum += tmp - 'a' + 10;
                        if(count == 0) {
                            sum *= 16;
                            ++count;
                        } else {
                            message[j++] = (char)sum;
                            sum = 0;
                            count = 0;
                        }
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
            String p = intent.getStringExtra("len");
            int len = 0, i = 0;
            while(i < p.length()) {
                len *= 10;
                len += p.charAt(i) - '0';
                ++i;
            }
            //s.length();
            String s2 = s.substring(lastlen, len*2);
            if(newline > showline){
                showline++;
                showMsg = showMsg + '\n';
            }
            showMsg = showMsg + s2;
            textViewLogs.setText("收到消息："+showMsg);
            lastlen = len * 2;
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
            if(s4.length() == 1) s4 = '0' + s4;
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