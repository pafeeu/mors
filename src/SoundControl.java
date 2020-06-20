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

    //playing sound
    private byte[] unitOfHighSignal, unitOfLowSignal;
    private int unitLengthInMs=60;
    private double volume=0.4;

    //listening and anaylse sound
    private int bytesPerMs = sampleRate / 1000;
    private int avgForPeriodOfMs = 2;
    private int samplesToAvg = bytesPerMs * avgForPeriodOfMs;
    private int signalGain = 1, previousBoundary=1;
    private int periodOfAnalyse = 200;
    private int periodOfRecalculate = 1000;
    private boolean preventiveRecalculate = false;
    private ArrayList<Integer> signalsLength = new ArrayList<>();
    private StringBuilder output = new StringBuilder();
    private int recognizedLengthUnitOfHighSignal = 30;
    private int recognizedLengthUnitOfLowSignal = 30;

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
        unitsOfSignalPreparation();
    }
    private void unitsOfSignalPreparation() {
        int maxSampleValue = (int) Math.pow(2,sampleSizeInBits)-1;
        int fullSinsPerSec = 550; //600-800 - number of times in 1sec sin function repeats (frequency)
        double samplesToRepresentFullSin = (double) sampleRate / fullSinsPerSec; //lenght of full sin in samples
        int amountOfSamples = (int)(unitLengthInMs * (double) sampleRate / 1000); //summary amount of all generated samlpes,

        unitOfHighSignal = new byte[amountOfSamples];
        unitOfLowSignal = new byte[amountOfSamples];

        for( int i = 0; i < amountOfSamples; i++ ) {
            double angle = i / samplesToRepresentFullSin * 2.0 * Math.PI;  // full sin goes 0PI to 2PI
            unitOfHighSignal[i] = (byte) (Math.sin(angle) * maxSampleValue * volume);
            unitOfLowSignal[i] = 0;
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
            if(c==Interpreter.SIGNAL_HIGH) sdl.write(unitOfHighSignal, 0, unitOfHighSignal.length);
            else sdl.write(unitOfLowSignal, 0, unitOfLowSignal.length);
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
    private void analyser(ArrayList<Integer> avgData) {
        int dataSize=avgData.size();
        //calculate boundary
        int max=0, avg=0, avgHigh=0, iHigh=1;
        for (int el: avgData) {
            el *= signalGain;
            if(max<el) max = el;
            avg += el;
            if(el>previousBoundary) {
                avgHigh += el;
                iHigh++;
            }
        }
        avgHigh /= iHigh;
        avg /= dataSize;
        controller.setProgressBarMax(max);
        //over engineered
        //int boundary = (int) Math.round(avg>2 ? (max<avg*5 ? avg*2.0+max*0.5/2.5 : previousBoundary>avg*2.0?previousBoundary:avg*2.0) : previousBoundary );
        //depends on avg + possibly max
        //int boundary = (int) Math.round(max<avg*5 ? avg+max*0.5/2.0 : avg*1.1);
        //avg from high signals
        int boundary = (int) Math.round(avgHigh>2 ? avgHigh*0.45 : max);

        //recognize units length
        int possibleLengthUnitOfHighSignal=0, appearancesHighSignal=0;
        int possibleLengthUnitOfLowSignal=0, appearancesLowSignal=0;
        int tempAppearencesHighSignal, tempAppearencesLowSignal;
        double approximation=0.1; //lengths of unit
        for (int el:signalsLength) {
            if (el<3) continue; // filter too small lengths which can cause wrong recognizing
            tempAppearencesHighSignal = 0;
            tempAppearencesLowSignal = 0; //0 not 1, because it count itself in loop:
            for (int el2 : signalsLength) {
                //TODO: when whole analyser will be merge into one loop, first check is it low or high signal, then recognize length
                boolean nearOneLengthUnit = Math.abs(el2 - el) < el * approximation;
                boolean nearThreeLengthsUnit = Math.abs(el2 - el*3) < el * approximation;
                boolean nearSevenLengthUnit = Math.abs(el2 - el*7) < el * approximation;
                //high signal
                if (nearOneLengthUnit || nearThreeLengthsUnit)
                    ++tempAppearencesHighSignal;
                //low signal
                if (nearOneLengthUnit || nearThreeLengthsUnit || nearSevenLengthUnit)
                    ++tempAppearencesLowSignal;
            }
            if (tempAppearencesHighSignal>appearancesHighSignal) {
                appearancesHighSignal = tempAppearencesHighSignal;
                possibleLengthUnitOfHighSignal = el;
            }
            if (tempAppearencesLowSignal>appearancesLowSignal) {
                appearancesLowSignal = tempAppearencesLowSignal;
                possibleLengthUnitOfLowSignal = el;
            }
        }
        //if same boundary and unit length, can calculate only newest data
        int i;
        if(previousBoundary==boundary &&
                recognizedLengthUnitOfHighSignal==possibleLengthUnitOfHighSignal &&
                recognizedLengthUnitOfLowSignal==possibleLengthUnitOfLowSignal &&
                !preventiveRecalculate) {
            i = dataSize-periodOfAnalyse;
        } else {
            //System.out.println("reset the results");
            i = 0;
            signalsLength.clear();
            output.setLength(0);
            if(boundary>0) previousBoundary = boundary;
            if(possibleLengthUnitOfHighSignal>0) recognizedLengthUnitOfHighSignal = possibleLengthUnitOfHighSignal;
            if(possibleLengthUnitOfLowSignal>0) recognizedLengthUnitOfLowSignal = possibleLengthUnitOfLowSignal;
            preventiveRecalculate = false;

            System.out.print("\nRecognizedLengthUnitOf: HS="+recognizedLengthUnitOfHighSignal);
            System.out.print(" LS="+recognizedLengthUnitOfLowSignal+"\n");
        }

        //analyse is it low or high + prepare output
        int lengthCounter = 1;
        boolean high=false;
        while(i<dataSize) {
            if(high==avgData.get(i)*signalGain>boundary) lengthCounter++;
            else {
                signalsLength.add(lengthCounter);
                if(high) {
                    output.append(Interpreter.SIGNAL_HIGH.toString().repeat((int) Math.round((double) lengthCounter / recognizedLengthUnitOfHighSignal)));
                } else {
                    output.append(Interpreter.SIGNAL_LOW.toString().repeat((int) Math.round((double) lengthCounter / recognizedLengthUnitOfLowSignal)));
                }
                high = !high;
                lengthCounter=1;
            }
            i++;
        }

        /*
        TODO:
            -w rozpoznaniu low or high + prepare output mozna wcisnac rozpoznanie nowej granicy
                (i tak sie iteruje po avgData)
            -w trakcie tworzenia signalsLength można by analizować długość (zliczanie wystąpień maybe?)
            -ofc przetestować wtedy wydajność
         */

        //if signal is bad, raise signal gain
        if(boundary<20 && avg>1) {
            signalGain++;
            //System.out.println("signal gained to: "+signalGain);
        }

        //System.out.println("boundary: "+boundary+" avg: "+avg+" max: "+max+" avgHigh: "+avgHigh+" unit length: "+recognizedLengthUnit);
        controller.setSignalCode(output.toString());
        controller.setSpinnerUnitLength(recognizedLengthUnitOfHighSignal *avgForPeriodOfMs);
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
            //compressing data into one avg of 2 milliseconds
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
        signalsLength.clear();
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
    public void setUnitLengthInMs(int ms) {
        if(ms>0 && ms<=1200) {
            unitLengthInMs = ms;
            unitsOfSignalPreparation();
        }
    }
}
