package com.loyo.fasttest.common.download;

import android.os.Environment;

import com.loyo.fasttest.common.BaseApplication;

import java.io.File;
import java.io.IOException;

/**
 * 创建下载的文件
 * @author myjie
 */
public class FileUtil {
	public static File updateFile = null;
	public static final String updateApkPackage = "apk";//升级包保存的目录
	public static boolean isCreateFileSucess = false;
    public static File updateDir = BaseApplication.getInstance().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
    /**
	 * 创建一个apk的文件，创建后，可以使用updateFile使用文件
	 * @param fileName 创建的文件名
	 * @return 是否成功创建
	 */
	public static Boolean createFile(String fileName) {

		if (android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment
				.getExternalStorageState())) {
			updateFile = new File(updateDir + "/" + fileName + ".apk");
			if (!updateDir.exists()) { //文件夹不存在，就创建
				updateDir.mkdirs();
			}
			if (updateFile.exists()) {//文件已经存在，删除
				updateFile.delete();
			}
			try {
				updateFile.createNewFile(); //创建
				isCreateFileSucess = true;
			} catch (IOException e) {
				isCreateFileSucess = false;
				e.printStackTrace();
			}

		} else {
			isCreateFileSucess = false;
		}
		return isCreateFileSucess;
	}

	/**
	 * 删除文件夹
	 * @param file
     */
	public static void deleteFile(File file){

		if(file.isDirectory()){
			File[] files = file.listFiles();
			for(int i=0; i<files.length; i++){
				deleteFile(files[i]);
			}
		}
		file.delete();
	}

}