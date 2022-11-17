package com.example.webrtcstudy

import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

interface SignalListener {
    fun onConnectionEstablished()
    fun onOfferReceived(description: SessionDescription)
    fun onAnswerReceived(description: SessionDescription)
    fun onIceCandidateReceived(iceCandidate: IceCandidate)
    fun onCallEnded()
}