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
import com.google.appinventor.components.runtime.HorizontalScrollArrangement;

import android.content.Context;
import android.util.Log;

import android.view.View;
import android.view.ViewTreeObserver;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

@DesignerComponent(version = HorizontalScrollHandler.VERSION,
    description = "by ColinTree at http://aix.colintree.cn/",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = "data:img/jpg;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAIVSURBVDhPlVNNj9JQFD1tIahTAgOhCQGRDkNilCGu3LkwMez8Cbp25z/wl7hxLxuWbIiGEHZkArivxBYQkJShTKAf9t52Oo07T3J7X1/POe+811aAj26361H/X7RaLUEgsXf5FfA8eK4D13OD7lfQ7agHc0F3HRup+UeI5BQRQrFHPSrfOOz+JbynwEFoNiA3Ft6JY2N/EAi4x41YHxr45KAoXhg1NOJUofDe0O/xBLQv6/c5TO0JdloVNzMVe/1xZHJYqLCMp7idP8Pt4jlOZonFhARdaFXHeInXr97wJGE8HmN784VNFOktrl5chU+Ab9+7cNOfeRxtQX50jmKxGFU+n4dji3BticfxZ/JZnsUENrh48IFv/kU99w4XmfdYrVZwHIfn+v0+TNPE5cNPEEUxMDiTatjtdjgej0wirNdr7P+k8OP6FxMNw8BisWBeuVyG/nPvJ3eDM6ATrlQq6HQ6LBYEAZlMhuMSiQSapuF0OkFVVSSTSTZiLn2J1WoVkiQFkWJF4tFohFqtxmQSZbNZHA4HbDYb2LYdvkY/AZHvarlcotfr8X4pDW2NVp3NZphMJmyuKAqnZgNC3GAwGECWZTQaDaTTaTYhs0KhgFKphOFwiFQqxVuKDAgkptOmmPP5nIuIuq6j2WyyOJfLoV6vYzqdYrvdgn/ndrsdftn3oMhEoFUpsmVZvAAhkUjwmSmKgr/4UX5IUCJy/wAAAABJRU5ErkJggg==")

@SimpleObject(external = true)

public class HorizontalScrollHandler extends AndroidNonvisibleComponent implements Component {

    public static final int VERSION = 3;

    private ComponentContainer container;
    private Context context;
    private static final String LOG_TAG = "HorizontalScrollHandler";
    
    private int oldScrollX=0;

    private boolean userControl=true;
    private boolean scrollBarEnabled=true;
    private boolean fadingEdgeEnabled=true;
    private int overScrollMode=View.OVER_SCROLL_IF_CONTENT_SCROLLS;

    private HorizontalScrollView scrollView=null;
    
    public HorizontalScrollHandler(ComponentContainer container) {
        super(container.$form());
        this.container = container;
        context = (Context) container.$context();
        Log.d(LOG_TAG, LOG_TAG+" Created" );
    }
    
    /**
     * <p>
     * greater than 1 usually, making the px numbers smaller than dx numbers.
     * which means a px contains to more than one dx
     * </p>
     * e.g. on my P9 plus, once i got 3 from this, and width of an button in px is 30, and dx is 90.
     */
    private float deviceDensity(){
        return container.$form().deviceDensity();
    }
    /**
     * px (appinventor unit) to dx (android unit)
    */
    private int px2dx(int px){
        return Math.round(px * deviceDensity());
    }
    /**
     * dx (android unit) to px (appinventor unit)
     */
    private int dx2px(int dx){
        return Math.round(dx / deviceDensity());
    }
    private float dx2px(float dx){
        return dx / deviceDensity();
    }

    
    @SimpleFunction
    public void RegisterScrollView(HorizontalScrollArrangement horizontalScrollArrangement){
        scrollView=(HorizontalScrollView)horizontalScrollArrangement.getView();
        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged(){
                onScroll();
            }
        });
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            private boolean touchDownDetected=false;
            private int touchDownScrollX=0;
            private int touchDownPointerId;
            private float touchDownPointerX;
            @Override
            public boolean onTouch(View v, MotionEvent event){
                int action=event.getActionMasked();
                if(action==MotionEvent.ACTION_DOWN){
                    onTouchDown(event);
                }else if(action==MotionEvent.ACTION_MOVE){
                    if(onMove(event)){
                        return true;
                    }
                }else if(action==MotionEvent.ACTION_UP || action==MotionEvent.ACTION_CANCEL){
                    onTouchUp(event);
                }
                return !UserControl(); // => (UserControl()?false:true);
            }
            private void onTouchDown(MotionEvent event){
                TouchDown();
                touchDownDetected=true;
                touchDownScrollX=dx2px(scrollView.getScrollX());
                touchDownPointerId=dx2px(event.getPointerId(0));
                touchDownPointerX=dx2px(event.getX(0));
            }
            private boolean onMove(MotionEvent event){
                if(touchDownDetected==true){
                    int currentScrollX=dx2px(scrollView.getScrollX());
                    float currentPointerX=dx2px(event.getX(event.findPointerIndex(touchDownPointerId)));

                    if(touchDownScrollX<=0 && currentScrollX<=0){
                        return OverScrollRight(currentPointerX-touchDownPointerX);
                    }
                    int max=MaxScrollPosition();
                    if(touchDownScrollX>=(max) && currentScrollX>=(max)){
                        return OverScrollLeft(touchDownPointerX-currentPointerX);
                    }
                }else{
                    onTouchDown(event);
                    return onMove(event);
                }
                return false;
            }
            private void onTouchUp(MotionEvent event){
                TouchUp();
                touchDownDetected=false;
            }
        });
        OverScrollMode(OverScrollMode());
        ScrollBarEnabled(ScrollBarEnabled());
        FadingEdgeEnabled(FadingEdgeEnabled());
    }

    @SimpleEvent
    public void ReachLeftEnd(){
        EventDispatcher.dispatchEvent(this, "ReachLeftEnd");
    }
    @SimpleEvent
    public void ReachRightEnd(){
        EventDispatcher.dispatchEvent(this, "ReachRightEnd");
    }
    @SimpleEvent
    public void ScrollChanged(int scrollX){
        EventDispatcher.dispatchEvent(this, "ScrollChanged", scrollX);
        if(scrollX==0){
            ReachLeftEnd();
        }else{
            if(MaxScrollPosition()-ScrollPosition()<=0){
                ReachRightEnd();
            }
        }
    }
    public void onScroll(){
        int scrollX=dx2px(scrollView.getScrollX());
        if(scrollX<0){
            scrollX=0;
        }
        if(oldScrollX!=scrollX){
            ScrollChanged(scrollX);
            oldScrollX=scrollX;
        }
    }
    @SimpleEvent
    public void TouchDown(){
        EventDispatcher.dispatchEvent(this, "TouchDown");
    }
    @SimpleEvent
    public void TouchUp(){
        EventDispatcher.dispatchEvent(this, "TouchUp");
    }

    @SimpleEvent
    public boolean OverScrollRight(float displacement){
        if(displacement>0){
            return EventDispatcher.dispatchEvent(this, "OverScrollRight", displacement);
        }
        return false;
    }
    @SimpleEvent
    public boolean OverScrollLeft(float displacement){
        if(displacement>0){
            return EventDispatcher.dispatchEvent(this, "OverScrollLeft", displacement);
        }
        return false;
    }


    @SimpleProperty
    public boolean UserControl(){
        return userControl;
    }
    @SimpleProperty
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
    public void UserControl(boolean enable){
        userControl=enable;
    }
    
    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public boolean ScrollBarEnabled() {
        return scrollBarEnabled;
    }
    @SimpleProperty
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
    public void ScrollBarEnabled(boolean enabled){
        this.scrollBarEnabled=enabled;
        if(scrollView!=null){
            ((HorizontalScrollView)scrollView).setHorizontalScrollBarEnabled(enabled);
        }
    }

    @SimpleProperty(category = PropertyCategory.APPEARANCE)
    public boolean FadingEdgeEnabled() {
        return fadingEdgeEnabled;
    }
    @SimpleProperty
    @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BOOLEAN, defaultValue = "True")
    public void FadingEdgeEnabled(boolean enabled){
        this.fadingEdgeEnabled=enabled;
        if(scrollView!=null){
            ((HorizontalScrollView)scrollView).setHorizontalFadingEdgeEnabled(enabled);
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
    public void OverScrollMode(int mode){
        if(mode!=View.OVER_SCROLL_ALWAYS && mode!=View.OVER_SCROLL_IF_CONTENT_SCROLLS && mode!=View.OVER_SCROLL_NEVER){
            mode=View.OVER_SCROLL_IF_CONTENT_SCROLLS;
        }
        this.overScrollMode=mode;
    }

    @SimpleProperty(category = PropertyCategory.APPEARANCE, 
        description = "The scroll position is the same as the number of pixels that "
            + "are hidden from view above the scrollable area. "
            + "If the scroll bar is at the very left, or if the element is not scrollable, this number will be 0.")
    public int ScrollPosition() {
        int dxPosition = dx2px(scrollView.getScrollX());
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
        return dx2px(view.getRight() - scrollView.getWidth());
    }

    // go top or go bottom
    @SimpleFunction
    public void ScrollLeftEnd() {
        if(scrollView==null){
            return;
        }
        scrollView.fullScroll(HorizontalScrollView.FOCUS_LEFT);
    }
    @SimpleFunction
    public void ScrollRightEnd() {
        if(scrollView==null){
            return;
        }
        scrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
    }
    // arrow scroll (in the test it looks like half screen scrolling)
    @SimpleFunction
    public void ArrowScrollLeftward() {
        if(scrollView==null){
            return;
        }
        scrollView.arrowScroll(HorizontalScrollView.FOCUS_LEFT);
    }
    @SimpleFunction
    public void ArrowScrollRightward() {
        if(scrollView==null){
            return;
        }
        scrollView.arrowScroll(HorizontalScrollView.FOCUS_RIGHT);
    }
    // pageScroll up and down (for one page each)
    @SimpleFunction
    public void PageScrollLeftward() {
        if(scrollView==null){
            return;
        }
        scrollView.pageScroll(HorizontalScrollView.FOCUS_LEFT);
    }
    @SimpleFunction
    public void PageScrollRightward() {
        if(scrollView==null){
            return;
        }
        scrollView.pageScroll(HorizontalScrollView.FOCUS_RIGHT);
    }
    // scroll to
    @SimpleFunction
    public void ScrollTo(int px) {
        if(scrollView==null){
            return;
        }
        scrollView.scrollTo(px2dx(px), 0);
    }
    // scroll by
    @SimpleFunction
    public void ScrollBy(int px) {
        if(scrollView==null){
            return;
        }
        scrollView.scrollBy(px2dx(px), 0);
    }
    // smooth scroll to
    @SimpleFunction
    public void SmoothScrollTo(int px) {
        if(scrollView==null){
            return;
        }
        scrollView.smoothScrollTo(px2dx(px), 0);
    }
    // smooth scroll by
    @SimpleFunction
    public void SmoothScrollBy(int px) {
        if(scrollView==null){
            return;
        }
        scrollView.smoothScrollBy(px2dx(px), 0);
    }

}