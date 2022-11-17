package com.example.webrtcstudy

import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

open class AppSDPObserver : SdpObserver {
    override fun onCreateSuccess(p0: SessionDescription?) {

    }

    override fun onSetSuccess() {

    }

    override fun onCreateFailure(p0: String?) {

    }

    override fun onSetFailure(p0: String?) {

    }
}