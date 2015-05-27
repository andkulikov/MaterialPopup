package com.github.andkulikov.popups;

import android.app.Activity;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by Andrey Kulikov on 20.05.15.
 */
public class Popup extends FrameLayout {

    private Activity mActivity;

    public Popup(Activity activity, int layoutResId) {
        super(activity);
        mActivity = activity;
        View.inflate(activity, layoutResId, this);
        setBackgroundResource(R.drawable.default_light_popup_bg);
        setClickable(true);
    }

    public boolean isModal() {
        return true;
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
