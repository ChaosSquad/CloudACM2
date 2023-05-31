package net.jandie1505.cloudacm2;

public interface GamePart {
    boolean tick();

    GamePart getNextStatus();
}
