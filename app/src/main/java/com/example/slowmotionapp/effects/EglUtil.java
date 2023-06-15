package com.example.slowmotionapp.effects;

import static android.opengl.GLES20.GL_CLAMP_TO_EDGE;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_T;

import android.opengl.GLES20;

public class EglUtil {

    public static void setupSampler(final int target, final int mag, final int min) {
        GLES20.glTexParameterf(target, GL_TEXTURE_MAG_FILTER, mag);
        GLES20.glTexParameterf(target, GL_TEXTURE_MIN_FILTER, min);
        GLES20.glTexParameteri(target, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(target, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    }
}