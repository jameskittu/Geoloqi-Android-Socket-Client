package com.geoloqi.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class GeoloqiSocketClient extends Activity {
	static String TAG = "geoloqi.socket";
	static String host = "api.geoloqi.com";
	static int port = 40000;
	
	public InputStream in;
	public TextView textView;

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

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		textView = (TextView)findViewById(R.id.textView1); 
		textView.setText("Connecting...\n");

		try
		{
			Socket s = new Socket(host, port);
			OutputStream out = s.getOutputStream();
			in = s.getInputStream();

			// doing this stuff on the main thread for now, move it later, possibly using the "tag" scheme from the iPhone version
			
			// connect and get the access token prompt
			String response = readFromSocketInputStream(in);

			// got the prompt, send the access token
			out.write(GeoloqiConstants.ACCESS_TOKEN.getBytes());
			out.flush();

			// read the "logged in" prompt
			response = readFromSocketInputStream(in);
			Log.i(TAG, response);
			textView.append(response);

			// Start listening to the socket for data
			Thread readerThread = new Thread(new IncomingReader());
			readerThread.start();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			textView.append("Error: " + e.getMessage() + "\n");
		}

	}
	
	final Handler incomingHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			textView.append((String)msg.obj + "\n");
			super.handleMessage(msg);
		}
	};
	
	public class IncomingReader implements Runnable {
		
		public void run() {
			// TODO Auto-generated method stub
			try {
				while(true) {
					String response = readFromSocketInputStream(in);
					Message msg = incomingHandler.obtainMessage();
					msg.obj = response;
					incomingHandler.sendMessage(msg);
					Log.i(TAG, "Got this in the background: " + response);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}