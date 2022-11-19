package com.example.webrtcstudy

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.webrtc.*
import org.webrtc.PeerConnection.Observer

class WebRTCClient(
    context: Application,
    private val observer: PeerConnection.Observer
) {
    companion object {
        private const val LOCAL_TRACK_ID = "local_track"
        private const val LOCAL_STREAM_ID = "local_track"
        private const val FIELD_TRIALS = "WebRTC-H264HighProfile/Enabled/"
        private const val ICE_SERVER_URL = "stun:stun.l.google.com:19302"
    }

    private val constraints = MediaConstraints().apply {
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
    }

    private val rootEglBase: EglBase = EglBase.create()

    private var localAudioTrack: AudioTrack? = null
    private var localVideoTrack: VideoTrack? = null

    private val database = Firebase.firestore

    private val peerConnectionFactory by lazy { buildPeerConnectionFactory() }

    private val videoCapturer by lazy { getVideoCapturer(context) }

    private val peerConnection by lazy { buildPeerConnection() }

    private val localVideoSource by lazy { peerConnectionFactory.createVideoSource(false) }

    private val audioSource by lazy { peerConnectionFactory.createAudioSource(MediaConstraints()) }

    init {
        initPeerConnectionFactory(context)
    }

    private val iceServer =
        listOf(PeerConnection.IceServer.builder(ICE_SERVER_URL).createIceServer())

    private fun initPeerConnectionFactory(context: Application) {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .setFieldTrials(FIELD_TRIALS)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
    }

    private fun buildPeerConnectionFactory() = PeerConnectionFactory.builder().apply {
        setVideoDecoderFactory(DefaultVideoDecoderFactory(rootEglBase.eglBaseContext))
        setVideoEncoderFactory(DefaultVideoEncoderFactory(rootEglBase.eglBaseContext, true, true))
        setOptions(PeerConnectionFactory.Options().apply {
            disableEncryption = true
            disableNetworkMonitor = true
        })
    }.createPeerConnectionFactory()

    private fun buildPeerConnection() =
        peerConnectionFactory.createPeerConnection(iceServer, observer)

    fun initSurfaceView(view: SurfaceViewRenderer) = view.run {
        setMirror(true)
        setEnableHardwareScaler(true)
        init(rootEglBase.eglBaseContext, null)
    }

    fun startLocalView(localVideoOutput: SurfaceViewRenderer){
        val surfaceTextureHelper = SurfaceTextureHelper.create(Thread.currentThread().name, rootEglBase.eglBaseContext)
        (videoCapturer as VideoCapturer).initialize(surfaceTextureHelper, localVideoOutput.context, localVideoSource.capturerObserver)
        videoCapturer.startCapture(320, 240, 60)
        localAudioTrack = peerConnectionFactory.createAudioTrack(LOCAL_TRACK_ID + "_audio", audioSource);
        localVideoTrack = peerConnectionFactory.createVideoTrack(LOCAL_TRACK_ID, localVideoSource)
        localVideoTrack?.addSink(localVideoOutput)
        val localStream = peerConnectionFactory.createLocalMediaStream(LOCAL_STREAM_ID)
        localStream.addTrack(localVideoTrack)
        localStream.addTrack(localAudioTrack)
        peerConnection?.addStream(localStream)
    }
    private fun getVideoCapturer(context: Context) =
        Camera2Enumerator(context).run {
            deviceNames.find {
                isFrontFacing(it)
            }?.let {
                createCapturer(it, null)
            } ?: throw IllegalStateException()
        }

    private fun PeerConnection.Call(roomID: String) {
        createOffer(object : SdpObserver {
            override fun onCreateSuccess(p0: SessionDescription?) {
                peerConnection?.setLocalDescription(this, p0)
                val offer = hashMapOf(
                    "sdp" to p0?.description,
                    "type" to p0?.type
                )
                database.collection("calls").document(roomID).set(offer)
            }

            override fun onSetSuccess() {
                Log.e("Rsupport", "onSetSuccess")
            }

            override fun onCreateFailure(p0: String?) {
                Log.e("Rsupport", "onCreateFailure: $p0")
            }

            override fun onSetFailure(p0: String?) {

            }

        }, constraints)
    }

    private fun PeerConnection.Answer(roomID: String) {
        createAnswer(object : SdpObserver {
            override fun onCreateSuccess(p0: SessionDescription?) {
                Log.e("Rsupport", "setLocalDescription")
                peerConnection?.setLocalDescription(this,p0)
                val answer = hashMapOf(
                    "sdp" to p0?.description,
                    "type" to p0?.type
                )
                database.collection("calls").document(roomID).set(answer)
            }

            override fun onSetSuccess() {
                Log.e("Rsupport", "onSetSuccess")
            }

            override fun onCreateFailure(p0: String?) {
                Log.e("Rsupport", "onCreateFailure: $p0")
            }

            override fun onSetFailure(p0: String?) {

            }

        }, constraints)
    }

    fun onRemoteSessionReceived(description: SessionDescription) {
        Log.e("Rsupport", "setRemoteDescription")
        peerConnection?.setRemoteDescription(object : SdpObserver{
            override fun onCreateSuccess(p0: SessionDescription?) {

            }

            override fun onSetSuccess() {
                Log.e("Rsupport", "onSetSuccess")
            }

            override fun onCreateFailure(p0: String?) {
                Log.e("Rsupport", "onCreateFailure: $p0")
            }

            override fun onSetFailure(p0: String?) {

            }

        },description)
    }

    fun addCandidate(iceCandidate: IceCandidate?){
        peerConnection?.addIceCandidate(iceCandidate)
    }

    fun call(roomID: String) =
        peerConnection?.Call(roomID)

    fun answer(roomID: String) =
        peerConnection?.Answer(roomID)

    fun destroy(){
        peerConnection?.dispose()
        peerConnection?.close()
    }
}