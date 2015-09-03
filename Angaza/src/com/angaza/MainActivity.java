package com.angaza;

import java.util.Random;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.angaza.network.NetworkConnectionMonitor;
import com.angaza.network.NetworkUtils;
import com.angaza.network.OnNetworkStatusChangedListener;

public class MainActivity extends FragmentActivity 
	implements OnClickListener, OnNetworkStatusChangedListener{
	
	public static final String PERCENT = "pct";
	private static final String HAS_CLICKED = "hasClicked";
	private static final String TIME_START = "timeStart";
	private static final String TRUTH = "truth";
	private static final String TRUTH_FRAG_TAG = "truthFrag";
	
	private Button mTruthBtn;

	private boolean mHasClicked = false;
	private long mTimeStart = -1;
	private String mTruth = null;
	
	private static final int MSG_CHECK_AGAIN = 1;
	private static final long TIME_MAX = 10000;
	private static final long TIME_CHECK_AGAIN = 1;
	
	private boolean mFakedGet = false;//This wouldn't really be used in the long run
	private long mFakeTime;//This wouldn't really be used in the long run
	
	//This won't leak becuase all of the messages are removed in onStop
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case MSG_CHECK_AGAIN:
				checkForTruth();
				break;
			}
		}		
	};
	
	private AsyncTask<Void, Void, String> mApiTask;
	
	//Use one centralized network monitor for all that need connectivity status
	private NetworkConnectionMonitor mNetworkConnectionMonitor;
	private boolean mIsNetworkAvailable = false;
			
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mNetworkConnectionMonitor = new NetworkConnectionMonitor(this);
		
		//Don't reuse connections pre-Froyo
        NetworkUtils.disableConnectionReuseIfNecessary();
        
        //Listen for network status updates
        mNetworkConnectionMonitor.addOnNetworkStatusChangedListener(this);
		
		if(savedInstanceState != null){
			mHasClicked = savedInstanceState.getBoolean(HAS_CLICKED);
			mTimeStart = savedInstanceState.getLong(TIME_START);
			mTruth = savedInstanceState.getString(TRUTH);
		}
				
		if(mHasClicked){
			createTruthFragment();
			mHandler.sendEmptyMessage(MSG_CHECK_AGAIN);
		}else{
			mTruthBtn = (Button) findViewById(R.id.truth);	
			mTruthBtn.setOnClickListener(this);
		}
	}
	
	@Override
	protected void onPause() {
		/**
		 * Unregister or else the app will wake the device every time a broadcast is received,
		 * which is pretty frequent (this means the battery will drain faster because waking means showing screens)
		 */
		if(mNetworkConnectionMonitor != null){			
			mNetworkConnectionMonitor.onPause();
		}
		super.onPause();
	}
	
	@Override
	protected void onResume(){
		mNetworkConnectionMonitor.onResume();
		super.onResume();
	}


	@Override
	protected void onStop() {
		removeAllMessages();
		if(mApiTask != null){
			mApiTask.cancel(true);
			mApiTask = null;
		}
		super.onStop();
	}
		
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong(TIME_START, mTimeStart);
		outState.putBoolean(HAS_CLICKED, mHasClicked);
		outState.putString(TRUTH, mTruth);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.truth:
			if(!mHasClicked){
				mHasClicked = true;
				mTruthBtn.setOnClickListener(null);
				
				mTimeStart = System.currentTimeMillis();
				mTruth = null;
				
				createTruthFragment();
				
				//Change up the fake response time...this wouldn't be used in the real situation (see the note in ApiTask)
				mFakeTime = new Random().nextInt(mFakedGet ? 20000 : 8000);
				mFakedGet = !mFakedGet;
								
				//Start trying to get the truth
				mHandler.sendEmptyMessage(MSG_CHECK_AGAIN);
			}
			break;		
		}
	}
		
	private void createTruthFragment(){
		
		//First get the view and make it visible
		View fragHolder = findViewById(R.id.truth_frag_holder);
		fragHolder.setVisibility(View.VISIBLE);
	
		FragmentManager fragMan = getSupportFragmentManager();
		Fragment truthFrag = fragMan.findFragmentByTag(TRUTH_FRAG_TAG);
		
		if(truthFrag != null){
			fragMan.beginTransaction().remove(truthFrag).commit();
		}
		
		truthFrag = new TruthFragment();
		
		Bundle args = new Bundle();
		args.putFloat(PERCENT, calcPct(System.currentTimeMillis()));
		truthFrag.setArguments(args);
		
		getSupportFragmentManager().beginTransaction()
		.replace(R.id.truth_frag_holder, truthFrag, TRUTH_FRAG_TAG)
		.commit();	
	}
		
	public void requestMoreTruth(){		
		mTimeStart = System.currentTimeMillis();
		mTruth = null;
		mFakeTime = new Random().nextInt(mFakedGet ? 20000 : 8000);
		mFakedGet = !mFakedGet;
		mHandler.sendEmptyMessage(MSG_CHECK_AGAIN);
	}
		
	private void checkForTruth(){
		TruthFragment truth = ((TruthFragment)getSupportFragmentManager().findFragmentByTag(TRUTH_FRAG_TAG));
		if(mTruth != null){
			truth.setTruthContent(mTruth);
			truth.setWipePercent(1.0f);
			return;
		}
		
		float pct = calcPct(System.currentTimeMillis());		
		truth.setWipePercent(pct);
		
		if(pct < 1.0f && mIsNetworkAvailable){			
			if(mApiTask == null){
				//Since the truth hasn't been set and there is still some time to burn, try
				//to get the truth from online again
				mApiTask = new ApiTask();
				mApiTask.execute();
			}
			
			//And check again later if the max time limit hasn't been reached
			mHandler.sendEmptyMessageDelayed(MSG_CHECK_AGAIN, TIME_CHECK_AGAIN);	
		}else{
			removeAllMessages();
			
			if(mApiTask != null){
				//Times up! Too bad.
				mApiTask.cancel(true);
				mApiTask = null;
			}
			
			//Get our canned truth
			mTruth = getResources().getString(R.string.lorem_ipsum);
			truth.setTruthContent(mTruth);
			
			//If the network because unavailable all of a sudden, then animate 
			//the truth the rest of the way
			if(!mIsNetworkAvailable){
				truth.startWipeAnimation();
			}
		}
	}
	
	private float calcPct(long time){
		long dt = time - mTimeStart;
		float pct = dt / (TIME_MAX * 1.0f);
		return pct < 1.0f ? pct : 1.0f;
	}
	
	private void removeAllMessages(){
		mHandler.removeMessages(MSG_CHECK_AGAIN);
	}
	
	private class ApiTask extends AsyncTask<Void, Void, String>{
		
		private Handler handler = new Handler();
		private boolean hasResponded = false;
		private String truth = null;
		
		@Override
		protected String doInBackground(Void... params) {
			//XXX
			//Since I don't know how to use RESTful services outside of Google App Engine, I added
			//this to fake like data was being retrieved
			handler.postDelayed(new Runnable(){

				@Override
				public void run() {
					truth = "Does close only count in horse shoes and hand granades?";
					hasResponded = true;
				}
			
			}, mFakeTime);
			//XXX
			
			while(!hasResponded){
				//XXX
				//Keep making requests to http://api.acme.international/fortune
				/*try {							
					//Try to get the truths from online - I've only used GoogleAppEngine and this
					//is how I'd do it
					TruthModelCollection truthCollection = mApi.truth().getTruthModels().execute();
					List<TruthModel> truthModels = truthCollection.getItems();
					
					//Select a truth at random
					Random rand = new Random();
					int next = rand.nextInt(truthModels.size() - 1);
					truth = truthModels.get(next).getTruthString();
					
	    			hasResponded = true;
				} catch (IOException e) {
					Log.e("MainActivity", "TruthAPI error:" + e.getMessage());
				}*/
				//XXX
			}

			return truth;
		}

		@Override
		protected void onPostExecute(String result) {
			//The server responded before this task was destroyed so it means there's some
			//time left and so the wipe should be animated the rest of the way
			if(result != null){
				removeAllMessages();
				mTruth = result;
			    TruthFragment truthFrag = ((TruthFragment)getSupportFragmentManager().findFragmentByTag(TRUTH_FRAG_TAG));
		    	truthFrag.setTruthContent(mTruth);
		    	truthFrag.startWipeAnimation();
		    	
		    	mApiTask = null;
			}
		}	
	}

	@Override
	public void onNetworkStatusChanged(boolean isAvailable) {
		//If the network status changed...how do we react?
		mIsNetworkAvailable = isAvailable;
	}
}
