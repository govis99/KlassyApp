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
