
import java.util.ArrayList;
import java.util.Random;

public class SetupDungeon {

    private static String dungeonName;
    private static ArrayList<Player> playerList = new ArrayList();
    private static ArrayList<String> roomList = new ArrayList();

    public ArrayList<String> getRoomList() {
        return roomList;
    }

    public String getRoom(int i) {
        return roomList.get(i);
    }

    public String getDungeonName() {
        return dungeonName;
    }

    public void setDungeonName(String inputDungeonName) {
        dungeonName = inputDungeonName;
    }

    public ArrayList<Player> getPlayerList() {
        return playerList;
    }

    public void cleanPlayerList() {
        playerList.clear();
    }

    public void cleanRoomList() {
        roomList.clear();
    }

    public void addPlayerToList(Player player) {
        playerList.add(player);
    }

    public void createDungeon() {

        Random rand = new Random();
        DatabaseInteractions dbi = new DatabaseInteractions();
        int dungeonID;

        //setup dungeon info (name, id)
        dbi.setDungeonName(dungeonName);
        dungeonID = dbi.getDungeonID(dungeonName);

        //setup players
        setupPlayerDungeonLink(dungeonID);

        for (int i = 0; i < rand.nextInt(3) + 4; i++) {
            createRoom(i + 1, dungeonID);
        }

    }

    private void setupPlayerDungeonLink(int dungeonID) {
        //calls the method that links the players to the dungeon in the database
        DatabaseInteractions dbi = new DatabaseInteractions();
        int playerID;
        for (int i = 0; i < playerList.size(); i++) {
            playerID = dbi.getID(playerList.get(i).getName(), "Player");
            dbi.setupPlayerDungeonLink(playerID, dungeonID);
        }
    }

    private void createRoom(int roomNumber, int dungeonID) {

        //creates a room
        DatabaseInteractions dbi = new DatabaseInteractions();
        ArrayList monsterList = new ArrayList();

        String room;
        int roomID;

        //setup room
        dbi.setupRoom(roomNumber, dungeonID);
        roomID = dbi.getRoomID(roomNumber, dungeonID);

        //add furniture
        room = "FURNITURE: \n";
        room = room + createFurniture(roomID);

        //add monsters
        room = room + "\nMONSTERS: \n";
        monsterList = createMonsters(roomID);
        if (!monsterList.get(0).equals("No Monsters")) {
            monsterList = checkMonsterList(monsterList);
        }
        for (int i = 0; i < monsterList.size(); i++) {
            room = room + monsterList.get(i) + "\n";
        }
        roomList.add(room);

    }

    private ArrayList<String> checkMonsterList(ArrayList<String> monsterList) {
        //removes duplicate monsters by adding the number of them instead
        int counter;
        int duplicates;
        ArrayList<Integer> grammarNeeded = new ArrayList();
        for (int i = 0; i < monsterList.size(); i++) {
            grammarNeeded.add(i);
        }
        for (int i = 0; i < monsterList.size() - 1; i++) {
            counter = i + 1;
            duplicates = 1;
            while (counter < monsterList.size()) {
                if (monsterList.get(i).equals(monsterList.get(counter))) {
                    duplicates++;
                }
                monsterList.set(counter, i + counter + "");
                counter++;
            }
            if (duplicates > 1) {
                monsterList.set(i, duplicates + " " + monsterList.get(i) + "s");
                grammarNeeded.remove(i);
            }
        }
        //find the items in the list to remove
        int[] indeciesToRemove = new int[monsterList.size()];
        counter = 0;
        for (int i = 0; i < monsterList.size(); i++) {
            if (monsterList.get(i).matches("-?\\\\d+")) {
                indeciesToRemove[counter] = i;
                counter++;
            }
        }
        //remove the specified items in the list
        for (int i = 0; i < counter; i++) {
            monsterList.remove(indeciesToRemove[counter]);
        }
        monsterList = checkGrammar(monsterList, grammarNeeded);
        return monsterList;
    }

    private ArrayList<String> checkGrammar(ArrayList<String> monsterList, ArrayList<Integer> grammarNeeded) {
        //adds "A" or "An" to the start of singular monsters as required
        for (int i = 0; i < grammarNeeded.size(); i++) {
            monsterList.set(grammarNeeded.get(i), "A" + vowelHelper(monsterList.get(grammarNeeded.get(i))) + monsterList.get(grammarNeeded.get(i)));
        }
        return monsterList;
    }

    private String createFurniture(int roomID) {
        //randomly generates the furniture that goes into a certain room
        DatabaseInteractions dbi = new DatabaseInteractions();
        Random rand = new Random();
        Furniture tempFrn;
        String furniture = "";

        for (int i = 0; i < rand.nextInt(3) + 2; i++) {
            tempFrn = dbi.getFurniture();
            dbi.setupFurnitureRoomLink(tempFrn.getID(), roomID);
            furniture = furniture + "A" + vowelHelper(tempFrn.getName()) + " " + tempFrn.getName() + "\n";
        }
        return furniture;
    }

    private ArrayList<String> createMonsters(int roomID) {
        //determines whether a room has monsters in it or not
        Random rand = new Random();
        ArrayList<String> monsterList = new ArrayList();
        if (rand.nextInt(2) == 0) {
            monsterList = addMonsters(roomID);
        } else {
            monsterList.add("No Monsters");
        }
        return monsterList;
    }

    private ArrayList<String> addMonsters(int roomID) {
        //the main method for adding a monster encounter
        int totalMonsterXP = 0, count = 0, tempCount, monsterCount = 0;
        boolean addedMonster = true;
        double[] multipliers = setupMultiplier();
        int multiplierModifier = setupMultiplierModifier(playerList.size());
        int[] monsterXPs = setupMonsterValues();
        int encounterXpValue = getEncounterXpValue(playerList);
        String monsterName;
        ArrayList<String> monsters = new ArrayList();

        //tempCount = highest count possible for monster
        //higher the count (up to 27), weaker the monster
        tempCount = getMaxMonster(encounterXpValue, monsterXPs);
        do {
            do {
                if (encounterXpValue < (monsterXPs[checkValid(tempCount + 5)] + totalMonsterXP) * (getMultiplier(multipliers, monsterCount) + multiplierModifier)) {
                    addedMonster = false;
                    break;
                }
                count = getSingleMonsterCountValue(tempCount);
            } while (encounterXpValue < (monsterXPs[count] + totalMonsterXP) * (getMultiplier(multipliers, monsterCount) + multiplierModifier));
            if (addedMonster) {
                totalMonsterXP = totalMonsterXP + monsterXPs[count];
                monsterName = getMonster(monsterXPs[count], roomID);
                monsters.add(monsterName);
                monsterCount++;
            }
        } while (addedMonster);
        return monsters;
    }

    private String getMonster(int xpValue, int roomID) {
        //randomly selects a monster from the randomly selected xpValue and prints the name out
        ArrayList<String> monsterList;
        String singleMonster = "";

        DatabaseInteractions dbi = new DatabaseInteractions();
        monsterList = dbi.getMonsterList(xpValue);
        singleMonster = getRandomMonster(monsterList);
        int monsterID = dbi.getID(singleMonster, "Monster");
        dbi.setupMonsterRoomLink(monsterID, roomID);

        return singleMonster;
    }

    private String getRandomMonster(ArrayList<String> monsterList) {
        //gets a random monster from the list of names
        Random rand = new Random();
        return monsterList.get(rand.nextInt(monsterList.size()));
    }

    private int getSingleMonsterCountValue(int tempCount) {
        //gets the xp level for a single monster
        Random rand = new Random();
        tempCount = tempCount + rand.nextInt(6);
        tempCount = checkValid(tempCount);
        return tempCount;
    }

    private double getMultiplier(double[] multipliers, int monsterCount) {
        //works out the multiplier that makes an encounter fair based on the amount of monsters in the encounter
        double multiplier;
        if (monsterCount < 16) {
            multiplier = multipliers[monsterCount];
        } else {
            multiplier = 4;
        }
        return multiplier;
    }

    private int checkValid(int check) {
        //simple check to make sure array errors do not occour
        if (check > 27) {
            check = 27;
        }
        return check;
    }

    private int getMaxMonster(int xpValue, int[] monsterXPs) {
        //gets what the hardest single monster the encounter could be
        int count = 0;
        while (xpValue < monsterXPs[count]) {
            count++;
        }
        return count;
    }

    private String getEncounterDifficulty() {
        //selects a random difficulty for the encounter
        Random rand = new Random();
        int random = rand.nextInt(7);
        String difficulty = "";
        switch (random) {
            case 0:
            case 1:
            case 2:
            case 3:
                difficulty = "Medium";
                break;
            case 4:
            case 5:
                difficulty = "Hard";
                break;
            case 6:
                difficulty = "Deadly";
        }
        return difficulty;
    }

    private int getEncounterXpValue(ArrayList<Player> players) {
        //gets the XP budget for the encounter
        int totalXpValue = 0, playerCount = players.size(), tempCount = 0;
        String difficulty = getEncounterDifficulty();
        DatabaseInteractions dbi = new DatabaseInteractions();
        do {
            totalXpValue = totalXpValue + dbi.getXpValue(difficulty, (players.get(tempCount)).getLevel());
            tempCount++;
        } while (tempCount < playerCount);
        return totalXpValue;
    }

    private int[] setupMonsterValues() {

        //Sets up the different XP values of all the monsters
        int[] monsterXps = new int[28];
        monsterXps[0] = 155000;
        monsterXps[1] = 62000;
        monsterXps[2] = 50000;
        monsterXps[3] = 41000;
        monsterXps[4] = 33000;
        monsterXps[5] = 25000;
        monsterXps[6] = 22000;
        monsterXps[7] = 18000;
        monsterXps[8] = 15000;
        monsterXps[9] = 13000;
        monsterXps[10] = 11500;
        monsterXps[11] = 10000;
        monsterXps[12] = 8400;
        monsterXps[13] = 7200;
        monsterXps[14] = 5900;
        monsterXps[15] = 5000;
        monsterXps[16] = 3900;
        monsterXps[17] = 2900;
        monsterXps[18] = 2300;
        monsterXps[19] = 1800;
        monsterXps[20] = 1100;
        monsterXps[21] = 700;
        monsterXps[22] = 450;
        monsterXps[23] = 200;
        monsterXps[24] = 100;
        monsterXps[25] = 50;
        monsterXps[26] = 25;
        monsterXps[27] = 10;

        return monsterXps;
    }

    private int setupMultiplierModifier(int playerCount) {
        //works out the modifier to make balanced encounters based on the number of players.
        int modifier = 0;
        if (playerCount < 3) {
            modifier = 1;
        } else if (playerCount > 5) {
            modifier = -1;
        }
        return modifier;
    }

    private double[] setupMultiplier() {

        //sets up the array which plays a part in adding monsters to an encounter
        double[] multipliers = new double[15];
        double count = 2;
        multipliers[0] = 0.5;
        multipliers[1] = 1;
        multipliers[2] = 1.5;
        for (int i = 3; i < 7; i++) {
            multipliers[i] = 2;
        }
        for (int i = 7; i < 11; i++) {
            multipliers[i] = 2.5;
        }
        for (int i = 11; i < 15; i++) {
            multipliers[i] = 3;
        }

        return multipliers;
    }

    public void loadRooms(int dungeonID) {
        //load up the rooms from a previously made dungeon
        DatabaseInteractions dbi = new DatabaseInteractions();
        String room, temp;
        ArrayList<Integer> roomIDs = dbi.getRoomIDs(dungeonID);
        ArrayList<Integer> furnitureIDs, monsterIDs;
        for (int i = 0; i < roomIDs.size(); i++) {

            room = "FURNITURE: \n";
            room = room + loadFurniture(roomIDs.get(i));

            room = room + "\nMONSTERS: \n";
            room = room + loadMonsters(roomIDs.get(i));

            roomList.add(room);

        }

    }

    private String loadFurniture(int roomNumber) {
        //load up the furniture from a previously made dungeon
        DatabaseInteractions dbi = new DatabaseInteractions();
        String temp, room = "";
        ArrayList<Integer> furnitureIDs;
        furnitureIDs = dbi.getFurnitureIDs(roomNumber);
        for (int j = 0; j < furnitureIDs.size(); j++) {
            temp = dbi.getFurnitureName(furnitureIDs.get(j));
            temp = "A" + vowelHelper(temp) + " " + temp;
            room = room + temp + "\n";
        }
        return room;
    }

    private String loadMonsters(int roomNumber) {
        //load up the monsters from a previously made dungeon
        DatabaseInteractions dbi = new DatabaseInteractions();
        String temp, room = "";
        ArrayList<Integer> monsterIDs;
        monsterIDs = dbi.getMonsterIDs(roomNumber);
        if (!monsterIDs.isEmpty()) {
            for (int j = 0; j < monsterIDs.size(); j++) {
                temp = dbi.getMonsterName(monsterIDs.get(j));
                temp = "A" + vowelHelper(temp) + " " + temp;
                room = room + temp + "\n";
            }
        } else {
            room = room + "No Monsters";
        }
        return room;
    }

    private String vowelHelper(String line) {
        //checks to see if the a string begins with a vowel, and if so, returns an "n" so the correct grammar is used
        String helper = "", vowels = "AEIOU";
        if (vowels.contains(String.valueOf(line.charAt(0)))) {
            helper = "n";
        }
        return helper;
    }

}
