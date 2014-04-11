package com.derekjass.jacksonutilitysubmitter.views;

import java.util.ArrayList;
import java.util.Collections;
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
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.AttributeSet;
import android.view.View;

import com.derekjass.jacksonutilitysubmitter.R;

public class BarGraph extends View {

	private class BarView extends View {

		public BarView(Context context) {
			super(context);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			mBarDrawable.setColorFilter(mBarColorFilter);
			mBarDrawable.setBounds(getLeft(), getTop(),
					getRight(), getBottom());
			mBarDrawable.draw(canvas);
			mBarDrawable.clearColorFilter();
		}
	}

	private class Gridline {
		public PointF mStart;
		public PointF mEnd;
		public String mLabel;

		public Gridline(float startX, float startY, float endX, float endY,
				String label) {
			mStart = new PointF(startX, startY);
			mEnd = new PointF(endX, endY);
			mLabel = label;
		}

		public void draw(Canvas canvas) {
			canvas.drawLine(mStart.x, mStart.y,
					mEnd.x, mEnd.y,
					mGridlinePaint);
			canvas.drawText(mLabel,
					mStart.x, mStart.y + -mGridlineTextPaint.ascent() / 2.5f,
					mGridlineTextPaint);
		}
	}

	private static final int DEFAULT_GRID_SPACING_DP = 30;
	private static final int DEFAULT_GRIDLINE_PROTRUSION_DP = 5;
	private static final int DEFAULT_BAR_WIDTH_DP = 30;
	private static final float DEFAULT_BAR_TO_SPACING_RATIO = 0.7f;

	private Paint mAxisPaint;
	private Paint mGridlinePaint;
	private Paint mGridlineTextPaint;
	private Paint mLabelPaint;

	private List<Integer> mValues;
	private List<String> mLabels;
	private List<Gridline> mGridlines;
	private List<View> mBars;

	private int mBarCount;
	private LightingColorFilter mBarColorFilter;
	private Drawable mBarDrawable;
	private int mMaxValue;

	private RectF mViewBounds;
	private RectF mGraphBounds;

	public BarGraph(Context context) {
		this(context, null);
	}

	public BarGraph(Context context, AttributeSet attrs) {
		super(context, attrs);

		mGridlines = new ArrayList<Gridline>();
		mBars = new ArrayList<View>();
		mViewBounds = new RectF();
		mGraphBounds = new RectF();

		TypedArray a = context.getTheme().obtainStyledAttributes(
				attrs,
				R.styleable.BarGraph,
				0, 0);

		try {
			setBarCount(a.getInteger(R.styleable.BarGraph_barCount, 1));
			setBarColor(a.getColor(R.styleable.BarGraph_barColor, 0xFF888888));
			setBarDrawable(a.getDrawable(R.styleable.BarGraph_barDrawable));
		} finally {
			a.recycle();
		}

		mAxisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mAxisPaint.setColor(0xFF444444);
		mAxisPaint.setStrokeWidth(1f * getDensity());

		mGridlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mGridlinePaint.setColor(0xFF888888);
		mGridlinePaint.setStrokeWidth(0f);

		mGridlineTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mGridlineTextPaint.setColor(0xFF888888);
		mGridlineTextPaint.setTextSize(8f * getDensity());
		mGridlineTextPaint.setTextAlign(Align.RIGHT);

		mLabelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mLabelPaint.setColor(0xFF888888);
		mLabelPaint.setTextSize(10f * getDensity());
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
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		float totalBarWidth = mGraphBounds.width() / mBarCount;
		float barWidth = totalBarWidth * DEFAULT_BAR_TO_SPACING_RATIO;
		float startOffset = (totalBarWidth - barWidth) / 2;

		for (int i = 0; i < Math.min(mValues.size(), mBarCount) ; i++) {
			float l = mGraphBounds.left + startOffset + totalBarWidth * i;
			float r = l + barWidth;
			float pct = mValues.get(i) / (float) mMaxValue;
			float height = (mGraphBounds.bottom - mGraphBounds.top) * (1 - pct);
			mBars.get(i).layout((int) l, (int) (mGraphBounds.top + height),
					(int) r, (int) mGraphBounds.bottom);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		drawGridlines(canvas);
		drawLabels(canvas);
		drawTicks(canvas);
		drawBars(canvas);
		drawAxes(canvas);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mGraphBounds.set(getLeftLabelsWidth(), 0,
				w, h - getBottomLabelsWidth());
		mViewBounds.set(0, 0, w, h);
		generateGridlines();
	}

	private void drawGridlines(Canvas canvas) {
		for (Gridline line : mGridlines) {
			line.draw(canvas);
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
					x, mGraphBounds.bottom + px(DEFAULT_GRIDLINE_PROTRUSION_DP),
					mGridlinePaint);
		}
	}

	private void drawBars(Canvas canvas) {
		for (View v : mBars) {
			v.draw(canvas);
		}
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
		float label = getLeftLabelsWidth();
		float bars = px(DEFAULT_BAR_WIDTH_DP) / DEFAULT_BAR_TO_SPACING_RATIO
				* mBarCount;
		return (int) Math.ceil(label + bars);
	}

	private float getLeftLabelsWidth() {
		float label = mGridlineTextPaint
				.measureText(String.valueOf(mMaxValue));
		float gridline = px(DEFAULT_GRIDLINE_PROTRUSION_DP);
		return label + gridline;
	}

	@Override
	protected int getSuggestedMinimumHeight() {
		float label = getBottomLabelsWidth();
		float bars = px(DEFAULT_GRID_SPACING_DP) * 3;
		return (int) Math.ceil(bars + label);
	}

	private float getBottomLabelsWidth() {
		return mLabelPaint.getFontSpacing();
	}

	public void setValues(ArrayList<Integer> values) {
		setMaxValue(Math.max(Collections.max(values), 1));
		mValues = Collections.unmodifiableList(new ArrayList<Integer>(values));
		requestLayout();
	}

	public void setLabels(ArrayList<String> labels) {
		mLabels = Collections.unmodifiableList(new ArrayList<String>(labels));
		invalidate();
	}

	public void setBarCount(int num) {
		if (num < 1) throw new IllegalArgumentException(
				"Bar count must be > 0");

		if (num > mBarCount) {
			while (mBars.size() < num) {
				mBars.add(new BarView(getContext()));
			}
		} else if (num < mBarCount) {
			while (mBars.size() > num) {
				mBars.remove(mBars.size() - 1);
			}
		}
		mBarCount = num;
		requestLayout();
	}

	public void setBarColor(int color) {
		mBarColorFilter = new LightingColorFilter(color, 0);
		invalidate();
	}

	public void clearBarColor() {
		mBarColorFilter = null;
		invalidate();
	}

	public void setBarDrawable(Drawable drawable) {
		if (drawable != null) {
			mBarDrawable = drawable;
		} else {
			ShapeDrawable d = new ShapeDrawable(new RectShape());
			d.getPaint().setColor(0xFFFFFFFF);
			mBarDrawable = d;
		}

		invalidate();
	}

	public void setBarDrawable(int resId) {
		getResources().getDrawable(resId);
	}

	private void setMaxValue(int max) {
		if (max <= 0) {
			mMaxValue = 1;
		} else {
			mMaxValue = max;
		}
		generateGridlines();
		invalidate();
	}

	private void generateGridlines() {
		mGridlines.clear();
		int[] gridlineValues = getGridlineValues();
		if (gridlineValues == null) return;
		float gridHeight = mGraphBounds.height();
		float startX = mGraphBounds.left - px(DEFAULT_GRIDLINE_PROTRUSION_DP);
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
		float headroom = -mGridlineTextPaint.ascent() / 2.5f;
		int maxValue = (int) (mMaxValue * ((height - headroom) / height));
		int numLines = (int) (height / px(DEFAULT_GRID_SPACING_DP));
		if (maxValue == 0 || numLines == 0) return null;
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
		return dp * getDensity();
	}

	private float getDensity() {
		return getResources().getDisplayMetrics().density;
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
