package com.justthetipband.christophertino.justthetipband;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4;

/**
 * Website Blog Posts Fragment
 * Retrive blog content from http://www.justthetipband.com/blog
 *
 * @author christophertino
 * @since  Apr 2015
 */
public class WebsiteBlogPostsFragment extends ListFragment {
    private static final String TAG = "WebsiteBlogPostsFragment";
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
            currentIndex = 1;
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

        //set the title of each fragment
        TextView fragmentTitle = (TextView) view.findViewById(R.id.fragment_title);
        String titleOutput;
        switch (currentIndex) {
            case 0 :
                titleOutput = "Latest Blogs";
                break;
            case 1 :
                titleOutput = "Upcoming Shows";
                break;
            case 2 :
                titleOutput = "Latest Videos";
                break;
            case 3 :
                titleOutput = "Latest Tweets";
                break;
            default:
                titleOutput = "Just the Tip";
        }
        fragmentTitle.setText(titleOutput);
        return view;
    }

    @Override //this gets called when the parent Activity onCreate() finishes
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //setListAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, cheeses));
        RequestParams params = new RequestParams();
        params.put("posts_per_page", "10");
        try {
            getJSONContent(params);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.v(TAG, "HTTP GET request failed.");
        }

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Log.i("FragmentList", "Item clicked: " + id);
    }

    //return the WebsiteBlogPostsFragment to the MainActivity
    public static WebsiteBlogPostsFragment newInstance(int fragmentIndex) {
        WebsiteBlogPostsFragment fragment = new WebsiteBlogPostsFragment();

        //Pass fragment index as argument
        Bundle args = new Bundle();
        args.putInt("fragmentIndex", fragmentIndex);
        fragment.setArguments(args);

        return fragment;
    }

    private void getJSONContent(RequestParams params) throws JSONException {
        HTTPClient.get("posts", params, new JsonHttpResponseHandler() {
            @Override
            public void onProgress(int bytesWritten, int totalSize) {
                int progressPercentage = 100 * bytesWritten/totalSize;
                pb.setProgress(progressPercentage);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                JSONObject row = null;
                ArrayList<JSONObject> postsList = new ArrayList<>();

                for (int i = 0; i < response.length(); i++) {
                    //get each row as JSON object
                    try {
                        row = response.getJSONObject(i);
                    } catch (JSONException e) {
                        e.printStackTrace(System.out);
                    }

                    //add JSON to arrayList
                    if (row != null) {
                        postsList.add(row);
                    }
                }

                //hide progress bar
                pb.setVisibility(View.GONE);

                //Build ArrayAdapter and refresh view
                WebsiteArrayAdapter itemsAdapter = new WebsiteArrayAdapter(getActivity().getApplicationContext(), postsList);
                listView.setAdapter(itemsAdapter);
            }
        });
    }

    /*
     * HTTPClient
     * Factory Class for AsyncHttpClient
     */
    private static class HTTPClient {
        private static final String BASE_URL = "http://www.justthetipband.com/wp-json/";

        private static AsyncHttpClient client = new AsyncHttpClient();

        public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
            client.get(getAbsoluteUrl(url), params, responseHandler);
        }

        private static String getAbsoluteUrl(String relativeUrl) {
            return BASE_URL + relativeUrl;
        }
    }

    /*
     * WebsiteArrayAdapter
     * Custom ArrayAdapter used to populate the fragment
     * @param   JSONObject from getJSONContent()
     * @return  rendered content in fragment_list_item
     */
    private static class WebsiteArrayAdapter extends ArrayAdapter<JSONObject> {
        // View lookup cache
        private static class ViewHolder {
            TextView postTitle;
            TextView postContent;
        }

        public WebsiteArrayAdapter(Context context, ArrayList<JSONObject> row) {
            super(context, 0, row);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            JSONObject row = getItem(position);

            // Using ViewHolder pattern to cache findViewById() recurrences
            // https://github.com/codepath/android_guides/wiki/Using-an-ArrayAdapter-with-ListView#improving-performance-with-the-viewholder-pattern
            ViewHolder viewHolder;

            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_list_item, parent, false);
                viewHolder.postTitle = (TextView) convertView.findViewById(R.id.postTitle);
                viewHolder.postContent = (TextView) convertView.findViewById(R.id.postContent);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            // Populate the data into the template view using the data object
            try {
                String excerpt = Html.fromHtml(row.getString("excerpt")).toString(); //strip out html tags
                viewHolder.postTitle.setText(unescapeHtml4(row.getString("title")));//convert html chars to string
                viewHolder.postContent.setText(unescapeHtml4(excerpt)); //convert html chars to string
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Return the completed view to render on screen
            return convertView;
        }
    }



}
