package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.sql.Timestamp;
import java.util.Random;

public class MovingImage extends JPanel implements Runnable{

    private Thread thread;
    BufferedImage img;
    int[] resolution;
    int deltaTime;
    int deathBehavior;

    Random r = new Random();
    int initY, finalY, initX, leftOrRight, speed, currentX, currentY;
    float alpha;
    double deltaY;

    public static final int NEVER_DIE = 0;
    public static final int DIE_WHEN_OFF_SCREEN = 1;
    public static final int DIE_WHEN_OFF_SCREEN_FOR_MORE_THAN_120_FRAMES = 2;

    public static final int RANDOM_DIRECTION = 0;
    public static final int LEFT_TO_RIGHT = 1;
    public static final int RIGHT_TO_LEFT = 2;

    public MovingImage(BufferedImage img, int[] resolution, int speed, Color tint, float alpha, int deathBehavior, int direction) {
        this.resolution = resolution;
        this.speed = speed;
        this.alpha = alpha;
        this.deathBehavior = deathBehavior;
        deltaTime = 0;

        if (tint != null) {
            this.img = tintImage(img, tint, false);
        } else {
            this.img = img;
        }

        setSize(resolution[0], resolution[1]);
        setBackground(new Color(0, 0, 0, 0));
        setOpaque(false);

        makeRandomFlyBy(speed, direction);
        start();
    }

    public void setDeathBehavior(int deathBehavior) {
        this.deathBehavior = deathBehavior;
    }

    public boolean checkDeathCondition() {
        if (deathBehavior == NEVER_DIE) {
            return false;
        } else if (deathBehavior == DIE_WHEN_OFF_SCREEN || deathBehavior == DIE_WHEN_OFF_SCREEN_FOR_MORE_THAN_120_FRAMES) {
            Rectangle imgBounds = new Rectangle(currentX, currentY, img.getWidth(), img.getHeight());
            Rectangle panelBounds = new Rectangle(0, 0, getWidth(), getHeight());
            if (imgBounds.intersects(panelBounds) || deltaTime < 120) { //keeps it from dying before it goes on screen
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    public void makeRandomFlyBy (int speed, int direction) {
        if (direction == LEFT_TO_RIGHT) {
            leftOrRight = 0;
        } else if (direction == RIGHT_TO_LEFT) {
            leftOrRight = 1;
        } else {
            leftOrRight = r.nextInt(2);
        }
        initY = r.nextInt(resolution[1]);
        if (leftOrRight == 0) { //starting left
            initX = -200;
            System.out.println("left");
        } else { //starting right
            System.out.println("right");
            initX = resolution[0] + 200;
        }
        finalY = r.nextInt(resolution[1]);
        deltaY = -((double) (initY - finalY) / (resolution[0] / speed));

        System.out.println("initY: " + initY + " | finalY: " + finalY + " | deltaY: " + deltaY);
    }

    public BufferedImage tintImage(BufferedImage img, Color color, boolean tintTransparent) {

        int r2 = color.getRed();
        int g2 = color.getGreen();
        int b2 = color.getBlue();

        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                Color initialColor = new Color(img.getRGB(x, y));
                if (!isTransparent(img, x, y) || tintTransparent) {

                    int r1 = initialColor.getRed();
                    int g1 = initialColor.getGreen();
                    int b1 = initialColor.getBlue();

                    img.setRGB(x, y, new Color((r1 + r2) / 2, (g1 + g2) / 2, (b1 + b2) / 2).getRGB()); //averages colors
                }
            }
        }

        return img;
    }

    public boolean isTransparent(BufferedImage img, int x, int y) {
        int pixel = img.getRGB(x, y);
        if ((pixel >> 24) == 0x00) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        if (leftOrRight == 0) { //starting left
            g2d.drawImage(img, initX + (speed * deltaTime), initY + (int)(deltaY * deltaTime), null); //TODO maybe replace this with one that cumulatively adds to current x and y
            currentX = initX + (speed * deltaTime);
        } else {
            g2d.drawImage(img, initX - (speed * deltaTime), initY + (int)(deltaY * deltaTime), null);
            currentX = initX - (speed * deltaTime);
        }
        currentY = initY + (int)(deltaY * deltaTime);
    }

    @Override
    public void run() {
        while(true) {
            deltaTime++;
            if (checkDeathCondition()) {
                invalidate();
                setVisible(false);
                setEnabled(false);
                //System.out.println("invalidated");
                //System.gc();
                return;
            }
            try {
                Thread.sleep(1000/60);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public void start () {
        System.out.println(new Timestamp(System.currentTimeMillis()) + " Starting " +  this.getClass().getName() + "!");
        if (thread == null) {
            thread = new Thread (this, "MovingImageRunnable");
            thread.start();
        }
    }
}
