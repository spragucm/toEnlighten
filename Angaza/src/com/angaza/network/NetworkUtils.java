package com.angaza.network;

import java.util.List;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

public class NetworkUtils {
   
    /**
     * Note that you should not base decisions on whether a network is "available."
     * You should always check isConnected() before performing network operations, 
     * since isConnected() handles cases like flaky mobile networks, airplane mode,
     * and restricted background data.
     * 
     * Determines the state of the wifi and mobile networks. Returns
     * a boolean where boolean[0] = isWifiNetworkConnected
     * and where       boolean[1] = isMobileNetworkConnected 
     */
    public static boolean[] getWifiAndMobileNetworkStatus(Context context){
    	boolean[] wifiMobileStatus = new boolean[2];
    	
    	//Determine the state of the networks
		ConnectivityManager connMgr = 
				(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    	
    	//Determine if wifi is connected
    	NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    	if(networkInfo != null){
    		wifiMobileStatus[0] = networkInfo.isConnected();
    	}else{
    		//Means this type of network is not supported by the device
    		wifiMobileStatus[0] = false;
    	}
    	
    	//Determine if mobile network is connected
    	networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
    	if(networkInfo != null){
    		wifiMobileStatus[1] = networkInfo.isConnected();
    	}else{
    		//Means this type of network is not supported by the device
    		wifiMobileStatus[1] = false;
    	}
        	
    	return wifiMobileStatus;
    }
    
    /**
     * Gets the default network preference whether it is wifi or mobile. Returns
     * a boolean where boolean[0] = isWifiNetworkDefault
     * and where       boolean[1] = isMobileNetworkDefault
     * 
     * If no networks are connected this will return false for both since there's
     * no preferred route that is active.
     * 
     * @param context
     */
    public static boolean[] getDefaultNetwork(Context context){
    	boolean[] wifiMobileDefaults = new boolean[2];
    
    	//Determine the state of the networks
		ConnectivityManager connMgr = 
				(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
        	//If something is connected then determine which is the default route
            wifiMobileDefaults[0] = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            wifiMobileDefaults[1] = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
        } else {
        	//No networks are connected and hence neither are preferred
        	wifiMobileDefaults[0] = false;
        	wifiMobileDefaults[1] = false;
        }
        
        return wifiMobileDefaults;
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager connMgr = 
        		(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        
        return (networkInfo != null && networkInfo.isConnected());//can also use networkInfo.isConnectedOrConnecting()
    } 
    
    /**
     * Workaround for bug pre-Froyo, see here for more info:
     * http://android-developers.blogspot.com/2011/09/androids-http-clients.html
     */
    public static void disableConnectionReuseIfNecessary() {
        // HTTP connection reuse which was buggy pre-froyo
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        } 
    }  
    
    /**
     * Determine if any of the filepaths need network connectivity.
     * @param filepaths A list of absolute filepaths
     * @return True if any file needs network connectivity to be retrieved
     */
    public static boolean hasNetworkFiles(List<String> filePaths){
    	if(filePaths == null){
    		return false;
    	}
    	
    	String[] array = new String[filePaths.size()];
    	filePaths.toArray(array);
    	
    	return hasNetworkFiles(array);
    }
    
    public static boolean hasNetworkFiles(String[] filePaths){
    	if(filePaths == null){
    		return false;
    	}
    	
    	for(int i = 0; i < filePaths.length; i++){
    		if(isNetworkFile(filePaths[i])){
    			return true;
    		}
    	}
    	return false;
    }
    
    /**
     * Determine whether or not a filepath needs network connectivity.
     * @param filepath The absolute filepath
     * @return True if any file needs network connectivity to be retrieved
     */
    public static boolean isNetworkFile(String filepath){
    	if(filepath.startsWith("http")){
    		return true;
    	}
    	return false;
    }
}
