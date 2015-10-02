package com.app;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.zxing.client.android.CaptureActivity;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;

/*
 * case: http://item.m.jd.com/product/1032476598.html
 */
public class MainActivity extends Activity {
    private Thread mThread;
    private TextView mHttpResult;
    private TextView mScanResult;
    private TextView mTitleResult;
    private int _tmp = 0;
    private static int OP_OK = 0;
    private static int OP_FAIL = 1;
    private static int OP_TITLE = 2;
    private static int OP_PRICE = 3;
    private static float g_price = 0;

    public static final int SCAN_CODE = 1;

    public void vibrate(long msec){
        Vibrator vib = (Vibrator)this.getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(msec);
    }

    private void sound(){
        Uri notif = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        Ringtone rt = RingtoneManager.getRingtone(this, notif);
        try {
            rt.wait((long)1000);
        }
        catch (Exception ex){
            ;
        }
        rt.play();
    }

    private String urlConvert(String s){
        String _url = s;
        if (_url.indexOf('/')==-1 || _url.indexOf(".html")==-1)
            return "";
        int pos = _url.lastIndexOf('/');
        _url = _url.substring(pos+1, pos+_url.length()-pos);
        _url = "http://item.m.jd.com/product/" + _url;//"1425705940.html";
        return _url;
    }

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
        mTitleResult = (TextView) findViewById(R.id.title_result);

        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                try{
                    //mHttpResult.setText(EntityUtils.toString( ((HttpResponse)msg.obj).getEntity()));
                    if (msg.what == OP_PRICE){
                        float _price = Float.valueOf(msg.obj.toString());
                        if (_price != g_price){
                            if (g_price > _price){
                                ;//vibrate(3000);//hint
                                sound();
                            }
                            g_price = _price;
                        }
                        mHttpResult.setText("价格:  " + _price);
                        //mHttpResult.setText( Integer.toString(EntityUtils.toString(((HttpResponse) msg.obj).getEntity()).length()) );
                    }
                    else if (msg.what == OP_TITLE){
                        mTitleResult.setText(msg.obj.toString());
                    }
                }catch(Exception ex){
                    mHttpResult.setText("Exception:  " + ex.getMessage().toString());
                    vibrate(1000);
                    return;
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
                        msg.what = OP_PRICE;

                        if (mScanResult.getText().length() == 0) continue;

                        String _url = urlConvert(mScanResult.getText().toString());
                        if ("" == _url) continue;

                        HttpGet request = new HttpGet(_url);
                        HttpClient hc = new DefaultHttpClient();
                        hc.getParams().setParameter(CoreProtocolPNames.USER_AGENT,
                                "Mozilla/5.0 (Linux; U; Android 2.3.6; en-us; Nexus S Build/GRK39F) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");//Android 2.3 Nexus S
                        try{
                            HttpResponse resp = hc.execute(request);
                            String tmps = EntityUtils.toString( ((HttpResponse)resp).getEntity());

                            Message _msg = new Message();
                            _msg.what = OP_TITLE;
                            String _findtitle = "id=\"wareName\">";
                            int pos = tmps.indexOf(_findtitle) + _findtitle.length();
                            if (-1 == pos){
                                //mTitleResult.setText("null");
                                _msg.obj = "null";
                            }
                            else{
                                String _tmp = tmps.substring(pos, pos+100);
                                //mTitleResult.setText( _tmp.substring(0, _tmp.indexOf('<')) );
                                _msg.obj = _tmp.substring(0, _tmp.indexOf('<'));
                            }
                            handler.sendMessage(_msg);

                            String _findstr = "<span id=\"price\" class=\"p-price\">&yen;";
                            pos = tmps.indexOf(_findstr) + _findstr.length();
                            tmps = tmps.substring(pos, pos+100);
                            pos = tmps.indexOf('<');

                            //msg.obj = Integer.toString(tmps.length());
                            msg.obj = tmps.substring(0, pos);
                        }catch(Exception ex){
                            //msg.what = OP_FAIL;
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
                if (resultCode == RESULT_OK) {
                    String result = data.getStringExtra("scan_result");
                    result = urlConvert(result);
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


