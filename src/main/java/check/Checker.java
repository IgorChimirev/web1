package check;

public class Checker {
    public boolean hit(float x, float y, float r) {
        return inRect(x, y, r) || inTriangle(x, y, r) || inCircle(x, y, r);
    }

    private boolean inRect(float x, float y, float r) {
        return x <= 0 && y <= 0 && x >= -r && y >= -((float) r) / 2;
    }

    private boolean inTriangle(float x, float y, float r) {
        return (x >= -r/2) && (y <= r) && (2 * x - y >= -r) && x <= 0 && y >= 0;
    }

    private boolean inCircle(float x, float y, float r) {
        return (x * x + y * y) <= (float) (r * r) / 4 && x >= 0 && y <= 0;
    }
}