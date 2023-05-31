package net.jandie1505;

public interface GamePart {
    boolean tick();

    GamePart getNextStatus();
}
