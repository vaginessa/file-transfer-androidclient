package zh;

import android.util.Log;
import android.widget.ArrayAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import zh3.maven.org.myapplication.FtActivity;

public class LogUtils {
	private static final String TAG="LogFt";
	private final ArrayAdapter adapter;
	private final LinkedList logModel;
	private final FtActivity ftActivity;
	private PrintWriter writer;
	private SimpleDateFormat formatter;
    
	public LogUtils(File outFile,ArrayAdapter adapter,LinkedList log, FtActivity ftActivity){
		try {
			 formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ");
			 writer=new PrintWriter(new OutputStreamWriter(new FileOutputStream(outFile),"UTF-8"));
		} catch (Exception e) {
			throw new RuntimeException("打开日志文件失败",e);
		}
		this.adapter=adapter;
		this.logModel =log;
		this.ftActivity=ftActivity;
   }
   public   String log(String text){
	   final String log = formatter.format(new Date()) + text;
	   writer.println(log);
	   writer.flush();
	   Log.v(TAG, log);

	 /*  ftActivity.runOnUiThread(new Runnable() {
		   @Override
		   public void run() {
			   if(logModel.size()>200){
				   logModel.removeFirst();
			   }

			   logModel.add(log);
			   adapter.notifyDataSetChanged();
		   }
	   });*/
       return log;
   }
   public   void close(){
	   if(writer!=null){
		   writer.close();
	   }
   }
   
}
