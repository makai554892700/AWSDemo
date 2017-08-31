package com.mayousheng.www.utils;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.apache.http.util.TextUtils;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class S3Util {

    private static S3Util s3Util = new S3Util();
    private AmazonS3 amazonS3 = null;
    private boolean haveError = false;

    public interface FileBack {
        public void onStart();

        public void onList(String fileName);

        public void onEnd();
    }

    private S3Util() {
        try {
            AWSCredentials credentials = new ProfileCredentialsProvider().getCredentials();
            amazonS3 = AmazonS3Client.builder().withRegion(Regions.US_WEST_2)
                    .withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
        } catch (Exception e) {
            haveError = true;
            System.out.println("-------------------------------初始化s3对象失败e=" + e);
        }
    }

    public static S3Util getInstance() {
        return s3Util;
    }

    public void listFile(String bucketName, String folder, FileBack fileBack) {
        if (fileBack != null && bucketName != null && !bucketName.isEmpty() && folder != null && !folder.isEmpty()) {
            ObjectListing objectListing = amazonS3.listObjects(bucketName, folder);
            List<S3ObjectSummary> s3ObjectSummaries = objectListing.getObjectSummaries();
            fileBack.onStart();
            for (S3ObjectSummary s3ObjectSummarie : s3ObjectSummaries) {
                fileBack.onList(s3ObjectSummarie.getKey());
            }
            fileBack.onEnd();
        } else {
            System.out.println("bucketName or folder or fileBack is null.bucketName=" + bucketName + ";folder=" + folder + ";fileBack=" + fileBack);
        }
    }

    //根据桶名，文件名下载文件
    public File downloadFile(String bucketName, String fileName, File resultFile) {
        S3Object object = amazonS3.getObject(new GetObjectRequest(bucketName, fileName));
        if (object != null) {
            InputStream inputStream = object.getObjectContent();
            if (inputStream != null) {
                resultFile = FileUtils.sureFileIsNew(resultFile.getAbsolutePath());
                if (resultFile != null) {
                    FileUtils.inputStream2File(inputStream, resultFile);
                } else {
                    System.out.println("create file error.");
                }
                try {
                    inputStream.close();
                } catch (Exception e) {
                    System.out.println("e=" + e);
                }
            }
        }
        return resultFile;

    }

    //根据桶名，文件名逐行读取这个文件
    public boolean readLine(String bucketName, String fileName, FileUtils.LineBack lineBack) {
        boolean result = false;
        if (lineBack != null) {
            File file = FileUtils.sureFileIsNew(BASE_PATH + File.separator + getNameTimeStart());
            if (file != null) {
                file = downloadFile(bucketName, fileName, file);
                FileUtils.readLine(file.getAbsolutePath(), lineBack);
                file.delete();//缓存文件用完后删除
                result = true;
            }
        }
        return result;
    }

    //根据桶名，文件名，本地文件路径上传文件至s3
    public boolean uploadFile(String bucketName, String fileName, String filePath) {
        if (haveError) {
            s3Util = new S3Util();
        }
        try {
            amazonS3.putObject(bucketName, fileName, new File(filePath));
        } catch (Exception e) {
            System.out.println("-------------------------------使用s3保存文件失败,bucketName=" + bucketName + ";fileName=" + fileName + ";filePath=" + filePath);
            return false;
        }
        return true;
    }

    private static final String BASE_PATH = PathUtils.getBasePath() + "tmp";

    static {
        FileUtils.sureDir(BASE_PATH);
    }

    public static String getNameTimeStart() {
        return new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS").format(new Date());
    }

    //根据桶名，数据字符串上传文件到s3，默认文件名为当前时间戳对应。
    public boolean uploadString(String bucketName, String data) {
        boolean result = false;
        if (TextUtils.isEmpty(data) || TextUtils.isEmpty(bucketName)) {
            System.out.println("-------------------------------上传文件时传入了空字符串或空桶名,bucketName=" + bucketName);
            return false;
        }
        String parentPath = BASE_PATH + File.separator + bucketName;
        if (FileUtils.sureDir(parentPath) != null) {
            String localPath = parentPath + File.separator + getNameTimeStart();
            File tempFile = FileUtils.sureFile(localPath);//这里也可以用java自带的Java.createTempFile()方法创建临时文件
            if (tempFile != null) {
                if (FileUtils.strToFile(tempFile, new StringBuilder(data))) {
                    result = uploadFile(bucketName, tempFile.getName(), localPath);
                    if (result) {
                        tempFile.delete();
                    }
                }
            } else {
                System.out.println("-------------------------------创建本地文件失败 localPath=" + localPath);
            }
        }
        return result;
    }


}
