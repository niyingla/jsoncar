package com.github.demo;

/**
 * @author shuang.kou
 **/
public class TestConfig {
    private TestConfig() {
    }

    public static final String host = "http://localhost:8080";

    public static void main(String[] args) {

        String xcfx1D0C4X12DF1X20 = new String("XCFX1D0C4X12DF1X20");
        System.out.println(xcfx1D0C4X12DF1X20.hashCode());
        System.out.println((xcfx1D0C4X12DF1X20 + 45352413).hashCode());
    }
}

