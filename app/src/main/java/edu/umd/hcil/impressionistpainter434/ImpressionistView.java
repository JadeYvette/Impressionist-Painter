package edu.umd.hcil.impressionistpainter434;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.ColorFilter;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.ImageView;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Random;
import java.io.File;
import android.widget.Toast;
import android.support.v4.view.VelocityTrackerCompat;
import android.os.*;

/**
 * Created by jon on 3/20/2016.
 */
public class ImpressionistView extends View {

    public ImageView _imageView;

    private Canvas _offScreenCanvas = null;
    private Bitmap _offScreenBitmap = null;
    private Paint _paint = new Paint();

    private int _alpha = 125;
    private int _defaultRadius = 25;
    private Point _lastPoint = null;
    private long _lastPointTime = -1;
    private boolean _useMotionSpeedForBrushStrokeSize = true;
    private Paint _paintBorder = new Paint();
    private BrushType _brushType = BrushType.Square;
    private float _minBrushRadius = 5;
    private int pixelColor = 0;
    private VelocityTracker  mVelocityTracker;
    private float MAX_BRUSH_SIZE = 4;
    private int MAX_SPEED = 3000;
    private int imnum = 0;
    private String imageName = "CMSC434";

    public ImpressionistView(Context context) {
        super(context);
        init(null, 0);
    }

    public ImpressionistView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ImpressionistView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    /**
     * Because we have more than one constructor (i.e., overloaded constructors), we use
     * a separate initialization method
     * @param attrs
     * @param defStyle
     */
    private void init(AttributeSet attrs, int defStyle){

        // Set setDrawingCacheEnabled to true to support generating a bitmap copy of the view (for saving)
        // See: http://developer.android.com/reference/android/view/View.html#setDrawingCacheEnabled(boolean)
        //      http://developer.android.com/reference/android/view/View.html#getDrawingCache()
        this.setDrawingCacheEnabled(true);

        _paint.setColor(Color.RED);
        _paint.setAlpha(_alpha);
        _paint.setAntiAlias(true);
        _paint.setStyle(Paint.Style.FILL);
        _paint.setStrokeWidth(2);

        _paintBorder.setColor(Color.BLACK);
        _paintBorder.setStrokeWidth(3);
        _paintBorder.setStyle(Paint.Style.STROKE);
        _paintBorder.setAlpha(50);


        //_paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh){

        Bitmap bitmap = getDrawingCache();
        Log.v("onSizeChanged", MessageFormat.format("bitmap={0}, w={1}, h={2}, oldw={3}, oldh={4}", bitmap, w, h, oldw, oldh));
        if(bitmap != null) {
            _offScreenBitmap = getDrawingCache().copy(Bitmap.Config.ARGB_8888, true);
            _offScreenCanvas = new Canvas(_offScreenBitmap);


        }
    }

    /**
     * Sets the ImageView, which hosts the image that we will paint in this view
     * @param imageView
     */
    public void setImageView(ImageView imageView){
        _imageView = imageView;

    }

    /**
     * Sets the brush type. Feel free to make your own and completely change my BrushType enum
     * @param brushType
     */
    public void setBrushType(BrushType brushType){
        _brushType = brushType;
    }

    /**
     * Clears the painting
     * Works by drawing a white square the size of the canvas
     */
    public void clearPainting(){
        if(_offScreenCanvas != null) {
            Paint paintScreen = new Paint();
            paintScreen.setStyle(Paint.Style.FILL);
            paintScreen.setColor(Color.WHITE);
            _offScreenCanvas.drawRect(0, 0, this.getWidth(), this.getHeight(), paintScreen);
            invalidate();
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(_offScreenBitmap != null) {
            canvas.drawBitmap(_offScreenBitmap, 0, 0, _paint);
        }

        // Draw the border. Helpful to see the size of the bitmap in the ImageView
        canvas.drawRect(getBitmapPositionInsideImageView(_imageView), _paintBorder);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent){



        //TODO
        //Basically, the way this works is to liste for Touch Down and Touch Move events and determine where those
        //touch locations correspond to the bitmap in the ImageView. You can then grab info about the bitmap--like the pixel color--
        //at that location

        int touchX = (int) motionEvent.getX();
        int  touchY = (int) motionEvent.getY();
        int index = motionEvent.getActionIndex();
        int pointerId = motionEvent.getPointerId(index);

       Rect currRect = getBitmapPositionInsideImageView(_imageView);


        if( touchX > currRect.left &&
            touchX < currRect.right &&
            touchY > currRect.top &&
            touchY < currRect.bottom){


        }else{


            return false;
        }

        //VELOCITY METHOD BASED ON
        // http://stackoverflow.com/questions/5815975/get-speed-of-a-ontouch-action-move-event-in-android

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                _paint.setColor(getPixelColor(touchX, touchY));
                if (mVelocityTracker == null) {

                    // Retrieve a new VelocityTracker object to watch the velocity
                    // of a motion.
                    mVelocityTracker = VelocityTracker.obtain();
                } else {

                    // Reset the velocity tracker back to its initial state.
                    mVelocityTracker.clear();
                }

                // Add a user's movement to the tracker.
                mVelocityTracker.addMovement(motionEvent);
                break;
            case MotionEvent.ACTION_MOVE:

                _paint.setColor(getPixelColor(touchX, touchY));
                mVelocityTracker.addMovement(motionEvent);
                // When you want to determine the velocity, call
                // computeCurrentVelocity(). Then call getXVelocity()
                // and getYVelocity() to retrieve the velocity for each pointer ID.

                mVelocityTracker.computeCurrentVelocity(1000);
                speedPaint(pointerId,_paint);


                if( touchX > currRect.left &&
                        touchX < currRect.right &&
                        touchY > currRect.top &&
                        touchY < currRect.bottom){


                }else{

                    return false;
                }


                     if(_brushType.equals(BrushType.Square)){

                         _offScreenCanvas.drawRect(touchX - 2, touchY - 2,  touchX + _paint.getStrokeWidth() * _defaultRadius,
                                 touchY + _paint.getStrokeWidth() * _defaultRadius , _paint);
                     }


                    if(_brushType.equals(BrushType.Circle)){

                        _offScreenCanvas.drawCircle(touchX-4, touchY-4, _paint.getStrokeWidth() * 8, _paint);

                    }

                    if(_brushType.equals(BrushType.Line)){

                        _offScreenCanvas.drawLine(touchX, touchY, touchX + 6 * _defaultRadius,
                                touchY + _defaultRadius, _paint);

                    }

                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        invalidate();
        return true;
    }


    private int getPixelColor(int x, int y) {

            Bitmap colorMap = ((BitmapDrawable) _imageView.getDrawable()).getBitmap();
            pixelColor = colorMap.getPixel(x, y);

         //     int red  = Color.red(pixelColor);
        //     int green  = Color.green(pixelColor);
        //     int blue  = Color.blue(pixelColor);

return pixelColor;
    }





    /**
     * This method is useful to determine the bitmap position within the Image View. It's not needed for anything else
     * Modified from:
     *  - http://stackoverflow.com/a/15538856
     *  - http://stackoverflow.com/a/26930938
     * @param imageView
     * @return
     */
    private static Rect getBitmapPositionInsideImageView(ImageView imageView){
        Rect rect = new Rect();

        if (imageView == null || imageView.getDrawable() == null) {

            return rect;
        }

        // Get image dimensions
        // Get image matrix values and place them in an array
        float[] f = new float[9];
        imageView.getImageMatrix().getValues(f);

        // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];

        // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
        final Drawable d = imageView.getDrawable();
        final int origW = d.getIntrinsicWidth();
        final int origH = d.getIntrinsicHeight();

        // Calculate the actual dimensions
        final int widthActual = Math.round(origW * scaleX);
        final int heightActual = Math.round(origH * scaleY);

        // Get image position
        // We assume that the image is centered into ImageView
        int imgViewW = imageView.getWidth();
        int imgViewH = imageView.getHeight();

        int top = (int) (imgViewH - heightActual)/2;
        int left = (int) (imgViewW - widthActual)/2;

        rect.set(left, top, left + widthActual, top + heightActual);

        return rect;
    }


    //VELOCITY METHOD BASED ON
    // http://stackoverflow.com/questions/5815975/get-speed-of-a-ontouch-action-move-event-in-android
    public void speedPaint(int pointer ,Paint paintIn) {



        float xVelocity = VelocityTrackerCompat.getXVelocity(mVelocityTracker, pointer);
        float yVelocity = VelocityTrackerCompat.getYVelocity(mVelocityTracker, pointer);
        double speed = Math.sqrt(Math.pow(xVelocity, 2) + Math.pow(yVelocity, 2));

        if(speed > MAX_SPEED){

            speed = MAX_SPEED;
        }

        float brushSize = Math.round((MAX_BRUSH_SIZE * speed) / MAX_SPEED);

          if(brushSize < 1){
            brushSize = 1;
        }


        paintIn.setStrokeWidth(brushSize);
        }


    protected Drawable convertToGrayscale(Drawable drawable)
    {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);

        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);

        drawable.setColorFilter(filter);

        return drawable;
    }

   //TRANSFORMS PAINT AND CANVAS TO GRAYSCALE USING A FILTER
    public void toGrayscale()
    {
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        _paint.setColorFilter(f);
        _imageView.setColorFilter(f);

        invalidate();
    }


    //REMOVES COLOR FILTER
    public void revertGrayscale()
    {

        _paint.setColorFilter(null);
        _imageView.setColorFilter(null);

        invalidate();
    }


    //BASED ON http://stackoverflow.com/questions/13533471/how-to-save-view-from-canvas-to-png-file
    //SAVES PAINTING INTO IMAGE GALLERY OF DEVICE
    public void savePainting(Context context) {

        imageName = imageName + imnum;
        imnum++;
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),imageName);
        try {
            boolean compressSucceeded = _offScreenBitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));
            FileUtils.addImageToGallery(file.getAbsolutePath(), context);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }











}

