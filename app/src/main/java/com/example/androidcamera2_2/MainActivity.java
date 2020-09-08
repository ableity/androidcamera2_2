package com.example.androidcamera2_2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class  MainActivity extends AppCompatActivity {
    TextureView textureView;
    TextureView.SurfaceTextureListener surfaceTextureListener;
    CameraManager cameraManager;
    CameraDevice.StateCallback cam_stateCallback;
    CameraDevice opened_camera;
    Surface texture_surface;
    CameraCaptureSession.StateCallback cam_capture_session_stateCallback;
    CameraCaptureSession cameraCaptureSession;
    CaptureRequest.Builder requestBuilder;
    CaptureRequest.Builder requestBuilder_image_reader;
    ImageReader imageReader;
    Surface imageReaderSurface;
    Bitmap bitmap;
    CaptureRequest request;
    CaptureRequest takephoto_request;
    Button takephoto_btn;
    ImageView takephoto_imageView;
    Button zoom;
    Button zoom_d;
    Button takephoto_continus;
    Rect zoomValue = null;
    int zoomValue_int = 0;
    int img_name = 0;
    private static final int COMPLETED = 0;
    int capture_num = 10;
    int space_time = 1000;
    //判定设置的返回值
    int TIME = 0;
    int NUM = 1;




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //创建菜单，菜单的项目在menu目录下的main.xml
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.set_time) {
            //点击菜单中设置时间的按钮
            Toast.makeText(this, "you clicked set_time", Toast.LENGTH_SHORT).show();
            //往SecondActivity传送信息，第一个参数为启动活动的上下文，第二个参数为目标的活动
            Intent intent = new Intent(MainActivity.this, SecondActivity.class);
            //第一个参数为键，第二个为数据
            intent.putExtra("space_time", space_time);
            intent.putExtra("flag",TIME);
            startActivityForResult(intent, 1);
        }
        else if (item.getItemId() == R.id.set_num){
            //点击菜单中设置数量的按钮
            Toast.makeText(this, "you clicked set_num", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, SecondActivity.class);
            intent.putExtra("capture_num", capture_num);
            intent.putExtra("flag",NUM);
            //requestCode是为了标识从哪个intent回来的数据（下方的onActivityResult中使用）
            startActivityForResult(intent, 1);
        }
        else if (item.getItemId() == R.id.socket){
            //点击菜单中设置数量的按钮
            Toast.makeText(this, "you clicked socket", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, socket.class);
            startActivity(intent);
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d("requestCode", String.valueOf(requestCode));
        Log.d("resultCode", String.valueOf(resultCode));
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                //根据flag选择间隔或者数量进行设置
                if(data.getIntExtra("flag", 1000)==TIME)
                {
                    //获取时间信息并设置到全局信息
                    space_time = data.getIntExtra("space_time", 1000);
                    Log.d("spacetime", String.valueOf(space_time));
                }
                else if(data.getIntExtra("flag", 1000)==NUM)
                {
                    //设置间隔到全局信息
                    capture_num = data.getIntExtra("capture_num", 400);
                    Log.d("capture_num", String.valueOf(capture_num));
                }
            }
            else{
                Log.d("resultCode:", "error");
            }
        } else {
            Log.d("default", "default");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textureView=findViewById(R.id.texture_view_camera2);
        takephoto_btn=findViewById(R.id.btn_camera2_takephoto);
        takephoto_imageView= findViewById(R.id.image_view_preview_image);
        takephoto_continus = findViewById(R.id.btn_camera2_takephoto_continus);

        zoom = findViewById(R.id.zoom);
        zoom_d = findViewById(R.id.zoom_d);
        //设置画面预览监听参数
        surfaceTextureListener=new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                texture_surface=new Surface(textureView.getSurfaceTexture());
                openCamera();
            }
            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            }
            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }
            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            }
        };

        //画面预览监听函数
        textureView.setSurfaceTextureListener(surfaceTextureListener);
        //B1. 准备工作：初始化ImageReader
        imageReader = ImageReader.newInstance(1080  ,1920, ImageFormat.JPEG,2);
        //B2. 准备工作：设置ImageReader收到图片后的回调函数及保存图像
        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                //B2.1 接收图片：从ImageReader中读取最近的一张，转成Bitmap
                Image image= reader.acquireLatestImage();
                ByteBuffer buffer= image.getPlanes()[0].getBuffer();
                int length= buffer.remaining();
                byte[] bytes= new byte[length];
                buffer.get(bytes);
                image.close();
                bitmap = BitmapFactory.decodeByteArray(bytes,0,length);
                int w = bitmap.getWidth();
                int h = bitmap.getHeight();
                Matrix m = new Matrix();
                m.postRotate(90);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, m, true);
                //B2.2 显示图片
                takephoto_imageView.setImageBitmap(bitmap);
                Log.d("外置目录",String.valueOf(getExternalCacheDir()));
                saveBitmap(bitmap, getExternalCacheDir()+"/"+String.valueOf(img_name)+".jpg");

            }
        },null);
        //B3 配置：获取ImageReader的Surface
        imageReaderSurface = imageReader.getSurface();

        //B4. 相机点击事件
        takephoto_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //B4.1 配置request的参数 拍照模式(这行代码要调用已启动的相机 opened_camera，所以不能放在外面

                    try {
                        requestBuilder_image_reader = opened_camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                    requestBuilder_image_reader.set(CaptureRequest.JPEG_ORIENTATION,90);
                    requestBuilder_image_reader.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
                    if(zoomValue!=null)
                    {
                        requestBuilder_image_reader.set(CaptureRequest.SCALER_CROP_REGION, zoomValue);
                    }
                    //B4.2 配置request的参数 的目标对象
                    requestBuilder_image_reader.addTarget(imageReaderSurface );
                    try {
                        //B4.3 触发拍照
                        cameraCaptureSession.capture(requestBuilder_image_reader.build(), null, null);
                        //设置拍照的命名
                        img_name++;
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }


            }
        });

        //设置连续拍照
        takephoto_continus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Timer timer = new Timer();
                img_name=0;
                final TextView led = (TextView) findViewById(R.id.led);
                led.setText("拍摄中");
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        if(img_name>=capture_num){
                            led.setText("拍摄结束");
                            this.cancel();
                            return;
                        }
                        Message message = new Message();
                        message.what = COMPLETED;
                        handler.sendMessage(message);
                    }

                    @Override
                    public boolean cancel() {
                        return super.cancel();
                    }
                },0,space_time);
            }
        });
        //设置缩放
        zoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (zoomValue_int<45) {
                    zoomValue_int = zoomValue_int + 5;
                }
                else if (zoomValue_int<=49)
                {
                    zoomValue_int =zoomValue_int+1;
                }
                else
                {
                    return;
                }
                //zoomValue_int = 50;
                CameraCharacteristics mCameraCharacteristics = null;
                try {
                    mCameraCharacteristics = cameraManager.getCameraCharacteristics("0");
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
                Rect rect2 = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
                int radio2 = mCameraCharacteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM).intValue() / 3;
                int realRadio2 = mCameraCharacteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM).intValue();
                float maxzoom = mCameraCharacteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM).floatValue()*10;


//                int centerX2 = rect2.centerX();
//                int centerY2 = rect2.centerY();
//                int minMidth2 = (rect2.right - ((i * centerX2) / 100 / radio2) - 1) - 20;
//                int minHeight2 = (rect2.bottom - ((i * centerY2) / 100 / radio2) - 1) - 20;
//                if (minMidth2 < rect2.right / realRadio2 || minHeight2 < rect2.bottom / realRadio2) {
//                    Log.i("sb_zoom", "sb_zoomsb_zoomsb_zoom");
//                    return;
//                }
//                zoomValue = new Rect(20, 20, rect2.right - ((i * centerX2) / 100 / radio2) - 1, rect2.bottom - ((i * centerY2) / 100 / radio2) - 1);
//                Log.i("sb_zoom", "left--->" + "20" + ",,,top--->" + "20" + ",,,right--->" + (rect2.right - ((i * centerX2) / 100 / radio2) - 1) + ",,,bottom--->" + (rect2.bottom - ((i * centerY2) / 100 / radio2) - 1));


                int minW =(int)(rect2.width()/maxzoom);
                int minH =(int)(rect2.height()/maxzoom);
                int difW = rect2.width() -  minW;
                int difH = rect2.height() -  minH;
                int cropW = difW / 100 *(int)zoomValue_int;
                int cropH = difH / 100 *(int)zoomValue_int;
                cropW  -= cropW& 3;
                cropH  -= cropH& 3;
                zoomValue = new Rect(cropW, cropH, rect2.width()-cropW,rect2.height()-cropH);

                requestBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoomValue);
                try {
                    cameraCaptureSession.setRepeatingRequest(requestBuilder.build(),null,null);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        });
        zoom_d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (zoomValue_int>=46) {
                    zoomValue_int = zoomValue_int-1;
                }
                else if (zoomValue_int>5 && zoomValue_int<=45)
                {
                    zoomValue_int =zoomValue_int-5;
                }
                else
                {
                    return;
                }
                CameraCharacteristics mCameraCharacteristics = null;
                try {
                    mCameraCharacteristics = cameraManager.getCameraCharacteristics("0");
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
                Rect rect2 = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
                float maxzoom = mCameraCharacteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM).floatValue()*10;


//                int centerX2 = rect2.centerX();
//                int centerY2 = rect2.centerY();
//                int minMidth2 = (rect2.right - ((i * centerX2) / 100 / radio2) - 1) - 20;
//                int minHeight2 = (rect2.bottom - ((i * centerY2) / 100 / radio2) - 1) - 20;
//                if (minMidth2 < rect2.right / realRadio2 || minHeight2 < rect2.bottom / realRadio2) {
//                    Log.i("sb_zoom", "sb_zoomsb_zoomsb_zoom");
//                    return;
//                }
//                zoomValue = new Rect(20, 20, rect2.right - ((i * centerX2) / 100 / radio2) - 1, rect2.bottom - ((i * centerY2) / 100 / radio2) - 1);
//                Log.i("sb_zoom", "left--->" + "20" + ",,,top--->" + "20" + ",,,right--->" + (rect2.right - ((i * centerX2) / 100 / radio2) - 1) + ",,,bottom--->" + (rect2.bottom - ((i * centerY2) / 100 / radio2) - 1));


                int minW =(int)(rect2.width()/maxzoom);
                int minH =(int)(rect2.height()/maxzoom);
                int difW = rect2.width() -  minW;
                int difH = rect2.height() -  minH;
                int cropW = difW / 100 *(int)zoomValue_int;
                int cropH = difH / 100 *(int)zoomValue_int;
                cropW  -= cropW& 3;
                cropH  -= cropH& 3;
                zoomValue = new Rect(cropW, cropH, rect2.width()-cropW,rect2.height()-cropH);

                requestBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoomValue);
                try {
                    cameraCaptureSession.setRepeatingRequest(requestBuilder.build(),null,null);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void openCamera() {
        cameraManager= (CameraManager) getSystemService(Context.CAMERA_SERVICE);  // 初始化
        cam_stateCallback=new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                opened_camera=camera;
                try {
                    requestBuilder = opened_camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                    requestBuilder.addTarget(texture_surface);

                    request = requestBuilder.build();
                    cam_capture_session_stateCallback=new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            cameraCaptureSession=session;
                            try {
                                session.setRepeatingRequest(request,null,null);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }
                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                        }
                    };
                    opened_camera.createCaptureSession( Arrays.asList(texture_surface,imageReaderSurface), cam_capture_session_stateCallback,null);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {
            }
            @Override
            public void onError(@NonNull CameraDevice camera, int error) {
            }
        };
        checkPermission();
        try {
            cameraManager.openCamera(cameraManager.getCameraIdList()[0],cam_stateCallback,null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void checkPermission() {
        // 检查是否申请了权限
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CAMERA)){

            }else{
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},1);
            }
        }
    }

    public static void saveBitmap(Bitmap bitmap,String path) {
        String savePath;
        File filePic;
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            savePath = path;
        } else {
            Log.e("tag", "saveBitmap failure : sdcard not mounted");
            return;
        }
        try {
            filePic = new File(savePath);
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            Log.e("tag", "saveBitmap: " + e.getMessage());
            return;
        }
        Log.i("tag", "saveBitmap success: " + filePic.getAbsolutePath());
    }


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == COMPLETED) {
                 //UI更改操作
                takephoto_btn.performClick();
            }
        }
    };



    @Override
    protected void onResume() {
        super.onResume();
        // 如果 textureView可用，就直接打开相机
//        if(textureView.isAvailable()){
//            openCamera();
//        }else{
//            // 否则，就开启它的可用时监听。
//            textureView.setSurfaceTextureListener(surfaceTextureListener);
//        }
    }
    @Override
    protected void onPause() {
//        // 先把相机的session关掉
//        if(cameraCaptureSession!=null){
//            cameraCaptureSession.close();
//        }
//        // 再关闭相机
//        if(null!=opened_camera){
//            opened_camera.close();
//        }
//        // 最后关闭ImageReader
//        if(null!=imageReader){
//            imageReader.close();
//        }
//        // 最后交给父View去处理
        super.onPause();
    }

}