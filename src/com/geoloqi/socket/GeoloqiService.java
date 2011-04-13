package com.geoloqi.socket;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class GeoloqiService extends Service implements LocationListener {
	private static final String TAG = "geoloqi.socket.service";
	private static final int NOTIFICATION_ID = 1024;

	static String host = "api.geoloqi.com";
	static int port = 40000;
	public Socket s;
	public OutputStream out;
	public InputStream in;
	private Thread readerThread;

	// Binder given to clients
    private final IBinder mBinder = new LocalBinder();

	Notification notification;
	LocationManager locationManager;
	Date lastPointReceived;
	Date lastPointSent;
	float minDistance = 0.0f;
	long minTime = 1000l;
	Timer sendingTimer;
	private Handler handler = new Handler();
	private int lastBatteryLevel;
	private Location lastLocation;
	BatteryReceiver batteryReceiver;
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        GeoloqiService getService() {
            // Return this instance of GeoloqiService so clients can call public methods
            return GeoloqiService.this;
        }
    }

	@Override
	public void onCreate() {
		// Toast.makeText(this, "Geoloqi Tracker Created", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onCreate");
		
		try
		{
			s = new Socket(host, port);
			out = s.getOutputStream();
			in = s.getInputStream();

			// doing this stuff on the main thread for now, move it later, possibly using the "tag" scheme from the iPhone version
			
			// connect and get the access token prompt
			String response = readFromSocketInputStream(in);

			// got the prompt, send the access token
			out.write(GeoloqiConstants.ACCESS_TOKEN.getBytes());
			out.flush();

			// read the "logged in" prompt
			response = readFromSocketInputStream(in);
			logMsg(response);

			// Start listening to the socket for data
			readerThread = new Thread(new IncomingReader());
			readerThread.start();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			logMsg("Error: " + e.getMessage() + "\n");
		}

		
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		
		batteryReceiver = new BatteryReceiver();
		registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	}
	
	public void logMsg(String msg) {
		Log.i(TAG, "LOG MESSAGE: " + msg);
		// scrollView.fullScroll(View.FOCUS_DOWN);
	}
	
	@Override
	public void onDestroy() {
		Toast.makeText(this, "Geoloqi Tracker Stopped", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onDestroy");
		
		locationManager.removeUpdates(this);
		sendingTimer.cancel();
		
		NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(GeoloqiService.NOTIFICATION_ID);

		try {
			s.shutdownInput();
			s.shutdownOutput();
			s.close();
			readerThread.interrupt();
			Log.i(TAG, "Closing socket");
		} catch(Exception e) {
			logMsg("Error Closing Socket: " + e.getMessage() + "\n");
		}
	}
	
	@Override
	public void onStart(Intent intent, int startid) {
		Toast.makeText(this, "Geoloqi Tracker Started", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onStart");
		
		// String bestProvider = locationManager.getBestProvider(new Criteria(), true);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, this);
	}

	public void onLocationChanged(Location location) {
		Log.d(TAG, location.toString());

		if(lastPointReceived == null || lastPointReceived.getTime() < System.currentTimeMillis() - 500)
		{
			// Ignore points worse than 600m accurate (super rough position appears to be about 1000m)
			if(location.hasAccuracy() && location.getAccuracy() > 600)
				return;
			
			// Ignore duplicate points
			if(lastLocation != null
				&& location.getLatitude() == lastLocation.getLatitude()
				&& location.getLongitude() == lastLocation.getLongitude()
				&& location.getAccuracy() == lastLocation.getAccuracy()
				&& location.getSpeed() == lastLocation.getSpeed()) {
				Log.i(TAG, "Ignoring duplicate point");
				return;
			}
			
			lastLocation = location;
			lastPointReceived = new Date();

			byte[] update = new byte[24];
			update[0] = 0;
			update[1] = 65;

			int datestamp = (int)(System.currentTimeMillis() / 1000);
			update[2] = (byte)((byte)(datestamp >> 24) & 0xFF);
			update[3] = (byte)((byte)(datestamp >> 16) & 0xFF);
			update[4] = (byte)((byte)(datestamp >> 8) & 0xFF);
			update[5] = (byte)((byte)(datestamp) & 0xFF);

			int lat1 = (int)(location.getLatitude()) + 90;
			update[6] = (byte)((byte)(lat1 >> 8) & 0xFF);
			update[7] = (byte)((byte)(lat1) & 0xFF);

			double lat = Math.abs(location.getLatitude());
			lat = lat - (int)lat;
			int lat2 = (int)(lat * 1000000);
			update[8] = (byte)((byte)(lat2 >> 24) & 0xFF);
			update[9] = (byte)((byte)(lat2 >> 16) & 0xFF);
			update[10] = (byte)((byte)(lat2 >> 8) & 0xFF);
			update[11] = (byte)((byte)(lat2) & 0xFF);

			int lng1 = (int)(location.getLongitude()) + 180;
			update[12] = (byte)((byte)(lng1 >> 8) & 0xFF);
			update[13] = (byte)((byte)(lng1) & 0xFF);

			double lng = Math.abs(location.getLongitude());
			lng = lng - (int)lng;
			int lng2 = (int)(lng * 1000000);
			update[14] = (byte)((byte)(lng2 >> 24) & 0xFF);
			update[15] = (byte)((byte)(lng2 >> 16) & 0xFF);
			update[16] = (byte)((byte)(lng2 >> 8) & 0xFF);
			update[17] = (byte)((byte)(lng2) & 0xFF);

			Log.i(TAG, "Latitude: " + location.getLatitude());
			Log.i(TAG, "Sending: lat/lng: " + lat1 + "." + lat2 + " " + lng1 + "." + lng2);
			
			int spd = (int)(location.getSpeed() * 3.6);
			update[18] = (byte)((byte)(spd >> 8) & 0xFF);
			update[19] = (byte)((byte)(spd) & 0xFF);
			
			int acc = (int)(location.getAccuracy());
			update[20] = (byte)((byte)(acc >> 8) & 0xFF);
			update[21] = (byte)((byte)(acc) & 0xFF);

			int bat = lastBatteryLevel;
			update[22] = (byte)((byte)(bat >> 8) & 0xFF);
			update[23] = (byte)((byte)(bat) & 0xFF);
			
			try {
				out.write(update);
				out.flush();
			} catch(Exception e) {
				Log.w(TAG, "Error writing to output buffer!");
			}
		}
	}
	
	final Handler incomingHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			logMsg((String)msg.obj + "\n");
			super.handleMessage(msg);
		}
	};

	public class IncomingReader implements Runnable {
		public void run() {
			try {
				while(true) {
					String response = readFromSocketInputStream(in);
					Message msg = incomingHandler.obtainMessage();
					msg.obj = response;
					incomingHandler.sendMessage(msg);
					// browserHandler.sendDataToBrowser(response);
					Log.i(TAG, "Got this in the background: " + response);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
	}

	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub		
	}
	
	private class BatteryReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			lastBatteryLevel = intent.getIntExtra("level", 0);
		}
	};
	
	String readFromSocketInputStream(InputStream is) throws Exception
	{
		// Create a byte array to store the number of bytes coming up
		byte[] b = new byte[4];
		Log.i(TAG, "waiting for message length packet from stream");
		int numread = is.read(b, 0, 4);
		if(numread != 4) {
			throw new Exception("Wrong number of bytes were read: " + numread + " expecting 4");
		}
		int numBytes = ((int)(b[3] & 0xFF) << 24) 
					  | ((int)(b[2] & 0xFF) << 16) 
					  | ((int)(b[1] & 0xFF) << 8) 
					  | (int)b[0] & 0xFF;

		byte[] buffer = new byte[numBytes];  // TODO: How do we handle reading large chunks of data?
		Log.i(TAG, "attempting to read " + numBytes + " bytes from socket");
		numread = is.read(buffer, 0, numBytes);
		
		if(numread != numBytes) {
			throw new Exception("Wrong number of bytes were read: " + numread + "expecting " + numBytes);
		}
		
		String str = new String(buffer, 0, numBytes);
		Log.i(TAG, "read message: '" + str + "'");
		return str;
	}


}
