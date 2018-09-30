package com.routon.smartcampus.graffiti;

import android.graphics.Matrix;
import android.graphics.Path;

import static com.routon.smartcampus.graffiti.DrawUtil.rotatePoint;
import static com.routon.smartcampus.graffiti.DrawUtil.rotatePointInGraffiti;

/**
 * Created by huangziwei on 2017/3/16.
 */

public class GraffitiPath extends Undoable {
    GraffitiView.Pen mPen; // 画笔类型
    GraffitiView.Shape mShape; // 画笔形状
    float mStrokeWidth; // 大小
    GraffitiColor mColor; // 颜色
    Path mPath; // 画笔的路径
    float mSx, mSy; // 映射后的起始坐标，（手指点击）
    float mDx, mDy; // 映射后的终止坐标，（手指抬起）
    private Matrix mMatrix = new Matrix(); //　图片的偏移矩阵
    int mRotateDegree = 0; // 旋转的角度（围绕图片中心旋转）
    float mPivotX, mPivotY;

    public Path getPath(int currentDegree) {
        int degree = currentDegree - mRotateDegree;
        if (degree == 0) {
            return mPath;
        }
        Path path = new Path(mPath);
        Matrix matrix = new Matrix();

        float px = mPivotX, py = mPivotY;
        if (mRotateDegree == 90 || mRotateDegree == 270) { //　交换中心点的xy坐标
            float t = px;
            px = py;
            py = t;
        }

        matrix.setRotate(degree, px, py);
        if (Math.abs(degree) == 90 || Math.abs(degree) == 270) {
            matrix.postTranslate((py - px), -(py - px));
        }
        path.transform(matrix);
        return path;
    }

    public float[] getDxDy(int currentDegree) {

        return rotatePointInGraffiti(currentDegree, mRotateDegree, mDx, mDy, mPivotX, mPivotY);
    }

    public float[] getSxSy(int currentDegree) {

        return rotatePointInGraffiti(currentDegree, mRotateDegree, mSx, mSy, mPivotX, mPivotY);
    }

    public Matrix getMatrix(int currentDegree) {
        if (mMatrix == null) {
            return null;
        }
            return mMatrix;

    }

    static GraffitiPath toShape(GraffitiView.Pen pen, GraffitiView.Shape shape, float width, GraffitiColor color,
                                float sx, float sy, float dx, float dy, int degree, float px, float py) {
        GraffitiPath path = new GraffitiPath();
        path.mPen = pen;
        path.mShape = shape;
        path.mStrokeWidth = width;
        path.mColor = color;
        path.mSx = sx;
        path.mSy = sy;
        path.mDx = dx;
        path.mDy = dy;
        path.mRotateDegree = degree;
        path.mPivotX = px;
        path.mPivotY = py;
        return path;
    }

    static GraffitiPath toPath(GraffitiView.Pen pen, GraffitiView.Shape shape, float width, GraffitiColor color, Path p, int degree, float px, float py) {
        GraffitiPath path = new GraffitiPath();
        path.mPen = pen;
        path.mShape = shape;
        path.mStrokeWidth = width;
        path.mColor = color;
        path.mPath = p;
        path.mRotateDegree = degree;
        path.mPivotX = px;
        path.mPivotY = py;
        return path;
    }
}

