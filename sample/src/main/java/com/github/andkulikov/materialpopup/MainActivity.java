package com.github.andkulikov.materialpopup;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.github.andkulikov.popups.Popup;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.popup_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Popup popup = new Popup(MainActivity.this, R.layout.popup);
                popup.findViewById(R.id.one_item).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(v.getContext(), "One", Toast.LENGTH_SHORT).show();
                        popup.dismiss();
                    }
                });
                popup.show(v);
            }
        });
    }

}
