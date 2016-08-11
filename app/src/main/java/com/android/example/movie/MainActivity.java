package com.android.example.movie;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // adding grid fragment programmatically
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, new GridFragment())
                    .commit();
        }
    }

    public static class GridFragment extends Fragment {

        // using this tag for logging (debugging)
        private final String LOG_TAG = GridFragment.class.getSimpleName();

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View rootView =  inflater.inflate(R.layout.fragment_grid, container, false);

            // first phase passing dummy data to the grid view
            String[] testStrs = {"test1", "test2", "test3"};
            List<String> data = Arrays.asList(testStrs);

            // getting grid view from the fragment root view then set to it dummy data
            GridView gridView = (GridView)rootView.findViewById(R.id.gridView);
            gridView.setAdapter(
                    new ArrayAdapter<String>(getActivity(), // activity that contain the fragment
                            R.layout.list_item_grid,
                            R.id.list_item_grid_textView,
                            data)
            );


            return rootView;
        }
    }
}
