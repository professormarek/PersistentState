package com.example.marek.persistentstate;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    /*
    HELPER CLASS for defining the table contents
    we need it because the Android database expects a "magical" "_ID" field
    (remember the fish)
     */
    public static class MyDataEntry implements BaseColumns {
        //here I will define my table and column names as String constants
        //note that there's a _ID field inherited from BaseColumns
        public static final String TABLE_NAME = "students";
        public static final String STUDENT_ID_COLUMN = "studentID";
        public static final String GRADE_COLUMN = "grade";
    }

    /*
    ANOTHER HELPER CLASS - is for managing database creating and version management
     */
    public class MyDbHelper extends SQLiteOpenHelper{
        //some static variables to remember information about our database
        //one is the database name
        public static final String DB_NAME = "CoolDatabase.db";
        //note: every time you change the database schema, you must increment this database version
        //the other is the database version
        public static final int DB_VERSION = 1;

        private static final String SQL_CREATE_TABLE = "CREATE TABLE "+ MyDataEntry.TABLE_NAME +
                 " (" + MyDataEntry._ID + " INTEGER PRIMARY KEY," + MyDataEntry.STUDENT_ID_COLUMN +
                " TEXT," + MyDataEntry.GRADE_COLUMN + " TEXT )";
        private static final String SQL_DELETE_QUERY = "DROP TABLE IF EXISTS " + MyDataEntry.TABLE_NAME;


        public MyDbHelper(Context context){
            super(context, DB_NAME, null, DB_VERSION);
        }

        /**
         * This framework method is called whenever the database is opened but doesn't exist yet
         * @param db
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            System.out.println("Executing Query: SQL_CREATE_TABLE "  + SQL_CREATE_TABLE);
            db.execSQL(SQL_CREATE_TABLE);
        }

        /**
         * this is another framework method that is called whenever the DB_VERSION is incremented
         * @param db
         * @param oldVersion
         * @param newVersion
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //shortcut: let's discard the table and start over
            db.execSQL(SQL_DELETE_QUERY);
            onCreate(db);
        }

        /**
         * called whenever the database version is decremented
         * @param db
         * @param oldVersion
         * @param newVersion
         */
        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            super.onDowngrade(db, oldVersion, newVersion);
            onUpgrade(db, oldVersion,newVersion);
        }
    }

    //declare the preferences file name as a constant string for easy use elsewhere in code
    //choose a cool file name
    public static final String PREF_FILE_NAME = "MySenecaPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //create a handler for the button
        Button saveGradeButton = (Button)findViewById(R.id.gradeButton);
        saveGradeButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                saveGrade();
            }
        });

        //read in any information from the preferences file if it exists
        loadPreferences();

        loadDatabase();
    }

    @Override
    protected void onStop() {
        super.onStop();

        //when the activity stops, we want to save the data entered into the Edit boxes for next time
        savePreferences();



    }

    private void loadPreferences(){
        //read the data we saved in SharedPreferences and update the EditText boxes

        //first, open up the sharedpreferences file, if it doesn't exist create it
        //recall second argument, mode=0 means private preferences
        SharedPreferences sp = getSharedPreferences(PREF_FILE_NAME, 0);

        //get data out of the SharedPrefereces object

        String studentID = sp.getString("studentID", "Enter student ID:");
        EditText studentIDBox = (EditText)findViewById(R.id.studentID);
        studentIDBox.setText(studentID);

        String studentGrade = sp.getString("studentGrade", "Enter student grade:");
        EditText studentGradeBox = (EditText) findViewById(R.id.studentGrade);
        studentGradeBox.setText(studentGrade);
    }

    private void savePreferences(){
        //in order to do that we need an Sharedpreferences.Editor object
        //second argument is the mode
        SharedPreferences sp = getSharedPreferences(PREF_FILE_NAME, 0);
        //going to need the Editor in order to write/save preferences
        //.edit() returns a reference to the Editor
        SharedPreferences.Editor editor = sp.edit();

        //get the values of the EditText fields in the main activity layout
        EditText studentIDBox = (EditText)findViewById(R.id.studentID);
        String studentID = studentIDBox.getText().toString();
        EditText studentGradeBox = (EditText) findViewById(R.id.studentGrade);
        String studentGrade = studentGradeBox.getText().toString();

        //use the Editor to write info into the SharedPreferences file
        editor.putString("studentID", studentID);
        editor.putString("studentGrade", studentGrade);

        //don't forget to commit, or else nothing is written!
        editor.commit();
    }

    /**
     * demonstates how to access the database for writing!
     */
    private void saveGrade(){
        //here we will write the student id and grade to the database

        //ideally this work would be done in an AsyncTask - I leave this to you as an exercise

        //let's get an instance of MyDbHelper - notice it needs a context
        MyDbHelper dbHelper = new MyDbHelper(this);
        //get a reference to the database in write mode
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //create a new map of values, where column names are the keys
        ContentValues newRow = new ContentValues();
        //add the rows to the map
        EditText studentIDBox = (EditText)findViewById(R.id.studentID);
        String studentID = studentIDBox.getText().toString();
        newRow.put(MyDataEntry.STUDENT_ID_COLUMN,studentID); //this adds the student ID to the record
        EditText studentGradeBox = (EditText) findViewById(R.id.studentGrade);
        String studentGrade = studentGradeBox.getText().toString();
        newRow.put(MyDataEntry.GRADE_COLUMN,studentGrade);
        System.out.println("Writing to database: " + studentID +", " +  studentGrade);
        //Insert the new row, return the primary key value of the new row
        //(in case we need it for some reason)
        //the middle argument is what to insert if newRow is a null object (null)
        long newRowId = db.insert(MyDataEntry.TABLE_NAME, null, newRow);
        System.out.println("Result: " + newRowId);
    }

    /**
     * this method shows how to access the database for reading
     */
    private void loadDatabase(){
        //get a reference to MyDbHelper
        MyDbHelper dbHelper = new MyDbHelper(this);
        //get a readable database reference
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        //define which columns we want inlcluded in our query
        String[] query_columns = {
                MyDataEntry._ID,
                MyDataEntry.STUDENT_ID_COLUMN,
                MyDataEntry.GRADE_COLUMN
        };
        //create a select query
        String selectQuery = MyDataEntry.STUDENT_ID_COLUMN + " = ?";
        //these the arguments to my select query
        String[] selectionArgs = {" Filter string "};

        System.out.println("Executing select query!");

        //define how we want the results to be ordered
        String sortOrder = MyDataEntry.STUDENT_ID_COLUMN + " DESC";
        //form our select query - get a cursor object.. see documentation
        Cursor cursor = db.query(
                MyDataEntry.TABLE_NAME, //which table to execute the query against
                query_columns, //the columns that will be returned
                null, //provide a WHERE clause - or null to return all rows
                null, //arguments for the WHERE clause
                null, //grouping
                null, //filter
                sortOrder //sorting order
        );
        //move to the first record result - returns false if there are no records
        boolean hasMoreData = cursor.moveToFirst();
        while(hasMoreData){
            //get the value of each column...
            long recordID = cursor.getLong(cursor.getColumnIndexOrThrow(MyDataEntry._ID));
            String studentID = cursor.getString(cursor.getColumnIndexOrThrow(MyDataEntry.STUDENT_ID_COLUMN));
            String studentGrade = cursor.getString(cursor.getColumnIndexOrThrow(MyDataEntry.GRADE_COLUMN));
            //I'm going to write to the log, you will populate an ArrayList for the ListView
            System.out.println("RECORD KEY: " + recordID + " Student ID: " + studentID + " Student Grade: " + studentGrade);
            //advance to the next record - returns false if there are no more records
            hasMoreData = cursor.moveToNext();
        }


    }
}
