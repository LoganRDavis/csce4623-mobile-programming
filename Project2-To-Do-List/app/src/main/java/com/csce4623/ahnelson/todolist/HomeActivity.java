package com.csce4623.ahnelson.todolist;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

//Create HomeActivity and implement the OnClick listener
public class HomeActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText editTxt;
    private Button btn;
    private ListView toDoRepeater;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> toDoArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

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

    //Set the OnClick Listener for buttons
    void initializeComponents(){
        findViewById(R.id.btnNewNote).setOnClickListener(this);
        toDoRepeater.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent myIntent = new Intent(HomeActivity.this, NoteActivity.class);
                myIntent.putExtra("noteIndex", id);
                startActivity(myIntent);
            }
        });
        refreshToDoRepeater();
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

    void refreshToDoRepeater() {
        String[] projection = {
                ToDoProvider.TODO_TABLE_COL_ID,
                ToDoProvider.TODO_TABLE_COL_TITLE};

        Cursor cursor = getContentResolver().query(ToDoProvider.CONTENT_URI,projection,null,null,"_ID DESC");
        toDoArrayList.clear();
        try {
            while (cursor.moveToNext()) {
                int index = cursor.getColumnIndexOrThrow(ToDoProvider.TODO_TABLE_COL_TITLE);
                String title = cursor.getString(index);
                toDoArrayList.add(title);
            }
        } finally {
            cursor.close();
        }
        adapter.notifyDataSetChanged();
    }

    //Create a new note with the title "New Note" and content "Note Content"
    void createNewNote(){
        ContentValues myCV = new ContentValues();
        myCV.put(ToDoProvider.TODO_TABLE_COL_TITLE,"New Note");
        myCV.put(ToDoProvider.TODO_TABLE_COL_CONTENT,"");

        getContentResolver().insert(ToDoProvider.CONTENT_URI,myCV);
        refreshToDoRepeater();
    }

}
