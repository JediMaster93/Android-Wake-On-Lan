package com.teamgy.wakeonlan;

import android.app.IntentService;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class WOLService extends IntentService{

    private WifiManager wifiManager;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public WOLService(String name) {
        super(name);
    }

    public WOLService(){
        super("WOLService");

    }

    @Override
    public void onCreate() {
        super.onCreate();
        wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);

    }
    /*public WOLService(WifiManager wifiManager){
        this.wifiManager = wifiManager;

    }*/




    public  byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    InetAddress getBroadcastAddress() throws IOException {
        DhcpInfo dhcp = wifiManager.getDhcpInfo();
        // handle null somehow

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

            try{

                String [] macAdresses = intent.getStringArrayExtra("macAdresses");
                if(macAdresses != null) {
                    DatagramSocket socket = new DatagramSocket(4000);
                    socket.setBroadcast(true);
                    socket.setReuseAddress(true);
                    String wolHeader = "ffffffffffff";

                    for (String macAdress:macAdresses ) {

                        String macWolData = new String(new char[16]).replace("\0", macAdress); //repeat mac 16 times
                        byte[] data = hexStringToByteArray(wolHeader + macWolData); //6 byes
                        DatagramPacket packet = new DatagramPacket(data, data.length, getBroadcastAddress(), 40000);
                        socket.send(packet);
                        Log.d("wol service:", "sent packet ");


                    }
                    socket.close();
                }
            }catch (IOException e){


            }
        }

}