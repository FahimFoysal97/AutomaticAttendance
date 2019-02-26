package com.foysal.automaticattendance;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class AddStudentGroup extends AppCompatActivity {


    WifiManager wifiManager;
    WifiP2pManager wifiP2pManager;
    WifiP2pManager.Channel channel;
    BroadcastReceiver broadcastReceiver;
    IntentFilter intentFilter;
    WifiP2pDevice[] wifiP2pDevices;
    Vector<WifiP2pDevice> connectedDevices = new Vector<>();
    Vector<String> connectedDevicesName = new Vector<>();
    String[] deviceNameArray;
    List<WifiP2pDevice> peers = new ArrayList<>();
    WifiP2pManager.PeerListListener peerListListener;
    //ConnectClass connectClass;
    boolean add = false;
    boolean isDeviceListChanged = false;
    boolean isRefreshing = false;

    ListView listView;
    //int i;
    boolean first = true;
    Thread connectthread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student_group);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null && !wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        else if(wifiManager != null && wifiManager.isWifiEnabled()){
            wifiManager.setWifiEnabled(false);
            wifiManager.setWifiEnabled(true);
        }
        //i=0;
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
        //connectClass = new ConnectClass();
        //connectClass.start();
        //connectClass = new ConnectClass();
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

            if(peers.size()==0)Toast.makeText(getApplicationContext(),"No device found",Toast.LENGTH_SHORT).show();
        };
        //refresh();
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
            refresh();
            ((Button) findViewById(R.id.button_Refresh)).setText("Refresh");
        });

    }

    void connectToDevice(List<WifiP2pDevice> peerList,int key){
        connectthread = new Thread(() -> {
            System.out.println("into thread");

            WifiP2pDevice device = peerList.get(key);

                System.out.println(device.deviceAddress);

                if (!connectedDevices.contains(device)) {
                    WifiP2pConfig wifiP2pConfig = new WifiP2pConfig();
                    wifiP2pConfig.deviceAddress = device.deviceAddress;
                    add = false;
                    System.out.println("trying to connect to " + device.deviceName);

                    wifiP2pManager.connect(channel, wifiP2pConfig, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            System.out.println("Connection making successful with "+device.deviceName);
                        }

                        @Override
                        public void onFailure(int reason) {
                            //Toast.makeText(getApplicationContext(), "Not connected = " + reason, Toast.LENGTH_SHORT).show();
                        }
                    });

                    try {
                        Thread.sleep(6000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(add) {

                        System.out.println(device.deviceName + " inside adding");
                        connectedDevices.add(device);
                        connectedDevicesName.add(device.deviceName);

                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, connectedDevicesName);

                        listView.post(new Runnable() {
                            @Override
                            public void run() {
                                listView.setAdapter(arrayAdapter);
                            }
                        });

                        wifiP2pManager.removeGroup(channel, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {
                                System.out.println("Disconnecting from device : "+device.deviceName);
                            }

                            @Override
                            public void onFailure(int reason) {

                            }
                        });

                        //System.out.println(arrayAdapter);

                        //Toast.makeText(getApplicationContext(), "Connected to " + device.deviceName, Toast.LENGTH_LONG).show();
                    }
                    else { System.out.println("Canceling connection");
                        wifiP2pManager.cancelConnect(channel, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {
                                if(key+1<peerList.size())connectToDevice(peerList,key+1);
                            }

                            @Override
                            public void onFailure(int reason) {

                            }
                        });
                    }

                }


        });
        connectthread.start();
    }

    void connectToDevices(){
        List<WifiP2pDevice> peerList = new ArrayList<>(peers);
        if(peerList.size()>0)connectToDevice(peerList,0);
        isRefreshing = false;
    }


    void refresh(){
        if(!isRefreshing){
            wifiP2pManager.stopPeerDiscovery(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getApplicationContext(), "Searching Devices...", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int reason) {

                }
            });
            wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getApplicationContext(), "Searching Devices...", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int reason) {

                }
            });
            System.out.println("Refreshing");
            isRefreshing = true;

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Connecting to devices");
            //while(isDeviceListChanged)
            connectToDevices();
            //isRefreshing = false;
        }
    }


    WifiP2pManager.ConnectionInfoListener connectionInfoListener =  wifiP2pInfo -> {
        //final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;



            if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
                Toast.makeText(getApplicationContext(), "inside info", Toast.LENGTH_SHORT).show();
                add = true;
                //notifyTheThread();
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


    /*class ConnectClass extends Thread{

        *//*ConnectClass(){
            this.start();
        }*//*
        @Override
        public void run() {
            //final List<WifiP2pDevice> devices = new ArrayList<>(peers);

            System.out.println("Inside run method");
            synchronized (this) {
                for (WifiP2pDevice device : peers) {

                    add = false;
                    if (!connectedDevices.contains(device)) {
                        WifiP2pConfig wifiP2pConfig = new WifiP2pConfig();
                        wifiP2pConfig.deviceAddress = device.deviceAddress;

                        System.out.println("trying to connect to " + device.deviceName);

                        wifiP2pManager.connect(channel, wifiP2pConfig, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {

                                System.out.println("Connection making successful");
                            }

                            @Override
                            public void onFailure(int reason) {
                                //Toast.makeText(getApplicationContext(), "Not connected = " + reason, Toast.LENGTH_SHORT).show();
                            }
                        });

                        try {
                            wait(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (add) {

                            System.out.println(device.deviceName + " inside adding");
                            connectedDevices.add(device);
                            connectedDevicesName.add(device.deviceName);

                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, connectedDevicesName);

                            //listView.setAdapter(arrayAdapter);
                            System.out.println(arrayAdapter);

                            //Toast.makeText(getApplicationContext(), "Connected to " + device.deviceName, Toast.LENGTH_LONG).show();
                        }
                        *//*else{
                            wifiP2pManager.cancelConnect(channel, new WifiP2pManager.ActionListener() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onFailure(int reason) {

                                }

                            });
                        }*//*

                        add = false;

                    }

                }
            }

        }

        synchronized void startIt(){
            start();
        }

        synchronized void resumeIt(){
            System.out.println("Resuming It");

            notify();
        }
    }*/
















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
