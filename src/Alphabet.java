import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Alphabet {
    private ControlUnit controller=null;
    public void setController(ControlUnit controller) {
        this.controller = controller;
    }
    private void pushMessage(String s) {
        if(controller!=null)
            controller.pushMessage(s);
        else
            System.out.println("[Alphabet] "+s);
    }
    private void pushError(String s) {
        if(controller!=null)
            controller.pushError(s);
        else
            System.err.println("[Alphabet] "+s);
    }

    private List<AlphabetPair> alphabetList = new ArrayList<>();

    private final static String INTERNAL_SEPARATOR=" ", NEXT_PAIR_SEPARATOR ="\n";
    //private String alphabetFilePath = getClass().getResource("alfabetMorsa.txt").toExternalForm();
    //private String alphabetFilePath = "resources/alfabetMorsa.txt";

    Alphabet() {
        //readAlphabetFromFile();
        loadDefaultAlphabet();
    }
    Alphabet(ControlUnit controller) {
        setController(controller);
        //readAlphabetFromFile();
        loadDefaultAlphabet();
    }

    /*
    public URL getAlphabetFilePath() {
        return alphabetFilePath;
    }
    public void setAlphabetFilePath(URL alphabetFilePath) {
        this.alphabetFilePath = alphabetFilePath;
        alphabetList = new ArrayList<>();
        readAlphabetFromFile();
    }*/

    public void setAlphabetList(List<AlphabetPair> alphabetList) {
        this.alphabetList = new ArrayList<>(alphabetList);
    }

    public String findMorseCode(Character searched) {
        for (AlphabetPair elem: alphabetList) {
            if (elem.getCharacter().equals(searched)) return elem.getMorseCode();
        }
        pushError("Letter not found: "+searched);
        return null;
    }
    public Character findCharacter(String searched) {
        for (AlphabetPair elem: alphabetList) {
            if (elem.getMorseCode().equals(searched)) return elem.getCharacter();
        }
        //pushError("Morse code not found: "+searched);
        return null;
    }

    private boolean findDuplicate(AlphabetPair searched) {
        for (AlphabetPair elem: alphabetList) {
            if(elem.partialSame(searched)) {
                pushError("Duplicate found: "+searched.getCharacter()+" "+searched.getMorseCode());
                return true;
            }
        }
        return false;
    }

    public boolean addAlphabetPair(AlphabetPair elem) {
        if(!findDuplicate(elem) && elem.isValid()) {
            alphabetList.add(elem);
            return true;
        }
        pushError("Can't add this elem to alphabet: "+elem.getCharacter()+" "+elem.getMorseCode());
        return false;
    }
    public void loadDefaultAlphabet() {
        addAlphabetPair(new AlphabetPair('A', "•-"));
        addAlphabetPair(new AlphabetPair('B', "-•••"));
        addAlphabetPair(new AlphabetPair('C', "-•-•"));
        addAlphabetPair(new AlphabetPair('D', "-••"));
        addAlphabetPair(new AlphabetPair('E', "•"));
        addAlphabetPair(new AlphabetPair('F', "••-•"));
        addAlphabetPair(new AlphabetPair('G', "--•"));
        addAlphabetPair(new AlphabetPair('H', "••••"));
        addAlphabetPair(new AlphabetPair('I', "••"));
        addAlphabetPair(new AlphabetPair('J', "•---"));
        addAlphabetPair(new AlphabetPair('K', "-•-"));
        addAlphabetPair(new AlphabetPair('L', "•-••"));
        addAlphabetPair(new AlphabetPair('M', "--"));
        addAlphabetPair(new AlphabetPair('N', "-•"));
        addAlphabetPair(new AlphabetPair('O', "---"));
        addAlphabetPair(new AlphabetPair('P', "•--•"));
        addAlphabetPair(new AlphabetPair('Q', "--•-"));
        addAlphabetPair(new AlphabetPair('R', "•-•"));
        addAlphabetPair(new AlphabetPair('S', "•••"));
        addAlphabetPair(new AlphabetPair('T', "-"));
        addAlphabetPair(new AlphabetPair('U', "••-"));
        addAlphabetPair(new AlphabetPair('V', "•••-"));
        addAlphabetPair(new AlphabetPair('W', "•--"));
        addAlphabetPair(new AlphabetPair('X', "-••-"));
        addAlphabetPair(new AlphabetPair('Y', "-•--"));
        addAlphabetPair(new AlphabetPair('Z', "--••"));
        addAlphabetPair(new AlphabetPair('Ą', "•-•-"));
        addAlphabetPair(new AlphabetPair('Ć', "•-•••"));
        addAlphabetPair(new AlphabetPair('Ę', "••-••"));
        addAlphabetPair(new AlphabetPair('Ł', "•-••-"));
        addAlphabetPair(new AlphabetPair('Ń', "--•--"));
        addAlphabetPair(new AlphabetPair('Ó', "---•"));
        addAlphabetPair(new AlphabetPair('Ś', "•••-•••"));
        addAlphabetPair(new AlphabetPair('Ź', "--••-•"));
        addAlphabetPair(new AlphabetPair('Ż', "--••-"));
        addAlphabetPair(new AlphabetPair('1', "•----"));
        addAlphabetPair(new AlphabetPair('2', "••---"));
        addAlphabetPair(new AlphabetPair('3', "•••--"));
        addAlphabetPair(new AlphabetPair('4', "••••-"));
        addAlphabetPair(new AlphabetPair('5', "•••••"));
        addAlphabetPair(new AlphabetPair('6', "-••••"));
        addAlphabetPair(new AlphabetPair('7', "--•••"));
        addAlphabetPair(new AlphabetPair('8', "---••"));
        addAlphabetPair(new AlphabetPair('9', "----•"));
        addAlphabetPair(new AlphabetPair('0', "-----"));
        addAlphabetPair(new AlphabetPair('.', "•-•-•-"));
        addAlphabetPair(new AlphabetPair(',', "--••--"));
        addAlphabetPair(new AlphabetPair('\'', "•---•"));
        addAlphabetPair(new AlphabetPair('\"', "•-••-•"));
        addAlphabetPair(new AlphabetPair(':', "---•••"));
        addAlphabetPair(new AlphabetPair(';', "-•-•-•"));
        addAlphabetPair(new AlphabetPair('?', "••--••"));
        addAlphabetPair(new AlphabetPair('!', "-•-•--"));
        addAlphabetPair(new AlphabetPair('-', "-••••-"));
        addAlphabetPair(new AlphabetPair('+', "•-•-•"));
        addAlphabetPair(new AlphabetPair('/', "-••-•"));
        addAlphabetPair(new AlphabetPair('(', "-•--•"));
        addAlphabetPair(new AlphabetPair(')', "-•--•-"));
        addAlphabetPair(new AlphabetPair('=', "-•••-"));

    }
/*
    public void readAlphabetFromFile() {
        this.readAlphabetFromFile(this.alphabetFilePath);
    }
    public void readAlphabetFromFile(String path) {
        Scanner sc;
        StringBuilder wynik = new StringBuilder("");
        try {
            sc = new Scanner(new File(path));
            while(sc.hasNextLine()) wynik.append(sc.nextLine()).append("\n");
        } catch (FileNotFoundException e) {
            pushError("File with alphabet is unavailable");
            e.printStackTrace();
        }
        int i=0, separatorIndex, endIndex;
        while(i<wynik.length()) {
            separatorIndex = wynik.indexOf(INTERNAL_SEPARATOR,i);
            endIndex = wynik.indexOf(NEXT_PAIR_SEPARATOR,separatorIndex);
            if(separatorIndex<0 || endIndex<0) break;
            if(!addAlphabetPair(new AlphabetPair(wynik.charAt(separatorIndex-1), wynik.substring(separatorIndex+1,endIndex))))
                pushError("Can't add this element to alphabet: "+wynik.charAt(separatorIndex-1)+" "+wynik.substring(separatorIndex+1,endIndex));
            i=endIndex;
        }
    }

 */

    public void showAlphabet() {
        StringBuilder s = new StringBuilder("Alphabet: ");
        for (AlphabetPair elem: alphabetList) {
            s.append("\n").append(elem.getCharacter()).append(" ").append(elem.getMorseCode());
        }
        pushMessage(s.toString());
    }
}
