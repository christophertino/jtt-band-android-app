package com.justthetipband.christophertino.androidapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.justthetipband.christophertino.androidapp.YouTubeConstants.DEVELOPER_KEY;
import static com.justthetipband.christophertino.androidapp.YouTubeConstants.PLAYLIST_ID;

/**
 * YouTube Fragment
 *
 * @author christophertino
 * @since Apr 2015
 */
public class YouTubeFragment extends ListFragment {
	private static final String TAG = "YouTubeFragment";
	private ListView listView;
	private ProgressDialog pd;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override //use onCreateView to inflate our layout fragment so that we can access views within
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		//We inflate the fragment with a single ListView element, because ListFragment must find a ListView with @android:id/list
		//Below in onActivityCreated -> SimpleCursorAdapter we bind to R.layout.list_item
		View view = inflater.inflate(R.layout.fragment_list, container, false);

		//set our listview element here because we need access to the view parameter
		listView = (ListView) view.findViewById(android.R.id.list);

		//set the title of the fragment
		TextView fragmentTitle = (TextView) view.findViewById(R.id.fragment_title);
		fragmentTitle.setText("Video Feed");

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		try {
			getYouTubeVideos();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	//return the YouTubeFragment to the MainActivity
	public static YouTubeFragment newInstance(int fragmentIndex) {
		YouTubeFragment fragment = new YouTubeFragment();

		//Pass fragment index as argument
		Bundle args = new Bundle();
		args.putInt("fragmentIndex", fragmentIndex);
		fragment.setArguments(args);

		return fragment;
	}

	private void getYouTubeVideos() throws JSONException {
		String url = "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&playlistId=" + PLAYLIST_ID + "&key=" + DEVELOPER_KEY;
		AsyncHttpClient client = new AsyncHttpClient();
		client.get(url, new JsonHttpResponseHandler() {
			@Override
			public void onStart() {
				pd = ProgressDialog.show(getActivity(), "Please Wait", "Downloading videos...", true);
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
				JSONObject row = null;
				ArrayList<JSONObject> videoList = new ArrayList<>();

				for (int i = 0; i < response.length(); i++) {
					//get each row as JSON object
					try {
						row = response.getJSONObject(i);
					} catch (JSONException e) {
						e.printStackTrace(System.out);
					}

					//add JSON to arrayList
					if (row != null) {
						videoList.add(row);
					}
				}

				//Build ArrayAdapter and refresh view
				VideoArrayAdapter itemsAdapter = new VideoArrayAdapter(getActivity().getApplicationContext(), videoList);
				listView.setAdapter(itemsAdapter);
			}

			@Override
			public void onFinish() {
				pd.dismiss();
			}
		});
	}

	/*
	 * VideoArrayAdapter
	 * Custom ArrayAdapter used to populate the fragment
	 * @param   JSONObject from getYouTubeVideos()
	 * @return  rendered content in youtube_fragment_list_item
	 */
	private static class VideoArrayAdapter extends ArrayAdapter<JSONObject> {
		// View lookup cache
		private static class ViewHolder {
			TextView videoTitle;
		}

		public VideoArrayAdapter(Context context, ArrayList<JSONObject> row) {
			super(context, 0, row);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// Get the data item for this position
			JSONObject row = getItem(position);

			ViewHolder viewHolder;

			// Check if an existing view is being reused, otherwise inflate the view
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.youtube_fragment_list_item, parent, false);
				viewHolder.videoTitle = (TextView) convertView.findViewById(R.id.videoTitle);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			// Populate the data into the template view using the data object
			try {
				viewHolder.videoTitle.setText(row.getJSONObject("snippet").getString("title"));
			} catch (JSONException e) {
				e.printStackTrace();
			}

			// Return the completed view to render on screen
			return convertView;
		}
	}
}
