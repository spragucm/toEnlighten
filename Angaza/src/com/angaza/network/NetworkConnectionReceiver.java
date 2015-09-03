package com.angaza.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * NetworkConnectionReceiver should not hold all the listeners and update info because it should always
 * be able to quickly receive the the broadcast and move on so other things aren't waiting on it.
 * So, it should simply set the vars, update its one listener and move on
 */
public class NetworkConnectionReceiver extends BroadcastReceiver{
	
	private boolean mIsWifiConnected;
	private boolean mIsMobileConnected;
	
	private OnWifiNetworkStatusChangedListener mOnWifiNetworkStatusChangedListener;
	private OnMobileNetworkStatusChangedListener mOnMobileNetworkStatusChangedListener;
	
	public NetworkConnectionReceiver(Context context){
		//Get the network state when the receiver is first created
		boolean[] wifiMobileNetStatus = NetworkUtils.getWifiAndMobileNetworkStatus(context);
		mIsWifiConnected = wifiMobileNetStatus[0];
		mIsMobileConnected = wifiMobileNetStatus[1];
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		//Determine the state of the wifi and mobile networks since something has changed
		boolean[] wifiMobileStatus = NetworkUtils.getWifiAndMobileNetworkStatus(context);
		
		//If the wifi network status has changed
		if(wifiMobileStatus[0] != mIsWifiConnected){
			mIsWifiConnected = wifiMobileStatus[0];
			
			//And inform the listener
			if(mOnWifiNetworkStatusChangedListener != null){
				mOnWifiNetworkStatusChangedListener.onWifiStatusChanged(mIsWifiConnected);
			}
		}
		
		//If the mobile network status has changed
		if(wifiMobileStatus[1] != mIsMobileConnected){
			mIsWifiConnected = wifiMobileStatus[0];
			
			//And inform the listener
			if(mOnMobileNetworkStatusChangedListener != null){
				mOnMobileNetworkStatusChangedListener.onMobileStatusChanged(mIsWifiConnected);
			}
		}
	}
	
	public void setOnWifiNetworkStatusChangedListener(OnWifiNetworkStatusChangedListener onStatusChangedListener){
		mOnWifiNetworkStatusChangedListener = onStatusChangedListener;
	}
	
	public void setOnMobileNetworkStatusChangedListener(OnMobileNetworkStatusChangedListener onStatusChangedListener){
		mOnMobileNetworkStatusChangedListener = onStatusChangedListener;
	}
	
	public boolean isWifiConnected(){
		return mIsWifiConnected;
	}
	
	public boolean isMobileConnected(){
		return mIsMobileConnected;
	}
}
