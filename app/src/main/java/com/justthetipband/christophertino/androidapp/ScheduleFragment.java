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
import com.loopj.android.http.SaxAsyncHttpResponseHandler;

import org.apache.http.Header;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

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
		Log.i(TAG, "Item clicked: " + id);
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
        final DataHandler handlerInstance = new DataHandler();
        client.get("http://www.justthetipband.com/?feed=gigpress", new SaxAsyncHttpResponseHandler<DefaultHandler>(handlerInstance) {
            private ArrayList<String> rssItems = new ArrayList<>();

	        @Override
	        public void onStart() {
		        pd = ProgressDialog.show(getActivity(), "Please Wait", "Downloading schedule dates...", true);
	        }

            @Override
            public void onSuccess(int statusCode, Header[] headers, DefaultHandler handler) {
                Log.v(TAG, "XML Fetch Success: " + statusCode);
                rssItems = handlerInstance.getDescriptions();

	            //Build ArrayAdapter and refresh view
	            ScheduleArrayAdapter itemsAdapter = new ScheduleArrayAdapter(getActivity().getApplicationContext(), rssItems);
	            listView.setAdapter(itemsAdapter);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, DefaultHandler handler) {
                Log.i(TAG, "XML Fetch Failure " + statusCode);
            }

	        @Override
	        public void onFinish() {
		        pd.dismiss();
	        }
        });
    }

    /*
     * DataHandler
     * Custom SAX event handler to parse RSS/ATOM feed
     */
    public static class DataHandler extends DefaultHandler {
        private static final String TAG = "DataHandler";
        private ArrayList<String> rssItems = new ArrayList<>();
	    private boolean parsingItem;
        private boolean parsingDescription;
        private StringBuffer currentDescriptionSB;

        public DataHandler() {
            super();
        }

        //simple getter method to call from SaxAsyncHttpResponseHandler
        public ArrayList<String> getDescriptions() {
            return rssItems;
        }

        public void startDocument () {
            Log.i(TAG, "Start of XML document");
        }

        public void endDocument () {
            Log.i(TAG, "End of XML document");
        }

        //create new StringBuffer for each <description> element
        public void startElement (String uri, String name, String qName, Attributes atts) {
	        if (qName.equals("item")) {
		        parsingItem = true; //flag here to handle nested <item><description> tag
	        }
            if (parsingItem && qName.equals("description")) {
                parsingDescription = true; //set flag here to use in characters() method
                currentDescriptionSB = new StringBuffer();
            }
        }

        //add description content to StringBuffer
        public void characters (char ch[], int start, int length) {
            if (parsingDescription) {
                currentDescriptionSB.append(new String(ch, start, length));
            }
        }

	    //add currentDescription to ArrayList
	    public void endElement (String uri, String name, String qName) {
		    if (parsingItem && qName.equals("description")) {
			    parsingItem = false;
			    parsingDescription = false;

			    if (currentDescriptionSB != null) {
				    rssItems.add(currentDescriptionSB.toString());
			    }
		    }
	    }
    }

	/*
	 * ScheduleArrayAdapter
	 * Custom ArrayAdapter used to populate the fragment
	 * @param   String from getRSSContent()
	 * @return  rendered content in schedule_fragment_list_item
	 */
	private static class ScheduleArrayAdapter extends ArrayAdapter<String> {
		// View lookup cache
		private static class ViewHolder {
			TextView scheduleContent;
		}

		public ScheduleArrayAdapter(Context context, ArrayList<String> row) {
			super(context, 0, row);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// Get the data item for this position
			String row = getItem(position);

			// Using ViewHolder pattern to cache findViewById() recurrences
			// https://github.com/codepath/android_guides/wiki/Using-an-ArrayAdapter-with-ListView#improving-performance-with-the-viewholder-pattern
			ViewHolder viewHolder;

			// Check if an existing view is being reused, otherwise inflate the view
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.schedule_fragment_list_item, parent, false);
				viewHolder.scheduleContent = (TextView) convertView.findViewById(R.id.scheduleItem);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			//Use jSoup to break HTML into useable string data
			StringBuilder output = new StringBuilder();
			Document htmlDoc = Jsoup.parse(row);
			output.append(htmlDoc.select("ul li:eq(4) strong").text());
			output.append(" ");
			output.append(htmlDoc.select("ul li:eq(4) a").text());
			output.append("\n");
			output.append(htmlDoc.select("ul li:eq(1)").text());
			output.append("\n");
			output.append(htmlDoc.select("ul li:eq(2)").text());
			output.append("\n");
			output.append(htmlDoc.select("ul li:eq(5) strong").text());
			output.append(" ");
			output.append(htmlDoc.select("ul li:eq(5) a").text());
			output.append("\n");
			output.append(htmlDoc.select("ul li:eq(3)").text());

			viewHolder.scheduleContent.setText(output.toString());

			// Return the completed view to render on screen
			return convertView;
		}
	}
}
