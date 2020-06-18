public class AlphabetPair {
    private Character character;
    private String morseCode;
    public final static Character SHORT_SIGN = '•', LONG_SIGN = '-', SHORT_GAP = ' ';
    public final static String MEDIUM_GAP = SHORT_GAP.toString().repeat(2);

    AlphabetPair() {
        this.character = null;
        this.morseCode = null;
    }
    AlphabetPair(Character character, String morseCode) {
        this.character = character;
        this.morseCode = morseCode;
    }

    public Character getCharacter() {
        return character;
    }

    public String getMorseCode() {
        return morseCode;
    }

    public boolean partialSame(AlphabetPair el) {
        return character.equals(el.character) || morseCode.equals(el.morseCode);
    }
    public static boolean isValidCharacter(Character c) {
        return c!=null && Character.isDefined(c) && !Character.isISOControl(c); //iso control: \t \n
    }
    public static boolean isCharacterOfMorseCode(Character m) {
        return m.equals(SHORT_SIGN) || m.equals(LONG_SIGN);
    }
    public static boolean isShortGap(Character c) {
        return c.equals(SHORT_GAP);
    }
    public static boolean isValidMorseCodeSign(String m) {
        if(m.isEmpty()) return false;
        for (int i=0; i<m.length(); i++) {
            if(!isCharacterOfMorseCode(m.charAt(i))) return false;
        }
        return true;
    }
    public static boolean isValidMorseCode(String m) {
        if(m.isEmpty()) return false;
        char c;
        for (int i=0; i<m.length(); i++) {
            c = m.charAt(i);
            if(!isCharacterOfMorseCode(c) && !isShortGap(c)) return false;
        }
        return true;
    }
    public boolean isValid() {
        return isValidCharacter(this.character) && isValidMorseCodeSign(this.morseCode);
    }

    public boolean setCharacter(Character character) {
        if(isValidCharacter(character)) {
            this.character = character;
            return true;
        } else {
            System.err.println("Wrong character: "+character);
            return false;
        }
    }
    public boolean setMorseCode(String morseCode) {
        if(isValidMorseCodeSign(morseCode)) {
            this.morseCode = morseCode;
            return true;
        } else {
            System.err.println("Wrong morse code: "+morseCode);
            return false;
        }
    }
}
