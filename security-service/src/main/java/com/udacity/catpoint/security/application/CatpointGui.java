package com.udacity.catpoint.security.application;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.udacity.catpoint.image.service.FakeImageService;
import com.udacity.catpoint.security.data.PretendDatabaseSecurityRepositoryImpl;
import com.udacity.catpoint.security.data.SecurityRepository;
import com.udacity.catpoint.security.service.SecurityService;

import net.miginfocom.swing.MigLayout;

/**
 * This is the primary JFrame for the application that contains all the top-level JPanels.
 *
 * We're not using any dependency injection framework, so this class also handles constructing
 * all our dependencies and providing them to other classes as necessary.
 */
public class CatpointGui extends JFrame {

    // ---------------------- Instance Variables ----------------------

    // Repository for storing security-related data
    private final SecurityRepository securityRepo = new PretendDatabaseSecurityRepositoryImpl(); 
    private final FakeImageService imageService = new FakeImageService(); // Service for handling image-based operations
    private final SecurityService securityService = new SecurityService(securityRepo, imageService); // Main service that handles business logic

    // Panels for different parts of the UI
    private final DisplayPanel displayPanel = new DisplayPanel(securityService);
    private final SensorPanel sensorPanel = new SensorPanel(securityService);
    private final ControlPanel controlPanel = new ControlPanel(securityService, sensorPanel);
    private final ImagePanel imagePanel = new ImagePanel(securityService);

    public CatpointGui() {
        // Setting the window properties
        setLocation(100, 100);
        setSize(600, 850);
        setTitle("Very Secure App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create and set up the main panel layout
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new MigLayout());
        
        // Add individual panels to the main layout
        mainPanel.add(displayPanel, "wrap");
        mainPanel.add(imagePanel, "wrap");
        mainPanel.add(controlPanel, "wrap");
        mainPanel.add(sensorPanel);

        // Add the main panel to the content pane
        getContentPane().add(mainPanel);
    }
}
