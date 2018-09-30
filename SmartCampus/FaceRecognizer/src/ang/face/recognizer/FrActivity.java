package ang.face.recognizer;

import java.io.UnsupportedEncodingException;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class FrActivity extends Activity implements CvCameraViewListener2 {

    private static final String    TAG                 = "FrActivity";
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    public static final int        JAVA_DETECTOR       = 0;
    public static final int        NATIVE_DETECTOR     = 1;
    public static final String     INTENT_PATH_DATA     = "path";

    private MenuItem               mItemFace50;
    private MenuItem               mItemFace40;
    private MenuItem               mItemFace30;
    private MenuItem               mItemFace20;
    private MenuItem               mItemType;

    private Mat                    mface;
    private Mat                    mRgba;
    private Mat                    mGray;
//    private File                   mCascadeFile;
//    private CascadeClassifier      mJavaDetector;
    private DetectionBasedTracker			   mNativeDetector;

//    private int                    mDetectorType       = JAVA_DETECTOR;
    private int                    mDetectorType       = NATIVE_DETECTOR;
    private String[]               mDetectorName;

    private float                  mRelativeFaceSize   = 0.2f;
    private int                    mAbsoluteFaceSize   = 0;

    private CameraBridgeViewBase   mOpenCvCameraView;
    private ImageView			   mFaceView;
    private TextView			   mTextView;
    private String					mLastName = " ";
    
    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("seeta_facedet");
                    System.loadLibrary("detection_based_tracker");

                    mNativeDetector = new DetectionBasedTracker(mModelPath, mImagePath, 0);

                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    
    private String mModelPath = "/sdcard/routon/model";
    private String mImagePath = "/sdcard/routon/hdpic";
    public static final String INTENT_MODEL_DIR_DATA = "modelpath";
    public static final String INTENT_IMAGE_DIR_DATA = "imagepath";

    public FrActivity() {
        mDetectorName = new String[2];
        mDetectorName[JAVA_DETECTOR] = "Java";
        mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";

        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);  
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.face_detect_surface_view);
        
        mModelPath = this.getIntent().getStringExtra(INTENT_MODEL_DIR_DATA);
        mImagePath = this.getIntent().getStringExtra(INTENT_IMAGE_DIR_DATA);
        
        Log.d(TAG,"mModelPath:"+mModelPath+",mImagePath:"+mImagePath);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
//        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mFaceView = (ImageView)this.findViewById(R.id.face_surface_view); 
        mTextView = (TextView)this.findViewById(R.id.text_view); 
        mTextView.setTextSize(50.0f);
        mTextView.setTextColor(Color.rgb(255, 255, 0));
    }

	Handler fdHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			//super.handleMessage(msg);
//			Log.i(tag, "handleMessage " + msg.what);
			switch (msg.what){
			case 0:
            	Bitmap bmp = null;
            	bmp = Bitmap.createBitmap( mface.cols(), mface.rows(), Bitmap.Config.ARGB_8888);
            	Utils.matToBitmap(mface, bmp);
            	mFaceView.setImageBitmap(bmp);
            	mFaceView.invalidate();
            	mFaceView.setVisibility(View.VISIBLE);
				break;
			case 1:
				String id = (String)(msg.obj);
				mTextView.setText("  姓 名：" + id);
				Intent intent = new Intent();
				intent.putExtra(INTENT_PATH_DATA, id+".jpg");
				Log.i(TAG, "handleMessage id" + id);
				FrActivity.this.setResult(Activity.RESULT_OK, intent);
				FrActivity.this.finish();
				break;
			default:
				break;
			}
		}

	};
    
    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mNativeDetector != null) {
			synchronized (mNativeDetector) {
				mNativeDetector.release();
				mNativeDetector = null;
			}
		}
        mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mface = new Mat();
        mGray = new Mat();
        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
        mface.release();
        mGray.release();
        mRgba.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
            mNativeDetector.setMinFaceSize(mAbsoluteFaceSize);
        }

        MatOfByte id = new MatOfByte();
        MatOfRect faces = new MatOfRect();

        if (mNativeDetector != null)
        {
            mNativeDetector.detect(mRgba, mGray, faces, id);
            if(id.cols() > 0 && id.rows() > 0)
            {
				try {
	            	byte[] idarr = id.toArray();
//					String idstr = new String (idarr, "GBK");
					String idstr = new String (idarr, "UTF-8");
					Log.i(TAG, "Recognizer id " + idstr);
					if( idstr != null && idstr.trim().length() > 0 ){
						Message msg = new Message();
						msg.obj = idstr;
						msg.what = 1;
						fdHandler.sendMessageDelayed(msg, 0);
					}
//					if(!mLastName.equals(idstr))
//					{
//						mLastName = idstr;
//						fdHandler.sendEmptyMessage(1);
//					}
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
//            	fdHandler.sendEmptyMessage(0);
            }
            else
            {
				if(!mLastName.equals(" "))
				{
					mLastName = " ";
					fdHandler.sendEmptyMessage(1);
				}
            }
        }
        else {
            Log.e(TAG, "Detection method is not selected!");
        }

        Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++)
            Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);

        return mRgba;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemFace50 = menu.add("Face size 50%");
        mItemFace40 = menu.add("Face size 40%");
        mItemFace30 = menu.add("Face size 30%");
        mItemFace20 = menu.add("Face size 20%");
        mItemType   = menu.add(mDetectorName[mDetectorType]);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item == mItemFace50)
            setMinFaceSize(0.5f);
        else if (item == mItemFace40)
            setMinFaceSize(0.4f);
        else if (item == mItemFace30)
            setMinFaceSize(0.3f);
        else if (item == mItemFace20)
            setMinFaceSize(0.2f);
        else if (item == mItemType) {
            int tmpDetectorType = (mDetectorType + 1) % mDetectorName.length;
            item.setTitle(mDetectorName[tmpDetectorType]);
            setDetectorType(tmpDetectorType);
        }
        return true;
    }

    private void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
    }

    private void setDetectorType(int type) {
        if (mDetectorType != type) {
            mDetectorType = type;

            if (type == NATIVE_DETECTOR) {
                Log.i(TAG, "Detection Based Tracker enabled");
                mNativeDetector.start();
            } else {
                Log.i(TAG, "Cascade detector enabled");
                mNativeDetector.stop();
            }
        }
    }
}
