package zh3.maven.org.myapplication;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.View;

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

import zh.Config;
import zh.Connection;
import zh.FileItem;
import zh.Transfer;

/**
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
public class FileTransferTask extends AsyncTask<Void,  FileProgress, Boolean> {
    private FtActivity ftActivity;
    volatile boolean  RUN=true;
    private volatile  boolean restart=true;
    private FileProgress fp=new  FileProgress();
    private  FileProgress last=new  FileProgress();
    private Transfer transfer;

    FileTransferTask(FtActivity ftActivity) {
        this.ftActivity = ftActivity;

    }
    @Override
    protected void onPreExecute(){
             ftActivity.adapter.clear();
             ftActivity.progressBar.setVisibility(View.VISIBLE);
             ftActivity.progressBar.setMax(100);
             RUN=true;
           ftActivity.statusLog.setText("连接中...");
           ftActivity.invalidateOptionsMenu();
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
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ftActivity);
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



    Socket client = null;
    Connection connection=null;



    private void testClient( Config config){

        String ip=config.getIp();
        int port=config.getPort();
        try {
            publishProgress(log("尝试连接 "+timeoutTry+"  "+ip+" ......"));
            client = new Socket();
            client.connect(new InetSocketAddress(ip, port), 5*1000);
            client.setSoTimeout(15000);
            publishProgress(log("连接成功."+ip));
            InputStream in =new BufferedInputStream(client.getInputStream());
            OutputStream out =new BufferedOutputStream( client.getOutputStream());
            connection=new Connection(in,out);
            File src=new File(config.getSrcDir());
           transfer=new Transfer(config, ftActivity.logUtils);
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
            closeConnection();
        }
    }
    private int timeoutTry=0;
    public void closeAll(){
        RUN=false;
        closeConnection();
        restart=false;
    }

    private void closeConnection() {
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
    }

    @Override
    protected void onCancelled() {
        closeAll();
        ftActivity.invalidateOptionsMenu();
    }
    @Override
    protected void onPostExecute(final Boolean success) {
        StringBuilder sb=new StringBuilder();
        sb.append(last.current);
        sb.append("/");
        sb.append(last.total);
        sb.append("   ");
        if (success &&last.current>0&&last.current==last.total ) {
            ftActivity.logUtils.log("成功处理全部文件");
            sb.append("成功发送全部文件");
        } else if(last.current>0&&last.current<last.total){
            ftActivity.logUtils.log("失败");
            sb.append("部分文件没有发送");
        }
        ftActivity.statusLog.setText(sb.toString());
        closeAll();
        ftActivity.invalidateOptionsMenu();
    }
 private DecimalFormat df = new DecimalFormat("#.00");
    @Override
    protected void onProgressUpdate(FileProgress... ps) {
       FileProgress progress=ps[0];
        if(progress.txt!=null){
            ftActivity.logMessage(progress.txt);
            progress.txt=null;
        }
        if(last.total!=progress.total){
            ftActivity.progressBar.setMax(progress.total);
            last.total=progress.total;
        }

        if(last.current!=progress.current){
            ftActivity.progressBar.setProgress(progress.current);
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
            ftActivity.statusLog.setText(sb.toString());
        }
    }


    private long lastSend=0;
    private long lastSendTime=System.currentTimeMillis();



}
