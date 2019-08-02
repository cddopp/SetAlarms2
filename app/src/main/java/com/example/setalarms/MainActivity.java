package com.example.setalarms;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;


import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private AssignmentHelper mHelper;
    private ListView mTaskListView;
    private ArrayAdapter<String> mAdapter;
    private ArrayAdapter<String> mAdapter2;

    private String[] dbCols =
            {Assignments.TaskEntry._ID, Assignments.TaskEntry.COL_TASK_TITLE, Assignments.TaskEntry.COL_TASK_MONTH,
            Assignments.TaskEntry.COL_TASK_DAY, Assignments.TaskEntry.COL_TASK_HOUR, Assignments.TaskEntry.COL_TASK_MIN};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        FloatingActionButton fab = findViewById(R.id.alarm);


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
        ArrayList<String> Dates = new ArrayList<>();
        SQLiteDatabase db = mHelper.getReadableDatabase();
        Cursor cursor = db.query(Assignments.TaskEntry.TABLE, dbCols, null, null, null, null, null);

        while (cursor.moveToNext()) {
            int idx = cursor.getColumnIndex(Assignments.TaskEntry.COL_TASK_TITLE);
            int month = cursor.getColumnIndex(Assignments.TaskEntry.COL_TASK_MONTH);
            int day = cursor.getColumnIndex(Assignments.TaskEntry.COL_TASK_DAY);
            int Hour = cursor.getColumnIndex(Assignments.TaskEntry.COL_TASK_HOUR);
            int Min = cursor.getColumnIndex(Assignments.TaskEntry.COL_TASK_MIN);
            taskList.add(cursor.getString(idx)+ "\n" + cursor.getString(month)  + "/" + cursor.getString(day)+ " at " + cursor.getString(Hour)+ ":" + cursor.getString(Min));
        }

        if (mAdapter == null) {
            mAdapter = new ArrayAdapter<>(this,
                    R.layout.list,
                    R.id.task_title,
                    taskList);
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
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.delete(Assignments.TaskEntry.TABLE,
                Assignments.TaskEntry.COL_TASK_TITLE + " = ?",
                new String[]{task});
        db.close();
        updateUI();
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
                    month = dp.getMonth() + 1;
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
                    sqLiteDatabase.close();
                    updateUI();

                    dialog.dismiss();
                }
            });

            dialog.show();
        }
    }

}


