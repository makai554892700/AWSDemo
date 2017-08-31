package com.mayousheng.www.utils;

import java.io.File;

public class PathUtils {

    private static final String BASE_PATH_LINUX = "/";
    private static final String BASE_PATH_WIN = "c:/";

    public static String getBasePath() {
        String result = getExistsPath(BASE_PATH_LINUX);
        if (result == null) {
            result = getExistsPath(BASE_PATH_WIN);
        }
        return result;
    }

    private static String getExistsPath(String path) {
        File file = new File(path);
        if (file.exists()) {
            return path;
        } else {
            return null;
        }
    }
}
