package com.example.stergios.androiduomproject;


import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;

//to ViewPager gia tin emfanisi twn fragment
public class ViewPagerAdapter extends FragmentPagerAdapter {

    Drawable myDrawable;

    private final Activity context;
    public ViewPagerAdapter(Activity context, FragmentManager fm) {

        super(fm);
        this.context=context;

    }

    @Override
    public int getCount() {
        // o arithmos twn diaforetikwn tabs
        return 3;
    }

    @Override
    public android.support.v4.app.Fragment getItem(int position) {
        // kathe case kai 1 tab (fragment)
        // i seira twn cases einai kai i seira emfanisis
        switch (position) {
            case 0:
                return new HomeFragment();
            case 1:
                return new SearchFragment();
            case 2:
                return new SuggestionsFragment();
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        //dimiourgia eikonas gia to kathe tab gia emfanisi sto header
        SpannableStringBuilder sb = new SpannableStringBuilder(" "); // space added before text for convenience
        switch (position) {
            case 0:
                myDrawable = context.getResources().getDrawable(R.drawable.app_icon);
                try {
                    myDrawable.setBounds(0, 0, (int) context.getResources().getDimension(R.dimen.tabwidth), (int) context.getResources().getDimension(R.dimen.tabheight));
                    ImageSpan span = new ImageSpan(myDrawable, DynamicDrawableSpan.ALIGN_BASELINE);
                    sb.setSpan(span, 0,  1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } catch (Exception e) {

                }
                break;
            case 1:
                myDrawable = context.getResources().getDrawable(R.drawable.search);
                try {
                    myDrawable.setBounds(0, 0, (int) context.getResources().getDimension(R.dimen.tabwidth), (int) context.getResources().getDimension(R.dimen.tabheight));
                    ImageSpan span = new ImageSpan(myDrawable, DynamicDrawableSpan.ALIGN_BASELINE);
                    sb.setSpan(span, 0,  1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } catch (Exception e) {

                }
                break;
            case 2:
                myDrawable = context.getResources().getDrawable(R.drawable.suggest);
                try {
                    myDrawable.setBounds(0, 0, (int) context.getResources().getDimension(R.dimen.tabwidth), (int) context.getResources().getDimension(R.dimen.tabheight));
                    ImageSpan span = new ImageSpan(myDrawable, DynamicDrawableSpan.ALIGN_BASELINE);
                    sb.setSpan(span, 0,  1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } catch (Exception e) {

                }
                break;


            default:
                break;
        }



        return sb;
    }



}



