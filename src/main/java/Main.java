import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONObject;


/**
 * Created by Sam Carson on 2/19/2019.
 */
public class Main {

    private static ArrayList<StudentInfo> readInfoFile(File file) {

        ArrayList<StudentInfo> list = new ArrayList<StudentInfo>();

        try {
            FileInputStream fis = new FileInputStream(file);

            // Finds the workbook instance for XLSX file
            XSSFWorkbook myWorkBook = new XSSFWorkbook (fis);

            // Return first sheet from the XLSX workbook
            XSSFSheet sheet = myWorkBook.getSheetAt(0);

            int rowsCount = sheet.getLastRowNum();

            //Loops through the rows
            for (int i = 1; i <= rowsCount; i++) {
                Row row = sheet.getRow(i);
                int colCounts = row.getLastCellNum();

                int id = 0;
                String major = "";
                String gender = "";

                //Loops through the columns
                for (int j = 0; j < colCounts; j++) {
                    Cell cell = row.getCell(j);

                    //Assigns the cell data to the correct variables
                    switch (j){
                        case 0:
                            id = (int) cell.getNumericCellValue();
                            break;
                        case 1:
                            major = cell.getStringCellValue();
                            break;
                        case 2:
                            gender = cell.getStringCellValue();
                        default:
                    }
                }

                //Creates and adds a StudentInfo object to a list
                StudentInfo temp = new StudentInfo(id, major, gender);
                list.add(temp);

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    private static ArrayList<TestScores> readScoreFile(File file, boolean isRetake ) {

        ArrayList<TestScores> list = new ArrayList<TestScores>();

        try {
            FileInputStream fis = new FileInputStream(file);

            // Finds the workbook instance for XLSX file
            XSSFWorkbook myWorkBook = new XSSFWorkbook (fis);

            // Return first sheet from the XLSX workbook
            XSSFSheet sheet = myWorkBook.getSheetAt(0);

            int rowsCount = sheet.getLastRowNum();

            //Loops through the rows
            for (int i = 1; i <= rowsCount; i++) {
                Row row = sheet.getRow(i);
                int colCounts = row.getLastCellNum();

                int id = 0;
                int score = 0;

                //Loops through the columns
                for (int j = 0; j < colCounts; j++) {
                    Cell cell = row.getCell(j);

                    //Assigns the cell data to the correct variables
                    switch (j){
                        case 0:
                            id = (int) cell.getNumericCellValue();
                            break;
                        case 1:
                            score = (int) cell.getNumericCellValue();
                            break;
                        default:
                    }
                }

                //Creates a TestScores object and adds it to a list
                TestScores temp = new TestScores(id, score, isRetake);
                list.add(temp);

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    public static void main(String[] args) {

        ArrayList<Integer> femaleCSMajors = new ArrayList<Integer>();

        //Import the Student info Excel file
        File myFile = new File("/SCHOOL/intellij_projects/TopBloc/files/Student Info.xlsx");
        ArrayList<StudentInfo> infoList = readInfoFile(myFile);

        //Import the test scores file
        File myFile1 = new File("/SCHOOL/intellij_projects/TopBloc/files/Test Scores.xlsx");
        ArrayList<TestScores> scoreList = readScoreFile(myFile1, false);

        //Import the test retake file
        File myFile2 = new File("/SCHOOL/intellij_projects/TopBloc/files/Test Retake Scores.xlsx");
        ArrayList<TestScores> retakeScoreList = readScoreFile(myFile2, true);

        //Find final test scores for each student
        for (StudentInfo student: infoList) {
            //Loop through all original test scores
            for (TestScores test1: scoreList) {
                //Picks the test belonging to the student
                if (student.id == test1.id) {
                    //Sets initial final score to the original test
                    student.finalScore = test1.score;
                    //Loop through all test retakes
                    for (TestScores test2 : retakeScoreList) {
                        //If the student did a retake test check if it is better that test1
                        if (student.id == test2.id) {
                            if (test1.score < test2.score) {
                                //Sets the final score to test2 if it was better than test1
                                student.finalScore = test2.score;
                            }
                            continue;
                        }
                    }
                }
                continue;
            }
        }

        int count = 0;

        //Find class average with final scores
        for (StudentInfo s: infoList) {
            count += s.finalScore;
        }

        double classAvg = (double) count / infoList.size();

        System.out.println("\nClass Average: " + classAvg);

        //Get id's of female CS majors
        for (StudentInfo s: infoList) {
            if (s.major.equals("computer science") && s.gender.equals("F")) {
                femaleCSMajors.add(s.id);
            }
        }

        //Sort the list
        Collections.sort(femaleCSMajors);

        //Print female CS majors
        System.out.println("\nFemale CS Majors\n");
        for (int s: femaleCSMajors) {
            System.out.println(s);
        }
        System.out.println("\n");

        //Build the JSON object
        JSONObject obj = new JSONObject();

        obj.put("id", "carsonsa@mail.gvsu.edu");
        obj.put("name", "Samuel Carson");
        obj.put("average", classAvg);
        obj.put("studentIds", femaleCSMajors);

        //Prints the Object
        System.out.println(obj);

        //Send the JSON object
        CloseableHttpClient client = HttpClients.createDefault();

        try {
            //Set where the Post request is being sent
            HttpPost post = new HttpPost("http://3.86.140.38:5000/challenge");

            //Add the JSON object
            StringEntity params = new StringEntity(obj.toJSONString());

            //Add a header
            post.addHeader("content-type", "application/json");

            //Set the parameters
            post.setEntity(params);

            //Send the Post request
            HttpResponse response = client.execute(post);

            //Print the response
            System.out.println(response.getStatusLine().toString());

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
