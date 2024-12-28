#!/bin/bash
sleep 5
echo "Creating bucket sample-bucket"
aws --no-cli-pager --endpoint-url=http://localstack:4566 s3api create-bucket --bucket sample-bucket-2
aws --no-cli-pager --endpoint-url=http://localstack:4566 sns create-topic --name sample-topic
TOPIC_ARN=$(aws --no-cli-pager --endpoint-url=http://localstack:4566 sns create-topic --name sample-topic --query 'TopicArn' --output text)
echo "$TOPIC_ARN"
QUEUE_URL=$(aws --no-cli-pager --endpoint-url=http://localstack:4566 sqs create-queue --queue-name sample-queue --query 'QueueUrl' --output text)
echo "$QUEUE_URL"
QUEUE_ARN=$(aws sqs --no-cli-pager --endpoint-url=http://localstack:4566 get-queue-attributes --queue-url "$QUEUE_URL" --attribute-names 'QueueArn' --output text)
aws --no-cli-pager --endpoint-url=http://localstack:4566 sns subscribe --topic-arn "$TOPIC_ARN" --protocol sqs --notification-endpoint "$QUEUE_ARN"
