package com.mialab.socketdemo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;  
@SuppressLint("NewApi")
public class MainActivity extends Activity {  
    Socket socket = null;  
    private int flag=0;
    private final String SDCARD_PATH= android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
	private final String FILE_PATH =  SDCARD_PATH + "/fppr/"; 
	private File path = new File(FILE_PATH);// 创建目录
	private File f2;// 创建文件0
	 int[] rssi1;
	 int[] rssi;
	 int Sample_cnt=0; 
	// private WifiManager wifiManager;  
	 private WifiManager mWifiManager;  
	    //定义一个WifiInfo对象  
	    private WifiInfo mWifiInfo;  
	    //扫描出的网络连接列表  
	    private List<ScanResult> mWifiList;  
	   
	    private List<WifiConfiguration> mWifiConfigurations;  
	    WifiLock mWifiLock; 
	// private WiFiAdmin mWiFiAdmin;
     // 扫描结果列表    
     private List<ScanResult> list;    
     private ScanResult mScanResult;    
     private StringBuffer sb=new StringBuffer();  
    String buffer = "";  
    TextView text1;  
    Button send;  
    EditText ed1;  
    EditText editText1;
    String geted1;  
    String times;
    public Handler myHandler = new Handler() {  
        @Override  
        public void handleMessage(Message msg) {  
            if (msg.what == 0x11) {  
                Bundle bundle = msg.getData();  
                text1.append("server:"+bundle.getString("msg")+"\n");     
            }  
          if(msg.what == 0x12) {
        	  Bundle bundle = msg.getData();
        	  Toast.makeText(MainActivity.this, bundle.getString("msg1"),Toast.LENGTH_LONG).show();
          }
          if(msg.what == 0x13) {
        	  Bundle bundle = msg.getData();
        	  Toast.makeText(MainActivity.this, bundle.getString("msg2"),Toast.LENGTH_LONG).show();
          }
        }  
  
    };  
  
    @Override  
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.activity_main);  
        text1 = (TextView) findViewById(R.id.txt1);  
        send = (Button) findViewById(R.id.send);  
        ed1 = (EditText) findViewById(R.id.ed1); 
        editText1=(EditText) findViewById(R.id.editText1); 
        
        
        mWifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE); 
       

        
        send.setOnClickListener(new myListener());
        
        
    }  
  private class myListener implements OnClickListener{

	  @Override  
      public void onClick(View v) {  
		 
          geted1 = ed1.getText().toString();  
          text1.append("client:"+geted1+"\n"); 
          times = editText1.getText().toString();
          Sample_cnt = Integer.parseInt(times);
          //启动线程 向服务器发送和接收信息  
          new MyThread(geted1).start();  
      }  
	}
	  

    class MyThread extends Thread {  
  
        public String txt1;  
  
        public MyThread(String str) {  
            txt1 = str;  
        }  
  
        @Override  
        public void run() {  
        	
            //定义消息  
        	
            Message msg = new Message();  
            Message msg1 = new Message();
            Message msg2 = new Message();
            String s="";
            String l="";
           // String ip;
            //String info="test1";
            FileOutputStream fos;
            msg.what = 0x11;  
            msg1.what= 0x12;
            msg2.what= 0x13;
            Bundle bundle = new Bundle();  
            bundle.clear();  
            Bundle bundle1 = new Bundle();
            bundle1.clear();
            Bundle bundle2 = new Bundle();
            bundle2.clear();
           
            try {  
                //连接服务器 并设置连接超时为5秒  
                socket = new Socket();  
                socket.connect(new InetSocketAddress(geted1, 30000), 5000);  
                //获取输入输出流  
                OutputStream ou = socket.getOutputStream();  
                BufferedReader bff = new BufferedReader(new InputStreamReader(  
                        socket.getInputStream()));  
                //读取发来服务器信息  
                String line = null;  
                buffer="";  
                while ((line = bff.readLine()) != null) {  
                    buffer = line + buffer;  
                }  
                  
                //向服务器发送信息  
                ou.write(times.getBytes());  
                  
                ou.flush();  
               
                bundle.putString("msg", buffer.toString());  
                msg.setData(bundle);  
                //发送消息 修改UI线程中的组件  
                myHandler.sendMessage(msg); 
                bff.close();  
                ou.close();  
                socket.close();  
                buffer= "正在接收wifi信号";
                bundle1.putString("msg1", buffer.toString());  
                msg1.setData(bundle1);  
                //发送消息 修改UI线程中的组件  
                myHandler.sendMessage(msg1);
                
                int data_cnt=1;
                rssi1=new int[Sample_cnt];
                rssi=new int[Sample_cnt];
              
               for(data_cnt=1;data_cnt<=Sample_cnt;data_cnt++){
                     // 每次点击扫描之前清空上一次的扫描结果   
                     if(sb!=null){ sb=new StringBuffer(); }  
                     Thread.sleep(50); 
                    //开始扫描网络
                     mWifiInfo=mWifiManager.getConnectionInfo(); 
                     if (!mWifiManager.isWifiEnabled()) {mWifiManager.setWifiEnabled(true); } 
                    
                    // mWifiManager.startScan();  
                     
                     //得到扫描结果  
                    // Log.i("scan", "start");
                  //   mWifiList=mWifiManager.getScanResults();  
                     //得到配置好的网络连接  
                  //   mWifiConfigurations=mWifiManager.getConfiguredNetworks(); 
                    // Log.i("mac", mScanResult.BSSID);
                   //  list = mWifiList;
                    // if(list!=null){ 
                         	//for(int i=0;i<list.size();i++){  
                	              //得到扫描结果  
                	          //   mScanResult=list.get(i);
                	             // send.setText("cccg5");
                	            //  text1.append(mScanResult.BSSID);
                	           String wifimac = mWifiManager.getConnectionInfo().getBSSID();
                	          // Log.i("scan", "end");
                	          // Log.i("mac", wifimac);
                	           //if(mScanResult.BSSID.equals(wifimac)){
                	       
                	        	  // Log.i("准备", "ccg1");
                	        	   //if(data_cnt<=2) text1.append(mScanResult.BSSID);
               				  //  rssi1[data_cnt-1]=mScanResult.level;
               					rssi[data_cnt-1]=mWifiInfo.getRssi();
               				    //String level=rssi1[data_cnt-1]+"";
               				    String rssi_=rssi[data_cnt-1]+"";
               				//	Log.i("level", level);
               					//Log.i("level", rssi_);
               				    //Log.isLoggable("值", rssi1[data_22111cnt-1]);
               				    s = s + rssi[data_cnt-1];//拼接成字符串，最终放在变量s中
               				   // l = l + rssi1[data_cnt-1];
               					  // text1.append(data);
               				   // send.setText("wifi");
               					//}  
                	           
                         	//}
               
                         	
                         //}
                     
                    }
               
              
                //关闭各种输入输出流  
               
      
                
            } catch (SocketTimeoutException aa) {  
                //连接超时 在UI界面显示消息  
                bundle.putString("msg", "服务器连接失败！请检查网络是否打开");  
                msg.setData(bundle);  
                //发送消息 修改UI线程中的组件  
                myHandler.sendMessage(msg);  
            } catch (IOException e) {  
                e.printStackTrace();  
            } catch (InterruptedException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}  
            
          //  Toast.makeText(MainActivity.this, "开始存储数据", Toast.LENGTH_LONG).show();
            if(s!="")
            {
              flag++;
       		  String name = flag+"";
       		  f2 = new File(FILE_PATH+name+"second.txt");// 创建文件0
       		  PreCollect();
            try {
       			fos = new FileOutputStream(f2);
       			fos.write("rssi:".getBytes());
       			fos.write(s.getBytes());
       			//fos.write("\r\n".getBytes());
       			//fos.write("level:".getBytes());
       			//fos.write(l.getBytes());
       			//fos.write("kjkhkhhh".getBytes());
       			//fos.write("\r\n".getBytes());
       			fos.close();
       			
       			} catch (FileNotFoundException e1) {
       			e1.printStackTrace();
       		// text1.setText( "异常"); 
       		} catch (IOException e) {
       			
       			e.printStackTrace();
       		// text1.setText( "异常"); 
       		}
            bundle2.putString("msg2", "数据接收存储完成".toString());  
            msg2.setData(bundle2);  
            //发送消息 修改UI线程中的组件  
            myHandler.sendMessage(msg2);
            }
            
        }  
    }  
  
    @Override  
    public boolean onCreateOptionsMenu(Menu menu) {  
        // Inflate the menu; this adds items to the action bar if it is present.  
        getMenuInflater().inflate(R.menu.main, menu);  
        return true;  
    }  

private void PreCollect(){
    	
        String SdState=android.os.Environment.getExternalStorageState();
        //send.setText("fffff6");
        if(!SdState.equals(android.os.Environment.MEDIA_MOUNTED)){       	
        	//text.setText("请插入MicroSD卡!");
        	// send.setText("fffff3");
        	this.finish();}
        else{ 
        	if (!path.exists()) {// 目录存在返回false
  	          path.mkdirs();// 创建一个目录
  	       // send.setText("fffff2");
  	         }
        	
  	        if (!f2.exists()) {// 文件存在返回false
  	          try {
  	           f2.createNewFile();//创建文件 
  	           //send.setText("fffff");
  	          } catch (IOException e) {
  	           // TODO Auto-generated catch block
  	           e.printStackTrace();
  	         //send.setText("fffff1");
  	           }
  	         } 	
  	       
            }   
          }
        

}
