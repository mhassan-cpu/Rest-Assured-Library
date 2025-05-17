package model;

public class UpdateUserBody {
    public static String getUpdateUserBody(String firstName , String lastName , String email){

        return  "{\n" +
                "    \"firstName\": \""+firstName+"\",\n" +
                "    \"lastName\": \""+lastName+"\",\n" +
                "    \"email\": \""+email+"\"\n" +
                "}\n" ;

    }
}
