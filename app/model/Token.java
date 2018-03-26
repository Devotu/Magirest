package model;

import java.util.Date;

public class Token {

    public int id;
    public String token;
    public Date valid;

    public Token(String token){
        this.token = token;
    }

}