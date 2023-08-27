package com.example.finalproject1;

import static java.lang.Thread.sleep;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import org.tensorflow.lite.Interpreter;
import android.widget.TextView;
import android.widget.CompoundButton;

import android.content.Context;
import android.graphics.Matrix;

import android.hardware.Camera;
import android.view.Surface;
import android.view.View;
import android.widget.*;
import android.widget.FrameLayout;
import android.hardware.Camera.PictureCallback;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import android.os.Bundle;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View;

import kotlin.jvm.internal.Intrinsics;

public class MainActivity extends AppCompatActivity implements JNIListener{
    private static final String TAB = "CamTestActivity";
    private static final String TAG = "EdgeDetection";
    private DigitClassifier digitClassifier = new DigitClassifier((Context)this);

    //LED용
    private native static int openDriverLe(String path);
    private native static void closeDriverLe();
    private native static void writeDriverLe(byte[] data3, int length);

    //segment용
    private native static int openDriverSe(String path);
    private native static void closeDriverSe();
    private native static void writeDriverSe(byte[] data, int length);
    /* mnist 처리를 위한 value=--------------------
    private  Interpreter interpreter= null;
    private int inputImageWidth = 0; // will be inferred from TF Lite model.
    private int inputImageHeight  = 0; // will be inferred from TF Lite model.
    private int modelInputSize = 0; // will be inferred from TF Lite model.
    //-----------------------------------------*/

    private final int REQ_CODE_SELECT_IMAGE =100;
    TextView tv1;
    TextView tv2;
    TextView tv3;
    TextView tv4;

    String str2="";
    JNIDriver mDriver;
    boolean mThreadRun = true;

    int data_int, i;
    boolean mThreadRun1, mStart;
    SegmentThread mSegThread;

    private ImageView capturedImageHolder;
    private ImageView imageView1;
    private ImageView imageView2;

    public Bitmap bitmap1;
    public Bitmap bitmap2;
    public Bitmap bitmap3; //GPU blur
    public Bitmap bita;

    private Camera mCamera;
    private CameraPreview mPreview;

    ToggleButton[] mBtn= new ToggleButton[8];
    byte[] data3 = {0,0,0,0,0,0,0,0};
    byte[] data2= {1,0,1,0,1,0,1,0};
    public int a;
    public int b=0;
    static {
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV is not loaded");
        } else {
            Log.d(TAG, "OpenCV is loaded successfully");
        }
        System.loadLibrary("OpenCLDriver");
        //System.loadLibrary("JNIDriver");
    }
    //blur CPU
    public native Bitmap GaussianBlurBitmap(Bitmap bitmap);
    //blur GPU
    public native Bitmap GaussianBlurGPU(Bitmap bitmap);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView1=findViewById(R.id.imageView1);
        imageView2=findViewById(R.id.imageView2);

        tv1= (TextView)findViewById(R.id.textView1);
        tv2= (TextView)findViewById(R.id.textView2);
        tv3= (TextView)findViewById(R.id.textView3);
        tv4= (TextView)findViewById(R.id.textView4);


        Button btn = (Button) findViewById(R.id.button_capture);
        Button btn2 = (Button) findViewById(R.id.button2);
        Button btn2p = (Button) findViewById(R.id.button2prime);
        Button btn3 = (Button) findViewById(R.id.button3);
        Button btn4 = (Button) findViewById(R.id.button4);
        Button btn5 = (Button) findViewById(R.id.button5);
        Button btn6 = (Button) findViewById(R.id.button6);
        Button btn7 = (Button) findViewById(R.id.button7);


        capturedImageHolder = (ImageView) findViewById(R.id.captured_image);
        // Create an instance of Camera 33
        mCamera = getCameraInstance();
        mCamera.setDisplayOrientation(90);
        // Create our Preview view and set is as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        tv4 = (TextView)findViewById(R.id.textView4);
        mDriver = new JNIDriver();
        mDriver.setListener(this);
        if(mDriver.open("/dev/sm9s5422_interrupt")<0){
            Toast.makeText(MainActivity.this,"interrupt Driver Open Failed", Toast.LENGTH_SHORT).show();

        }
        if(openDriverLe("/dev/sm9s5422_led")<0){
            Toast.makeText(MainActivity.this, "LED Driver Open Failed", Toast.LENGTH_SHORT).show();
        }
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.takePicture(null, null, pictureCallback);
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if(bita!=null){
                    bita=bitmap1;
                }*/
                bitmap1=bita.copy(Bitmap.Config.ARGB_8888,true);; // 엣지시킬 이미지
                bitmap2=bita.copy(Bitmap.Config.ARGB_8888,true); ;// 오리지널이미지
                bitmap3=bita.copy(Bitmap.Config.ARGB_8888,true);;// GPU blur 이미지
                    detectEdge();
                    imageView1.setImageBitmap(bitmap1);
                /*
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQ_CODE_SELECT_IMAGE);*/
            }
        });


        // GPU blur
        btn2p.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bitmap3 = GaussianBlurGPU(bitmap3);
                imageView2.setImageBitmap(bitmap3);
                releaseCamera();

            }
        });



        btn3.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        // DigitClassifier 셋업


                                        digitClassifier.initialize();
                                        if (bitmap2 != null && digitClassifier.isInitialized()) {
                                            //오리지널 이미지 처리
                                            digitClassifier
                                                    .classifyAsync(bitmap2)
                                                    .addOnSuccessListener((OnSuccessListener) (new OnSuccessListener() {
                                                        public void onSuccess(Object var1) {
                                                            this.onSuccess((String) var1);
                                                        }

                                                        public final void onSuccess(String resultText) {
                                                            TextView var10000 = MainActivity.this.tv1;
                                                            if (var10000 != null) {
                                                                tv1.setText((CharSequence) resultText);
                                                                String[] k=resultText.split("\\n");
                                                                a=Integer.parseInt(k[1]);
                                                                Log.v(TAG, "a:"+a);
                                                            }

                                                        }
                                                    }))
                                                    .addOnFailureListener((OnFailureListener) (new OnFailureListener() {
                                                        public final void onFailure(Exception e) {
                                                            TextView var10000 = MainActivity.this.tv1;
                                                            if (var10000 != null) {
                                                                MainActivity var10001 = MainActivity.this;
                                                                Object[] var10003 = new Object[1];
                                                                Intrinsics.checkNotNullExpressionValue(e, "e");
                                                                var10003[0] = e.getLocalizedMessage();
                                                                //var10000.setText((CharSequence)var10001.getString(1900024, var10003));
                                                            }

                                                            Log.e("MainActivity", "Error classifying drawing.", (Throwable) e);
                                                        }
                                                    }));

                                        }
                                    }
                                });
                btn4.setOnClickListener(new View.OnClickListener() {
                                            public void onClick(View v) {

                                                digitClassifier.initialize();
                                                if (bitmap1 != null && digitClassifier.isInitialized()) {
                                                    //엣지 이미지 처리
                                                    digitClassifier
                                                            .classifyAsync(bitmap1)
                                                            .addOnSuccessListener((OnSuccessListener) (new OnSuccessListener() {
                                                                public void onSuccess(Object var1) {
                                                                    this.onSuccess((String) var1);
                                                                }

                                                                public final void onSuccess(String resultText) {
                                                                    TextView var10000 = MainActivity.this.tv2;
                                                                    if (var10000 != null) {
                                                                        tv2.setText((CharSequence) resultText);

                                                                    }

                                                                }
                                                            }))
                                                            .addOnFailureListener((OnFailureListener) (new OnFailureListener() {
                                                                public final void onFailure(Exception e) {
                                                                    TextView var10000 = MainActivity.this.tv2;
                                                                    if (var10000 != null) {
                                                                        MainActivity var10001 = MainActivity.this;
                                                                        Object[] var10003 = new Object[1];
                                                                        Intrinsics.checkNotNullExpressionValue(e, "e");
                                                                        var10003[0] = e.getLocalizedMessage();
                                                                        //var10000.setText((CharSequence)var10001.getString(1900024, var10003));
                                                                    }

                                                                    Log.e("MainActivity", "Error classifying drawing.", (Throwable) e);
                                                                }
                                                            }));


                                                }
                                            }
                                        });
                        btn5.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                digitClassifier.initialize();
                if (bitmap3 != null && digitClassifier.isInitialized()) {
                    //blur 이미지 처리
                    digitClassifier
                            .classifyAsync(bitmap3)
                            .addOnSuccessListener((OnSuccessListener)(new OnSuccessListener() {
                                public void onSuccess(Object var1) {
                                    this.onSuccess((String)var1);
                                }

                                public final void onSuccess(String resultText) {
                                    TextView var10000 = MainActivity.this.tv3;
                                    if (var10000 != null) {
                                        tv3.setText((CharSequence)resultText);

                                    }

                                }
                            }))
                            .addOnFailureListener((OnFailureListener)(new OnFailureListener() {
                                public final void onFailure(Exception e) {
                                    TextView var10000 = MainActivity.this.tv3;
                                    if (var10000 != null) {
                                        MainActivity var10001 = MainActivity.this;
                                        Object[] var10003 = new Object[1];
                                        Intrinsics.checkNotNullExpressionValue(e, "e");
                                        var10003[0] = e.getLocalizedMessage();
                                        //var10000.setText((CharSequence)var10001.getString(1900024, var10003));
                                    }

                                    Log.e("MainActivity", "Error classifying drawing.", (Throwable)e);
                                }
                            }));
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    digitClassifier.close();
                }
                                b= a;

            }

        });
                        //LED
        btn6.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    Log.d(TAG, "btn6 start");

                    Log.d(TAG, "btn6: b=a");

                    switch (a) {
                        case 0: {
                            data3[0] = 0;
                            data3[1] = 0;
                            data3[2] = 0;
                            data3[3] = 0;
                            data3[4] = 0;
                            data3[5] = 0;
                            data3[6] = 0;
                            data3[7] = 0;break;
                        }
                        case 1: {
                            data3[0] = 1;
                            data3[1] = 0;
                            data3[2] = 0;
                            data3[3] = 0;
                            data3[4] = 0;
                            data3[5] = 0;
                            data3[6] = 0;
                            data3[7] = 0;break;
                        }
                        case 2: {
                            data3[0] = 0;
                            data3[1] = 1;
                            data3[2] = 0;
                            data3[3] = 0;
                            data3[4] = 0;
                            data3[5] = 0;
                            data3[6] = 0;
                            data3[7] = 0;break;
                        }
                        case 3: {
                            data3[0] = 1;
                            data3[1] = 1;
                            data3[2] = 0;
                            data3[3] = 0;
                            data3[4] = 0;
                            data3[5] = 0;
                            data3[6] = 0;
                            data3[7] = 0;break;
                        }
                        case 4: {
                            data3[0] = 0;
                            data3[1] = 0;
                            data3[2] = 1;
                            data3[3] = 0;
                            data3[4] = 0;
                            data3[5] = 0;
                            data3[6] = 0;
                            data3[7] = 0;break;
                        }
                        case 5: {
                            data3[0] = 1;
                            data3[1] = 0;
                            data3[2] = 1;
                            data3[3] = 0;
                            data3[4] = 0;
                            data3[5] = 0;
                            data3[6] = 0;
                            data3[7] = 0;break;
                        }
                        case 6: {
                            data3[0] = 0;
                            data3[1] = 1;
                            data3[2] = 1;
                            data3[3] = 0;
                            data3[4] = 0;
                            data3[5] = 0;
                            data3[6] = 0;
                            data3[7] = 0;
                            break;
                        }
                        case 7: {
                            data3[0] = 1;
                            data3[1] = 1;
                            data3[2] = 1;
                            data3[3] = 0;
                            data3[4] = 0;
                            data3[5] = 0;
                            data3[6] = 0;
                            data3[7] = 0;
                            break;
                        }
                        case 8: {
                            data3[0] = 0;
                            data3[1] = 0;
                            data3[2] = 0;
                            data3[3] = 1;
                            data3[4] = 0;
                            data3[5] = 0;
                            data3[6] = 0;
                            data3[7] = 0;break;
                        }
                        case 9: {
                            data3[0] = 1;
                            data3[1] = 0;
                            data3[2] = 0;
                            data3[3] = 1;
                            data3[4] = 0;
                            data3[5] = 0;
                            data3[6] = 0;
                            data3[7] = 0;break;
                        }
                        /*
                    writeDriverLe(data2, data2.length);
                        data3[0] = (byte) (a % 100000000 / 1000000);
                        data3[1] = (byte) (a % 10000000 / 1000000);
                        data3[2] = (byte) (a % 1000000 / 100000);
                        data3[3] = (byte) (a % 100000 / 10000);
                        data3[4] = (byte) (a % 10000 / 1000);
                        data3[5] = (byte) (a % 1000 / 100);
                        data3[6] = (byte) (a % 100 / 10);
                        data3[7] = (byte) (a % 10);*/


                    }
                    Log.d(TAG, "btn6: data all allocated");
                    Log.v(TAG, "data:" + data3);
                    Log.v(TAG, "data length:" + data3.length);
                    writeDriverLe(data3, data3.length);
                    Log.d(TAG, "data write driver");
                }
    });

        btn7.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String str3 = String.valueOf(b);
                try {
                    data_int = Integer.parseInt(str3);
                    mStart = true;

                } catch (NumberFormatException E) {
                    Toast.makeText(MainActivity.this, "Input Error", Toast.LENGTH_SHORT).show();
                }
            }
        });

        }

    public Handler handler = new Handler(Looper.getMainLooper()){
        public void handleMessage(Message msg){
            switch(msg.arg1){
                case 1:
                    b+=10;
                    Log.v(TAG, "button b:" + b);
                    tv4.setText(String.valueOf(b));
                    break;
                case 2:
                    b-=10;
                    Log.v(TAG, "button b:" + b);
                    tv4.setText(String.valueOf(b));
                    break;
                case 3:
                    b-=1;
                    Log.v(TAG, "button b:" + b);
                    tv4.setText(String.valueOf(b));
                    break;
                case 4:
                    b+=1;
                    Log.v(TAG, "button b:" + b);
                    tv4.setText(String.valueOf(b));
                    break;
                case 5:
                    Log.v(TAG, "button b:" + b);
                    tv4.setText(String.valueOf(b));
                    //break;

            }

        }
    };
    private class SegmentThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (mThreadRun1) {
                byte[] n = {0, 0, 0, 0, 0, 0, 0};

                if (mStart == false) {writeDriverSe(n, n.length);
                } else {

                        n[0] = (byte) (data_int % 1000000 / 100000);
                        n[1] = (byte) (data_int % 100000 / 10000);
                        n[2] = (byte) (data_int % 10000 / 1000);
                        n[3] = (byte) (data_int % 1000 / 100);
                        n[4] = (byte) (data_int % 100 / 10);
                        n[5] = (byte) (data_int % 10);
                        writeDriverSe(n, n.length);

                }
            }
        }
    }
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use of does not exist)
        }
        return c;
    }


    PictureCallback pictureCallback = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();

            Matrix mtx = new Matrix();
            mtx.postRotate(180);
            mtx.postScale(0.25f,0.25f);
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);

            if (bitmap == null) {
                Toast.makeText(MainActivity.this, "Capture image is empty", Toast.LENGTH_LONG).show();
                return;
            }
            capturedImageHolder.setImageBitmap(scaleDownBitmapImage(rotatedBitmap, 450, 300));
            bita=scaleDownBitmapImage(rotatedBitmap, 450, 300);
        }
    };
    private Bitmap scaleDownBitmapImage(Bitmap bitmap, int newWidth, int newHeight) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        return resizedBitmap;
    }
    protected void onDestroy(){
        bitmap1.recycle();
        bitmap1=null;
        bita.recycle();
        bita=null;

        digitClassifier.close();
        super.onDestroy();
    }


    public String getImagePathFromURI(Uri contentUri){
        String[] proj={MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj,null,null,null );
        if (cursor==null){
            return contentUri.getPath();
        }
        else{
            int idx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String imgPath = cursor.getString(idx);
            cursor.close();
            return  imgPath;
        }
    }
    public  void detectEdge(){
        Mat src = new Mat();
        Utils.bitmapToMat(bitmap1,src); //bitmap을 Mat 형태로 변환 시킨다.
        Mat edge = new Mat();
        Imgproc.Canny(src,edge,50,150);//src에 Canny 효과를 적용 edge로
        Utils.matToBitmap(edge,bitmap1); //edge를 다시 Bitmap bita으로 변환
        src.release();
        edge.release();
    }


    @Override
    protected void onPause() {
        closeDriverLe();
        mThreadRun1=false;
        mSegThread=null;
        mDriver.close();
        closeDriverSe();
        super.onPause();
        releaseMediaRecorder(); // if you are using MediaRecorder, release it first
        releaseCamera(); // release the camera immediately on pause event
    }
    private void releaseMediaRecorder(){mCamera.lock();} // lock camera for later use

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release(); // release the camera for other applications
            mCamera = null;
        }
    }
    @Override
    protected void onResume(){
        //TODO Auto-generated method stub
        if(openDriverLe("/dev/sm9s5422_led")<0){
            Toast.makeText(MainActivity.this, "Driver Open Failed", Toast.LENGTH_SHORT).show();
        }
        if(openDriverSe("/dev/sm9s5422_segment")<0){
            Toast.makeText(MainActivity.this, "Driver Open Failed", Toast.LENGTH_SHORT).show();
        }
        mThreadRun1=true;
        mSegThread = new SegmentThread();
        mSegThread.start();
        super.onResume();
    }

    @Override
    public void onReceivebu(int val){
        Message text = Message.obtain();
        text.arg1=val;
        handler.sendMessage(text);
    }
}