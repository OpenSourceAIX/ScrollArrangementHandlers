package cn.colintree.aix.ScrollArrangementHandlers;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.VerticalScrollArrangement;

import android.util.Log;

import android.view.View;
import android.view.ViewTreeObserver;
import android.view.MotionEvent;
import android.widget.ScrollView;

@DesignerComponent(version = VerticalScrollHandler.VERSION,
    description = "by ColinTree at http://aix.colintree.cn/",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = "aiwebres/iconV.png")

@SimpleObject(external = true)

public class VerticalScrollHandler extends AndroidNonvisibleComponent implements Component {

    public static final int VERSION = 3;

    private ComponentContainer container;
    private static final String LOG_TAG = "VerticalScrollHandler";

    private int oldScrollY = 0;

    private boolean userControl = true;
    private boolean scrollBarEnabled = true;
    private boolean fadingEdgeEnabled = true;
    private int overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS;

    private ScrollView scrollView = null;

    public VerticalScrollHandler(ComponentContainer container) {
        super(container.$form());
        this.container = container;
        Log.d(LOG_TAG, LOG_TAG+" Created" );
    }

    /**
     * <p>
     * greater than 1 usually, making the px numbers smaller than dx numbers.
     * which means a px contains to more than one dx
     * </p>
     * e.g. on my P9 plus, once i got 3 from this, and width of an button in px is 30, and dx is 90.
     */
    private float deviceDensity() {
        return container.$form().deviceDensity();
    }
    /**
     * px (appinventor unit) to dx (android unit)
     */
    private int px2dx(int px) {
        return Math.round(px * deviceDensity());
    }
    /**
     * dx (android unit) to px (appinventor unit)
     */
    private int dx2px(int dx) {
        return Math.round(dx / deviceDensity());
    }
    private float dx2px(float dx) {
        return dx / deviceDensity();
    }


    @SimpleFunction
    public void RegisterScrollView(VerticalScrollArrangement verticalScrollArrangement) {
        scrollView = (ScrollView) verticalScrollArrangement.getView();
        scrollView.getViewTreeObserver().addOnScrollChangedListener(
            new ViewTreeObserver.OnScrollChangedListener() {
                @Override
                public void onScrollChanged() {
                    onScroll();
                }
            });
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            private boolean touchDownDetected = false;
            private int touchDownScrollY = 0;
            private int touchDownPointerId;
            private float touchDownPointerY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getActionMasked();
                if (action == MotionEvent.ACTION_DOWN) {
                    onTouchDown(event);
                } else if (action == MotionEvent.ACTION_MOVE) {
                    if (onMove(event)) {
                        return true;
                    }
                } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    onTouchUp(event);
                }
                return !UserControl(); // => (UserControl()?false:true);
            }
            private void onTouchDown(MotionEvent event) {
                TouchDown();
                touchDownDetected = true;
                touchDownScrollY = dx2px(scrollView.getScrollY());
                touchDownPointerId = dx2px(event.getPointerId(0));
                touchDownPointerY = dx2px(event.getY(0));
            }
            private boolean onMove(MotionEvent event) {
                if (touchDownDetected == true) {
                    int currentScrollY = dx2px(scrollView.getScrollY());
                    float currentPointerY = dx2px(event.getY(event.findPointerIndex(touchDownPointerId)));

                    if (touchDownScrollY <= 0 && currentScrollY <= 0) {
                        return OverScrollDown(currentPointerY - touchDownPointerY);
                    }
                    int max = MaxScrollPosition();
                    if (touchDownScrollY >= max && currentScrollY >= max) {
                        return OverScrollUp(touchDownPointerY - currentPointerY);
                    }
                } else {
                    onTouchDown(event);
                    return onMove(event);
                }
                return false;
            }
            private void onTouchUp(MotionEvent event) {
                TouchUp();
                touchDownDetected = false;
            }
        });
        OverScrollMode(OverScrollMode());
        ScrollBarEnabled(ScrollBarEnabled());
        FadingEdgeEnabled(FadingEdgeEnabled());
    }

    @SimpleEvent
    public void ReachTop() {
        EventDispatcher.dispatchEvent(this, "ReachTop");
    }
    @SimpleEvent
    public void ReachBottom() {
        EventDispatcher.dispatchEvent(this, "ReachBottom");
    }
    @SimpleEvent
    public void ScrollChanged(int scrollY) {
        EventDispatcher.dispatchEvent(this, "ScrollChanged", scrollY);
        if (scrollY == 0) {
            ReachTop();
        } else {
            if (MaxScrollPosition() - ScrollPosition()<=0) {
                ReachBottom();
            }
        }
    }
    public void onScroll() {
        int scrollY = dx2px(scrollView.getScrollY());
        if (scrollY<0) {
            scrollY = 0;
        }
        if (oldScrollY != scrollY) {
            ScrollChanged(scrollY);
            oldScrollY = scrollY;
        }
    }
    @SimpleEvent
    public void TouchDown() {
        EventDispatcher.dispatchEvent(this, "TouchDown");
    }
    @SimpleEvent
    public void TouchUp() {
        EventDispatcher.dispatchEvent(this, "TouchUp");
    }

    @SimpleEvent
    public boolean OverScrollDown(float displacement) {
        if (displacement>0) {
            return EventDispatcher.dispatchEvent(this, "OverScrollDown", displacement);
        }
        return false;
    }
    @SimpleEvent
    public boolean OverScrollUp(float displacement) {
        if (displacement>0) {
            return EventDispatcher.dispatchEvent(this, "OverScrollUp", displacement);
        }
        return false;
    }


    @SimpleProperty
    public boolean UserControl() {
        return userControl;
    }
    @SimpleProperty
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
    public void UserControl(boolean enable) {
        userControl = enable;
    }

    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public boolean ScrollBarEnabled() {
        return scrollBarEnabled;
    }
    @SimpleProperty
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
    public void ScrollBarEnabled(boolean enabled) {
        this.scrollBarEnabled = enabled;
        if (scrollView != null) {
            ((ScrollView) scrollView).setVerticalScrollBarEnabled(enabled);
        }
    }

    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public boolean FadingEdgeEnabled() {
        return fadingEdgeEnabled;
    }
    @SimpleProperty
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
    public void FadingEdgeEnabled(boolean enabled) {
        this.fadingEdgeEnabled = enabled;
        if (scrollView != null) {
            ((ScrollView) scrollView).setVerticalFadingEdgeEnabled(enabled);
        }
    }

    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public int OverScrollMode() {
        return overScrollMode;
    }
    @SimpleProperty(description = "Can be:\n "+
            View.OVER_SCROLL_ALWAYS+": ALWAYS\n"+
            View.OVER_SCROLL_IF_CONTENT_SCROLLS+": OVER SCROLL IF CONTENT SCROLLS\n"+
            View.OVER_SCROLL_NEVER+": NEVER")
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_NON_NEGATIVE_INTEGER, defaultValue = "1")
    public void OverScrollMode(int mode) {
        if (mode != View.OVER_SCROLL_ALWAYS && mode != View.OVER_SCROLL_IF_CONTENT_SCROLLS &&
                mode != View.OVER_SCROLL_NEVER) {
            mode = View.OVER_SCROLL_IF_CONTENT_SCROLLS;
        }
        this.overScrollMode = mode;
    }

    @SimpleProperty(category = PropertyCategory.APPEARANCE, 
        description = "The scroll position is the same as the number of pixels that "
            + "are hidden from view above the scrollable area. "
            + "If the scroll bar is at the very top, or if the element is not scrollable, this number will be 0.")
    public int ScrollPosition() {
        int dxPosition = dx2px(scrollView.getScrollY());
        if (dxPosition < 0) {
            return 0;
        } else if (dxPosition > MaxScrollPosition()) {
            return MaxScrollPosition();
        }
        return dxPosition;
    }

    @SimpleProperty(category = PropertyCategory.APPEARANCE, 
        description = "Return the maximum position that the ScrollArrangement can reach")
    public int MaxScrollPosition() {
        View view = (View) scrollView.getChildAt(scrollView.getChildCount() - 1);
        return dx2px(view.getBottom() - scrollView.getHeight());
    }


    @SimpleFunction
    public void ScrollTop() {
        if (scrollView == null) {
            return;
        }
        scrollView.fullScroll(ScrollView.FOCUS_UP);
    }

    @SimpleFunction
    public void ScrollBottom() {
        if (scrollView == null) {
            return;
        }
        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
    }

    /**
     * Scroll up for half screen
     */
    @SimpleFunction
    public void ArrowScrollUpward() {
        if (scrollView == null) {
            return;
        }
        scrollView.arrowScroll(ScrollView.FOCUS_UP);
    }

    /**
     * Scroll down for half screen
     */
    @SimpleFunction
    public void ArrowScrollDownward() {
        if (scrollView == null) {
            return;
        }
        scrollView.arrowScroll(ScrollView.FOCUS_DOWN);
    }

    /*
     * Scroll up for one page
     */
    @SimpleFunction
    public void PageScrollUpward() {
        if (scrollView == null) {
            return;
        }
        scrollView.pageScroll(ScrollView.FOCUS_UP);
    }

    /*
     * Scroll down for one page
     */
    @SimpleFunction
    public void PageScrollDownward() {
        if (scrollView == null) {
            return;
        }
        scrollView.pageScroll(ScrollView.FOCUS_DOWN);
    }

    /**
     * Scroll to a specific location
     */
    @SimpleFunction
    public void ScrollTo(int px) {
        if (scrollView == null) {
            return;
        }
        scrollView.scrollTo(0, px2dx(px));
    }

    @SimpleFunction
    public void ScrollBy(int px) {
        if (scrollView == null) {
            return;
        }
        scrollView.scrollBy(0, px2dx(px));
    }

    @SimpleFunction
    public void SmoothScrollTo(int px) {
        if (scrollView == null) {
            return;
        }
        scrollView.smoothScrollTo(0, px2dx(px));
    }

    @SimpleFunction
    public void SmoothScrollBy(int px) {
        if (scrollView == null) {
            return;
        }
        scrollView.smoothScrollBy(0, px2dx(px));
    }

}