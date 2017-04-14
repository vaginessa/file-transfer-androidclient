package zh3.maven.org.myapplication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
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
import java.util.ArrayList;
import java.util.List;

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

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private ArrayAdapter adapter;
    private File cacheDir;


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
               // attemptLogin();
            }else if(resultCode==RESULT_CANCELED){
                showTxt("取消");
            }
        }
    }



    private void showTxt(String txt) {
        Toast.makeText(this, txt, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ft);
        ListView logList= (ListView) this.findViewById(R.id.logList);
        cacheDir=getCacheDir();
        adapter   = new ArrayAdapter(this,
                android.R.layout.simple_list_item_2, android.R.id.text1, new ArrayList<>());
        logList.setAdapter(adapter);
        
        
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
        showProgress(true);
        mFileTask = new FileTransferTask();
        mFileTask.execute((Void) null);
        
    }

    

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(FtActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class FileTransferTask extends AsyncTask<Void, Void, Boolean> {

        FileTransferTask() {

        }
        private static final String logFile="logSend.txt";
        @Override
        protected Boolean doInBackground(Void... params) {
            adapter.clear();
            adapter.notifyDataSetChanged();
            //客户端请求与本机在20006端口建立TCP连接
            File logFile = null;
            try {
                logFile = File.createTempFile("log", ".inf", cacheDir);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("读取配置失败"+e.getMessage());
            }
            LogUtils logUtils=new LogUtils(logFile,adapter);
            Config config;
            try {
                SharedPreferences sp=getSharedPreferences(FtSetActivity.SETTING_FILE, Context.MODE_PRIVATE);
                config = new Config(sp);
            } catch (IOException e2) {
                e2.printStackTrace();
                logUtils.log("读取配置失败");
                return false;
            }
            String srcDir=config.getSrcDir();
            File fileSrc = new File(srcDir);
            if(!fileSrc.exists()){
                logUtils.log(fileSrc.getAbsolutePath()+" 文件目录不存在，请选择要转移的目录！");
                return false;
            }
            if(!fileSrc.isDirectory()){
                logUtils.log(fileSrc.getAbsolutePath()+" 不是目录，请选择要转移的目录！");
                return false;
            }
            logUtils.log("转移目录 "+fileSrc.getAbsolutePath());

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
                        logUtils.log("skip not  file "+file.getAbsolutePath());
                        continue;
                    }
                    FileItem fileItem=new FileItem();
                    fileItem.setName(file.getName());
                    fileItem.setPath(file.getAbsolutePath());
                    fileItem.setSize(file.length());
                    logUtils.log("发送文件 "+file.getName());
                    transfer.clientSend(fileItem, connection);
                    totalSend++;
                }
                transfer.exitFile(connection);
                logUtils.log("共发送"+totalSend+"个文件");
                logUtils.log("退出执行");
                logUtils.close();
                return true;
            }  catch (Exception e1) {
                e1.printStackTrace();
                logUtils.log("出现错误："+e1.getMessage());
            }  finally {
                if(connection!=null){
                    connection.close();
                }
                if(logUtils!=null){
                    logUtils.close();
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
            showProgress(false);

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mFileTask = null;
            showProgress(false);
        }
    }
}

