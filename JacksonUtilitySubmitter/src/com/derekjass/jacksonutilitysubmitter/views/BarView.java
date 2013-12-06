package com.derekjass.jacksonutilitysubmitter.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

public class BarView extends View {

	private float mScale;

	public BarView(Context context) {
		this(context, null);
	}

	public BarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		Animation anim = new ScaleAnimation(
				1f, 1f,
				0f, 0f,
				Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 1f);
		anim.setFillAfter(true);
		anim.setDuration(0);
		startAnimation(anim);
		mScale = 0f;
	}

	public void setScale(float scale) {
		Animation anim = new ScaleAnimation(
				1f, 1f,
				mScale, scale,
				Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 1f);
		anim.setFillAfter(true);
		anim.setDuration(400);
		anim.setInterpolator(getContext(),
				android.R.anim.accelerate_decelerate_interpolator);
		startAnimation(anim);
		mScale = scale;
	}

	public float getScale() {
		return mScale;
	}
}
