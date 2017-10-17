package ir.flutter.androidorangepigpio.enums;

/**
 * Created by m.beiranvand on 2017-10-17.
 */

public enum EnPinDirection {

    out("out"),
    in("in");

    private final String dir;

    EnPinDirection(String dir) {
        this.dir = dir;
    }

    public String getDir() {
        return dir;
    }
}
