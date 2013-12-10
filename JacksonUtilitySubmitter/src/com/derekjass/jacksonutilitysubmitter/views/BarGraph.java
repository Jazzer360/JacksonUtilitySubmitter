package com.derekjass.jacksonutilitysubmitter.views;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PointF;
import android.graphics.RectF;
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

		public Gridline(float startX, float startY, float endX, float endY,
				String label) {
			mStart = new PointF(startX, startY);
			mEnd = new PointF(endX, endY);
			mLabel = label;
		}

		public void draw(Canvas canvas, Paint linePaint, Paint labelPaint) {
			canvas.drawLine(mStart.x, mStart.y, mEnd.x, mEnd.y, linePaint);
			canvas.drawText(mLabel, mStart.x, mStart.y, labelPaint);
		}
	}

	private static final String TAG = "BarGraph";
	private static final int GRID_SPACING_DP = 50;
	private static final int BAR_WIDTH_DP = 40;
	private static final int BAR_SPACING_DP = 8;
	private static final int GRIDLINE_PROTRUSION_DP = 5;

	private float mDensity;

	private int leftPadding =
			(int) (getResources().getDisplayMetrics().density * 20);
	private int bottomPadding =
			(int) (getResources().getDisplayMetrics().density * 20);

	private static Paint sAxisPaint;
	private static Paint sGridlinePaint;
	private static Paint sGridlineTextPaint;
	private static Paint sLabelPaint;

	private List<Integer> mValues;
	private List<String> mLabels;
	private List<Gridline> mGridlines;

	private RectF mViewBounds;
	private RectF mGraphBounds;
	private int mBarCount;
	private int mBarWidth;
	private int mBarColor;
	private int mMaxValue;

	static {
		sAxisPaint = new Paint();
		sAxisPaint.setColor(0xFF444444);
		sAxisPaint.setStrokeWidth(2f);

		sGridlinePaint = new Paint();
		sGridlinePaint.setColor(0xFF888888);
		sGridlinePaint.setStrokeWidth(1f);

		sGridlineTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		sGridlineTextPaint.setColor(0xFF888888);
		sGridlineTextPaint.setTextSize(8f);
		sGridlineTextPaint.setTextAlign(Align.RIGHT);

		sLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		sLabelPaint.setColor(0xFF888888);
		sLabelPaint.setTextSize(10f);
		sLabelPaint.setTextAlign(Align.CENTER);
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
		mGridlines = new ArrayList<Gridline>();
		mViewBounds = new RectF();
		mGraphBounds = new RectF();
		setBarCount(1);
		setMaxValue(1);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int minW = getSuggestedMinimumWidth();
		int w = myResolveSizeAndState(minW, widthMeasureSpec, 0);

		int minH = getSuggestedMinimumHeight();
		int h = myResolveSizeAndState(minH, heightMeasureSpec, 0);

		setMeasuredDimension(w, h);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mGraphBounds.set(45, 0, w, h - 45);
		mViewBounds.set(0, 0, w, h);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		Log.i(TAG, "onLayout children: " + String.valueOf(getChildCount()));
		for (int i = 0; i < getChildCount(); i++) {
			int totalBarWidth = mBarWidth * 2;
			int left = leftPadding +
					i * totalBarWidth;
			int right = left + mBarWidth;
			getChildAt(i).layout(left, 0, right, getHeight() - bottomPadding);
		}

	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		generateGridlines();
		Log.i(TAG, "dispatchDraw");
		for (Gridline line : mGridlines) {
			line.draw(canvas, sGridlinePaint, sGridlineTextPaint);
		}
		super.dispatchDraw(canvas);
		canvas.drawLine(mGraphBounds.left, mGraphBounds.top,
				mGraphBounds.left, mGraphBounds.bottom,
				sAxisPaint);
		canvas.drawLine(mGraphBounds.left, mGraphBounds.bottom,
				mGraphBounds.right, mGraphBounds.bottom,
				sAxisPaint);
	}
	
	@Override
	protected int getSuggestedMinimumWidth() {
		int label = (int) sGridlineTextPaint
				.measureText(String.valueOf(mMaxValue));
		int gridline = (int) px(GRIDLINE_PROTRUSION_DP);
		int bars = (int) ((px(BAR_WIDTH_DP) + px(BAR_SPACING_DP)) * mBarCount);
		return label + gridline + bars;
	}
	
	@Override
	protected int getSuggestedMinimumHeight() {
		int bars = (int) (px(GRID_SPACING_DP) * 4);
		int label = (int) sLabelPaint.getFontMetrics().leading;
		return bars + label;
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

	private void generateGridlines() {
		mGridlines.clear();
		int[] gridlineValues = getGridlineValues();
		float gridHeight = mGraphBounds.height();
		float startX = mGraphBounds.left - px(GRIDLINE_PROTRUSION_DP);
		for (int gridlineValue : gridlineValues) {
			float lineY = mGraphBounds.bottom -
					(float) gridlineValue / mMaxValue * gridHeight;
			mGridlines.add(new Gridline(
					startX, lineY,
					mGraphBounds.right, lineY,
					String.valueOf(gridlineValue)));
		}
	}

	private int[] getGridlineValues() {
		int numLines = (int) (mGraphBounds.height() /
				px(GRID_SPACING_DP));
		int increment = Math.max(mMaxValue / numLines, 1);
		while ((numLines + 1) * increment <= mMaxValue) {
			numLines++;
		}
		int[] values = new int[numLines];
		for (int i = 0; i < values.length; i++) {
			values[i] = increment * (i + 1);
		}
		return values;
	}

	private float px(int dp) {
		return dp * mDensity;
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
