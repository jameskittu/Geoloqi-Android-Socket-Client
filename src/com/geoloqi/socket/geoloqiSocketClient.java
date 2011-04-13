package com.geoloqi.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.geoloqi.socket.GeoloqiService.LocalBinder;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

public class GeoloqiSocketClient extends Activity implements OnClickListener {
	static String TAG = "geoloqi.socket";
	
	GeoloqiService mService;
    boolean mBound = false;

	private Handler handler = new Handler();
	private Button buttonStart;

	public TextView textView;
	public ScrollView scrollView;
	
	public WebView webView;
	LQWebViewClient browserHandler = new LQWebViewClient();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		buttonStart = (Button)findViewById(R.id.buttonStart);
		
		scrollView = (ScrollView)findViewById(R.id.scrollView);

		textView = (TextView)findViewById(R.id.textView1); 
		textView.setText("Connecting\n");

		webView = (WebView)findViewById(R.id.webView);
		webView.loadUrl("http://loqi.me/pdx-pacmap/app-test.php");
		WebSettings ws = webView.getSettings();
		ws.setJavaScriptEnabled(true);
		webView.setWebViewClient(browserHandler);

		if(!isServiceRunning()) {
			buttonStart.setText("Stop Tracking");
			startService(new Intent(this, GeoloqiService.class));
		} else {
			buttonStart.setText("Start Tracking");
		}
		buttonStart.setOnClickListener(this);

		new Timer().schedule(new MyTimerTask(), 0, 1000);
	}

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Log.i(TAG, "Binding to Geoloqi service");
        Intent intent = new Intent(this, GeoloqiService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {
        // @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
        	Log.i(TAG, "Service connected: " + service);
        	if(service.getClass() != GeoloqiService.class) {
        		return;
        	}
            // We've bound to GeoloqiService, cast the IBinder and get GeoloqiService instance
        	LocalBinder binder = (LocalBinder)service;
            mService = binder.getService();
            mBound = true;
        }

        // @Override
        public void onServiceDisconnected(ComponentName arg0) {
        	Log.i(TAG, "Service Disconnected");
            mBound = false;
        }
    };

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public void onClick(View src) {
		switch (src.getId()) {
		case R.id.buttonStart:
			if(!isServiceRunning()) {
				logMsg("starting gps\n");
				startService(new Intent(this, GeoloqiService.class));
			} else {
				logMsg("stopping gps\n");
				stopService(new Intent(this, GeoloqiService.class));
			}
			break;
		}
	}
	
	public void logMsg(String msg) {
		textView.setText(msg + textView.getText());
		// scrollView.fullScroll(View.FOCUS_DOWN);
	}
	
	public class LQWebViewClient extends WebViewClient {
		private Boolean isReady = false;
		
		@Override
		public void onPageFinished(WebView view, String url) {
			Log.i(TAG, "browser finished loading");
			isReady = true;
		}
		
		public void sendDataToBrowser(String jsonObject) {
			if(isReady) {
				Log.i(TAG, "sending to browser: " + jsonObject);
				webView.loadUrl("javascript:window.lqReceive(" + jsonObject + ");");
			}
		}
	}

	public class MyTimerTask extends TimerTask {
		private Runnable runnable = new Runnable() {
			public void run() {
				if(isServiceRunning()) {
					buttonStart.setText("Stop Tracking");
				} else {
					buttonStart.setText("Start Tracking");
				}
			}
		};

		@Override
		public void run() {
			handler.post(runnable);
		}
	}

	public boolean isServiceRunning() {
        final ActivityManager activityManager = (ActivityManager)getSystemService(GeoloqiSocketClient.ACTIVITY_SERVICE);
        final List<RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);
	
	    boolean isServiceFound = false;
	
	    for (int i = 0; i < services.size(); i++) {
	        if("com.geoloqi.socket".equals(services.get(i).service.getPackageName())) {
	            if("com.geoloqi.socket.GeoloqiService".equals(services.get(i).service.getClassName())) {
	                isServiceFound = true;
	            }
	        }
	    }
	    return isServiceFound;
	}

}