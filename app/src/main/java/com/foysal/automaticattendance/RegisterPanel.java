package com.foysal.automaticattendance;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

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
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class RegisterPanel extends AppCompatActivity {

    EditText nameField,idField,courseField;
    TextView warning,pleaseWait;
    ProgressBar progressBar;


    String name,id,courseName;

    /*ServerSocket serverSocket;
    Socket socket;*/


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



    ServerClass serverClass;
    ClientClass clientClass;
    SendReceive sendReceive;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        nameField = findViewById(R.id.editText_student_name);
        idField = findViewById(R.id.editText_student_id);
        courseField = findViewById(R.id.editText_course_name_register);
        warning = findViewById(R.id.textView_warning_register);


        /*try{
            if(serverSocket!=null)serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        serverSocket = null;

        try {
            serverSocket = new ServerSocket(8888);

        } catch (IOException e) {
            *//*try {
                serverSocket.setReuseAddress(true);
            } catch (SocketException e1) {
                e1.printStackTrace();
            }*//*
            System.out.println("Server socket not initialized");
            e.printStackTrace();
        }
*/



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


        isRefreshing = false;

        peerListListener = peerlist -> {

            if(!peerlist.getDeviceList().equals(peers)){
                peers.clear();
                peers.addAll(peerlist.getDeviceList());

                //System.out.println("Device list changed");
                System.out.println(peers);
                isDeviceListChanged = true;
                //System.out.println("Device list changed 2");
            }

            if( (peers.size()==0) && connectedDevices.isEmpty()  )Toast.makeText(getApplicationContext(),"No device found",Toast.LENGTH_SHORT).show();
        };








    }

    public void registerButtonClicked(View view){
        if(nameField.getText().toString().equals("")){
            warning.setText("Enter your name");
        }
        else if(idField.getText().toString().equals("")){
            warning.setText("Enter your id");
        }
        else if(courseField.getText().toString().equals("")){
            warning.setText("Enter course name");
        }
        else {
            setContentView(R.layout.registering);
            progressBar = findViewById(R.id.progressBar);
            pleaseWait = findViewById(R.id.textView9_pleaseWait);
            name = nameField.getText().toString();
            id = idField.getText().toString();
            courseName = courseField.getText().toString();
            register();
        }
    }


    void register(){

        wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {

            }
        });

    }


    WifiP2pManager.ConnectionInfoListener connectionInfoListener =  wifiP2pInfo -> {

        final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;
        if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
            //connectionStatus.setText("Host");
            serverClass = new ServerClass();
            serverClass.start();
            System.out.println("Inside server");
            /*String str =  "{\"id\":\"" + id + "\",\"name\":\"" + name + "\",\"done\":\"true\"}";
            sendReceive.write(str.getBytes());*/
        } else if (wifiP2pInfo.groupFormed) {
            //connectionStatus.setText("Client");
            clientClass = new ClientClass(groupOwnerAddress);
            clientClass.start();
            System.out.println("Inside client");
            /*String str =  "{\"id\":\"" + id + "\",\"name\":\"" + name + "\",\"done\":\"true\"}";
            sendReceive.write(str.getBytes());*/
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
        destroyAllConnections();
        if(wifiManager!=null && wifiManager.isWifiEnabled())wifiManager.setWifiEnabled(false);
    }


    /*@Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            socket.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        sendReceive.interrupt();
        serverSocket = null;
        socket = null;
        wifiP2pManager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {

            }
        });
        wifiP2pManager.stopPeerDiscovery(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {

            }
        });
        if(wifiManager!=null && wifiManager.isWifiEnabled())wifiManager.setWifiEnabled(false);

    }*/

    void done(){

        pleaseWait.post(()->{
            pleaseWait.setText("Registration Complete");
            pleaseWait.setTextColor(Color.GREEN);
        });

        progressBar.post(()->progressBar.setVisibility(View.GONE));


        SQLiteDatabase sqLiteDatabase = openOrCreateDatabase("StudentPanel",MODE_PRIVATE,null);
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS CourseList (coursename varchar, name varchar, id varchar) ");
        ContentValues values = new ContentValues();
        values.put("coursename",courseName);
        values.put("name",name);
        values.put("id",id);
        sqLiteDatabase.insert("Courselist",null,values);

        if(wifiManager.isWifiEnabled())wifiManager.setWifiEnabled(false);
    }


    void destroyAllConnections(){
        if(sendReceive!=null)if(sendReceive.isAlive())try {
            sendReceive.bufferedWriter.close();
            sendReceive.bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(serverClass!=null)if(serverClass.isAlive())try {
            serverClass.socket.close();
            serverClass.serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        else if(clientClass!=null)if(clientClass.isAlive()){
            try {
                clientClass.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("id",id);
                    jsonObject.put("name",name);
                    jsonObject.put("done","true");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                System.out.println("Sending Data : " + jsonObject.toString());
                //String str =  "{\"id\":\"" + id + "\",\"name\":\"" + name + "\",\"done\":\"true\"}";
                sendReceive.write(jsonObject.toString()+"\n");
                System.out.println("Data sent");
                //Thread.sleep(500);

                //done();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }





    class SendReceive extends Thread{

        Socket socket;
        BufferedReader bufferedReader;
        BufferedWriter bufferedWriter;

        SendReceive(Socket socket){
            this.socket = socket;
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream())) ;

                bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void run() {
           byte[] buffer = new byte[1024];
            int bytes;
            while(socket!=null && !isInterrupted()){
                try {
                    String temp = bufferedReader.readLine();
                    System.out.println(temp);
                    if(temp!=null )if( !temp.equals("null")){
                        JSONObject jsonObject = new JSONObject(temp);
                        if(jsonObject.getString("done").equals("done"))done();
                            //write("done");

                    }

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(String str){
            try {
                bufferedWriter.write(str);
                bufferedWriter.newLine();
                bufferedWriter.flush();
                //outputStream.write(bytes);
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
                socket.getKeepAlive();
                sendReceive = new SendReceive(socket);
                sendReceive.start();
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("id",id);
                    jsonObject.put("name",name);
                    jsonObject.put("done","true");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                System.out.println("Sending data");
                //String str =  "{\"id\":\"" + id + "\",\"name\":\"" + name + "\",\"done\":\"true\"}";
                sendReceive.write(jsonObject.toString());
                System.out.println("Data sent");

                //while(true);
                done();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }










































    class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

        private WifiP2pManager wifiP2pManager;
        private WifiP2pManager.Channel channel;
        private RegisterPanel registerPanel;

        public WiFiDirectBroadcastReceiver(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel, RegisterPanel registerPanel){
            this.wifiP2pManager = wifiP2pManager;
            this.channel = channel;
            this.registerPanel = registerPanel;
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
                    wifiP2pManager.requestPeers(channel,registerPanel.peerListListener);
                }
            }
            else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
                if(wifiP2pManager == null){
                    return;
                }
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if(networkInfo.isConnected()){

                    synchronized (this){
                        wifiP2pManager.requestConnectionInfo(channel,registerPanel.connectionInfoListener);
                    }
                }
                else {
                    registerPanel.destroyAllConnections();
                }
            }
            else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){

            }
        }
    }


}
