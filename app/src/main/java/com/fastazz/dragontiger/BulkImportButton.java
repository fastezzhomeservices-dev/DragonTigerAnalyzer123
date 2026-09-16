package com.fastazz.dragontiger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

public class BulkImportButton extends Button {
    public BulkImportButton(Context context) { super(context); init(context); }
    public BulkImportButton(Context context, AttributeSet attrs) { super(context, attrs); init(context); }
    public BulkImportButton(Context context, AttributeSet attrs, int style) { super(context, attrs, style); init(context); }

    private void init(Context context) {
        setText("BULK DATA UPLOAD");
        setTextSize(16);
        setTextColor(Color.WHITE);
        setAllCaps(false);
        setOnClickListener(v -> {
            if (context instanceof Activity) {
                ((Activity) context).startActivity(new Intent(context, ImportActivity.class));
            }
        });
    }
}
