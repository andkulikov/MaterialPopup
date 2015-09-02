package com.github.andkulikov.popups;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.FloatProperty;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by Andrey Kulikov on 20.05.15.
 */
public class Popup extends FrameLayout {

    private Activity mActivity;

    private boolean mCanceledOnTouchOutside = true;

    Drawable mBgDrawable;

    private float mVisibleRateX;
    private float mVisibleRateY;
    private float mVisibleRate;

    private Rect mVisibleRect = new Rect();

    private Rect mDrawablePadding = new Rect();

    private int mAnimGravity = Gravity.TOP;

    private int mOffset;

    private int mDrawableAlpha = 255;

    public static FloatProperty<Popup> VISIBLE_RATE_FLOAT =
            new FloatProperty<Popup>("visibleRate") {

                @Override
                public void setValue(Popup popup, float v) {
                    popup.mVisibleRate = v;
                    popup.mVisibleRateY = v;
                    popup.mVisibleRateX = v;
                    popup.invalidate();
                }

                @Override
                public Float get(Popup popup) {
                    return popup.mVisibleRate;
                }
            };

    public Popup(Activity activity, int layoutResId) {
        super(activity);
        mActivity = activity;
        View.inflate(activity, layoutResId, this);
        mBgDrawable = getResources().getDrawable(R.drawable.default_light_popup_bg);
        if (mBgDrawable.getPadding(mDrawablePadding)) {
            setPadding(mDrawablePadding.left, mDrawablePadding.top,
                    mDrawablePadding.right, mDrawablePadding.bottom);
        }
        setWillNotDraw(false);
        setClickable(true);
        mOffset = (int) (getResources().getDisplayMetrics().density * 16);
    }

    public void setAnimGravity(int animGravity) {
        mAnimGravity = animGravity;
    }

    public boolean isModal() {
        return true;
    }

    public void setCanceledOnTouchOutside(boolean canceledOnTouchOutside) {
        mCanceledOnTouchOutside = canceledOnTouchOutside;
    }

    public boolean isCanceledOnTouchOutside() {
        return mCanceledOnTouchOutside;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float transX = 0;
        float transY = 0;
        float invRate = 1 - mVisibleRate;
        if (mAnimGravity == (Gravity.TOP | Gravity.RIGHT)) {
            mVisibleRect.set((int) (invRate * (getMeasuredWidth() - mOffset)),
                    (int) (invRate * mOffset),
                    getMeasuredWidth() - (int) (invRate * mOffset),
                    (int) (getHeight() - invRate * (getMeasuredHeight() - mOffset)));
            transX = (getWidth() / 7) * (1 - mVisibleRate);
            transY = -(getHeight() / 7) * (1 - mVisibleRate);
        } else if (mAnimGravity == (Gravity.BOTTOM | Gravity.RIGHT)) {
            mVisibleRect.set((int) (invRate * (getMeasuredWidth() - mOffset)),
                    (int) (invRate * (getHeight() - mOffset)),
                    getMeasuredWidth() - (int) (invRate * mOffset),
                    (int) (getHeight() - invRate * mOffset));
            transX = (getWidth() / 7) * (1 - mVisibleRate);
            transY = (getHeight() / 7) * (1 - mVisibleRate);
        } else if (mAnimGravity == Gravity.BOTTOM) {
            float bottomOffset = getHeight() * 0.2f;
            mVisibleRect.set((int) ((1 - mVisibleRateX) * getMeasuredWidth() / 2),
                    (int) ((1 - mVisibleRateY) * (getMeasuredHeight() - bottomOffset)),
                    (int) (getWidth() - invRate * getMeasuredWidth() / 2),
                    getMeasuredHeight() - (int) (invRate * bottomOffset));
            transY = getHeight() / 7 * (1 - mVisibleRate);
        } else if (mAnimGravity == Gravity.TOP) {
            float topOffset = getHeight() * 0.2f;
            mVisibleRect.set((int) ((1 - mVisibleRateX) * getMeasuredWidth() / 2),
                    (int) ((1 - mVisibleRateY) * topOffset),
                    (int) (getWidth() - invRate * getMeasuredWidth() / 2),
                    getMeasuredHeight() - (int) (invRate * (getMeasuredHeight() - topOffset)));
            transY = -getHeight() / 7 * (1 - mVisibleRate);
        } else {// (mAnimGravity == Gravity.CENTER) {
            mVisibleRect.set((int) ((1 - mVisibleRateX) * getMeasuredWidth() / 2),
                    (int) ((1 - mVisibleRateY) * getMeasuredHeight() / 2),
                    (int) (getWidth() - invRate * getMeasuredWidth() / 2),
                    (int) (getHeight() - invRate * getMeasuredHeight() / 2));
        }
        mBgDrawable.setBounds(mVisibleRect);
        int newAlpha = 255;
        if (mVisibleRate < 0.2f) {
            newAlpha = (int) (255 * (mVisibleRate * 0.5f));
        }
        if (mDrawableAlpha != newAlpha) {
            mDrawableAlpha = newAlpha;
            mBgDrawable.setAlpha(newAlpha);
        }
        mBgDrawable.draw(canvas);

        if (mVisibleRate != 1f) {
            mVisibleRect.left += mDrawablePadding.left;
            mVisibleRect.top += mDrawablePadding.top;
            mVisibleRect.right -= mDrawablePadding.right;
            mVisibleRect.bottom -= mDrawablePadding.bottom;
            if (mVisibleRect.right < mVisibleRect.left) {
                mVisibleRect.right = mVisibleRect.left;
            }
            if (mVisibleRect.bottom < mVisibleRect.top) {
                mVisibleRect.bottom = mVisibleRect.top;
            }
        }
        canvas.clipRect(mVisibleRect);

        if (transX != 0 || transY != 0) {
            canvas.translate(transX, transY);
        }
        float scale = 1f - invRate * 0.2f;
        canvas.scale(scale, scale, canvas.getWidth() / 2, canvas.getHeight() / 2);

        super.onDraw(canvas);
    }

    public void show(View anchor) {
        getContainer().show(this, anchor);
    }

    public void show(int gravity) {
        getContainer().show(this, gravity);
    }

    public void show(int x, int y) {
        getContainer().show(this, x, y);
    }

    public void dismiss() {
        getContainer().dismiss(this);
    }

    private PopupContainer getContainer() {
        return PopupContainer.getContainer(mActivity);
    }

    public static void dismissAllPopups(Activity activity) {
        PopupContainer.getContainer(activity).dismissCurrentPopup();
    }

}
