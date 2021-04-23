package com.example.qaclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class CommunicateActivity extends AppCompatActivity {

    private Socket socket = null;
    private PrintWriter out = null;
    private BufferedReader in = null;
    private TextView tv;

    // In onCreate, connect to the server, and then wait for the
    // user to input the question and press the button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communicate);

        int port;
        String hostname;

        // Get the hostname from the intent

        Intent intent = getIntent();
        hostname = intent.getStringExtra(MainActivity.HOST_NAME);

        // Get the port from the intent.  Default port is 4000

        port = intent.getIntExtra(MainActivity.PORT, 4000);

        // get a handle on the TextView for displaying the status

        tv = (TextView) findViewById(R.id.text_answer);

        // Try to open the connection to the server

        try {
            tv.setTextColor(Color.GREEN);
            tv.setText("Connected to " + hostname + ":" + port);

            socket = new Socket(hostname, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            tv.setText("Connected to " + hostname + ":" + port);
        } catch (IOException e) // socket problems
        {
            tv.setText("Problem: " + e.toString());
            tv.setTextColor(Color.RED);
            socket = null;
        }

    } // end onCreate

    public void sendQuestion(View view) {

        EditText et;
        String user_question;
        String answer;
        boolean finished = false;

        // are we connected?

        if (socket == null) {
            tv.setTextColor(Color.RED);
            tv.setText("Not connected.");
        } else {
            tv.setTextColor(Color.RED);
            String num1 = ((EditText) findViewById(R.id.num1)).getText().toString();
            String num2 = ((EditText) findViewById(R.id.num2)).getText().toString();
            et = ((EditText) findViewById(R.id.edit_question));
            user_question = et.getText().toString();

            if (num1.length() == 0) {
                tv.setText("Enter 1st number");
                return;
            } else if (num2.length() == 0) {
                tv.setText("Enter 2nd number");
                return;
            } else if (user_question.length() == 0) {
                tv.setText("Enter question");
                return;
            }

            tv.setTextColor(Color.DKGRAY);
            tv.setText("");

            if (user_question.toLowerCase().equals("quit")) {
                finished = true;
                out.println("quit");
            } else {
                try {
                    PrintWriter out = new PrintWriter(socket.getOutputStream());
                    out.println(user_question);
                    out.println(num1);
                    out.println(num2);
                    out.flush();
                    InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
                    BufferedReader bfReader = new BufferedReader(streamReader);
                    tv.setText(bfReader.readLine());
                    finished = true;
                } catch (IOException e) {
                    Log.d("ddbg", e.toString());
                }
            }

            // get the question to send to the server (place it in "user_question")

            // if the (input) question is "quit", we're finished; let
            // the server know by sending it "quit".  Also, don't forget
            // to "raise the flag" locally.  Otherwise, just send the
            // question and get a response


            // if we're finished, close the connection

            if (finished) {
                try {
                    out.close();
                    in.close();
                    socket.close();

                    // set socket back to null to indicate that we're disconnected

                    socket = null;

                    tv.setText(tv.getText() + "\n\nFinished.  Connection closed.");
                } catch (IOException e)  // socket problems
                {
                    tv.setTextColor(Color.RED);
                    tv.setText("Problem: " + e.toString());
                }

            }

        }

    } // end sendQuestion

} // end CommunicateActivity
