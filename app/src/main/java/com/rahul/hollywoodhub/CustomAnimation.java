package com.rahul.hollywoodhub;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

/**
 * Created by rahul on 8/3/16.
 */
public class CustomAnimation {

    public static Animation fadeOut(Context mContext){
        return AnimationUtils.loadAnimation(mContext, android.R.anim.fade_out);
    }
    public static Animation fadeIn(Context mContext){
        return AnimationUtils.loadAnimation(mContext, android.R.anim.fade_in);
    }
}
