package com.cbhb.util;

import java.io.File;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Bootstrap {

    public static void main(String[] args) {

        LocalDate localDate = LocalDate.now();
        LocalDate minusDays = localDate.minusDays(89);
        System.out.println(minusDays.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
    }
}
