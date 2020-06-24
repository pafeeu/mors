import javax.sound.sampled.*;
import java.util.ArrayList;

public class SoundControl {
    private ControlUnit controller=null;
    public void setController(ControlUnit controller) {
        this.controller = controller;
    }
    private void pushMessage(String s) {
        if(controller!=null)
            controller.pushMessage(s);
        else
            System.out.println("[SoundControl] "+s);
    }
    private void pushError(String s) {
        if(controller!=null)
            controller.pushError(s);
        else
            System.err.println("[SoundControl] "+s);
    }

    //global
    private int sampleRate=16000, sampleSizeInBits=8;
    private AudioFormat af;
    private SourceDataLine sdl;
    private TargetDataLine tdl;
    private Thread playThread, listenThread;
    private boolean processCondition;
    //basicUnitLength is speed of single character's signal
    //gapUnitLength is adaptation of 'Farnsworth  timing' which cause gap between alphabetic chars and between words is slower than basic speed (so it's longer)
    private int basicUnitLength=40, gapUnitLength=40;

    //playing sound
    private byte[] highSignalOnBasicSpeed, lowSignalOnBasicSpeed, lowSignalOnGapSpeed;
    private double volume=0.4;

    //listening and anaylse sound
    private int bytesPerMs = sampleRate / 1000;
    private int avgForPeriodOfMs = 1;
    private int samplesToAvg = bytesPerMs * avgForPeriodOfMs;
    private int periodOfAnalyse = 200;
    private int periodOfRecalculate = 1000;
    private boolean preventiveRecalculate = false;
    private ArrayList<Integer> lengthsGapUnit = new ArrayList<>();
    private ArrayList<Integer> lengthsGapUnitAppearances = new ArrayList<>();
    private ArrayList<Integer> lengthsBasicUnit = new ArrayList<>();
    private ArrayList<Integer> lengthsBasicUnitAppearances = new ArrayList<>();
    private StringBuilder output = new StringBuilder();
    private int signalGain = 1, recognizedBoundary = 1;
    //temporary stored values for analyser
    private int max = 0, avgHigh = 0, iteratorAvgHigh = 1;
    private int possibleBoundary = 1;
    private int possibleBasicUnitLength = 0, appearancesBasicUnitLength = 0;
    private int possibleGapUnitLength = 0, appearancesGapUnitLength = 0;
    private double approximation = 0.1; //of unit length

    SoundControl() {
        intialize();
    }
    SoundControl(ControlUnit controller) {
        setController(controller);
        intialize();
    }
    public void intialize() {
        controller.disableSoundModule(false);

        af = new AudioFormat((float) sampleRate, sampleSizeInBits, 1, true, false);
        //init: source and target data lines
        try {
            sdl = AudioSystem.getSourceDataLine(af);
        } catch (LineUnavailableException e) {
            pushError("Nie można uzyskać dostępu do odtwarzania dźwięku");
            controller.disablePlayButtonSound(true);
            e.printStackTrace();
        }

        try {
            tdl = AudioSystem.getTargetDataLine(af);
        } catch (LineUnavailableException | IllegalArgumentException e) {
            pushError("Nie można uzyskać dostępu do nagrywania dźwięku");
            controller.disableListenButtonSound(true);
            e.printStackTrace();
        }

        controller.disableStopButtonSound(true);
        controller.setSpinnerUnitLength(basicUnitLength, gapUnitLength);
        //if without gui, needed:
        //unitsOfSignalPreparation();
    }
    private void unitsOfSignalPreparation() {
        //System.out.println("[SC.preparation] basic="+basicUnitLength+" gap="+gapUnitLength);
        int maxSampleValue = (int) Math.pow(2,sampleSizeInBits)-1;
        int fullSinsPerSec = 550; //600-800 - number of times in 1sec sin function repeats (frequency)
        double samplesToRepresentFullSin = (double) sampleRate / fullSinsPerSec; //lenght of full sin in samples
        int amountOfSamplesToBasicSpeed = (int)(basicUnitLength * (double) sampleRate / 1000); //summary amount of all generated samlpes,
        int amountOfSamplesToGapSpeed = (int)(gapUnitLength * (double) sampleRate / 1000);

        highSignalOnBasicSpeed = new byte[amountOfSamplesToBasicSpeed];
        lowSignalOnBasicSpeed = new byte[amountOfSamplesToBasicSpeed];
        lowSignalOnGapSpeed = new byte[amountOfSamplesToGapSpeed];

        double angle;
        int i=0;
        while(i<amountOfSamplesToBasicSpeed) {
            angle = i / samplesToRepresentFullSin * 2.0 * Math.PI;  // full sin goes 0PI to 2PI
            highSignalOnBasicSpeed[i] = (byte) (Math.sin(angle) * maxSampleValue * volume);
            lowSignalOnBasicSpeed[i] = 0;
            if(i<amountOfSamplesToGapSpeed) {
                lowSignalOnGapSpeed[i] = 0;
            }
            i++;
        }
        while(i<amountOfSamplesToGapSpeed) {
            lowSignalOnGapSpeed[i] = 0;
            i++;
        }

    }
    private void player() {
        try {
            sdl.open();
        } catch (LineUnavailableException e) {
            pushError("Nie można uzyskać dostępu do odtwarzania dźwięku");
            e.printStackTrace();
        }
        sdl.start();
        String signal = controller.getSignalCode();
        int signalLength = signal.length();
        controller.setProgressBarMax(signalLength);
        controller.setProgressBarStringPainted(true);
        char c;
        for(int i=0; i<signalLength && processCondition; i++) {
            c=signal.charAt(i);
            if(c==Interpreter.SIGNAL_HIGH) sdl.write(highSignalOnBasicSpeed, 0, highSignalOnBasicSpeed.length);
            else {
                if(i>0 && i<signalLength-1 && signal.charAt(i-1)==Interpreter.SIGNAL_HIGH && signal.charAt(i+1)==Interpreter.SIGNAL_HIGH)
                    sdl.write(lowSignalOnBasicSpeed, 0, lowSignalOnBasicSpeed.length);
                else sdl.write(lowSignalOnGapSpeed, 0, lowSignalOnGapSpeed.length);
            }
            controller.setProgressBarVal(i);
        }

        sdl.drain();
        controller.stopSound();
    }
    public void play() {
        processCondition = true;
        playThread = new Thread(this::player,"player");
        playThread.start();
    }
    private boolean isApproximateNear(double el1, int el2) {
        return Math.abs(el1 - el2) < el2 * approximation;
    }
    private boolean lengthIsNearAnyMultiplicity(boolean isHighSignal, int length, int comparedLength) {
        if (isHighSignal) return isApproximateNear(length, comparedLength) || isApproximateNear((double)length/3, comparedLength);
        else return isApproximateNear(length, comparedLength) || isApproximateNear(length/2.33, comparedLength);
    }
    private void putIntoLengthsList(boolean isHighSignal, int length) {
        if(length<3) return;
        ArrayList<Integer> list = (isHighSignal ? lengthsBasicUnit : lengthsGapUnit);
        ArrayList<Integer> appearances = (isHighSignal ? lengthsBasicUnitAppearances : lengthsGapUnitAppearances);
        boolean notFound = true;
        for(int i=0; i<list.size(); i++) {
            if(lengthIsNearAnyMultiplicity(isHighSignal, length, list.get(i))) {
                appearances.set(i, appearances.get(i)+1);
                notFound = false;
            }
        }
        if(notFound) {
            list.add(length);
            appearances.add(1);
        }
    }
    private void analyser(ArrayList<Integer> avgData) {
        int dataSize=avgData.size();
        //if same boundary and unit lengths, can calculate only newest data
        int i;
        if(recognizedBoundary == possibleBoundary &&
                basicUnitLength == possibleBasicUnitLength &&
                gapUnitLength == possibleGapUnitLength &&
                !preventiveRecalculate) {
            i = dataSize-periodOfAnalyse;
        } else {
            //System.out.println("reset the results");
            if(possibleBoundary>0) recognizedBoundary = possibleBoundary;
            if(possibleBasicUnitLength >0) basicUnitLength = possibleBasicUnitLength;
            if(possibleGapUnitLength >0) gapUnitLength = possibleGapUnitLength;

            i=0;
            max=0;
            avgHigh=0;
            iteratorAvgHigh=1;
            lengthsBasicUnit.clear();
            lengthsGapUnit.clear();
            lengthsBasicUnitAppearances.clear();
            lengthsGapUnitAppearances.clear();
            possibleBasicUnitLength =0;
            possibleGapUnitLength =0;
            appearancesGapUnitLength =0;
            appearancesBasicUnitLength =0;
            output.setLength(0);
            preventiveRecalculate = false;
        }
        //main loop
        int elem;
        int lengthCounter = 1;
        boolean isHigh=false;
        int sumHigh = avgHigh * iteratorAvgHigh;
        while(i<dataSize) {
            elem = avgData.get(i)*signalGain;
            //prepare data for calculate boundary
            if(max<elem) max = elem;
            if(elem>recognizedBoundary) {
                sumHigh += elem;
                iteratorAvgHigh++;
            }

            //analyse is it low or high + prepare output
            if(isHigh == elem>recognizedBoundary) lengthCounter++;
            else {
                if(isHigh) {
                    putIntoLengthsList(true, lengthCounter);
                    output.append(Interpreter.SIGNAL_HIGH.toString().repeat(
                            (int) Math.round((double) lengthCounter / basicUnitLength)));
                } else if(isApproximateNear(lengthCounter, basicUnitLength)) {
                    putIntoLengthsList(true, lengthCounter);
                    output.append(Interpreter.SIGNAL_LOW.toString().repeat(
                            (int) Math.round((double) lengthCounter / basicUnitLength)));
                } else {
                    putIntoLengthsList(false, lengthCounter);
                    output.append(Interpreter.SIGNAL_LOW.toString().repeat(
                            (int) Math.round((double) lengthCounter / gapUnitLength)));
                }
                isHigh = !isHigh;
                lengthCounter=1;
            }
            i++;
        }

        //calculate boundary
        avgHigh = sumHigh/iteratorAvgHigh;
        possibleBoundary = (int) Math.round(avgHigh>2 ? avgHigh*0.66 : max);
        //for visualisation
        controller.setProgressBarMax(max);

        //select new unit lengths
        for (int j = 0; j < lengthsBasicUnitAppearances.size(); j++) {
            if(lengthsBasicUnitAppearances.get(j) > appearancesBasicUnitLength) {
                appearancesBasicUnitLength = lengthsBasicUnitAppearances.get(j);
                possibleBasicUnitLength = lengthsBasicUnit.get(j);
            }
        }
        for (int j = 0; j < lengthsGapUnitAppearances.size(); j++) {
            if(lengthsGapUnitAppearances.get(j) > appearancesGapUnitLength) {
                appearancesGapUnitLength = lengthsGapUnitAppearances.get(j);
                //the result must by divided by 3 cause this arr has only 3 units
                possibleGapUnitLength = lengthsGapUnit.get(j)/3;
            }
        }
        //if signal is bad, raise signal gain
        if(possibleBoundary<20 && avgHigh>1) {
            signalGain++;
            //System.out.println("signal gained to: "+signalGain);
        }

        //setting results
        //System.out.println("boundary: "+recognizedBoundary+" max: "+max+" avgHigh: "+avgHigh+" HS="+recognizedLengthUnitOfHighSignal+" LS="+recognizedLengthUnitOfLowSignal);
        controller.setSignalCode(output.toString());
        controller.setSpinnerUnitLength(basicUnitLength*avgForPeriodOfMs, gapUnitLength*avgForPeriodOfMs);
    }
    private void listener() {
        try {
            tdl.open(af);
        } catch (LineUnavailableException e) {
            pushError("Nie można uzyskać dostępu do nagrywania dźwięku");
            e.printStackTrace();
        }

        byte[] data = new byte[Math.min(tdl.getBufferSize(), samplesToAvg)];
        ArrayList<Integer> avgData = new ArrayList<>();
        tdl.start();
        controller.setProgressBarStringPainted(false);
        int i, j=1;
        double sampleAvg;
        do {
            tdl.read(data, 0, data.length);
            //compressing data into one avg of some milliseconds
            sampleAvg=0;
            for(i=0; i<data.length; i++) {
                sampleAvg += Math.abs(data[i]);
            }
            sampleAvg /= data.length;
            controller.setProgressBarVal((int) sampleAvg);
            avgData.add((int) Math.round(sampleAvg));
            if(j%periodOfRecalculate==0) preventiveRecalculate = true;
            if(j%periodOfAnalyse==0) analyser(avgData);
            j++;
        } while (processCondition);

        tdl.stop();
        tdl.close();
        //last analyse
        preventiveRecalculate = true;
        analyser(avgData);
    }
    public void listen() {
        lengthsGapUnit.clear();
        output.setLength(0);
        signalGain=1;
        processCondition = true;
        listenThread = new Thread(this::listener,"listener");
        listenThread.start();
    }
    public void stop() {
        processCondition = false;
        if(sdl.isActive()) { //play
            sdl.flush();
            sdl.stop();
            sdl.close();
        }
        if(tdl.isActive()) { //listen
            tdl.flush();
            tdl.stop();
            tdl.close();
        }
    }
    public void setVolume(double vol) {
        if(vol>=0 && vol<=100) vol /= 100;
        if(vol>=0.0 && vol<=1.0) {
            volume = vol;
            unitsOfSignalPreparation();
        }
    }
    public void setUnitLength(int basicUnitLengthInMS, int gapUnitLengthInMS) {
        if(basicUnitLengthInMS>0 && basicUnitLengthInMS<=1200 && gapUnitLengthInMS>0 && gapUnitLengthInMS<=1200) {
            basicUnitLength = basicUnitLengthInMS;
            gapUnitLength = gapUnitLengthInMS;
            unitsOfSignalPreparation();
        }
    }
}
