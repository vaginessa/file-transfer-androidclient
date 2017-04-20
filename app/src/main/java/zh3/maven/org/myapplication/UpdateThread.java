package zh3.maven.org.myapplication;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;

public class UpdateThread extends Thread{
	
	private static volatile long lastUpdateTime=0;
	private static volatile long oneHour=1000*60*60*1;

	public static synchronized void start(ReulstProcess p){
		if(new Date().getTime()-lastUpdateTime>oneHour){
			lastUpdateTime=new Date().getTime();
		}else{
			System.out.println("跳过检查更新");
			return ;
		}
		
		UpdateThread ut=new UpdateThread();
		if(p!=null){
			ut.process= p;
		}
		ut.start();
	}
	public static synchronized void startConsole() {
		start(new ReulstProcess(){
			@Override
			public void on(UpItem item) {
				 if(item.update&&item.url!=null){
					 String message=item.message;
					 if(message==null)message="";
					 System.out.println("有新版本，请从以下地址下载：");
					 System.out.println(item.url);
				 }
			}
		});
	}
	private volatile ReulstProcess process=null;
	private static final int version=1;
	@Override
	public void run(){
		try {
			String request1="http://101game.esy.es/filetransfer/update.php";
			String request2="http://www.zhangjinwen.win/filetransfer/update.php";
			String[] urls=new String[]{request1,request2};
			for(String str:urls){
				UpItem item = check(str,version);
				if(item!=null){
					process.on(item);
					return ;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	

	public static class UpItem{
		  boolean update;
		  public String name;
		  public String message;
		  public String url;
	}
	public interface ReulstProcess{
		public void on(UpItem item);
	}
	
	private UpItem check(String request1,int versionCode) throws Exception {
		String charset="UTF-8";
		String value = URLEncoder.encode(String.valueOf(versionCode),charset);
		String path = request1+"?version="+value;
		URL url = new URL(path);//此处的URL需要进行URL编码；
		 HttpURLConnection urlConnection = null;
		 BufferedReader in =null;
		try {
			  urlConnection = (HttpURLConnection) url.openConnection();
			  in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),charset));
	         String inputLine = null;
	         StringBuilder sb=new StringBuilder();
	         while ((inputLine = in.readLine()) != null) {
	        	 sb.append(inputLine).append("\n");
	         }
	       String[] lines = sb.toString().split(";;");
	         if(lines.length>0){
	        	 UpItem up=new UpItem();
	        	 boolean update=Boolean.parseBoolean(lines[0]);
	        	 up.update=update;
	        	 if(lines.length>1){
	        		 up.name=lines[1];
	        	 }
	        	 if(lines.length>2){
	        		 up.message=lines[2];
	        	 }
	        	 if(lines.length>3){
	        		 up.url=lines[3];
	        	 }
	        	 return up;
	         }
	         
		   } finally {
			   if(in!=null){
				   in.close(); //关闭字符输入流对象
			   }
			   if(urlConnection!=null){
				   urlConnection.disconnect();
			   }
		   }
		return null;
	}

}
