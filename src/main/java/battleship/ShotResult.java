package battleship;

public class ShotResult {
    // Apenas campos públicos para facilitar o acesso sem precisar de getters complexos
    public boolean isValid;
    public boolean isRepeated;
    public IShip ship;
    public boolean isSunk;

    public ShotResult(boolean isValid, boolean isRepeated, IShip ship, boolean isSunk) {
        this.isValid = isValid;
        this.isRepeated = isRepeated;
        this.ship = ship;
        this.isSunk = isSunk;
    }
}