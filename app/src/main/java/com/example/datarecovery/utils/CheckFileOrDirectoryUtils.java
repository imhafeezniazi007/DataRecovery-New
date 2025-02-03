package com.example.datarecovery.utils;

import java.io.File;

public class CheckFileOrDirectoryUtils {
    public static File[] getFileList(String str) {
        File file = new File(str);
        if (!file.isDirectory()) {
            return new File[0];
        }

        if (file.listFiles() != null) {

            return file.listFiles();
        } else return new File[0];

    }
}