package com.company.klassyapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ClassesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private com.company.klassyapp.CourseAdapter adapter;
    private ArrayList<String> courseList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classes);

        recyclerView = findViewById(R.id.classesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new com.company.klassyapp.CourseAdapter(courseList);
        recyclerView.setAdapter(adapter);

        fetchAccessTokenAndCourses();
    }

    private void fetchAccessTokenAndCourses() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            new Thread(() -> {
                try {
                    String token = GoogleAuthUtil.getToken(
                            this,
                            account.getAccount(),
                            "oauth2:https://www.googleapis.com/auth/classroom.courses.readonly"
                    );
                    fetchCourses(token);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void fetchCourses(String accessToken) {
        try {
            URL url = new URL("https://classroom.googleapis.com/v1/courses");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray courses = jsonResponse.getJSONArray("courses");

            courseList.clear();
            for (int i = 0; i < courses.length(); i++) {
                JSONObject course = courses.getJSONObject(i);
                String name = course.optString("name");
                courseList.add(name);
            }

            runOnUiThread(() -> adapter.notifyDataSetChanged());

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, "Failed to load courses", Toast.LENGTH_SHORT).show());
        }
    }
}
