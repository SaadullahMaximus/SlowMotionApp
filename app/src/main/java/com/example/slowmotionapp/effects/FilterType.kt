package com.example.slowmotionapp.effects

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import com.daasuu.epf.filter.*
import com.example.slowmotionapp.R
import java.io.IOException

/**
 * Created by sudamasayuki on 2017/05/18.
 */
enum class FilterType {
    DEFAULT, BITMAP_OVERLAY_SAMPLE, BILATERAL_BLUR, BOX_BLUR, BRIGHTNESS, BULGE_DISTORTION, CGA_COLORSPACE, CONTRAST, CROSSHATCH, EXPOSURE, FILTER_GROUP_SAMPLE, GAMMA, GAUSSIAN_FILTER, GRAY_SCALE, HAZE, HALFTONE, HIGHLIGHT_SHADOW, HUE, INVERT, LUMINANCE, LUMINANCE_THRESHOLD, MONOCHROME, OPACITY, OVERLAY, PIXELATION, POSTERIZE, RGB, SATURATION, SEPIA, SHARP, SOLARIZE, SPHERE_REFRACTION, SWIRL, TONE_CURVE_SAMPLE, TONE, VIBRANCE, VIGNETTE, LOOK_UP_TABLE_SAMPLE, WATERMARK, WEAK_PIXEL, WHITE_BALANCE, ZOOM_BLUR;

    companion object {
        fun createFilterList(): List<FilterType> {
            return listOf(*values())
        }

        //    public static GlFilter createGlFilter(FilterType filterType, Context context) {
        //        switch (filterType) {
        //            case DEFAULT:
        //                return new GlFilter();
        //            case SEPIA:
        //                return new GlSepiaFilter();
        //            case GRAY_SCALE:
        //                return new GlGrayScaleFilter();
        //            case INVERT:
        //                return new GlInvertFilter();
        //            case HAZE:
        //                return new GlHazeFilter();
        //            case MONOCHROME:
        //                return new GlMonochromeFilter();
        //            case BILATERAL_BLUR:
        //                return new GlBilateralFilter();
        //            case BOX_BLUR:
        //                return new GlBoxBlurFilter();
        //            case LOOK_UP_TABLE_SAMPLE:
        //                Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.lookup_sample);
        //
        //                return new GlLookUpTableFilter(bitmap);
        //            case TONE_CURVE_SAMPLE:
        //                try {
        //                    InputStream is = context.getAssets().open("acv/tone_cuver_sample.acv");
        //                    return new GlToneCurveFilter(is);
        //                } catch (IOException e) {
        //                    Log.e("FilterType", "Error");
        //                }
        //                return new GlFilter();
        //
        //            case SPHERE_REFRACTION:
        //                return new GlSphereRefractionFilter();
        //            case VIGNETTE:
        //                return new GlVignetteFilter();
        //            case FILTER_GROUP_SAMPLE:
        //                return new GlFilterGroup(new GlSepiaFilter(), new GlVignetteFilter());
        //            case GAUSSIAN_FILTER:
        //                return new GlGaussianBlurFilter();
        //            case BULGE_DISTORTION:
        //                return new GlBulgeDistortionFilter();
        //            case CGA_COLORSPACE:
        //                return new GlCGAColorspaceFilter();
        //            case SHARP:
        //                GlSharpenFilter glSharpenFilter = new GlSharpenFilter();
        //                glSharpenFilter.setSharpness(4f);
        //                return glSharpenFilter;
        //            case BITMAP_OVERLAY_SAMPLE:
        //                return new GlBitmapOverlaySample(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_round));
        //            default:
        //                return new GlFilter();
        //        }
        //    }
        fun createGlFilter(filterType: FilterType?, context: Context): GlFilter {
            return when (filterType) {
                DEFAULT -> GlFilter()
                BILATERAL_BLUR -> GlBilateralFilter()
                BOX_BLUR -> GlBoxBlurFilter()
                BRIGHTNESS -> {
                    val glBrightnessFilter = GlBrightnessFilter()
                    glBrightnessFilter.setBrightness(0.2f)
                    glBrightnessFilter
                }
                BULGE_DISTORTION -> GlBulgeDistortionFilter()
                CGA_COLORSPACE -> GlCGAColorspaceFilter()
                CONTRAST -> {
                    val glContrastFilter = GlContrastFilter()
                    glContrastFilter.setContrast(2.5f)
                    glContrastFilter
                }
                CROSSHATCH -> GlCrosshatchFilter()
                EXPOSURE -> GlExposureFilter()
                FILTER_GROUP_SAMPLE -> GlFilterGroup(GlSepiaFilter(), GlVignetteFilter())
                GAMMA -> {
                    val glGammaFilter = GlGammaFilter()
                    glGammaFilter.setGamma(2f)
                    glGammaFilter
                }
                GAUSSIAN_FILTER -> GlGaussianBlurFilter()
                GRAY_SCALE -> GlGrayScaleFilter()
                HALFTONE -> GlHalftoneFilter()
                HAZE -> {
                    val glHazeFilter = GlHazeFilter()
                    glHazeFilter.slope = -0.5f
                    glHazeFilter
                }
                HIGHLIGHT_SHADOW -> GlHighlightShadowFilter()
                HUE -> GlHueFilter()
                INVERT -> GlInvertFilter()
                LOOK_UP_TABLE_SAMPLE -> {
                    val bitmap =
                        BitmapFactory.decodeResource(context.resources, R.drawable.lookup_sample)
                    GlLookUpTableFilter(bitmap)
                }
                LUMINANCE -> GlLuminanceFilter()
                LUMINANCE_THRESHOLD -> GlLuminanceThresholdFilter()
                MONOCHROME -> GlMonochromeFilter()
                OPACITY -> GlOpacityFilter()
                PIXELATION -> GlPixelationFilter()
                POSTERIZE -> GlPosterizeFilter()
                RGB -> {
                    val glRGBFilter = GlRGBFilter()
                    glRGBFilter.setRed(0f)
                    glRGBFilter
                }
                SATURATION -> GlSaturationFilter()
                SEPIA -> GlSepiaFilter()
                SHARP -> {
                    val glSharpenFilter = GlSharpenFilter()
                    glSharpenFilter.sharpness = 4f
                    glSharpenFilter
                }
                SOLARIZE -> GlSolarizeFilter()
                SPHERE_REFRACTION -> GlSphereRefractionFilter()
                SWIRL -> GlSwirlFilter()
                TONE_CURVE_SAMPLE -> {
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
                WATERMARK -> GlWatermarkFilter(
                    BitmapFactory.decodeResource(
                        context.resources,
                        R.mipmap.ic_launcher_round
                    ), GlWatermarkFilter.Position.RIGHT_BOTTOM
                )
                WEAK_PIXEL -> GlWeakPixelInclusionFilter()
                WHITE_BALANCE -> {
                    val glWhiteBalanceFilter = GlWhiteBalanceFilter()
                    glWhiteBalanceFilter.setTemperature(2400f)
                    glWhiteBalanceFilter.setTint(2f)
                    glWhiteBalanceFilter
                }
                ZOOM_BLUR -> GlZoomBlurFilter()
//                BITMAP_OVERLAY_SAMPLE -> GlBitmapOverlaySample(
//                    BitmapFactory.decodeResource(
//                        context.resources,
//                        R.mipmap.ic_launcher_round
//                    )
//                )
                else -> GlFilter()
            }
        }
    }
}