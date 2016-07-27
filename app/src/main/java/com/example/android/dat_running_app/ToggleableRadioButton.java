package com.example.android.dat_running_app;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;

/**
 * Created by Ben on 7/24/2016.
 */

public class ToggleableRadioButton extends RadioButton {
    public ToggleableRadioButton(Context context) {
        super(context);
    }

    public ToggleableRadioButton(Context context, AttributeSet attrs){
        super(context,attrs);
    }

    @Override
    public void toggle() {
        if(isChecked()){
            if(getParent() instanceof RadioGroup) {
                ((RadioGroup)getParent()).clearCheck();
            }
        }
        else
            setChecked(true);
    }
}
