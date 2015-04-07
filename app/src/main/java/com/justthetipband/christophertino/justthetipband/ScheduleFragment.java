package com.justthetipband.christophertino.justthetipband;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.SaxAsyncHttpResponseHandler;

import org.apache.http.Header;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Schedule Fragment
 * Retrieve RSS feed from http://www.justthetipband.com/?feed=gigpress
 *
 * @author christophertino
 * @since Apr 2015
 */
public class ScheduleFragment extends ListFragment {
    private static final String TAG = "ScheduleFragment";
    private ListView listView;
    private int currentIndex;
    private ProgressBar pb;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set current index based on args MainActivity
        if (getArguments() != null) {
            currentIndex = getArguments().getInt("fragmentIndex");
        } else {
            currentIndex = 2;
        }
    }

    @Override //use onCreateView to inflate our layout fragment so that we can access views within
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        //We inflate the fragment with a single ListView element, because ListFragment must find a ListView with @android:id/list
        //Below in onActivityCreated -> SimpleCursorAdapter we bind to R.layout.list_item
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        //set our listview element here because we need access to the view parameter
        listView = (ListView) view.findViewById(android.R.id.list);

        //set progress bar
        pb = (ProgressBar) view.findViewById(R.id.progress_bar);

        //set the title of the fragment
        TextView fragmentTitle = (TextView) view.findViewById(R.id.fragment_title);
        fragmentTitle.setText("Upcoming Shows");

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getRSSContent();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Log.i("FragmentList", "Item clicked: " + id);
    }

    //return the ScheduleFragment to the MainActivity
    public static ScheduleFragment newInstance(int fragmentIndex) {
        ScheduleFragment fragment = new ScheduleFragment();

        //Pass fragment index as argument
        Bundle args = new Bundle();
        args.putInt("fragmentIndex", fragmentIndex);
        fragment.setArguments(args);

        return fragment;
    }

    private void getRSSContent() {
        AsyncHttpClient client = new AsyncHttpClient();
        DefaultHandler handlerInstance = new DefaultHandler();
        client.get("http://www.justthetipband.com/?feed=gigpress", new SaxAsyncHttpResponseHandler(handlerInstance) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, DefaultHandler defaultHandler) {
                Log.v(TAG, "success " + statusCode);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, DefaultHandler defaultHandler) {
                Log.i(TAG, "failure " + statusCode);
            }
        });
    }
}
