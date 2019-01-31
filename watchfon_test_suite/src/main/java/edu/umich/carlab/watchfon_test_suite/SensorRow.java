package edu.umich.carlab.watchfon_test_suite;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * TODO: document your custom view class.
 */
public class SensorRow extends FrameLayout {
    private String name;
    private TextView nameTV, injectionTV, detectionTV;


    public SensorRow(Context context) {
        super(context);
        init(null, 0);
    }

    public SensorRow(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public SensorRow(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }


    private void init(AttributeSet attrs, int defStyle) {
        inflate(getContext(), R.layout.sensor_row, this);
        nameTV = findViewById(R.id.sensor_name);
        injectionTV = findViewById(R.id.sensor_injection);
        detectionTV = findViewById(R.id.sensor_detection);

        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.SensorRow, defStyle, 0);
        name = a.getString(R.styleable.SensorRow_name);
        nameTV.setText(name);
    }

    public void initializeParameters (Float duration, Float magnitude) {
        detectionTV.setText(String.format("(%.02f, %.02f)", magnitude, duration));
    }

    public void setDetection (boolean detected) {
        detectionTV.setBackground(
                (detected)
                ? getContext().getDrawable(R.drawable.background_green)
                : getContext().getDrawable(R.drawable.background_white));
    }

    public void setInjection (float value) {
        injectionTV.setText("" + value);
        injectionTV.setBackground(
                (value != 0)
                ? getContext().getDrawable(R.drawable.background_green)
                : getContext().getDrawable(R.drawable.background_white)
        );
    }
}
