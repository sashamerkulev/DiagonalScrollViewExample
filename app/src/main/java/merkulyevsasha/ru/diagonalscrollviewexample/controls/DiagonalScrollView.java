package merkulyevsasha.ru.diagonalscrollviewexample.controls;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EdgeEffect;
import android.widget.FrameLayout;
import android.widget.OverScroller;

/**
 * Created by sasha_merkulev on 15.05.2018.
 */
public class DiagonalScrollView extends FrameLayout{
    private final static String ERROR_MESSAGE = "DiagonalScrollView can host only one direct child";

    /**
     * Sentinel value for no current active pointer.
     * Used by {@link #mActivePointerId}.
     */
    private static final int INVALID_POINTER = -1;

    private OverScroller mScroller;
    private EdgeEffect mEdgeGlowTop;
    private EdgeEffect mEdgeGlowBottom;
    private EdgeEffect mEdgeGlowLeft;
    private EdgeEffect mEdgeGlowRight;

    /**
     * Position of the last motion event.
     */
    private int mLastMotionY;
    private int mLastMotionX;

    /**
     * True if the user is currently dragging this ScrollView around. This is
     * not the same as 'is being flinged', which can be checked by
     * mScroller.isFinished() (flinging begins when the user lifts his finger).
     */
    private boolean mIsBeingDragged = false;

    /**
     * Determines speed during touch scrolling
     */
    private VelocityTracker mVelocityTracker;

    private int mTouchSlop;
    private int mMinimumVelocity;
    private int mMaximumVelocity;

    private int mOverscrollDistance;
    private int mOverflingDistance;

    /**
     * ID of the active pointer. This is used to retain consistency during
     * drags/flings if multiple pointers are used.
     */
    private int mActivePointerId = INVALID_POINTER;

    /**
     * Used during scrolling to retrieve the new offset within the window.
     */
    private final int[] mScrollOffset = new int[2];
    private final int[] mScrollConsumed = new int[2];
    private int mNestedYOffset;

    private ScrollChangedListener onScrollChanged;


    public DiagonalScrollView(Context context) {
        this(context, null);
    }

    public DiagonalScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DiagonalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public DiagonalScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initScrollView();
    }

    public void setScrollViewListener(ScrollChangedListener onScrollChanged){
        this.onScrollChanged = onScrollChanged;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (onScrollChanged != null){
            onScrollChanged.onScrollChanged(this, l, t, oldl, oldt);
        }
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return true;
    }

    @Override
    protected float getTopFadingEdgeStrength() {
        if (getChildCount() == 0) {
            return 0.0f;
        }

        final int length = getVerticalFadingEdgeLength();
        if (getScrollY() < length) {
            return getScrollY() / (float) length;
        }

        return 1.0f;
    }

    @Override
    protected float getBottomFadingEdgeStrength() {
        if (getChildCount() == 0) {
            return 0.0f;
        }

        final int length = getVerticalFadingEdgeLength();
        final int bottomEdge = getHeight() - getPaddingBottom();
        final int span = getChildAt(0).getBottom() - getScrollY() - bottomEdge;
        if (span < length) {
            return span / (float) length;
        }

        return 1.0f;
    }


    @Override
    protected float getLeftFadingEdgeStrength() {
        if (getChildCount() == 0) {
            return 0.0f;
        }

        final int length = getHorizontalFadingEdgeLength();
        if (getScrollX() < length) {
            return getScrollX() / (float) length;
        }

        return 1.0f;
    }

    @Override
    protected float getRightFadingEdgeStrength() {
        if (getChildCount() == 0) {
            return 0.0f;
        }

        final int length = getHorizontalFadingEdgeLength();
        final int rightEdge = getWidth() - getPaddingRight();
        final int span = getChildAt(0).getRight() - getScrollX() - rightEdge;
        if (span < length) {
            return span / (float) length;
        }

        return 1.0f;
    }

    private void initScrollView() {
        mScroller = new OverScroller(getContext());
        setFocusable(true);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        setWillNotDraw(false);
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mOverscrollDistance = configuration.getScaledOverscrollDistance();
        mOverflingDistance = configuration.getScaledOverflingDistance();
    }

    @Override
    public void addView(View child) {
        if (getChildCount() > 0) {
            throw new IllegalStateException(ERROR_MESSAGE);
        }

        super.addView(child);
    }

    @Override
    public void addView(View child, int index) {
        if (getChildCount() > 0) {
            throw new IllegalStateException(ERROR_MESSAGE);
        }

        super.addView(child, index);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        if (getChildCount() > 0) {
            throw new IllegalStateException(ERROR_MESSAGE);
        }

        super.addView(child, params);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (getChildCount() > 0) {
            throw new IllegalStateException(ERROR_MESSAGE);
        }

        super.addView(child, index, params);
    }

    private boolean inChild(int x, int y) {
        if (getChildCount() > 0) {
            final int scrollY = getScrollY();
            final View child = getChildAt(0);
            return !(y < child.getTop() - scrollY
                    || y >= child.getBottom() - scrollY
                    || x < child.getLeft()
                    || x >= child.getRight());
        }
        return false;
    }

    private void initOrResetVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (disallowIntercept) {
            recycleVelocityTracker();
        }
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        /*
         * This method JUST determines whether we want to intercept the motion.
         * If we return true, onMotionEvent will be called and we do the actual
         * scrolling there.
         */

        /*
         * Shortcut the most recurring case: the user is in the dragging
         * state and he is moving his finger.  We want to intercept this
         * motion.
         */
        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE) && (mIsBeingDragged)) {
            return true;
        }

        if (super.onInterceptTouchEvent(ev)) {
            return true;
        }

        /*
         * Don't try to intercept touch if we can't scroll anyway.
         */
//        if (getScrollY() == 0 && !canScrollVertically(1)) {
//            return false;
//        }
//        if (getScrollX() == 0 && !canScrollHorizontally(1)) {
//            return false;
//        }

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE: {
                /*
                 * mIsBeingDragged == false, otherwise the shortcut would have caught it. Check
                 * whether the user has moved far enough from his original down touch.
                 */

                /*
                 * Locally do absolute value. mLastMotionY is set to the y value
                 * of the down event.
                 */
                final int activePointerId = mActivePointerId;
                if (activePointerId == INVALID_POINTER) {
                    // If we don't have a valid id, the touch down wasn't on content.
                    break;
                }

                final int pointerIndex = ev.findPointerIndex(activePointerId);
                if (pointerIndex == -1) {
                    break;
                }

                final int y = (int) ev.getY(pointerIndex);
                final int yDiff = Math.abs(y - mLastMotionY);
                final int x = (int) ev.getX(pointerIndex);
                final int xDiff =  Math.abs(x - mLastMotionX);

                if (yDiff > mTouchSlop || xDiff > mTouchSlop /*&& (getNestedScrollAxes() & SCROLL_AXIS_VERTICAL) == 0*/) {
                    mIsBeingDragged = true;
                    mLastMotionX = x;
                    mLastMotionY = y;
                    initVelocityTrackerIfNotExists();
                    mVelocityTracker.addMovement(ev);
                    mNestedYOffset = 0;
//                    if (mScrollStrictSpan == null) {
//                        mScrollStrictSpan = StrictMode.enterCriticalSpan("ScrollView-scroll");
//                    }
                    final ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                }
                break;
            }

            case MotionEvent.ACTION_DOWN: {
                final int y = (int) ev.getY();
                final int x = (int) ev.getX();
                if (!inChild(x, y)) {
                    mIsBeingDragged = false;
                    recycleVelocityTracker();
                    break;
                }

                /*
                 * Remember location of down touch.
                 * ACTION_DOWN always refers to pointer index 0.
                 */
                mLastMotionY = y;
                mLastMotionX = x;
                mActivePointerId = ev.getPointerId(0);

                initOrResetVelocityTracker();
                mVelocityTracker.addMovement(ev);
                /*
                 * If being flinged and user touches the screen, initiate drag;
                 * otherwise don't. mScroller.isFinished should be false when
                 * being flinged. We need to call computeScrollOffset() first so that
                 * isFinished() is correct.
                 */
                mScroller.computeScrollOffset();
                mIsBeingDragged = !mScroller.isFinished();
//                if (mIsBeingDragged && mScrollStrictSpan == null) {
//                    mScrollStrictSpan = StrictMode.enterCriticalSpan("ScrollView-scroll");
//                }
                //startNestedScroll(SCROLL_AXIS_VERTICAL);
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                /* Release the drag */
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                recycleVelocityTracker();
                if (mScroller.springBack(getScrollX(), getScrollY(), 0, getScrollRangeX(), 0, getScrollRangeY())) {
                    postInvalidateOnAnimation();
                }
                //stopNestedScroll();
                break;
        }

        /*
         * The only time we want to intercept motion events is if we are in the
         * drag mode.
         */
        return mIsBeingDragged;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        initVelocityTrackerIfNotExists();

        MotionEvent vtev = MotionEvent.obtain(ev);

        final int actionMasked = ev.getActionMasked();

        if (actionMasked == MotionEvent.ACTION_DOWN) {
            mNestedYOffset = 0;
        }
        vtev.offsetLocation(0, mNestedYOffset);

        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN: {
                if (getChildCount() == 0) {
                    return false;
                }
                if ((mIsBeingDragged = !mScroller.isFinished())) {
                    final ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                }

                /*
                 * If being flinged and user touches, stop the fling. isFinished
                 * will be false if being flinged.
                 */
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }

                // Remember where the motion event started
                mLastMotionY = (int) ev.getY();
                mLastMotionX = (int) ev.getX();
                mActivePointerId = ev.getPointerId(0);
                //startNestedScroll(SCROLL_AXIS_VERTICAL);
                break;
            }
            case MotionEvent.ACTION_MOVE:
                final int activePointerIndex = ev.findPointerIndex(mActivePointerId);
                if (activePointerIndex == -1) {
                    break;
                }

                final int y = (int) ev.getY(activePointerIndex);
                int deltaY = mLastMotionY - y;

                final int x = (int) ev.getX(activePointerIndex);
                int deltaX = mLastMotionX - x;

                if (dispatchNestedPreScroll(deltaX, deltaY, mScrollConsumed, mScrollOffset)) {
                    deltaY -= mScrollConsumed[1];
                    deltaX -= mScrollConsumed[0];
                    vtev.offsetLocation(mScrollOffset[0], mScrollOffset[1]);
                    mNestedYOffset += mScrollOffset[1];
                }

                if (!mIsBeingDragged && (Math.abs(deltaY) > mTouchSlop || Math.abs(deltaX) > mTouchSlop)) {
                    final ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                    mIsBeingDragged = true;
                    if (deltaY > 0) {
                        deltaY -= mTouchSlop;
                    } else {
                        deltaY += mTouchSlop;
                    }
                    if (deltaX > 0) {
                        deltaX -= mTouchSlop;
                    } else {
                        deltaX += mTouchSlop;
                    }
                }

                if (mIsBeingDragged) {
                    // Scroll to follow the motion event
                    mLastMotionY = y - mScrollOffset[1];
                    mLastMotionX = x - mScrollOffset[0];

                    final int oldX = getScrollX();
                    final int oldY = getScrollY();
                    final int rangeY = getScrollRangeY();
                    final int rangeX = getScrollRangeX();
                    final int overscrollMode = getOverScrollMode();
                    boolean canOverscrollY = overscrollMode == OVER_SCROLL_ALWAYS || (overscrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS && rangeY > 0);
                    boolean canOverscrollX = overscrollMode == OVER_SCROLL_ALWAYS || (overscrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS && rangeX > 0);

                    // Calling overScrollBy will call onOverScrolled, which
                    // calls onScrollChanged if applicable.
                    if (overScrollBy(deltaX, deltaY, getScrollX(), getScrollY(),
                            rangeX, rangeY, mOverscrollDistance, mOverscrollDistance, true) && !hasNestedScrollingParent()) {
                        // Break our velocity if we hit a scroll barrier.
                        mVelocityTracker.clear();
                    }

                    final int scrolledDeltaY = getScrollY() - oldY;
                    final int unconsumedY = deltaY - scrolledDeltaY;
                    final int scrolledDeltaX = getScrollX() - oldX;
                    final int unconsumedX = deltaX - scrolledDeltaX;
                    if (dispatchNestedScroll(scrolledDeltaX, scrolledDeltaY, unconsumedX, unconsumedY, mScrollOffset)) {
                        mLastMotionY -= mScrollOffset[1];
                        vtev.offsetLocation(0, mScrollOffset[1]);
                        mNestedYOffset += mScrollOffset[1];
                    } else {
                        if (canOverscrollY) {
                            final int pulledToY = oldY + deltaY;
                            if (pulledToY < 0) {
                                mEdgeGlowTop.onPull((float) deltaY / getHeight(),
                                        ev.getX(activePointerIndex) / getWidth());
                                if (!mEdgeGlowBottom.isFinished()) {
                                    mEdgeGlowBottom.onRelease();
                                }
                            } else if (pulledToY > rangeY) {
                                mEdgeGlowBottom.onPull((float) deltaY / getHeight(),
                                        1.f - ev.getX(activePointerIndex) / getWidth());
                                if (!mEdgeGlowTop.isFinished()) {
                                    mEdgeGlowTop.onRelease();
                                }
                            }
                            if (mEdgeGlowTop != null
                                    && (!mEdgeGlowTop.isFinished() || !mEdgeGlowBottom.isFinished())) {
                                postInvalidateOnAnimation();
                            }
                        }

                        if (canOverscrollX) {
                            final int pulledToX = oldX + deltaX;
                            if (pulledToX < 0) {
                                mEdgeGlowLeft.onPull((float) deltaX / getWidth(),
                                        1.f - ev.getY(activePointerIndex) / getHeight());
                                if (!mEdgeGlowRight.isFinished()) {
                                    mEdgeGlowRight.onRelease();
                                }
                            } else if (pulledToX > rangeX) {
                                mEdgeGlowRight.onPull((float) deltaX / getWidth(),
                                        ev.getY(activePointerIndex) / getHeight());
                                if (!mEdgeGlowLeft.isFinished()) {
                                    mEdgeGlowLeft.onRelease();
                                }
                            }
                            if (mEdgeGlowLeft != null
                                    && (!mEdgeGlowLeft.isFinished() || !mEdgeGlowRight.isFinished())) {
                                postInvalidateOnAnimation();
                            }
                        }

                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mIsBeingDragged) {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int initialYVelocity = (int) velocityTracker.getYVelocity(mActivePointerId);
                    int initialXVelocity = (int) velocityTracker.getXVelocity(mActivePointerId);

                    if (Math.abs(initialYVelocity) > mMinimumVelocity || Math.abs(initialXVelocity) > mMinimumVelocity) {
                        flingWithNestedDispatch(-initialXVelocity, -initialYVelocity);
                    } else if (mScroller.springBack(getScrollX(), getScrollY(), 0, getScrollRangeX(), 0, getScrollRangeY())) {
                        postInvalidateOnAnimation();
                    }

                    mActivePointerId = INVALID_POINTER;
                    endDrag();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mIsBeingDragged && getChildCount() > 0) {
                    if (mScroller.springBack(getScrollX(), getScrollY(), 0, getScrollRangeX(), 0, getScrollRangeY())) {
                        postInvalidateOnAnimation();
                    }
                    mActivePointerId = INVALID_POINTER;
                    endDrag();
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN: {
                final int index = ev.getActionIndex();
                mLastMotionY = (int) ev.getY(index);
                mLastMotionX = (int) ev.getX(index);
                mActivePointerId = ev.getPointerId(index);
                break;
            }
        }

        if (mVelocityTracker != null) {
            mVelocityTracker.addMovement(vtev);
        }
        vtev.recycle();
        return true;
    }

    @Override
    protected void onOverScrolled(int scrollX, int scrollY,
                                  boolean clampedX, boolean clampedY) {
        // Treat animating scrolls differently; see #computeScroll() for why.
        if (!mScroller.isFinished()) {
            final int oldX = getScrollX();
            final int oldY = getScrollY();
            scrollTo(scrollX, scrollY);
            onScrollChanged(getScrollX(), getScrollY(), oldX, oldY);
        } else {
            super.scrollTo(scrollX, scrollY);
        }

        awakenScrollBars();
    }

    @Override
    public CharSequence getAccessibilityClassName() {
        return DiagonalScrollView.class.getName();
    }

    private int getScrollRangeY() {
        int scrollRange = 0;
        if (getChildCount() > 0) {
            View child = getChildAt(0);
            scrollRange = Math.max(0, child.getHeight() - (getHeight() - getPaddingBottom() - getPaddingTop()));
        }
        return scrollRange;
    }

    private int getScrollRangeX() {
        int scrollRange = 0;
        if (getChildCount() > 0) {
            View child = getChildAt(0);
            scrollRange = Math.max(0, child.getWidth() - (getWidth() - getPaddingLeft() - getPaddingRight()));
        }
        return scrollRange;
    }

    /**
     * <p>The scroll range of a scroll view is the overall height of all of its
     * children.</p>
     */
    @Override
    protected int computeVerticalScrollRange() {
        final int count = getChildCount();
        final int contentHeight = getHeight() - getPaddingBottom() - getPaddingTop();
        if (count == 0) {
            return contentHeight;
        }

        int scrollRange = getChildAt(0).getBottom();
        final int scrollY = getScrollY();
        final int overscrollBottom = Math.max(0, scrollRange - contentHeight);
        if (scrollY < 0) {
            scrollRange -= scrollY;
        } else if (scrollY > overscrollBottom) {
            scrollRange += scrollY - overscrollBottom;
        }

        return scrollRange;
    }

    @Override
    protected int computeVerticalScrollOffset() {
        return Math.max(0, super.computeVerticalScrollOffset());
    }

    /**
     * <p>The scroll range of a scroll view is the overall width of all of its
     * children.</p>
     */
    @Override
    protected int computeHorizontalScrollRange() {
        final int count = getChildCount();
        final int contentWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        if (count == 0) {
            return contentWidth;
        }

        int scrollRange = getChildAt(0).getRight();
        final int scrollX = getScrollX();
        final int overscrollRight = Math.max(0, scrollRange - contentWidth);
        if (scrollX < 0) {
            scrollRange -= scrollX;
        } else if (scrollX > overscrollRight) {
            scrollRange += scrollX - overscrollRight;
        }

        return scrollRange;
    }

    @Override
    protected int computeHorizontalScrollOffset() {
        return Math.max(0, super.computeHorizontalScrollOffset());
    }

    @Override
    protected void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {

        final int verticalPadding = getPaddingTop() + getPaddingBottom();
        final int horizontalPadding = getPaddingLeft() + getPaddingRight();

        final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                Math.max(0, MeasureSpec.getSize(parentHeightMeasureSpec) - verticalPadding),
                MeasureSpec.UNSPECIFIED);

        final int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                Math.max(0, MeasureSpec.getSize(parentWidthMeasureSpec) - horizontalPadding),
                MeasureSpec.UNSPECIFIED);

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    @Override
    protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

        final int usedTotalHeight = getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin +
                heightUsed;

        final int usedTotalWidth = getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin +
                widthUsed;

        final int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                Math.max(0, MeasureSpec.getSize(parentWidthMeasureSpec) - usedTotalWidth),
                MeasureSpec.UNSPECIFIED);

        final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                Math.max(0, MeasureSpec.getSize(parentHeightMeasureSpec) - usedTotalHeight),
                MeasureSpec.UNSPECIFIED);

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            // This is called at drawing time by ViewGroup.  We don't want to
            // re-show the scrollbars at this point, which scrollTo will do,
            // so we replicate most of scrollTo here.
            //
            //         It's a little odd to call onScrollChanged from inside the drawing.
            //
            //         It is, except when you remember that computeScroll() is used to
            //         animate scrolling. So unless we want to defer the onScrollChanged()
            //         until the end of the animated scrolling, we don't really have a
            //         choice here.
            //
            //         I agree.  The alternative, which I think would be worse, is to post
            //         something and tell the subclasses later.  This is bad because there
            //         will be a window where getScrollX()/Y is different from what the app
            //         thinks it is.
            //
            int oldX = getScrollX();
            int oldY = getScrollY();
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();

            if (oldX != x || oldY != y) {
                final int overscrollMode = getOverScrollMode();

                final int rangeY = getScrollRangeY();
                final boolean canOverscrollY = overscrollMode == OVER_SCROLL_ALWAYS || (overscrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS && rangeY > 0);

                final int rangeX = getScrollRangeX();
                final boolean canOverscrollX = overscrollMode == OVER_SCROLL_ALWAYS || (overscrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS && rangeX > 0);

                overScrollBy(x - oldX, y - oldY, oldX, oldY, rangeX, rangeY, mOverflingDistance, mOverflingDistance, false);
                onScrollChanged(getScrollX(), getScrollY(), oldX, oldY);

                if (canOverscrollY) {
                    if (y < 0 && oldY >= 0) {
                        mEdgeGlowTop.onAbsorb((int) mScroller.getCurrVelocity());
                    } else if (y > rangeY && oldY <= rangeY) {
                        mEdgeGlowBottom.onAbsorb((int) mScroller.getCurrVelocity());
                    }
                }

                if (canOverscrollX) {
                    if (x < 0 && oldX >= 0) {
                        mEdgeGlowLeft.onAbsorb((int) mScroller.getCurrVelocity());
                    } else if (x > rangeX && oldX <= rangeX) {
                        mEdgeGlowRight.onAbsorb((int) mScroller.getCurrVelocity());
                    }
                }

            }

            if (!awakenScrollBars()) {
                // Keep on drawing until the animation has finished.
                postInvalidateOnAnimation();
            }
        }
    }

    /**
     * Fling the scroll view
     *
     * @param velocityY The initial velocity in the Y direction. Positive
     *                  numbers mean that the finger/cursor is moving down the screen,
     *                  which means we want to scroll towards the top.
     */
    public void fling(int velocityX, int velocityY) {
        if (getChildCount() > 0) {
            int height = getHeight() - getPaddingBottom() - getPaddingTop();
            int bottom = getChildAt(0).getHeight();
            int width = getWidth() - getPaddingRight() - getPaddingLeft();
            int right = getChildAt(0).getWidth();

            mScroller.fling(getScrollX(), getScrollY(), velocityX, velocityY,
                    0, Math.max(0, right - width),
                    0, Math.max(0, bottom - height),
                    width/2, height/2);

            postInvalidateOnAnimation();
        }
    }

    private void flingWithNestedDispatch(int velocityX, int velocityY) {
        final boolean canFlingY = (getScrollY() > 0 || velocityY > 0) &&
                (getScrollY() < getScrollRangeY() || velocityY < 0);

        final boolean canFlingX = (getScrollX() > 0 || velocityX > 0) &&
                (getScrollX() < getScrollRangeX() || velocityX < 0);

        if (!dispatchNestedPreFling(velocityX, velocityY)) {
            dispatchNestedFling(velocityX, velocityY, canFlingY);
            if (canFlingY && canFlingX) {
                fling(velocityX, velocityY);
            } else {
                if (canFlingY) {
                    fling(0, velocityY);
                }
                if (canFlingX) {
                    fling(velocityX, 0);
                }
            }
        }
    }

    private void endDrag() {
        mIsBeingDragged = false;

        recycleVelocityTracker();

        if (mEdgeGlowTop != null) {
            mEdgeGlowTop.onRelease();
            mEdgeGlowBottom.onRelease();
        }

        if (mEdgeGlowLeft != null) {
            mEdgeGlowLeft.onRelease();
            mEdgeGlowRight.onRelease();
        }

    }

    /**
     * {@inheritDoc}
     *
     * <p>This version also clamps the scrolling to the bounds of our child.
     */
    @Override
    public void scrollTo(int x, int y) {
        // we rely on the fact the View.scrollBy calls scrollTo.
        if (getChildCount() > 0) {
            View child = getChildAt(0);
            x = clamp(x, getWidth() - getPaddingRight() - getPaddingLeft(), child.getWidth());
            y = clamp(y, getHeight() - getPaddingBottom() - getPaddingTop(), child.getHeight());
            if (x != getScrollX() || y != getScrollY()) {
                super.scrollTo(x, y);
            }
        }
    }

    @Override
    public void setOverScrollMode(int mode) {
        if (mode != OVER_SCROLL_NEVER) {
            Context context = getContext();
            if (mEdgeGlowTop == null) {
                mEdgeGlowTop = new EdgeEffect(context);
                mEdgeGlowBottom = new EdgeEffect(context);
            }
            if (mEdgeGlowLeft == null) {
                mEdgeGlowLeft = new EdgeEffect(context);
                mEdgeGlowRight = new EdgeEffect(context);
            }
        } else {
            mEdgeGlowTop = null;
            mEdgeGlowBottom = null;
            mEdgeGlowLeft = null;
            mEdgeGlowRight = null;
        }
        super.setOverScrollMode(mode);
    }

    private static int clamp(int n, int my, int child) {
        if (my >= child || n < 0) {
            /* my >= child is this case:
             *                    |--------------- me ---------------|
             *     |------ child ------|
             * or
             *     |--------------- me ---------------|
             *            |------ child ------|
             * or
             *     |--------------- me ---------------|
             *                                  |------ child ------|
             *
             * n < 0 is this case:
             *     |------ me ------|
             *                    |-------- child --------|
             *     |-- getScrollX() --|
             */
            return 0;
        }
        if ((my+n) > child) {
            /* this case:
             *                    |------ me ------|
             *     |------ child ------|
             *     |-- getScrollX() --|
             */
            return child-my;
        }
        return n;
    }


    public interface ScrollChangedListener {
        void onScrollChanged(FrameLayout scrollView, int x, int y, int oldx, int oldy);
    }
}

