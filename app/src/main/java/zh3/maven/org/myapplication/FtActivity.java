package zh3.maven.org.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

import zh.Config;
import zh.Connection;
import zh.FileItem;
import zh.LogUtils;
import zh.Transfer;


/**
 * A login screen that offers login via email/password.
 */
public class FtActivity extends AppCompatActivity  {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;
    private static final int REQUEST_CODE_SETTINGS_ACTIVITY=102;
    private static final int REQUEST_CODE_FAQ_ACTIVITY=103;
    private static final int REQUEST_CODE_FEEDBACK_ACTIVITY=104;


    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private volatile FileTransferTask mFileTask = null;

    private ArrayAdapter adapter;
    private File cacheDir;
    private LinkedList logModel;
    private ProgressBar progressBar;
    private TextView statusLog;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mFileTask!=null&&!mFileTask.RUN) {
            menu.findItem(R.id.ft_start).setTitle("停止" );
        }else{
            menu.findItem(R.id.ft_start).setTitle("开始" );
        }
        return super.onPrepareOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ft_start: {
                // Launch the DeviceListActivity to see devices and do scan
                startActivityForResult(new Intent(this,
                        FtSetActivity.class), REQUEST_CODE_SETTINGS_ACTIVITY);
                return true;
            }
            case R.id.ft_faq: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse("http://101game.esy.es/filetransfer/faq.php");
                intent.setData(content_url);
                startActivity(intent);
                return true;
            }
            case R.id.ft_feedback: {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse("http://101game.esy.es/filetransfer/feedback.php");
                intent.setData(content_url);
                startActivity(intent);
                return true;
            }
            default: return super.onOptionsItemSelected(item);
        }

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SETTINGS_ACTIVITY  ) {
            if(resultCode==RESULT_OK){
               attemptLogin();
            }else if(resultCode==RESULT_CANCELED){
                showTxt("取消");
            }
        }
    }

    private LogUtils logUtils;

    private void showTxt(String txt) {
        Toast.makeText(this, txt, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        logUtils.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ft);
        ListView logList= (ListView) this.findViewById(R.id.logList);
        progressBar= (ProgressBar) this.findViewById(R.id.progressBar);
        statusLog= (TextView) this.findViewById(R.id.status_log);



        logModel=new LinkedList();
        cacheDir=getCacheDir();
        adapter   = new ArrayAdapter(this,
                android.R.layout.simple_list_item_2, android.R.id.text1,logModel);
        logList.setAdapter(adapter);


        File logFile = null;
        try {
            logFile = File.createTempFile("log", ".inf", cacheDir);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("读取配置失败"+e.getMessage());
        }
        logUtils=new LogUtils(logFile,adapter,logModel,FtActivity.this);
        logMessage("点击右上角| 开始.");

    }

    private void logMessage(String progres) {
        String log=   logUtils.log(progres);
        if(logModel.size()>200){
            logModel.removeFirst();
        }
        logModel.add(log);
        adapter.notifyDataSetChanged();
    }




    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mFileTask != null) {
            return;
        }

        boolean cancel = false;
        View focusView = null;
        mFileTask = new FileTransferTask();
        mFileTask.execute((Void) null);

        invalidateOptionsMenu();

    }

    



    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class FileTransferTask extends AsyncTask<Void, FileProgress, Boolean> {
        private volatile boolean  RUN=true;
        private volatile  boolean restart=true;
        private  FileProgress fp=new FileProgress();
        private  FileProgress last=new FileProgress();
        private Transfer transfer;

        FileTransferTask() {

        }
        @Override
        protected void onPreExecute(){
                 adapter.clear();
                 progressBar.setVisibility(View.VISIBLE);
                 progressBar.setMax(100);
                 RUN=true;
               invalidateOptionsMenu();
        }
        private FileProgress log(String txt){
            fp.txt=txt;
            return fp;
        }


        @Override
        protected Boolean doInBackground(Void... params) {

            //客户端请求与本机在20006端口建立TCP连接
            Config config;
            try {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(FtActivity.this);
                config = new Config(sp);
            } catch (IOException e2) {
                e2.printStackTrace();

                publishProgress(log("读取配置失败"));
                return false;
            }
            String srcDir=config.getSrcDir();
            File fileSrc = new File(srcDir);
            if(!fileSrc.exists()){
                publishProgress(log(fileSrc.getAbsolutePath()+" 文件目录不存在，请选择要转移的目录！"));
                return false;
            }
            if(!fileSrc.isDirectory()){
                publishProgress(log(fileSrc.getAbsolutePath()+" 不是目录，请选择要转移的目录！"));
                return false;
            }
            while(restart&&RUN){
                restart=false;
                publishProgress(log("转移目录 "+fileSrc.getAbsolutePath()));
                testClient(config);
                if(restart){
                    //延迟5秒再试
                    try {
                        Thread.sleep(5*1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            RUN=false;
            return true;
        }

         public void shutdown(){
             RUN=false;
             closeAll();
         }

        Socket client = null;
        Connection connection=null;



        private void testClient( Config config){

            String ip=config.getIp();
            int port=config.getPort();

            try {
                publishProgress(log("尝试连接 "+ip+" ......"+timeoutTry));
                client = new Socket();
                client.connect(new InetSocketAddress(ip, port), 5*1000);
                client.setSoTimeout(15000);
                publishProgress(log("连接成功."+ip));
                InputStream in =new BufferedInputStream(client.getInputStream());
                OutputStream out =new BufferedOutputStream( client.getOutputStream());
                connection=new Connection(in,out);
                File src=new File(config.getSrcDir());
               transfer=new Transfer(config,logUtils);
                int totalSend=0;
                File[] allFiles = src.listFiles();
                  fp.total=allFiles.length;
                  fp.current=0;
                  publishProgress(fp);
                for(File file:allFiles){
                    if(!file.isFile()){
                        fp.current++;
                        publishProgress(log("skip not  file "+file.getAbsolutePath()));
                        continue;
                    }
                    FileItem fileItem=new FileItem();
                    fileItem.setName(file.getName());
                    fileItem.setPath(file.getAbsolutePath());
                    fileItem.setSize(file.length());
                    fp.current++;
                    publishProgress(log("发送文件 "+file.getName()));
                    transfer.clientSend(fileItem, connection);
                    totalSend++;
                }
                transfer.exitFile(connection);
                publishProgress(log("共发送"+totalSend+"个文件"));
                publishProgress(log("完成全部文件发送,退出。"));
                restart=false;
            }  catch (Exception e1) {
                if(e1 instanceof SocketTimeoutException){
                    publishProgress(log("连接超时，请检查服务端是否打开;ip地址和端口配置."));
                    if(timeoutTry++>10){
                        restart=false;
                        publishProgress(log("连接超时，10次尝试失败退出."));
                        return;
                    }
                }
                e1.printStackTrace();
                publishProgress(log("出现错误："+e1.getMessage()));
                restart=true;
            }  finally {
                closeAll();
            }
        }
        private int timeoutTry=0;
        public void closeAll(){
            if(connection!=null){
                connection.close();
                connection=null;
            }
            if (client != null) {
                try {
                    client.close();
                    client=null;
                } catch (IOException e) {
                    e.printStackTrace();
                    restart=true;
                }
            }
            mFileTask = null;
            invalidateOptionsMenu();
        }

        @Override
        protected void onCancelled() {
            mFileTask.closeAll();
            invalidateOptionsMenu();

        }
        @Override
        protected void onPostExecute(final Boolean success) {
            StringBuilder sb=new StringBuilder();
            sb.append(last.current);
            sb.append("/");
            sb.append(last.total);
            sb.append("   ");
            if (success) {
                logUtils.log("成功处理全部文件");
                sb.append("成功发送全部文件");
            } else {
                logUtils.log("失败");
                sb.append("部分文件没有发送");
            }
            statusLog.setText(sb.toString());
            if(mFileTask!=null){
                mFileTask.closeAll();
            }

            invalidateOptionsMenu();
        }
     private   DecimalFormat df = new DecimalFormat("#.00");
        @Override
        protected void onProgressUpdate(FileProgress... ps) {
            FileProgress   progress=ps[0];
            if(progress.txt!=null){
                logMessage(progress.txt);
                progress.txt=null;
            }
            if(last.total!=progress.total){
                progressBar.setMax(progress.total);
                last.total=progress.total;
            }

            if(last.current!=progress.current){
                progressBar.setProgress(progress.current);
                last.current=progress.current;
            }
            if(transfer!=null){
                long totalSend = transfer.getTotalByteSend();
                long curTime=System.currentTimeMillis();
                double rate = ((double) totalSend - lastSend)/((curTime-lastSendTime)/1000);
                int  kb=1024;
                int  mb=1024*1024;
                String rateTxt="";
                if(rate>mb){
                    rateTxt= df.format(rate/mb)+"MB/S";
                }else if(rate>kb){
                    rateTxt= df.format(rate/kb)+"KB/S";
                }else{
                    rateTxt= df.format(rate)+"B/S";
                }
                StringBuilder sb=new StringBuilder();
                sb.append(rateTxt);
                sb.append("  ");
                sb.append(last.current);
                sb.append("/");
                sb.append(last.total);
                statusLog.setText(sb.toString());
            }
        }


        private long lastSend=0;
        private long lastSendTime=System.currentTimeMillis();
    }



    static class  FileProgress{
       private volatile String txt;
       private volatile int total;
       private volatile int current;


    }

}

