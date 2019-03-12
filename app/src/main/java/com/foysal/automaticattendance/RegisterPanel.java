package com.foysal.automaticattendance;

import android.content.BroadcastReceiver;
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

public class RegisterPanel extends AppCompatActivity {

    EditText nameField,idField,courseField;
    TextView warning;

    String name,id,courseName;



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

                System.out.println("Device list changed");
                System.out.println(peers);
                isDeviceListChanged = true;
                System.out.println("Device list changed 2");
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
            String str =  "{\"id\":\"" + id + "\",\"name\":\"" + name + "\",\"done\":\"true\"}";
            sendReceive.write(str.getBytes());
        } else if (wifiP2pInfo.groupFormed) {
            //connectionStatus.setText("Client");
            clientClass = new ClientClass(groupOwnerAddress);
            clientClass.start();
            String str =  "{\"id\":\"" + id + "\",\"name\":\"" + name + "\",\"done\":\"true\"}";
            sendReceive.write(str.getBytes());
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
                    if(tempMsg.equals("1")){
                        ((TextView)findViewById(R.id.textView9_pleaseWait)).setText("Registration Complete");
                        ((TextView)findViewById(R.id.textView9_pleaseWait)).setTextColor(Color.GREEN);
                        ((ProgressBar)findViewById(R.id.progressBar)).setVisibility(View.GONE);
                        try {
                            Thread.sleep(2000);
                            finish();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        SQLiteDatabase sqLiteDatabase = openOrCreateDatabase("StudentPanel",MODE_PRIVATE,null);
                        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS CourseList (coursename varchar PRIMARY KEY, name varchar, id varchar) ");
                        String str = "Insert into courselist (coursename,name,id) values(?,?,?)";
                        sqLiteDatabase.rawQuery(str,new String[]{courseName,name,id});
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
                    //mainActivity.connectionStatus.setText("Device disconnected");
                }
            }
            else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){

            }
        }
    }


}
