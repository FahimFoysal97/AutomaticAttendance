package com.foysal.automaticattendance;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class TakingAttendance extends AppCompatActivity {

    WifiManager wifiManager;
    WifiP2pManager wifiP2pManager;
    IntentFilter intentFilter;
    WifiP2pManager.Channel channel;
    WifiP2pManager.PeerListListener peerListListener;
    WifiP2pManager.ConnectionInfoListener connectionInfoListener;
    BroadcastReceiver broadcastReceiver;


    List<WifiP2pDevice> peers = new ArrayList<>();


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_roll_call);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        channel = wifiP2pManager.initialize(this,getMainLooper(),null);
        broadcastReceiver = new WiFiDirectBroadcastReceiver(wifiP2pManager,channel,this);

        peerListListener = peerList -> {
            /*if(!peerList.getDeviceList().equals(peers)){
                peers.clear();
                peers.addAll(peerList.getDeviceList());
                System.out.println(peers);
            }*/
            System.out.println(peerList);
            if(peerList.getDeviceList().size()==0)System.out.println("No devices");
        };

        connectionInfoListener = info -> {
            if(info.groupFormed){
                System.out.println("Connected");
            }
        };

        wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                System.out.println("Discovering...");
            }

            @Override
            public void onFailure(int reason) {

            }
        });

    }








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
                    wifiP2pManager.requestPeers(channel,peerListListener);
                }
            }
            else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
                if(wifiP2pManager == null){
                    return;
                }
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if(networkInfo.isConnected()){

                    wifiP2pManager.requestConnectionInfo(channel,connectionInfoListener);

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






