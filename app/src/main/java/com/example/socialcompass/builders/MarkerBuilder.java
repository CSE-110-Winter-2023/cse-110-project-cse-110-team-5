package com.example.socialcompass.builders;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.example.socialcompass.R;

public class MarkerBuilder {
    private final ConstraintLayout layout;
    private final ConstraintLayout markerLayout;
    private final TextView textView;

    public MarkerBuilder(Activity context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        this.layout = context.findViewById(R.id.constraint_layout);
        this.markerLayout = (ConstraintLayout) inflater.inflate(R.layout.marker, this.layout, false);
        this.textView = new TextView(context);
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.circleRadius = 0;
        layoutParams.circleConstraint = R.id.pivot;
        layoutParams.circleAngle = 0;
        layoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        this.textView.setLayoutParams(layoutParams);
        this.markerLayout.addView(this.textView);
        this.textView.setVisibility(View.VISIBLE);
    }

    public MarkerBuilder setText(String text) {
        textView.setText(text);
        return this;
    }

    public TextView getNewMarker() {
        this.textView.setId(this.textView.getText().hashCode());
        this.layout.addView(this.markerLayout);
        return this.textView;
    }
}
