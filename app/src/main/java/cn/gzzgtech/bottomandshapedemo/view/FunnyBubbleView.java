package cn.gzzgtech.bottomandshapedemo.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.PointFEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.LinearInterpolator;
import cn.gzzgtech.bottomandshapedemo.R;

/**
 * 主要功能:
 *
 * @Prject: BottomAndShapeDemo
 * @Package: cn.gzzgtech.bottomandshapedemo.view
 * @author: Abraham
 * @date: 2018年12月24日 18:58
 * @Copyright: 个人版权所有
 * @Company:
 * @version: 1.0.0
 */

public class FunnyBubbleView extends View {
    /**
     * 气泡的状态
     * */
    public enum State{
        DEFAULT, //默认状态即静止状态
        CONNECTING, //连接状态即范围内连接状态
        APART, //分离状态即气泡已被拖拽到范围外
        DISAPPEARING, //开始消失动画
        DISAPPEARED, //气泡已经消失
    }

    private final int MAX_DISTANCE_MULTIPLE_OF_RADIUS = 8; //可移动距离相对于可移动气泡半径的倍数即movableBubbleRadius * 8
    private final int MOVABLE_DISTANCE_RATIO_OF_MAX_DISTANCE = 4; //当状态为default的时候，当手指按下的点离圆(非圆心，圆边坐标)的多长的距离可开始移动，这里设置的距离为最大距离的多少分之一，即 MAX_DISTANCE_MULTIPLE_OF_RADIUS / 4

    private String text; //显示的文字
    private float textSize = 10; //显示文字的大小,默认为10
    private int textColor = Color.WHITE; //显示的文字的颜色,默认为白色

    private float movableBubbleRadius = 15; //可移动气泡大小,默认为15
    private float stillBubbleRadius = 15; //不动气泡的半径

    private int bubbleColor = Color.RED; //气泡颜色, 默认颜色为红色

    private State state = State.DEFAULT; //记录当前气泡状态

    private Paint bubblePaint; //气泡画笔
    private Paint textPaint; //文字画笔

    private PointF stillCircleCenterPoint; //静止的气泡的圆心点
    private PointF movableCircleCenterPoint; //可移动的气泡的圆心点

    private float distance; //移动之后，两个圆圆心之间的距离
    private float maxDistance; //最大的可移动距离，超过则认为为分离状态
    private float movableDistance; //当状态为default的时候，当手指按下的点离圆(非圆心，圆边坐标)的多长的距离可开始移动

    private Path bezierPath; //贝塞尔曲线路径
    private OnBubbleStateChangeListener bubbleStateChangeListener;

    private ValueAnimator movableBubbleReboundAnimator; //可移动气泡的回弹动画
    private ValueAnimator movableBubbleDisappearAnimator; //可移动气泡的消失动画

    private Bitmap[] disappearAnimFrames; //消失动画每一帧的
    private int currentDisappearAnimFrameIndex; //当前消失动画的帧下标

    public FunnyBubbleView(Context context) {
        super(context);
        init();
    }

    public FunnyBubbleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        parseAttr(attrs);
        init();
    }

    public FunnyBubbleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        parseAttr(attrs);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public FunnyBubbleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        parseAttr(attrs);
        init();
    }

    private void init(){
        bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG); //抗锯齿
        bubblePaint.setColor(bubbleColor);
        bubblePaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);

        bezierPath = new Path();

        //消失动画每一帧图片id
        int frameIds[] = new int[]{R.mipmap.bubble_burst_frame_1, R.mipmap.bubble_burst_frame_2,
            R.mipmap.bubble_burst_frame_3, R.mipmap.bubble_burst_frame_4, R.mipmap.bubble_burst_frame_5};

        disappearAnimFrames = new Bitmap[frameIds.length];

        for (int i = 0; i < frameIds.length; i++){
            disappearAnimFrames[i] = BitmapFactory.decodeResource(getResources(), frameIds[i]);
        }
    }

    /**
     * 视图的大小发生改变后的回调
     * */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        initSize(w, h);
    }

    /**
     * 初始化view宽高
     * */
    private void initSize(int width, int height){
        //设置两气泡圆心初始坐标
        stillCircleCenterPoint = new PointF(width / 2,height / 2);
        movableCircleCenterPoint = new PointF(width / 2,height / 2);

        state = State.DEFAULT;
    }

    /**
     * 解析属性
     * */
    private void parseAttr(AttributeSet attrs){
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.FunnyBubbleView);

        int count = a.getIndexCount();
        for (int i = 0; i < count; i++){
            int attr = a.getIndex(i);
            switch (attr){
                case R.styleable.FunnyBubbleView_radius:
                    movableBubbleRadius = a.getDimension(attr, 15);
                    break;

                case R.styleable.FunnyBubbleView_color:
                    bubbleColor = a.getColor(attr, Color.RED);
                    break;

                case R.styleable.FunnyBubbleView_text:
                    TypedValue typedValue = a.peekValue(attr);
                    if (typedValue != null) {
                        if (typedValue.type == TypedValue.TYPE_REFERENCE) {
                            text = getResources().getString(a.getResourceId(i, 0));
                        } else if (typedValue.type == TypedValue.TYPE_STRING){
                            text = a.getString(attr);
                        }
                    }
                    break;

                case R.styleable.FunnyBubbleView_textColor:
                    textColor = a.getColor(attr, Color.WHITE);
                    break;

                case R.styleable.FunnyBubbleView_textSize:
                    textSize = a.getDimension(attr, 10);
                    break;

                default: break;

            }
        }
        a.recycle();

        maxDistance = movableBubbleRadius * MAX_DISTANCE_MULTIPLE_OF_RADIUS; //设置最大移动距离为半径的8倍
        movableDistance = maxDistance / MOVABLE_DISTANCE_RATIO_OF_MAX_DISTANCE; //这里设置当手指按下的点在最大可移动距离的四分之一以内可进行气泡移动，否则还是静止状态
        stillBubbleRadius = movableBubbleRadius;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //若两个气泡相连
        if (state == State.CONNECTING){
            drawConnectingState(canvas);
        }

        //若不为消失状态，绘制移动气泡
        if (state != State.DISAPPEARED
            && state != State.DISAPPEARING){
            drawMovableBubble(canvas);
        }

        //若为消失动画执行中状态，绘制消失动画的帧bitmap
        if (state == State.DISAPPEARING){
            Rect rect = new Rect((int)(movableCircleCenterPoint.x - movableBubbleRadius),
                (int)(movableCircleCenterPoint.y - movableBubbleRadius),
                (int)(movableCircleCenterPoint.x + movableBubbleRadius),
                (int)(movableCircleCenterPoint.y + movableBubbleRadius));

            canvas.drawBitmap(disappearAnimFrames[currentDisappearAnimFrameIndex],null,
                rect, bubblePaint);
        }
    }

    /**
     * 画可移动的气泡
     * */
    private void drawMovableBubble(Canvas canvas){
        canvas.drawCircle(movableCircleCenterPoint.x, movableCircleCenterPoint.y, movableBubbleRadius, bubblePaint);

        //画text
        text = text == null ? "" : text;
        Rect bounds = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), bounds); //计算得到要显示的文字的宽高
        canvas.drawText(text, movableCircleCenterPoint.x - bounds.width() / 2, movableCircleCenterPoint.y + bounds.height() / 2, textPaint); //文字居中
    }

    /**
     * 绘制两个气泡相连
     * */
    private void drawConnectingState(Canvas canvas){
        //画静止气泡，静止气泡会根据移动的距离增加而变小
        canvas.drawCircle(stillCircleCenterPoint.x, stillCircleCenterPoint.y, stillBubbleRadius, bubblePaint);

        //先计算两个圆心连接直线中心坐标
        float anchorX = (movableCircleCenterPoint.x + stillCircleCenterPoint.x) / 2;
        float anchorY = (movableCircleCenterPoint.y + stillCircleCenterPoint.y) / 2;

        //计算圆心直接连线与X轴的夹角的角度
        //分别得到对边/斜边，邻边/斜边的值
        float sin = (movableCircleCenterPoint.y - stillCircleCenterPoint.y) / distance;
        float cos = (movableCircleCenterPoint.x - stillCircleCenterPoint.x) / distance;

        //静止气泡的开始点和结束点坐标
        float stillBubbleStartX = stillCircleCenterPoint.x - sin * stillBubbleRadius;
        float stillBubbleStartY = stillCircleCenterPoint.y + cos * stillBubbleRadius;
        float stillBubbleEndX = stillCircleCenterPoint.x + sin * stillBubbleRadius;
        float stillBubbleEndY = stillCircleCenterPoint.y - cos * stillBubbleRadius;


        //可移动气泡的开始点和结束点坐标
        float movableBubbleStartX = movableCircleCenterPoint.x - movableBubbleRadius * sin;
        float movableBubbleStartY = movableCircleCenterPoint.y + movableBubbleRadius * cos;
        float movableBubbleEndX = movableCircleCenterPoint.x + movableBubbleRadius * sin;
        float movableBubbleEndY = movableCircleCenterPoint.y - movableBubbleRadius * cos;

        bezierPath.reset();
        bezierPath.moveTo(stillBubbleStartX, stillBubbleStartY); //先移动至静止气泡的起始点
        bezierPath.quadTo(anchorX, anchorY, movableBubbleStartX, movableBubbleStartY); //绘制贝塞尔曲线
        bezierPath.lineTo(movableBubbleEndX, movableBubbleEndY);//再移动至可移动气泡的结束点
        bezierPath.quadTo(anchorX, anchorY, stillBubbleEndX, stillBubbleEndY); //再次绘制贝塞尔曲线
        bezierPath.close(); //闭合path

        canvas.drawPath(bezierPath, bubblePaint); //绘制path
    }

    /**
     * 要显示的内容
     *
     * @param text 内容
     * */
    public void setText(String text) {
        this.text = text;

        state = State.DEFAULT;
        invalidate();
    }

    /**
     * touch down事件
     * */
    private void onTouchDown(MotionEvent event){
        if (state == State.DISAPPEARED ||
            state == State.DISAPPEARING){ //若气泡消失，则不处理
            return;
        }

        distance = (float) Math.hypot(event.getX() - stillCircleCenterPoint.x,
            event.getY() - stillCircleCenterPoint.y); //计算手指按下的点离静止圆圆心的距离

        if (distance < (movableBubbleRadius + movableDistance)){ //气泡是否可移动
            state = State.CONNECTING; //气泡开始连接
        }else{
            state = State.DEFAULT;
        }

        notifyStateChange();
    }

    /**
     * touch up事件
     * */
    private void onTouchUp(MotionEvent event) {
        if(state == State.CONNECTING){
            startMovableBubbleReboundAnim();
        }else if(state == State.APART){
            if(distance < maxDistance){ //若不超过最长可移动范围，则执行回弹动画
                startMovableBubbleReboundAnim();
            }else{ //否则执行消失动画
                startBubbleDisappearAnim();
            }
        }
    }

    /**
     * touch move事件
     * */
    private void onTouchMove(MotionEvent event) {
        if (state == State.DEFAULT){
            return;
        }

        //将可移动的气泡的圆心移至touch down的点
        movableCircleCenterPoint.x = event.getX();
        movableCircleCenterPoint.y = event.getY();

        distance = (float) Math.hypot(event.getX() - stillCircleCenterPoint.x,
            event.getY() - stillCircleCenterPoint.y); //计算手指按下的点离静止圆圆心的距离，即计算两点之间的距离

        if(state == State.CONNECTING){
            if(distance < (maxDistance - movableDistance)){
                //这里伴随着两个气泡之间移动距离的增加，静止的气泡大小会按比例变小，这里我们为了不让静止气泡过小，设置超过maxDistance - movableDistance的时候，则设置为分离状态
                stillBubbleRadius = movableBubbleRadius - distance / MAX_DISTANCE_MULTIPLE_OF_RADIUS;
            }else{
                state = State.APART;
                notifyStateChange();
            }
        }

        //刷新
        invalidate();
    }

    /**
     * 开始可移动气泡回弹动画，恢复到DEFAULT状态
     * */
    private void startMovableBubbleReboundAnim(){
        //可移动气泡动画回弹效果
        movableBubbleReboundAnimator = ValueAnimator.ofObject(new PointFEvaluator(),
            new PointF(movableCircleCenterPoint.x, movableCircleCenterPoint.y),
            new PointF(stillCircleCenterPoint.x, stillCircleCenterPoint.y));

        movableBubbleReboundAnimator.setDuration(150);
        movableBubbleReboundAnimator.setInterpolator(new AnticipateOvershootInterpolator());

        movableBubbleReboundAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                movableCircleCenterPoint = (PointF) animation.getAnimatedValue();
                invalidate();
            }
        });

        movableBubbleReboundAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                state = State.DEFAULT;
                notifyStateChange();
            }
        });

        movableBubbleReboundAnimator.start();
    }

    /**
     * 开始气泡消失效果
     * */
    private void startBubbleDisappearAnim(){
        //做一个int型属性动画，从0~mBurstDrawablesArray.length结束
        movableBubbleDisappearAnimator = ValueAnimator.ofInt(0, disappearAnimFrames.length);
        movableBubbleDisappearAnimator.setInterpolator(new LinearInterpolator());
        movableBubbleDisappearAnimator.setDuration(500);
        movableBubbleDisappearAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //设置当前绘制的爆炸图片index
                currentDisappearAnimFrameIndex = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
        movableBubbleDisappearAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                state = State.DISAPPEARED;
                notifyStateChange();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                state = State.DISAPPEARING;
                notifyStateChange();
            }
        });
        movableBubbleDisappearAnimator.start();
    }

    /**
     * 通知状态改变
     * */
    private void notifyStateChange(){
        if (bubbleStateChangeListener != null){
            bubbleStateChangeListener.onStateChange(state);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                onTouchDown(event);
                break;

            case MotionEvent.ACTION_MOVE:
                onTouchMove(event);
                break;

            case MotionEvent.ACTION_UP:
                onTouchUp(event);
                break;

            default:
                break;
        }

        return true;
    }

    /**
     * 恢复到起始状态
     * */
    public void reset(){
        if (movableBubbleReboundAnimator != null) {
            movableBubbleReboundAnimator.cancel();
        }

        if (movableBubbleDisappearAnimator != null){
            movableBubbleDisappearAnimator.cancel();
        }

        initSize(getWidth(), getHeight()); //重新设置圆心坐标
        state = State.DEFAULT; //状态重置
        notifyStateChange(); //通知变化
        invalidate(); //重新绘制
    }

    public void setBubbleStateChangeListener(OnBubbleStateChangeListener bubbleStateChangeListener) {
        this.bubbleStateChangeListener = bubbleStateChangeListener;
    }

    /**
     * 自定义状态监听器
     * */
    public interface OnBubbleStateChangeListener{
        /**
         * 状态改变
         *
         * @param state 当前状态
         * */
        void onStateChange(State state);
    }
}
