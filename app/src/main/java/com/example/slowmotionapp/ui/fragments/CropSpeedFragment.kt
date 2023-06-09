package com.example.slowmotionapp.ui.fragments

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.edmodo.cropper.CropImageView
import com.edmodo.cropper.cropwindow.edge.Edge
import com.example.slowmotionapp.R
import com.example.slowmotionapp.customviews.CustomWaitingDialog
import com.example.slowmotionapp.databinding.FragmentCropSpeedBinding
import com.example.slowmotionapp.effects.EPlayerView
import com.example.slowmotionapp.interfaces.MyListener
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.backSave
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.mainCachedFile
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.musicReady
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.myMusicUri
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.wannaGoBack
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.wannaGoBackCheckViewModel
import com.example.slowmotionapp.utils.Utils.commandsGenerator
import com.example.slowmotionapp.utils.Utils.createCacheTempFile
import com.example.slowmotionapp.utils.Utils.deleteFromGallery
import com.example.slowmotionapp.utils.Utils.getScreenWidth
import com.example.slowmotionapp.utils.Utils.getVideoDuration
import com.example.slowmotionapp.utils.Utils.getVideoSize
import com.example.slowmotionapp.utils.Utils.milliSecondsToTimer
import com.example.slowmotionapp.utils.Utils.player
import com.example.slowmotionapp.utils.Utils.saveEditedVideo
import com.example.slowmotionapp.utils.Utils.setListener
import com.example.slowmotionapp.utils.Utils.setUpSimpleExoPlayer
import com.example.slowmotionapp.utils.Utils.singleClick
import com.example.slowmotionapp.viewmodel.SharedViewModel
import com.google.android.exoplayer2.Player
import java.io.File

class CropSpeedFragment : Fragment(), MyListener {

    private var _binding: FragmentCropSpeedBinding? = null
    private val binding get() = _binding!!

    private var childFragmentManager: FragmentManager? = null
    private var currentChildFragment: Fragment? = null
    private var effectMusicFragment: Fragment? = null

    private var mStartPosition = 0
    private var mDuration = 0
    private var mEndPosition = 0
    private var mTimeVideo = 0
    private val mMaxDuration = 120
    private var mDurationWithoutEdit = 0

    private val mHandler = Handler(Looper.getMainLooper())

    private var mediaPlayer: MediaPlayer? = null

    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var videoView: VideoView
    private lateinit var cropView: CropImageView
    private lateinit var layoutMovieWrapper: FrameLayout

    private lateinit var seekBar1: SeekBar
    private lateinit var seekBar2: SeekBar

    private lateinit var playPauseButton1: ImageView
    private lateinit var playPauseButton2: ImageView

    private var screenWidth = 0

    private var keyCodeR = 0
    private var keyCodeW = 0
    private var keyCodeQ = 0

    private var aa = 0f
    private var ab = 0f
    private var ac = 0f
    private var ad = 0f

    private var a = 0
    private var b = 0
    private var y = 0
    private var z = 0

    private var m = 0
    private var n = 0
    private var o = 0
    private var p = 0
    private var q = 0
    private var r = 0
    private var s = 0
    private var t = 0
    private var u = 0
    private var v = 0

    private var cropVideoDuration: String? = null

    private var ag = 0
    private var ah = 0

    private var cropOutputFilePath: String? = null

    private var handler: Handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable = Runnable { updateSeekBar2() }
    private var runnable2: Runnable = Runnable { updateSeekBar() }

    private var audioPlayer: MediaPlayer? = null

    private var enhanced = false

    private var videoDuration = 0

    companion object {
        var ePlayerView: EPlayerView? = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        val layoutParams = binding.frameLayout.layoutParams
        layoutParams.width = screenWidth
        layoutParams.height = screenWidth
        binding.frameLayout.layoutParams = layoutParams

        videoView = view.findViewById(R.id.videoView)
        cropView = view.findViewById(R.id.cropperView)

        seekBar1 = view.findViewById(R.id.seekBar)
        seekBar2 = view.findViewById(R.id.seekBar2)

        playPauseButton1 = view.findViewById(R.id.playPauseButton)
        playPauseButton2 = view.findViewById(R.id.playPauseButton2)

        layoutMovieWrapper = view.findViewById(R.id.layout_movie_wrapper)

        setUpSimpleExoPlayer(requireContext())
        setUoGlPlayerView()

        sharedViewModel.videoPath.observe(viewLifecycleOwner) { path ->
            path?.let {
                videoView.setVideoURI(Uri.parse(path))
                playPauseButton2.setImageResource(R.drawable.baseline_play_arrow)
                binding.totalDurationTextView.text =
                    milliSecondsToTimer(getVideoDuration(requireContext(), path).toLong() * 1000)
                layoutMovieWrapper.removeAllViews()
                player?.release()
                setUpSimpleExoPlayer(requireContext())
                setUoGlPlayerView()
            }

            playerRestart()
        }

        sharedViewModel.booleanLiveData.observe(viewLifecycleOwner) { newValue ->
            if (newValue == true && videoView.isPlaying) {
                videoView.pause()
                videoView.seekTo(0)
                playPauseButton1.setImageResource(R.drawable.baseline_play_arrow)
            }
        }

        sharedViewModel.booleanCropVisible.observe(viewLifecycleOwner) { newValue ->
            if (newValue == true) {
                cropView.visibility = View.VISIBLE
                videoView.visibility = View.VISIBLE
                layoutMovieWrapper.visibility = View.GONE
                seekBar1.visibility = View.VISIBLE
                seekBar2.visibility = View.GONE

                player?.pause()
                player?.seekTo(0)
                playPauseButton2.setImageResource(R.drawable.baseline_play_arrow)


                playPauseButton1.visibility = View.VISIBLE
                playPauseButton2.visibility = View.INVISIBLE
            } else {
                cropView.visibility = View.GONE
                videoView.visibility = View.GONE
                layoutMovieWrapper.visibility = View.VISIBLE
                seekBar1.visibility = View.GONE
                seekBar2.visibility = View.VISIBLE

                playPauseButton1.visibility = View.INVISIBLE
                playPauseButton2.visibility = View.VISIBLE
            }
        }

        sharedViewModel.cropSelected.observe(viewLifecycleOwner) { newValue ->
            cropViewDisplay()
            when (newValue) {
                1 -> {
                    cropView.setFixedAspectRatio(false)
                }
                2 -> {
                    cropView.setFixedAspectRatio(true)
                    cropView.setAspectRatio(10, 10)
                }
                3 -> {
                    cropView.setFixedAspectRatio(true)
                    cropView.setAspectRatio(9, 16)
                }
                4 -> {
                    cropView.setFixedAspectRatio(true)
                    cropView.setAspectRatio(16, 9)
                }
            }
        }

        sharedViewModel.startCrop.observe(viewLifecycleOwner) { newValue ->
            if (newValue) {
                getDimension()
                startCrop()
            }
        }

        sharedViewModel.musicSet.observe(viewLifecycleOwner) { newValue ->
            if (newValue) {
                audioPlayer = MediaPlayer.create(requireContext(), Uri.parse(myMusicUri))
                musicReady = true
            }
        }

        sharedViewModel.audioVolumeLevel.observe(viewLifecycleOwner) {
            audioPlayer?.setVolume(it, it)
        }

        wannaGoBackCheckViewModel.observe(viewLifecycleOwner) {
            if (it) {
                Log.d("HELLOJIMMY", "showFullScreenDialog: Go Back")
                fragmentSwap()
            }
        }

        sharedViewModel.videoVolumeLevel.observe(viewLifecycleOwner) {
            player!!.volume = it
        }

        sharedViewModel.musicSelectPauseEveryThing.observe(viewLifecycleOwner) {
            if (it) {
                player?.seekTo(0)
                player?.pause()
                audioPlayer?.let {
                    audioPlayer?.seekTo(0)
                    audioPlayer?.pause()
                }

                binding.playPauseButton2.setImageResource(R.drawable.baseline_play_arrow)

                sharedViewModel.musicSelectPauseEveryThing(false)

            }
        }

        sharedViewModel.crossClick.observe(viewLifecycleOwner) {
            if (it) {
                audioPlayer?.stop()
                audioPlayer?.reset()
                player?.seekTo(0)
                player?.pause()
                binding.playPauseButton2.setImageResource(R.drawable.baseline_play_arrow)
            }
        }

        sharedViewModel.stopAllMusic.observe(viewLifecycleOwner) { newValue ->
            if (newValue) {
                audioPlayer?.pause()
            }
        }

        playerRestart()

    }

    private fun playerRestart() {
        player?.addListener(object : Player.Listener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    player!!.seekTo(0)
                    if (musicReady && audioPlayer?.isPlaying == true) {
                        audioPlayer?.seekTo(0)
                    }
                }
            }
        })
    }

    private fun setUoGlPlayerView() {
        ePlayerView = EPlayerView(requireContext())
        ePlayerView!!.setSimpleExoPlayer(player)

        val videoSize = getVideoSize(requireContext(), Uri.parse(mainCachedFile))
        if (videoSize != null) {

            val layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )

            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

            ePlayerView!!.layoutParams = layoutParams
        } else {
            ePlayerView!!.layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        ePlayerView!!.layoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )

        layoutMovieWrapper.setBackgroundColor(Color.TRANSPARENT)
        layoutMovieWrapper.invalidate()

        layoutMovieWrapper.addView(ePlayerView)

        ePlayerView!!.onResume()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCropSpeedBinding.inflate(inflater, container, false)

        setListener(this)

        screenWidth = getScreenWidth()

        childFragmentManager = getChildFragmentManager()

        currentChildFragment = MainFragment()
        effectMusicFragment = EffectMusicFragment()

        binding.videoView.setVideoURI(Uri.parse(mainCachedFile))

        binding.totalDurationTextView.text =
            milliSecondsToTimer(getVideoDuration(requireContext(), mainCachedFile).toLong() * 1000)


        binding.videoView.setOnPreparedListener { mediaPlayer: MediaPlayer? ->
            mediaPlayer?.let {
                onVideoPrepared()
            }
            this.mediaPlayer = mediaPlayer
            videoPlay()
        }

        binding.seekBar.max = binding.videoView.duration / 1000

        binding.videoView.setOnCompletionListener { onVideoCompleted() }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {
                binding.videoView.seekTo(seekBar.progress * 1000)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                mHandler.removeCallbacks(mUpdateTimeTask)
                binding.videoView.seekTo(mStartPosition * 1000 + seekBar.progress)
            }
        })

        binding.seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val newPosition = progress * 1000L
                    player!!.seekTo(newPosition)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}

        })

        binding.backBtn.setOnClickListener {
            singleClick {
                exitDialog()
            }
        }

        childFragmentManager!!.beginTransaction()
            .add(R.id.fragment_container_main, currentChildFragment as MainFragment).commit()

        binding.enhanceBtn.setOnClickListener {
            singleClick {
                enhanced = true
                binding.enhanceBtn.visibility = View.GONE
                binding.backTextBtn.visibility = View.VISIBLE

                binding.videoView.visibility = View.GONE

                binding.rotateLeft.visibility = View.GONE
                binding.rotateRight.visibility = View.GONE

                binding.seekBar.visibility = View.GONE
                binding.playPauseButton.visibility = View.GONE

                binding.seekBar2.visibility = View.VISIBLE
                binding.playPauseButton2.visibility = View.VISIBLE

                playPauseButton2.setImageResource(R.drawable.baseline_play_arrow)

                player?.release()

                setUpSimpleExoPlayer(requireContext())
                setUoGlPlayerView()

                playerRestart()

                binding.videoView.stopPlayback()
                binding.layoutMovieWrapper.visibility = View.VISIBLE

                sharedViewModel.cropViewVisible(false)
                childFragmentManager!!.beginTransaction()
                    .replace(
                        R.id.fragment_container_main,
                        effectMusicFragment as EffectMusicFragment
                    )
                    .commit()
            }
        }

        binding.playPauseButton2.setOnClickListener {
            binding.seekBar2.max = (player!!.duration / 1000).toInt()
            if (player!!.isPlaying) {
                binding.playPauseButton2.setImageResource(R.drawable.baseline_play_arrow)
                player!!.pause()
                if (musicReady && audioPlayer?.isPlaying == true) {
                    audioPlayer?.pause()
                }
                stopTrackingSeekBar()
            } else {
                binding.playPauseButton2.setImageResource(R.drawable.baseline_pause)
                player!!.play()
                if (musicReady) {
                    audioPlayer?.start()
                }
                startTrackingSeekBar()
            }
        }

        binding.playPauseButton.setOnClickListener {
            if (binding.videoView.isPlaying) {
                binding.videoView.pause()
                binding.playPauseButton.setImageResource(R.drawable.baseline_play_arrow)
                stopTrackingSeekBar()
            } else {
                videoPlay()
            }
        }

        binding.backTextBtn.setOnClickListener {
            singleClick {
                sharedViewModel.musicSelectPauseEveryThing(true)
                showFullScreenDialog()
            }
        }

        binding.rotateRight.setOnClickListener {
            singleClick {
                sharedViewModel.musicSelectPauseEveryThing(true)
                rotateVideoCommand(1)
            }
        }

        binding.rotateLeft.setOnClickListener {
            singleClick {
                sharedViewModel.musicSelectPauseEveryThing(true)
                rotateVideoCommand(2)
            }
        }

        binding.saveBtn.setOnClickListener {
            singleClick {
                if (audioPlayer != null) {
                    audioPlayer!!.pause()
                }
                player?.pause()
                binding.playPauseButton.setImageResource(R.drawable.baseline_play_arrow)
                if (enhanced) {
                    sharedViewModel.enhanced(true)
                } else {
                    backSave = false
                    saveEditedVideo(requireContext())
                }
            }
        }

        return binding.root
    }

    private fun exitDialog() {
        val dialog = Dialog(requireContext(), R.style.FullScreenDialogStyle)
        dialog.setContentView(R.layout.exit_dialog)

        val noBtn = dialog.findViewById<TextView>(R.id.noBtn)
        val yesBtn = dialog.findViewById<TextView>(R.id.yesBtn)

        yesBtn.setOnClickListener {
            requireActivity().finish()
            sharedViewModel.stopAllMusic(true)
            dialog.dismiss()
        }

        noBtn.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun updateSeekBar2() {
        binding.seekBar2.progress = (player!!.currentPosition.toInt() / 1000)
        handler.postDelayed(runnable, 200)
    }

    private fun updateSeekBar() {
        binding.seekBar.progress = (binding.videoView.currentPosition)
        handler.postDelayed(runnable2, 200)
    }

    private fun startTrackingSeekBar() {
        handler.postDelayed(runnable, 0)
        handler.postDelayed(runnable2, 0)
    }

    private fun stopTrackingSeekBar() {
        handler.removeCallbacks(runnable)
        handler.removeCallbacks(runnable2)
    }

    private fun rotateVideoCommand(rotateValue: Int) {
        val sb3 = StringBuilder()

        sb3.append("[0:v]transpose=$rotateValue[out]")

        val tempFile = createCacheTempFile(requireContext())

        videoDuration = getVideoDuration(requireContext(), mainCachedFile)

        Log.d("videoDuration", "rotateVideoCommand: $videoDuration")

        executeFFMPEG(
            arrayOf(
                "-y",
                "-ss",
                "0",
                "-t",
                videoDuration.toString(),
                "-i",
                mainCachedFile,
                "-vf",
                sb3.toString(),
                "-c:a",
                "copy",
                tempFile
            ), tempFile, 0
        )
    }

    private fun executeFFMPEG(strArr: Array<String>, str: String, valueCheck: Int) {
        sharedViewModel.pauseVideo(true)

        val progressDialog = CustomWaitingDialog(requireContext())
        progressDialog.setCloseButtonClickListener {
            progressDialog.dismiss()
            FFmpeg.cancel()
        }
        progressDialog.show()
        progressDialog.setText("Please wait")

        val ffmpegCommand: String = commandsGenerator(strArr)
        FFmpeg.executeAsync(
            ffmpegCommand
        ) { _, returnCode ->
            Config.printLastCommandOutput(Log.INFO)
            progressDialog.dismiss()
            when (returnCode) {
                Config.RETURN_CODE_SUCCESS -> {
                    progressDialog.dismiss()
                    mainCachedFile = str
                    sharedViewModel.setVideoUri(str)
                    Config.resetStatistics()
                    sharedViewModel.animateKnob(700)

                    when (valueCheck) {
                        1 -> {
                            cropViewDisplay()
                            sharedViewModel.switchFragmentB(true)
                        }
                    }
                }
                Config.RETURN_CODE_CANCEL -> {
                    try {
                        Config.resetStatistics()
                        progressDialog.setText("Please wait")
                        File(str).delete()
                        deleteFromGallery(str, requireContext())
                    } catch (th: Throwable) {
                        th.printStackTrace()
                    }
                }
                else -> {
                    try {
                        File(str).delete()
                        deleteFromGallery(str, requireContext())
                    } catch (th: Throwable) {
                        th.printStackTrace()
                    }
                }
            }
        }

        when (valueCheck) {
            0 -> {
                Config.printLastCommandOutput(Log.INFO)
                Config.enableStatisticsCallback {
                    val percentage =
                        ((it.time.toFloat() / (videoDuration * 1000).toFloat()) * 100).toInt()
                    if (percentage in 0..100) {
                        progressDialog.setText("Rotated $percentage%")
                    } else {
                        progressDialog.setText("Please wait")
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player!!.release()
    }

    private fun videoPlay() {
        binding.videoView.start()
        binding.playPauseButton.setImageResource(R.drawable.baseline_pause)
        startTrackingSeekBar()
    }

    private fun onVideoCompleted() {
        binding.seekBar.progress = 0
        binding.videoView.seekTo(0)
        binding.videoView.start()
        binding.playPauseButton.setImageResource(R.drawable.baseline_pause)
    }

    private val mUpdateTimeTask: Runnable = object : Runnable {
        override fun run() {
            if (binding.seekBar.progress >= binding.seekBar.max) {
                binding.seekBar.progress = binding.videoView.currentPosition - mStartPosition * 1000
                binding.videoView.seekTo(mStartPosition * 1000)
                binding.videoView.pause()
                binding.seekBar.progress = 0
                binding.playPauseButton.setImageResource(R.drawable.baseline_play_arrow)
            } else {
                binding.seekBar.progress = binding.videoView.currentPosition - mStartPosition * 1000
                mHandler.postDelayed(this, 100)
            }
        }
    }

    private fun onVideoPrepared() {
        mDuration = binding.videoView.duration / 1000
        setSeekBarPosition()
    }

    private fun setSeekBarPosition() {
        if (mDuration >= mMaxDuration) {
            mStartPosition = 0
            mEndPosition = mMaxDuration
            mDurationWithoutEdit = mDuration
        } else {
            mStartPosition = 0
            mEndPosition = mDuration
            mDurationWithoutEdit = mDuration
        }
        mTimeVideo = mDuration
        binding.seekBar.max = mDurationWithoutEdit * 1000
        binding.videoView.seekTo(mStartPosition * 1000)

        var mStart = mStartPosition.toString() + ""
        if (mStartPosition < 10) mStart = "0$mStartPosition"
        mStart.toInt() / 60
        mStart.toInt() % 60
        var mEnd = mEndPosition.toString() + ""
        if (mEndPosition < 10) mEnd = "0$mEndPosition"
        mEnd.toInt() / 60
        mEnd.toInt() % 60

    }

    private fun cropViewDisplay() {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(mainCachedFile)

        keyCodeR = Integer.valueOf(mediaMetadataRetriever.extractMetadata(18)!!).toInt()
        keyCodeQ = Integer.valueOf(mediaMetadataRetriever.extractMetadata(19)!!).toInt()
        keyCodeW = Integer.valueOf(mediaMetadataRetriever.extractMetadata(24)!!).toInt()


        val layoutParams = binding.cropperView.layoutParams as FrameLayout.LayoutParams

        if (keyCodeW == 90 || keyCodeW == 270) {
            if (keyCodeR >= keyCodeQ) {
                if (keyCodeR >= screenWidth) {
                    layoutParams.height = screenWidth
                    layoutParams.width =
                        (screenWidth.toFloat() / (keyCodeR.toFloat() / keyCodeQ.toFloat())).toInt()
                } else {
                    layoutParams.width = screenWidth
                    layoutParams.height =
                        (keyCodeQ.toFloat() * (screenWidth.toFloat() / keyCodeR.toFloat())).toInt()
                }
            } else if (keyCodeQ >= screenWidth) {
                layoutParams.width = screenWidth
                layoutParams.height =
                    (screenWidth.toFloat() / (keyCodeQ.toFloat() / keyCodeR.toFloat())).toInt()
            } else {
                layoutParams.width =
                    (keyCodeR.toFloat() * (screenWidth.toFloat() / keyCodeQ.toFloat())).toInt()
                layoutParams.height = screenWidth
            }
        } else if (keyCodeR >= keyCodeQ) {
            if (keyCodeR >= screenWidth) {
                layoutParams.width = screenWidth
                layoutParams.height =
                    (screenWidth.toFloat() / (keyCodeR.toFloat() / keyCodeQ.toFloat())).toInt()
            } else {
                layoutParams.width = screenWidth
                layoutParams.height =
                    (keyCodeQ.toFloat() * (screenWidth.toFloat() / keyCodeR.toFloat())).toInt()
            }
        } else if (keyCodeQ >= screenWidth) {
            layoutParams.width =
                (screenWidth.toFloat() / (keyCodeQ.toFloat() / keyCodeR.toFloat())).toInt()
            layoutParams.height = screenWidth
        } else {
            layoutParams.width =
                (keyCodeR.toFloat() * (screenWidth.toFloat() / keyCodeQ.toFloat())).toInt()
            layoutParams.height = screenWidth
        }
        binding.cropperView.layoutParams = layoutParams

        binding.cropperView.setImageBitmap(
            Bitmap.createBitmap(
                layoutParams.width, layoutParams.height, Bitmap.Config.ARGB_8888
            )
        )
    }

    private fun startCrop() {
        when (keyCodeW) {
            90 -> {
                try {
                    o = b
                    val i2: Int = z
                    u = b
                    v = a
                    m = y
                    n = z
                    s = y
                    t = a
                    ag = m - o
                    ah = v - i2
                    p = q - (ah + i2)
                } catch (e2: Exception) {
                    e2.printStackTrace()
                }
            }
            270 -> {
                try {
                    val i3: Int = b
                    val i4: Int = z
                    u = b
                    v = a
                    m = y
                    n = z
                    s = y
                    t = a
                    ag = m - i3
                    ah = v - i4
                    o = r - (ag + i3)
                    p = i4
                } catch (e3: Exception) {
                    e3.printStackTrace()
                }
            }
            else -> {
                try {
                    o = z
                    p = b
                    u = a
                    v = b
                    m = z
                    n = y
                    s = a
                    t = y
                    ag = u - o
                    ah = n - p
                } catch (e4: Exception) {
                    e4.printStackTrace()
                }
            }
        }
        cropVideoDuration = getVideoDuration(requireContext(), mainCachedFile).toString()


        cropOutputFilePath = createCacheTempFile(requireContext())

        try {
            val sb = java.lang.StringBuilder()
            sb.append("crop=w=")
            sb.append(ag)
            sb.append(":h=")
            sb.append(ah)
            sb.append(":x=")
            sb.append(o)
            sb.append(":y=")
            sb.append(p)
            executeFFMPEG(
                arrayOf(
                    "-y",
                    "-ss",
                    "0",
                    "-t",
                    cropVideoDuration!!,
                    "-i",
                    mainCachedFile,
                    "-strict",
                    "experimental",
                    "-vf",
                    sb.toString(),
                    "-r",
                    "15",
                    "-ab",
                    "128k",
                    "-vcodec",
                    "mpeg4",
                    "-acodec",
                    "copy",
                    "-b:v",
                    "2500k",
                    "-sample_fmt",
                    "s16",
                    "-ss",
                    "0",
                    "-t",
                    cropVideoDuration!!,
                    cropOutputFilePath!!
                ), cropOutputFilePath!!, 1
            )
        } catch (unused: Exception) {
            val file2 = File(cropOutputFilePath!!)
            if (file2.exists()) {
                file2.delete()
                return
            }
            Toast.makeText(requireContext(), "please select any option!", Toast.LENGTH_LONG).show()
        }
    }

    private fun getDimension() {
        if (keyCodeW == 90 || keyCodeW == 270) {
            aa = keyCodeQ.toFloat()
            ab = keyCodeR.toFloat()
            ac = binding.cropperView.width.toFloat()
            ad = binding.cropperView.height.toFloat()
            z = (Edge.LEFT.coordinate * aa / ac).toInt()
            a = (Edge.RIGHT.coordinate * aa / ac).toInt()
            b = (Edge.TOP.coordinate * ab / ad).toInt()
            y = (Edge.BOTTOM.coordinate * ab / ad).toInt()
            return
        }
        aa = keyCodeR.toFloat()
        ab = keyCodeQ.toFloat()
        ac = binding.cropperView.width.toFloat()
        ad = binding.cropperView.height.toFloat()
        z = (Edge.LEFT.coordinate * aa / ac).toInt()
        a = (Edge.RIGHT.coordinate * aa / ac).toInt()
        b = (Edge.TOP.coordinate * ab / ad).toInt()
        y = (Edge.BOTTOM.coordinate * ab / ad).toInt()
    }

    private fun showFullScreenDialog() {
        val dialog = Dialog(requireContext(), R.style.FullScreenDialogStyle)
        dialog.setContentView(R.layout.back_dialog)

        val btnYes = dialog.findViewById<TextView>(R.id.yesBtn)
        val btnNo = dialog.findViewById<TextView>(R.id.noBtn)

        btnYes.setOnClickListener {
            Log.d("HELLOJIMMY", "showFullScreenDialog: Yes Click")
            backSave = true
            wannaGoBack = true
            enhanced = false

            sharedViewModel.enhanced(true)
            sharedViewModel.stopAllMusic(true)

            dialog.dismiss()
        }

        btnNo.setOnClickListener {
            backSave = false
            wannaGoBack = true

            wannaGoBackCheckViewModel.postValue(true)

            player!!.release()

            sharedViewModel.switchFragmentB(true)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun fragmentSwap() {
        Log.d("HELLOJIMMY", "showFullScreenDialog: Finally Going Back")

        enhanced = false
        binding.enhanceBtn.visibility = View.VISIBLE
        binding.backTextBtn.visibility = View.GONE

        binding.seekBar2.visibility = View.VISIBLE
        binding.playPauseButton2.visibility = View.VISIBLE

        binding.rotateLeft.visibility = View.VISIBLE
        binding.rotateRight.visibility = View.VISIBLE

        binding.videoView.visibility = View.GONE
        binding.layoutMovieWrapper.visibility = View.VISIBLE

        ePlayerView!!.onPause()
        binding.layoutMovieWrapper.removeView(ePlayerView)
        player!!.release()

        audioPlayer?.stop()
        audioPlayer?.reset()

        binding.seekBar.visibility = View.GONE
        binding.playPauseButton.visibility = View.GONE

        val layoutParams = binding.frameLayout.layoutParams
        layoutParams.width = screenWidth
        layoutParams.height = screenWidth
        binding.frameLayout.layoutParams = layoutParams

        layoutMovieWrapper.removeAllViews()
        player?.release()

        setUpSimpleExoPlayer(requireContext())
        setUoGlPlayerView()

        playerRestart()

        Log.d("HELLOJIMMY", "showFullScreenDialog: playerReset")

        wannaGoBackCheckViewModel.postValue(false)

        childFragmentManager!!.beginTransaction()
            .replace(R.id.fragment_container_main, currentChildFragment as MainFragment).commit()
    }

    override fun onUtilityFunctionCalled() {
        fragmentSwap()
    }

}