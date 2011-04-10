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
	static String host = "api.geoloqi.com";
	static int port = 40000;
	static String accessToken = "";
	
	String readFromSocketInputStream(InputStream is)
		throws Exception
	{
		System.out.println("reading from socket");
		byte[] b = new byte[1024];
        int numread = is.read(b, 0, 1024);
        String str = "";
        System.out.println("xnumread: " + numread + " str: " + str);
        while(numread != -1)
        {
        	System.out.println("in while");
        	str += new String(b, 0, numread);
        	numread = is.read(b, 0, 1024);
        }
        System.out.println("str: " + str);
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
            System.out.println("response: " + response);
        	Log.i("geoloqi", response + " from socket");
            
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