package top.yxgu.pic.net;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import top.yxgu.pic.R;

public class NetWorkActivity extends Activity {

    private GridView gridView;
    private ArrayList<HashMap<String, Object>> list;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);

        gridView = findViewById(R.id.GridViewNetWork);
        list = new ArrayList<>();

        getNetworkInfo();
        readArp();
    }

    private void readArp() {
        try {
            BufferedReader br = new BufferedReader(
                    new FileReader("/proc/net/arp"));
            String line = "";
            String ip = "";
            String flag = "";
            String mac = "";

            while ((line = br.readLine()) != null) {
                try {
                    line = line.trim();
                    if (line.length() < 63) continue;
                    if (line.toUpperCase(Locale.US).contains("IP")) continue;
                    ip = line.substring(0, 17).trim();
                    flag = line.substring(29, 32).trim();
                    mac = line.substring(41, 63).trim();
                    if (mac.contains("00:00:00:00:00:00")) continue;
                    Log.e("scanner", "readArp: mac= "+mac+" ; ip= "+ip+" ;flag= "+flag);
                    String arp = "ip: "+ip+" | "+"mac: "+mac+" | "+"flag: "+flag;
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("ItemImage", R.drawable.icon_pc);
                    map.put("ItemText", ip);
                    list.add(map);
                } catch (Exception e) {
                    continue;
                }
            }
            br.close();

            SimpleAdapter sad = new SimpleAdapter(this, list, R.layout.activity_listitem,
                    new String[]{"ItemImage","ItemText"}, new int[]{R.id.ItemImage, R.id.ItemText});
            gridView.setAdapter(sad);
        } catch(Exception e) {
        }

    }

    private void getNetworkInfo() {
        try {
            WifiManager wm = null;
            try {
                wm = (WifiManager)this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            } catch (Exception e) {
                wm = null;
            }
            if (wm != null && wm.isWifiEnabled()) {
                WifiInfo wifi = wm.getConnectionInfo();
//                if (wifi.getRssi() != -200) {
                   String myIp = getWifiIPAddress(wifi.getIpAddress());
//                }
//                myWifiName = wifi.getSSID(); //获取被连接网络的名称
//                myMac =  wifi.getBSSID(); //获取被连接网络的mac地址
//                String str = "WIFI: "+myWifiName+"\n"+"WiFiIP: "+myIp+"\n"+"MAC: "+myMac;
//                connectWifiInfo.setText(str);
                discover(myIp);// 发送arp请求
            }
        } catch (Exception e) {
            e.getMessage();
        }
    }

    private void discover(String ip) {
        String newip = "";
        if (!ip.equals("")) {
            String ipseg = ip.substring(0, ip.lastIndexOf(".")+1);
            for (int i=2; i<255; i++) {
                newip = ipseg+String.valueOf(i);
                if (newip.equals(ip)) continue;
                Thread ut = new UDPThread(newip);
                ut.start();
            }
        }
    }

    private String getWifiIPAddress(int ipaddr) {
        String ip = "";
        if (ipaddr == 0) return ip;
        byte[] addressBytes = {(byte)(0xff & ipaddr), (byte)(0xff & (ipaddr >> 8)),
                (byte)(0xff & (ipaddr >> 16)), (byte)(0xff & (ipaddr >> 24))};
        try {
            ip = InetAddress.getByAddress(addressBytes).toString();
            if (ip.length() > 1) {
                ip = ip.substring(1, ip.length());
            } else {
                ip = "";
            }
        } catch (UnknownHostException e) {
            ip = "";
        } catch (Exception e) {
            ip = "";
        }
        return ip;
    }
}
