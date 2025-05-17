package model;

public class CreateUserBody {

    public static String getCreateUserBody(String firstName , String lastName , String email){

        return  "{\n" +
                "    \"firstName\": \""+firstName+"\",\n" +
                "    \"lastName\": \""+lastName+"\",\n" +
                "    \"email\": \""+email+"\"\n" +
                "}\n" ;

    }
}

