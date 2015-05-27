package com.github.andkulikov.popups;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
        mCurrentPopup.startAnimation(createAnim(true));
    }

    private Animation createAnim(boolean in) {
        return AnimationUtils.loadAnimation(getContext(), in ? R.anim.popup_in : R.anim.popup_out);
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
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                dismissCurrentPopup();
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
            Animation animOut = createAnim(false);
            animOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    // do nothing
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            removeView(view);
                        }
                    });
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    // do nothing
                }
            });
            mCurrentPopup.startAnimation(animOut);
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