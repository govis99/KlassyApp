package com.company.klassyapp.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.company.klassyapp.R;

import java.text.SimpleDateFormat;
import java.util.*;

public class FeedFragment extends Fragment {

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

        Set<String> updatedFeed = new HashSet<>();
        List<String> displayList = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        Date today = new Date();

        for (String entry : feedSet) {
            if (entry.contains("Due: ")) {
                try {
                    String dueStr = entry.substring(entry.indexOf("Due: ") + 5).trim();
                    Date dueDate = sdf.parse(dueStr);
                    if (dueDate != null && !dueDate.before(today)) {
                        updatedFeed.add(entry);        // keep valid
                        displayList.add(entry);        // show valid
                    }
                } catch (Exception e) {
                    e.printStackTrace(); // in case parsing fails, keep the item
                    updatedFeed.add(entry);
                    displayList.add(entry);
                }
            }
        }

        // Save updated feed without past-due items
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet("feed", updatedFeed);
        editor.apply();

        Collections.reverse(displayList); // Most recent first
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                displayList
        );
        feedListView.setAdapter(adapter);
    }

}
