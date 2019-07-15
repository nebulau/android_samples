package com.example.myapplication.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.myapplication.PhotoShow;
import com.example.myapplication.R;
import com.example.myapplication.bean.PostVideoResponse;
import com.example.myapplication.network.IMiniDouyinService;
import com.example.myapplication.utils.ResourceUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.app.Activity.RESULT_OK;
import static android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT;
import static com.example.myapplication.utils.Utils.MEDIA_TYPE_IMAGE;
import static com.example.myapplication.utils.Utils.MEDIA_TYPE_VIDEO;
import static com.example.myapplication.utils.Utils.getOutputMediaFile;
import static com.example.myapplication.utils.Utils.rotateImage;

public class RecordUploadFragment extends Fragment {
    public static final String TAG = "RecordUploadFragment: ";
    private View view;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;
    private Camera mCamera;

    private int CAMERA_TYPE = Camera.CameraInfo.CAMERA_FACING_BACK;

    private boolean isRecording = false;
    private boolean lightCanOn = false;

    private int rotationDegree = 0;
    private Timer timer;
    private Camera.AutoFocusCallback myAutoFocusCallback = null;
    private final static int REQUEST_PERMISSION = 123;
    public Uri mSelectedImage;
    private Uri mSelectedVideo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        if (!checkPermissionAllGranted(mPermissionsArrays)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(mPermissionsArrays, REQUEST_PERMISSION);
            }
        } else {
//            Toast.makeText(getActivity(), "已经获取所有所需权限", Toast.LENGTH_SHORT).show();
        }
        view = inflater.inflate(R.layout.record_upload_fragment, container, false);
        mCamera = getCamera(CAMERA_TYPE);
        mSurfaceView = view.findViewById(R.id.img);
        //todo 给SurfaceHolder添加Callback
        mHolder = mSurfaceView.getHolder();
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                startPreview(surfaceHolder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                releaseCameraAndPreview();
            }
        });

//        timer = new Timer();
//        timer.schedule(new startFocus(),0,800);

        view.findViewById(R.id.take_photo).setOnClickListener(v -> {
            //todo 拍一张照片
            if (lightCanOn) {
                turnLightOn();
            }
            mCamera.takePicture(null, null, mPicture);
            if (lightCanOn) {
                turnLightOff();
            }
            Toast.makeText(getActivity(), "已保存", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.start_record).setOnClickListener(v -> {
            //todo 录制，第一次点击是start，第二次点击是stop
            if (isRecording) {
                //todo 停止录制
                Toast.makeText(getActivity(), "结束录制", Toast.LENGTH_SHORT).show();
                releaseMediaRecorder();
                isRecording = false;
                ImageView imageView = view.findViewById(R.id.start_record);
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.start_record));
            } else {
                //todo 录制
                mMediaRecorder = new MediaRecorder();
                mCamera.unlock();
                mMediaRecorder.setCamera(mCamera);
                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

                mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
                mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());
                mMediaRecorder.setPreviewDisplay(mHolder.getSurface());
                mMediaRecorder.setOrientationHint(rotationDegree);

                try {
                    mMediaRecorder.prepare();
                    mMediaRecorder.start();
                    Toast.makeText(getActivity(), "开始录制", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    releaseMediaRecorder();
                    e.printStackTrace();
                }
                ImageView imageView = view.findViewById(R.id.start_record);
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.stop_record));
                isRecording = true;
            }
        });

        view.findViewById(R.id.reverse_camera).setOnClickListener(v -> {
            //todo 切换前后摄像头
            if (CAMERA_TYPE == CAMERA_FACING_FRONT) {
                CAMERA_TYPE = Camera.CameraInfo.CAMERA_FACING_BACK;
            } else {
                CAMERA_TYPE = CAMERA_FACING_FRONT;
            }

            releaseCameraAndPreview();
            mCamera = getCamera(CAMERA_TYPE);
            startPreview(mHolder);
        });

        view.findViewById(R.id.zoom).setOnClickListener(v -> {
            //todo 调焦，需要判断手机是否支持
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean b, Camera camera) {

                }
            });
        });

        view.findViewById(R.id.upload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectStatus == 0) {
                    Toast.makeText(getActivity(), "先选择一张图片然后再点击", Toast.LENGTH_SHORT).show();
                    chooseImage();
                } else if (selectStatus == 1) {
                    Toast.makeText(getActivity(), "再选择一段视频然后再点击", Toast.LENGTH_SHORT).show();
                    chooseVideo();
                } else if (selectStatus == 2) {
                    if (mSelectedVideo != null && mSelectedImage != null) {
                        Toast.makeText(getActivity(), "正在上传请稍等", Toast.LENGTH_SHORT).show();
                        postVideo();
                        selectStatus = 3;
                    } else {
                        throw new IllegalArgumentException("error data uri, mSelectedVideo = " + mSelectedVideo + ", mSelectedImage = " + mSelectedImage);
                    }
                } else if (selectStatus == 3) {
                    selectStatus = 0;
                }
            }
        });

        view.findViewById(R.id.light_status).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (lightCanOn) {
                    //todo打开灯


                    ImageView imageView = view.findViewById(R.id.light_status);
                    imageView.setImageDrawable(getResources().getDrawable(R.drawable.light_off));
                    lightCanOn = false;
                } else {


                    ImageView imageView = view.findViewById(R.id.light_status);
                    imageView.setImageDrawable(getResources().getDrawable(R.drawable.light_on));
                    lightCanOn = true;
                }
            }
        });

        myAutoFocusCallback = new Camera.AutoFocusCallback() {
            public void onAutoFocus(boolean success, Camera
                    camera) {
                if (success)//success表示对焦成功
                {
                    Log.i("Warning", "myAutoFocusCallback: success...");
                } else {
                    Log.i("warning", "myAutoFocusCallback: 失败了...");
                }
            }
        };

        return view;
    }

    private void turnLightOn() {
        try {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters = mCamera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void turnLightOff() {
        try {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters = mCamera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Camera getCamera(int position) {
        CAMERA_TYPE = position;
        if (mCamera != null) {
            releaseCameraAndPreview();
        }
        Camera cam = Camera.open(position);

        //todo 摄像头添加属性，例是否自动对焦，设置旋转方向等
        int rotationDegree = getCameraDisplayOrientation(position);
        cam.setDisplayOrientation(rotationDegree);

        return cam;
    }

    private void prepareCamera() {
        mCamera = getCamera(CAMERA_TYPE);
    }

    private void releaseCameraAndPreview() {
        //todo 释放camera资源
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }


    Camera.Size size;

    private void startPreview(SurfaceHolder holder) {
        if (mCamera != null) {
            int width = mSurfaceView.getWidth();
            int height = mSurfaceView.getHeight();
            Camera.Parameters parameters = mCamera.getParameters();
            Camera.Size size = getOptimalPreviewSize(parameters.getSupportedPictureSizes(), width, height);

            parameters.setPictureSize(size.width, size.height);
            mCamera.setParameters(parameters);

            //todo 开始预览
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static final int DEGREE_90 = 90;
    private static final int DEGREE_180 = 180;
    private static final int DEGREE_270 = 270;
    private static final int DEGREE_360 = 360;
    private MediaRecorder mMediaRecorder;

    private int getCameraDisplayOrientation(int cameraId) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = getActivity().getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = DEGREE_90;
                break;
            case Surface.ROTATION_180:
                degrees = DEGREE_180;
                break;
            case Surface.ROTATION_270:
                degrees = DEGREE_270;
                break;
            default:
                break;
        }

        int result;
        if (info.facing == CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % DEGREE_360;
            result = (DEGREE_360 - result) % DEGREE_360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + DEGREE_360) % DEGREE_360;
        }
        return result;
    }

    private String[] mPermissionsArrays = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO};

    private void releaseMediaRecorder() {
        //todo 释放MediaRecorder
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
            if (mCamera != null) {
                mCamera.lock();
            }
        }
    }

    private Camera.PictureCallback mPicture = (data, camera) -> {
        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (pictureFile == null) {
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
//            //改变角度
//            FileInputStream fis = new FileInputStream(pictureFile);
//            Bitmap bitmap = rotateImage(BitmapFactory.decodeStream(fis), pictureFile.getAbsolutePath());
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            Bitmap newBitmap = rotateImage(bitmap, pictureFile.getAbsolutePath());
            FileOutputStream fos1 = new FileOutputStream(pictureFile);
            newBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos1);
            fos1.flush();
            fos1.close();
        } catch (IOException e) {
            Log.d("mPicture", "Error accessing file: " + e.getMessage());
        }
        Intent intent = new Intent();
        intent.setClass(getActivity(), PhotoShow.class);
        intent.putExtra("path", pictureFile.getAbsolutePath());
        startActivity(intent);

        mCamera.startPreview();
    };


    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = Math.min(w, h);

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    private boolean checkPermissionAllGranted(String[] permissions) {
        // 6.0以下不需要
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        for (String permission : permissions) {
            if (getActivity().checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                // 只要有一个权限没有被授予, 则直接返回 false
                return false;
            }
        }
        return true;
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView: ");
        super.onDestroyView();
        releaseCameraAndPreview();
        releaseMediaRecorder();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
        releaseCameraAndPreview();
        releaseMediaRecorder();
    }

    private static final int PICK_IMAGE = 1;
    private static final int PICK_VIDEO = 2;
    private int selectStatus = 0;//0待选图片,1待选视频,2待上传

    public void chooseImage() {
        releaseCameraAndPreview();
        releaseMediaRecorder();
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),
                PICK_IMAGE);
    }


    public void chooseVideo() {
        releaseCameraAndPreview();
        releaseMediaRecorder();
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"),
                PICK_VIDEO);
    }

    private MultipartBody.Part getMultipartFromUri(String name, Uri uri) {
        // if NullPointerException thrown, try to allow storage permission in system settings
        File f = new File(ResourceUtils.getRealPath(getActivity(), uri));
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), f);
        return MultipartBody.Part.createFormData(name, f.getName(), requestFile);
    }

    private void postVideo() {
        selectStatus = 4;//正在上传

        // TODO-C2 (6) Send Request to post a video with its cover image
        // if success, make a text Toast and show
        if (ContextCompat.checkSelfPermission(getActivity(), READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{READ_EXTERNAL_STORAGE}, 1);
        } else {
            new UploadTask().execute("http://test.androidcamp.bytedance.com");
        }
    }

    private class UploadTask extends AsyncTask<String, Void, PostVideoResponse> {
        /**
         * The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute()
         */
        protected PostVideoResponse doInBackground(String... urls) {
            MultipartBody.Part file1 = getMultipartFromUri("cover_image", mSelectedImage);
            MultipartBody.Part file2 = getMultipartFromUri("video", mSelectedVideo);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(urls[0])
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            Response<PostVideoResponse> response = null;
            try {
                response = retrofit.create(IMiniDouyinService.class).createVideo("16061196",
                        "nebulau", file1, file2).
                        execute();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Log.d(TAG, "doInBackground: upload status is successful? " + response.body().isSuccess());
            return response.body();
        }

        /**
         * The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground()
         */
        protected void onPostExecute(PostVideoResponse response) {
            if (response.isSuccess()) {
                Toast.makeText(getActivity(), "上传成功!", Toast.LENGTH_SHORT).show();
                selectStatus = 3;//上传成功
            } else {
                Toast.makeText(getActivity(), "上传失败", Toast.LENGTH_SHORT).show();
                selectStatus = 2;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult() called with: requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");

        if (resultCode == RESULT_OK && null != data) {

            if (requestCode == PICK_IMAGE) {
                mSelectedImage = data.getData();
                Log.d(TAG, "selectedImage = " + mSelectedImage);
                selectStatus = 1;
            } else if (requestCode == PICK_VIDEO) {
                mSelectedVideo = data.getData();
                Log.d(TAG, "mSelectedVideo = " + mSelectedVideo);
                selectStatus = 2;
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
        releaseCameraAndPreview();
        releaseMediaRecorder();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
        releaseCameraAndPreview();
        releaseMediaRecorder();
//        timer.cancel();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            prepareCamera();
            if (mHolder != null) {
                startPreview(mHolder);
            }
        } else {
            releaseCameraAndPreview();
            releaseMediaRecorder();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        prepareCamera();
        if (mHolder != null) {
            startPreview(mHolder);
        }
    }

}
