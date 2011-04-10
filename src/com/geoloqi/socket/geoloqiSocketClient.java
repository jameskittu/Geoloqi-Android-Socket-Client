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

//TextView text = (TextView) findViewById(R.id.textView1); 
//text.setText("Android yay.");


public class geoloqiSocketClient extends Activity {
	static String TAG = "geoloqi.socket";
	static String host = "api.geoloqi.com";
	static int port = 40000;
	static String accessToken = "";

	String readFromSocketInputStream(InputStream is) throws Exception
	{

		// Create a byte array to store the number of bytes coming up
		byte[] b = new byte[4];
		Log.i(TAG, "reading from socket");
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
		Log.i(TAG, "read buffer into a string: " + str);
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
			OutputStream os = s.getOutputStream();
			InputStream in = s.getInputStream();
			//connect and get the access token prompt
			String response = readFromSocketInputStream(in);
			Log.i(TAG, "'" + response + "' from socket");

			//got the prompt, send the access token
			os.write(accessToken.getBytes());
			os.flush();



		}
		catch(Exception e)
		{
			e.printStackTrace();
			text.setText("Error: " + e.getMessage());
		}


	}
}