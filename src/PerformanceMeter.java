public class PerformanceMeter {
    private boolean dependencyOnData;
    private int measurementCounter=0;
    private int dataSize=1;
    private double averageTime=0;
    private long startTime=0;
    private long measuredTime=0;
    private String name="";
    private static int instancesCounter=1;

    PerformanceMeter(String name, boolean dependencyOnData) {
        this.name = name;
        this.dependencyOnData = dependencyOnData;
        instancesCounter++;
    }
    PerformanceMeter(String name) {
        new PerformanceMeter(name, false);
    }
    PerformanceMeter() {
        new PerformanceMeter("PerformanceMeter no. "+instancesCounter);
    }
    public void start(int dataSize) {
        if(dependencyOnData) this.dataSize=dataSize;
        else System.err.println(name+" doesn't depend on amount of data");
        measurementCounter++;
        startTime = System.nanoTime();
    }
    public void start() {
        if(dependencyOnData) {
            System.err.println(name+" missing argument: amount of processing data");
        }
        measurementCounter++;
        startTime = System.nanoTime();
    }
    public void stop() {
        measuredTime = System.nanoTime()-startTime;
        calcAverageTime();
    }
    private void calcAverageTime() {
        averageTime = (averageTime*(measurementCounter-1) + measuredTime/(double)dataSize) / measurementCounter;
    }
    public void showAverageTime() {
        System.out.println("Average time of "+name+"="+averageTime);
    }
    public void reset() {
        System.out.println(name+" has been reset ");
        averageTime = 0;
        measurementCounter = 0;
    }
}
