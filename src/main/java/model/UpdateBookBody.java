package model;

public class UpdateBookBody {

    public static String getUpdateBookBody(String Title , String Author , String ISBN , String ReleaseDate){

        return  "{\n" +
                "    \"title\": \""+Title+"\",\n" +
                "    \"author\": \""+Author+"\",\n" +
                "    \"isbn\": \"" + ISBN + "\",\n" +  // ✅ fixed quote
                "    \"releaseDate\": \""+ReleaseDate+"\"\n" +
                "}\n" ;

    }
}

