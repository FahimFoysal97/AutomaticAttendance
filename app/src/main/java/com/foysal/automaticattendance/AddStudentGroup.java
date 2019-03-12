package com.foysal.automaticattendance;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static java.lang.Thread.sleep;

public class AddStudentGroup extends AppCompatActivity {


    WifiManager wifiManager;
    WifiP2pManager wifiP2pManager;
    WifiP2pManager.Channel channel;
    BroadcastReceiver broadcastReceiver;
    IntentFilter intentFilter;
    Vector<WifiP2pDevice> connectedDevices = new Vector<>();
    Vector<String> connectedDevicesName = new Vector<>();
    List<WifiP2pDevice> peers = new ArrayList<>();
    WifiP2pManager.PeerListListener peerListListener;

    boolean add = false;
    boolean isDeviceListChanged = false;
    boolean isRefreshing = false;
    boolean connectToDevice = false;
    boolean selectDevice = true;

    Thread connectToDeviceThread;
    Thread selectDeviceThread;

    ListView listView;


    ServerClass serverClass;
    ClientClass clientClass;
    SendReceive sendReceive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student_group);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null && !wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        /*else if(wifiManager != null && wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(false);
            wifiManager.setWifiEnabled(true);
        }*/

        listView = findViewById(R.id.listView);
        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this,getMainLooper(),null);
        broadcastReceiver = new WiFiDirectBroadcastReceiver(wifiP2pManager, channel, this);
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        ((Button)findViewById(R.id.button_Refresh)).setText("Start connecting");

        isRefreshing=false;

        peerListListener = peerlist -> {

                if(!peerlist.getDeviceList().equals(peers)){
                    peers.clear();
                    peers.addAll(peerlist.getDeviceList());

                    System.out.println("Device list changed");
                    System.out.println(peers);
                    isDeviceListChanged = true;
                    System.out.println("Device list changed 2");
                }

            if( (peers.size()==0) && connectedDevices.isEmpty()  )Toast.makeText(getApplicationContext(),"No device found",Toast.LENGTH_SHORT).show();
        };

        wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                //Toast.makeText(getApplicationContext(), "Searching Devices...", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailure(int reason) {
                /*connectionStatus.setText("Discovery starting failed");*/
            }
        });

        findViewById(R.id.button_Refresh).setOnClickListener(v->{
            if(!isRefreshing){
                isRefreshing = true;
                startConnecting();
                ((Button) findViewById(R.id.button_Refresh)).setText("Stop Connecting");
            }
            else {
                isRefreshing = false;
                stopConnecting();
                ((Button) findViewById(R.id.button_Refresh)).setText("Start Connecting");
            }

        });

        findViewById(R.id.button_Back).setOnClickListener(v-> finish());

        findViewById(R.id.button_next).setOnClickListener( v -> {
            if(connectedDevices.isEmpty())return;
            if(isRefreshing)selectDeviceThread.interrupt();
            setContentView(R.layout.activity_add_student_group_done);
            findViewById(R.id.button_done).setOnClickListener(v1 -> {
                //finish();
                String groupName,session;
                groupName = ((EditText)findViewById(R.id.editText_group_name)).getText().toString();
                session = ((EditText)findViewById(R.id.editText_session)).getText().toString();
                SQLiteDatabase sqLiteDatabase = this.openOrCreateDatabase("TeacherPanel",MODE_PRIVATE,null);
                sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS StudentGroupList (groupname varchar PRIMARY KEY, session varchar) ");
                String string = "select * from StudentGroupList where groupname = ?" ;
                Cursor c = sqLiteDatabase.rawQuery(string ,new String[]{groupName});
                //int i = c.getColumnIndex("groupname");
                c.moveToFirst();
                if(c!=null && c.getCount()>0){
                    ((TextView)findViewById(R.id.textView_warning)).setText("GroupName exist");
                }
                else {
                    String string2 = "Insert into StudentGroupList (groupname,session) values (?,?)";
                    sqLiteDatabase.rawQuery(string2 ,new String[]{groupName,session});
                    String str = groupName.replaceAll(" ","_")+"_"+session;
                    sqLiteDatabase.execSQL("CREATE TABLE " + str +" (id varchar primary key, name varchar, deviceaddress varchar unique)");
                    finish();
                }
                c.close();
            });
        });




    }



    WifiP2pDevice device;
    void startConnecting(){
        wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {

            }
        });

        selectDeviceThread = new Thread(() -> {

            System.out.println("Into the selectThread");

            List<WifiP2pDevice> peerList;
            int i = 0;
            while (!selectDeviceThread.isInterrupted()) {
                System.out.println("Into the selectThread 2");
                peerList = new ArrayList<>(peers);
                System.out.println("selectDevice : " + selectDevice);
                System.out.println("isDeviceListChanged : " + isDeviceListChanged);
                if (selectDevice && isDeviceListChanged) {
                    System.out.println("Into the selectThread 3 (if) ");
                    if (i < peerList.size()) {
                        device = peerList.get(i);
                        i++;
                        selectDevice = false;

                        //connectToDevice = true;




                        if (!connectedDevices.contains(device)) {
                            WifiP2pConfig wifiP2pConfig = new WifiP2pConfig();
                            wifiP2pConfig.deviceAddress = device.deviceAddress;
                            add = false;
                            System.out.println("trying to connect to " + device.deviceName);

                            wifiP2pManager.connect(channel, wifiP2pConfig, new WifiP2pManager.ActionListener() {
                                @Override
                                public void onSuccess() {
                                    System.out.println("Connection making successful with " + device.deviceName);
                                }

                                @Override
                                public void onFailure(int reason) {
                                    //Toast.makeText(getApplicationContext(), "Not connected = " + reason, Toast.LENGTH_SHORT).show();
                                }
                            });

                            try {
                                sleep(6000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (add) {
                                System.out.println(device.deviceName + " inside adding");
                                connectedDevices.add(device);
                                connectedDevicesName.add(device.deviceName);

                                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(AddStudentGroup.this.getApplicationContext(), android.R.layout.simple_list_item_1, connectedDevicesName);

                                listView.post(() -> listView.setAdapter(arrayAdapter));

                                wifiP2pManager.removeGroup(channel, new WifiP2pManager.ActionListener() {
                                    @Override
                                    public void onSuccess() {
                                        System.out.println("Disconnected from device : " + device.deviceName);
                                        selectDevice = true;
                                    }

                                    @Override
                                    public void onFailure(int reason) {

                                    }
                                });

                            } else {
                                System.out.println("Canceling connection");
                                wifiP2pManager.cancelConnect(channel, new WifiP2pManager.ActionListener() {
                                    @Override
                                    public void onSuccess() {
                                        selectDevice = true;
                                        System.out.println("Canceled connection");
                                    }

                                    @Override
                                    public void onFailure(int reason) {

                                    }
                                });
                            }

                            //AddStudentGroup.this.connectToDevice = false;

                        }













                    } else {
                        System.out.println("Into the selectThread 3 (else) ");
                        i = 0;
                        isDeviceListChanged = false;
                        selectDevice = true;

                        wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onFailure(int reason) {

                            }
                        });
                    }
                        //else selectDeviceThread.interrupt();
                }
                System.out.println("Select  device loop complete ");
            }
        });

        connectToDeviceThread = new Thread(() -> {
            while (!connectToDeviceThread.isInterrupted()) {

                if (AddStudentGroup.this.connectToDevice) {
                    System.out.println("Into the connectThread");
                    connectToDevice = false;
                    if (!connectedDevices.contains(device)) {
                        WifiP2pConfig wifiP2pConfig = new WifiP2pConfig();
                        wifiP2pConfig.deviceAddress = device.deviceAddress;
                        add = false;
                        System.out.println("trying to connect to " + device.deviceName);

                        wifiP2pManager.connect(channel, wifiP2pConfig, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {
                                System.out.println("Connection making successful with " + device.deviceName);
                            }

                            @Override
                            public void onFailure(int reason) {
                                //Toast.makeText(getApplicationContext(), "Not connected = " + reason, Toast.LENGTH_SHORT).show();
                            }
                        });

                        try {
                            sleep(6000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (add) {
                            System.out.println(device.deviceName + " inside adding");
                            connectedDevices.add(device);
                            connectedDevicesName.add(device.deviceName);

                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(AddStudentGroup.this.getApplicationContext(), android.R.layout.simple_list_item_1, connectedDevicesName);

                            listView.post(() -> listView.setAdapter(arrayAdapter));

                            wifiP2pManager.removeGroup(channel, new WifiP2pManager.ActionListener() {
                                @Override
                                public void onSuccess() {
                                    System.out.println("Disconnected from device : " + device.deviceName);
                                }

                                @Override
                                public void onFailure(int reason) {

                                }
                            });

                        } else {
                            System.out.println("Canceling connection");
                            wifiP2pManager.cancelConnect(channel, new WifiP2pManager.ActionListener() {
                                @Override
                                public void onSuccess() {
                                    System.out.println("Canceled connection");

                                }

                                @Override
                                public void onFailure(int reason) {

                                }
                            });
                        }
                        try {
                            sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        AddStudentGroup.this.connectToDevice = false;
                        AddStudentGroup.this.selectDevice = true;
                    }
                }
                System.out.println("Connect to device loop complete ");
            }
        });

        if(!selectDeviceThread.isAlive())selectDeviceThread.start();
        //if(!connectToDeviceThread.isAlive())connectToDeviceThread.start();

    }

    void stopConnecting(){

            selectDeviceThread.interrupt();
            connectToDeviceThread.interrupt();

    }

    WifiP2pManager.ConnectionInfoListener connectionInfoListener =  wifiP2pInfo -> {

            if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
                Toast.makeText(getApplicationContext(), "inside info", Toast.LENGTH_SHORT).show();
                add = true;
            }

        final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;
        if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
            //connectionStatus.setText("Host");
            serverClass = new ServerClass();
            serverClass.start();
        } else if (wifiP2pInfo.groupFormed) {
            //connectionStatus.setText("Client");
            clientClass = new ClientClass(groupOwnerAddress);
            clientClass.start();
        }

    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver,intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(wifiManager != null && wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(false);
        }
    }



    class ServerClass extends Thread{

        Socket socket;
        ServerSocket serverSocket;

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(8888);
                socket = serverSocket.accept();
                sendReceive = new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch(msg.what){
                case 1 :
                    byte[] readbuff = (byte[])msg.obj;
                    String tempMsg = new String(readbuff,0,msg.arg1);
                    String id,name,done;
                    try {
                        JSONObject jsonObject = new JSONObject(tempMsg);
                        id = jsonObject.getString("id");
                        name = jsonObject.getString("name");
                        done = jsonObject.getString("done");
                        if(done.equals("true")){
                            sendReceive.write(("1").getBytes());
                        }
                    } catch (JSONException e) {
                        Toast.makeText(AddStudentGroup.this, "Wrong Json", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }


                    //what happens with the massage
                    //readMsgBox.setText(tempMsg);
                    break;
            }
            return true;
        }
    });


    class SendReceive extends Thread{

        Socket socket;
        InputStream inputStream;
        OutputStream outputStream;

        SendReceive(Socket socket){
            this.socket = socket;
            try {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            while(socket!=null){
                try {
                    bytes = inputStream.read(buffer);
                    if(bytes>0){
                        handler.obtainMessage(1,bytes,-1,buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes){
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    class ClientClass extends Thread{

        Socket socket;
        String hostAdd;

        ClientClass(InetAddress inetAddress){
            hostAdd = inetAddress.getHostAddress();
            socket = new Socket();
        }

        @Override
        public void run() {
            try {
                socket.connect(new InetSocketAddress(hostAdd,8888),500);
                sendReceive = new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }










    class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

        private WifiP2pManager wifiP2pManager;
        private WifiP2pManager.Channel channel;
        private AddStudentGroup addStudentGroup;

        public WiFiDirectBroadcastReceiver(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel, AddStudentGroup addStudentGroup){
            this.wifiP2pManager = wifiP2pManager;
            this.channel = channel;
            this.addStudentGroup = addStudentGroup;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)){
                if(intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE,-1) == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                    Toast.makeText(context,"Wifi is on",Toast.LENGTH_SHORT).show();
                }
                else Toast.makeText(context,"Wifi is off",Toast.LENGTH_SHORT).show();
            }
            else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){
                if(wifiP2pManager!=null){
                    wifiP2pManager.requestPeers(channel,addStudentGroup.peerListListener);
                }
            }
            else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
                if(wifiP2pManager == null){
                    return;
                }
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if(networkInfo.isConnected()){

                    synchronized (this){
                        wifiP2pManager.requestConnectionInfo(channel,addStudentGroup.connectionInfoListener);
                    }
                }
                else {
                    //mainActivity.connectionStatus.setText("Device disconnected");
                }
            }
            else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){

            }
        }
    }

}
