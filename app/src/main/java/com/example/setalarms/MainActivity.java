package com.example.setalarms;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Debug;
import android.provider.AlarmClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private AssignmentHelper mHelper;
    private ListView mTaskListView;
    private ArrayAdapter<String> mAdapter;

    private String[] dbCols =
            {Assignments.TaskEntry._ID, Assignments.TaskEntry.COL_TASK_TITLE, Assignments.TaskEntry.COL_TASK_MONTH,
                    Assignments.TaskEntry.COL_TASK_DAY, Assignments.TaskEntry.COL_TASK_HOUR, Assignments.TaskEntry.COL_TASK_MIN};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHelper = new AssignmentHelper(this);
        mTaskListView = (ListView) findViewById(R.id.ass_list);

        SQLiteDatabase db = mHelper.getReadableDatabase();

        //db.query(table name, array of columns, ...)
        Cursor cursor = db.query(Assignments.TaskEntry.TABLE, dbCols, null, null, null, null, null);

        while (cursor.moveToNext()) {
            int idx = cursor.getColumnIndex(Assignments.TaskEntry.COL_TASK_TITLE);
            Log.d(TAG, "Task: " + cursor.getString(idx));
        }
        cursor.close();
        updateUI();
        db.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        AssignmentDialog ad = new AssignmentDialog();
        ad.showDialog(this);

        return super.onOptionsItemSelected(item);
    }

    private void updateUI() {
        ArrayList<String> taskList = new ArrayList<>();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(Assignments.TaskEntry.TABLE, dbCols, null, null, null, null, null);

        while (cursor.moveToNext()) {
            int idIdx = cursor.getColumnIndex(Assignments.TaskEntry._ID);
            int titleIdx = cursor.getColumnIndex(Assignments.TaskEntry.COL_TASK_TITLE);
            int monthIdx = cursor.getColumnIndex(Assignments.TaskEntry.COL_TASK_MONTH);
            int dayIdx = cursor.getColumnIndex(Assignments.TaskEntry.COL_TASK_DAY);
            int hourIdx = cursor.getColumnIndex(Assignments.TaskEntry.COL_TASK_HOUR);
            int minIdx = cursor.getColumnIndex(Assignments.TaskEntry.COL_TASK_MIN);
            taskList.add(cursor.getString(titleIdx)+ "\n" + cursor.getInt(monthIdx)  + "/" +
                    cursor.getInt(dayIdx)+ " at " + cursor.getInt(hourIdx)+ ":" + cursor.getInt(minIdx)
                    + "\n" + cursor.getInt(idIdx));
        }

        if (mAdapter == null) {
            mAdapter = new ArrayAdapter<>(this, R.layout.list, R.id.task_title, taskList);
            mTaskListView.setAdapter(mAdapter);
        } else {
            mAdapter.clear();
            mAdapter.addAll(taskList);
            mAdapter.notifyDataSetChanged();
        }
        cursor.close();
        db.close();
    }

    public void deleteTask(View view) {
        View parent = (View) view.getParent();
        TextView taskTextView = (TextView) parent.findViewById(R.id.task_title);

        String task = String.valueOf(taskTextView.getText());
        int index = task.indexOf("\n");
        String newTask = task.substring(0, index);

        //hacky solution, needs better way
        int index2 = task.indexOf("\n");
        String c = task.substring(index2 + 1);
        int index3 = c.indexOf("\n");
        String c2 = c.substring(index3 + 1);

        //should be same code as when alarm was created
        int code = Integer.parseInt(c2);
        cancelAlarm(code);

        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.delete(Assignments.TaskEntry.TABLE,Assignments.TaskEntry.COL_TASK_TITLE +
            " = ?" , new String[]{newTask});
        db.close();
        updateUI();
    }

    public void startAlarm(int code, int month, int day, int hour, int min){
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this.getApplicationContext(), code, intent, 0);

        Calendar c = Calendar.getInstance();

        int currentMonth = c.get(Calendar.MONTH) + 1;
        int currentDay = c.get(Calendar.DAY_OF_MONTH);
        int currentHour = c.get(Calendar.HOUR_OF_DAY);
        int currentMin = c.get(Calendar.MINUTE);
        Log.d(TAG, "" + currentMonth + " " + currentDay + " " + currentHour + " " + currentMin);
        Log.d(TAG, "" + month + " " + day + " " + hour + " " + min);

        long diff = 0;
        diff += (month - currentMonth) * 43200; //convert to minutes
        diff += (day - currentDay) * 1440;
        diff += (hour - currentHour) * 60;
        diff += (min - currentMin);

        diff = diff * 60000; //convert to milliseconds

        Log.d(TAG, "" + diff);


        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, diff, pendingIntent);

        Toast.makeText(this, "Alarm set in " + diff / 1000 + " seconds",Toast.LENGTH_LONG).show();
    }

    //code needs to be the same as when the alarm was created
    public void cancelAlarm(int code){
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this.getApplicationContext(), code, intent, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        alarmManager.cancel(pendingIntent);
    }

    private class AssignmentDialog {

        public String task;
        public int month, day, hour, min;
        EditText et;
        DatePicker dp;
        TimePicker tp;

        public void showDialog(Activity activity){
            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.assignment_dialog);

            TextView text = (TextView) dialog.findViewById(R.id.text_title);
            text.setText("Add an Assignment");

            TextView title = (TextView) dialog.findViewById(R.id.ass_title);
            title.setText("Task Name: ");

            et = (EditText) dialog.findViewById(R.id.task);

            dp = (DatePicker)dialog.findViewById(R.id.datePicker);
            tp = (TimePicker) dialog.findViewById(R.id.timePicker);
            tp.setIs24HourView(true);

            Button cancelButton = (Button) dialog.findViewById(R.id.btn_cancel);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            Button addButton = (Button) dialog.findViewById(R.id.btn_add);
            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    task = et.getText().toString();
                    month = dp.getMonth() + 1;  //June returns as 5 instead of 6, that's why we add 1
                    day = dp.getDayOfMonth();
                    hour = tp.getHour();
                    min = tp.getMinute();

                    Log.d("picker", task + " " + month + " " + day + " " + hour + " " + min);

                    SQLiteDatabase sqLiteDatabase = mHelper.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put(Assignments.TaskEntry.COL_TASK_TITLE, task);
                    values.put(Assignments.TaskEntry.COL_TASK_MONTH, month);
                    values.put(Assignments.TaskEntry.COL_TASK_DAY, day);
                    values.put(Assignments.TaskEntry.COL_TASK_HOUR, hour);
                    values.put(Assignments.TaskEntry.COL_TASK_MIN, min);
                    sqLiteDatabase.insertWithOnConflict(Assignments.TaskEntry.TABLE,
                            null,
                            values,
                            SQLiteDatabase.CONFLICT_REPLACE);


                    Cursor cursor = sqLiteDatabase.query(Assignments.TaskEntry.TABLE, new String[]{Assignments.TaskEntry._ID}, null,
                            null, null, null, null);
                    cursor.moveToLast();
                    int idIdx = cursor.getColumnIndex(Assignments.TaskEntry._ID);

                    startAlarm(cursor.getInt(idIdx), month, day, hour, min);
                    sqLiteDatabase.close();
                    updateUI();
                    dialog.dismiss();
                }
            });

            dialog.show();
        }
    }

}


