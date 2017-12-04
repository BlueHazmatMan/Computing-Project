
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;

public class DatabaseInteractions {

    /*select m.`MonsterName`, m.`MonsterXP`, r.`RoomID` from monster m
inner join monsterroomlink mrl on mrl.MonsterID = m.MonsterID
inner join room r on r.`RoomID` = mrl.`RoomID`
where r.`RoomID` = 88*/
    
    // init database constants
    private static final String DATABASE_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DATABASE_URL = "jdbc:mysql://localhost:3306/dungeondatabase";
    private static final String USERNAME = "user";
    private static final String PASSWORD = "password";
    private static final String MAX_POOL = "250";
    private Statement stmt;

    // init connection object
    private Connection connection;
    // init properties object
    private Properties properties;

    // create properties
    private Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
            properties.setProperty("user", USERNAME);
            properties.setProperty("password", PASSWORD);
            properties.setProperty("MaxPooledStatements", MAX_POOL);
            properties.setProperty("useSSL", "false");
        }
        return properties;
    }

    // connect database
    private Connection connect() {
        if (connection == null) {
            try {
                Class.forName(DATABASE_DRIVER);
                connection = DriverManager.getConnection(DATABASE_URL, getProperties());
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }
        }
        return connection;
    }

    public void deleteDungeon(String dungeonName) {

        //Get dungeonID
        int dungeonID = getDungeonID(dungeonName);

        deleteDungeonRow(dungeonID);

        //find all rooms with that dungeonID
        ArrayList<Integer> roomIDs = getRoomIDs(dungeonID);

        //delete those rooms
        deleteRooms(roomIDs);

        //find all furnitureroomlinks with those roomIDs and delete
        deleteRoomLink(roomIDs, "furniture");

        //^ but with monsters
        deleteRoomLink(roomIDs, "monster");
    }

    public void deletePlayer(String playerName){
        
        //get playerID
        int playerID = getPlayerID(playerName);
        
        //deletes from the player table
        deletePlayerFromPlayerTable(playerID);
        
        //deletes from the link table
        deletePlayerDungeonLink(playerID);
        
    }
    
    private void deletePlayerDungeonLink(int playerID){
        String sql = "DELETE FROM playerDungeonLink WHERE PlayerID = " + playerID;
        try {
            connect();
            command(sql);
            disconnect();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    
    private void deletePlayerFromPlayerTable(int playerID){
        String sql = "DELETE FROM player WHERE PlayerID = " + playerID;
        try {
            connect();
            command(sql);
            disconnect();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    
    public int getDungeonID(String dungeonName) {
        String sql = "SELECT * FROM dungeon WHERE DungeonName = '" + dungeonName + "';";
        int dungeonID = -1;
        try {
            connect();
            ResultSet rs = query(sql);
            while (rs.next()) {
                dungeonID = rs.getInt("DungeonID");
            }
            closeStatement();
            disconnect();
        } catch (Exception e) {
            System.out.println(e);
        }
        return dungeonID;
    }

    private void deleteDungeonRow(int dungeonID) {
        String sql = "DELETE FROM dungeon WHERE DungeonID = " + dungeonID;
        try {
            connect();
            command(sql);
            disconnect();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public ArrayList<Integer> getRoomIDs(int dungeonID) {
        String sql = "SELECT * FROM room WHERE DungeonID = " + dungeonID + ";";
        ArrayList<Integer> roomIDs = new ArrayList();
        try {
            connect();
            ResultSet rs = query(sql);
            while (rs.next()) {
                roomIDs.add(rs.getInt("RoomID"));
            }
            closeStatement();
            disconnect();
        } catch (Exception e) {
            System.out.println(e);
        }
        return roomIDs;
    }

    private void deleteRooms(ArrayList roomIDs) {
        String sql;
        for (int i = 0; i < roomIDs.size(); i++) {
            sql = "DELETE FROM room WHERE RoomID = " + roomIDs.get(i);
            try {
                connect();
                command(sql);
                disconnect();
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    private void deleteRoomLink(ArrayList roomIDs, String tableName) {
        String sql;
        for (int i = 0; i < roomIDs.size(); i++) {
            sql = "DELETE FROM " + tableName + "roomlink WHERE RoomID = " + roomIDs.get(i);
            try {
                connect();
                command(sql);
                disconnect();
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    public ArrayList<String> getDungeonList() {
        String sql = "SELECT DungeonName FROM dungeon";
        ArrayList<String> dungeonNames = new ArrayList();
        try {
            connect();
            ResultSet rs = query(sql);
            while (rs.next()) {
                dungeonNames.add(rs.getString("DungeonName"));
            }
            closeStatement();
            disconnect();
        } catch (Exception e) {
            System.out.println(e);
        }
        return dungeonNames;
    }

    public ArrayList<Integer> getFurnitureIDs(int roomID) {
        String sql = "SELECT * FROM furnitureroomlink WHERE RoomID = " + roomID + ";";
        ArrayList<Integer> furnitureIDs = new ArrayList();
        try {
            connect();
            ResultSet rs = query(sql);
            while (rs.next()) {
                furnitureIDs.add(rs.getInt("FurnitureID"));
            }
            closeStatement();
            disconnect();
        } catch (Exception e) {
            System.out.println(e);
        }
        return furnitureIDs;
    }

    public String getFurnitureName(int furnitureID) {
        String sql = "SELECT * FROM furniture WHERE FurnitureID = " + furnitureID + ";";
        String furnitureName = "ERROR";
        try {
            connect();
            ResultSet rs = query(sql);
            while (rs.next()) {
                furnitureName = rs.getString("FurnitureName");
            }
            closeStatement();
            disconnect();
        } catch (Exception e) {
            System.out.println(e);
        }
        return furnitureName;
    }

    public ArrayList<Integer> getMonsterIDs(int roomID) {
        String sql = "SELECT * FROM monsterroomlink WHERE RoomID = " + roomID + ";";
        ArrayList<Integer> monsterIDs = new ArrayList();
        try {
            connect();
            ResultSet rs = query(sql);
            while (rs.next()) {
                monsterIDs.add(rs.getInt("MonsterID"));
            }
            closeStatement();
            disconnect();
        } catch (Exception e) {
            System.out.println(e);
        }
        return monsterIDs;
    }

    public int getPlayerID(String playerName) {
        String sql = "SELECT PlayerID, PlayerName FROM player WHERE PlayerName = '" + playerName + "';";
        int playerID = -1;
        try {
            connect();
            ResultSet rs = query(sql);
            while (rs.next()) {
                playerID = rs.getInt("PlayerID");
            }
            closeStatement();
            disconnect();
        } catch (Exception e) {
            System.out.println(e);
        }
        return playerID;
    }

    public String getPlayerClass(String playerName){
        String sql = "SELECT PlayerClass, PlayerName FROM player WHERE PlayerName = '" + playerName + "';";
        String playerClass = "NULL";
        try {
            connect();
            ResultSet rs = query(sql);
            while (rs.next()) {
                playerClass = rs.getString("PlayerClass");
            }
            closeStatement();
            disconnect();
        } catch (Exception e) {
            System.out.println(e);
        }
        return playerClass;
    }
    
    public int getPlayerLevel(String playerName){
        String sql = "SELECT PlayerLevel, PlayerName FROM player WHERE PlayerName = '" + playerName + "';";
        int playerLevel = -1;
        try {
            connect();
            ResultSet rs = query(sql);
            while (rs.next()) {
                playerLevel = rs.getInt("PlayerLevel");
            }
            closeStatement();
            disconnect();
        } catch (Exception e) {
            System.out.println(e);
        }
        return playerLevel;
    }
    
    public String getPlayerName(int playerID){
        String sql = "SELECT PlayerID, PlayerName FROM player WHERE PlayerID = " + playerID + ";";
        String playerName = "";
        try {
            connect();
            ResultSet rs = query(sql);
            while (rs.next()) {
                playerName = rs.getString("PlayerName");
            }
            closeStatement();
            disconnect();
        } catch (Exception e) {
            System.out.println(e);
        }
        return playerName;
    }
    
    public Player setupPlayerEditInformation(int playerID) {
        Player editPlayer = new Player();
        String sql = "SELECT * FROM player WHERE PlayerID = " + playerID + ";";
        try {
            connect();
            ResultSet rs = query(sql);
            while (rs.next()) {
                editPlayer.setName(rs.getString("PlayerName"));
                editPlayer.setLevel(rs.getInt("PlayerLevel"));
                editPlayer.setClass(rs.getString("PlayerClass"));
            }
            closeStatement();
            disconnect();
        } catch (Exception e) {
            System.out.println(e);
        }
        return editPlayer;
    }

    public void editPlayer(Player editPlayer, int playerID) {
        String sql = "UPDATE player SET PlayerName = '" + editPlayer.getName() + "', PlayerLevel = " + editPlayer.getLevel() + ", PlayerClass = '" + editPlayer.getPlayerClass() + "' WHERE PlayerID = " + playerID + ";";
        try {
            connect();
            command(sql);
            disconnect();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public String getMonsterName(int monsterID) {
        String sql = "SELECT * FROM monster WHERE MonsterID = " + monsterID + ";";
        String monsterName = "ERROR";
        try {
            connect();
            ResultSet rs = query(sql);
            while (rs.next()) {
                monsterName = rs.getString("MonsterName");
            }
            closeStatement();
            disconnect();
        } catch (Exception e) {
            System.out.println(e);
        }
        return monsterName;
    }

    public void setupRoom(int roomNumber, int dungeonID) {
        String sql = "INSERT INTO room (RoomNumber, DungeonID) VALUES ('" + roomNumber + "', '" + dungeonID + "');";
        try {
            connect();
            command(sql);
            disconnect();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public int getRoomID(int roomNumber, int dungeonID) {
        String sql = "SELECT * FROM room WHERE RoomNumber = " + roomNumber + " AND DungeonID = " + dungeonID + ";";
        int roomID = -1;
        try {
            connect();
            ResultSet rs = query(sql);
            while (rs.next()) {
                roomID = rs.getInt("RoomID");
            }
            closeStatement();
            disconnect();
        } catch (Exception e) {
            System.out.println(e);
        }
        return roomID;
    }

    public Furniture getFurniture() {
        Random rand = new Random();
        Furniture frn = new Furniture();
        String sql = "SELECT FurnitureID, FurnitureName FROM furniture WHERE FurnitureID = " + (rand.nextInt(24) + 1);
        try {
            connect();
            ResultSet rs = query(sql);
            while (rs.next()) {
                frn.setID(rs.getInt("FurnitureID"));
                frn.setName(rs.getString("FurnitureName"));
            }
            closeStatement();
            disconnect();
        } catch (Exception e) {
            System.out.println(e);
        }
        return frn;
    }

    public void addPlayer(Player newPlayer) {
        String sql = "INSERT INTO player (PlayerName, PlayerLevel, PlayerClass) VALUES ('" + newPlayer.getName() + "', " + newPlayer.getLevel() + ", '" + newPlayer.getPlayerClass() + "')";
        try {
            connect();
            command(sql);
            disconnect();
            System.out.println("Player added!");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public ArrayList<Integer> getPlayersFromDungeon(int dungeonID){
        ArrayList<Integer> playerIDList = new ArrayList();
        String sql = "SELECT * FROM playerdungeonlink WHERE DungeonID = "+dungeonID;
        try {
            connect();
            ResultSet rs = query(sql);
            while (rs.next()) {
                playerIDList.add(rs.getInt("PlayerID"));
            }
            closeStatement();
            disconnect();
        } catch (Exception e) {
            System.out.println(e);
        }
        return playerIDList;
    }
    
    public ArrayList<String> getPlayerList() {
        ArrayList<String> playerList = new ArrayList();
        String sql = "SELECT PlayerName FROM player";
        try {
            connect();
            ResultSet rs = query(sql);
            while (rs.next()) {
                playerList.add(rs.getString("PlayerName"));
            }
            closeStatement();
            disconnect();
        } catch (Exception e) {
            System.out.println(e);
        }
        return playerList;
    }

    public Player loadPlayer(String playerName) {
        Player newPlayer = new Player();
        String sql = "SELECT PlayerName, PlayerLevel, PlayerClass FROM player WHERE PlayerName = '" + playerName + "';";
        try {
            connect();
            ResultSet rs = query(sql);
            while (rs.next()) {
                newPlayer.setName(rs.getString("PlayerName"));
                newPlayer.setLevel(rs.getInt("PlayerLevel"));
                newPlayer.setClass(rs.getString("PlayerClass"));
            }
            closeStatement();
            disconnect();
            System.out.println("Player added!");
        } catch (Exception e) {
            System.out.println(e);
        }
        return newPlayer;
    }

    public int getXpValue(String difficulty, int playerLevel) {
        int xpValue = 0;
        String sql = "SELECT * FROM playerxps WHERE PlayerLevelID = " + playerLevel;
        try {
            connect();
            ResultSet rs = query(sql);
            while (rs.next()) {
                xpValue = rs.getInt(difficulty + "XP");
            }
            closeStatement();
            disconnect();
        } catch (Exception e) {
            System.out.println(e);
        }
        return xpValue;
    }

    public ArrayList getMonsterList(int xpValue) {
        ArrayList<String> monsterList = new ArrayList();
        String sql = "SELECT * FROM monster WHERE MonsterXP = " + xpValue;
        try {
            connect();
            ResultSet rs = query(sql);
            while (rs.next()) {
                monsterList.add(rs.getString("MonsterName"));
            }
            closeStatement();
            disconnect();
        } catch (Exception e) {
            System.out.println(e);
        }
        return monsterList;
    }

    public void setDungeonName(String name) {
        String sql = "INSERT INTO dungeon (DungeonName) VALUES ('" + name + "')";
        try {
            connect();
            command(sql);
            disconnect();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public int getID(String name, String table) {
        String sql = "SELECT * FROM " + table + " WHERE (" + table + "Name) = '" + name + "'";
        int ID = -1;
        try {
            connect();
            ResultSet rs = query(sql);
            while (rs.next()) {
                ID = rs.getInt(table + "ID");
            }
            closeStatement();
            disconnect();
        } catch (Exception e) {
            System.out.println(e);
        }
        return ID;
    }

    public void setupPlayerDungeonLink(int playerID, int dungeonID) {
        String sql = "INSERT INTO playerdungeonlink (PlayerID, DungeonID) VALUES ('" + playerID + "', '" + dungeonID + "');";
        try {
            connect();
            command(sql);
            disconnect();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void setupFurnitureRoomLink(int furnitureID, int roomID) {
        String sql = "INSERT INTO furnitureroomlink (RoomID, FurnitureID) VALUES ('" + roomID + "', '" + furnitureID + "');";
        try {
            connect();
            command(sql);
            disconnect();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void setupMonsterRoomLink(int monsterID, int roomID) {
        String sql = "INSERT INTO monsterroomlink (RoomID, MonsterID) VALUES ('" + roomID + "', '" + monsterID + "');";
        try {
            connect();
            command(sql);
            disconnect();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void closeStatement() {
        try {
            stmt.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    //Runs an query with the given SQL that returns a result set
    private ResultSet query(String sql) {
        try {
            stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            return rs;
        } catch (SQLException e) {
            System.err.println("Query failed: " + e);
        }
        return null;
    }

    private void command(String sql) {
        try {
            stmt = connection.createStatement();
            stmt.execute(sql);
            closeStatement();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // disconnect database
    private void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
