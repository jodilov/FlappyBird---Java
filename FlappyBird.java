import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import javax.sound.sampled.*;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    // Constants
    final int BOARD_WIDTH = 360;
    final int BOARD_HEIGHT = 640;
    final int BIRD_WIDTH = 34;
    final int BIRD_HEIGHT = 24;
    final int PIPE_WIDTH = 64;
    final int PIPE_HEIGHT = 512;
    final int GRAVITY = 1;
    final int JUMP_STRENGTH = -10;

    // Images
    static Image backgroundImg = new ImageIcon(FlappyBird.class.getResource("/flappybirdbg.png")).getImage();
    static Image birdImg = new ImageIcon(FlappyBird.class.getResource("/flappybird.png")).getImage();
    static Image topPipeImg = new ImageIcon(FlappyBird.class.getResource("/toppipe.png")).getImage();
    static Image bottomPipeImg = new ImageIcon(FlappyBird.class.getResource("/bottompipe.png")).getImage();

    // Bird Class
    class Bird {
        int x, y, width, height;
        Image img;

        Bird(int x, int y, int width, int height, Image img) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.img = img;
        }
    }

    // Pipe Class
    class Pipe {
        int x, y, width, height;
        Image img;
        boolean passed = false;

        Pipe(int x, int y, int width, int height, Image img) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.img = img;
        }
    }

    // Game variables
    Bird bird;
    ArrayList<Pipe> pipes;
    Random random = new Random();
    Timer gameLoop, placePipeTimer;
    boolean gameOver = false, paused = false;
    int velocityY = 0;
    double score = 0;

    FlappyBird() {
        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        setFocusable(true);
        addKeyListener(this);

        // Initialize game objects
        bird = new Bird(BOARD_WIDTH / 8, BOARD_HEIGHT / 2, BIRD_WIDTH, BIRD_HEIGHT, birdImg);
        pipes = new ArrayList<>();

        // Place pipes periodically
        placePipeTimer = new Timer(1500, e -> placePipes());
        placePipeTimer.start();

        // Game loop
        gameLoop = new Timer(1000 / 60, this);
        gameLoop.start();
    }

    void placePipes() {
        int randomPipeY = -PIPE_HEIGHT / 4 - random.nextInt(PIPE_HEIGHT / 2);
        int openingSpace = BOARD_HEIGHT / 4;

        pipes.add(new Pipe(BOARD_WIDTH, randomPipeY, PIPE_WIDTH, PIPE_HEIGHT, topPipeImg));
        pipes.add(new Pipe(BOARD_WIDTH, randomPipeY + PIPE_HEIGHT + openingSpace, PIPE_WIDTH, PIPE_HEIGHT, bottomPipeImg));
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImg, 0, 0, BOARD_WIDTH, BOARD_HEIGHT, null);
        g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);

        for (Pipe pipe : pipes) {
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        // Score Display
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.setColor(Color.BLACK);
        g.drawString(String.valueOf((int) score), BOARD_WIDTH / 2 - 20, 50);
        g.setColor(Color.WHITE);
        g.drawString(String.valueOf((int) score), BOARD_WIDTH / 2 - 22, 48);
    }

    public void move() {
        if (paused || gameOver) return;
        velocityY += GRAVITY;
        bird.y = Math.max(bird.y + velocityY, 0);

        pipes.removeIf(pipe -> pipe.x + pipe.width < 0);

        for (Pipe pipe : pipes) {
            pipe.x -= 4;
            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                score += 0.5;
                pipe.passed = true;
            }
            if (collision(bird, pipe)) {
                gameOver = true;
                endGame();
            }
        }

        if (bird.y > BOARD_HEIGHT) {
            gameOver = true;
            endGame();
        }
    }

    void endGame() {
        Timer stopGameTimer = new Timer(1000, e -> {
            placePipeTimer.stop();
            gameLoop.stop();
            System.out.println("Game Over! Final Score: " + (int) score);
        });
        stopGameTimer.setRepeats(false);
        stopGameTimer.start();
    }

    boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width && a.x + a.width > b.x && a.y < b.y + b.height && a.y + a.height > b.y;
    }
    void restartGame() {
        bird.y = BOARD_HEIGHT / 2;
        velocityY = 0;
        pipes.clear();
        gameOver = false;
        score = 0;
        gameLoop.start();
        placePipeTimer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (gameOver) {
                restartGame();
            } else {
                velocityY = JUMP_STRENGTH;
                playSound("jump.wav");
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_P) {
            paused = !paused;
            if (paused) {
                gameLoop.stop();
                placePipeTimer.stop();
            } else {
                gameLoop.start();
                placePipeTimer.start();
            }
        }
    }

    void playSound(String soundFile) {
        try {
            AudioInputStream audio = AudioSystem.getAudioInputStream(new File(soundFile));
            Clip clip = AudioSystem.getClip();
            clip.open(audio);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void keyReleased(KeyEvent e) {}
}

