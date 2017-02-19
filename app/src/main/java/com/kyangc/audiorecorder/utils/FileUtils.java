package com.kyangc.audiorecorder.utils;

import android.text.TextUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Usage: File utils for handling IO.
 *
 * Created by chengkangyang on 2017/2/18.
 */
public class FileUtils {

    public static OutputStream outputStream(File file) {
        if (file == null) throw new RuntimeException("file is null !");
        OutputStream outputStream;
        try {
            outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("could not build OutputStream from" +
                    " this file" + file.getName(), e);
        }
        return outputStream;
    }

    public static List<File> listFiles(File dir, String extention, boolean isIterate) {
        List<File> files = new ArrayList<>();
        if (dir == null || TextUtils.isEmpty(extention) || dir.isFile()) return files;
        File[] filesArray = dir.listFiles();
        for (File file : filesArray) {
            if (file.isFile()) {
                if (TextUtils.equals(getFileExtention(file), extention)) {
                    files.add(file);
                }
            } else {
                if (isIterate) files.addAll(listFiles(file, extention, isIterate));
            }
        }
        return files;
    }

    public static String getFileName(File file) {
        if (file == null) return null;
        String path = file.getPath();
        if (TextUtils.isEmpty(path)) return null;
        int dot = path.lastIndexOf('.');
        int sep = path.lastIndexOf('/');
        if ((dot > -1) && (dot < (path.length() - 1))) {
            return path.substring(sep + 1, dot);
        }
        return null;
    }

    public static String getFileExtention(File file) {
        if (file == null) return null;
        String path = file.getPath();
        if (TextUtils.isEmpty(path)) return null;
        int dot = path.lastIndexOf('.');
        if ((dot > -1) && (dot < (path.length() - 1))) {
            return path.substring(dot + 1);
        }
        return null;
    }
}
