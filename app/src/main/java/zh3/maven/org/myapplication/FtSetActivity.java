package zh3.maven.org.myapplication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class FtSetActivity   extends AppCompatActivity{
    private static final String TAG="FtSetActivity";
    private static final int REQUEST_CODE_FILE_EXPLORER_ACTIVITY=204;
    public static final String SETTING_FILE= "ft_setting";
    private SettingsFragment sf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Display the fragment as the main content.
         sf=   new SettingsFragment();
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, sf)
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragment {
        private Preference button;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesName(SETTING_FILE);
            addPreferencesFromResource(R.xml.ft_pref_set);
            button = findPreference("src_dir_text");
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(SettingsFragment.this.getContext(),
                            ListFileActivity.class);
                    Bundle bundle=new Bundle();
                    SharedPreferences sp=getActivity().getSharedPreferences(FtSetActivity.SETTING_FILE, Context.MODE_PRIVATE);
                    String srcDir = sp.getString("src_dir_text", "/");
                    bundle.putString("path", srcDir);
                    intent.putExtras(bundle);
                    getActivity().startActivityForResult(intent, REQUEST_CODE_FILE_EXPLORER_ACTIVITY);
                    return true;
                }
            });
        }

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        Log.d(TAG,"onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_FILE_EXPLORER_ACTIVITY  ) {
            if(resultCode==RESULT_OK){
                String path=data.getExtras().get("path").toString();
                Log.d(TAG,path);
                sf.button.setSummary(path);
            }else if(resultCode==RESULT_CANCELED){
                showTxt("取消");
            }
        }
    }

    private void showTxt(String txt) {
        Toast.makeText(this, txt, Toast.LENGTH_LONG).show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.confirm, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ft_confirm: {
                checkSet();
                return true;
            }
            default: return super.onOptionsItemSelected(item);
        }

    }

    private void checkSet() {
        SharedPreferences sp = sf.getPreferenceManager().getSharedPreferences();
        String srcDir = sp.getString("src_dir_text", "/");
        boolean delAfterSuccess = sp.getBoolean("def_success_switch", true);
        String ip = sp.getString("conn_ip_text", "192.168.0.10");
        int port = sp.getInt("conn_port_text", 20009);

        this.setResult(RESULT_OK);
        this.finish();
    }
}

