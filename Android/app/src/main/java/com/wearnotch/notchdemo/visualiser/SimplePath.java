package com.wearnotch.notchdemo.visualiser;

import android.graphics.Color;
import android.opengl.GLES20;

import com.wearnotch.notchmaths.fvec3;
import com.wearnotch.visualiser.renderable.BaseRenderable;
import com.wearnotch.visualiser.shader.BaseShader;
import com.wearnotch.visualiserutil.RenderableData;

import java.io.IOException;
import java.nio.FloatBuffer;

public class SimplePath extends BaseRenderable {
    private BaseShader mShader;
    private FloatBuffer mVBO;
    private int[] mVBOLoc;
    private int mCount;
    private int mBeginPosition;
    private int mEndPosition;
    private float mScale;
    private int[] mFrameIndices;
    private int[] mColors;

    SimplePath(fvec3[] positions, float scale, int color) {
        this(positions, scale, new int[]{0}, new int[]{color});
    }

    SimplePath(fvec3[] positions, float scale, int[] frameIndices, int[] colors) {
        if (frameIndices.length != colors.length) {
            throw new IllegalArgumentException("FrameIndices and colors must have the same length!");
        } else if (frameIndices.length == 0) {
            throw new IllegalArgumentException("You must provide at least one color!");
        } else {
            checkAscending(frameIndices);

            mScale = scale;
            mFrameIndices = frameIndices;
            mColors = colors;

            this.mCount = positions.length;

            this.mVBO = RenderableData.allocateBuffer(generateVertices(positions, scale, frameIndices, colors));
        }
    }

    private static float[] generateVertices(fvec3[] positions, float scale, int[] frameIndices, int[] colors) {
        float[] vertices = new float[7 * positions.length];
        int colorIdx = 0;

        for (int frameIndex = 0; frameIndex < positions.length; ++frameIndex) {
            fvec3 pos = positions[frameIndex];
            int baseIndex = 7 * frameIndex;
            vertices[baseIndex] = pos.get(0) * scale;
            vertices[baseIndex + 1] = pos.get(1) * scale;
            vertices[baseIndex + 2] = pos.get(2) * scale;
            if (colorIdx + 1 < colors.length && frameIndices[colorIdx + 1] <= frameIndex) {
                ++colorIdx;
            }

            vertices[baseIndex + 3] = floatColorNorm((float) Color.red(colors[colorIdx]) / 255.0F);
            vertices[baseIndex + 4] = floatColorNorm((float) Color.green(colors[colorIdx]) / 255.0F);
            vertices[baseIndex + 5] = floatColorNorm((float) Color.blue(colors[colorIdx]) / 255.0F);
            vertices[baseIndex + 6] = floatColorNorm((float) Color.alpha(colors[colorIdx]) / 255.0F);
        }

        return vertices;
    }

    public void refreshPositions(fvec3[] positions) {
        this.mCount = positions.length;
        this.mVBO.clear();
        this.mVBO.put(generateVertices(positions, mScale, mFrameIndices, mColors));
        this.mVBO.flip();
        if (mVBOLoc != null && mVBOLoc.length > 0) {
            GLES20.glBindBuffer(34962, this.mVBOLoc[0]);
            GLES20.glBufferData(34962, this.mVBO.remaining() * 4, this.mVBO, 35044);
        }
        //this.mVBOLoc = bindArrayBuffer(this.mVBO);

    }

    private static void checkAscending(int[] arr) {
        for (int idx = 1; idx < arr.length; ++idx) {
            if (arr[idx - 1] > arr[idx]) {
                throw new IllegalArgumentException("Array not ascending!");
            }
        }

    }

    private static float floatColorNorm(float component) {
        return Math.min(1.0F, Math.max(0.0F, component));
    }

    public void prepare(BaseShader shader) {
        this.mShader = shader;
        this.mVBOLoc = bindArrayBuffer(this.mVBO);
    }

    public void draw() {
        this.mShader.prepare();
        GLES20.glEnableVertexAttribArray(0);
        GLES20.glEnableVertexAttribArray(1);
        this.mShader.updateUniforms();
        GLES20.glBindBuffer('袒', this.mVBOLoc[0]);
        GLES20.glVertexAttribPointer(0, 3, 5126, false, 28, 0);
        GLES20.glVertexAttribPointer(1, 4, 5126, false, 28, 12);
        if (this.mEndPosition >= 0 && this.mBeginPosition <= this.mEndPosition) {
            GLES20.glLineWidth(5.0F);
            GLES20.glDrawArrays(3, Math.max(0, this.mBeginPosition), this.mEndPosition - this.mBeginPosition + 1);
        }

        GLES20.glDisableVertexAttribArray(0);
        GLES20.glDisableVertexAttribArray(1);
        this.mShader.finish();
    }

    public void setBeginPosition(int beginPosition) {
        this.mBeginPosition = Math.min(beginPosition, this.mCount - 1);
    }

    void setEndPosition(int endPosition) {
        this.mEndPosition = Math.min(endPosition, this.mCount - 1);
    }

    @Override
    public void close() throws IOException {

    }

    BaseShader getShader() {
        return mShader;
    }
}