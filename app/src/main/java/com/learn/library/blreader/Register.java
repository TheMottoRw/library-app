package com.learn.library.blreader;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.learn.library.R;
import com.learn.library.fgtit.reader.BluetoothReaderService;
import com.learn.library.fgtit.reader.DeviceListActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class Register extends AppCompatActivity {

    //definition of commands
    private String sDirectory = "";
    private static final String TAG = "BluetoothReader";
    private final static byte CMD_PASSWORD = 0x01;    //Password
    private final static byte CMD_ENROLID = 0x02;        //Enroll in Device
    private final static byte CMD_VERIFY = 0x03;        //Verify in Device
    private final static byte CMD_IDENTIFY = 0x04;    //Identify in Device
    private final static byte CMD_DELETEID = 0x05;    //Delete in Device
    private final static byte CMD_CLEARID = 0x06;        //Clear in Device

    private final static byte CMD_ENROLHOST = 0x07;    //Enroll to Host
    private final static byte CMD_CAPTUREHOST = 0x08;    //Caputre to Host
    private final static byte CMD_MATCH = 0x09;        //Match
    private final static byte CMD_GETIMAGE = 0x30;      //GETIMAGE
    private final static byte CMD_GETCHAR = 0x31;       //GETDATA


    private final static byte CMD_WRITEFPCARD = 0x0A;    //Write Card Data
    private final static byte CMD_READFPCARD = 0x0B;    //Read Card Data
    private final static byte CMD_CARDSN = 0x0E;        //Read Card Sn
    private final static byte CMD_GETSN = 0x10;

    private final static byte CMD_FPCARDMATCH = 0x13;   //

    private final static byte CMD_WRITEDATACARD = 0x14;    //Write Card Data
    private final static byte CMD_READDATACARD = 0x15;     //Read Card Data

    private final static byte CMD_PRINTCMD = 0x20;        //Printer Print
    private final static byte CMD_GETBAT = 0x21;
    private final static byte CMD_UPCARDSN = 0x43;
    private final static byte CMD_GET_VERSION = 0x22;        //Version

    private byte mDeviceCmd = 0x00;
    private boolean mIsWork = false;
    private byte mCmdData[] = new byte[10240];
    private int mCmdSize = 0;

    private Timer mTimerTimeout = null;
    private TimerTask mTaskTimeout = null;
    private Handler mHandlerTimeout;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Layout Views
    private ListView mConversationView;
    private ImageView fingerprintImage;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothReaderService mChatService = null;

    //definition of variables which used for storing the fingerprint template
    public byte mRefData[] = new byte[512]; //enrolled FP template data
    public int mRefSize = 0;


    public byte mCardSn[] = new byte[7];


    public byte mBat[] = new byte[2];  // data of battery status

    private int userId; // User ID number
    private SQLiteDatabase dbInstance; //SQLite database object

    //dynamic setting of the permission for writing the data into phone memory
    private int REQUEST_PERMISSION_CODE = 1;
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    // end bluetooth definition command


    private Button btnRegister,btnConnect,btnConnectionStatus,btnBatteryValue;
    private EditText edtCard;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bl_register);

        edtCard = findViewById(R.id.edtCard);
        edtCard.setClickable(false);
        btnRegister = findViewById(R.id.btnRegister);
        btnConnect = findViewById(R.id.btnConnection);
        btnConnectionStatus = findViewById(R.id.btnConnectionStatus);
        btnBatteryValue = findViewById(R.id.btnBatteryValue);
        btnBatteryValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendCommand(CMD_GETBAT, null, 0);
            }
        });

        edtCard.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {

                // you can call or do what you want with your EditText here

                // yourEditText...
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                recordAttendance();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordAttendance();
            }
        });
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //connect to bluetooth
                Intent serverIntent = new Intent(Register.this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);

            }
        });

        //Initiate bluetooth
        initBluetoothDefault();
    }
    void initBluetoothDefault(){


        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {
            if (mChatService == null) setupChat();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();
    }

    /**
     * configure for the UI components
     */
    private void setupChat() {
        Log.d(TAG, "setupChat()");
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        mConversationView = (ListView) findViewById(R.id.in);
        mConversationView.setAdapter(mConversationArrayAdapter);

        edtCard.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SendCommand(CMD_CARDSN, null, 0);
            }
        });




        mChatService = new BluetoothReaderService(this, mHandler);    // Initialize the BluetoothChatService to perform bluetooth connections
        mOutStringBuffer = new StringBuffer("");                    // Initialize the buffer for outgoing messages
    }


    private void AddStatusList(String text) {
        mConversationArrayAdapter.add(text);
        Toast.makeText(getApplicationContext(),text,Toast.LENGTH_SHORT).show();

    }

    private void AddStatusListHex(byte[] data, int size) {
        String text = "";
        for (int i = 0; i < size; i++) {
            text = text + " " + Integer.toHexString(data[i] & 0xFF).toUpperCase() + "  ";
        }
        mConversationArrayAdapter.add(text);
    }
    private void memcpy(byte[] dstbuf, int dstoffset, byte[] srcbuf, int srcoffset, int size) {
        for (int i = 0; i < size; i++) {
            dstbuf[dstoffset + i] = srcbuf[srcoffset + i];
        }
    }

    private int calcCheckSum(byte[] buffer, int size) {
        int sum = 0;
        for (int i = 0; i < size; i++) {
            sum = sum + buffer[i];
        }
        return (sum & 0x00ff);
    }

    // The Handler that gets information back from the BluetoothChatService
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothReaderService.STATE_CONNECTED:
                            btnConnectionStatus.setText(mConnectedDeviceName);
//                            mToolbar.setSubtitle(mConnectedDeviceName);
                            mConversationArrayAdapter.clear();
                            break;
                        case BluetoothReaderService.STATE_CONNECTING:
                            btnConnectionStatus.setText(getString(R.string.title_connecting));
//                            mToolbar.setSubtitle(R.string.title_connecting);
                            break;
                        case BluetoothReaderService.STATE_LISTEN:
                        case BluetoothReaderService.STATE_NONE:
                            btnConnectionStatus.setText(getString(R.string.not_connected));
//                            mToolbar.setSubtitle(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    if (readBuf.length > 0) {
                        if (readBuf[0] == (byte) 0x1b) {
                            AddStatusListHex(readBuf, msg.arg1);
                        } else {
                            ReceiveCommand(readBuf, msg.arg1);
                        }
                    }
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    btnConnectionStatus.setText("Bluetooth "+mConnectedDeviceName);
                    Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    /**
     * stat the timer for counting
     */
    public void TimeOutStart() {
        if (mTimerTimeout != null) {
            return;
        }
        mTimerTimeout = new Timer();
        mHandlerTimeout = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                TimeOutStop();
                if (mIsWork) {
                    mIsWork = false;
                    //AddStatusList("Time Out");
                }
                super.handleMessage(msg);
            }
        };
        mTaskTimeout = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                mHandlerTimeout.sendMessage(message);
            }
        };
        mTimerTimeout.schedule(mTaskTimeout, 10000, 10000);
    }

    /**
     * stop the timer
     */
    public void TimeOutStop() {
        if (mTimerTimeout != null) {
            mTimerTimeout.cancel();
            mTimerTimeout = null;
            mTaskTimeout.cancel();
            mTaskTimeout = null;
        }
    }

    private void SendCommand(byte cmdid, byte[] data, int size) {
        if (mIsWork) return;

        int sendsize = 9 + size;
        byte[] sendbuf = new byte[sendsize];
        sendbuf[0] = 'F';
        sendbuf[1] = 'T';
        sendbuf[2] = 0;
        sendbuf[3] = 0;
        sendbuf[4] = cmdid;
        sendbuf[5] = (byte) (size);
        sendbuf[6] = (byte) (size >> 8);
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                sendbuf[7 + i] = data[i];
            }
        }
        int sum = calcCheckSum(sendbuf, (7 + size));
        sendbuf[7 + size] = (byte) (sum);
        sendbuf[8 + size] = (byte) (sum >> 8);

        mIsWork = true;
        TimeOutStart();
        mDeviceCmd = cmdid;
        mCmdSize = 0;
        mChatService.write(sendbuf);

        switch (sendbuf[4]) {


            case CMD_GETSN:
                AddStatusList("Get card SN ...");
                break;
            case CMD_CARDSN:
                AddStatusList("Read Card SN ...");
                break;

            case CMD_GETBAT:
                AddStatusList("Get Battery Value ...");
                break;
        }
    }

    /**
     * Received the response from the device
     * @param databuf the data package response from the device
     * @param datasize the size of the data package
     */
    private void ReceiveCommand(byte[] databuf, int datasize) {
        if (mDeviceCmd == CMD_GETIMAGE) { //receiving the image data from the device

            Log.d(TAG,"Image not used");

        } else { //other data received from the device
            // append the databuf received into mCmdData.
            memcpy(mCmdData, mCmdSize, databuf, 0, datasize);
            mCmdSize = mCmdSize + datasize;
            int totalsize = (byte) (mCmdData[5]) + ((mCmdData[6] << 8) & 0xFF00) + 9;
            if (mCmdSize >= totalsize) {
                mCmdSize = 0;
                mIsWork = false;
                TimeOutStop();

                //parsing the mCmdData
                if ((mCmdData[0] == 'F') && (mCmdData[1] == 'T')) {
                    switch (mCmdData[4]) {
                        case CMD_ENROLHOST:
                        break;
                        case CMD_CAPTUREHOST:
                        break;
                        case CMD_UPCARDSN:
                        case CMD_CARDSN: {
                            int size = (byte) (mCmdData[5]) + ((mCmdData[6] << 8) & 0xF0) - 1;
                            if (size > 0) {
                                memcpy(mCardSn, 0, mCmdData, 8, size);
                                String cardID = Integer.toHexString(mCardSn[0] & 0xFF) + Integer.toHexString(mCardSn[1] & 0xFF) + Integer.toHexString(mCardSn[2] & 0xFF) + Integer.toHexString(mCardSn[3] & 0xFF) + Integer.toHexString(mCardSn[4] & 0xFF) + Integer.toHexString(mCardSn[5] & 0xFF) + Integer.toHexString(mCardSn[6] & 0xFF);
                                edtCard.setText(cardID);
                                AddStatusList("Read Card SN Succeed:" + cardID);
                            } else {
                                Toast.makeText(getApplicationContext(), "Card not found", Toast.LENGTH_LONG).show();
                                AddStatusList("Search Fail");
                            }
                        }
                        break;
                        case CMD_GETSN:
                        break;
                        case CMD_GETBAT: {
                            int size = (byte) (mCmdData[5]) + ((mCmdData[6] << 8) & 0xFF00) - 1;
                            if (size > 0) {
                                memcpy(mBat, 0, mCmdData, 8, size);
                                double batVal = mBat[0] / 10.0;
                                double batPercent = ((batVal - 3.45) / 0.75) * 100;
                                DecimalFormat decimalFormat = new DecimalFormat("0.00");
                                String batPercentage = decimalFormat.format(batPercent) + " %";
                                AddStatusList("Battery Percentage:" + batPercentage);
                            } else
                                AddStatusList("Search Fail");
                        }
                        break;
                        case CMD_GET_VERSION:
                        break;
                    }
                }
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    // Attempt to connect to the device
                    mChatService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Log.d(TAG, "Bluetooth not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    void recordAttendance() {

        RequestQueue queue = Volley.newRequestQueue(Register.this);
        String url = "http://192.168.43.161/RUT/library/api/requests/attendance.php";
        Log.d("Req", url);
// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("url response",response);
                        // Display the first 500 characters of the response string.
                        try{
                            JSONObject obj = new JSONObject(response);
                            if(obj.getString("status").equals("ok")){
                                AddStatusList("Thank you,"+obj.getString("student")+" For your attendance");
                                Toast.makeText(getApplicationContext(),"Thank you,"+obj.getString("student")+" For your attendance",Toast.LENGTH_SHORT).show();
                            } else if(obj.getString("status").equals("notexist"))
                                Toast.makeText(getApplicationContext(),"Sorry student not found",Toast.LENGTH_LONG).show();
                        }catch (JSONException ex){

                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "error " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            public HashMap<String,String> getParams() throws AuthFailureError {
                HashMap<String,String> params = new HashMap<>();
                params.put("cate","attendance");
                params.put("cardno",edtCard.getText().toString());
                return params;
            }
        };

// Add the request to the RequestQueue.
        queue.add(stringRequest);


    }


}