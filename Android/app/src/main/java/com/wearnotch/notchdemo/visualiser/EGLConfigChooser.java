package com.wearnotch.notchdemo.visualiser;

import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGL11;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

public class EGLConfigChooser implements GLSurfaceView.EGLConfigChooser {

    private static final int[] PRIMARY_ATTRS = {
            EGL10.EGL_RED_SIZE, 8,
            EGL10.EGL_GREEN_SIZE, 8,
            EGL10.EGL_BLUE_SIZE, 8,
            EGL10.EGL_DEPTH_SIZE, 16,
            EGL11.EGL_SAMPLE_BUFFERS, 1,
            EGL11.EGL_SAMPLES, 4,
            EGL11.EGL_NONE
    };

    private static final int[] SECONDARY_ATTRS = {
            EGL10.EGL_RED_SIZE, 8,
            EGL10.EGL_GREEN_SIZE, 8,
            EGL10.EGL_BLUE_SIZE, 8,
            EGL10.EGL_DEPTH_SIZE, 16,
            EGL11.EGL_NONE
    };

    @Override
    public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
        int[] confAttribs = PRIMARY_ATTRS;
        int[] numConfig = new int[1];

        egl.eglChooseConfig(display, confAttribs, null, 0, numConfig);
        if (numConfig[0] <= 0) {
            confAttribs = SECONDARY_ATTRS;
            egl.eglChooseConfig(display, confAttribs, null, 0, numConfig);
            if (numConfig[0] <= 0)
                throw new IllegalArgumentException("No configs match configSpec");
        }

        EGLConfig[] configs = new EGLConfig[numConfig[0]];
        egl.eglChooseConfig(display, confAttribs, configs, numConfig[0], numConfig);

        return configs[0];
    }
}
