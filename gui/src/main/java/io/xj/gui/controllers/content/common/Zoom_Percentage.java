package io.xj.gui.controllers.content.common;

public enum Zoom_Percentage {
    PERCENT_5(.05),
    PERCENT_10(.10),
    PERCENT_25(.25),
    PERCENT_50(.5),
    PERCENT_100(1),
    PERCENT_200(2),
    PERCENT_300(3),
    PERCENT_400(4);

    private final double value;

    Zoom_Percentage(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }
}
