public class Interpreter {
    private ControlUnit controller=null;
    public void setController(ControlUnit controller) {
        this.controller = controller;
        alphabet.setController(controller);
    }
    private void pushMessage(String s) {
        if(controller!=null)
            controller.pushMessage(s);
        else
            System.out.println("[Interpreter] "+s);
    }
    private void pushError(String s) {
        if(controller!=null)
            controller.pushError(s);
        else
            System.err.println("[Interpreter] "+s);
    }

    private Alphabet alphabet;
    private String text="", morseCode="", signalCode="";
    public final static Character SIGNAL_HIGH='=', SIGNAL_LOW='_';
    //List of special commands
    // dedicated class: id, morsCode, text. always written in [text]

    Interpreter() {
        alphabet = new Alphabet();
    }
    Interpreter(ControlUnit controller) {
        alphabet = new Alphabet(controller);
        this.controller = controller;
    }

    public void setAlphabet(Alphabet a) {
        alphabet = a;
    }
    public void showAlphabet() {
        alphabet.showAlphabet();
    }

    //TODO: input validation
    public void setText(String text) {
        this.text = text.toUpperCase();
        //in case of adding special commands:
        //input must be checked about existence special commands
        //if text in [] isn't command, change it on ()
        //System.out.println("Setting text: "+text);
        textToMorse();
        morseToSignal();
    }
    public void setMorseCode(String morseCode) {
        this.morseCode = validateMorseCode(morseCode);
        //System.out.println("Setting morseCode: "+morseCode);
        morseToText();
        morseToSignal();
    }
    public void setSignalCode(String signalCode) {
        this.signalCode = validateSignalCode(signalCode);
        //System.out.println("Setting signalCode: "+signalCode);
        signalToMorse();
        morseToText();
    }

    public String getText() {
        return text;
    }
    public String getMorseCode() {
        return morseCode;
    }
    public String getSignalCode() {
        return signalCode;
    }

    public String validateMorseCode(String morseCode) {
        StringBuilder s = new StringBuilder(morseCode);
        char c;
        for (int i = 0; i < s.length(); i++) {
            c = s.charAt(i);
            if(c!=AlphabetPair.SHORT_SIGN && c!=AlphabetPair.LONG_SIGN && c!=AlphabetPair.SHORT_GAP) s.deleteCharAt(i);
        }
        return s.toString();
    }
    public String validateSignalCode(String signalCode) {
        StringBuilder s = new StringBuilder(signalCode);

        String[] errorSought={
                SIGNAL_HIGH+ SIGNAL_LOW.toString().repeat(2) +SIGNAL_HIGH,
                SIGNAL_HIGH+ SIGNAL_LOW.toString().repeat(6) +SIGNAL_HIGH,
                SIGNAL_LOW+ SIGNAL_HIGH.toString().repeat(2) +SIGNAL_LOW,
                SIGNAL_LOW+ SIGNAL_HIGH.toString().repeat(4) +SIGNAL_LOW};
        String[] fixed={
                SIGNAL_HIGH+ SIGNAL_LOW.toString().repeat(3) +SIGNAL_HIGH,
                SIGNAL_HIGH+ SIGNAL_LOW.toString().repeat(7) +SIGNAL_HIGH,
                SIGNAL_LOW+ SIGNAL_HIGH.toString().repeat(3) +SIGNAL_LOW,
                SIGNAL_LOW+ SIGNAL_HIGH.toString().repeat(3) +SIGNAL_LOW};


        char c;
        int i, index;
        for (i = 0; i < s.length(); i++) {
            c = s.charAt(i);
            if(c!=SIGNAL_HIGH && c!=SIGNAL_LOW) s.deleteCharAt(i);
        }

        for (int j = 0; j < errorSought.length; j++) {
            i=0;
            while(i<s.length()) {
                index = s.indexOf(errorSought[j], i);
                if(index>=0) {
                    s.replace(index, index+errorSought[j].length(), fixed[j]);
                    i=index+errorSought[j].length();
                } else break;
            }
        }
        index = s.indexOf(SIGNAL_HIGH.toString());
        if (index>=0) s.delete(0, index);
        index = s.lastIndexOf(SIGNAL_HIGH.toString());
        if (index>=0) s.delete(index+1, s.length());

        return s.toString();
    }
    private void textToMorse() {
        StringBuilder wynik = new StringBuilder();
        char c;
        String outcome;
        for (int i = 0; i < text.length(); i++) {
            c=text.charAt(i);
            //if c=='[' -> find ], and check text is a special command, if is get that morse code, and i = indexOf ] +1
            if(Character.isWhitespace(c)) wynik.append(AlphabetPair.MEDIUM_GAP);
            else {
                if(wynik.length()>0 && wynik.charAt(wynik.length()-1)!=AlphabetPair.SHORT_GAP)
                    wynik.append(AlphabetPair.SHORT_GAP);
                outcome = alphabet.findMorseCode(c);
                if(outcome!=null) wynik.append(outcome);
            }
        }
        morseCode = wynik.toString();
    }
    private void morseToText() {
        StringBuilder wynik = new StringBuilder();
        char c;
        Character outcome;
        int i=0, l, len = morseCode.length();
        while (i<len) {
            c=morseCode.charAt(i);
            l=i;
            if (AlphabetPair.isCharacterOfMorseCode(c)) {
                while(l<len && AlphabetPair.isCharacterOfMorseCode(morseCode.charAt(l))) l++;
                //check is it a command or not
                outcome=alphabet.findCharacter(morseCode.substring(i,l));
                if(outcome!=null) wynik.append(outcome);
            } else if (AlphabetPair.isShortGap(c)) {
                l++;
                if (l<len && AlphabetPair.isShortGap(morseCode.charAt(l)))
                    wynik.append(" ");
            }
            i=l;
        }
        text = wynik.toString();
    }
    private void morseToSignal() {
        /*
        kropka to jedna jednostka dlugosci
        kreska to trzy jednostki
        odstep miedzy elementami znaku jedna jednostka
        odstep miedzy znakami trzy jednostki (w morsie jedna spacja)
        odstep miedzy slowami siedem jednostek (w morsie dwie spacje)
         */
        StringBuilder wynik = new StringBuilder();
        String SIGNAL_LOW_3_TIMES=SIGNAL_LOW.toString().repeat(3), SIGNAL_HIGH_3_TIMES=SIGNAL_HIGH.toString().repeat(3);
        char c;
        for (int i = 0; i < morseCode.length(); i++) {
            c=morseCode.charAt(i);

            if(c==AlphabetPair.SHORT_GAP) { //aktualnie jest spacja
                wynik.append(SIGNAL_LOW_3_TIMES); //kazda spacja to 3 low
                if(i>0 && morseCode.charAt(i-1)==AlphabetPair.SHORT_GAP)
                    wynik.append(SIGNAL_LOW); //jesli to druga spacja musi byc 1 wiecej low
            } else { //aktualnie jest jakis znak
                if(i>0 && morseCode.charAt(i-1)!=AlphabetPair.SHORT_GAP) //poprzednio tez byl znak
                    wynik.append(SIGNAL_LOW); //to trzeba oddzielic (wiec 1 low)
                if(c==AlphabetPair.SHORT_SIGN)
                    wynik.append(SIGNAL_HIGH);
                else if(c==AlphabetPair.LONG_SIGN)
                    wynik.append(SIGNAL_HIGH_3_TIMES);
            }
        }
        signalCode = wynik.toString();
    }
    private void signalToMorse() {
        StringBuilder wynik = new StringBuilder();
        char c;
        int i=0, l, len=signalCode.length();
        while(i<len) {
            c=signalCode.charAt(i);
            l=1;
            while(i+l<len && signalCode.charAt(i+l)==c) l++;
            if (c==SIGNAL_HIGH) {
                if (l==3) wynik.append(AlphabetPair.LONG_SIGN);
                else wynik.append(AlphabetPair.SHORT_SIGN);
            } else { //najprawdopodobniej signal low
                if (l==3) wynik.append(AlphabetPair.SHORT_GAP);
                else if (l>=7) wynik.append(AlphabetPair.MEDIUM_GAP);
            }
            i+=l;
        }
        morseCode = wynik.toString();
    }
}
