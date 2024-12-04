package com.udacity.catpoint.security.application;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.udacity.catpoint.security.data.AlarmStatus;
import com.udacity.catpoint.security.service.SecurityService;
import com.udacity.catpoint.security.service.StyleService;

import net.miginfocom.swing.MigLayout;

/**
 * Panel containing the 'camera' output. Allows users to 'refresh' the camera
 * by uploading their own picture, and 'scan' the picture, sending it for image analysis.
 */
public class ImagePanel extends JPanel implements StatusListener {

    private final SecurityService securityService;

    private JLabel cameraHeader;
    private JLabel cameraLabel;
    private BufferedImage currentCameraImage;

    private static final int IMAGE_WIDTH = 300;
    private static final int IMAGE_HEIGHT = 225;

    public ImagePanel(SecurityService securityService) {
        super();
        this.securityService = securityService;
        setLayout(new MigLayout());
        securityService.addStatusListener(this);

        initializeComponents();
    }

    /**
     * Initializes the UI components and sets up the layout.
     */
    private void initializeComponents() {
        // Camera header with title
        cameraHeader = new JLabel("Camera Feed");
        cameraHeader.setFont(StyleService.HEADING_FONT);

        // Camera image label
        cameraLabel = new JLabel();
        cameraLabel.setBackground(Color.WHITE);
        cameraLabel.setPreferredSize(new Dimension(IMAGE_WIDTH, IMAGE_HEIGHT));
        cameraLabel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

        // Button to upload a new image as the current camera image
        JButton refreshCameraButton = new JButton("Refresh Camera");
        refreshCameraButton.addActionListener(e -> loadNewImage());

        // Button to scan the uploaded image for cat detection
        JButton scanImageButton = new JButton("Scan Picture");
        scanImageButton.addActionListener(e -> scanImage());

        // Add components to the layout
        add(cameraHeader, "span 3, wrap");
        add(cameraLabel, "span 3, wrap");
        add(refreshCameraButton);
        add(scanImageButton);
    }

    /**
     * Loads a new image from the file system to be used as the camera feed.
     */
    private void loadNewImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File("."));
        chooser.setDialogTitle("Select Picture");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            currentCameraImage = ImageIO.read(chooser.getSelectedFile());
            Image scaledImage = new ImageIcon(currentCameraImage)
                    .getImage()
                    .getScaledInstance(IMAGE_WIDTH, IMAGE_HEIGHT, Image.SCALE_SMOOTH);
            cameraLabel.setIcon(new ImageIcon(scaledImage));
        } catch (IOException | NullPointerException e) {
            JOptionPane.showMessageDialog(null, "Invalid image selected.");
        }

        repaint();
    }

    /**
     * Sends the current camera image to the image service for processing.
     */
    private void scanImage() {
        if (currentCameraImage != null) {
            securityService.processImage(currentCameraImage);
        } else {
            JOptionPane.showMessageDialog(null, "No image to scan. Please refresh the camera.");
        }
    }

    @Override
    public void notify(AlarmStatus status) {
        // No behavior necessary for status updates in this panel
    }

    @Override
    public void catDetected(boolean catDetected) {
        // Update header text based on cat detection status
        if (catDetected) {
            cameraHeader.setText("DANGER - CAT DETECTED");
        } else {
            cameraHeader.setText("Camera Feed - No Cats Detected");
        }
    }

    @Override
    public void sensorStatusChanged() {
        // No behavior necessary for sensor status changes
    }
}
