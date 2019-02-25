package com.foysal.automaticattendance;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

public class WiFiDirectBroadcastReceiverExample extends BroadcastReceiver {

    WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private AddStudentGroup addStudentGroup;

    public WiFiDirectBroadcastReceiverExample(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel, AddStudentGroup addStudentGroup){
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

                wifiP2pManager.requestConnectionInfo(channel,addStudentGroup.connectionInfoListener);
            }
            else {
                //mainActivity.connectionStatus.setText("Device disconnected");
            }
        }
        else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){

        }
    }
}
