package com.company.klassyapp.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.company.klassyapp.R;
import com.company.klassyapp.ui.ClassAdapter;
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
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<String> classNames = new ArrayList<>();
    private ClassAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = root.findViewById(R.id.classRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ClassAdapter(classNames);
        recyclerView.setAdapter(adapter);

        fetchGoogleClassroomCourses();

        return root;
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
                    for (int i = 0; i < courses.length(); i++) {
                        JSONObject course = courses.getJSONObject(i);
                        classNames.add(course.optString("name"));
                    }

                    requireActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());

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

