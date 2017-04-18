package zh3.maven.org.myapplication;

import android.Manifest;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;



import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by user on 2017/4/13.
 */

public class ListFileActivity extends ListActivity {
    private static final String TAG = "ListFileActivity";

    private ArrayAdapter adapter;
    private TextView currentFileDirView;
    private Button fileUp;
    private Button fileConfirm;
    private Button ftStorageSd;
    private Button ftStoragePic;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_files);
        currentFileDirView = (TextView) this.findViewById(R.id.current_file_dir);
        //currentFileDirView.setText("请选择转移目录");
        fileUp = (Button) this.findViewById(R.id.ft_file_up);
        fileConfirm = (Button) this.findViewById(R.id.ft_file_confirm);
        checkSdcard();
        ftStoragePic = (Button) this.findViewById(R.id.ft_storage_pic);
        ftStorageSd = (Button) this.findViewById(R.id.ft_storage_sd);

        ftStorageSd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String path = Environment.getExternalStorageDirectory()
                        .getAbsolutePath();
               if(!Environment.getExternalStorageState().equals(
                        Environment.MEDIA_MOUNTED)){
                   setFilePath(path);
               }else{

                   File file=new File(path);
                   if(file.canRead()){
                       setFilePath(path);
                   }else{
                       Toast.makeText(ListFileActivity.this,  "存储卡不可用", Toast.LENGTH_LONG).show();
                   }
               }

            }
        });
        ftStoragePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             File file = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES);
                if (!file.mkdirs()) {
                    Log.e(TAG, "Directory not created");
                }
                setFilePath(file.getAbsolutePath());
            }
        });
        fileUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String path = ListFileActivity.this.getTitle().toString();
                File f = new File(path);
                if (f.getParentFile() != null) {
                    setFilePath(f.getParentFile().getAbsolutePath());
                } else {
                    Toast.makeText(ListFileActivity.this, f.getAbsolutePath() + "上级目录不存在", Toast.LENGTH_LONG).show();
                }
            }
        });
        fileConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String path=ListFileActivity.this.getTitle().toString();
                Intent intent = new Intent();
                intent.putExtra("path",path );
                Log.d(TAG, "fileConfirm" + ListFileActivity.this.getTitle().toString());

                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ListFileActivity.this);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString ("src_dir_text",path);
                editor.commit();

                ListFileActivity.this.setResult(RESULT_OK, intent);
                ListFileActivity.this.finish();
            }
        });


        // Read all files sorted into the values-array
        // Put the data into the list
        adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_2, android.R.id.text1, new ArrayList<>());
        setListAdapter(adapter);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ListFileActivity.this);

        setFilePath(sharedPref.getString("src_dir_text","/"));

    }
    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private AlertDialog dialog;
    private void checkSdcard() {
        // 版本判断。当手机系统大于 23 时，才有必要去判断权限是否获取
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 检查该权限是否已经获取
            int i = ContextCompat.checkSelfPermission(this, permissions[0]);
            // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
            if (i != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                showDialogTipUserRequestPermission();
            }
        }
    }
    // 提示用户该请求权限的弹出框
    private void showDialogTipUserRequestPermission() {

        new AlertDialog.Builder(this)
                .setTitle("存储权限不可用")
                .setMessage("需要获取存储空间\n否则，您将无法从存储卡转移文件")
                .setPositiveButton("立即开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startRequestPermission();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setCancelable(false).show();
    }
    // 开始提交请求权限
    private void startRequestPermission() {
        ActivityCompat.requestPermissions(this, permissions, 321);
    }
    // 用户权限 申请 的回调方法
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 321) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    // 判断用户是否 点击了不再提醒。(检测该权限是否还可以申请)
                    boolean b = shouldShowRequestPermissionRationale(permissions[0]);
                    if (!b) {
                        // 用户还是想用我的 APP 的
                        // 提示用户去应用设置界面手动开启权限
                        showDialogTipUserGoToAppSettting();
                    } else
                        finish();
                } else {
                    Toast.makeText(this, "权限获取成功", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    // 提示用户去应用设置界面手动开启权限

    private void showDialogTipUserGoToAppSettting() {

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("存储权限不可用")
                .setMessage("请在-应用设置-权限-中，允许使用存储权限")
                .setPositiveButton("立即开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 跳转到应用设置界面
                        goToAppSetting();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setCancelable(false).show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123) {

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 检查该权限是否已经获取
                int i = ContextCompat.checkSelfPermission(this, permissions[0]);
                // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
                if (i != PackageManager.PERMISSION_GRANTED) {
                    // 提示用户应该去应用设置界面手动开启权限
                    showDialogTipUserGoToAppSettting();
                } else {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    Toast.makeText(this, "权限获取成功", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // 跳转到当前应用的设置界面
    private void goToAppSetting() {
        Intent intent = new Intent();

        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);

        startActivityForResult(intent, 123);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String filename = (String) getListAdapter().getItem(position);
        String path = this.getTitle().toString();
        if (path.endsWith(File.separator)) {
            filename = path + filename;
        } else {
            filename = path + File.separator + filename;
        }

        File file = new File(filename);
        if (file.isDirectory()) {
            setFilePath(file.getAbsolutePath());
        } else {
            Toast.makeText(this, filename + " 不是目录", Toast.LENGTH_LONG).show();
        }
    }

    private void setFilePath(String path) {
        setTitle(path);
        currentFileDirView.setText(path);
        adapter.clear();
        adapter.addAll(getAllFiles(path));
        adapter.notifyDataSetChanged();
    }

    @NonNull
    private List getAllFiles(String dir) {
        File dirFile = new File(dir);
        if (!dirFile.canRead()) {
            currentFileDirView.setText(dir);
            Toast.makeText(this, dir + "不可访问", Toast.LENGTH_LONG).show();
            return new ArrayList();
        }
        setTitle(dir);
        File[] list = dirFile.listFiles();
        if (list != null) {
            List<String> values = new ArrayList<String>(list.length);
            for (File file : list) {
                if (!file.getName().startsWith(".")  && file.canRead()) {
                    values.add(file.getName());
                }
            }
            Collections.sort(values);
            return values;
        } else {
            Toast.makeText(this, dir + "不可访问", Toast.LENGTH_LONG).show();
        }
        return new ArrayList();
    }



    @Override
    public void onStart() {
        super.onStart();


    }

    @Override
    public void onStop() {
        super.onStop();


    }
}