package com.csce4623.ahnelson.todolist;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TimePicker;
import android.graphics.Color;

import java.util.Calendar;

public class NoteActivity extends AppCompatActivity implements View.OnClickListener{

    private long noteIndex;
    TimePickerDialog.OnTimeSetListener mOnTimeSetListener;
    DatePickerDialog.OnDateSetListener mOnDateSetListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_activity);
        initializeComponents();
    }

    //Set the OnClick Listener for buttons
    void initializeComponents(){
        findViewById(R.id.backButton).setOnClickListener(this);
        findViewById(R.id.btnDelete).setOnClickListener(this);
        findViewById(R.id.btnSave).setOnClickListener(this);
        findViewById(R.id.changeTimeButton).setOnClickListener(this);
        findViewById(R.id.changeDateButton).setOnClickListener(this);

        loadThisNote();
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.backButton:
                finish();
                break;
            case R.id.btnSave:
                saveThisNote();
                break;
            case R.id.btnDelete:
                deleteThisNote();
                break;
            case R.id.changeTimeButton:
                showTimePicker();
                break;
            case R.id.changeDateButton:
                showDatePicker();
                break;
            default:
                break;
        }
    }

    void loadThisNote() {
        String[] projection = {
                ToDoProvider.TODO_TABLE_COL_ID,
                ToDoProvider.TODO_TABLE_COL_TITLE,
                ToDoProvider.TODO_TABLE_COL_CONTENT,
                ToDoProvider.TODO_TABLE_COL_DONE};

        Intent homeIntent = getIntent();
        noteIndex = homeIntent.getLongExtra("noteIndex", -1);
        Log.d("myDebug", String.valueOf(noteIndex));
        Cursor cursor = getContentResolver().query(ToDoProvider.CONTENT_URI,projection,null,null,"_ID DESC");
        cursor.moveToFirst();
        cursor.move((int) noteIndex);
        String title = cursor.getString(cursor.getColumnIndexOrThrow(ToDoProvider.TODO_TABLE_COL_TITLE));
        String content = cursor.getString(cursor.getColumnIndexOrThrow(ToDoProvider.TODO_TABLE_COL_CONTENT));
        int done = cursor.getInt(cursor.getColumnIndexOrThrow(ToDoProvider.TODO_TABLE_COL_DONE));

        EditText titleInput = findViewById(R.id.tvNoteTitle);
        EditText contentInput = findViewById(R.id.etNoteContent);
        CheckBox doneInput = findViewById((R.id.doneCheckBox));

        titleInput.setText(title);
        contentInput.setText(content);
        doneInput.setChecked(done != 0);
    }

    void saveThisNote(){
        String[] projection = {
                ToDoProvider.TODO_TABLE_COL_ID};

        Cursor cursor = getContentResolver().query(ToDoProvider.CONTENT_URI,projection,null,null,"_ID DESC");
        if(cursor != null & cursor.getCount() > 0) {
            cursor.moveToFirst();
            cursor.move((int) noteIndex);
            int noteId = cursor.getInt(0);

            EditText titleInput = findViewById(R.id.tvNoteTitle);
            EditText contentInput = findViewById(R.id.etNoteContent);
            CheckBox doneInput = findViewById((R.id.doneCheckBox));

            ContentValues myCV = new ContentValues();
            myCV.put(ToDoProvider.TODO_TABLE_COL_ID, noteId);
            myCV.put(ToDoProvider.TODO_TABLE_COL_TITLE, String.valueOf(titleInput.getText()));
            myCV.put(ToDoProvider.TODO_TABLE_COL_CONTENT, String.valueOf(contentInput.getText()));
            myCV.put(ToDoProvider.TODO_TABLE_COL_DONE, doneInput.isChecked() ? 1 : 0);

            int didWork = getContentResolver().update(Uri.parse(ToDoProvider.CONTENT_URI + "/" + noteId), myCV, null, null);
            if (didWork == 1) {
                finish();
            }
        } else{
            Toast.makeText(getApplicationContext(), "Error Saving Note", Toast.LENGTH_LONG).show();
        }
    }

    void deleteThisNote(){
        String[] projection = {
                ToDoProvider.TODO_TABLE_COL_ID};

        Cursor cursor = getContentResolver().query(ToDoProvider.CONTENT_URI,projection,null,null,"_ID DESC");
        if(cursor != null & cursor.getCount() > 0) {
            cursor.moveToFirst();
            cursor.move((int) noteIndex);
            int newestId = cursor.getInt(0);
            int didWork = getContentResolver().delete(Uri.parse(ToDoProvider.CONTENT_URI + "/" + newestId), null, null);
            if (didWork == 1) {
                finish();
            }
        } else{
            Toast.makeText(getApplicationContext(), "Error Deleting Note", Toast.LENGTH_LONG).show();
        }
    }

    void showTimePicker() {
        Calendar mCalendar =  Calendar.getInstance();
        int hour = mCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = mCalendar.get(Calendar.MINUTE);

        TimePickerDialog mTimePickerDialog = new TimePickerDialog(
                NoteActivity.this,
                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                mOnTimeSetListener,
                hour,minute,true);

        mTimePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mTimePickerDialog.show();

        mOnTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourofday, int minute) {
                String mTime = hourofday+":"+minute;
                Button mTimePicker = findViewById(R.id.changeTimeButton);
                mTimePicker.setText(mTime);
            }
        };
    }

    void showDatePicker() {
        Calendar mCalendar =  Calendar.getInstance();
        int year = mCalendar.get(Calendar.YEAR);
        int month = mCalendar.get(Calendar.MONTH);
        int day = mCalendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog mDatePickerDialog = new DatePickerDialog(
                NoteActivity.this,
                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                mOnDateSetListener,
                year,month,day);
        mDatePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDatePickerDialog.show();

        mOnDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
             public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month+1;
                String mDate = dayOfMonth+"/"+month+"/"+year;
                Button changeDateButton = findViewById(R.id.changeDateButton);
                changeDateButton.setText(mDate);
            }
        };
    }
}

