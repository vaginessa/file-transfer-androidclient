package zh;

import android.widget.ArrayAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtils {
	private final ArrayAdapter adapter;
	private PrintWriter writer;
	private SimpleDateFormat formatter;
    
	public LogUtils(File outFile, ArrayAdapter adapter){
		try {
			 formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ");
			 writer=new PrintWriter(new OutputStreamWriter(new FileOutputStream(outFile),"UTF-8"));
		} catch (Exception e) {
			throw new RuntimeException("打开日志文件失败",e);
		}
		this.adapter=adapter;
   }
   public   void log(String text){
	   String log = formatter.format(new Date()) + text;
	   writer.println(log);
	   writer.flush();
	   System.out.println(log);

	   adapter.add(log);
	   adapter.notifyDataSetChanged();
   }
   public   void close(){
	   if(writer!=null){
		   writer.close();
	   }
   }
   
}
