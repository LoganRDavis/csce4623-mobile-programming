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
            //If new Note, call createNewNote()
            case R.id.btnNewNote:
                createNewNote();
                break;
            //If delete note, call deleteNewestNote()
            //case R.id.btnDeleteNote:
            //    deleteNewestNote();
            //    break;
            //This shouldn't happen
            default:
                break;
        }
    }

    void refreshToDoRepeater() {
        String[] projection = {
                ToDoProvider.TODO_TABLE_COL_ID,
                ToDoProvider.TODO_TABLE_COL_TITLE};

        Cursor cursor = getContentResolver().query(ToDoProvider.CONTENT_URI,projection,null,null,null);
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
        myCV.put(ToDoProvider.TODO_TABLE_COL_CONTENT,"Note Content");

        getContentResolver().insert(ToDoProvider.CONTENT_URI,myCV);
        refreshToDoRepeater();
    }

    //Delete the newest note placed into the database
    void deleteNewestNote(){
        //Create the projection for the query
        String[] projection = {
                ToDoProvider.TODO_TABLE_COL_ID,
                ToDoProvider.TODO_TABLE_COL_TITLE,
                ToDoProvider.TODO_TABLE_COL_CONTENT};

        //Perform the query, with ID Descending
        Cursor myCursor = getContentResolver().query(ToDoProvider.CONTENT_URI,projection,null,null,"_ID DESC");
        if(myCursor != null & myCursor.getCount() > 0) {
            //Move the cursor to the beginning
            myCursor.moveToFirst();
            //Get the ID (int) of the newest note (column 0)
            int newestId = myCursor.getInt(0);
            //Delete the note
            int didWork = getContentResolver().delete(Uri.parse(ToDoProvider.CONTENT_URI + "/" + newestId), null, null);
            //If deleted, didWork returns the number of rows deleted (should be 1)
            if (didWork == 1) {
                //If it didWork, then create a Toast Message saying that the note was deleted
                Toast.makeText(getApplicationContext(), "Deleted Note " + newestId, Toast.LENGTH_LONG).show();
            }
        } else{
            Toast.makeText(getApplicationContext(), "No Note to delete!", Toast.LENGTH_LONG).show();

        }
    }

}
