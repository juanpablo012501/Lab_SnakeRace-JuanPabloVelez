package co.eci.snake.concurrency;

public final class GameMonitor {
    private boolean paused = false;

    public GameMonitor() {}

    public synchronized void checkPaused() throws InterruptedException {
        while (paused) wait();
    }

    public synchronized void pause() {
        paused = true;
    }

    public synchronized void resume() {
        paused = false;
        notifyAll();
    }
}
