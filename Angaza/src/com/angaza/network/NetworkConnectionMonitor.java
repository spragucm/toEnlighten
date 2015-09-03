package com.angaza.network;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.util.Log;

public class NetworkConnectionMonitor
	implements OnWifiNetworkStatusChangedListener, OnMobileNetworkStatusChangedListener{

	// TODO The user's current network preference setting. Don't store this here
	public static String sPref = null;
	
	private Context mContext;
	
	//The BroadcastReceiver that receives callbacks when the network status changes
	private NetworkConnectionReceiver mNetworkConnectionReceiver;		
	
	//Settings for monitoring mobile network and wifi status.
	private boolean mIsWifiNetworkAvailable = false;
	private boolean mIsMobileNetworkAvailable = false;
	private boolean mIsNetworkAvailable = false;//true if either networks are available
	
	//Notify the following listeners when the different network status changes
	private List<OnWifiNetworkStatusChangedListener> onWifiNetworkStatusChangedListeners;
	private List<OnMobileNetworkStatusChangedListener> onMobileNetworkStatusChangedListeners;
	private List<OnNetworkStatusChangedListener> onNetworkStatusChangedListeners;
	
	public NetworkConnectionMonitor(Context context){
		mContext = context;
	}
	
	public void onResume(){
		if(mNetworkConnectionReceiver == null){
			mNetworkConnectionReceiver = new NetworkConnectionReceiver(mContext);
		}
		
		//Get the initial status
		mIsWifiNetworkAvailable = mNetworkConnectionReceiver.isWifiConnected();
		mIsMobileNetworkAvailable = mNetworkConnectionReceiver.isMobileConnected();
		setNetworkAvailable();
		
		Log.d("NCM","NCM onResume wifi:"+mIsWifiNetworkAvailable+",mobile:"+mIsMobileNetworkAvailable);
		
		//Register to receive callbacks when the network settings change
		mNetworkConnectionReceiver.setOnWifiNetworkStatusChangedListener(this);
		mNetworkConnectionReceiver.setOnMobileNetworkStatusChangedListener(this);
		
		//Register the receiver so it receives callbacks when networks change
		mContext.registerReceiver(mNetworkConnectionReceiver,
				new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
	}
	
	/**
	 * Determine if either network is available. If the availability status changed then
	 * notify the listeners
	 * @return
	 */
	private void setNetworkAvailable(){
		boolean availabilityChanged = false;
		
		if(mIsWifiNetworkAvailable || mIsMobileNetworkAvailable){
			if(mIsNetworkAvailable != true){
				availabilityChanged = true;
			}
			mIsNetworkAvailable = true;
		}else{
			if(mIsNetworkAvailable != false){
				availabilityChanged = true;
			}
			mIsNetworkAvailable = false;
		}	
		
		Log.d("NCM","NCM onNetCh:"+availabilityChanged+",wifi:"+mIsWifiNetworkAvailable+",mobile:"+mIsMobileNetworkAvailable);
		if(availabilityChanged && onNetworkStatusChangedListeners != null){
			for(int i = 0; i < onNetworkStatusChangedListeners.size(); i++){
				onNetworkStatusChangedListeners.get(i).onNetworkStatusChanged(mIsNetworkAvailable);
			}
		}
	}
	
	/**
	 * Unregister or else the app will wake the device every time a broadcast is received,
	 * which is pretty frequent (and note that this means your app will drain the battery
	 * faster because waking means showing screens)
	 * @param context
	 */
	public void onPause(){
		if(mNetworkConnectionReceiver != null){
			mContext.unregisterReceiver(mNetworkConnectionReceiver);
		}
	}		
	
	public boolean isWifiNetworkAvailable() {
		return mIsWifiNetworkAvailable;
	}
	
	public boolean isMobileNetworkAvailable() {
		return mIsMobileNetworkAvailable;
	}
	
	public boolean isNetworkAvailable() {
		return mIsMobileNetworkAvailable || mIsWifiNetworkAvailable;
	}
	
	public void addOnWifiNetworkStatusChangedListener(
			OnWifiNetworkStatusChangedListener onStatusChangedListener) {
		
		if(onWifiNetworkStatusChangedListeners == null){
			onWifiNetworkStatusChangedListeners = new ArrayList<OnWifiNetworkStatusChangedListener>();
		}
		onWifiNetworkStatusChangedListeners.add(onStatusChangedListener);		
	}
	
	public void addOnMobileNetworkStausChangedListener(
			OnMobileNetworkStatusChangedListener onStatusChangedListener) {
	
		if(onMobileNetworkStatusChangedListeners == null){
			onMobileNetworkStatusChangedListeners = new ArrayList<OnMobileNetworkStatusChangedListener>();
		}
		onMobileNetworkStatusChangedListeners.add(onStatusChangedListener);		
	}
	
	public void addOnNetworkStatusChangedListener(
			OnNetworkStatusChangedListener onStatusChangedListener) {
	
		if(onNetworkStatusChangedListeners == null){
			onNetworkStatusChangedListeners = new ArrayList<OnNetworkStatusChangedListener>();
		}
		onNetworkStatusChangedListeners.add(onStatusChangedListener);		
	}
	
	@Override
	public void onWifiStatusChanged(boolean isAvailable) {
		mIsWifiNetworkAvailable = isAvailable;
		
		setNetworkAvailable();
		
		//Notify all listeners that the status has changed
		if(onWifiNetworkStatusChangedListeners != null){
			for(int i = 0; i < onWifiNetworkStatusChangedListeners.size(); i++){
				onWifiNetworkStatusChangedListeners.get(i).onWifiStatusChanged(mIsWifiNetworkAvailable);
			}			
		}
		
		Log.d("NCM","NCM onWifiChanged wifi:"+mIsWifiNetworkAvailable+",mobile:"+mIsMobileNetworkAvailable);
		
	}
	
	@Override
	public void onMobileStatusChanged(boolean isAvailable) {
		
		mIsMobileNetworkAvailable = isAvailable;
		
		setNetworkAvailable();
		
		//Notify all listeners that the status has changed
		if(onMobileNetworkStatusChangedListeners != null){
			for(int i = 0; i < onMobileNetworkStatusChangedListeners.size(); i++){
				onMobileNetworkStatusChangedListeners.get(i).onMobileStatusChanged(mIsMobileNetworkAvailable);
			}	
		}
	}
}
