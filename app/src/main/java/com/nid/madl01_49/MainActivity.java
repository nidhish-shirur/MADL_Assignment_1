package com.nid.madl01_49;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.location.Address;
import android.location.Geocoder;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private EditText etTitle, etDescription, etTime;
    private AutoCompleteTextView etCategory;
    private Button btnLocation, btnAdd, btnView;
    private TextView tvLocation;
    private DatabaseHelper databaseHelper;
    private LocationManager locationManager;

    private String currentLocationString = "Not set";
    private static final int LOCATION_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etTime = findViewById(R.id.etTime);
        etCategory = findViewById(R.id.etCategory);
        btnLocation = findViewById(R.id.btnLocation);
        btnAdd = findViewById(R.id.btnAdd);
        btnView = findViewById(R.id.btnView);
        tvLocation = findViewById(R.id.tvLocation);

        databaseHelper = new DatabaseHelper(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Setup the Category Dropdown
        String[] categories = new String[]{"Work", "Personal", "Study", "Health", "Shopping", "Other"};
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                categories
        );
        etCategory.setAdapter(arrayAdapter);

        // Request Notification Permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        // Schedule Background Task
        PeriodicWorkRequest reminderWorkRequest =
                new PeriodicWorkRequest.Builder(ReminderWorker.class, 10, TimeUnit.MINUTES).build();
        WorkManager.getInstance(this).enqueue(reminderWorkRequest);

        etTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                String amPm;
                                if (hourOfDay >= 12) {
                                    amPm = "PM";
                                    if (hourOfDay > 12) hourOfDay -= 12;
                                } else {
                                    amPm = "AM";
                                    if (hourOfDay == 0) hourOfDay = 12;
                                }
                                String formattedTime = String.format("%02d:%02d %s", hourOfDay, minute, amPm);
                                etTime.setText(formattedTime);
                            }
                        }, hour, minute, false);
                timePickerDialog.show();
            }
        });

        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLocationPermissionAndGetLocation();
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveReminder();
            }
        });

        btnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ViewRemindersActivity.class);
                startActivity(intent);
            }
        });
    }

    private void checkLocationPermissionAndGetLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        } else {
            getLocation();
        }
    }

    private void getLocation() {
        try {
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();

                    // NEW: Use Geocoder to turn Lat/Lon into a real address
                    Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
                        if (addresses != null && !addresses.isEmpty()) {
                            // Get the full street address
                            currentLocationString = addresses.get(0).getAddressLine(0);
                        } else {
                            // Fallback if no address is found
                            currentLocationString = "Lat: " + lat + ", Lon: " + lon;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        // Fallback if there is an internet/geocoder error
                        currentLocationString = "Lat: " + lat + ", Lon: " + lon;
                    }

                    tvLocation.setText("Location: " + currentLocationString);
                }
            }, null);
            Toast.makeText(this, "Fetching location...", Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveReminder() {
        String title = etTitle.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String category = etCategory.getText().toString().trim();

        if (title.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "Title and Time are required", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isInserted = databaseHelper.insertReminder(title, desc, time, currentLocationString, category);

        if (isInserted) {
            Toast.makeText(this, "Reminder Added Successfully!", Toast.LENGTH_SHORT).show();
            etTitle.setText("");
            etDescription.setText("");
            etTime.setText("");
            etCategory.setText("");
            tvLocation.setText("Location: Not set");
            currentLocationString = "Not set";
        } else {
            Toast.makeText(this, "Error adding reminder", Toast.LENGTH_SHORT).show();
        }
    }
}