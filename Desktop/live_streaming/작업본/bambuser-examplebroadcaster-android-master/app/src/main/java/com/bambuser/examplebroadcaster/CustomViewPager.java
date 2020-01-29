package com.bambuser.examplebroadcaster;

//import android.content.Context;
//import android.content.res.TypedArray;
//import android.support.annotation.NonNull;
//import android.support.v4.view.ViewPager;
//import android.util.AttributeSet;
//import android.view.MotionEvent;
//import android.view.View;
//
//public class CustomViewPager extends ViewPager {
//    public static final int HORIZONTAL = 0;
//    public static final int VERTICAL = 1;
//
//    private int mSwipeOrientation;
//
//    public CustomViewPager(Context context) {
//        super(context);
//        mSwipeOrientation = HORIZONTAL;
//    }
//
//    public CustomViewPager(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        setSwipeOrientation(context, attrs);
//    }
//
////    @Override
////    public boolean onTouchEvent(MotionEvent event) {
////        return super.onTouchEvent(mSwipeOrientation == VERTICAL ? swapXY(event) : event);
////    }
////
////    @Override
////    public boolean onInterceptTouchEvent(MotionEvent event) {
////        if (mSwipeOrientation == VERTICAL) {
////            boolean intercepted = super.onInterceptHoverEvent(swapXY(event));
////            swapXY(event);
////            return intercepted;
////        }
////        return super.onInterceptTouchEvent(event);
////    }
//
//    private void setSwipeOrientation(Context context, AttributeSet attrs) {
////        TypedArray typedArray = context.obtainStyledAttributes(attrs, );
////        mSwipeOrientation = typedArray.getInteger(R.styleable., 0);
////        typedArray.recycle();
//        mSwipeOrientation = VERTICAL;
//        initSwipeMethods();
//    }
//
//    private void initSwipeMethods() {
//        if (mSwipeOrientation == VERTICAL) {
//            // The majority of the work is done over here
//            setPageTransformer(true, new VerticalPageTransformer());
//            // The easiest way to get rid of the overscroll drawing that happens on the left and right
//            setOverScrollMode(OVER_SCROLL_NEVER);
//        }
//    }
//
//    private MotionEvent swapXY(MotionEvent event) {
//        float width = getWidth();
//        float height = getHeight();
//
//        float newX = (event.getY() / height) * width;
//        float newY = (event.getX() / width) * height;
//
//        event.setLocation(newX, newY);
//        return event;
//    }
//
//    private class VerticalPageTransformer implements ViewPager.PageTransformer {
//        private View view;
//        private float v;
//
//        @Override
//        public void transformPage(View page, float position) {
//            if (position < -1) {
//                // This page is way off-screen to the left
//                page.setAlpha(1);
//            } else if (position <= 1) {
//                page.setAlpha(1);
//
//                // Counteract the default slide transition
//                page.setTranslationX(page.getWidth() * -position);
//
//                // set Y position to swipe in from top
//                float yPosition = position * page.getHeight();
//                page.setTranslationY(yPosition);
//            } else {
//                // This page is way off screen to the right
//                page.setAlpha(1);
//            }
//        }
//    }
//}
import android.content.Context;


import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
        import android.view.MotionEvent;
        import android.view.View;
public class CustomViewPager extends ViewPager {
    public CustomViewPager(Context context) {
        this(context, null);
    }
    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    /**
     * @return {@code false} since a vertical view pager can never be scrolled horizontally
     */
    @Override
    public boolean canScrollHorizontally(int direction) {
        return false;
    }
    /**
     * @return {@code true} iff a normal view pager would support horizontal scrolling at this time
     */
    @Override
    public boolean canScrollVertically(int direction) {
        return super.canScrollHorizontally(direction);
    }
    private void init() {
        // Make page transit vertical
        setPageTransformer(true, new VerticalPageTransformer());
        // Get rid of the overscroll drawing that happens on the left and right (the ripple)
        setOverScrollMode(View.OVER_SCROLL_NEVER);
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final boolean toIntercept = super.onInterceptTouchEvent(flipXY(ev));
        // Return MotionEvent to normal
        flipXY(ev);
        return toIntercept;
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final boolean toHandle = super.onTouchEvent(flipXY(ev));
        // Return MotionEvent to normal
        flipXY(ev);
        return toHandle;
    }
    private MotionEvent flipXY(MotionEvent ev) {
        final float width = getWidth();
        final float height = getHeight();
        final float x = (ev.getY() / height) * width;
        final float y = (ev.getX() / width) * height;
        ev.setLocation(x, y);
        return ev;
    }
    private static final class VerticalPageTransformer implements ViewPager.PageTransformer {
        @Override
        public void transformPage(View view, float position) {
            final int pageWidth = view.getWidth();
            final int pageHeight = view.getHeight();
            if (position < -1) {
                // This page is way off-screen to the left.
                view.setAlpha(0);
            } else if (position <= 1) {
                view.setAlpha(1);
                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);
                // set Y position to swipe in from top
                float yPosition = position * pageHeight;
                view.setTranslationY(yPosition);
            } else {
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }
}


