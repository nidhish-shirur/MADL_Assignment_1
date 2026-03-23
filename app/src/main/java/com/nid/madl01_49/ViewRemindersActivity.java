package com.nid.madl01_49;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ViewRemindersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ReminderAdapter adapter;
    private List<Reminder> reminderList;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_reminders);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        reminderList = new ArrayList<>();
        databaseHelper = new DatabaseHelper(this);

        loadRemindersFromDatabase();
    }

    private void loadRemindersFromDatabase() {
        Cursor cursor = databaseHelper.getAllReminders();

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                // Fetch the ID (Column 0) and the rest of the data
                int id = cursor.getInt(0);
                String title = cursor.getString(1);
                String desc = cursor.getString(2);
                String time = cursor.getString(3);
                String location = cursor.getString(4);
                String category = cursor.getString(5);

                reminderList.add(new Reminder(id, title, desc, time, location, category));
            }
            cursor.close();

            // Pass 'this' (the context) along with the list
            adapter = new ReminderAdapter(this, reminderList);
            recyclerView.setAdapter(adapter);
        } else {
            Toast.makeText(this, "No reminders found", Toast.LENGTH_SHORT).show();
        }
    }
}