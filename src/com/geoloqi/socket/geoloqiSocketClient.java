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
import android.os.RemoteException;
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
import android.widget.Toast;

public class GeoloqiSocketClient extends Activity implements OnClickListener {
	static String TAG = "geoloqi.socket";
	
	IGeoloqiService mService = null;
    boolean mIsBound = false;

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

//		if(!isServiceRunning()) {
//			buttonStart.setText("Stop Tracking");
//			startService(new Intent(this, GeoloqiService.class));
//		} else {
//			buttonStart.setText("Start Tracking");
//		}
		buttonStart.setOnClickListener(this);

//		new Timer().schedule(new MyTimerTask(), 0, 1000);
	}

//    @Override
//    protected void onStart() {
//        super.onStart();
//        // Bind to LocalService
//        Log.i(TAG, "Binding to Geoloqi service");
//        Intent intent = new Intent(this, GeoloqiService.class);
//        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        // Unbind from the service
//        if (mBound) {
//            unbindService(mConnection);
//            mBound = false;
//        }
//    }

	 /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            mService = IGeoloqiService.Stub.asInterface(service);
            logMsg("Attached to service.");

            // We want to monitor the service for as long as we are
            // connected to it.
            try {
                mService.registerCallback(mCallback);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }

            // As part of the sample, tell the user what happened.
            Toast.makeText(GeoloqiSocketClient.this, "Remote service connected", Toast.LENGTH_SHORT).show();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            logMsg("Disconnected from service.");

            // As part of the sample, tell the user what happened.
            Toast.makeText(GeoloqiSocketClient.this, "Remove service disconnected", Toast.LENGTH_SHORT).show();
        }
    };

	public void onClick(View src) {
		switch (src.getId()) {
		case R.id.buttonStart:
			if(!isServiceRunning()) {
				logMsg("Binding to service\n");
				bindService(new Intent(IGeoloqiService.class.getName()), mConnection, Context.BIND_AUTO_CREATE);
			} else {
				logMsg("stopping gps\n");
//				stopService(new Intent(this, GeoloqiService.class));
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

    // ----------------------------------------------------------------------
    // Code showing how to deal with callbacks.
    // ----------------------------------------------------------------------

    /**
     * This implementation is used to receive callbacks from the remote
     * service.
     */
    private IGeoloqiServiceCallback mCallback = new IGeoloqiServiceCallback.Stub() {
        /**
         * This is called by the remote service regularly to tell us about
         * new values.  Note that IPC calls are dispatched through a thread
         * pool running in each process, so the code executing here will
         * NOT be running in our main thread like most other things -- so,
         * to update the UI, we need to use a Handler to hop over there.
         */
        public void messageReceived(int value) {
            mHandler.sendMessage(mHandler.obtainMessage(BUMP_MSG, value, 0));
        }
        public void locationUpdated(int value) {
            mHandler.sendMessage(mHandler.obtainMessage(BUMP_MSG, value, 0));
        }
    };

    private static final int BUMP_MSG = 1;

    private Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            switch (msg.what) {
                case BUMP_MSG:
                    logMsg("Received from service: " + msg.arg1);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }

    };	
	
}