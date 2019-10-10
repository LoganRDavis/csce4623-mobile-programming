package com.csce4623.ahnelson.todolist;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener{
    private ListView toDoRepeater;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> toDoArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

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

    void createNewNote(){
        ContentValues myCV = new ContentValues();
        myCV.put(ToDoProvider.TODO_TABLE_COL_TITLE, "New Note");
        myCV.put(ToDoProvider.TODO_TABLE_COL_CONTENT,"");

        getContentResolver().insert(ToDoProvider.CONTENT_URI,myCV);
        Intent myIntent = new Intent(HomeActivity.this, NoteActivity.class);
        myIntent.putExtra("noteIndex", 0);
        startActivity(myIntent);
    }

}
