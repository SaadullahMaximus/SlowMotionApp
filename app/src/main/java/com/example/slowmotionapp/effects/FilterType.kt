package com.example.slowmotionapp.effects

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import com.daasuu.epf.filter.*
import com.example.slowmotionapp.R
import java.io.IOException

enum class FilterType {
    No, Bilateral, Blur, Brightness, Bulge, CGA, Contrast, Crosshatch, Exposure, Sharp, Gamma, Gaussian, Gray, Haze, Halftone, Shadow, Hue, Invert, Luminance, Threshold, Monochrome, Opacity, Pixelation, Posterize, RGB, Saturation, Sepia, SHARP, SOLARIZE, Sphere, SWIRL, HOE, TONE, VIBRANCE, VIGNETTE, LoopUp, Weak, ZBlur;

    companion object {
        fun createFilterList(): List<FilterType> {
            return listOf(*values())
        }

        fun createGlFilter(filterType: FilterType?, context: Context): GlFilter {
            return when (filterType) {
                No -> GlFilter()
                Bilateral -> GlBilateralFilter()
                Blur -> GlBoxBlurFilter()
                Brightness -> {
                    val glBrightnessFilter = GlBrightnessFilter()
                    glBrightnessFilter.setBrightness(0.2f)
                    glBrightnessFilter
                }
                Bulge -> GlBulgeDistortionFilter()
                CGA -> GlCGAColorspaceFilter()
                Contrast -> {
                    val glContrastFilter = GlContrastFilter()
                    glContrastFilter.setContrast(2.5f)
                    glContrastFilter
                }
                Crosshatch -> GlCrosshatchFilter()
                Exposure -> GlExposureFilter()
                Sharp -> GlFilterGroup(GlSepiaFilter(), GlVignetteFilter())
                Gamma -> {
                    val glGammaFilter = GlGammaFilter()
                    glGammaFilter.setGamma(2f)
                    glGammaFilter
                }
                Gaussian -> GlGaussianBlurFilter()
                Gray -> GlGrayScaleFilter()
                Halftone -> GlHalftoneFilter()
                Haze -> {
                    val glHazeFilter = GlHazeFilter()
                    glHazeFilter.slope = -0.5f
                    glHazeFilter
                }
                Shadow -> GlHighlightShadowFilter()
                Hue -> GlHueFilter()
                Invert -> GlInvertFilter()
                LoopUp -> {
                    val bitmap =
                        BitmapFactory.decodeResource(context.resources, R.drawable.lookup_sample)
                    GlLookUpTableFilter(bitmap)
                }
                Luminance -> GlLuminanceFilter()
                Threshold -> GlLuminanceThresholdFilter()
                Monochrome -> GlMonochromeFilter()
                Opacity -> GlOpacityFilter()
                Pixelation -> GlPixelationFilter()
                Posterize -> GlPosterizeFilter()
                RGB -> {
                    val glRGBFilter = GlRGBFilter()
                    glRGBFilter.setRed(0f)
                    glRGBFilter
                }
                Saturation -> GlSaturationFilter()
                Sepia -> GlSepiaFilter()
                SHARP -> {
                    val glSharpenFilter = GlSharpenFilter()
                    glSharpenFilter.sharpness = 4f
                    glSharpenFilter
                }
                SOLARIZE -> GlSolarizeFilter()
                Sphere -> GlSphereRefractionFilter()
                SWIRL -> GlSwirlFilter()
                HOE -> {
                    try {
                        val `is` = context.assets.open("acv/tone_cuver_sample.acv")
                        return GlToneCurveFilter(`is`)
                    } catch (e: IOException) {
                        Log.e("FilterType", "Error")
                    }
                    GlFilter()
                }
                TONE -> GlToneFilter()
                VIBRANCE -> {
                    val glVibranceFilter = GlVibranceFilter()
                    glVibranceFilter.setVibrance(3f)
                    glVibranceFilter
                }
                VIGNETTE -> GlVignetteFilter()
                Weak -> GlWeakPixelInclusionFilter()
                ZBlur -> GlZoomBlurFilter()
                else -> GlFilter()
            }
        }
    }
}