package model;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class User {
    private int id;
    public String name;
    public String password;

    public User(ObjectNode json){
        try {
            
            this.id = json.get("id").asInt();
            this.name = json.get("name").asText();
            this.password = json.get("password").asText();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getPassword() {
        return this.password;
    }

}