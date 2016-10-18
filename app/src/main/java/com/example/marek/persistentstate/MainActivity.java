package com.example.marek.persistentstate;

import android.content.Context;
import android.content.SharedPreferences;
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

        public MyDbHelper(Context context){
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

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

    private void saveGrade(){
        //here we will write the student id and grade to the database
    }
}
