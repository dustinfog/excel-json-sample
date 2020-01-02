package com.kingsgroup.zday;

public class Head {
    private String name;
    private String shortName;

    String getName() {
        return name;
    }

    String getShortName() {
        return shortName;
    }

    Head(String name) {
        this.name = name;

        int indexSep = name.indexOf('@');
        if (indexSep == -1) {
            indexSep = name.indexOf('^');
        }

        if (indexSep == -1) {
            shortName = name;
        } else {
            shortName = name.substring(0, indexSep);
        }
    }
}