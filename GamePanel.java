/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.snake;

import java.util.Arrays;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Random;
import java.util.HashSet;
import java.util.Set;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class GamePanel extends JPanel implements ActionListener {

    // Screen settings
    static final int SCREEN_WIDTH = 600;
    static final int SCREEN_HEIGHT = 600;
    static final int UNIT_SIZE = 25;
    static final int BORDER_SIZE = UNIT_SIZE;  // Border width, same as unit size
    static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / UNIT_SIZE;
    static int DELAY = 75;

    // Snake coordinates
    final int x[] = new int[GAME_UNITS];
    final int y[] = new int[GAME_UNITS];
    int bodyParts = 6;
    int applesEaten = 0;

    // Apple coordinates and type
    int appleX;
    int appleY;
    AppleType currentAppleType;

    // Movement
    char direction = 'R';
    boolean running = false;
    Timer timer;
    Random random;

    // Apple probabilities
    private static final double GOLD_APPLE_PROB = 0.05;     // 5%
    private static final double RAINBOW_APPLE_PROB = 0.10;  // 10%
    private static final double NORMAL_APPLE_PROB = 0.85;   // 89%

    // Rainbow mode variables
    boolean isRainbowSnake = false;
    long rainbowEndTime = 0;

    // Try Again button
    JButton tryAgainButton;

    // Music variables
    Clip musicClip;
    boolean isMusicPlaying = false;

    // Enum for Apple Types
    enum AppleType {
        NORMAL, RAINBOW, GOLD
    }

    GamePanel() {
        random = new Random();
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.black);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        startGame();
    }

    public void startGame() {
        newApple();
        running = true;
        DELAY = 75;  // Reset speed when the game restarts
        timer = new Timer(DELAY, this);
        timer.start();
        isRainbowSnake = false; // Reset rainbow mode on restart
        tryAgainButton = null;  // Remove "Try Again" button if exists
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        if (running) {
            // Draw yellow borders
            g.setColor(Color.yellow);
            // Top border
            g.fillRect(0, 0, SCREEN_WIDTH, BORDER_SIZE);
            // Bottom border
            g.fillRect(0, SCREEN_HEIGHT - BORDER_SIZE, SCREEN_WIDTH, BORDER_SIZE);
            // Left border
            g.fillRect(0, 0, BORDER_SIZE, SCREEN_HEIGHT);
            // Right border
            g.fillRect(SCREEN_WIDTH - BORDER_SIZE, 0, BORDER_SIZE, SCREEN_HEIGHT);

            // Draw the apple
            switch (currentAppleType) {
                case NORMAL:
                    g.setColor(Color.red);
                    break;
                case RAINBOW:
                    // Rapidly changing colors for the apple
                    g.setColor(new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
                    break;
                case GOLD:
                    g.setColor(new Color(212, 175, 55)); // Gold color
                    break;
            }
            g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);

            // Draw the snake
            for (int i = 0; i < bodyParts; i++) {
                if (i == 0) {
                    // Snake head
                    g.setColor(Color.green);
                    g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
                } else {
                    if (isRainbowSnake) {
                        // Rainbow snake body
                        g.setColor(new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
                    } else {
                        g.setColor(new Color(45, 180, 0));
                    }
                    g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
                }
            }

            // Display score
            g.setColor(Color.red);
            g.setFont(new Font("Ink Free", Font.BOLD, 25));
            FontMetrics metrics = getFontMetrics(g.getFont());
            g.drawString("SCORE: " + applesEaten, (SCREEN_WIDTH - metrics.stringWidth("SCORE: " + applesEaten)) / 2, g.getFont().getSize());
        } else {
            gameOver(g);
        }
    }

    // Generate a new apple ensuring it doesn't spawn on the snake or in the borders
    public void newApple() {
        Set<Point> snakePoints = new HashSet<>();
        for (int i = 0; i < bodyParts; i++) {
            snakePoints.add(new Point(x[i], y[i]));
        }

        boolean validPosition = false;
        while (!validPosition) {
            // Ensure the apple spawns within the playable area (not inside borders)
            appleX = random.nextInt((SCREEN_WIDTH - 2 * BORDER_SIZE) / UNIT_SIZE) * UNIT_SIZE + BORDER_SIZE;
            appleY = random.nextInt((SCREEN_HEIGHT - 2 * BORDER_SIZE) / UNIT_SIZE) * UNIT_SIZE + BORDER_SIZE;

            Point newApple = new Point(appleX, appleY);
            if (!snakePoints.contains(newApple)) {
                validPosition = true;
            }
        }

        // Determine apple type based on probabilities
        double rand = random.nextDouble();
        if (rand < GOLD_APPLE_PROB) {
            currentAppleType = AppleType.GOLD;
        } else if (rand < GOLD_APPLE_PROB + RAINBOW_APPLE_PROB) {
            currentAppleType = AppleType.RAINBOW;
        } else {
            currentAppleType = AppleType.NORMAL;
        }
    }

    public void move() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        // Move snake head based on direction, but stop at borders
        switch (direction) {
            case 'U':
                if (y[0] - UNIT_SIZE >= BORDER_SIZE) {
                    y[0] = y[0] - UNIT_SIZE;
                }
                break;
            case 'D':
                if (y[0] + UNIT_SIZE < SCREEN_HEIGHT - BORDER_SIZE) {
                    y[0] = y[0] + UNIT_SIZE;
                }
                break;
            case 'L':
                if (x[0] - UNIT_SIZE >= BORDER_SIZE) {
                    x[0] = x[0] - UNIT_SIZE;
                }
                break;
            case 'R':
                if (x[0] + UNIT_SIZE < SCREEN_WIDTH - BORDER_SIZE) {
                    x[0] = x[0] + UNIT_SIZE;
                }
                break;
        }
    }

    public void checkApple() {
        if ((x[0] == appleX) && (y[0] == appleY)) {
            switch (currentAppleType) {
                case NORMAL:
                    bodyParts++;
                    applesEaten += 1;
                    break;
                case RAINBOW:
                    bodyParts++;
                    applesEaten += 2;
                    activateRainbowMode();
                    break;
                case GOLD:
                    bodyParts++;
                    applesEaten += 5;
                    break;
            }
            newApple();
        }
    }

    private void activateRainbowMode() {
        isRainbowSnake = true;
        rainbowEndTime = System.currentTimeMillis() + 5000;  // Rainbow lasts for 5 seconds
        
    }

    public void checkCollisions() {
        // Check if head collides with body
        for (int i = bodyParts; i > 0; i--) {
            if ((x[0] == x[i]) && (y[0] == y[i])) {
                running = false;
            }
        }

        if (!running) {
            timer.stop();
        }
    }

    public void gameOver(Graphics g) {

        // Display Game Over text
        g.setColor(Color.blue);
        g.setFont(new Font("Ink Free", Font.BOLD, 75));
        FontMetrics metrics1 = getFontMetrics(g.getFont());
        g.drawString("Game Over", (SCREEN_WIDTH - metrics1.stringWidth("Game Over")) / 2, SCREEN_HEIGHT / 2);

        // Display Score
        g.setFont(new Font("Ink Free", Font.BOLD, 40));
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        g.drawString("SCORE: " + applesEaten, (SCREEN_WIDTH - metrics2.stringWidth("SCORE: " + applesEaten)) / 2, g.getFont().getSize());

        // Create and display "Try Again" button
        if (tryAgainButton == null) {
            tryAgainButton = new JButton("Try Again");
            tryAgainButton.setFont(new Font("Ink Free", Font.BOLD, 40));
            tryAgainButton.setBounds((SCREEN_WIDTH - 200) / 2, (SCREEN_HEIGHT - 50) / 2 + 100, 200, 50);
            tryAgainButton.setFocusable(false);
            tryAgainButton.addActionListener(e -> restartGame());
            this.setLayout(null);  // Set layout to null for absolute positioning
            this.add(tryAgainButton);
            this.revalidate();
            this.repaint();
        }
    }

    public void restartGame() {
        // Remove the "Try Again" button
        this.remove(tryAgainButton);
        tryAgainButton = null;

        // Reset the game state
        applesEaten = 0;
        bodyParts = 6;
        direction = 'R';
        Arrays.fill(x, 0);
        Arrays.fill(y, 0);
        startGame();
        this.revalidate();
        this.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkApple();
            checkCollisions();

            // Disable rainbow mode if the time is up
            if (isRainbowSnake && System.currentTimeMillis() > rainbowEndTime) {
                isRainbowSnake = false;
            }
        }
        repaint();
    }

    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    if (direction != 'R') {
                        direction = 'L';
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                    if (direction != 'L') {
                        direction = 'R';
                    }
                    break;
                case KeyEvent.VK_UP:
                    if (direction != 'D') {
                        direction = 'U';
                    }
                    break;
                case KeyEvent.VK_DOWN:
                    if (direction != 'U') {
                        direction = 'D';
                    }
                    break;
            }
        }
    }
} 