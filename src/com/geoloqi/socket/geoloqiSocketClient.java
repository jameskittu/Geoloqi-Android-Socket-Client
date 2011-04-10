package com.geoloqi.socket;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class geoloqiSocketClient extends Activity {
	static String TAG = "geoloqi.socket";
	static String host = "api.geoloqi.com";
	static int port = 40000;
	static String accessToken = "";

	String readFromSocketInputStream(InputStream is) throws Exception
	{
		// Create a byte array to store the number of bytes coming up
		byte[] b = new byte[4];
		Log.i(TAG, "waiting for input from stream");
		int numread = is.read(b, 0, 4);
		if(numread != 4) {
			throw new Exception("Wrong number of bytes were read: " + numread + " expecting 4");
		}
		int numBytes = ((int)(b[3] & 0xFF) << 24) 
					  | ((int)(b[2] & 0xFF) << 16) 
					  | ((int)(b[1] & 0xFF) << 8) 
					  | (int)b[0] & 0xFF;

		byte[] buffer = new byte[numBytes];  // TODO: How to do this properly? Maybe we need a buffered reader?
		Log.i(TAG, "attempting to read " + numBytes + " from socket");
		numread = is.read(buffer, 0, numBytes);
		
		if(numread != numBytes) {
			throw new Exception("Wrong number of bytes were read: " + numread + "expecting " + numBytes);
		}
		
		String str = new String(buffer, 0, numBytes);
		return str;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		TextView text = (TextView) findViewById(R.id.textView1); 
		text.setText("Android yay.");

		try
		{
			Socket s = new Socket(host, port);
			OutputStream out = s.getOutputStream();
			InputStream in = s.getInputStream();
			
			// connect and get the access token prompt
			String response = readFromSocketInputStream(in);

			// got the prompt, send the access token
			out.write(accessToken.getBytes());
			out.flush();

			// read the "logged in" prompt
			response = readFromSocketInputStream(in);
			Log.i(TAG, response);
			
			// Wait for more data from the socket and output it
			while(true) {
				response = readFromSocketInputStream(in);
				text.setText(response);
				Log.i(TAG, response);
			}

		}
		catch(Exception e)
		{
			e.printStackTrace();
			text.setText("Error: " + e.getMessage());
		}

	}
}