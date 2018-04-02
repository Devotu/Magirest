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
}