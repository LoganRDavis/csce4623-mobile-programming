package com.csce4623.ahnelson.todolist;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener{
    private ListView toDoRepeater;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> toDoArrayList;
    private NetworkChangeReceiver networkChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        createNotificationChannel();
        updateFromBackup();
        listenForNetworkChanges();

        Intent reminderIntent = getIntent();
        int noteId = reminderIntent.getIntExtra("noteId", -1);
        if (noteId >= 0) {
            loadFromReminder(noteId);
        }

        toDoRepeater = findViewById(R.id.toDoRepeater);
        toDoArrayList = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, toDoArrayList);
        toDoRepeater.setAdapter(adapter);

        initializeComponents();
    }

    @Override
    public void onResume(){
        super.onResume();
        refreshToDoRepeater();
    }

    void initializeComponents(){
        findViewById(R.id.btnNewNote).setOnClickListener(this);
        toDoRepeater.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent myIntent = new Intent(HomeActivity.this, NoteActivity.class);
                myIntent.putExtra("noteIndex", (int) id);
                startActivity(myIntent);
            }
        });
        refreshToDoRepeater();
    }

    //loads into note from a notification
    void loadFromReminder(int noteId) {
        String[] projection = { ToDoProvider.TODO_TABLE_COL_ID};
        Cursor cursor = getContentResolver().query(ToDoProvider.CONTENT_URI,projection,null,null,"_ID DESC");
        try {
            int noteIndex = -1;
            while (cursor.moveToNext()) {
                noteIndex += 1;
                int id = cursor.getInt(0);
                if (id == noteId) {
                    Intent myIntent = new Intent(HomeActivity.this, NoteActivity.class);
                    myIntent.putExtra("noteIndex", noteIndex);
                    startActivity(myIntent);
                }
            }
        } finally {
            cursor.close();
        }
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.btnNewNote:
                createNewNote();
                break;
            default:
                break;
        }
    }

    //Loads in toDoRepeater list view from db
    void refreshToDoRepeater() {
        String[] projection = {
                ToDoProvider.TODO_TABLE_COL_ID,
                ToDoProvider.TODO_TABLE_COL_TITLE,
                ToDoProvider.TODO_TABLE_COL_DONE};

        Cursor cursor = getContentResolver().query(ToDoProvider.CONTENT_URI,projection,null,null,"_ID DESC");
        toDoArrayList.clear();
        try {
            while (cursor.moveToNext()) {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(ToDoProvider.TODO_TABLE_COL_TITLE));
                int done = cursor.getInt(cursor.getColumnIndexOrThrow(ToDoProvider.TODO_TABLE_COL_DONE));
                if (done != 0) {
                    title = "(DONE) " + title;
                }
                toDoArrayList.add(title);
            }
        } finally {
            cursor.close();
        }
        adapter.notifyDataSetChanged();
    }

    //Creates a note to then be loaded for editing
    void createNewNote(){
        ContentValues myCV = new ContentValues();
        myCV.put(ToDoProvider.TODO_TABLE_COL_TITLE, "New Note");
        myCV.put(ToDoProvider.TODO_TABLE_COL_CONTENT,"");

        getContentResolver().insert(ToDoProvider.CONTENT_URI,myCV);
        Intent myIntent = new Intent(HomeActivity.this, NoteActivity.class);
        myIntent.putExtra("noteIndex", 0);
        startActivity(myIntent);
    }

    //Creates default notification channel if not created
    void createNotificationChannel() {
        NotificationManager mNotificationManager = (NotificationManager) (HomeActivity.this).getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel mChannel = new NotificationChannel("my_channel_01", "TEST", NotificationManager.IMPORTANCE_HIGH);
        mChannel.setDescription("This is a test.");
        mChannel.enableLights(true);
        mChannel.setLightColor(Color.RED);
        mChannel.enableVibration(true);
        mNotificationManager.createNotificationChannel(mChannel);
    }

    //Listens for network changes and updates user on disconnect
    void listenForNetworkChanges() {
        networkChangeReceiver = new NetworkChangeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.registerReceiver(networkChangeReceiver, filter);
    }

    //If data has been saved to backup file, updates content provider
    void updateFromBackup(){
        //Fetches the file and creates a string
        File file = new File(this.getFilesDir(), "backupData");
        FileInputStream fin = null;
        String jsonFileAsString = "";
        try {
            fin = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            jsonFileAsString = sb.toString();
            fin.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //If content in file, converts string to JSON and uploads to content provider
        if (jsonFileAsString.length() > 1) {
            try {
                jsonFileAsString = jsonFileAsString.trim();
                jsonFileAsString = jsonFileAsString.substring(0, jsonFileAsString.length() - 1);
                jsonFileAsString = "{" + jsonFileAsString + "}";
                JSONObject backupNotes = new JSONObject(jsonFileAsString);

                Iterator<String> keys = backupNotes.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    if (backupNotes.get(key) instanceof JSONObject) {
                        JSONObject note = backupNotes.getJSONObject(key);
                        ContentValues myCV = new ContentValues();
                        myCV.put(ToDoProvider.TODO_TABLE_COL_ID, note.getInt(ToDoProvider.TODO_TABLE_COL_ID));
                        myCV.put(ToDoProvider.TODO_TABLE_COL_TITLE, note.getString(ToDoProvider.TODO_TABLE_COL_TITLE));
                        myCV.put(ToDoProvider.TODO_TABLE_COL_CONTENT, note.getString(ToDoProvider.TODO_TABLE_COL_CONTENT));
                        myCV.put(ToDoProvider.TODO_TABLE_COL_DONE, note.getInt(ToDoProvider.TODO_TABLE_COL_DONE));
                        myCV.put(ToDoProvider.TODO_TABLE_COL_DATE, note.getLong(ToDoProvider.TODO_TABLE_COL_DATE));

                        getContentResolver().update(Uri.parse(ToDoProvider.CONTENT_URI + "/" + note.getInt(ToDoProvider.TODO_TABLE_COL_ID)),
                                myCV, null, null);
                    }
                }
            } catch (JSONException e) {
                Log.d("myDebug", "ERROR: " + e);
                e.printStackTrace();
            }
            try (FileWriter fileWriter = new FileWriter(file, false)) {
                fileWriter.write("");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(networkChangeReceiver);
    }
}
