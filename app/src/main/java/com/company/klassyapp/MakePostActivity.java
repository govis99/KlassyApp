package com.company.klassyapp;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.signin.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.util.*;

public class MakePostActivity extends AppCompatActivity {

    private Spinner classSpinner;
    private EditText assignmentInput;
    private ArrayList<String> classNames = new ArrayList<>();
    private ArrayList<String> classIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_post);

        classSpinner = findViewById(R.id.classSpinner);
        assignmentInput = findViewById(R.id.assignmentInput);

        fetchGoogleClassroomCourses();

        findViewById(R.id.submitAssignmentBtn).setOnClickListener(v -> {
            int position = classSpinner.getSelectedItemPosition();
            String selectedClass = classNames.get(position);
            String assignmentText = assignmentInput.getText().toString().trim();

            if (assignmentText.isEmpty()) {
                Toast.makeText(this, "Please enter an assignment.", Toast.LENGTH_SHORT).show();
            } else {
                // TODO: Save assignment (e.g. Firestore, local DB)
                Toast.makeText(this, "Assignment posted to " + selectedClass, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchGoogleClassroomCourses() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            new Thread(() -> {
                try {
                    String token = GoogleAuthUtil.getToken(
                            this,
                            account.getAccount(),
                            "oauth2:https://www.googleapis.com/auth/classroom.courses.readonly"
                    );

                    URL url = new URL("https://classroom.googleapis.com/v1/courses");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                    conn.setRequestMethod("GET");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JSONObject json = new JSONObject(response.toString());
                    JSONArray courses = json.getJSONArray("courses");

                    classNames.clear();
                    classIds.clear();
                    for (int i = 0; i < courses.length(); i++) {
                        JSONObject course = courses.getJSONObject(i);
                        classNames.add(course.optString("name"));
                        classIds.add(course.optString("id"));
                    }

                    runOnUiThread(() -> {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                android.R.layout.simple_spinner_item, classNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        classSpinner.setAdapter(adapter);
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() ->
                            Toast.makeText(this, "Failed to load classes.", Toast.LENGTH_SHORT).show()
                    );
                }
            }).start();
        }
    }
}
