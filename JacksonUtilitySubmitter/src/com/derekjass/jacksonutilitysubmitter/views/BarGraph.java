package com.derekjass.jacksonutilitysubmitter.views;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

import com.derekjass.jacksonutilitysubmitter.R;

public class BarGraph extends ViewGroup {

	private static class AnimatingBarView extends View {

		private float mScale;
		private static SparseArray<WeakReference<Drawable>> sBackgrounds =
				new SparseArray<WeakReference<Drawable>>();

		public AnimatingBarView(Context context) {
			this(context, null);
		}

		public AnimatingBarView(Context context, AttributeSet attrs) {
			super(context, attrs);
			setBackgroundResource(R.drawable.bar);
			Animation anim = new ScaleAnimation(
					1f, 1f,
					0f, 0f,
					Animation.RELATIVE_TO_SELF, 0f,
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
					Animation.RELATIVE_TO_SELF, 0f,
					Animation.RELATIVE_TO_SELF, 1f);
			anim.setFillAfter(true);
			anim.setDuration(400);
			anim.setInterpolator(getContext(),
					android.R.anim.accelerate_decelerate_interpolator);
			startAnimation(anim);
			mScale = scale;
		}

		@SuppressWarnings("deprecation")
		public void setColor(int color) {
			WeakReference<Drawable> ref = sBackgrounds.get(color);
			Drawable bg = ref != null ? sBackgrounds.get(color).get() : null;

			if (bg == null) {
				bg = getBackground().mutate();
				bg.setColorFilter(new LightingColorFilter(color, 0));
			}
			setBackgroundDrawable(bg);
		}
	}

	private int tempXLabelSize =
			(int) (getResources().getDisplayMetrics().density * 20);
	private int tempYLabelSize =
			(int) (getResources().getDisplayMetrics().density * 20);

	private static Paint linePaint;

	private List<Integer> mValues;
	private List<String> mLabels;

	private int mBarCount;
	private int mBarSpacing;
	private int mBarWidth;
	private int mBarColor;
	private int mMaxValue;
	private int mTickMinSpacing;
	private int mTickResolution;

	static {
		linePaint = new Paint();
		linePaint.setColor(0xFF777777);
	}

	public BarGraph(Context context) {
		this(context, null);
	}

	public BarGraph(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Custom attributes
		mValues = new ArrayList<Integer>();
		mLabels = new ArrayList<String>();
		setBarCount(1);
		setMaxValue(1);
		setTickMinSpacing(40);
		setBarSpacing(20);

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int minW = mBarCount * (mBarWidth + mBarSpacing * 2) + tempYLabelSize;
		int w = myResolveSizeAndState(minW, widthMeasureSpec, 0);

		int minH = tempXLabelSize * 2;
		int h = myResolveSizeAndState(minH, heightMeasureSpec, 0);

		setMeasuredDimension(w, h);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		for (int i = 0; i < getChildCount(); i++) {
			int totalBarWidth = mBarWidth + mBarSpacing * 2;
			int left = l + tempYLabelSize + mBarSpacing +
					i * totalBarWidth;
			int right = r + tempYLabelSize + mBarSpacing +
					mBarWidth + i * totalBarWidth;

			getChildAt(i).layout(left, t, right, b - tempXLabelSize);
		}

	}

	@Override
	protected void onDraw(Canvas canvas) {
		Log.i("BarGraph", "onDraw");
		super.onDraw(canvas);
		canvas.drawLine(tempYLabelSize, 0f,
				tempYLabelSize, getHeight() - tempXLabelSize,
				linePaint);
		// TODO Set bar values
	}

	public void setValues(ArrayList<Integer> values) {
		mValues = values;
		for (int i = 0; i < Math.min(mBarCount, values.size()); i++) {
			((AnimatingBarView) getChildAt(i)).setScale(
					values.get(i) / mMaxValue);
		}
	}

	public void setLabels(ArrayList<String> labels) {
		mLabels = labels;
		invalidate();
	}

	public void setBarCount(int num) {
		if (num == getChildCount()) {
			return;
		} else if (num < getChildCount()) {
			while (getChildCount() < num) {
				AnimatingBarView v = new AnimatingBarView(getContext());
				v.setColor(mBarColor);
				addView(v);
			}
		} else {
			while (getChildCount() > num) {
				removeViewAt(0);
			}
		}
		mBarCount = num;
		requestLayout();
		invalidate();
	}

	public void setBarSpacing(int pixels) {
		mBarSpacing = pixels;
		requestLayout();
		invalidate();
	}

	public void setBarWidth(int pixels) {
		mBarWidth = pixels;
		requestLayout();
		invalidate();
	}

	public void setBarColor(int color) {
		for (int i = 0; i < mBarCount; i++) {
			((AnimatingBarView) getChildAt(i)).setColor(color);
		}
		mBarColor = color;
		invalidate();
	}

	public void setMaxValue(int max) {
		mMaxValue = max;
		requestLayout();
		invalidate();
	}

	public void setTickMinSpacing(int pixels) {
		mTickMinSpacing = pixels;
		invalidate();
	}

	public void setTickResolution(int modulo) {
		mTickResolution = modulo;
		invalidate();
	}

	private static int myResolveSizeAndState(int size, int measureSpec,
			int childMeasuredState) {
		int result = size;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize =  MeasureSpec.getSize(measureSpec);
		switch (specMode) {
		case MeasureSpec.UNSPECIFIED:
			result = size;
			break;
		case MeasureSpec.AT_MOST:
			if (specSize < size) {
				result = specSize | MEASURED_STATE_TOO_SMALL;
			} else {
				result = size;
			}
			break;
		case MeasureSpec.EXACTLY:
			result = specSize;
			break;
		}
		return result | (childMeasuredState & MEASURED_STATE_MASK);
	}
}
