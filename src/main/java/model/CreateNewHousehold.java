package model;

public class CreateNewHousehold {

    public static String getCreateHouseholdBody(String name){

        return  "{\n" +
                "        \"name\": \""+name+"\"\n" +
                "    }";

    }
}
