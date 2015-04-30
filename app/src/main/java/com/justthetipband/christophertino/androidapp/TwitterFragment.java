package com.justthetipband.christophertino.androidapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.Base64;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.justthetipband.christophertino.androidapp.TwitterConstants.CONSUMER_KEY;
import static com.justthetipband.christophertino.androidapp.TwitterConstants.CONSUMER_SECRET;
import static com.justthetipband.christophertino.androidapp.TwitterConstants.TWEET_COUNT;
import static com.justthetipband.christophertino.androidapp.TwitterConstants.TWITTER_HANDLE;

/**
 * Twitter Fragment
 *
 * @author christophertino
 * @since April 2015
 */
public class TwitterFragment extends ListFragment {
	private static final String TAG = "TwitterFragment";
	ArrayList<String> tweets;
	TwitterArrayAdapter itemsAdapter;
	private ProgressDialog pd;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		tweets = new ArrayList<>();
		itemsAdapter = new TwitterArrayAdapter(getActivity().getApplicationContext(), tweets);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View view = inflater.inflate(R.layout.fragment_list, container, false);

		ListView listView = (ListView) view.findViewById(android.R.id.list);
		listView.setAdapter(itemsAdapter);

		TextView fragmentTitle = (TextView) view.findViewById(R.id.fragment_title);
		fragmentTitle.setText("Twitter Feed");

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		try {
			if (tweets.isEmpty()) {
				try {
					beginOAUTH();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				itemsAdapter.notifyDataSetChanged();
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	//return the TwitterFragment to the MainActivity
	public static TwitterFragment newInstance(int fragmentIndex) {
		TwitterFragment fragment = new TwitterFragment();

		//Pass fragment index as argument
		Bundle args = new Bundle();
		args.putInt("fragmentIndex", fragmentIndex);
		fragment.setArguments(args);

		return fragment;
	}

	//Create OAUTH/2 workflow to and receive access token before
	public void beginOAUTH() throws JSONException {
		RequestParams requestParams = new RequestParams();
		requestParams.put("grant_type", "client_credentials");
		AsyncHttpClient httpClient = new AsyncHttpClient();
		httpClient.addHeader("Authorization", "Basic " + Base64.encodeToString((CONSUMER_KEY + ":" + CONSUMER_SECRET).getBytes(), Base64.NO_WRAP));
		httpClient.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
		httpClient.post("https://api.twitter.com/oauth2/token", requestParams, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				try {
					String accessToken = response.getString("token_type") + " " + response.getString("access_token");
					//pass access token to second leg OAUTH query
					getTwitterContent(accessToken);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				Log.e(TAG, "Error " + statusCode + " Response " + errorResponse);
			}
		});
	}

	public void getTwitterContent(String accessToken) throws JSONException {
		String url = "https://api.twitter.com/1.1/statuses/user_timeline.json?screen_name=" + TWITTER_HANDLE + "&count=" + TWEET_COUNT;
		AsyncHttpClient client = new AsyncHttpClient();
		client.addHeader("Authorization", accessToken);
		client.get(url, new JsonHttpResponseHandler() {
			@Override
			public void onStart() {
				pd = ProgressDialog.show(getActivity(), "Please Wait", "Downloading...", true);
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
				JSONObject row;
				for (int i = 0; i < timeline.length(); i++) {
					try {
						row = timeline.getJSONObject(i);
						tweets.add(row.getString("text"));
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				itemsAdapter.notifyDataSetChanged();
			}

			@Override
			public void onFinish() {
				pd.dismiss();
			}
		});
	}

	/*
	 * TwitterArrayAdapter
	 * Custom ArrayAdapter used to populate the fragment
	 * @param   String from getTwitterContent()
	 * @return  rendered content in twitter_fragment_list_item
	 */
	private static class TwitterArrayAdapter extends ArrayAdapter<String> {
		// View lookup cache
		private static class ViewHolder {
			TextView twitterContent;
		}

		public TwitterArrayAdapter(Context context, ArrayList<String> row) {
			super(context, 0, row);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// Get the data item for this position
			String row = getItem(position);
			ViewHolder viewHolder;

			// Check if an existing view is being reused, otherwise inflate the view
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.twitter_fragment_list_item, parent, false);
				viewHolder.twitterContent = (TextView) convertView.findViewById(R.id.tweet_text);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			viewHolder.twitterContent.setText(row);

			// Return the completed view to render on screen
			return convertView;
		}
	}
}