public class ControlUnit {
    private Interpreter interpreter;
    private SoundControl soundControl;
    private GraphicalInterface gui;
    //there can be also custom settings about morse code chars, alfabet, using of special commands

    ControlUnit() {
        gui = new GraphicalInterface(this);
        gui.disableSoundModule(true);
        gui.miSpinnersWPM.doClick();
        setProgressBarVal(0);
        setProgressBarMax(100);

        interpreter = new Interpreter(this);
        soundControl = new SoundControl(this);
        gui.setSpinnerVolume(30);
        gui.show();
    }

    public void setText(String text) {
        interpreter.setText(text);
        gui.setResults();
    }
    public String getText() { return interpreter.getText(); }

    public void setMorseCode(String text) {
        interpreter.setMorseCode(text);
        gui.setResults();
    }
    public String getMorseCode() { return interpreter.getMorseCode(); }

    public void setSignalCode(String text) {
        interpreter.setSignalCode(text);
        gui.setResults();
    }
    public String getSignalCode() { return interpreter.getSignalCode(); }

    public boolean isCharactersOfAlphabet(String s) {
        return interpreter.getAlphabet().isCharactersOfAlphabet(s);
    }
    public void disablePlayButtonSound(boolean disabled) {
        gui.disablePlayButtonSound(disabled);
    }
    public void disableListenButtonSound(boolean disabled) {
        gui.disableListenButtonSound(disabled);
    }
    public void disableStopButtonSound(boolean disabled) {
        gui.disableStopButtonSound(disabled);
    }
    public void disableSoundModule(boolean disabled) {
        gui.disableSoundModule(disabled);
    }
    public void disableTextAreas(boolean disabled) {
        gui.disableTextAreas(disabled);
    }
    public void disableSpinnerVolume(boolean disabled) {
        gui.disableSpinnerVolume(disabled);
    }
    public void disableSpinnerUnitLengthAndWpm(boolean disabled) {
        gui.disableSpinnersUnitLength(disabled);
    }
    public void setProgressBarVal(int val) {
        gui.setProgressBarVal(val);
    }
    public void setProgressBarMax(int max) {
        gui.setProgressBarMax(max);
    }
    public void setProgressBarStringPainted(boolean stringPainted) {
        gui.setProgressBarStringPainted(stringPainted);
    }

    public void playSound() {
        //TODO: check that there is any morse code to play
        disableListenButtonSound(true);
        disablePlayButtonSound(true);
        disableStopButtonSound(false);
        disableTextAreas(true);
        soundControl.play();
    }
    public void listenSound() {
        disablePlayButtonSound(true);
        disableListenButtonSound(true);
        disableStopButtonSound(false);
        disableTextAreas(true);
        disableSpinnerVolume(true);
        disableSpinnerUnitLengthAndWpm(true);
        soundControl.listen();
    }
    public void stopSound() {
        soundControl.stop();
        disablePlayButtonSound(false);
        disableListenButtonSound(false);
        disableStopButtonSound(true);
        disableTextAreas(false);
        disableSpinnerVolume(false);
        disableSpinnerUnitLengthAndWpm(false);
        setProgressBarStringPainted(false);
        setProgressBarVal(0);
        setProgressBarMax(100);
    }
    public void volumeChange(int vol) {
        soundControl.setVolume(vol);
    }
    public void unitLengthChange(int basicLength, int gapLength) {
        soundControl.setUnitLength(basicLength, gapLength);
    }
    public void setSpinnerUnitLength(int basicLength, int gapLength) {
        gui.setSpinnerUnitLength(basicLength, gapLength);
    }

    public void pushMessage(String s) {
        if(gui!=null)
            gui.showMessage(s);
        else
            System.out.println("[ControlUnit] "+s);
    }
    public void pushError(String s) {
        if(gui!=null)
            gui.showError(s);
        else
            System.err.println("[ControlUnit] "+s);
    }

}
