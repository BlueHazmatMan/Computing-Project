
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public class TempPlayerInteractions {
    
    private String filename = "tempPlayers.txt";
    
    public boolean checkPlayerNotInParty(String playerName) {
        ArrayList<String> playerList = getPlayerList();
        if (playerList.contains(playerName)) {
            return false;
        }
        return true;
    }
    
    public ArrayList<String> getPlayerList() {
        ArrayList<String> playerList = new ArrayList();
        String currentLine;
        try {
            FileReader fr = new FileReader(filename);
            BufferedReader br = new BufferedReader(fr);
            
            while (!(currentLine = br.readLine()).isEmpty()) {
                if (!currentLine.equals("")) {
                    playerList.add(currentLine);
                }
            }
            br.close();
            
        } catch (Exception e) {
            System.out.println(e);
        }
        return playerList;
    }
    
    public void addPlayerToTemp(String playerName) {
        try {
            FileWriter fw = new FileWriter(filename, true);
            BufferedWriter bw = new BufferedWriter(fw);
            
            bw.write(playerName);
            bw.newLine();
            bw.close();
            
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    
    public void wipeTempPlayers() {
        try {
            FileWriter fw = new FileWriter(filename);
            BufferedWriter bw = new BufferedWriter(fw);
            
            bw.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
