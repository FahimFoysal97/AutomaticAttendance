package com.foysal.automaticattendance;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static java.lang.Thread.sleep;

public class GivingAttendance extends AppCompatActivity {


    WifiManager wifiManager;
    WifiP2pManager wifiP2pManager;
    WifiP2pManager.Channel channel;
    BroadcastReceiver broadcastReceiver;
    IntentFilter intentFilter;
    Vector<WifiP2pDevice> connectedDevices = new Vector<>();
    List<WifiP2pDevice> peers = new ArrayList<>();
    WifiP2pManager.PeerListListener peerListListener;
    //Thread discover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_giving_attendance);


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




        peerListListener = peerlist -> {

            if(!peerlist.getDeviceList().equals(peers)){
                peers.clear();
                peers.addAll(peerlist.getDeviceList());

                System.out.println("Device list changed");
                System.out.println(peers);

                System.out.println("Device list changed 2");
            }

            if( (peers.size()==0) && connectedDevices.isEmpty()  )Toast.makeText(getApplicationContext(),"No device found",Toast.LENGTH_SHORT).show();
        };

        try {
            Thread.sleep(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                System.out.println("Discovering.....");
            }

            @Override
            public void onFailure(int reason) {
                System.out.println("Reason " + reason);
                (findViewById(R.id.progressBar2)).post(()->(findViewById(R.id.progressBar2)).setVisibility(View.GONE));
                ((TextView)findViewById(R.id.textView9_pleaseWait2)).post(()->{
                    ((TextView)findViewById(R.id.textView9_pleaseWait2)).setText("Please try again..");
                    ((TextView)findViewById(R.id.textView9_pleaseWait2)).setTextColor(Color.RED);
                    if (wifiManager != null && !wifiManager.isWifiEnabled()) {
                        wifiManager.setWifiEnabled(true);
                    }
                });
            }
        });

        //discover.start();



    }


    /*@Override
    protected void onDestroy() {
        super.onDestroy();
        if (wifiManager != null && wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
        }
    }*/

    void onConnected(){
        System.out.println("Attendance Giving Successful");
        (findViewById(R.id.progressBar2)).post(()->(findViewById(R.id.progressBar2)).setVisibility(View.GONE));
        ((TextView)findViewById(R.id.textView9_pleaseWait2)).post(()->{
            ((TextView)findViewById(R.id.textView9_pleaseWait2)).setText("Attendance Giving Successfull");
            ((TextView)findViewById(R.id.textView9_pleaseWait2)).setTextColor(Color.GREEN);
        });
    }

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = wifiP2pInfo -> {

        if (wifiP2pInfo.groupFormed) {
            onConnected();
        }
    };

    class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

        private WifiP2pManager wifiP2pManager;
        private WifiP2pManager.Channel channel;
        private GivingAttendance givingAttendance;

        public WiFiDirectBroadcastReceiver(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel, GivingAttendance givingAttendance){
            this.wifiP2pManager = wifiP2pManager;
            this.channel = channel;
            this.givingAttendance = givingAttendance;
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
                    wifiP2pManager.requestPeers(channel,givingAttendance.peerListListener);
                }
            }
            else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
                if(wifiP2pManager == null){
                    return;
                }
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if(networkInfo.isConnected()){

                        wifiP2pManager.requestConnectionInfo(channel,givingAttendance.connectionInfoListener);

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
