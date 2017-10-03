package csvtest;

//import com.opencsv.*; // change this to the old package name shown below before building in Gradle!
import au.com.bytecode.opencsv.*; // this is the old package name required by the Gradle plugin!
import java.io.StringReader;
import java.util.List;
import org.json.simple.*;
import org.json.simple.parser.*;
import static java.lang.Math.toIntExact;
import java.util.Iterator;

public class Converter {

    /*
        Consider a CSV file like the following:
        
        ID,Total,Assignment 1,Assignment 2,Exam 1
        111278,611,146,128,337
        111352,867,227,228,412
        111373,461,96,90,275
        111305,835,220,217,398
        111399,898,226,229,443
        111160,454,77,125,252
        111276,579,130,111,338
        111241,973,236,237,500
        
        The corresponding JSON file would be as follows (note the curly braces):
        
        {
            "colHeaders":["ID","Total","Assignment 1","Assignment 2","Exam 1"],
            "rowHeaders":["111278","111352","111373","111305","111399","111160","111276","111241"],
            "data":[[611,146,128,337],
                    [867,227,228,412],
                    [461,96,90,275],
                    [835,220,217,398],
                    [898,226,229,443],
                    [454,77,125,252],
                    [579,130,111,338],
                    [973,236,237,500]
            ]
        }  
    */

    @SuppressWarnings("unchecked")
    public static String csvToJson(String csvString) {
        
        try {
        
            CSVReader reader = new CSVReader(new StringReader(csvString));
            List<String[]> csvList = reader.readAll();
            
            long lines = reader.getLinesRead();
            boolean closeBracketNoComma = false;  //Switch to add bracket without comma and end line
            boolean closeBracketWithComma = false;  //Switch to add bracket and comma to end of line
            boolean firstColHeader = false; //Switch to avoid printing the first col header
            int counter = 0; //Used to keep track of iterations through enhanced for loop
            boolean ignoreRowSwitch = false; //Switch to prevent rowHeaders being printed as data
            boolean convertDataToInt = false; //Switch
            int totalLinesInResult = toIntExact(lines); //Convert lines to int to use for array initialization
            String[] resultsArray = new String[totalLinesInResult + 4]; //+4 for curly braces/bracket
            resultsArray[0] = "{";
            int currentLine = 1;
            
            for(String[] a : csvList) {
                
                //Formatting for each line:
                if (lines == reader.getLinesRead()) {
                    resultsArray[currentLine] = "\t\"colHeaders\":[";
                    closeBracketWithComma = true;
                    firstColHeader = true;  //Used to avoid printing first col header
                }
                if (lines == (reader.getLinesRead() - 1)) {
                    resultsArray[currentLine] = "\t\"rowHeaders\":[";
                    
                    //iterate through the csvlist and add the first string of each line to rowHeaders
                    for(String[] x : csvList) {
                        
                        //to avoid printing a colHeader
                        if (counter >= 1) {
                            
                            for(int i = 0; i < 1; i++) {
                                //if this is the last line being parsed, do not add a comma
                                //otherwise, add the first string of each line to rowHeaders
                                if (counter == (csvList.size()-1)) {
                                    resultsArray[currentLine] += x[i];
                                }
                                else {
                                    resultsArray[currentLine] += x[i] + ",";
                                }
                            }
                        }
                        counter++;
                    }
                    
                    lines--; //Lines is decremented an extra time on this iteration since the entire line is printed in this conditional
                    resultsArray[currentLine] += "],";
                    ignoreRowSwitch = true; //Prevents rowHeaders from being printed as data
                    currentLine++;
                }
                //Data Header and Int switch
                if (lines == (reader.getLinesRead() - 2)) {
                    resultsArray[currentLine] = "\t\"data\":[[";
                    convertDataToInt = true; //Switched to true to convert "data" strings into ints
                    closeBracketWithComma = true;
                }
                //Close all lines of data with a bracket and comma
                //lines is equal to 0 when the last line of reader is being parsed
                if (lines < (reader.getLinesRead() - 2) && lines != 0) {
                    resultsArray[currentLine] = "\t\t[";  //Tabs each line of "data"
                    closeBracketWithComma = true;
                }
                if (lines == 0) {
                    resultsArray[currentLine] = "\t\t[";  //Tabs last line of "data"
                    closeBracketNoComma = true; //No comma for last line of data
                }
                
                //Keep rowHeaders out of data after reading colHeaders
                if (lines < (reader.getLinesRead() - 1)) {
                    ignoreRowSwitch = true;  //Must be switched to true after every line of "data"
                }
                
                lines--;
                
                for(int i = 0; i < a.length; i++) {

                    // "s" is the next field from the next line of the CSV data.
                    // Parse these into a new string with matches the format
                    // of the JSON string shown in the sample.
                    
                    //If this is not the first col header element, add each result to array
                    if (firstColHeader == false) {
                        //Prevents rowHeaders from being considered data
                        if (ignoreRowSwitch == false) {
                            
                            //If this is the last string in the line, add it to the array with no comma
                            if (i == (a.length - 1)) {
                                if (convertDataToInt == false) {
                                    resultsArray[currentLine] += a[i];
                                }
                                //If data is being parsed, convert to int
                                else {
                                    int dataInt = Integer.parseInt(a[i]);
                                    resultsArray[currentLine] += dataInt;
                                }
                            }
                            //If this is not the last string, add it to the array with a comma
                            else {
                                if (convertDataToInt == false) {
                                    resultsArray[currentLine] += a[i] + ",";
                                }
                                //If data is being parsed, convert to int
                                else {
                                    int dataInt = Integer.parseInt(a[i]);
                                    resultsArray[currentLine] += dataInt + ",";
                                }
                            }
                        }
                        else {
                            //Prevents rowheaders from being printed as data
                            ignoreRowSwitch = false;
                        }
                    }
                    else {
                        //Prevents the first colHeader from being added
                        firstColHeader = false;
                    }
                }
                
               //if there should be a closing bracket, add it and end line. Otherwise just end line
                if (closeBracketWithComma == true) {
                    resultsArray[currentLine] += "],";
                    closeBracketWithComma = false;
                }
                if (closeBracketNoComma == true) {
                    resultsArray[currentLine] += "]";
                    closeBracketNoComma = false;
                }
                currentLine++;
            }
            //Add a closing bracket/curly brace
            resultsArray[currentLine] = "\t ]";
            currentLine++;
            resultsArray[currentLine] = "}";
            
            //Combine all elements in string array to create a returnable string
            String resultsString = "";
            for (int i = 0; i < resultsArray.length; i++) {
                resultsString = (resultsString + resultsArray[i] + "\n" );
            }
            return resultsString;
            
        }
        
        catch (Exception e) {
            //
        }
        
        return "";
        
    }
    
    public static String jsonToCsv(String jsonString) {
        
        JSONParser parser = new JSONParser();
        
		
        try{
           
           //Create a jsonobject from the string
           JSONObject obj = (JSONObject) parser.parse(jsonString);
           //System.out.println(obj); //Debugging
           
           //Create arrays from jsonobject
           JSONArray rowHeaders = (JSONArray) obj.get("rowHeaders");
           JSONArray colHeaders = (JSONArray) obj.get("colHeaders");
           JSONArray data = (JSONArray) obj.get("data");
           
           //Create string arrays to eventually create return string
           String[] dataArray = new String[data.size()];
           String[] colArray = new String[colHeaders.size()];
           String[] rowArray = new String[rowHeaders.size()];
           
           int counter = 0;
           String finishedResult = "";
           
           //Record data to a string array
           for (int i = 0; i < data.size(); i++) {
            dataArray[i] = data.get(i).toString().replace("[", "").replace("]", "");
           }

           //Record colHeaders to a string array
           Iterator<String> it = colHeaders.iterator();
           while (it.hasNext()) {
               colArray[counter] = it.next();
               counter++;
           }
           
           counter = 0; //Reset counter
           
           //Record rowHeaders to a string array
           Iterator<String> it2 = rowHeaders.iterator();
           while (it2.hasNext()) {
               rowArray[counter] = it2.next();
               counter++;
           }
           
           //Create first line of string from colheaders
           for (int i = 0; i < colArray.length ; i++) {
               //if this is the last line, dont add a comma
               if (i == colArray.length - 1) {
                   finishedResult += colArray[i];
               }
               //otherwise, add colheaders as normal
               else {
                   finishedResult += colArray[i] + ",";
               }
           }
           finishedResult += "\n"; //End the first line
           
           //Add the rest of the lines, placing rowHeaders in front of data
           for (int j = 0; j < rowArray.length ; j++) {
               finishedResult += (rowArray[j] + "," + dataArray[j] + "\n");
           }
           
           //System.out.println("finishedResult = " + finishedResult);  //Debugging
           return finishedResult;
        }
        catch (Exception e) {
        }
        
        return "";
    }
    
}
