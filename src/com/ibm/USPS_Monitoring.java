package com.ibm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class USPS_Monitoring {
	private static String str_Mailbody = "";
	public static void main(String[] args) throws Exception {
		Date date = new Date();
		SimpleDateFormat dateFormatr = new SimpleDateFormat("MM/dd/yyyy");
		SimpleDateFormat dateFormatx = new SimpleDateFormat("MM-dd-yyyy");
		String dateToday=dateFormatx.format(date);
		String Path_Details_Excel = "/opt/IBM/FileNet/AE/Router/Automation/USPS_Attachment/";
		String ExcelFileName_Today = "USPS Batch job sheet-"+dateToday+".xlsx";
		//String File_Attachement = Path_Details_Excel+ExcelFileName_Today;
		String currentDirectory = System.getProperty("user.dir");
		System.out.println(currentDirectory);
		USPS_Monitoring_Email email = new USPS_Monitoring_Email();
		boolean email_flag=false;
		String dt=dateFormatr.format(date);
		Calendar d = Calendar.getInstance();
		d.setTime(date);
		d.add(Calendar.DATE, -1);
		Date newDate1 = d.getTime();
		String dateYesterday=dateFormatr.format(newDate1);
		String query="select count(*) , to_char(last_process_date, 'MM/DD/YYYY') as process_date,status from tbl_source_data where to_char(last_process_date, 'MM/DD/YYYY') ='"+dt+"' group by to_char(last_process_date, 'MM/DD/YYYY'),status";
		String query2="select count(*) , to_char(last_process_date, 'MM/DD/YYYY') as process_date,status from tbl_source_data where to_char(last_process_date, 'MM/DD/YYYY') ='"+dateYesterday+"' group by to_char(last_process_date, 'MM/DD/YYYY'),status";

		int[] value=sqlserver_check(query);
		System.out.println("Processed Value : "+value[0]);
		System.out.println("New Value : "+value[1]);
		int total_value=(value[0]+value[1]);
		System.out.println("Total count : " + total_value);
		String sts="";
		if(value[0]>0 && value[1]<1) {
			sts="Completed";
		}else if(value[0]>0 && value[1]>0) {
			sts="In Progress";
		}else {
			sts="Not Started";
		}
		String[] dt_str =dt.split("/");
		String str_date = dt_str[2]+dt_str[0]+dt_str[1];
		String file_Name = "1030001_AddressCorrections_" + str_date + "_1";
		//String file_Namee = "1030001_AddressCorrections_20240708_1";
		System.out.println("file_Name : "+file_Name);
		if(parseFile("/opt/IBM/FileNet/AE/Router/USPS/COMPONENTS/UploadFiles/app/logs/IMB.log",file_Name)) {

			if(value[0]>0) {
				email_flag=true;
				String status = "In Progress";
				if(value[1]<1) {
					status = "Completed";
				}
				str_Mailbody="Hi Team,\r\n"
						+ "\r\n"
						+ "Kindly find below status for the USPS Job : \r\n"
						+ " \r\n"
						+ "USPS File Name               -  "+file_Name+"\r\n"
						+ "Start date and time         -  "+dt+" 4:00 AM EST\r\n"
						+ "Count (NEW)                    -  "+value[1]+"\r\n"
						+ "Count (PROCESSED)        -  "+value[0]+"\r\n"
						+ "Status                                 -   "+status+"\r\n"
						+ " \r\n"
						+ "\r\n"
						+ "\r\n"
						+ "\r\n"
						+ " \r\n"
						+ "Regards\r\n"
						+ "FileNetLightsOnSupport Team\r\n";
				//mail to business
			}else if (value[1]>0){
				str_Mailbody=str_Mailbody + "\r\n\r\n" + "Count uploaded in sql server but not processing. Please check CreateorSkip job. (This automation will not work in between 3:30 AM est to 4:10 AM EST)";
				//CreateorSkip job failed
			}else {
				str_Mailbody=str_Mailbody + "\r\n\r\n" + "Count not present in sql server. Please check USPS Upload job.";
			}
		}else {
			//Log -> (No file in the log) so check for input file in ftpload folder
			File file = new File("/opt/IBM/FileNet/AE/Router/USPS_AU/USPS_DataLoad/ftpload");
			File[] fileList = file.listFiles();
			boolean flag1 = false;
			if(fileList!=null) {
				for(int i=0;i<fileList.length;i++) {
					String namee_input1 = fileList[i].getName().toString().toLowerCase();
					System.out.println(namee_input1);
					String newFileName = file_Name + ".txt";
					//String newFileName = "1030001_AddressCorrections_20240701_1.txt";
					if(namee_input1.equalsIgnoreCase(newFileName.toLowerCase())) {
						flag1=true;
						break;
					}else {
						flag1=false;
					}
				}
				if(flag1) {
					System.out.println("Today's input file present");
					if(value[1]>0) {
						str_Mailbody ="Hi Team," + "\r\n\r\n" + "There is some issue in the USPS IMB.log file. Count present in sql server but log is not updated";
					}else {
						str_Mailbody="Hi Team," + "\r\n\r\n" + "USPS Upload Job is not executed properly today, so please check.";
						//Log not updated + Input file present
					}
				}else {
					System.out.println("There is no input file for today");
					str_Mailbody="Hi Team," + "\r\n\r\n" + "USPS - There is no input file for today. Please cross check manually.";
					//Log not updated + Input file not present
				}
			}
		}
		if(email_flag) {
			ExcelUpdationLastPart(file_Name,dt,total_value,sts,query2,ExcelFileName_Today,Path_Details_Excel);
			ExcelUpdation(file_Name,dt,total_value,sts,query2,ExcelFileName_Today,Path_Details_Excel);
		}
		email.email(str_Mailbody,email_flag,dt,Path_Details_Excel);
	}
	private static void ExcelUpdationLastPart(String file_Name, String dt, int total_value, String sts, String query2,
			String excelFileName_Today, String path_Details_Excel) throws IOException, InvalidFormatException, ClassNotFoundException, SQLException {
		File filee = new File(path_Details_Excel);
		File[] fileList = filee.listFiles();
		String ExcelFileName="";
		if(fileList!=null) {
			for(int i=0;i<fileList.length;i++) {
				ExcelFileName = fileList[0].getName().toString();
			}
		}
		String fileName = path_Details_Excel+ExcelFileName;
		// load the workbook
		InputStream inp = new FileInputStream(fileName);
		Workbook wb = WorkbookFactory.create(inp);
		inp.close();

		// make some changes
		Sheet sh = wb.getSheetAt(0);
		// Row s = sh.createRow(sh.getPhysicalNumberOfRows());
		int x=sh.getLastRowNum();
		System.out.println("Last Row : "+x);
		int[] valuee=sqlserver_check(query2);
		System.out.println("Processed Value : "+valuee[0]);
		System.out.println("New Value : "+valuee[1]);

		if(valuee[1]>0) {
			Cell cell1 =sh.getRow(x).getCell(6);
			cell1.setCellValue("In Progress");
		}else {
			Cell cell2 =sh.getRow(x).createCell(5);
			System.out.println("dt lastpart : "+dt.toString());
			cell2.setCellValue(dt.toString());
			Cell cell1 =sh.getRow(x).getCell(6);
			cell1.setCellValue("Completed");
		}

		// overwrite the workbook with changed workbook
		FileOutputStream fileOut = new FileOutputStream(fileName);
		wb.write(fileOut);
		fileOut.close();

		File filex = new File(fileName);
		File rename = new File(path_Details_Excel+excelFileName_Today);
		filex.renameTo(rename);
	}

	private static void ExcelUpdation(String file_Name, String dt, int total_value, String sts, String query2, String excelFileName_Today, String path_Details_Excel) throws InvalidFormatException, IOException, ClassNotFoundException, SQLException {

		File filee = new File(path_Details_Excel);
		File[] fileList = filee.listFiles();
		String ExcelFileName="";
		if(fileList!=null) {
			for(int i=0;i<fileList.length;i++) {
				ExcelFileName = fileList[0].getName().toString();
			}
		}
		String fileName = path_Details_Excel+ExcelFileName;
		// load the workbook
		InputStream inp = new FileInputStream(fileName);
		Workbook wb = WorkbookFactory.create(inp);
		inp.close();

		// make some changes
		Sheet sh = wb.getSheetAt(0);
		Row r = sh.createRow(sh.getLastRowNum()+1);
		Cell c = r.createCell(1);
		c.setCellValue(file_Name);
		c = r.createCell(2);
		c.setCellValue(dt);
		c = r.createCell(3);
		c.setCellValue(total_value);
		c = r.createCell(4);
		c.setCellValue(dt);
		c = r.createCell(6);
		c.setCellValue(sts);

		// overwrite the workbook with changed workbook
		FileOutputStream fileOut = new FileOutputStream(fileName);
		wb.write(fileOut);
		fileOut.close();

		File filex = new File(fileName);
		File rename = new File(path_Details_Excel+excelFileName_Today);
		filex.renameTo(rename);
	}
	private static int[] sqlserver_check(String query) throws ClassNotFoundException, SQLException {

		Class.forName("oracle.jdbc.driver.OracleDriver");
		Connection con2 = DriverManager.getConnection("jdbc:oracle:thin:@//fnetcengn-p-01.internal.das:1525/fnetpep", "IMB", "IMB"); 
		Statement stmt1 = con2.createStatement();
		ResultSet rs1 = stmt1.executeQuery(query);
		int[] value = new int[2];
		while (rs1.next()) {
			if(rs1.getString(3).equals("PROCESSED")) {
				value[0]= rs1.getInt(1);
			}else if(rs1.getString(3).equals("NEW")) {
				value[1]= rs1.getInt(1); 
			}else {
				value[0]=0;
				value[1]=0;
			}
		}
		con2.close();
		return value;
	}

	private static boolean parseFile(String fileName, String searchStr) throws FileNotFoundException {
		Scanner scan = new Scanner(new File(fileName));
		boolean flag = false;
		System.out.println("inside LogCheck method");
		while(scan.hasNext()) {
			String line = scan.nextLine().toLowerCase().toString();
			if(line.contains(searchStr.toLowerCase())) {
				flag=true;
			}
		}

		if(flag) {
			System.out.println("Input file name present in log (return true)");
			return true;
		}else {
			System.out.println("Input file name not present in log (return false)");
			return false;
		}
	}
}
