package com.justthetipband.christophertino.androidapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.OnInitializedListener;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerView;

import static com.justthetipband.christophertino.androidapp.YouTubeConstants.DEVELOPER_KEY;

/**
 * YouTube Video Activity
 *
 * @author christophertino
 * @since April 2015
 */
public class YouTubeVideoActivity extends YouTubeBaseActivity implements OnInitializedListener {
	private static final String TAG = "YouTubeVideoActivity";
	private static final int ERROR_DIALOG_SHOWN = 1;
	private String videoId;
	private YouTubePlayerView youTubeView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		//getWindow().setFlags(LayoutParams.FLAG_FULLSCREEN, LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.youtube_player_view);

		Intent intent = getIntent();
		if (intent.getExtras() != null) {
			videoId = intent.getStringExtra("videoId");
		}

		//initialize video player with developer key
		youTubeView = (YouTubePlayerView) findViewById(R.id.youtube_view);
		youTubeView.initialize(DEVELOPER_KEY, this);
	}

	@Override
	public void onInitializationFailure(Provider provider, YouTubeInitializationResult errorReason) {
		if (errorReason.isUserRecoverableError()) {
			errorReason.getErrorDialog(this, ERROR_DIALOG_SHOWN).show();
		} else {
			Log.e(TAG, errorReason.toString());
			Toast.makeText(this, errorReason.toString(), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onInitializationSuccess(Provider provider, YouTubePlayer player, boolean wasRestored) {
		if (!wasRestored) {
			player.loadVideo(videoId); //cueVideo() to load but not play

			//Hide player controls
			//player.setPlayerStyle(PlayerStyle.CHROMELESS);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ERROR_DIALOG_SHOWN) {
			//If the error dialog appears, re-initialize
			youTubeView.initialize(DEVELOPER_KEY, this);
		}
	}
}