package com.mayousheng.www.utils;

import java.io.*;

public class FileUtils {

    public interface LineBack {
        public void onStart(String fileName);

        public void onLine(String line);

        public void onEnd(String fileName);
    }

    public static File sureDir(String dir) {
        if (dir == null) {
            return null;
        }
        File tempFile = new File(dir);
        if (!tempFile.exists()) {
            tempFile.mkdir();
        }
        return tempFile;
    }

    public static File sureFile(String filePath) {
        if (filePath == null) {
            return null;
        }
        File tempFile = new File(filePath);
        if (!tempFile.exists()) {
            try {
                tempFile.createNewFile();
            } catch (Exception e) {
                return null;
            }
        }
        return tempFile;
    }

    public static File sureFileIsNew(String filePath) {
        if (filePath != null && !filePath.isEmpty()) {
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
            try {
                file.createNewFile();
            } catch (Exception e) {
                System.out.println("e=" + e);
                return null;
            }
            return file;
        }
        return null;
    }

    public static boolean strToFile(File file, StringBuilder data) {
        if (data == null) {
            return false;
        }
        AppendFileUtils appendFileUtils = AppendFileUtils.getInstance(file);
        final int len = 102400;
        while (data.length() > 0) {
            int tempLen;
            if (data.length() >= len) {
                tempLen = len;
            } else {
                tempLen = data.length();
            }
            appendFileUtils.appendString(data.substring(0, tempLen));
            data.delete(0, tempLen);
        }
        appendFileUtils.endAppendFile();
        return true;
    }

    public static boolean byte2File(File file, byte[] data) {
        if (data == null || file == null) {
            return false;
        }
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(data);
        } catch (Exception e) {
            return false;
        } finally {
            closeSilently(fileOutputStream);
        }
        return true;
    }

    public static boolean inputStream2File(InputStream inputStream, File file) {
        boolean result = false;
        if (inputStream == null || file == null || !file.exists()) {
            System.out.println("message is error.");
        } else {
            OutputStream outputStream;
            try {
                outputStream = new FileOutputStream(file);
            } catch (Exception e) {
                System.out.println("e=" + e);
                return false;
            }
            result = inputStream2OutputStream(inputStream, outputStream);
            closeSilently(outputStream);
        }
        return result;
    }

    private static boolean inputStream2OutputStream(InputStream inputStream, OutputStream outputStream) {
        if (inputStream != null && outputStream != null) {
            byte[] tempByte = new byte[1024];
            int len;
            try {
                while ((len = inputStream.read(tempByte)) != -1) {
                    outputStream.write(tempByte, 0, len);
                }
                return true;
            } catch (Exception e) {
                System.out.println("e=" + 3);
            }
        }
        return false;
    }

    public static boolean readLine(String filePath, LineBack lineBack) {
        boolean result = false;
        if (lineBack != null && filePath != null && !filePath.isEmpty()) {
            lineBack.onStart(filePath);
            File file = new File(filePath);
            if (file.exists()) {
                FileReader fileReader = null;
                try {
                    fileReader = new FileReader(file);
                } catch (Exception e) {
                    System.out.println("e1=" + e);
                    return false;
                }
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                result = onLine(lineBack, bufferedReader);
                closeReader(bufferedReader);
                closeReader(fileReader);
            }
            lineBack.onEnd(filePath);
        } else {
            System.out.println("filePath is null or lineBack is null;filePath=" + filePath + ";lineBack=" + lineBack);
        }
        return result;
    }

    private static boolean onLine(LineBack lineBack, BufferedReader bufferedReader) {
        String tempString;
        try {
            tempString = bufferedReader.readLine();
        } catch (Exception e) {
            System.out.println("e1=" + e);
            return false;
        }
        while (tempString != null) {
            lineBack.onLine(tempString);
            try {
                tempString = bufferedReader.readLine();
            } catch (Exception e) {
                System.out.println("e1=" + e);
                return false;
            }
        }
        return true;
    }

    public static void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
            }
        }
    }

    public static void closeReader(Reader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (Exception e) {
            }
        }
    }

}
