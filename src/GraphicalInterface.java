import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.*;


public class GraphicalInterface implements ActionListener, ChangeListener, DocumentListener {
    private ControlUnit controller=null;
    public void setController(ControlUnit controller) {
        this.controller = controller;
    }

    JFrame frame;
    JPanel panel;
    JMenuBar menuBar;
    JMenu mSettings;
    JLabel labSpeedUnit;
    ButtonGroup bgSpeedUnits;
    JRadioButtonMenuItem miSpinnersMS, miSpinnersWPM;
    JLabel labText, labMorse, labVolume, labUnitLength, labWpm;
    JTextArea taText, taMorse;
    JScrollPane spText, spMorse;
    JButton butStartPlay, butStartListen, butStop;
    JSpinner spinVolume, spinUnitLength, spinWpm;
    JProgressBar progressBar;
    Font font;
    int gap=20, smallHeight=40, bigHeight=150, maxWidth=500;
    int oneThirdWidth = (maxWidth-2*gap)/3;
    int tempX, tempY;

    boolean showSignalCode = true;
    boolean avoidLoopedTextInputEvents = false;
    JLabel lSignal;
    JTextArea taSignal;
    JScrollPane spSignal;

    GraphicalInterface() {
        initialize();
    }
    GraphicalInterface(ControlUnit controller) {
        setController(controller);
        initialize();
    }
    public void show() {
        frame.setVisible(true);
    }
    public void hide() {
        frame.setVisible(false);
    }
    public void initialize() {
        frame = new JFrame("Translator kodu morsa");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //initialization
        panel = new JPanel(null);
        menuBar = new JMenuBar();
        mSettings = new JMenu("Ustawienia");
        labSpeedUnit = new JLabel("Jednostka prędkości kodu morsa:");
        bgSpeedUnits = new ButtonGroup();
        miSpinnersMS = new JRadioButtonMenuItem("Milisekundy");
        miSpinnersWPM = new JRadioButtonMenuItem("Słowa na minutę");
        labText = new JLabel("Tekst (znaki alfanumeryczne):");
        labMorse = new JLabel("Kod morsa (znaki "+AlphabetPair.LONG_SIGN+" i "+AlphabetPair.SHORT_SIGN+"):");
        labVolume = new JLabel("Głośność: ");
        labUnitLength = new JLabel("Długość jednostki (ms)");
        labWpm = new JLabel("Słów na minutę: ");
        taText = new JTextArea();
        taMorse = new JTextArea();
        spText = new JScrollPane(taText);
        spMorse = new JScrollPane(taMorse);
        spinVolume = new JSpinner(new SpinnerNumberModel(50, 0, 100,1));
        spinUnitLength = new JSpinner(new SpinnerNumberModel(60, 1, 1200,1));
        spinWpm = new JSpinner(new SpinnerNumberModel(20, 1, 1200,1));
        progressBar = new JProgressBar(JProgressBar.HORIZONTAL);
        butStartPlay = new JButton("Odtwórz");
        butStartListen = new JButton("Słuchaj");
        butStop = new JButton("Stop");
        font = new Font(Font.MONOSPACED, Font.PLAIN, 15);

        //building menu
        menuBar.add(mSettings);
        //miSpeedUnit.setD(false);
        mSettings.add(labSpeedUnit);
        miSpinnersMS.setSelected(true);
        bgSpeedUnits.add(miSpinnersMS);
        bgSpeedUnits.add(miSpinnersWPM);
        mSettings.add(miSpinnersMS);
        mSettings.add(miSpinnersWPM);

        //options for scroll pane's and text area's
        spText.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        spMorse.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        taText.setLineWrap(true);
        taText.setWrapStyleWord(true);
        taMorse.setLineWrap(true);
        taMorse.setWrapStyleWord(true);

        taText.setFont(font);
        taMorse.setFont(font);

        progressBar.setMinimum(0);

        //setting event listeners
        taText.getDocument().addDocumentListener(this);
        taMorse.getDocument().addDocumentListener(this);
        spinVolume.addChangeListener(this);
        spinUnitLength.addChangeListener(this);
        spinWpm.addChangeListener(this);
        butStartPlay.addActionListener(this);
        butStartPlay.setActionCommand("play");
        butStartListen.addActionListener(this);
        butStartListen.setActionCommand("listen");
        butStop.addActionListener(this);
        butStop.setActionCommand("stop");

        tempX = gap;
        tempY = gap/2;

        labText.setBounds(tempX, tempY, maxWidth, smallHeight);
        tempY += smallHeight;
        spText.setBounds(tempX, tempY, maxWidth, bigHeight);
        tempY += bigHeight;
        labMorse.setBounds(tempX, tempY, maxWidth, smallHeight);
        tempY += smallHeight;
        spMorse.setBounds(tempX, tempY, maxWidth, bigHeight);
        tempY += bigHeight;

        if(showSignalCode) {
            lSignal = new JLabel("Kod sygnałowy (jeden znak odpowiada jednej jednostce czasu):");
            taSignal = new JTextArea();
            spSignal = new JScrollPane(taSignal);

            taSignal.getDocument().addDocumentListener(this);
            taSignal.setLineWrap(true);
            taSignal.setWrapStyleWord(true);
            taSignal.setFont(font);

            spSignal.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

            lSignal.setBounds(tempX, tempY, maxWidth, smallHeight);
            tempY += smallHeight;
            spSignal.setBounds(tempX, tempY, maxWidth, bigHeight);
            tempY += bigHeight;

            panel.add(lSignal);
            panel.add(spSignal);
        }

        labVolume.setBounds(tempX, tempY, oneThirdWidth, smallHeight);
        tempX += gap + oneThirdWidth;
        labUnitLength.setBounds(tempX, tempY, oneThirdWidth+gap, smallHeight);
        tempX += gap + oneThirdWidth;
        labWpm.setBounds(tempX, tempY, oneThirdWidth, smallHeight);
        tempX = gap;
        tempY += smallHeight;
        spinVolume.setBounds(tempX, tempY, oneThirdWidth, smallHeight);
        tempX += gap + oneThirdWidth;
        spinUnitLength.setBounds(tempX, tempY, oneThirdWidth, smallHeight);
        tempX += gap + oneThirdWidth;
        spinWpm.setBounds(tempX, tempY, oneThirdWidth, smallHeight);
        tempX = gap;
        tempY += gap + smallHeight;
        progressBar.setBounds(tempX, tempY, maxWidth, smallHeight);
        tempY += gap + smallHeight;

        butStartPlay.setBounds(tempX, tempY, oneThirdWidth, smallHeight);
        tempX += gap + oneThirdWidth;
        butStartListen.setBounds(tempX, tempY, oneThirdWidth, smallHeight);
        tempX += gap + oneThirdWidth;
        butStop.setBounds(tempX, tempY, oneThirdWidth, smallHeight);
        tempX += gap + oneThirdWidth;
        tempY += gap + smallHeight;

        panel.add(labText);
        panel.add(spText);
        panel.add(labMorse);
        panel.add(spMorse);
        panel.add(labVolume);
        panel.add(labUnitLength);
        panel.add(labWpm);
        panel.add(spinVolume);
        panel.add(spinUnitLength);
        panel.add(spinWpm);
        panel.add(progressBar);
        panel.add(butStartPlay);
        panel.add(butStartListen);
        panel.add(butStop);

        frame.setSize(maxWidth+3*gap, tempY+3*gap);
        frame.setMinimumSize(frame.getSize());
        frame.setJMenuBar(menuBar);
        frame.setContentPane(panel);
    }

    //event handling for press the buttons
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        String action = actionEvent.getActionCommand();
        System.out.println("button clicked: "+action);
        switch (action) {
            case "play" -> controller.playSound();
            case "listen" -> controller.listenSound();
            case "stop" -> controller.stopSound();
        }
    }
    //event handling for text input in text areas
    @Override
    public void insertUpdate(DocumentEvent e) {
        textInput(e);
    }
    @Override
    public void removeUpdate(DocumentEvent e) {
        textInput(e);
    }
    @Override
    public void changedUpdate(DocumentEvent e) {
        textInput(e);
    }
    private void textInput(DocumentEvent e) {
        if(!avoidLoopedTextInputEvents) {
            Document document = e.getDocument();
            if (taText.getDocument().equals(document)) {
                controller.setText(taText.getText());
            } else if (taMorse.getDocument().equals(document)) {
                controller.setMorseCode(taMorse.getText());
            } else if (taSignal.getDocument().equals(document)) {
                controller.setSignalCode(taSignal.getText());
            }
        }
    }
    //event handling for spinners of volume and unit length
    @Override
    public void stateChanged(ChangeEvent changeEvent) {
        Object source = changeEvent.getSource();
        if (spinVolume.equals(source)) {
            controller.volumeChange(Integer.parseInt(spinVolume.getValue().toString()));
        } else if (spinUnitLength.equals(source)) {
            int val = Integer.parseInt(spinUnitLength.getValue().toString());
            spinWpm.setValue(1200/val);
            controller.unitLengthChange(val);
        } else if (spinWpm.equals(source)) {
            int val = 1200/Integer.parseInt(spinWpm.getValue().toString());
            spinUnitLength.setValue(val);
            controller.unitLengthChange(val);
        }
    }
    public void setResults() {
        SwingUtilities.invokeLater(() -> {
            avoidLoopedTextInputEvents =true;
            JTextArea focused = (taText.hasFocus() ? taText :
                    (taMorse.hasFocus() ? taMorse :
                            (showSignalCode&&taSignal.hasFocus() ? taSignal : null)));
            int cursorPosition=0;
            if(focused != null) cursorPosition = focused.getCaretPosition();

            taText.setText(controller.getText());
            taMorse.setText(controller.getMorseCode());
            if(showSignalCode) taSignal.setText(controller.getSignalCode());

            if(focused != null && focused.getText().length()>cursorPosition) focused.setCaretPosition(cursorPosition);

            avoidLoopedTextInputEvents =false;
        });
    }
    public void showMessage(String s) {
        JOptionPane.showMessageDialog(frame, s, "Informacja", JOptionPane.INFORMATION_MESSAGE);
        System.out.println("[gui] "+s);
    }
    public void showError(String s) {
        JOptionPane.showMessageDialog(frame, s, "Błąd", JOptionPane.ERROR_MESSAGE);
        System.err.println("[gui] "+s);
    }
    public void disablePlayButtonSound(boolean disabled) {
        butStartPlay.setEnabled(!disabled);
    }
    public void disableListenButtonSound(boolean disabled) {
        butStartListen.setEnabled(!disabled);
    }
    public void disableStopButtonSound(boolean disabled) {
        butStop.setEnabled(!disabled);
    }
    public void disableSpinnerVolume(boolean disabled) {
        spinVolume.setEnabled(!disabled);
    }
    public void disableSpinnerUnitLengthAndWpm(boolean disabled) {
        spinUnitLength.setEnabled(!disabled);
        spinWpm.setEnabled(!disabled);
    }
    public void disableSoundModule(boolean disabled) {
        disablePlayButtonSound(disabled);
        disableListenButtonSound(disabled);
        disableStopButtonSound(disabled);
        disableSpinnerVolume(disabled);
        disableSpinnerUnitLengthAndWpm(disabled);
    }
    public void disableTextAreas(boolean disabled) {
        taText.setEditable(!disabled);
        taMorse.setEditable(!disabled);
        if(showSignalCode) taSignal.setEditable(!disabled);
    }
    public void setProgressBarVal(int value) {
        SwingUtilities.invokeLater(() -> progressBar.setValue(value));
    }
    public void setProgressBarMax(int maximum) {
        SwingUtilities.invokeLater(() -> progressBar.setMaximum(maximum));
    }
    public void setProgressBarStringPainted(boolean stringPainted) {
        progressBar.setStringPainted(stringPainted);
    }
    public void setSpinnerVolume(int val) {
        SwingUtilities.invokeLater(() -> spinVolume.setValue(val));
    }
    public void setSpinnerUnitLength(int val) {
        SwingUtilities.invokeLater(() -> spinUnitLength.setValue(val));
    }
}
