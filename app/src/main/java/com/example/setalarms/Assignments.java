package com.example.setalarms;

import android.provider.BaseColumns;

public class Assignments {

    public static final String DB_NAME = "com.aziflaj.todolist.db";
    public static final int DB_VERSION = 4;

    public class TaskEntry implements BaseColumns {
        public static final String TABLE = "tasks";

        public static final String COL_TASK_TITLE = "title";
        public static final String COL_TASK_MONTH = "month";
        public static final String COL_TASK_DAY = "day";
        public static final String COL_TASK_HOUR = "hour";
        public static final String COL_TASK_MIN = "min";
    }
}

