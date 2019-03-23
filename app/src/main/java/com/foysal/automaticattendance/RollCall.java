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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import static java.lang.Thread.sleep;

public class RollCall extends AppCompatActivity {


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
    //String id,name,done;
    //ArrayList<Student> studentList = new ArrayList<>();
    //ArrayList<String> studentInfoList = new ArrayList<>();
    //ArrayAdapter arrayAdapter;
    ArrayList<String> studentName = new ArrayList<>();
    ArrayList<String> studentId = new ArrayList<>();
    ArrayList<String> studentDeviceAddress = new ArrayList<>();
    ArrayList<String> studentArrayList = new ArrayList<>();
    ArrayList<String> studentArrayList2 = new ArrayList<>();
    ArrayAdapter adapter;
    ArrayAdapter adapter2;
    //ServerSocket serverSocket;
    boolean[] present;
    int totalStudent;
    ListView studentListView,studentListView2;

    volatile boolean add = false;
    volatile boolean remove = false;
    volatile boolean isDeviceListChanged = false;
    volatile boolean isRefreshing = false;
    volatile boolean connectToDevice = false;
    volatile boolean selectDevice = true;
    volatile boolean wait = true;
    String groupName,sheetName;
    //Thread connectToDeviceThread;
    Thread selectDeviceThread;
    //Thread waitThread;

    //ListView listView;


    //ServerClass serverClass;
    //ClientClass clientClass;
    //ServerSendReceive serverSendReceive;
    //ClientSendReceive clientSendReceive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roll_call);

        sheetName = getIntent().getExtras().getString("sheetName");
        groupName = getIntent().getExtras().getString("groupName");
        adapter2 = new ArrayAdapter(RollCall.this.getApplicationContext(), android.R.layout.simple_list_item_1, studentArrayList2);
        studentListView2 = findViewById(R.id.listView_studentList_rollCall2);
        studentListView2.setAdapter(adapter2);
        setAllData();
        totalStudent=studentName.size();
        adapter = new ArrayAdapter(RollCall.this.getApplicationContext(), android.R.layout.simple_list_item_1, studentArrayList);
        studentListView = findViewById(R.id.listView_studentList_rollCall);
        studentListView.setAdapter(adapter);

        present = new boolean[totalStudent];
        for(int i=0; i<present.length; i++){
            present[i] = false;
        }
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null && !wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        /*else if(wifiManager != null && wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(false);
            wifiManager.setWifiEnabled(true);
        }*/

        //listView = findViewById(R.id.listView_studentList_rollCall);
        //arrayAdapter = new ArrayAdapter<>(AddStudentGroup2.this.getApplicationContext(), android.R.layout.simple_list_item_1, studentInfoList);
        //listView.setAdapter(arrayAdapter);
        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this,getMainLooper(),null);
        broadcastReceiver = new WiFiDirectBroadcastReceiver(wifiP2pManager, channel, this);
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        adapter = new ArrayAdapter(RollCall.this.getApplicationContext(), android.R.layout.simple_list_item_1, studentArrayList);


        //((Button)findViewById(R.id.button_Refresh)).setText("Start connecting");
        /*serverSocket = null;
        try {
            serverSocket = new ServerSocket(8888);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        isRefreshing=false;

        peerListListener = peerlist -> {

                if(!peerlist.getDeviceList().equals(peers)){
                    peers.clear();
                    peers.addAll(peerlist.getDeviceList());

                    //System.out.println("Device list changed");
                    System.out.println(peers);
                    isDeviceListChanged = true;
                    //System.out.println("Device list changed 2");
                }

            //if( (peers.size()==0))Toast.makeText(getApplicationContext(),"No device found",Toast.LENGTH_SHORT).show();
        };

        /*wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                //Toast.makeText(getApplicationContext(), "Searching Devices...", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailure(int reason) {
                *//*connectionStatus.setText("Discovery starting failed");*//*
            }
        });*/

        findViewById(R.id.button_startRollCall_rollCall).setOnClickListener(v->{
            if(!isRefreshing){
                isRefreshing = true;
                startConnecting();
                ((Button) findViewById(R.id.button_startRollCall_rollCall)).setText("Stop Roll Call");
            }
            else {
                isRefreshing = false;
                stopConnecting();
                ((Button) findViewById(R.id.button_startRollCall_rollCall)).setText("Start Roll Call");
            }

        });

        /*findViewById(R.id.button_Back).setOnClickListener(v-> {
            if(selectDeviceThread!=null && selectDeviceThread.isAlive())selectDeviceThread.interrupt();
            finish();
        });*/

        /*findViewById(R.id.button_next).setOnClickListener( v -> {

        });*/

        studentListView2.setOnItemClickListener((parent, view, position, id) -> {
            if(isRefreshing){
                Toast.makeText(getApplicationContext(),"Stop roll call first",Toast.LENGTH_SHORT);
                return;
            }
            String s = studentListView2.getItemAtPosition(position).toString();
            String str = "";
            int index = -1;
            for(int i = 0; i<studentName.size(); i++){
                str = "ID : " + studentId.get(i)+
                        "\nName : " + studentName.get(i);
                if(str.equals(s)){
                    index = i;
                    break;
                }
            }
            if(index>-1 && !str.equals("")){
                present[index]=true;
                studentArrayList2.remove(str);
                studentArrayList.add(str);
                //adapter2.notifyDataSetChanged();

                studentListView2.post(() -> {
                    studentListView2.setAdapter(adapter2);
                    adapter2.notifyDataSetChanged();
                });
                studentListView.post(() -> {
                    studentListView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                });
                ((TextView)findViewById(R.id.textView_presentNumber_rollCall)).post(() -> {
                    ((TextView)findViewById(R.id.textView_presentNumber_rollCall)).setText("Present : "+studentArrayList.size());
                });
                ((TextView)findViewById(R.id.textView_absentNumber_rollCall)).post(() -> {
                    ((TextView)findViewById(R.id.textView_absentNumber_rollCall)).setText("Absent : "+studentArrayList2.size());
                });
            }
        });


        studentListView.setOnItemClickListener((parent, view, position, id) -> {
            if(isRefreshing){
                Toast.makeText(getApplicationContext(),"Stop roll call first",Toast.LENGTH_SHORT);
                return;
            }
            String s = studentListView.getItemAtPosition(position).toString();
            String str = "";
            int index = -1;
            for(int i = 0; i<studentName.size(); i++){
                str = "ID : " + studentId.get(i)+
                        "\nName : " + studentName.get(i);
                if(str.equals(s)){
                    index = i;
                    break;
                }
            }
            if(index>-1 && !str.equals("")){
                present[index]=false;
                studentArrayList.remove(str);
                studentArrayList2.add(str);
                //adapter2.notifyDataSetChanged();
                studentListView.post(() -> {
                    studentListView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                });
                studentListView2.post(() -> {
                    studentListView2.setAdapter(adapter2);
                    adapter2.notifyDataSetChanged();
                });
                ((TextView)findViewById(R.id.textView_presentNumber_rollCall)).post(() -> {
                    ((TextView)findViewById(R.id.textView_presentNumber_rollCall)).setText("Present : "+studentArrayList.size());
                });
                ((TextView)findViewById(R.id.textView_absentNumber_rollCall)).post(() -> {
                    ((TextView)findViewById(R.id.textView_absentNumber_rollCall)).setText("Absent : "+studentArrayList2.size());
                });
            }
        });



    }

    void setAllData(){
        SQLiteDatabase sqLiteDatabase = this.openOrCreateDatabase("TeacherPanel",MODE_PRIVATE,null);
        String str = "Select * from " + groupName + " order by id";
        Cursor c = sqLiteDatabase.rawQuery(str,null);
        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                studentName.add(c.getString(c.getColumnIndex("name")));
                studentId.add(c.getString(c.getColumnIndex("id")));
                studentDeviceAddress.add(c.getString(c.getColumnIndex("deviceAddress")));
                //sqLiteDatabase.execSQL(s1);
                String str1 = "ID : " + c.getString(c.getColumnIndex("id"))+
                        "\nName : " + c.getString(c.getColumnIndex("name"));
                studentArrayList2.add(str1);
                System.out.println(studentArrayList2);

                c.moveToNext();
            }
            c.close();
            //adapter2.notifyDataSetChanged();
            /*studentListView2.post(() -> {
                //studentListView2.setAdapter(adapter2);
                adapter2.notifyDataSetChanged();
            });*/
        }
        ((TextView)findViewById(R.id.textView_absentNumber_rollCall)).setText("Absent : "+studentArrayList2.size());
        studentListView2.setAdapter(adapter2);
        studentListView2.post(()->adapter2.notifyDataSetChanged());

    }









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

            //System.out.println("Into the selectThread");

            List<WifiP2pDevice> peerList;
            int i = 0;
            while (!selectDeviceThread.isInterrupted()) {
                //System.out.println("Into the selectThread 2");
                peerList = new ArrayList<>(peers);
                //System.out.println("selectDevice : " + selectDevice);
                //System.out.println("isDeviceListChanged : " + isDeviceListChanged);
                if (selectDevice && isDeviceListChanged) {
                    //System.out.println("Into the selectThread 3 (if) ");
                    if (i < peerList.size()) {
                        device = peerList.get(i);
                        i++;
                        selectDevice = false;

                        //connectToDevice = true;




                        if (studentDeviceAddress.contains(device.deviceAddress))if(!present[studentDeviceAddress.indexOf(device.deviceAddress)]) {

                            WifiP2pConfig wifiP2pConfig = new WifiP2pConfig();
                            wifiP2pConfig.deviceAddress = device.deviceAddress;
                            add = false;
                            System.out.println("trying to connect to " + device.deviceName);

                            wait = true;
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

                            for(int i1 = 0;  i1 <15; i1++){
                                try {
                                    sleep(200);
                                    if(!wait)break;
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            /*try {
                                sleep(6000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }*/
                            if (add) {
                                //waitThread.interrupt();
                                //remove = false;
                                System.out.println(device.deviceName + " inside adding");
                                present[studentDeviceAddress.indexOf(device.deviceAddress)]=true;

                                //connectedDevices.add(device);
                                //connectedDevicesName.add(device.deviceName);

                                String str = "ID : " + studentId.get(studentDeviceAddress.indexOf(device.deviceAddress))+
                                        "\nName : " + studentName.get(studentDeviceAddress.indexOf(device.deviceAddress));
                                studentArrayList.add(str);
                                ((TextView)findViewById(R.id.textView_presentNumber_rollCall)).post(() -> {
                                    ((TextView)findViewById(R.id.textView_presentNumber_rollCall)).setText("Present : "+studentArrayList.size());
                                });
                                //listView.post(() -> listView.setAdapter(arrayAdapter));
                                studentListView.post(() -> {
                                    studentListView.setAdapter(adapter);
                                    adapter.notifyDataSetChanged();
                                });

                                studentArrayList2.remove(str);
                                ((TextView)findViewById(R.id.textView_absentNumber_rollCall)).post(() -> {
                                    ((TextView)findViewById(R.id.textView_absentNumber_rollCall)).setText("Absent : "+studentArrayList2.size());
                                });
                                studentListView2.post(() -> {
                                    studentListView2.setAdapter(adapter2);
                                    adapter2.notifyDataSetChanged();
                                });

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
                //System.out.println("Select  device loop complete ");
            }
        });

        if(!selectDeviceThread.isAlive())selectDeviceThread.start();
        //if(!connectToDeviceThread.isAlive())connectToDeviceThread.start();

    }

    void stopConnecting(){
            System.out.println("Stop");
            if(selectDeviceThread!=null)selectDeviceThread.interrupt();
            //connectToDeviceThread.interrupt();

    }

    public void doneButtonClicked(View view){
        if(isRefreshing){
            Toast.makeText(getApplicationContext(),"Stop Roll Call First",Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase sqLiteDatabase = this.openOrCreateDatabase("TeacherPanel",MODE_PRIVATE,null);
        ContentValues values = new ContentValues();
        Date today = new Date();
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a");
        String dateToStr = format.format(today);
        values.put("dateTime",dateToStr);
        for(int i = 0; i<totalStudent; i++){
            if(present[i]){
                values.put("id"+studentId.get(i),1);
            }
            else {
                values.put("id"+studentId.get(i),0);
            }
        }
        sqLiteDatabase.insert(sheetName,null,values);
        sqLiteDatabase.close();
        finish();
    }

    WifiP2pManager.ConnectionInfoListener connectionInfoListener =  wifiP2pInfo -> {

        final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;
        if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
            remove = false;
            //serverSendReceive = new ServerSendReceive();
            //serverSendReceive.start();
            wait = false;
            add = true;
            System.out.println("Inside server");
        } else if (wifiP2pInfo.groupFormed) {
            remove = false;
            //clientSendReceive = new ClientSendReceive(groupOwnerAddress);
            //clientSendReceive.start();
            wait = false;
            add = true;
            System.out.println("Inside client");
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
        if(selectDeviceThread!=null && selectDeviceThread.isAlive())selectDeviceThread.interrupt();
        /*if(serverSendReceive!=null)if(serverSendReceive.isAlive())try {
            if(serverSendReceive.bufferedWriter!=null)serverSendReceive.bufferedWriter.close();
            if(serverSendReceive.bufferedReader!=null)serverSendReceive.bufferedReader.close();
            if(serverSendReceive.serverSocket!=null)serverSendReceive.serverSocket.close();
            if(serverSendReceive.socket!=null)serverSendReceive.socket.close();
            serverSendReceive.socket = null;
        } catch (IOException e) {
            e.printStackTrace();
        }

        else if(clientSendReceive!=null)if(clientSendReceive.isAlive()){
            try {
                clientSendReceive.socket.close();
                clientSendReceive.bufferedWriter.close();
                clientSendReceive.bufferedReader.close();
                if(clientSendReceive.socket!=null)clientSendReceive.socket.close();
                clientSendReceive.socket=null;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }*/
        if(wifiManager != null && wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(false);
        }

        //serverSocket = null;
    }

















    /*class ServerSendReceive extends Thread{

        Socket socket;
        ServerSocket serverSocket;
        BufferedReader bufferedReader;
        BufferedWriter bufferedWriter;

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(8888);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }

            while(socket!=null && !serverSendReceive.isInterrupted()){
                try {
                    String temp = bufferedReader.readLine();

                    if(temp!=null )if( !temp.equals("null")){
                        JSONObject jsonObject = new JSONObject(temp);
                        id = jsonObject.getString("id");
                        name = jsonObject.getString("name");
                        done = jsonObject.getString("done");

                        JSONObject jsonObject1 = new JSONObject();
                        jsonObject1.put("done","done");
                        write(jsonObject1.toString());

                        remove = true;
                        bufferedReader.close();
                        bufferedWriter.close();
                        socket.close();
                        socket = null;
                        serverSocket.close();
                        serverSocket = null;
                        return;
                    }

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }

            }

            try {
                if(bufferedReader!=null)bufferedReader.close();
                if(bufferedWriter!=null)bufferedWriter.close();
                if(socket!=null)socket.close();
                socket = null;
                if(serverSocket!=null)serverSocket.close();
                serverSocket = null;
            } catch (IOException  | NullPointerException e) {
                e.printStackTrace();
            }
        }

        public void write(String str){
            try {
                //bufferedWriter.
                bufferedWriter.write(str);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class ClientSendReceive extends  Thread{
        Socket socket;
        String hostAddress;
        BufferedReader bufferedReader;
        BufferedWriter bufferedWriter;

        ClientSendReceive(InetAddress inetAddress){
            hostAddress = inetAddress.getHostAddress();
            socket = new Socket();
        }
        @Override
        public void run() {
            try {
                socket.connect(new InetSocketAddress(hostAddress,8888),500);
                bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream())) ;
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }

            while(socket!=null && !clientSendReceive.isInterrupted()){
                try {
                    String temp = bufferedReader.readLine();
                    System.out.println(temp);
                    if(temp!=null )if( !temp.equals("null")){
                        JSONObject jsonObject = new JSONObject(temp);
                        id = jsonObject.getString("id");
                        name = jsonObject.getString("name");
                        done = jsonObject.getString("done");

                        JSONObject jsonObject1 = new JSONObject();
                        jsonObject1.put("done","done");
                        write(jsonObject1.toString());

                        remove = true;
                        bufferedReader.close();
                        bufferedWriter.close();
                        socket.close();
                        socket = null;
                    }

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }

            }
            try {
                if(bufferedReader!=null)bufferedReader.close();
                if(bufferedWriter!=null)bufferedWriter.close();
                if(socket!=null)socket.close();
                socket = null;
            } catch (IOException  | NullPointerException e) {
                e.printStackTrace();
            }
        }

        public void write(String str){
            try {
                bufferedWriter.write(str);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
*/














    class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

        private WifiP2pManager wifiP2pManager;
        private WifiP2pManager.Channel channel;
        private RollCall rollCall;

        public WiFiDirectBroadcastReceiver(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel, RollCall rollCall){
            this.wifiP2pManager = wifiP2pManager;
            this.channel = channel;
            this.rollCall = rollCall;
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
                    wifiP2pManager.requestPeers(channel,rollCall.peerListListener);
                }
            }
            else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
                if(wifiP2pManager == null){
                    return;
                }
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if(networkInfo.isConnected()){

                        wifiP2pManager.requestConnectionInfo(channel,rollCall.connectionInfoListener);

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
