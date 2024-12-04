package com.udacity.catpoint.image.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsResponse;
import software.amazon.awssdk.services.rekognition.model.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Image Recognition Service that can identify cats. Requires AWS credentials to
 * be entered in config.properties to work. Steps to make work (optional): 1.
 * Log into AWS and navigate to the AWS console 2. Search for IAM then click on
 * Users in the IAM nav bar 3. Click Add User. Enter a user name and select
 * Programmatic access 4. Next to Permissions. Select 'Attach existing policies
 * directly' and attach 'AmazonRekognitionFullAccess' 5. Next through the
 * remaining screens. Copy the 'Access key ID' and 'Secret access key' for this
 * user. 6. Create a config.properties file in the src/main/resources dir
 * containing the keys referenced in this class: aws.id=[your access key id]
 * aws.secret=[your Secret access key] aws.region=[an aws region of choice. For
 * example: us-east-2]
 */
public class AwsImageService implements IService {

    private final Logger logger = LoggerFactory.getLogger(AwsImageService.class);

    // AWS recommendation is to maintain only a single instance of client objects
    private static RekognitionClient rekognitionClient;

    public AwsImageService() {
        initializeRekognitionClient();
    }

    /**
     * Returns true if the provided image contains a cat.
     *
     * @param image Image to scan
     * @param confidenceThreshold Minimum threshold to consider for cat. For
     * example, 90.0f would require 90% confidence minimum
     * @return True if the image contains a cat, false otherwise
     */
    public boolean imageContainsCat(BufferedImage image, float confidenceThreshold) {
        Image awsImage = createAwsImage(image);
        if (awsImage == null) {
            return false;
        }

        DetectLabelsRequest detectLabelsRequest = DetectLabelsRequest.builder()
                .image(awsImage)
                .minConfidence(confidenceThreshold)
                .build();

        DetectLabelsResponse response = rekognitionClient.detectLabels(detectLabelsRequest);
        logLabels(response);
        return response.labels().stream()
                .anyMatch(label -> label.name().toLowerCase().contains("cat"));
    }

    /**
     * Initializes the AWS Rekognition client using credentials from the
     * config.properties file.
     */
    private void initializeRekognitionClient() {
        Properties properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (inputStream == null) {
                throw new IOException("Config file not found");
            }
            properties.load(inputStream);
        } catch (IOException ex) {
            logger.error("Unable to initialize AWS Rekognition, no properties file found", ex);
            return;
        }

        String awsId = properties.getProperty("aws.id");
        String awsSecret = properties.getProperty("aws.secret");
        String awsRegion = properties.getProperty("aws.region");

        AwsCredentials awsCredentials = AwsBasicCredentials.create(awsId, awsSecret);
        rekognitionClient = RekognitionClient.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .region(Region.of(awsRegion))
                .build();
    }

    /**
     * Converts a BufferedImage to an AWS Image object.
     *
     * @param bufferedImage The input BufferedImage
     * @return An AWS Image object, or null if an error occurs
     */
    private Image createAwsImage(BufferedImage bufferedImage) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(bufferedImage, "jpg", outputStream);
            return Image.builder().bytes(SdkBytes.fromByteArray(outputStream.toByteArray())).build();
        } catch (IOException ex) {
            logger.error("Error building image byte array", ex);
            return null;
        }
    }

    /**
     * Logs the detected labels for debugging and analysis purposes.
     *
     * @param response The response from the Rekognition DetectLabels API
     */
    private void logLabels(DetectLabelsResponse response) {
        String labelsLog = response.labels().stream()
                .map(label -> String.format("%s (%.1f%%)", label.name(), label.confidence()))
                .collect(Collectors.joining(", "));
        logger.info("Detected Labels: {}", labelsLog);
    }
}
