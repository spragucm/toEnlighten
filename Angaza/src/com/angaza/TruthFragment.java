package com.angaza;

import java.util.Random;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

public class TruthFragment extends Fragment
	implements OnClickListener{
	
	private static final int ANIM_TIME = 500;//ms
	
	private ObjectAnimator mWipeAnimator;
	private TextView mTruthContent;
	private TextView mMoreTruth;
	private WipeDrawable mBg;
	private String mTruth = null;
	private boolean mIsAnimating = false;
	
	@SuppressWarnings("deprecation")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		float pct = 0.0f;
		Bundle args = getArguments();
		if(args != null){
			pct = args.getFloat(MainActivity.PERCENT);
		}
		
		View rootView = inflater.inflate(R.layout.fragment_truth, container, false);	
		
		mTruthContent = (TextView) rootView.findViewById(R.id.truth_content);
		mTruthContent.setMovementMethod(new ScrollingMovementMethod());
		mTruthContent.setText(mTruth);
		
		mMoreTruth = (TextView) rootView.findViewById(R.id.more_truth);
		mMoreTruth.setOnClickListener(this);
		
		mBg = new WipeDrawable();
		Random rand = new Random();
		mBg.setColor(Color.rgb(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)));
		setWipePercent(pct);
		rootView.setBackgroundDrawable(mBg);		
			
		return rootView;
	}
	
	@Override
	public void onPause() {
		if(mIsAnimating){
			stopWipeAnimation();
			setWipePercent(1.0f);
		}
		super.onPause();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.more_truth:
			Random rand = new Random();
			mBg.setColor(Color.rgb(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)));
			((MainActivity)getActivity()).requestMoreTruth();
			break;
		}
	}	
	
	public void setWipePercent(float percent){
		mBg.setPercent(percent);
		int vis = View.INVISIBLE;
		if(percent >= 1.0f){
			vis = View.VISIBLE;
		}
		mTruthContent.setVisibility(vis);
		mMoreTruth.setVisibility(vis);
	}
	
	public void setTruthContent(String content){
		mTruth = content;
		if(mTruthContent != null){
			mTruthContent.setText(mTruth);
		}
	}
	
	public void startWipeAnimation(){
		stopWipeAnimation();
		mIsAnimating = true;
		
		float pct = mBg.getPercent();
		
		//Interpolate the animation time so that it appears to be the same rate
		//regardless of when it was started
		long duration = (long) (ANIM_TIME - pct * ANIM_TIME);		
		mWipeAnimator = ObjectAnimator.ofFloat(this, "wipePercent", pct, 1.0f);
		mWipeAnimator.setDuration(duration);
		mWipeAnimator.setInterpolator(new LinearInterpolator());
		mWipeAnimator.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {}
			
			@Override
			public void onAnimationRepeat(Animator animation) {}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				stopWipeAnimation();
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {}
		});
		mWipeAnimator.start();
	}
	
	public void stopWipeAnimation(){
		if(mWipeAnimator != null){
			mWipeAnimator.removeAllListeners();
			mWipeAnimator.cancel();
			mWipeAnimator = null;
		}
		mIsAnimating = false;
	}
}
