import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class GraphicalInterface implements ActionListener, ChangeListener {
    private ControlUnit controller=null;
    public void setController(ControlUnit controller) {
        this.controller = controller;
    }

    JFrame frame;
    JPanel panel;
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
    //TODO: progress bar, controls for volume and unit length in ms

    boolean showSignalCode = true;
    JLabel lSignal;
    JTextArea taSignal;
    JScrollPane spSignal;

    boolean conditionTextUpdateListener;

    GraphicalInterface() {
        initialize();
    }
    GraphicalInterface(ControlUnit controller) {
        setController(controller);
        initialize();
    }
    public void show() {
        frame.setVisible(true);
        setTextUpdater(true);
    }
    public void hide() {
        frame.setVisible(false);
        setTextUpdater(false);
    }
    public void initialize() {
        frame = new JFrame("Morse code translator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //initialization
        panel = new JPanel(null);
        labText = new JLabel("Tekst (znaki alfanumeryczne):");
        labMorse = new JLabel("Kod morsa (znaki - i •):");
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

        frame.setSize(maxWidth+2*gap, tempY+gap);

        frame.setContentPane(panel);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        //obsluga przyciskow od dzwieku
        String action = actionEvent.getActionCommand();
        System.out.println("button clicked: "+action);
        switch (action) {
            case "play" -> controller.playSound();
            case "listen" -> controller.listenSound();
            case "stop" -> controller.stopSound();
        }
    }
    public void setTextUpdater(boolean update) {
        if(update) {
            conditionTextUpdateListener = true;
            textUpdateListener();
        } else {
            conditionTextUpdateListener = false;
        }
    }
    private void textUpdateListener() {
        new Thread(() -> {
            String oldText="", oldMorse="";
            String newText, newMorse;
            //TODO disable when listening + ew add signal support
            while (conditionTextUpdateListener) {
                newText = taText.getText();
                newMorse = taMorse.getText();
                if (!newText.equals(oldText)) {
                    controller.setText(newText);
                    oldText = newText;
                    oldMorse = controller.getMorseCode();
                    setResults();
                } else if (!newMorse.equals(oldMorse)) {
                    controller.setMorseCode(newMorse);
                    oldMorse = newMorse;
                    oldText = controller.getText();
                    setResults();
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    public void setResults() {
        SwingUtilities.invokeLater(() -> {
            taText.setText(controller.getText());
            taMorse.setText(controller.getMorseCode());
            if(showSignalCode) taSignal.setText(controller.getSignalCode());
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
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(value);
        });
    }
    public void setProgressBarMax(int maximum) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setMaximum(maximum);
        });
    }
    public void setProgressBarStringPainted(boolean stringPainted) {
        progressBar.setStringPainted(stringPainted);
    }
    public void setSpinnerVolume(int val) {
        SwingUtilities.invokeLater(() -> {
            spinVolume.setValue(val);
        });
    }
    public void setSpinnerUnitLength(int val) {
        SwingUtilities.invokeLater(() -> {
            spinUnitLength.setValue(val);
        });
    }
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
}
