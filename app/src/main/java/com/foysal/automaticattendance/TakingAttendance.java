package com.foysal.automaticattendance;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import static java.lang.Thread.sleep;

public class TakingAttendance extends AppCompatActivity {

    String sheetName,groupName;
    ArrayList<String> studentName = new ArrayList<>();
    ArrayList<String> studentId = new ArrayList<>();
    ArrayList<String> studentDeviceAddress = new ArrayList<>();
    boolean[] present;
    int totalStudent;
    ListView studentListView;
    ArrayList<String> studentArrayList = new ArrayList<>();
    ArrayAdapter adapter;



    WifiManager wifiManager;
    WifiP2pManager wifiP2pManager;
    WifiP2pManager.Channel channel;
    BroadcastReceiver broadcastReceiver;
    IntentFilter intentFilter;
    //Vector<WifiP2pDevice> connectedDevices = new Vector<>();
    //Vector<String> connectedDevicesName = new Vector<>();
    List<WifiP2pDevice> peers = new ArrayList<>();
    WifiP2pManager.PeerListListener peerListListener;
    WifiP2pDevice device;


    boolean add = false;
    boolean remove = false;
    boolean isDeviceListChanged = false;
    boolean isRefreshing = false;
    boolean connectToDevice = false;
    boolean selectDevice = true;

    //Thread connectToDeviceThread;
    Thread selectDeviceThread;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taking_attendance);

        studentListView = findViewById(R.id.listView_studentList_taking_attendance);
        //arrayAdapter = new ArrayAdapter<>(AddStudentGroup.this.getApplicationContext(), android.R.layout.simple_list_item_1, studentList);
        adapter = new ArrayAdapter<>(TakingAttendance.this.getApplicationContext(),android.R.layout.simple_list_item_1, studentArrayList);
        studentListView.setAdapter(adapter);
        sheetName = getIntent().getExtras().getString("sheetName");
        groupName = getIntent().getExtras().getString("groupName");
        setAllData();
        totalStudent = studentName.size();
        present = new boolean[totalStudent];
        for(boolean p : present){
            p = false;
        }





        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null && !wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this,getMainLooper(),null);
        broadcastReceiver = new WiFiDirectBroadcastReceiver(wifiP2pManager, channel, this);
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

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

            //if( (peers.size()==0) && connectedDevices.isEmpty()  )Toast.makeText(getApplicationContext(),"No device found",Toast.LENGTH_SHORT).show();
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


    }

    void setAllData(){
        SQLiteDatabase sqLiteDatabase = this.openOrCreateDatabase("TeacherPanel",MODE_PRIVATE,null);
        String str = "Select * from " + groupName + "order by id";
        Cursor c = sqLiteDatabase.rawQuery(str,null);
        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                studentName.add(c.getString(c.getColumnIndex("name")));
                studentId.add(c.getString(c.getColumnIndex("id")));
                studentDeviceAddress.add(c.getString(c.getColumnIndex("deviceaddress")));
                //sqLiteDatabase.execSQL(s1);
                c.moveToNext();
            }
            c.close();
        }
    }



    public void doneButtonClicked(View view){
        if(isRefreshing){
            Toast.makeText(getApplicationContext(),"Stop Roll Call First",Toast.LENGTH_SHORT);
            return;
        }
        SQLiteDatabase sqLiteDatabase = this.openOrCreateDatabase("TeacherPanel",MODE_PRIVATE,null);
        ContentValues values = new ContentValues();
        Date today = new Date();
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a");
        String dateToStr = format.format(today);
        values.put("date",dateToStr);
        for(int i = 0; i<totalStudent; i++){
            if(present[i]){
                values.put(studentId.get(i),1);
            }
            else {
                values.put(studentId.get(i),0);
            }
        }
        sqLiteDatabase.insert(sheetName,null,values);
        finish();
    }






















    public void startConnecting(View view){

        if(!isRefreshing){
            isRefreshing = true;
            ((Button)findViewById(R.id.button_done_takingAttendance)).setText("Stop Roll Call");
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




                        if (studentDeviceAddress.contains(device.deviceAddress)) {
                            WifiP2pConfig wifiP2pConfig = new WifiP2pConfig();
                            //wifiP2pConfig.deviceAddress = device.deviceAddress;
                            add = false;
                            System.out.println("trying to connect to " + device.deviceName);
                            //serverSocket = null;
                            /*try {
                                serverSocket = new ServerSocket(8888);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }*/
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
                                sleep(3000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (add) {
                                remove = false;
                                System.out.println(device.deviceName + " inside adding");
                                present[studentDeviceAddress.indexOf(device.deviceAddress)]=true;
                                //connectedDevices.add(device);
                                //connectedDevicesName.add(device.deviceName);
                                //SQLiteDatabase sqLiteDatabase = this.openOrCreateDatabase("TeacherPanel",MODE_PRIVATE,null);
                                String str = "ID : " + studentId.get(studentDeviceAddress.indexOf(device.deviceAddress))+
                                        "Name : " + studentName.get(studentDeviceAddress.indexOf(device.deviceAddress));
                                studentArrayList.add(str);
                                findViewById(R.id.textView_presentNumber).post(()->((TextView)findViewById(R.id.textView_presentNumber)).setText("Present : "+studentArrayList.size()));
                                //adapter.notifyDataSetChanged();


                                studentListView.post(() -> {
                                    studentListView.setAdapter(adapter);
                                    adapter.notifyDataSetChanged();
                                });
                                //listView.post(() -> listView.setAdapter(arrayAdapter));



                                //listView.post(() -> listView.setAdapter(arrayAdapter));

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
        }
        else {
            isRefreshing = false;
            ((Button)findViewById(R.id.button_done_takingAttendance)).setText("Start Roll Call");
            selectDeviceThread.interrupt();
        }

        /*connectToDeviceThread = new Thread(() -> {
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
                            remove = false;
                            //System.out.println(device.deviceName + " inside adding");
                            connectedDevices.add(device);
                            connectedDevicesName.add(device.deviceName);

                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(AddStudentGroup.this.getApplicationContext(), android.R.layout.simple_list_item_1, connectedDevicesName);

                            listView.post(() -> listView.setAdapter(arrayAdapter));
                            while(!remove){

                            }

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
        });*/

        if(!selectDeviceThread.isAlive())selectDeviceThread.start();
        //if(!connectToDeviceThread.isAlive())connectToDeviceThread.start();

    }


    void stopConnecting(){

        selectDeviceThread.interrupt();
        //connectToDeviceThread.interrupt();

    }

    WifiP2pManager.ConnectionInfoListener connectionInfoListener =  wifiP2pInfo -> {

            /*if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
                Toast.makeText(getApplicationContext(), "inside info", Toast.LENGTH_SHORT).show();
                add = true;
            }*/

        //final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;
        if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
            //connectionStatus.setText("Host");

            add = true;
        } else if (wifiP2pInfo.groupFormed) {
            //connectionStatus.setText("Client");

            add = true;
        }

    };



    class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

        private WifiP2pManager wifiP2pManager;
        private WifiP2pManager.Channel channel;
        private TakingAttendance takingAttendance;

        public WiFiDirectBroadcastReceiver(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel, TakingAttendance takingAttendance){
            this.wifiP2pManager = wifiP2pManager;
            this.channel = channel;
            this.takingAttendance = takingAttendance;
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
                    wifiP2pManager.requestPeers(channel,takingAttendance.peerListListener);
                }
            }
            else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
                if(wifiP2pManager == null){
                    return;
                }
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if(networkInfo.isConnected()){

                    synchronized (this){
                        wifiP2pManager.requestConnectionInfo(channel,takingAttendance.connectionInfoListener);
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
