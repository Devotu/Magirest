package model;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class Player {
    public int id;
    public String name;


    public Player(int id, String name) {
        this.id = id;
        this.name = name;
    }
}