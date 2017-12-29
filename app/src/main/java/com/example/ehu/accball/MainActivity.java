package com.example.ehu.accball;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class MainActivity extends AppCompatActivity implements SensorEventListener, SurfaceHolder.Callback {

    public final String TAG = getClass().getSimpleName();
    SensorManager mSensorManager;
    Sensor mAccSensor;
    SurfaceHolder mHolder;
    int mSurfaceWidth;  //サーフェスビューの幅
    int mSurfaceHeight; //サーフェスビューの高さ
    static final float RADIUS = 50.0f;    //ボールを描画するための定数
    static final float COEF = 100.0f;   //ボールの移動量を調整するための係数

    float mBallX;   //ボールの現在のx座標
    float mBallY;   //ボールの現在のx座標
    float mVX;      //ボールのx軸方向への速度
    float mVY;      //ボールのx軸方向への速度

    long mFrom;     //前回、センサーから加速度を取得した時間
    long mTo;       //今回、センサーから加速度を取得した時間

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        //Androidに搭載されているサービスのマネージャークラスの生成
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //サービスの中から、加速度センサーを選ぶ
        mAccSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        SurfaceView surfaceView = findViewById(R.id.surfaceView);
        mHolder = surfaceView.getHolder();
        mHolder.addCallback(this);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            Log.d(TAG,
                    "x=" + String.valueOf(event.values[0]) +
                            "y=" + String.valueOf(event.values[1]) +
                            "z=" + String.valueOf(event.values[2]));
            float x = -event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            mTo = System.currentTimeMillis();
            float t = (float) (mTo - mFrom);
            t = t / 1000.0f;

            float dx = mVX * t + x * t + t / 2.0f;
            float dy = mVY * t + x * t + t / 2.0f;
            mBallX = mBallX + dx * COEF;
            mBallY = mBallY + dy * COEF;
            mVX = mVX + x * t;
            mVY = mVY + y + t;

            if (mBallX - RADIUS < 0 && mVX < 0) {
                mVX = -mVX / 1.5f;
                mBallX = RADIUS;
            } else if (mBallX + RADIUS > mSurfaceWidth && mVX > 0) {
                mVX = -mVX / 1.5f;
                mBallX = mSurfaceWidth - RADIUS;
            }
            if (mBallY - RADIUS < 0 && mVY < 0) {
                mVY = -mVY / 1.5f;
                mBallY = RADIUS;
            } else if (mBallY + RADIUS > mSurfaceHeight && mVY > 0) {
                mVY = -mVY / 1.5f;
                mBallY = mSurfaceHeight - RADIUS;
            }

            mFrom = System.currentTimeMillis();
            drawCanvas();
        }

    }

    private void drawCanvas() {
        Canvas c = mHolder.lockCanvas();
        c.drawColor(Color.YELLOW);
        Paint paint = new Paint();
        paint.setColor(Color.MAGENTA);
        c.drawCircle(mBallX, mBallY, RADIUS, paint);
        mHolder.unlockCanvasAndPost(c);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mFrom = System.currentTimeMillis();
        //加速度センサーの開始
        mSensorManager.registerListener(this, mAccSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mSurfaceWidth = width;
        mSurfaceHeight = height;
        mBallX = width / 2;
        mBallY = height / 2;
        mVX = 0;
        mVY = 0;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        //加速度センサーの停止
        mSensorManager.unregisterListener(this);
    }
}
