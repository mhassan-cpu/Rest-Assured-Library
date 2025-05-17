package util;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.Random;

public class Utility {



    public static class NameGenerator {

        private static final String LETTERS = "abcdefghijklmnopqrstuvwxyz";
        private static final Random RANDOM = new Random();

        public static String generateRandomName() {
            return generatePart() + " " + generatePart();
        }

        private static String generatePart() {
            StringBuilder part = new StringBuilder();
            // First letter is uppercase
            part.append(Character.toUpperCase(LETTERS.charAt(RANDOM.nextInt(LETTERS.length()))));
            // Next 3 letters are lowercase
            for (int i = 0; i < 3; i++) {
                part.append(LETTERS.charAt(RANDOM.nextInt(LETTERS.length())));
            }
            return part.toString();
        }




        public static String getSingleJsonData(String jsonFilePath, String key) throws IOException, ParseException, FileNotFoundException, org.json.simple.parser.ParseException {
            JSONParser jsonParser = new JSONParser();

            FileReader fileReader = new FileReader(jsonFilePath);
            Object obj = jsonParser.parse(fileReader);

            JSONObject jsonObject = (JSONObject) obj;
            return jsonObject.get(key).toString();
        }

        public static String getExcelData(int RowNum, int ColNum, String SheetName) {
            XSSFWorkbook workBook;
            XSSFSheet sheet;
            String projectPath = System.getProperty("user.dir");
            String cellData = null;
            try {
                workBook = new XSSFWorkbook(projectPath + "/src/test/resources/data/data.xlsx");
                sheet = workBook.getSheet(SheetName);
                cellData = sheet.getRow(RowNum).getCell(ColNum).getStringCellValue();

            } catch (IOException e) {
                System.out.println(e.getMessage());
                System.out.println(e.getCause());
                e.printStackTrace();
            }
            return cellData;
        }



            private static final Random random = new Random();

            public static String generateRandomIsbn() {
                return generate4Digit() + "-" + generate4Digit() + "-" + generate4Digit();
            }

            private static String generate4Digit() {
                // Generates a number between 1000 and 9999 (inclusive)
                return String.valueOf(1000 + random.nextInt(9000));
            }

            // Example usage
           /* public static void main(String[] args) {
                String isbn = generateRandomIsbn();
                System.out.println("Generated ISBN: " + isbn);
            }
        } */



        //private static final String LETTERS = "abcdefghijklmnopqrstuvwxyz";
        //private static final Random RANDOM = new Random();



       /* private static String generateemailPart() {
            StringBuilder part = new StringBuilder();
            // First letter uppercase
            part.append(Character.toUpperCase(LETTERS.charAt(RANDOM.nextInt(LETTERS.length()))));
            // Next 3 letters lowercase
            for (int i = 0; i < 3; i++) {
                part.append(LETTERS.charAt(RANDOM.nextInt(LETTERS.length())));
            }
            return part.toString();
        }*/

        public static String generateRandomEmail() {
            String LETTERS = "abcdefghijklmnopqrstuvwxyz";
            String firstPart = generatePart().toLowerCase();
            String secondPart = generatePart().toLowerCase();
            int number = 100 + RANDOM.nextInt(900); // random 3-digit number
            return firstPart + "." + secondPart + number + "@example.com";
        }



    }

}




