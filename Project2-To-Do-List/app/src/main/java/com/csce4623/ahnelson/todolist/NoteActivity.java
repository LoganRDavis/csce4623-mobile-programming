package com.csce4623.ahnelson.todolist;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class NoteActivity extends AppCompatActivity implements View.OnClickListener{

    private long noteIndex;
    TimePickerDialog.OnTimeSetListener mOnTimeSetListener;
    DatePickerDialog.OnDateSetListener mOnDateSetListener;
    int dueYear, dueMonth, dueDayOfMonth, dueHrs, dueMin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_activity);
        initializeComponents();
    }

    void initializeComponents(){
        findViewById(R.id.backButton).setOnClickListener(this);
        findViewById(R.id.btnDelete).setOnClickListener(this);
        findViewById(R.id.btnSave).setOnClickListener(this);
        findViewById(R.id.changeTimeButton).setOnClickListener(this);
        findViewById(R.id.changeDateButton).setOnClickListener(this);

        loadThisNote();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    void loadThisNote() {
        String[] projection = {
                ToDoProvider.TODO_TABLE_COL_ID,
                ToDoProvider.TODO_TABLE_COL_TITLE,
                ToDoProvider.TODO_TABLE_COL_CONTENT,
                ToDoProvider.TODO_TABLE_COL_DONE,
                ToDoProvider.TODO_TABLE_COL_DATE};

        Intent homeIntent = getIntent();
        noteIndex = homeIntent.getLongExtra("noteIndex", -1);
        Cursor cursor = getContentResolver().query(ToDoProvider.CONTENT_URI,projection,null,null,"_ID DESC");
        cursor.moveToFirst();
        cursor.move((int) noteIndex);

        String title = cursor.getString(cursor.getColumnIndexOrThrow(ToDoProvider.TODO_TABLE_COL_TITLE));
        String content = cursor.getString(cursor.getColumnIndexOrThrow(ToDoProvider.TODO_TABLE_COL_CONTENT));
        int done = cursor.getInt(cursor.getColumnIndexOrThrow(ToDoProvider.TODO_TABLE_COL_DONE));
        long dueMs = cursor.getLong(cursor.getColumnIndexOrThrow(ToDoProvider.TODO_TABLE_COL_DATE));

        loadDate(dueMs);
        EditText titleInput = findViewById(R.id.tvNoteTitle);
        EditText contentInput = findViewById(R.id.etNoteContent);
        CheckBox doneInput = findViewById((R.id.doneCheckBox));

        titleInput.setText(title);
        contentInput.setText(content);
        doneInput.setChecked(done != 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    void loadDate(long dueMs) {
        ZonedDateTime dateTime;
        if (dueMs > 0) {
            dateTime = Instant.ofEpochMilli(dueMs).atZone(ZoneId.systemDefault());
        } else {
            dateTime = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.systemDefault());
        }

        dueYear = dateTime.getYear();
        dueMonth = dateTime.getMonthValue();
        dueDayOfMonth = dateTime.getDayOfMonth();
        dueHrs = dateTime.getHour();
        dueMin = dateTime.getMinute();

        Button mTimePicker = findViewById(R.id.changeTimeButton);
        mTimePicker.setText(dueHrs+":"+ ((dueMin >= 10) ? dueMin : "0" + dueMin));
        Button changeDateButton = findViewById(R.id.changeDateButton);
        changeDateButton.setText(dueMonth+"/"+dueDayOfMonth+"/"+dueYear);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
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

            ZonedDateTime dateTime = LocalDateTime
                    .of(dueYear, dueMonth, dueDayOfMonth, dueHrs, dueMin)
                    .atZone(ZoneId.systemDefault());
            long dueMs = dateTime.toInstant().toEpochMilli();

            ContentValues myCV = new ContentValues();
            myCV.put(ToDoProvider.TODO_TABLE_COL_ID, noteId);
            myCV.put(ToDoProvider.TODO_TABLE_COL_TITLE, String.valueOf(titleInput.getText()));
            myCV.put(ToDoProvider.TODO_TABLE_COL_CONTENT, String.valueOf(contentInput.getText()));
            myCV.put(ToDoProvider.TODO_TABLE_COL_DONE, doneInput.isChecked() ? 1 : 0);
            myCV.put(ToDoProvider.TODO_TABLE_COL_DATE, dueMs);

            int didWork = getContentResolver().update(Uri.parse(ToDoProvider.CONTENT_URI + "/" + noteId), myCV, null, null);
            if (didWork == 1) {
                createAlarm();
                finish();
            }
        } else{
            Toast.makeText(getApplicationContext(), "Error Saving Note", Toast.LENGTH_LONG).show();
        }
    }

    void createAlarm() {
        Intent intent = new Intent(NoteActivity.this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), (int) noteIndex, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        alarmManager.set(AlarmManager.RTC_WAKEUP, targetCal.getTimeInMillis(), pendingIntent);
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
        mOnTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourofday, int minute) {
                dueHrs = hourofday;
                dueMin = minute;
                Button mTimePicker = findViewById(R.id.changeTimeButton);
                mTimePicker.setText(dueHrs+":"+ ((dueMin >= 10) ? dueMin : "0" + dueMin));
            }
        };

        TimePickerDialog mTimePickerDialog = new TimePickerDialog(
                NoteActivity.this,
                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                mOnTimeSetListener,
                dueHrs,dueMin,true);

        mTimePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mTimePickerDialog.show();
    }

    void showDatePicker() {
        mOnDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                dueYear = year;
                dueMonth = month + 1;
                dueDayOfMonth = dayOfMonth;
                Button changeDateButton = findViewById(R.id.changeDateButton);
                changeDateButton.setText(dueMonth+"/"+dueDayOfMonth+"/"+dueYear);
            }
        };

        DatePickerDialog mDatePickerDialog = new DatePickerDialog(
                NoteActivity.this,
                android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                mOnDateSetListener,
                dueYear,(dueMonth - 1),dueDayOfMonth);
        mDatePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDatePickerDialog.show();
    }
}

