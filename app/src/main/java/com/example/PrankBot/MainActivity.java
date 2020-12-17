package com.example.PrankBot;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.TextView;
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Dharmik";
    String PHONENUMBER;
    Handler handler = new Handler(Looper.getMainLooper());
    SMSReciver smsReciver;
    SmsManager smsManager;
    TextView stageOnScreen;
    boolean permissonGranted;
    private static final int RQ_READSMS = 1;
    private static final int RQ_SENDSMS = 2;
    private static final int RQ_RECIVESMS = 3;
    String[][] cbInputs = {
            {"how"},
            {"hi","yo ","hello","hola","ola","howdy","hey","helo","holla","morning","evening","afternoon", "hye"},
            {"what", "wath", "wat", "whta"},

            {},
            {"omg", "what", "wtf", "kill", "way", "shit","no way","mad","angry"},
            {"ok","idc", "i dont care","i do not care", "anymore","not"},
            {"lie", "prank", "liar", "bluff", "lying","do not","dont", "believe", "did not","belive"},
            /*ROW: 7*/{"lie", "prank", "liar", "bluff", "lying","do not","dont", "believe", "did not"},
            {"alright", "believe", "honest", "thank you", "thanks","guess","trust"},

            {},
            {"yes","ya","ye", "what", "good","maybe","go","start","ahead","okay","tell","sure"},
            {"no","not","hell","you","bad"}
    };
    String[][] cbOutput = {
            {"good","doing well","kool as a cat","i am well","well","couldnt be better"},
            {"hi","hello","hola","ola","howdy","hey","helo","holla"},
            {"nothing much is going on, either way I am well"},

            {"im sorry, but ur car is broken", "im sorry but i broke ur laptop screen"},
            {"dont be mad", "chill", "relax"},
            {"arent you mad", "wow you are very calm, arent u mad?"},
            {"why would i lie to you about something this important", "are u sereouse, i would not lie to u when the thing is that expensive"},

            /*ROW: 7*/{"call up mom rn, she was next to me"},
            {},

            {"are u ready to here something","i wanna tell u something", "listen up"},
            {"twas a prank", "i was joking, get pranked sucker","oye its a prank"},
            {"i think u will like it", "please let me", "trsut me", "u will want to hear this"},
    };
    String[][] cbDefaultOutputs = {
            {"I dont understand", "?"},
            {"come again please"},
            {"please reiterate"},
            {"do you want me to tell you, yes or no?"}
    };
    int[][] rowToCheck = {
            {0, 2},
            {3, 6},
            {7, 8},
            {9, 11}

    };
    int currentState = 0;
    boolean moveToNextStage = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        stageOnScreen = findViewById(R.id.id_stage);
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED  || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECEIVE_SMS}, RQ_RECIVESMS);
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS}, RQ_SENDSMS);
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_SMS}, RQ_READSMS);
        }
        smsManager = SmsManager.getDefault();
    }
    @Override
    protected void onResume() {
        super.onResume();
        smsReciver = new SMSReciver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(smsReciver, intentFilter);
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(smsReciver);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == RQ_RECIVESMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissonGranted = true;
            } else {
                permissonGranted = false;
            }
        }
        else if (requestCode == RQ_SENDSMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissonGranted = true;
            } else {
                permissonGranted = false;
            }
        }
        else if (requestCode == RQ_READSMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissonGranted = true;
            } else {
                permissonGranted = false;
            }
        }
    }
    public class SMSReciver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            Object[] pdus = (Object[]) bundle.get("pdus");
            SmsMessage[] smsMessages = new SmsMessage[pdus.length];
            String message = "";
            for (int x = 0; x < pdus.length; x++){
                byte[] currentByte = (byte[]) pdus[x];
                smsMessages[x] = SmsMessage.createFromPdu(currentByte, bundle.getString("format"));
                message = smsMessages[x].getMessageBody();
                PHONENUMBER = smsMessages[x].getOriginatingAddress();
            }
            message = message.toLowerCase();

            int inArrayResponse = inArray(currentState, message);
            String mesToSend = getMesToSend(currentState, inArrayResponse);
            new SMSThread(mesToSend, 2000);
            moveToNextStage();
            Log.d(TAG, "onReceive: "+inArrayResponse+"    "+ currentState);
        }
    }
    public int inArray(int currentState, String mes){
        int i = -1;
        for (int row = rowToCheck[currentState][0]; row <= rowToCheck[currentState][1]; row++){
            for(int col = 0; col < cbInputs[row].length; col++){
                String input = cbInputs[row][col];
                if (mes.contains(input)){
                    i = row;
                }
            }
        }
        return i;
    }
    public String getMesToSend(int currentState, int inArrayResponse){
        if(currentState == 0) {
            if (inArrayResponse == 1) {
                moveToNextStage = false;
                return cbOutput[inArrayResponse][(int) (Math.random() * cbOutput[inArrayResponse].length)];
            }
            else if(inArrayResponse == 0 || inArrayResponse == 2) {
                moveToNextStage = true;
                return cbOutput[inArrayResponse][(int) (Math.random() * cbOutput[inArrayResponse].length)];
            }
        }
        else if (currentState == 1){
            if (inArrayResponse >= 0 && inArrayResponse != 6) {
                moveToNextStage = false;
                return cbOutput[inArrayResponse][(int) (Math.random() * cbOutput[inArrayResponse].length)];
            }
            else if (inArrayResponse == 6){
                moveToNextStage = true;
                return cbOutput[inArrayResponse][(int) (Math.random() * cbOutput[inArrayResponse].length)];
            }
            else if (inArrayResponse == -2) {
                moveToNextStage = false;
                return cbOutput[3][(int) (Math.random() * cbOutput[3].length)];
            }
        }
        else if (currentState == 2){
            if (inArrayResponse >= 0 && inArrayResponse !=8){
                moveToNextStage = false;
                return cbOutput[inArrayResponse][(int) (Math.random() * cbOutput[inArrayResponse].length)];
            }
            else if (inArrayResponse == 8){
                moveToNextStage = true;
                return cbOutput[9][(int) (Math.random() * cbOutput[9].length)];
            }
            else if(inArrayResponse == -2){
                moveToNextStage = false;
                return cbOutput[7][(int) (Math.random() * cbOutput[7].length)];
            }
        }
        else if(currentState == 3){
            if(inArrayResponse >= 0 && inArrayResponse!= 10){
                moveToNextStage = false;
                return cbOutput[inArrayResponse][(int) (Math.random() * cbOutput[inArrayResponse].length)];
            }
            else if(inArrayResponse == 10){
                moveToNextStage = true;
                return cbOutput[10][(int) (Math.random() * cbOutput[10].length)];
            }
        }
        return cbDefaultOutputs[currentState][(int) (Math.random() * cbDefaultOutputs[currentState].length)];
    }
    public void moveToNextStage(){
        if (currentState == 0 && moveToNextStage){
            currentState++;
            stageOnScreen.setText("Stage 2: Prank Started");
            new SMSThread(getMesToSend(currentState, -2), 3000);
        }
        else if (currentState == 1 && moveToNextStage){
            stageOnScreen.setText("Stage 3: Prank Detected");
            currentState++;
        }
        else if (currentState == 2 && moveToNextStage){
            stageOnScreen.setText("Stage 4: Revealing the Prank");
            currentState++;
        }
        else if (currentState == 3 && moveToNextStage){
            stageOnScreen.setText("Prank Completed!");
        }
        moveToNextStage = false;

    }
    public class SMSThread extends Thread{
        public SMSThread(String message, int delay){
            handler.postDelayed(getRunnable(message), delay);
        }
        public synchronized Runnable getRunnable(final String message){
            return new Runnable() {
                @Override
                public void run() {
                    smsManager.sendTextMessage(PHONENUMBER, null, message, null, null);
                }
            };
        }
    }
}
