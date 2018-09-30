package ang.face.recognizer;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;

public class DetectionBasedTracker
{
	private String mDirPicture = null;
    public DetectionBasedTracker(String dirModel, String dirPicture, int minFaceSize) {
        mNativeObj = nativeCreateObject(dirModel, dirPicture, minFaceSize);
        mDirPicture = dirPicture;
    }
    
    public String getDirPicture(){
    	return mDirPicture;
    }

    public void extractFeature(String file) {
        nativeExtractFeature(mNativeObj, file);
    }

    public void start() {
        nativeStart(mNativeObj);
    }

    public void stop() {
        nativeStop(mNativeObj);
    }

    public void setMinFaceSize(int size) {
        nativeSetFaceSize(mNativeObj, size);
    }

    public void detect(Mat image, Mat imageGray, MatOfRect faces, Mat id) {
        nativeDetect(mNativeObj, image.getNativeObjAddr(), imageGray.getNativeObjAddr(), faces.getNativeObjAddr(), id.getNativeObjAddr());
    }

    public void release() {
        nativeDestroyObject(mNativeObj);
        mNativeObj = 0;
    }

    private long mNativeObj = 0;

    private static native long nativeCreateObject(String dirModel, String dirPicture, int minFaceSize);
    private static native void nativeDestroyObject(long thiz);
    private static native void nativeExtractFeature(long thiz, String file);
    private static native void nativeStart(long thiz);
    private static native void nativeStop(long thiz);
    private static native void nativeSetFaceSize(long thiz, int size);
    private static native void nativeDetect(long thiz, long image, long imageGray, long faces, long id);
}
