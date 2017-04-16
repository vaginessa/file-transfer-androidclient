package zh3.maven.org.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;

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
    private FileTransferTask mFileTask = null;

    private ArrayAdapter adapter;
    private File cacheDir;
    private LinkedList logModel;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
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
        
    }

    



    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class FileTransferTask extends AsyncTask<Void, String, Boolean> {



        FileTransferTask() {

        }
        @Override
        protected void onPreExecute(){
                 adapter.clear();


        }

        private static final String logFile="logSend.txt";
        @Override
        protected Boolean doInBackground(Void... params) {

            //客户端请求与本机在20006端口建立TCP连接
            Config config;
            try {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(FtActivity.this);
                config = new Config(sp);
            } catch (IOException e2) {
                e2.printStackTrace();
                publishProgress("读取配置失败");
                return false;
            }
            String srcDir=config.getSrcDir();
            File fileSrc = new File(srcDir);
            if(!fileSrc.exists()){
                publishProgress(fileSrc.getAbsolutePath()+" 文件目录不存在，请选择要转移的目录！");
                return false;
            }
            if(!fileSrc.isDirectory()){
                publishProgress(fileSrc.getAbsolutePath()+" 不是目录，请选择要转移的目录！");
                return false;
            }
            publishProgress("转移目录 "+fileSrc.getAbsolutePath());

            String ip=config.getIp();
            int port=config.getPort();
            Socket client = null;
            Connection connection=null;
            try {
                client = new Socket(ip, port);
                client.setSoTimeout(15000);
                InputStream in =new BufferedInputStream(client.getInputStream());
                OutputStream out =new BufferedOutputStream( client.getOutputStream());
                 connection=new Connection(in,out);
                File src=new File(srcDir);
                Transfer transfer=new Transfer(config,logUtils);
                int totalSend=0;
                for(File file:src.listFiles()){
                    if(!file.isFile()){
                        publishProgress("skip not  file "+file.getAbsolutePath());
                        continue;
                    }
                    FileItem fileItem=new FileItem();
                    fileItem.setName(file.getName());
                    fileItem.setPath(file.getAbsolutePath());
                    fileItem.setSize(file.length());
                    publishProgress("发送文件 "+file.getName());
                    transfer.clientSend(fileItem, connection);
                    totalSend++;
                }
                transfer.exitFile(connection);
                publishProgress("共发送"+totalSend+"个文件");
                publishProgress("退出执行");

                return true;
            }  catch (Exception e1) {
                e1.printStackTrace();
                publishProgress("出现错误："+e1.getMessage());
            }  finally {
                if(connection!=null){
                    connection.close();
                }
                if (client != null) {
                    try {
                        client.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mFileTask = null;
            if (success) {
                logUtils.log("成功处理全部文件");
            } else {
                logUtils.log("失败");
            }
        }
        protected void onProgressUpdate(String...  progress) {
            logMessage(progress[0]);
        }

        @Override
        protected void onCancelled() {
            mFileTask = null;

        }
    }

    private void logMessage(String progres) {
     String log=   logUtils.log(progres);
        if(logModel.size()>200){
            logModel.removeFirst();
        }
        logModel.add(log);
        adapter.notifyDataSetChanged();
    }
}

