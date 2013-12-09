package com.derekjass.jacksonutilitysubmitter.views;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
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
		
		public float getScale() {
			return mScale;
		}

		@SuppressWarnings("deprecation")
		public void setColor(int color) {
			WeakReference<Drawable> ref = sBackgrounds.get(color);
			Drawable bg = ref != null ? sBackgrounds.get(color).get() : null;

			if (bg == null) {
				bg = getBackground().mutate();
				bg.setColorFilter(new LightingColorFilter(color, 0));
				sBackgrounds.put(color, new WeakReference<Drawable>(bg));
			}
			setBackgroundDrawable(bg);
		}
	}
	
	private static class Gridline {
		public PointF mStart;
		public PointF mEnd;
		public String mLabel;
		
		public Gridline(PointF start, PointF end, String label) {
			mStart = start;
			mEnd = end;
			mLabel = label;
		}
		
		public Gridline(float startX, float startY, float endX, float endY
				, String label) {
			mStart = new PointF(startX, startY);
			mEnd = new PointF(endX, endY);
			mLabel = label;
		}
		
		public void draw(Canvas canvas, Paint linePaint, Paint labelPaint) {
			canvas.drawLine(mStart.x, mStart.y, mEnd.x, mEnd.y, linePaint);
			labelPaint.setTextAlign(Align.RIGHT);
			canvas.drawText(mLabel, mStart.x, mStart.y, labelPaint);
		}
	}

	private static final String TAG = "BarGraph";
	private static final int TICK_SPACING_MIN_DP = 50;
	private static final int TICK_SPACING_MAX_DP = 100;
	
	private float mDensity;
	
	private int leftPadding =
			(int) (getResources().getDisplayMetrics().density * 20);
	private int bottomPadding =
			(int) (getResources().getDisplayMetrics().density * 20);

	private static Paint sLinePaint;

	private List<Integer> mValues;
	private List<String> mLabels;

	private Rect mViewBounds;
	private Rect mGraphBounds;
	private int mBarCount;
	private int mBarSpacing;
	private int mBarWidth;
	private int mBarColor;
	private int mMaxValue;
	private int mTickSpacing;
	static {
		sLinePaint = new Paint();
		sLinePaint.setColor(0xFF999999);
		sLinePaint.setStrokeWidth(2f);
	}

	public BarGraph(Context context) {
		this(context, null);
	}

	public BarGraph(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDensity = getResources().getDisplayMetrics().density;
		// TODO Custom attributes
		mValues = new ArrayList<Integer>();
		mLabels = new ArrayList<String>();
		mGraphBounds = new Rect();
		setBarCount(1);
		setMaxValue(1);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int minW = mBarCount * (mBarWidth + mBarSpacing * 2) + bottomPadding;
		int w = myResolveSizeAndState(minW, widthMeasureSpec, 0);

		int minH = leftPadding * 2;
		int h = myResolveSizeAndState(minH, heightMeasureSpec, 0);

		setMeasuredDimension(w, h);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mGraphBounds.set(0, 0, w, h);
		mViewBounds.set(0, 0, w, h);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		Log.i(TAG, "onLayout children: " + String.valueOf(getChildCount()));
		for (int i = 0; i < getChildCount(); i++) {
			int totalBarWidth = mBarWidth + mBarSpacing * 2;
			int left = leftPadding + mBarSpacing +
					i * totalBarWidth;
			int right = left + mBarWidth;
			getChildAt(i).layout(left, 0, right, getHeight() - bottomPadding);
		}

	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		Log.i(TAG, "dispatchDraw");
		super.dispatchDraw(canvas);
		canvas.drawLine(leftPadding, 0f,
				leftPadding, getHeight() - bottomPadding,
				sLinePaint);
		canvas.drawLine(leftPadding, getHeight() - bottomPadding,
				getWidth(), getHeight() - bottomPadding,
				sLinePaint);
	}

	public void setValues(ArrayList<Integer> values) {
		mValues = values;
		for (int i = 0; i < Math.min(mBarCount, values.size()); i++) {
			AnimatingBarView v = (AnimatingBarView) getChildAt(i);
			float scale = (float) values.get(i) / mMaxValue;
			if (v.mScale != scale) v.setScale(scale);
		}
	}

	public void setLabels(ArrayList<String> labels) {
		mLabels = labels;
		invalidate();
	}

	public void setBarCount(int num) {
		if (getChildCount() == num) {
			return;
		} else if (getChildCount() < num) {
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

	private void setTickSpacing() {
		
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
