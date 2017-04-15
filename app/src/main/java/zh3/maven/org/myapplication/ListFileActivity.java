package zh3.maven.org.myapplication;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

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
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_files);
        currentFileDirView = (TextView) this.findViewById(R.id.current_file_dir);
        currentFileDirView.setText("请选择转移目录");
        fileUp = (Button) this.findViewById(R.id.ft_file_up);
        fileConfirm = (Button) this.findViewById(R.id.ft_file_confirm);

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
        currentFileDirView.setText("");
        adapter.clear();
        adapter.addAll(getAllFiles(path));
        adapter.notifyDataSetChanged();
    }

    @NonNull
    private List getAllFiles(String dir) {
        File dirFile = new File(dir);
        if (!dirFile.canRead()) {
            currentFileDirView.setText(dir + " (不可访问)");
            Toast.makeText(this, dir + "不可访问", Toast.LENGTH_LONG).show();
            return new ArrayList();
        }
        setTitle(dir);
        File[] list = dirFile.listFiles();
        if (list != null) {
            List<String> values = new ArrayList<String>(list.length);
            for (File file : list) {
                if (!file.getName().startsWith(".") && file.isDirectory() && file.canRead()) {
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


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("ListFile Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
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