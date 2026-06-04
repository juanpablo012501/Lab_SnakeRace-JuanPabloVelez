package co.eci.snake.concurrency;

import co.eci.snake.core.Board;
import co.eci.snake.core.Position;

import java.util.Map;
import java.util.Set;

public class BoardSnapshot {

    private final Set<Position> mice;
    private final Set<Position> obstacles;
    private final Set<Position> turbo;
    private final Map<Position, Position> teleports;

    public BoardSnapshot(Set<Position> mice, Set<Position> obstacles, Set<Position> turbo, Map<Position, Position> teleports) {
        this.mice = mice;
        this.obstacles = obstacles;
        this.turbo = turbo;
        this.teleports = teleports;
    }

    public Set<Position> getMice() {
        return mice;
    }

    public Set<Position> getObstacles() {
        return obstacles;
    }

    public Set<Position> getTurbo() {
        return turbo;
    }
    public Map<Position, Position> getTeleports() {
        return teleports;
    }
}