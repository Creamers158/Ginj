package info.ginj.ui;

import info.ginj.Ginj;
import info.ginj.model.Prefs;
import info.ginj.ui.listener.DragInsensitiveMouseClickListener;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * This "Star" Window is the original widget displayed at the border of the screen to initiate a capture
 * <p>
 * UI transparency based on sample code by MadProgrammer at https://stackoverflow.com/questions/26205164/java-custom-shaped-frame-using-image
 */
public class StarWindow extends JWindow {

    public static final Dimension SPLASH_SIZE = new Dimension(508, 292);

    public enum Border {TOP, LEFT, BOTTOM, RIGHT}

    public static final int CIRCLE_WIDTH_PIXELS = 50;
    public static final int CIRCLE_HEIGHT_PIXELS = 50;

    public static final int STAR_WIDTH_PIXELS = 150;
    public static final int STAR_HEIGHT_PIXELS = 150;

    public static final int SCREEN_CORNER_DEAD_ZONE_Y_PIXELS = 100;
    public static final int SCREEN_CORNER_DEAD_ZONE_X_PIXELS = 100;

    public static final float OPACITY_HALF = 0.5f;
    public static final float OPACITY_FULL = 1.0f;

    public static final int STAR_ONLY_RADIUS = 25;

    // Button sizes
    public static final int LARGE_SIZE_PIXELS = 40;
    public static final int MEDIUM_SIZE_PIXELS = 30;
    public static final int SMALL_SIZE_PIXELS = 20;

    // Button distance from center of star to center of button
    public static final int LARGE_RADIUS_PIXELS = 55;
    public static final int MEDIUM_RADIUS_PIXELS = 55;
    public static final int SMALL_RADIUS_PIXELS = 45;

    // Button index
    public static final int BTN_NONE = -1;
    public static final int BTN_CAPTURE = 0;
    public static final int BTN_HISTORY = 1;
    public static final int BTN_MORE = 2;

    // Button "states"
    public static final int LARGE = 0;
    public static final int MEDIUM = 1;
    public static final int SMALL = 2;

    // Precomputed constants
    private static final int OFFSET_X_LARGE = STAR_WIDTH_PIXELS / 2 - LARGE_SIZE_PIXELS / 2;
    private static final int OFFSET_X_MEDIUM = STAR_WIDTH_PIXELS / 2 - MEDIUM_SIZE_PIXELS / 2;
    private static final int OFFSET_X_SMALL = STAR_WIDTH_PIXELS / 2 - SMALL_SIZE_PIXELS / 2;
    private static final int OFFSET_Y_LARGE = STAR_HEIGHT_PIXELS / 2 - LARGE_SIZE_PIXELS / 2;
    private static final int OFFSET_Y_MEDIUM = STAR_HEIGHT_PIXELS / 2 - MEDIUM_SIZE_PIXELS / 2;
    private static final int OFFSET_Y_SMALL = STAR_HEIGHT_PIXELS / 2 - SMALL_SIZE_PIXELS / 2;
    private static final int SMALL_RADIUS_PIXELS_DIAG = (int) Math.round((SMALL_RADIUS_PIXELS * Math.sqrt(2)) / 2);
    private static final int MEDIUM_RADIUS_PIXELS_DIAG = (int) Math.round((MEDIUM_RADIUS_PIXELS * Math.sqrt(2)) / 2);
    private static final int LARGE_RADIUS_PIXELS_DIAG = (int) Math.round((LARGE_RADIUS_PIXELS * Math.sqrt(2)) / 2);


    // Caching
    private final static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private final Image[][] buttonImg = new Image[3][3]; // 3 buttons x 3 sizes
    Point[][] deltasByPosAndSize = new Point[3][3]; // 3 buttons x 3 sizes
    private Color defaultPaneBackground;

    public static Image appIcon = null;

    // Current state
    private boolean isWindowDeployed = false;
    private boolean isDragging = false;
    private int highlightedButtonId = BTN_NONE;

    // Monitor opened frames
    private HistoryFrame historyFrame;
    private MoreFrame moreFrame;
    private TargetManagementFrame targetManagementFrame;
    private final Set<TargetListChangeListener> targetListChangeListener = new HashSet<>();

    public StarWindow() {
        super();

//        showSplashIntro();

        appIcon = new ImageIcon(getClass().getResource("/img/app-icon-64.png")).getImage();

        // Background is transparent. Only the "star icon" is visible, and even then, it has half opacity
        setBackground(new Color(0, 0, 0, 0));
        setOpacity(OPACITY_HALF);

        // Prepare the main pane to paint and receive event
        JComponent contentPane = new MainPane();
        setContentPane(contentPane);
        addMouseBehaviour(contentPane);

        // Prepare to show Window
        pack();
        setLocationOnStartup();
        setAlwaysOnTop(true);
    }

    public static Image getAppIcon() {
        return appIcon;
    }


    public HistoryFrame getHistoryFrame() {
        return historyFrame;
    }

    public void setHistoryFrame(HistoryFrame historyFrame) {
        this.historyFrame = historyFrame;
    }

    public MoreFrame getMoreFrame() {
        return moreFrame;
    }

    public void setMoreFrame(MoreFrame moreFrame) {
        this.moreFrame = moreFrame;
    }

    public TargetManagementFrame getTargetManagementFrame() {
        return targetManagementFrame;
    }

    public void setTargetManagementFrame(TargetManagementFrame targetManagementFrame) {
        this.targetManagementFrame = targetManagementFrame;
    }

    public class MainPane extends JPanel {
        private BufferedImage starOnlyImg;
        private BufferedImage starRaysImg;
        // Array of images for the buttons.

        public MainPane() {
            try {
                starOnlyImg = ImageIO.read(getClass().getResource("/img/star-only.png"));
                starRaysImg = ImageIO.read(getClass().getResource("/img/star-rays.png"));

                Image originalImg = ImageIO.read(getClass().getResource("/img/capture.png"));
                buttonImg[BTN_CAPTURE][LARGE] = originalImg.getScaledInstance(LARGE_SIZE_PIXELS, LARGE_SIZE_PIXELS, Image.SCALE_DEFAULT);
                buttonImg[BTN_CAPTURE][MEDIUM] = originalImg.getScaledInstance(MEDIUM_SIZE_PIXELS, MEDIUM_SIZE_PIXELS, Image.SCALE_DEFAULT);
                buttonImg[BTN_CAPTURE][SMALL] = originalImg.getScaledInstance(SMALL_SIZE_PIXELS, SMALL_SIZE_PIXELS, Image.SCALE_DEFAULT);

                originalImg = ImageIO.read(getClass().getResource("/img/history.png"));
                buttonImg[BTN_HISTORY][LARGE] = originalImg.getScaledInstance(LARGE_SIZE_PIXELS, LARGE_SIZE_PIXELS, Image.SCALE_DEFAULT);
                buttonImg[BTN_HISTORY][MEDIUM] = originalImg.getScaledInstance(MEDIUM_SIZE_PIXELS, MEDIUM_SIZE_PIXELS, Image.SCALE_DEFAULT);
                buttonImg[BTN_HISTORY][SMALL] = originalImg.getScaledInstance(SMALL_SIZE_PIXELS, SMALL_SIZE_PIXELS, Image.SCALE_DEFAULT);

                originalImg = ImageIO.read(getClass().getResource("/img/more.png"));
                buttonImg[BTN_MORE][LARGE] = originalImg.getScaledInstance(LARGE_SIZE_PIXELS, LARGE_SIZE_PIXELS, Image.SCALE_DEFAULT);
                buttonImg[BTN_MORE][MEDIUM] = originalImg.getScaledInstance(MEDIUM_SIZE_PIXELS, MEDIUM_SIZE_PIXELS, Image.SCALE_DEFAULT);
                buttonImg[BTN_MORE][SMALL] = originalImg.getScaledInstance(SMALL_SIZE_PIXELS, SMALL_SIZE_PIXELS, Image.SCALE_DEFAULT);

            }
            catch (IOException e) {
                System.err.println("Error loading images for the main star UI");
                e.printStackTrace();
                System.exit(Ginj.ERR_STATUS_LOAD_IMG);
            }

            // Store the background so it can be changed and restored later on
            defaultPaneBackground = getBackground();
            setOpaque(false);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(starOnlyImg.getWidth(), starOnlyImg.getHeight());
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            if (isWindowDeployed) {
                // Image with rays
                g2d.drawImage(starRaysImg, 0, 0, this);
                if (!isDragging) {
                    // Draw 3 action icons, with size and position depending on highlight state
                    for (int button = 0; button < 3; button++) {
                        if (highlightedButtonId == BTN_NONE) {
                            g2d.drawImage(buttonImg[button][MEDIUM], deltasByPosAndSize[button][MEDIUM].x, deltasByPosAndSize[button][MEDIUM].y, this);
                        }
                        else {
                            if (button == highlightedButtonId) {
                                g2d.drawImage(buttonImg[button][LARGE], deltasByPosAndSize[button][LARGE].x, deltasByPosAndSize[button][LARGE].y, this);
                            }
                            else {
                                g2d.drawImage(buttonImg[button][SMALL], deltasByPosAndSize[button][SMALL].x, deltasByPosAndSize[button][SMALL].y, this);
                            }
                        }
                    }
                }
            }
            else {
                // Image without rays
                g2d.drawImage(starOnlyImg, 0, 0, this);
            }
            g2d.dispose();
        }
    }


    private void addMouseBehaviour(JComponent contentPane) {
        MouseInputListener mouseInputListener = new DragInsensitiveMouseClickListener(new MouseInputAdapter() {
            private Point mousePressedPoint;

            @Override
            public void mouseEntered(MouseEvent e) {
                setDeployed(contentPane, true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setDeployed(contentPane, false);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                // Get clicked point (relative to Window) and store it
                mousePressedPoint = e.getPoint();
                // If clicked happened on center of star
                // That is if distance between click point and center is less than radius
                if (Math.pow(StarWindow.this.getWidth() / 2.0 - mousePressedPoint.x, 2)
                        + Math.pow(StarWindow.this.getHeight() / 2.0 - mousePressedPoint.y, 2)
                        < Math.pow(STAR_ONLY_RADIUS, 2)) {
                    // Start dragging
                    isDragging = true;
                    StarWindow.this.repaint();
                }
            }

            // Move window to border closest to center
            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDragging) {
                    StarWindow.this.setLocation(getClosestPointOnScreenBorder());
                    isDragging = false;
                    StarWindow.this.repaint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDragging) {
                    StarWindow.this.setLocation(
                            StarWindow.this.getLocation().x + e.getX() - mousePressedPoint.x,
                            StarWindow.this.getLocation().y + e.getY() - mousePressedPoint.y
                    );
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (!isDragging) {
                    int hoveredButtonId = getButtonIdAtLocation(e.getX(), e.getY());
                    if (hoveredButtonId != highlightedButtonId) {
                        highlightedButtonId = hoveredButtonId;
                        StarWindow.this.repaint();
                    }
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (!isDragging) {
                    int clickedButtonId = getButtonIdAtLocation(e.getX(), e.getY());
                    // ignore other clicks
                    switch (clickedButtonId) {
                        case BTN_CAPTURE -> onCapture();
                        case BTN_HISTORY -> onHistory();
                        case BTN_MORE -> onMore();
                    }
                    isWindowDeployed = false;
                    StarWindow.this.repaint();
                }
            }

            // Find which button is being hovered, if any
            private int getButtonIdAtLocation(int x, int y) {
                int buttonId = BTN_NONE;
                for (int buttonIndex = 0; buttonIndex < 3; buttonIndex++) {
                    if (x >= deltasByPosAndSize[buttonIndex][LARGE].x
                            && x < deltasByPosAndSize[buttonIndex][LARGE].x + LARGE_SIZE_PIXELS
                            && y >= deltasByPosAndSize[buttonIndex][LARGE].y
                            && y < deltasByPosAndSize[buttonIndex][LARGE].y + LARGE_SIZE_PIXELS
                    ) {
                        buttonId = buttonIndex;
                        break;
                    }
                }
                return buttonId;
            }
        });

        StarWindow.this.addMouseListener(mouseInputListener);
        StarWindow.this.addMouseMotionListener(mouseInputListener);
    }

    private void setDeployed(JComponent contentPane, boolean deployed) {
        isWindowDeployed = deployed;
        if (deployed) {
            // Show the handle as opaque
            setOpacity(OPACITY_FULL);
            // And make the background of the window "visible", but filled with an almost transparent color
            // This has the effect of capturing mouse events on the full rectangular Window once it is deployed,
            // which is necessary so that mouse doesn't "fall in the transparent holes" causing MouseExited events that
            // make the window "retract" to the handle-only view
            contentPane.setOpaque(true);
            contentPane.setBackground(new Color(0, 0, 0, 1)); // 1/255 opacity
        }
        else {
            // Show the handle as semi-transparent
            setOpacity(OPACITY_HALF);
            // And make the background of the window invisible, so that all mouse events and clicks "pass through"
            contentPane.setOpaque(false);
            contentPane.setBackground(defaultPaneBackground); // Strangely enough, setting it to a transparent color break things up
        }
    }

    private void setLocationOnStartup() {
        Point savedCenterLocation = getSavedCenterLocation();
        computeButtonPositions(savedCenterLocation.x, savedCenterLocation.y);
        setLocation(savedCenterLocation.x - getWidth() / 2, savedCenterLocation.y - getHeight() / 2);
    }

    public static Point getSavedCenterLocation() {
        // Default to center of top border
        Point savedCenterLocation = new Point((int) (screenSize.getWidth() / 2), 0);
        try {
            // Try to load prefs and retrieve previous X/Y
            Border border = Border.valueOf(Prefs.get(Prefs.Key.STAR_WINDOW_POSTION_ON_BORDER));
            int distanceFromCorner = Integer.parseInt(Prefs.get(Prefs.Key.STAR_WINDOW_DISTANCE_FROM_CORNER));
            switch (border) {
                case TOP -> savedCenterLocation = new Point(Math.min(Math.max(distanceFromCorner, SCREEN_CORNER_DEAD_ZONE_X_PIXELS), screenSize.width - SCREEN_CORNER_DEAD_ZONE_X_PIXELS), 0);
                case BOTTOM -> savedCenterLocation = new Point(Math.min(Math.max(distanceFromCorner, SCREEN_CORNER_DEAD_ZONE_X_PIXELS), screenSize.width - SCREEN_CORNER_DEAD_ZONE_X_PIXELS), screenSize.height);
                case LEFT -> savedCenterLocation = new Point(screenSize.width, Math.min(Math.max(distanceFromCorner, SCREEN_CORNER_DEAD_ZONE_Y_PIXELS), screenSize.height - SCREEN_CORNER_DEAD_ZONE_Y_PIXELS));
                case RIGHT -> savedCenterLocation = new Point(0, Math.min(Math.max(distanceFromCorner, SCREEN_CORNER_DEAD_ZONE_Y_PIXELS), screenSize.height - SCREEN_CORNER_DEAD_ZONE_Y_PIXELS));
            }
        }
        catch (NullPointerException | IllegalArgumentException e) {
            // No (or unrecognized) position. keep default
        }
        return savedCenterLocation;
    }

    private Point getClosestPointOnScreenBorder() {
        // Compute window center
        Point center = new Point(getLocation().x + getWidth() / 2, getLocation().y + getHeight() / 2);

        // Closest to left or right ?
        int distanceX;
        int targetX;
        Border borderX;
        if (center.x < screenSize.width - center.x) {
            distanceX = center.x;
            targetX = 0;
            borderX = Border.RIGHT;
        }
        else {
            distanceX = screenSize.width - center.x;
            targetX = screenSize.width;
            borderX = Border.LEFT;
        }

        // Closest to top or bottom ?
        int distanceY;
        int targetY;
        Border borderY;
        if (center.y < screenSize.height - center.y) {
            distanceY = center.y;
            targetY = 0;
            borderY = Border.TOP;
        }
        else {
            distanceY = screenSize.height - center.y;
            targetY = screenSize.height;
            borderY = Border.BOTTOM;
        }

        // Now closest to a vertical or horizontal border
        Border border;
        int distanceFromCorner;
        if (distanceX < distanceY) {
            // Closest to vertical border
            // Keep Y unchanged unless too close to corner
            targetY = Math.min(Math.max(center.y, SCREEN_CORNER_DEAD_ZONE_Y_PIXELS), screenSize.height - SCREEN_CORNER_DEAD_ZONE_Y_PIXELS);
            border = borderX;
            distanceFromCorner = targetY;
        }
        else {
            // Closest to horizontal border
            // Keep X unchanged unless too close to corner
            targetX = Math.min(Math.max(center.x, SCREEN_CORNER_DEAD_ZONE_X_PIXELS), screenSize.width - SCREEN_CORNER_DEAD_ZONE_X_PIXELS);
            border = borderY;
            distanceFromCorner = targetX;
        }
        computeButtonPositions(targetX, targetY);

        Prefs.set(Prefs.Key.STAR_WINDOW_POSTION_ON_BORDER, border.name());
        Prefs.set(Prefs.Key.STAR_WINDOW_DISTANCE_FROM_CORNER, String.valueOf(distanceFromCorner));
        Prefs.save();
        return new Point(targetX - getWidth() / 2, targetY - getHeight() / 2);
    }

    // This fills up the deltasByPosAndSize array each time the window is move so that paintComponent() does not have to compute relative positions them over and over avain
    private void computeButtonPositions(int x, int y) {
        if (y == 0) {
            // TOP
            deltasByPosAndSize[0][LARGE] = new Point(OFFSET_X_LARGE - LARGE_RADIUS_PIXELS_DIAG, OFFSET_Y_LARGE + LARGE_RADIUS_PIXELS_DIAG);
            deltasByPosAndSize[0][MEDIUM] = new Point(OFFSET_X_MEDIUM - MEDIUM_RADIUS_PIXELS_DIAG, OFFSET_Y_MEDIUM + MEDIUM_RADIUS_PIXELS_DIAG);
            deltasByPosAndSize[0][SMALL] = new Point(OFFSET_X_SMALL - SMALL_RADIUS_PIXELS_DIAG, OFFSET_Y_SMALL + SMALL_RADIUS_PIXELS_DIAG);
            deltasByPosAndSize[1][LARGE] = new Point(OFFSET_X_LARGE, OFFSET_Y_LARGE + LARGE_RADIUS_PIXELS);
            deltasByPosAndSize[1][MEDIUM] = new Point(OFFSET_X_MEDIUM, OFFSET_Y_MEDIUM + MEDIUM_RADIUS_PIXELS);
            deltasByPosAndSize[1][SMALL] = new Point(OFFSET_X_SMALL, OFFSET_Y_SMALL + SMALL_RADIUS_PIXELS);
            deltasByPosAndSize[2][LARGE] = new Point(OFFSET_X_LARGE + LARGE_RADIUS_PIXELS_DIAG, OFFSET_Y_LARGE + LARGE_RADIUS_PIXELS_DIAG);
            deltasByPosAndSize[2][MEDIUM] = new Point(OFFSET_X_MEDIUM + MEDIUM_RADIUS_PIXELS_DIAG, OFFSET_Y_MEDIUM + MEDIUM_RADIUS_PIXELS_DIAG);
            deltasByPosAndSize[2][SMALL] = new Point(OFFSET_X_SMALL + SMALL_RADIUS_PIXELS_DIAG, OFFSET_Y_SMALL + SMALL_RADIUS_PIXELS_DIAG);
        }
        else if (y == screenSize.height) {
            // BOTTOM
            deltasByPosAndSize[0][LARGE] = new Point(OFFSET_X_LARGE - LARGE_RADIUS_PIXELS_DIAG, OFFSET_Y_LARGE - LARGE_RADIUS_PIXELS_DIAG);
            deltasByPosAndSize[0][MEDIUM] = new Point(OFFSET_X_MEDIUM - MEDIUM_RADIUS_PIXELS_DIAG, OFFSET_Y_MEDIUM - MEDIUM_RADIUS_PIXELS_DIAG);
            deltasByPosAndSize[0][SMALL] = new Point(OFFSET_X_SMALL - SMALL_RADIUS_PIXELS_DIAG, OFFSET_Y_SMALL - SMALL_RADIUS_PIXELS_DIAG);
            deltasByPosAndSize[1][LARGE] = new Point(OFFSET_X_LARGE, OFFSET_Y_LARGE - LARGE_RADIUS_PIXELS);
            deltasByPosAndSize[1][MEDIUM] = new Point(OFFSET_X_MEDIUM, OFFSET_Y_MEDIUM - MEDIUM_RADIUS_PIXELS);
            deltasByPosAndSize[1][SMALL] = new Point(OFFSET_X_SMALL, OFFSET_Y_SMALL - SMALL_RADIUS_PIXELS);
            deltasByPosAndSize[2][LARGE] = new Point(OFFSET_X_LARGE + LARGE_RADIUS_PIXELS_DIAG, OFFSET_Y_LARGE - LARGE_RADIUS_PIXELS_DIAG);
            deltasByPosAndSize[2][MEDIUM] = new Point(OFFSET_X_MEDIUM + MEDIUM_RADIUS_PIXELS_DIAG, OFFSET_Y_MEDIUM - MEDIUM_RADIUS_PIXELS_DIAG);
            deltasByPosAndSize[2][SMALL] = new Point(OFFSET_X_SMALL + SMALL_RADIUS_PIXELS_DIAG, OFFSET_Y_SMALL - SMALL_RADIUS_PIXELS_DIAG);
        }
        else if (x == 0) {
            // LEFT
            deltasByPosAndSize[0][LARGE] = new Point(OFFSET_X_LARGE + LARGE_RADIUS_PIXELS_DIAG, OFFSET_Y_LARGE - LARGE_RADIUS_PIXELS_DIAG);
            deltasByPosAndSize[0][MEDIUM] = new Point(OFFSET_X_MEDIUM + MEDIUM_RADIUS_PIXELS_DIAG, OFFSET_Y_MEDIUM - MEDIUM_RADIUS_PIXELS_DIAG);
            deltasByPosAndSize[0][SMALL] = new Point(OFFSET_X_SMALL + SMALL_RADIUS_PIXELS_DIAG, OFFSET_Y_SMALL - SMALL_RADIUS_PIXELS_DIAG);
            deltasByPosAndSize[1][LARGE] = new Point(OFFSET_X_LARGE + LARGE_RADIUS_PIXELS, OFFSET_Y_LARGE);
            deltasByPosAndSize[1][MEDIUM] = new Point(OFFSET_X_MEDIUM + MEDIUM_RADIUS_PIXELS, OFFSET_Y_MEDIUM);
            deltasByPosAndSize[1][SMALL] = new Point(OFFSET_X_SMALL + SMALL_RADIUS_PIXELS, OFFSET_Y_SMALL);
            deltasByPosAndSize[2][LARGE] = new Point(OFFSET_X_LARGE + LARGE_RADIUS_PIXELS_DIAG, OFFSET_Y_LARGE + LARGE_RADIUS_PIXELS_DIAG);
            deltasByPosAndSize[2][MEDIUM] = new Point(OFFSET_X_MEDIUM + MEDIUM_RADIUS_PIXELS_DIAG, OFFSET_Y_MEDIUM + MEDIUM_RADIUS_PIXELS_DIAG);
            deltasByPosAndSize[2][SMALL] = new Point(OFFSET_X_SMALL + SMALL_RADIUS_PIXELS_DIAG, OFFSET_Y_SMALL + SMALL_RADIUS_PIXELS_DIAG);
        }
        else {
            // RIGHT
            deltasByPosAndSize[0][LARGE] = new Point(OFFSET_X_LARGE - LARGE_RADIUS_PIXELS_DIAG, OFFSET_Y_LARGE - LARGE_RADIUS_PIXELS_DIAG);
            deltasByPosAndSize[0][MEDIUM] = new Point(OFFSET_X_MEDIUM - MEDIUM_RADIUS_PIXELS_DIAG, OFFSET_Y_MEDIUM - MEDIUM_RADIUS_PIXELS_DIAG);
            deltasByPosAndSize[0][SMALL] = new Point(OFFSET_X_SMALL - SMALL_RADIUS_PIXELS_DIAG, OFFSET_Y_SMALL - SMALL_RADIUS_PIXELS_DIAG);
            deltasByPosAndSize[1][LARGE] = new Point(OFFSET_X_LARGE - LARGE_RADIUS_PIXELS, OFFSET_Y_LARGE);
            deltasByPosAndSize[1][MEDIUM] = new Point(OFFSET_X_MEDIUM - MEDIUM_RADIUS_PIXELS, OFFSET_Y_MEDIUM);
            deltasByPosAndSize[1][SMALL] = new Point(OFFSET_X_SMALL - SMALL_RADIUS_PIXELS, OFFSET_Y_SMALL);
            deltasByPosAndSize[2][LARGE] = new Point(OFFSET_X_LARGE - LARGE_RADIUS_PIXELS_DIAG, OFFSET_Y_LARGE + LARGE_RADIUS_PIXELS_DIAG);
            deltasByPosAndSize[2][MEDIUM] = new Point(OFFSET_X_MEDIUM - MEDIUM_RADIUS_PIXELS_DIAG, OFFSET_Y_MEDIUM + MEDIUM_RADIUS_PIXELS_DIAG);
            deltasByPosAndSize[2][SMALL] = new Point(OFFSET_X_SMALL - SMALL_RADIUS_PIXELS_DIAG, OFFSET_Y_SMALL + SMALL_RADIUS_PIXELS_DIAG);
        }
    }

    // Util for other Windows

    public static void positionFrameNextToStarWindow(JFrame frame) {
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final Point starLocation = getSavedCenterLocation();
        Point location;
        if (starLocation.y == 0) {
            // Top
            location = new Point(starLocation.x - (frame.getWidth() / 2), STAR_HEIGHT_PIXELS / 2);
        }
        else if (starLocation.x == 0) {
            // Left
            location = new Point(STAR_WIDTH_PIXELS / 2, starLocation.y - (frame.getHeight() / 2));
        }
        else if (starLocation.x == screenSize.width) {
            // Right
            location = new Point(screenSize.width - STAR_WIDTH_PIXELS / 2 - frame.getWidth(), starLocation.y - (frame.getHeight() / 2));
        }
        else {
            // Bottom
            location = new Point(starLocation.x - (frame.getWidth() / 2), screenSize.height - STAR_HEIGHT_PIXELS / 2 - frame.getHeight());
        }

        frame.setLocation(Math.min(Math.max(location.x, STAR_WIDTH_PIXELS / 2), screenSize.width - frame.getWidth() - STAR_WIDTH_PIXELS / 2),
                Math.min(Math.max(location.y, STAR_HEIGHT_PIXELS / 2), screenSize.height - frame.getHeight() - STAR_HEIGHT_PIXELS / 2));
    }


    ////////////////////////////
    // EVENT HANDLERS

    private void onCapture() {
        // Hide star icon during the capture
        setVisible(false);
        // Creating the capture selection window will cause the screenshot to happen
        CaptureSelectionFrame frame = new CaptureSelectionFrame(this);
        // Show star icon again
        setVisible(true);
        // And show capture selection window
        frame.setVisible(true);
    }


    private void onHistory() {
        if (historyFrame == null) {
            historyFrame = new HistoryFrame(this);
        }
        historyFrame.setVisible(true);
        historyFrame.requestFocus();
    }


    private void onMore() {
        if (moreFrame == null) {
            moreFrame = new MoreFrame(this);
        }
        moreFrame.setVisible(true);
        moreFrame.requestFocus();
    }


    public void addTargetChangeListener(TargetListChangeListener listener) {
        targetListChangeListener.add(listener);
    }

    public void removeTargetChangeListener(TargetListChangeListener listener) {
        targetListChangeListener.add(listener);
    }

    public void notifyTargetListChange() {
        for (TargetListChangeListener targetListChangeListener : targetListChangeListener) {
            targetListChangeListener.onTargetListChange();
        }
    }

//
//    private void showSplashIntro() {
//        final Point startCenter = new Point(screenSize.width / 2, screenSize.height / 2);
//        final Point endCenter = getSavedCenterLocation();
//        Point currentCenter = new Point();
//        try {
//            AnimationJWindow window = new AnimationJWindow();
//            window.setBackground(new Color(0, 0, 0, 0));
//            window.setSize(SPLASH_SIZE);
//            window.setVisible(true);
//
//            final int[] progress = {0};
////            javax.swing.Timer timer = new javax.swing.Timer(100, arg0 -> {
////                // Linear progression
////                currentCenter.x = ((100 - progress[0]) * startCenter.x + progress[0] * endCenter.x) / 100;
////                currentCenter.y = ((100 - progress[0]) * startCenter.y + progress[0] * endCenter.y) / 100;
////                window.setProgress(progress[0]);
////                window.setLocation(currentCenter.x - (window.currentWidth / 2), currentCenter.y - (window.currentHeight / 2));
////                window.repaint();
////                if (progress[0] < 100) progress[0]++;
////            });
////            timer.start();
//
//            while (progress[0] < 100) {
//                Thread.sleep(10);
//            }
//            //timer.stop();
//        }
//        catch (Exception e){
//            e.printStackTrace();
//            // In all cases, ignore exceptions and skip animation
//        }
//    }
//
//    private static class AnimationJWindow extends JWindow {
//        private final BufferedImage image;
//        private int progress;
//        private int currentWidth;
//        private int currentHeight;
//
//        public AnimationJWindow() throws IOException {
//            image = ImageIO.read(getClass().getResource("/img/app-logo.png"));
//        }
//
//        public void setProgress(int progress) {
//            this.progress = progress;
//        }
//
//        public int getCurrentWidth() {
//            return currentWidth;
//        }
//
//        public int getCurrentHeight() {
//            return currentHeight;
//        }
//
//        @Override
//        public void paintComponents(Graphics g) {
//            super.paintComponents(g);
//            currentWidth = ((100 - progress) * image.getWidth() + progress * CIRCLE_WIDTH_PIXELS) / 100;
//            currentHeight = ((100 - progress) * image.getHeight() + progress * CIRCLE_HEIGHT_PIXELS) / 100;
//            // Always draw at (0,0)
//            g.drawImage(image, 0, 0, currentWidth, currentHeight, null);
//        }
//    }
}