package com.company.klassyapp.ui;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_posts, container, false);

        classSpinner = view.findViewById(R.id.classSpinner);
        assignmentInput = view.findViewById(R.id.assignmentInput);

        fetchGoogleClassroomCourses();

        view.findViewById(R.id.submitAssignmentBtn).setOnClickListener(v -> {
            int position = classSpinner.getSelectedItemPosition();
            String selectedClass = classNames.get(position);
            String assignmentText = assignmentInput.getText().toString().trim();

            if (assignmentText.isEmpty()) {
                Toast.makeText(getContext(), "Please enter an assignment.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Assignment posted to " + selectedClass, Toast.LENGTH_SHORT).show();
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
                    for (int i = 0; i < courses.length(); i++) {
                        JSONObject course = courses.getJSONObject(i);
                        classNames.add(course.optString("name"));
                        classIds.add(course.optString("id"));
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
}

