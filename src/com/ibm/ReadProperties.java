package com.ibm;


import java.io.*;
import java.util.Properties;

public class ReadProperties {
	public Properties readPropertiesFile() throws IOException {
		 String currentDirectory = System.getProperty("user.dir");
		//String filepath="\\\\dc04dwvfns306\\Shared\\L2 FileNet Support\\Gauri\\Sakya\\config.properties";
		//String filepath="C:\\Folder Monitoring\\FF_Automation\\config.properties";
		String filepath=currentDirectory + "//config.properties";
		Properties prop=null;
		FileInputStream fis = new FileInputStream(filepath);
		try {
			prop = new Properties();
			prop.load(fis);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			fis.close();
		}
		return prop;
	}

}
