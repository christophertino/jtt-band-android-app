package com.justthetipband.christophertino.androidapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
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
	private ArrayList<Video> videoList;
	private VideoArrayAdapter itemsAdapter;
	private ProgressDialog pd;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		videoList = new ArrayList<>();
		itemsAdapter = new VideoArrayAdapter(getActivity().getApplicationContext(), videoList);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View view = inflater.inflate(R.layout.fragment_list, container, false);

		ListView listView = (ListView) view.findViewById(android.R.id.list);
		listView.setAdapter(itemsAdapter);

		TextView fragmentTitle = (TextView) view.findViewById(R.id.fragment_title);
		fragmentTitle.setText("Latest Videos");

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		try {
			if (videoList.isEmpty()) {
				try {
					getYouTubeVideos();
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

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		//Get the videoId
		Video thisVideo = videoList.get(position);
		String videoId = thisVideo.getVideoId();

		//Play video in new Activity
		Intent intent = new Intent(getActivity(), YouTubeVideoActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //don't create another instance of the activity if its already in the stack
		intent.putExtra("videoId", videoId);
		startActivity(intent);

		/*Intent intent = new Intent(Intent.ACTION_VIEW);
		try{
			intent.setData(Uri.parse("vnd.youtube:" + videoId)); //open in youtube app
			startActivity(intent);
		} catch (ActivityNotFoundException ex){
			intent.setData(Uri.parse("http://www.youtube.com/watch?v=" + videoId)); //fallback to browser
			startActivity(intent);
		}*/
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
		String url = "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults=10&playlistId=" + PLAYLIST_ID + "&key=" + DEVELOPER_KEY;
		AsyncHttpClient client = new AsyncHttpClient();
		client.get(url, new JsonHttpResponseHandler() {
			@Override
			public void onStart() {
				pd = ProgressDialog.show(getActivity(), "Please Wait", "Downloading...", true);
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				JSONArray items = null;
				try {
					items  = response.getJSONArray("items");
					if (items != null) {
						for (int i = 0; i < items.length(); i++) {
							JSONObject objectData = items.getJSONObject(i);
							String title = objectData.getJSONObject("snippet").getString("title");
							String thumbUrl = objectData.getJSONObject("snippet").getJSONObject("thumbnails").getJSONObject("medium").getString("url");
							String videoId = objectData.getJSONObject("snippet").getJSONObject("resourceId").getString("videoId");
							videoList.add(new Video(title, thumbUrl, videoId));
						}
					} else {
						Log.v(TAG, "No video results");
					}
				} catch (JSONException e) {
					e.printStackTrace();
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
	 * Video Class
	 * Basic model for videos returned from YouTube Data API
	 */
	public static class Video {
		private String title;
		private String thumbUrl;
		private String videoId;

		public Video(String title, String thumbUrl, String videoId) {
			this.title = title;
			this.thumbUrl = thumbUrl;
			this.videoId = videoId;
		}

		public String getTitle(){
			return title;
		}

		public String getVideoId() {
			return videoId;
		}

		public String getThumbUrl() {
			return thumbUrl;
		}
	}

	/*
	 * VideoArrayAdapter
	 * Custom ArrayAdapter used to populate the fragment
	 * @param   Video Object from getYouTubeVideos()
	 * @return  rendered content in youtube_fragment_list_item
	 */
	private static class VideoArrayAdapter extends ArrayAdapter<Video> {
		// View lookup cache
		private static class ViewHolder {
			TextView videoTitle;
			ImageView videoThumbnail;
		}

		public VideoArrayAdapter(Context context, ArrayList<Video> row) {
			super(context, 0, row);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// Get the data item for this position
			Video row = getItem(position);

			ViewHolder viewHolder;

			// Check if an existing view is being reused, otherwise inflate the view
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.youtube_fragment_list_item, parent, false);
				viewHolder.videoTitle = (TextView) convertView.findViewById(R.id.videoTitle);
				viewHolder.videoThumbnail = (ImageView) convertView.findViewById(R.id.videoThumbnail);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			viewHolder.videoTitle.setText(row.getTitle());
			new LoadImagesTask(viewHolder.videoThumbnail).execute(row.getThumbUrl());

			// Return the completed view to render on screen
			return convertView;
		}
	}

	/*
	 * LoadImagesTask
	 * Custom AsyncTask to download and load YouTube thumbnails
	 * without locking up the UI
	 */
	private static class LoadImagesTask extends AsyncTask<String, Void, Bitmap> {
		ImageView videoThumbnail;

		public LoadImagesTask(ImageView videoThumbnail) {
			this.videoThumbnail = videoThumbnail;
		}

		protected Bitmap doInBackground(String... urls) {
			String thumbUrl = urls[0];
			Bitmap thumbnail = null;
			try {
				InputStream imageStream = new URL(thumbUrl).openStream();
				thumbnail = BitmapFactory.decodeStream(imageStream);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return thumbnail;
		}

		protected void onPostExecute(Bitmap result) {
			videoThumbnail.setImageBitmap(result);
		}
	}
}
