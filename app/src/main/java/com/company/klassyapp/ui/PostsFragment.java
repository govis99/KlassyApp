package com.company.klassyapp.ui;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.company.klassyapp.R;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.signin.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.util.*;

public class PostsFragment extends Fragment {

    private Spinner classSpinner;
    private EditText assignmentInput;
    private ArrayList<String> classNames = new ArrayList<>();
    private ArrayList<String> classIds = new ArrayList<>();
    private Button selectDueDateBtn;
    private String selectedDueDate = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_posts, container, false);

        classSpinner = view.findViewById(R.id.classSpinner);
        assignmentInput = view.findViewById(R.id.assignmentInput);
        selectDueDateBtn = view.findViewById(R.id.selectDueDateBtn);

        // Setup date picker logic
        selectDueDateBtn.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view1, year1, month1, dayOfMonth) -> {
                        selectedDueDate = (month1 + 1) + "/" + dayOfMonth + "/" + year1;
                        selectDueDateBtn.setText("Due: " + selectedDueDate);
                    },
                    year, month, day
            );

            datePickerDialog.show();
        });

        fetchGoogleClassroomCourses();

        view.findViewById(R.id.submitAssignmentBtn).setOnClickListener(v -> {
            int position = classSpinner.getSelectedItemPosition();
            String selectedClass = classNames.get(position);
            String assignmentText = assignmentInput.getText().toString().trim();

            if (assignmentText.isEmpty()) {
                Toast.makeText(getContext(), "Please enter an assignment.", Toast.LENGTH_SHORT).show();
            } else if (selectedDueDate.isEmpty()) {
                Toast.makeText(getContext(), "Please select a due date.", Toast.LENGTH_SHORT).show();
            } else {
                // Save to SharedPreferences
                String assignment = "Class: " + selectedClass +
                        "\nAssignment: " + assignmentText +
                        "\nDue: " + selectedDueDate;

                SharedPreferences prefs = requireContext().getSharedPreferences("FeedPrefs", Context.MODE_PRIVATE);
                Set<String> feedItems = new HashSet<>(prefs.getStringSet("feed", new HashSet<>()));
                feedItems.add(assignment);

                SharedPreferences.Editor editor = prefs.edit();
                editor.putStringSet("feed", feedItems);
                editor.apply();

                Toast.makeText(getContext(),
                        "Assignment posted to " + selectedClass + "\nDue: " + selectedDueDate,
                        Toast.LENGTH_LONG).show();

                // Optionally clear form
                assignmentInput.setText("");
                selectedDueDate = "";
                selectDueDateBtn.setText("Select Due Date");
            }
        });

        return view;
    }

    private void fetchGoogleClassroomCourses() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireContext());
        if (account != null) {
            new Thread(() -> {
                try {
                    String token = GoogleAuthUtil.getToken(
                            requireContext(),
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

                    Set<String> seen = new HashSet<>();

                    for (int i = 0; i < courses.length(); i++) {
                        JSONObject course = courses.getJSONObject(i);
                        String courseName = course.optString("name");

                        if (!seen.contains(courseName)) {
                            seen.add(courseName);
                            classNames.add(courseName);
                            classIds.add(course.optString("id"));
                        }
                    }

                    requireActivity().runOnUiThread(() -> {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                                android.R.layout.simple_spinner_item, classNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        classSpinner.setAdapter(adapter);
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Failed to load classes.", Toast.LENGTH_SHORT).show()
                    );
                }
            }).start();
        }
    }

    public static class FeedFragment extends Fragment {

        private ListView feedListView;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_feed, container, false);
            feedListView = view.findViewById(R.id.feedListView);

            loadFeedFromSharedPreferences();

            return view;
        }

        private void loadFeedFromSharedPreferences() {
            SharedPreferences prefs = requireContext().getSharedPreferences("FeedPrefs", Context.MODE_PRIVATE);
            Set<String> feedSet = prefs.getStringSet("feed", new HashSet<>());

            List<String> feedList = new ArrayList<>(feedSet);
            Collections.reverse(feedList); // Show most recent first

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_list_item_1,
                    feedList
            );

            feedListView.setAdapter(adapter);
        }
    }
}



