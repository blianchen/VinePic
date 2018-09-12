package top.yxgu.pic.net;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;

import jcifs.CIFSContext;
import jcifs.CIFSException;
import jcifs.CloseableIterator;
import jcifs.SmbResource;
import jcifs.config.PropertyConfiguration;
import jcifs.context.BaseContext;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbUnsupportedOperationException;
import top.yxgu.pic.R;

public class NetWorkActivity extends Activity implements AdapterView.OnItemClickListener {

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
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                getSharePcList();
//            }
//        }).start();

    }

    private void getSharePcList() {
        CIFSContext ctx = null;
        try {
            ctx = withAnonymousCredentials();

            try ( SmbFile smbFile = new SmbFile("smb://", ctx) ) {
                try ( CloseableIterator<SmbResource> it = smbFile.children() ) {
                    while ( it.hasNext() ) {
                        try ( SmbResource serv = it.next() ) {
    //                        System.err.println(serv.getName()+":"+serv.getType()+":"+serv.getLocator().getURL());
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("ItemImage", R.drawable.icon_pc);
                            map.put("ItemText", serv.getName());
                            list.add(map);
                        }
                    }

                    SimpleAdapter sad = new SimpleAdapter(this, list, R.layout.activity_listitem,
                            new String[]{"ItemImage","ItemText"}, new int[]{R.id.ItemImage, R.id.ItemText});
                    gridView.setAdapter(sad);
                    gridView.setOnItemClickListener(this);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } catch (CIFSException e) {
            e.printStackTrace();
        }
    }

    private void testBrowseDomain (String ip) throws MalformedURLException, CIFSException {

        CIFSContext ctx = withAnonymousCredentials();

        try ( SmbFile smbFile = new SmbFile("smb://" + ip, ctx) ) {
            // if domain is resolved through DNS this will be treated as a server and will enumerate shares instead
//            Assume.assumeTrue("Not workgroup", SmbConstants.TYPE_WORKGROUP == smbFile.getType());
            try ( CloseableIterator<SmbResource> it = smbFile.children() ) {
                if ( it.hasNext() ) {
                    try ( SmbResource serv = it.next() ) {
                        System.err.println(serv.getName());
//                        assertEquals(SmbConstants.TYPE_SERVER, serv.getType());
//                        assertTrue(serv.isDirectory());
//                        Toast.makeText(NetWorkActivity.this, serv.getName(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch ( SmbUnsupportedOperationException e ) {
            e.printStackTrace();
        }
    }

    private CIFSContext withAnonymousCredentials () throws CIFSException {
        Properties cfg = new Properties();
        cfg.put("jcifs.smb.client.maxVersion", "SMB1");
//        cfg.put("jcifs.smb.client.useUnicode", "false");
//        cfg.put("jcifs.smb.client.forceUnicode", "false");
//        cfg.put("jcifs.smb.client.useNtStatus", "false");
//        cfg.put("jcifs.smb.client.useNTSmbs", "false");
        BaseContext baseContext = new BaseContext(new PropertyConfiguration(cfg));
        return baseContext.withAnonymousCredentials();
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
//                    flag = line.substring(29, 32).trim();
                    mac = line.substring(41, 63).trim();
                    if (mac.contains("00:00:00:00:00:00")) continue;
//                    Log.e("scanner", "readArp: mac= "+mac+" ; ip= "+ip+" ;flag= "+flag);
//                    String arp = "ip: "+ip+" | "+"mac: "+mac+" | "+"flag: "+flag;
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
            gridView.setOnItemClickListener(this);
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
        final String text=map.get("ItemText");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    testBrowseDomain(text);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (CIFSException e) {
                    e.printStackTrace();
                }
            }
        }).start();
//        Toast.makeText(NetWorkActivity.this, Text, Toast.LENGTH_SHORT).show();
    }
}
