package com.example.springbootlocalstackdemo

import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.CreateTopicRequest
import software.amazon.awssdk.services.sns.model.PublishRequest
import software.amazon.awssdk.services.sns.model.SubscribeRequest
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesResponse
import software.amazon.awssdk.services.sqs.model.QueueAttributeName
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import java.io.File
import java.nio.file.Files


@Component
class ApplicationStartupRunner(
    private val sqsAsyncClient: SqsAsyncClient,
    private val snsClient: SnsClient,
    private val s3Service: S3Service
) : ApplicationRunner {
    override fun run(args: org.springframework.boot.ApplicationArguments?) {

        // setup
        val response = sqsAsyncClient.createQueue(CreateQueueRequest.builder().queueName("sample-sqs").build())
        val topic = snsClient.createTopic(CreateTopicRequest.builder().name("sample-topic").build())
        val topicArn: String = topic.topicArn()

        response.get().queueUrl().let {
            val queueAttributes: GetQueueAttributesResponse = sqsAsyncClient.getQueueAttributes(
                GetQueueAttributesRequest.builder()
                    .attributeNames(QueueAttributeName.QUEUE_ARN)
                    .queueUrl(it)
                    .build()
            ).get()
            val queueArn = queueAttributes.attributes()[QueueAttributeName.QUEUE_ARN]

            snsClient.subscribe(
                SubscribeRequest.builder()
                    .topicArn(topicArn)
                    .protocol("sqs")
                    .endpoint(queueArn)
                    .build()
            )
        }


        println("Application started")
        val file = File("sample.txt")
        Files.writeString(file.toPath(), "Hello, World!")
        s3Service.uploadFile("sample-bucket", file.name, file)
        s3Service.downloadFile("sample-bucket", file.name).let {
            println(it)
        }
        sqsAsyncClient.sendMessage(SendMessageRequest.builder()
            .queueUrl(response.get().queueUrl())
            .messageBody("Hello, World! from SQS!")
            .build()
        )
        snsClient.publish(PublishRequest.builder().topicArn(topicArn).message("Hello, World! from SNS!").build())
    }
}