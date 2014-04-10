package com.derekjass.jacksonutilitysubmitter.views;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

import com.derekjass.jacksonutilitysubmitter.R;

public class BarGraph extends View {

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
			canvas.drawText(mLabel,
					mStart.x, mStart.y + -labelPaint.ascent() / 2,
					labelPaint);
		}
	}
	
	private static class BarView extends View {
		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			// TODO Auto-generated method stub
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}

	private static final int GRID_SPACING_DP = 30;
	private static final int GRIDLINE_PROTRUSION_DP = 5;
	private static final int BAR_WIDTH_DP = 30;
	private static final float BAR_TO_SPACING_RATIO = 0.7f;

	private float mDensity;

	private Paint mBarPaint;
	private Paint mAxisPaint;
	private Paint mGridlinePaint;
	private Paint mGridlineTextPaint;
	private Paint mLabelPaint;

	private List<String> mLabels;
	private int mBarCount;
	private int mBarColor;
	private int mMaxValue;

	private List<Gridline> mGridlines;
	private RectF mViewBounds;
	private RectF mGraphBounds;

	public BarGraph(Context context) {
		this(context, null);
	}

	public BarGraph(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDensity = getResources().getDisplayMetrics().density;

		TypedArray a = context.getTheme().obtainStyledAttributes(
				attrs,
				R.styleable.BarGraph,
				0, 0);

		try {
			setBarCount(a.getInteger(R.styleable.BarGraph_barCount, 1));
			setBarColor(a.getColor(R.styleable.BarGraph_barColor, 0xFFFFFFFF));
		} finally {
			a.recycle();
		}

		mGridlines = new ArrayList<Gridline>();
		mViewBounds = new RectF();
		mGraphBounds = new RectF();

		mAxisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mAxisPaint.setColor(0xFF444444);
		mAxisPaint.setStrokeWidth(1f * mDensity);

		mGridlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mGridlinePaint.setColor(0xFF888888);
		mGridlinePaint.setStrokeWidth(0f);

		mGridlineTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mGridlineTextPaint.setColor(0xFF888888);
		mGridlineTextPaint.setTextSize(8f * mDensity);
		mGridlineTextPaint.setTextAlign(Align.RIGHT);

		mLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mLabelPaint.setColor(0xFF888888);
		mLabelPaint.setTextSize(10f * mDensity);
		mLabelPaint.setTextAlign(Align.CENTER);
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
	protected void onDraw(Canvas canvas) {
		generateGridlines();
		drawGridlines(canvas);
		drawLabels(canvas);
		drawTicks(canvas);
		drawBars(canvas);
		drawAxes(canvas);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mGraphBounds.set(getLeftPadding(), 0, w, h - getBottomPadding());
		mViewBounds.set(0, 0, w, h);
	}

	private void drawGridlines(Canvas canvas) {
		for (Gridline line : mGridlines) {
			line.draw(canvas, mGridlinePaint, mGridlineTextPaint);
		}
	}

	private void drawLabels(Canvas canvas) {
		if (mLabels == null) return;
		float spacing = mGraphBounds.width() / mBarCount;
		float startX = mGraphBounds.left + spacing / 2;
		for (int i = 0; i < Math.min(mBarCount, mLabels.size()); i++) {
			canvas.drawText(mLabels.get(i),
					startX + spacing * i,
					mGraphBounds.bottom + -mLabelPaint.ascent(),
					mLabelPaint);
		}
	}

	private void drawTicks(Canvas canvas) {
		float spacing = mGraphBounds.width() / mBarCount;
		for (int i = 1; i < mBarCount; i++) {
			float x = mGraphBounds.left + i * spacing;
			canvas.drawLine(x, mGraphBounds.bottom,
					x, mGraphBounds.bottom + px(GRIDLINE_PROTRUSION_DP),
					mGridlinePaint);
		}
	}

	private void drawBars(Canvas canvas) {
		
	}
	
	private void drawAxes(Canvas canvas) {
		canvas.drawLine(mGraphBounds.left, mGraphBounds.top,
				mGraphBounds.left, mGraphBounds.bottom,
				mAxisPaint);
		canvas.drawLine(mGraphBounds.left, mGraphBounds.bottom,
				mGraphBounds.right, mGraphBounds.bottom,
				mAxisPaint);
	}

	@Override
	protected int getSuggestedMinimumWidth() {
		float label = getLeftPadding();
		float bars = px(BAR_WIDTH_DP) / BAR_TO_SPACING_RATIO * mBarCount;
		return (int) Math.ceil(label + bars);
	}

	private float getLeftPadding() {
		float label = mGridlineTextPaint
				.measureText(String.valueOf(mMaxValue));
		float gridline = px(GRIDLINE_PROTRUSION_DP);
		return label + gridline;
	}

	@Override
	protected int getSuggestedMinimumHeight() {
		float bars = px(GRID_SPACING_DP) * 3;
		float label = getBottomPadding();
		return (int) Math.ceil(bars + label);
	}

	private float getBottomPadding() {
		return mLabelPaint.getFontSpacing();
	}

	public void setValues(ArrayList<Integer> values) {
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
			do {
				AnimatingBarView v = new AnimatingBarView(getContext()).animate();
				v.setColor(mBarColor);
				addView(v);
			} while (getChildCount() < num);
		} else {
			do {
				removeViewAt(getChildCount() - 1);
			} while (getChildCount() > num);
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
			Gridline line = new Gridline(
					startX, lineY,
					mGraphBounds.right, lineY,
					String.valueOf(gridlineValue));
			mGridlines.add(line);
		}
	}

	private int[] getGridlineValues() {
		float height = mGraphBounds.height();
		float headroom = -mGridlineTextPaint.ascent() / 2;
		int maxValue = (int) (mMaxValue * ((height - headroom) / height));
		int numLines = (int) (height / px(GRID_SPACING_DP));
		int increment = Math.max(maxValue / numLines, 1);
		while ((numLines + 1) * increment <= maxValue) {
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
