package model;

import util.Neo4jDriver;

import java.util.Date;
import java.util.HashMap;

import javax.naming.directory.InvalidAttributesException;

import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.exceptions.*;

import static org.neo4j.driver.v1.Values.parameters;

import play.Logger;

public class Deck {

    public int id;
	public String name;
	public String format;
	public long blackCards;
	public long whiteCards;
	public long redCards;
	public long greenCards;
	public long blueCards;
	public long colorlessCards;
	public String theme;
	public boolean active;
	public Date created;


    //TODO Test if still needed when attributes are public
    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getFormat() {
        return this.format;
    }

    public long getBlackCards(){
        return this.blackCards;
    }

    public long getWhiteCards(){
        return this.whiteCards;
    }

    public long getRedCards(){
        return this.redCards;
    }

    public long getGreenCards(){
        return this.greenCards;
    }

    public long getxBlueCards(){
        return this.blueCards;
    }

    public long getColorlessCards(){
        return this.colorlessCards;
    }

    public String getTheme() {
        return this.theme;
    }

    public boolean getActive() {
        return this.active;
    }

    public Date getCreated() {
        return this.created;
    }
}