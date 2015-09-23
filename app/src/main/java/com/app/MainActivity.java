package com.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.zxing.client.android.CaptureActivity;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;


public class MainActivity extends Activity {
    private Thread mThread;
    private TextView mHttpResult;
    private TextView mScanResult;
    private int _tmp = 0;
    private static int OP_OK = 0;
    private static int OP_FAIL = 1;

    public static final int SCAN_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = (Button) findViewById(R.id.scan_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                startActivityForResult(intent, SCAN_CODE);
            }
        });

        mScanResult = (TextView) findViewById(R.id.scan_result);
        mHttpResult = (TextView) findViewById(R.id.http_result);  //android.R.id.http_result is error

        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                try{
                    //mHttpResult.setText(EntityUtils.toString( ((HttpResponse)msg.obj).getEntity()));
                    mHttpResult.setText("size: "+msg.obj.toString());
                    //mHttpResult.setText( Integer.toString(EntityUtils.toString(((HttpResponse) msg.obj).getEntity()).length()) );
                }catch(Exception ex){
                    mHttpResult.setText(ex.getMessage().toString());
                }
            }
        };

        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    while(true){
                        Thread.currentThread().sleep(1000);
                        Message msg = new Message();
                        //msg.obj = mScanResult.getText() + Integer.toString(_tmp);
                        msg.what = OP_OK;

                        if (mScanResult.getText().length() == 0) continue;

                        HttpGet request = new HttpGet(mScanResult.getText().toString());
                        HttpClient hc = new DefaultHttpClient();
                        HttpParams httpParams = hc.getParams().setParameter(
                                CoreProtocolPNames.USER_AGENT,
                                "Mozilla/5.0 (Linux; U; Android 2.3.6; en-us; Nexus S Build/GRK39F) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");//Android 2.3 Nexus S
                        try{
                            HttpResponse resp = hc.execute(request);
                            String tmps = EntityUtils.toString( ((HttpResponse)resp).getEntity());
                            String _findstr = "<span id=\"price\" class=\"p-price\">&yen;";

                            int pos = tmps.indexOf(_findstr) + _findstr.length();
                            tmps = tmps.substring(pos, pos+100);
                            pos = tmps.indexOf(' ');

                            //msg.obj = Integer.toString(tmps.length());
                            msg.obj = tmps.substring(0, pos);
                        }catch(Exception ex){
                            msg.what = OP_FAIL;
                            msg.obj = ex.getMessage();
                        }
                        handler.sendMessage(msg);
                    }
                }catch(Exception e){
                    //e.printStackTrace();
                    mHttpResult.setText(e.getMessage().toString());
                }
            }
        });
        mThread.start();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SCAN_CODE:
                //TextView scanResult = (TextView) findViewById(R.id.scan_result);
                if (resultCode == RESULT_OK) {
                    String result = data.getStringExtra("scan_result");
                    mScanResult.setText(result);
                } else if (resultCode == RESULT_CANCELED) {
                    mScanResult.setText("没有扫描出结果");
                }
                break;
            default:
                break;
        }
    }
























    

}

/*
public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // A placeholder fragment containing a simple view.
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }
}
*/


