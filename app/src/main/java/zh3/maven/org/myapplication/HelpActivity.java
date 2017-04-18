package zh3.maven.org.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import static android.R.attr.description;

public class HelpActivity extends AppCompatActivity {

    private WebView helpView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        helpView = (WebView) findViewById(R.id.helpView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        setUpView();
    }

    private void setUpView() {
        //加载需要显示的网页

     /*   Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri content_url = Uri.parse("http://101game.esy.es/filetransfer/faq.php");
        intent.setData(content_url);
        startActivity(intent);*/


        //设置WebView属性，能够执行Javascript脚本
        helpView.getSettings().setJavaScriptEnabled(true);
        WebSettings mWebSettings = helpView.getSettings();
        mWebSettings.setJavaScriptEnabled(true);    //允许加载javascript
        mWebSettings.setSupportZoom(true);          //允许缩放
        //mWebSettings.setBuiltInZoomControls(true);  //原网页基础上缩放
       // mWebSettings.setUseWideViewPort(true);      //任意比例缩放
        helpView.setWebViewClient(webClient);  //设置Web视图
        progressBar.setVisibility(View.VISIBLE);
        helpView.loadUrl("http://101game.esy.es/filetransfer/faq.php");
    }

    WebViewClient webClient = new WebViewClient(){
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            Toast.makeText(HelpActivity.this, "网络请求错误", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            String text=  getFileText(R.raw.help);
            helpView.loadData(text, "text/html; charset=UTF-8", "UTF-8");
            helpView.setWebViewClient(null);
        };

        @Override
        public void onPageFinished(WebView view, String url) {
            progressBar.setVisibility(View.GONE);
        }
    };

    private String getFileText(int help)  {
        InputStream is =null;
        BufferedReader reader=null;
        try {
             is = getResources().openRawResource(help) ;
             reader=new BufferedReader(new InputStreamReader(is,"UTF-8"));
            String line=null;
            StringBuilder sb=new StringBuilder();
            while((line=reader.readLine())!=null){
                sb.append(line);
            }
            return sb.toString();
        }  catch (Exception e) {
            e.printStackTrace();
            return null;
        }finally {
            if(reader!=null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(is!=null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
