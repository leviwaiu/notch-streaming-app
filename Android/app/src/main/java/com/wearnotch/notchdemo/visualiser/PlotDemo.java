package com.wearnotch.notchdemo.visualiser;

import android.graphics.Color;

import com.wearnotch.framework.Bone;
import com.wearnotch.framework.Pair;
import com.wearnotch.framework.Skeleton;
import com.wearnotch.framework.visualiser.VisualiserData;
import com.wearnotch.notchmaths.fvec3;
import com.wearnotch.visualiser.BonePath;
import com.wearnotch.visualiser.GroundMesh;
import com.wearnotch.visualiser.shader.ColorShader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class PlotDemo {
    private static boolean USE_HANDWRITING = false;

    private VisualiserData mData;
    private Bone mRightHand;
    private Bone mRightForeArm;

    private ColorShader mColorShader;

    private List<Bone> mBonesToShow, mAllBones;

    private Map<Bone, BonePath> mPathMap = new HashMap<>();
    private Map<Bone, SimplePath> mSimplePathMap = new HashMap<>();
    private Map<Bone, List<fvec3>> mSimpleDataMap = new HashMap<>();

    private GroundMesh mGroundMesh;
    private boolean isGroundDrawn;
    private boolean isRealTime;
    private int mDefaultColor1, mDefaultColor2;

    PlotDemo(VisualiserData data, boolean groundDrawn) {
        mData = data;
        mAllBones = data.getSkeleton().getBoneOrder();

        mRightHand = data.getSkeleton().getBone("RightHand");
        mRightForeArm = data.getSkeleton().getBone("RightForeArm");

        isGroundDrawn = groundDrawn;
        isRealTime = false;
        mDefaultColor1 = 0;
        mDefaultColor2 = 0;
    }

    PlotDemo(Map<Bone, List<fvec3>> simpleData, Skeleton skeleton, boolean groundDrawn, int defColor1, int defColor2) {
        mSimpleDataMap = simpleData;
        mAllBones = skeleton.getBoneOrder();

        mRightHand = skeleton.getBone("RightHand");
        mRightForeArm = skeleton.getBone("RightForeArm");

        isGroundDrawn = groundDrawn;
        isRealTime = true;
        mDefaultColor1 = defColor1;
        mDefaultColor2 = defColor2;
    }

    private static final int[] COLOR_PALETTE;

    static {
        int alpha = 255;
        COLOR_PALETTE = new int[]{
                Color.argb(alpha, 255, 146, 3),
                Color.argb(alpha, 255, 174, 6),
                Color.argb(alpha, 255, 201, 9),
                Color.argb(alpha, 255, 229, 12),

                Color.argb(alpha, 84, 255, 129),
                Color.argb(alpha, 0, 234, 198),
                Color.argb(alpha, 0, 166, 235),
                Color.argb(alpha, 20, 88, 255),

                Color.argb(alpha, 255, 85, 194),
                Color.argb(alpha, 244, 69, 154),
                Color.argb(alpha, 232, 52, 115),
                Color.argb(alpha, 221, 36, 75)
        };
    }

    private int[] createPlotColorArray(int[] positions) {
        int[] colors = new int[positions.length];
        for (int idx = 0; idx < colors.length; ++idx) {
            colors[idx] = COLOR_PALETTE[idx % COLOR_PALETTE.length];
        }
        return colors;
    }

    private int[] createPlotPositionArray() {
        int linear = 0;
        if (isRealTime) {
            linear = (int) Math.floor(1f / 12 * mSimpleDataMap.get(mSimpleDataMap.keySet().toArray()[0]).size());
        } else {
            linear = (int) Math.floor(1f / 12 * mData.getFrameCount());
        }

        return new int[]{
                0, linear, 2 * linear, 3 * linear,
                4 * linear, 5 * linear, 6 * linear, 7 * linear,
                8 * linear, 9 * linear, 10 * linear, 11 * linear
        };
    }

    private Pair<int[], int[]> createHandWritingArrays(List<int[]> drawIntervals) {
        int[] positions = new int[drawIntervals.size() * 2];
        int[] colors = new int[drawIntervals.size() * 2];

        for (int idx = 0; idx < drawIntervals.size(); ++idx) {
            positions[idx * 2] = drawIntervals.get(idx)[0];
            positions[idx * 2 + 1] = drawIntervals.get(idx)[1];

            colors[idx * 2] = COLOR_PALETTE[idx % COLOR_PALETTE.length];
            colors[idx * 2 + 1] = Color.TRANSPARENT;
        }

        return Pair.create(positions, colors);
    }

    private List<int[]> findRightHandDrawIntervals() {
        List<int[]> drawIntervals = new ArrayList<int[]>();

        int[] intv = null;
        fvec3 relativeAngle = new fvec3();
        for (int idx = 0; idx < mData.getFrameCount(); ++idx) {

            mData.calculateRelativeAngle(mRightForeArm, idx, relativeAngle);

            boolean shouldDraw = Math.abs(relativeAngle.get(2)) < 45.0f;
            if (shouldDraw && intv == null) {
                intv = new int[]{idx, idx};
            } else if (!shouldDraw && intv != null) {
                intv[1] = idx;
                drawIntervals.add(intv);
                intv = null;
            }
        }
        if (intv != null) {
            intv[1] = mData.getFrameCount();
            drawIntervals.add(intv);

        }

        return drawIntervals;
    }

    void init() {

        //mGroundMesh = new GroundMesh(0.5f,Color.argb(255, 22, 22, 22)); // dark gray
        if (!isGroundDrawn)
            mGroundMesh = new GroundMesh(1f, Color.argb(80, 255, 255, 255)); // white

        // Handle all bone paths together
        if (mBonesToShow == null) mBonesToShow = Collections.emptyList();

        for (Bone bone : mAllBones) {
            if (isRealTime) {
                // Do nothing
            } else if (mData.getPos(bone, 0) != null) {
                BonePath path;
                if (bone.getName().equals("RightHand") && USE_HANDWRITING) {
                    Pair<int[], int[]> p = createHandWritingArrays(findRightHandDrawIntervals());
                    path = new BonePath(mData, mRightHand, 1f, p.first, p.second);
                } else {
                    int[] positions = createPlotPositionArray();
                    path = new BonePath(mData, bone, 1f, positions, createPlotColorArray(positions));
                }
                mPathMap.put(bone, path);
            }
        }

    }

    void prepare(ColorShader shader) {

        mColorShader = shader;

        if (!isGroundDrawn) mGroundMesh.prepare(shader);

        for (BonePath path : mPathMap.values()) {
            path.prepare(shader);
        }

        for (SimplePath path : mSimplePathMap.values()) {
            path.prepare(shader);
        }

    }

    void draw(int endPosition) {
        synchronized (mBonesToShow) {
            for (Bone bone : mBonesToShow) {
                if (isRealTime) {
                    SimplePath path = mSimplePathMap.get(bone);
                    if (path != null) {
                        if (path.getShader() == null && mColorShader != null)
                            path.prepare(mColorShader);
                        path.setEndPosition(mSimpleDataMap.get(mSimpleDataMap.keySet().toArray()[0]).size());
                        path.draw();
                    }
                } else {
                    BonePath path = mPathMap.get(bone);
                    if (path != null) {
                        path.setEndPosition(endPosition);
                        path.draw();
                    }
                }
            }
        }

        if (!isGroundDrawn) mGroundMesh.draw();

    }

    void setShowPath(List<Bone> bones) {
        if (bones != null) {
            mBonesToShow = bones;
        } else {
            mBonesToShow = Collections.emptyList();
        }
    }

    void setSimpleDataMap(Map<Bone, List<fvec3>> map) {

        mSimpleDataMap = map;

        for (Bone bone : mAllBones) {
            final List<fvec3> positions = map.get(bone);
            if (positions == null) {
                continue;
            }

            if (!positions.isEmpty() && positions.get(0) != null) {
                SimplePath path = mSimplePathMap.get(bone);
                fvec3[] positionsArray = new fvec3[positions.size()];
                positionsArray = positions.toArray(positionsArray);
                if (path == null) {
                    path = new SimplePath(positionsArray, 1f, bone.getName().contains("Right") ? mDefaultColor1 : mDefaultColor2);
                } else {
                    //path = new SimplePath(map.get(bone), 1f, bone.getName().contains("Right") ? mDefaultColor1 : mDefaultColor2);
                    path.refreshPositions(positionsArray);
                }
                mSimplePathMap.put(bone, path);
            }
        }
    }
}
