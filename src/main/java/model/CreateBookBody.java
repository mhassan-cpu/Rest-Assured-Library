package model;

//import static util.Utility.NameGenerator.generatePart;
import static util.Utility.NameGenerator.generateRandomName;

public class CreateBookBody {

    public static String getCreateBookBody(String Title , String Author , String ISBN , String ReleaseDate){

        return  "{\n" +
                "    \"title\": \""+Title+"\",\n" +
                "    \"author\": \""+Author+"\",\n" +
                "    \"isbn\": \"" + ISBN + "\",\n" +  // ✅ fixed quote
                "    \"releaseDate\": \""+ReleaseDate+"\"\n" +
                "}\n" ;

    }
}
