package zh;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import zh3.maven.org.myapplication.FtSetActivity;

public class Config {
	private  boolean delAfterSuccess=true;
	private int port=20009;
	private  String ip="127.0.0.1";
	private  String srcDir="";
	private  String destDir="";



	public Config(SharedPreferences sp) throws IOException {
		delAfterSuccess=sp.getBoolean("def_success_switch",true);

		port=Integer.parseInt(sp.getString("conn_port_text","20009"));
		ip=sp.getString("conn_ip_text","192.168.0.1");
		srcDir=sp.getString("src_dir_text","/");

		//destDir=sp.getString("src_dir_text","/");

	}
	
      public boolean delAfterSuccess(){
    	  return delAfterSuccess;
      }
      
      public int getPort(){
    	  return port;
      }
      
      public String getSrcDir(){
    	  return srcDir;
      }
      
      public String getDestDir(){
    	  return destDir ;
      }
      public String getIp(){
    	  return ip;
      }
      
      
}
