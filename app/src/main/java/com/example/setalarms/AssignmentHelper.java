package com.example.setalarms;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AssignmentHelper extends SQLiteOpenHelper {

    public AssignmentHelper(Context context) {
        super(context, Assignments.DB_NAME, null, Assignments.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String createTable = "CREATE TABLE " + Assignments.TaskEntry.TABLE + " ( " +
                Assignments.TaskEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Assignments.TaskEntry.COL_TASK_TITLE + " TEXT NOT NULL, " +
                Assignments.TaskEntry.COL_TASK_MONTH + " INTEGER NOT NULL, " +
                Assignments.TaskEntry.COL_TASK_DAY + " INTEGER NOT NULL, " +
                Assignments.TaskEntry.COL_TASK_HOUR + " INTEGER NOT NULL, " +
                Assignments.TaskEntry.COL_TASK_MIN + " INTEGER NOT NULL);";

        sqLiteDatabase.execSQL(createTable);
    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Assignments.TaskEntry.TABLE);
        onCreate(sqLiteDatabase);
    }
}
