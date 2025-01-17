package edaebugo.blooddonation_pro;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.concurrent.Semaphore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Camera2Test extends AppCompatActivity {

    private TextureView mCameraTextureView;
    private Preview mPreview;

    Activity mainActivity = this;

    private static final String TAG = "Camera2Test";

    static final int REQUEST_CAMERA = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2test);

        mCameraTextureView = (TextureView) findViewById(R.id.cameraTextureView);
        mPreview = new Preview(this, mCameraTextureView);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CAMERA:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    int grantResult = grantResults[i];
                    if (permission.equals(Manifest.permission.CAMERA)) {
                        if(grantResult == PackageManager.PERMISSION_GRANTED) {
                            mCameraTextureView = (TextureView) findViewById(R.id.cameraTextureView);
                            mPreview = new Preview(mainActivity, mCameraTextureView);
                            Log.d(TAG,"mPreview set");
                        } else {
                            Toast.makeText(this,"Should have camera permission to run", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPreview.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPreview.onPause();
    }


    //Camera2 Preview
    public class Preview extends Thread {
        private final static String TAG = "Preview : ";

        private Size mPreviewSize;
        private Context mContext;
        private CameraDevice mCameraDevice;
        private CaptureRequest.Builder mPreviewBuilder;
        private CameraCaptureSession mPreviewSession;
        private TextureView mTextureView;

        public Preview(Context context, TextureView textureView) {
            mContext = context;
            mTextureView = textureView;
        }

        private String getBackFacingCameraId(CameraManager cManager) {
            try {
                for (final String cameraId : cManager.getCameraIdList()) {
                    CameraCharacteristics characteristics = cManager.getCameraCharacteristics(cameraId);
                    int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                    if (cOrientation == CameraCharacteristics.LENS_FACING_BACK) return cameraId;
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            return null;
        }

        public void openCamera() {
            CameraManager manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
            Log.e(TAG, "openCamera E");
            try {
                String cameraId = getBackFacingCameraId(manager);
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                mPreviewSize = map.getOutputSizes(SurfaceTexture.class)[0];

                int permissionCamera = ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA);
                if(permissionCamera == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.CAMERA}, Camera2Test.REQUEST_CAMERA);
                } else {
                    manager.openCamera(cameraId, mStateCallback, null);
                }
            } catch (CameraAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Log.e(TAG, "openCamera X");
        }

        private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener(){

            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface,
                                                  int width, int height) {
                // TODO Auto-generated method stub
                Log.e(TAG, "onSurfaceTextureAvailable, width="+width+",height="+height);
                openCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface,
                                                    int width, int height) {
                // TODO Auto-generated method stub
                Log.e(TAG, "onSurfaceTextureSizeChanged");
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                // TODO Auto-generated method stub
            }
        };

        private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

            @Override
            public void onOpened(CameraDevice camera) {
                // TODO Auto-generated method stub
                Log.e(TAG, "onOpened");
                mCameraDevice = camera;
                startPreview();
            }

            @Override
            public void onDisconnected(CameraDevice camera) {
                // TODO Auto-generated method stub
                Log.e(TAG, "onDisconnected");
            }

            @Override
            public void onError(CameraDevice camera, int error) {
                // TODO Auto-generated method stub
                Log.e(TAG, "onError");
            }

        };

        protected void startPreview() {
            // TODO Auto-generated method stub
            if(null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
                Log.e(TAG, "startPreview fail, return");
            }

            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            if(null == texture) {
                Log.e(TAG,"texture is null, return");
                return;
            }

            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface surface = new Surface(texture);

            try {
                mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            } catch (CameraAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            mPreviewBuilder.addTarget(surface);

            try {
                mCameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {

                    @Override
                    public void onConfigured(CameraCaptureSession session) {
                        // TODO Auto-generated method stub
                        mPreviewSession = session;
                        updatePreview();
                    }

                    @Override
                    public void onConfigureFailed(CameraCaptureSession session) {
                        // TODO Auto-generated method stub
                        Toast.makeText(mContext, "onConfigureFailed", Toast.LENGTH_LONG).show();
                    }
                }, null);
            } catch (CameraAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        protected void updatePreview() {
            // TODO Auto-generated method stub
            if(null == mCameraDevice) {
                Log.e(TAG, "updatePreview error, return");
            }

            mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            HandlerThread thread = new HandlerThread("CameraPreview");
            thread.start();
            Handler backgroundHandler = new Handler(thread.getLooper());

            try {
                mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, backgroundHandler);
            } catch (CameraAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public void setSurfaceTextureListener()
        {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }

        public void onResume() {
            Log.d(TAG, "onResume");
            setSurfaceTextureListener();
        }

        private Semaphore mCameraOpenCloseLock = new Semaphore(1);

        public void onPause() {
            // TODO Auto-generated method stub
            Log.d(TAG, "onPause");
            try {
                mCameraOpenCloseLock.acquire();
                if (null != mCameraDevice) {
                    mCameraDevice.close();
                    mCameraDevice = null;
                    Log.d(TAG, "CameraDevice Close");
                }
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted while trying to lock camera closing.");
            } finally {
                mCameraOpenCloseLock.release();
            }
        }
    }

}
