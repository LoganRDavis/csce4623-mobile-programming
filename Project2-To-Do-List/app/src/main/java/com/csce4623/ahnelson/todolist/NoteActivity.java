package com.csce4623.ahnelson.todolist;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class NoteActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_activity);
        initializeComponents();
    }
    //Set the OnClick Listener for buttons
    void initializeComponents(){
        findViewById(R.id.backButton).setOnClickListener(this);

        String[] projection = {
                ToDoProvider.TODO_TABLE_COL_ID,
                ToDoProvider.TODO_TABLE_COL_TITLE,
                ToDoProvider.TODO_TABLE_COL_CONTENT};

        Intent homeIntent = getIntent();
        long noteIndex = homeIntent.getLongExtra("noteIndex", -1);
        Log.d("myDebug", String.valueOf(noteIndex));
        Cursor cursor = getContentResolver().query(ToDoProvider.CONTENT_URI,projection,null,null,null);
        cursor.moveToFirst();
        cursor.move((int) noteIndex);
        String title = cursor.getString(cursor.getColumnIndexOrThrow(ToDoProvider.TODO_TABLE_COL_TITLE));
        String content = cursor.getString(cursor.getColumnIndexOrThrow(ToDoProvider.TODO_TABLE_COL_CONTENT));

        TextView titleInput = findViewById(R.id.tvNoteTitle);
        EditText contentInput = findViewById(R.id.etNoteContent);

        titleInput.setText(title);
        contentInput.setText(content);
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            //If new Note, call createNewNote()
            case R.id.backButton:
                finish();
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

