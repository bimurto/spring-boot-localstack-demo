package com.example.springbootlocalstackdemo

import io.awspring.cloud.sqs.annotation.SqsListener
import org.springframework.stereotype.Service

@Service
class ApplicationListener {

    // function that listens to sqs event `sample-sqs` and process the event
    @SqsListener("sample-sqs")
    fun processSQSEvent(message: String) {
        println("Received message from SQS: $message")
    }
}