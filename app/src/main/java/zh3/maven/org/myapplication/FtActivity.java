package zh3.maven.org.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import zh.LogUtils;


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
     volatile FileTransferTask mFileTask = null;

     ArrayAdapter adapter;
    private File cacheDir;
    private LinkedList logModel;
     ProgressBar progressBar;
     TextView statusLog;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }
    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (isTaskRun()) {
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
                startFileTranfer();
                return true;
            }
            case R.id.ft_faq: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent intent = new Intent(this,
                        HelpActivity.class);
                startActivityForResult(intent, REQUEST_CODE_FAQ_ACTIVITY);
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

    private void startFileTranfer() {
        if (isTaskRun()) {
                new AlertDialog.Builder(this).setTitle("文件发送中，确定取消发送？")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(mFileTask!=null){
                                    mFileTask.closeAll();
                                    mFileTask=null;
                                    invalidateOptionsMenu();
                                }
                                /*startActivityForResult(new Intent(FtActivity.this,
                                        FtSetActivity.class), REQUEST_CODE_SETTINGS_ACTIVITY);*/
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
         }else{
            startActivityForResult(new Intent(this,
                    FtSetActivity.class), REQUEST_CODE_SETTINGS_ACTIVITY);
        }
    }

    private boolean isTaskRun() {
        return mFileTask!=null&&mFileTask.RUN;
    }

    @Override
    public void onBackPressed(){
        if (isTaskRun()) {
            new AlertDialog.Builder(this).setTitle("正在发送，确认退出？")
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 点击“确认”后的操作
                            if(mFileTask!=null){
                                mFileTask.closeAll();
                                mFileTask=null;
                            }
                            FtActivity.this.finish();
                        }
                    })
                    .setNegativeButton("返回", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
        }else{
            FtActivity.this.finish();
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
            invalidateOptionsMenu();
        }
    }

    LogUtils logUtils;

    private void showTxt(String txt) {
        Toast.makeText(this, txt, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mFileTask!=null){
            mFileTask.closeAll();
            mFileTask=null;
        }
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

     void logMessage(String progres) {
        String log=   logUtils.log(progres);
        if(logModel.size()>200){
            logModel.removeFirst();
        }
        logModel.add(log);
        adapter.notifyDataSetChanged();

         UpdateThread.start(new UpdateThread.ReulstProcess(){
             @Override
             public void on(final UpdateThread.UpItem item) {
                 if(item.update&&item.url!=null){
                     String message=item.message;
                     if(message==null)message="";
                     FtActivity.this.runOnUiThread(new Runnable() {
                         @Override
                         public void run() {
                             showUpdate("有新版本,是否更新?",item.url);
                         }
                     });

                 }
             }


         },"android");
    }

    private void showUpdate(String tile,final String url) {
        new AlertDialog.Builder(this).setTitle(tile)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent intent = new Intent();
                        intent.setAction("android.intent.action.VIEW");
                        Uri content_url = Uri.parse(url);
                        intent.setData(content_url);
                        startActivity(intent);

                        // 点击“确认”后的操作

                    }
                })
                .setNegativeButton("返回", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (isTaskRun()) {
            showTxt("运行中...");
            return;
        }
        boolean cancel = false;
        View focusView = null;
        mFileTask = new FileTransferTask(this);
        mFileTask.execute((Void) null);
        invalidateOptionsMenu();
    }



}

