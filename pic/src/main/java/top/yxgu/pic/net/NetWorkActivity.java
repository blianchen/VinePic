package top.yxgu.pic.net;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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

public class NetWorkActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = NetWorkActivity.class.getName();

    private static Handler handler=new Handler();
    
    private GridView gridView;
    private ArrayList<HashMap<String, Object>> dataList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);

        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;// 屏幕宽度（像素）
//        int height= dm.heightPixels; // 屏幕高度（像素）
        float density = dm.density;//屏幕密度（0.75 / 1.0 / 1.5）
//        int densityDpi = dm.densityDpi;//屏幕密度dpi（120 / 160 / 240）  == 440
//        //屏幕宽度算法:屏幕宽度（像素）/屏幕密度
        int screenWidth = (int) (width/density);//屏幕宽度(dp)  == 698
//        int screenHeight = (int)(height/density);//屏幕高度(dp) == 392
//        Log.e("*****", screenWidth + "======" + screenHeight );
        int col = screenWidth / 120;
        int wdp = (int)(screenWidth / col * density);

        gridView = findViewById(R.id.GridViewNetWork);
        gridView.setColumnWidth(wdp);
        dataList = new ArrayList<>();

        getNetworkInfo();
        readArp();
    }

    private void readArp() {
        try(BufferedReader br = new BufferedReader(new FileReader("/proc/net/arp"))) {
            String line;
            String ip;
//            String flag = "";
            String mac;

            while ((line = br.readLine()) != null) {
                try {
                    line = line.trim();
                    if (line.length() < 63) continue;
                    if (line.toUpperCase(Locale.US).contains("IP")) continue;
                    ip = line.substring(0, 17).trim();
//                    flag = line.substring(29, 32).trim();
                    mac = line.substring(41, 63).trim();
                    if (mac.contains("00:00:00:00:00:00")) continue;
//                    Log.e("scanner", "readArp: mac= "+mac+" ; ip= "+ip+" ;flag= "+flag);
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("ItemImage", R.drawable.icon_pc);
                    map.put("ItemText", ip);
                    dataList.add(map);
                } catch (Exception e) {
                    Log.d(TAG, "readArp: ");
                    continue;
                }
            }

            SimpleAdapter sad = new SimpleAdapter(this, dataList, R.layout.activity_listitem,
                    new String[]{"ItemImage","ItemText"}, new int[]{R.id.ItemImage, R.id.ItemText});
            gridView.setAdapter(sad);
            gridView.setOnItemClickListener(this);
        } catch(Exception e) {
            e.printStackTrace();
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
                if (wifi.getRssi() != -200) {
                   String myIp = getWifiIPAddress(wifi.getIpAddress());
                    discover(myIp);// 发送arp请求
                }
//                myWifiName = wifi.getSSID(); //获取被连接网络的名称
//                myMac =  wifi.getBSSID(); //获取被连接网络的mac地址
//                String str = "WIFI: "+myWifiName+"\n"+"WiFiIP: "+myIp+"\n"+"MAC: "+myMac;
//                connectWifiInfo.setText(str);

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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        /*parent是指当前的listview；
         *view是当前listview中的item的view的布局,就是可用这个view获取里面控件id后操作控件
         * position是当前item在listview中适配器的位置
         * id是当前item在listview里第几行的位置
         */
        //获得选中项中的HashMap对象
        HashMap<String,String> map=(HashMap<String,String>)parent.getItemAtPosition(position);
        String text=map.get("ItemText");
        String root = "smb://" + text + "/";

        Intent intent = new Intent("top.yxgu.pic.FilelistActivity");
//        Bundle bundle = new Bundle();
//        bundle.putString("path", "smb://" + text);
//        intent.putExtras(bundle);
        intent.putExtra("top.yxgu.pic.root", root);
        startActivity(intent);
    }

}
