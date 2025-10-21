package validation;

import java.util.Arrays;
import java.util.List;

public class Validate {
    private final List<Float> xRange = Arrays.asList(-2.0f, -1.5f, -1.0f, -0.5f, 0.0f, 0.5f, 1.0f, 1.5f, 2.0f);
    private String log = "all ok";

    public boolean check(Float x, Float y, Float r) {
        if (x == null || y == null || r == null) {
            log = "Input values must not be null";
            return false;
        }
        return checkX(x) && checkY(y) && checkR(r);
    }

    public String getErr() {
        return log;
    }

    public boolean checkX(float x) {
        if (xRange.contains(x)) {
            return true;
        } else {
            log = "X must be selected";
            return false;
        }
    }

    public boolean checkY(float y) {
        if (y > -3.0 && y < 5.0) {
            return true;
        } else {
            log = "Y value must be -3<y<5";
            return false;
        }
    }

    public boolean checkR(float r) {
        if (r > 2.0 && r < 5.0) {
            return true;
        } else {
            log = "R value must be 2<r<5";
            return false;
        }
    }
}