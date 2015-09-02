package com.github.andkulikov.popups;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by Andrey Kulikov on 20.05.15.
 */
class PopupContainer extends FrameLayout {

    private Popup mCurrentPopup;

    private boolean mCatchTouchEvents;

    private int[] mLocationOnScreen = new int[2];

    public PopupContainer(Context context) {
        super(context);
    }

    public PopupContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PopupContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        initSizes();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initSizes();
    }

    private void initSizes() {
        getLocationOnScreen(mLocationOnScreen);
        View parent = (View) getParent();
        if (parent != null) {
            setMeasuredDimension(parent.getMeasuredWidth(), parent.getMeasuredHeight());
        }
    }

    void show(Popup popup, View anchor) {
        setCurrentPopup(popup);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(popup, params);
        mCurrentPopup.measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.AT_MOST));
        int[] loc = new int[2];
        anchor.getLocationOnScreen(loc);
        params.leftMargin = getMeasuredWidth() - mCurrentPopup.getMeasuredWidth()
                - loc[0] + mLocationOnScreen[0];
        params.topMargin = loc[1] - mLocationOnScreen[1];

        showInternal();
    }

    void show(Popup popup, int gravity) {
        setCurrentPopup(popup);
        addView(popup, new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, gravity));
        showInternal();
    }

    void show(Popup popup, int x, int y) {
        setCurrentPopup(popup);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = x;
        params.topMargin = y;
        addView(popup, params);
        showInternal();
    }

    private void setCurrentPopup(Popup popup) {
        dismissCurrentPopup();
        mCurrentPopup = popup;
    }

    private void showInternal() {
        createAnim(mCurrentPopup, true).start();
    }

    private Animator createAnim(Popup popup, boolean in) {
        float start = in ? 0 : 1;
        float end = in ? 1 : 0;
        Animator animator = ObjectAnimator.ofFloat(popup, Popup.VISIBLE_RATE_FLOAT, start, end);
        animator.setDuration(400);
        animator.setInterpolator(new FastOutSlowInInterpolator());
        return animator;
    }

    public boolean isAnyPopupShowing() {
        return mCurrentPopup != null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = super.onTouchEvent(event);
        if (mCurrentPopup != null) {
            if (mCurrentPopup.isModal()) {
                mCatchTouchEvents = true;
            }
            if (mCurrentPopup.isCanceledOnTouchOutside() &&
                    event.getAction() == MotionEvent.ACTION_DOWN) {
                dismiss(mCurrentPopup);
            }
        }
        if (mCatchTouchEvents) {
            if (event.getAction() == MotionEvent.ACTION_UP ||
                    event.getAction() == MotionEvent.ACTION_CANCEL) {
                mCatchTouchEvents = false;
            }
            return true;
        }
        return result;
    }

    void dismiss(Popup popup) {
        if (popup != null && popup.equals(mCurrentPopup)) {
            dismissCurrentPopup();
        }
    }

    void dismissCurrentPopup() {
        if (isAnyPopupShowing()) {
            final View view = mCurrentPopup;
            Animator animOut = createAnim(mCurrentPopup, false);
            animOut.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            removeView(view);
                        }
                    });
                }

            });
            animOut.start();
            mCurrentPopup = null;
        }
    }

    static PopupContainer getContainer(Activity activity) {
        ViewGroup group = (ViewGroup) activity.findViewById(android.R.id.content);
        PopupContainer container = null;
        for (int i = 0, count = group.getChildCount(); i < count; i++) {
            if (group.getChildAt(i) instanceof PopupContainer) {
                container = (PopupContainer) group.getChildAt(i);
            }
        }
        if (container == null) {
            container = new PopupContainer(activity);
            group.addView(container);
        }
        return container;
    }
}