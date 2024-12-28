package com.example.springbootlocalstackdemo

import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.File

@Service
class S3Service(private val s3Client: S3Client) {
    fun uploadFile(bucketName: String, key: String, file: File) {

        // check if there is bucket with the given name, if not then create a bucket
         if (!s3Client.listBuckets().buckets().any { it.name() == bucketName }) {
             s3Client.createBucket { it.bucket(bucketName) }
         }

        // create a PutObjectRequest object with the bucket name and key
        // and use the putObject method of the S3Client to upload the file
        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .build()
        s3Client.putObject(putObjectRequest, file.toPath())
    }

    // function to download a file from S3 bucket
    fun downloadFile(bucketName: String, key: String): String {
        // create a GetObjectRequest object with the bucket name and key
        // and use the getObject method of the S3Client to download the file
        val getObjectRequest = GetObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .build()
        return String(s3Client.getObject(getObjectRequest).readAllBytes())
    }
}