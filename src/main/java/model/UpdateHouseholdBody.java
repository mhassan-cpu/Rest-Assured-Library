package model;

public class UpdateHouseholdBody {
    public static String getUpdateHouseholdBody(String name){

        return  "{\n" +
                "        \"name\": \""+name+"\"\n" +
                "    }";

    }
}


