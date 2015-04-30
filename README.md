# Just the Tip Band Android App

Uses FragmentPagerAdapter to asynchronously load four external content feeds:

+ WordPress blog posts over JSON API
+ GigPress RSS feed via SAX parser
+ YouTube Playlist using YouTube Data API v3
+ Twitter Timeline using Twitter OAUTH/2

## Building

This project was built using Android Studio v1.1. Refer to build.gradle for Android versioning.

## Configuration

Visit https://apps.twitter.com/ to create a new app and get your API keys. Update TwitterConstants.java as follows:

+ CONSUMER_KEY = "XXXXXXXXXX";
+ CONSUMER_SECRET = "XXXXXXXXXX";
+ TWITTER_HANDLE = "justthetipband";
+ TWEET_COUNT = "10"

Visit https://console.developers.google.com/ and create a new project.

+ From APIs & Auth menu, click APIs. In the list of APIs, make sure the status is ON for the YouTube Data API v3.
+ From APIs & Auth menu, click Credentials.
	+ Under Public API Access, click Create New Key.
	+ Select Server Key and hit Create, leaving IP field blank.
+ Update YouTubeConstants.java
	+ DEVELOPER_KEY = "XXXXXXXXXX"; //API key from developer console
	+ PLAYLIST_ID = "XXXXXXXXXX"; //ID of the playlist you want to show in the app

## Dependencies

+ Loopj HTTP Client (https://github.com/loopj/android-async-http)
+ Apache Commons Lang (http://commons.apache.org/proper/commons-lang/)
+ jsoup: Java HTML Parser (http://jsoup.org/)
+ YouTube Android Player API (https://developers.google.com/youtube/android/player/)

## License

[MIT License](http://opensource.org/licenses/MIT)