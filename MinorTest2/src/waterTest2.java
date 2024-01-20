import org.firmata4j.firmata.FirmataDevice;
import org.firmata4j.I2CDevice;
import org.firmata4j.Pin;
import  org.firmata4j.ssd1306.SSD1306;
import org.firmata4j.ssd1306.MonochromeCanvas;
import  java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class waterTest2 extends MinorTest2 {

    private static FirmataDevice myArduinoBoard = null;

    static final int D6 = 6; //Button
    static final int A2 = 16; //Sensor
    static final int D7 = 7; //Pump

    public waterTest2(FirmataDevice myArduinoBoard){
        this.myArduinoBoard = myArduinoBoard;
    }

    //Time Stuff
    private static final StringBuilder recLog = new StringBuilder();

    public static void process() throws IOException, InterruptedException{
        //Time Stuff
        Date now;
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        now = Calendar.getInstance().getTime();
        recLog.append(dateFormat.format(now)).append(" Starting the watering process.\n");

        try {
            //Button Initialization
            Pin buttonPin = myArduinoBoard.getPin(D6);
            buttonPin.setMode(Pin.Mode.INPUT);

            //Sensor Initialization
            Pin sensorPin = myArduinoBoard.getPin(A2);
            sensorPin.setMode(Pin.Mode.ANALOG);

            //Pump Initialization
            Pin pumpPin = myArduinoBoard.getPin(D7);
            pumpPin.setMode(Pin.Mode.OUTPUT);

            // OLED Initialization
            I2CDevice i2cObject = myArduinoBoard.getI2CDevice((byte) 0x3C);
            SSD1306 theOledObject = new SSD1306(i2cObject, SSD1306.Size.SSD1306_128_64);
            theOledObject.init();

            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleAtFixedRate(() -> {

                // Running Process
                int THRESHOLD = 675;
                int sensorValue;
                int i = 0;
                int buttonValue=0;

                //Beginning While Loop
                while (true) {

                    buttonValue = (int) buttonPin.getValue();
                    //System.out.println(buttonValue);
                    i++;
                    sensorValue = getSensorValue(sensorPin);
                    recLog.append(dateFormat.format(Calendar.getInstance().getTime()))
                            .append(" The moisture level is ").append(sensorValue).append(".\n");

                    //First Check (If Dry)
                    if (buttonValue==1 && sensorValue > THRESHOLD || buttonValue==1 && sensorValue < THRESHOLD) {
                        pumpOff(pumpPin);
                        break;
                    }else if (sensorValue > THRESHOLD && buttonValue!=1) {

                            theOledObject.getCanvas().drawString(20, 0, "The Soil is Dry!");
                            theOledObject.getCanvas().drawString(30, 20, "Pump is On!");
                            theOledObject.display();
                            pumpOn(pumpPin);
                            recLog.append(dateFormat.format(Calendar.getInstance().getTime()))
                                    .append(" The moisture level is low, watering for 2 seconds.\n");
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            pumpOff(pumpPin);
                            theOledObject.clear();
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }

                                //Nested While Loop
                                while (true) {

                                    buttonValue = (int) buttonPin.getValue();
                                    sensorValue = getSensorValue(sensorPin);
                                    i++;

                                        //Second Check (If Wet)
                                        if(buttonValue==1) {
                                            theOledObject.getCanvas().drawString(10, 0, "Pump has been Stopped!");
                                            theOledObject.getCanvas().drawString(10, 30, "Moisture Value: " + sensorValue);
                                            theOledObject.display();
                                            pumpOff(pumpPin);
                                            theOledObject.clear();
                                            recLog.append(dateFormat.format(Calendar.getInstance().getTime()))
                                                    .append(" The moisture level is now sufficient, after seconds "+((2*i)-2)+ " of watering.\n");
                                            executor.shutdown();
                                            try {
                                                Thread.sleep(3000);
                                            } catch (InterruptedException e) {
                                                throw new RuntimeException(e);
                                            }
                                            System.out.println(processLog());
                                            break;
                                        }else if (sensorValue < THRESHOLD) {

                                            theOledObject.getCanvas().drawString(20, 0, "The Soil is Wet!");
                                            theOledObject.getCanvas().drawString(10, 40, "Moisture Value: " + sensorValue);
                                            theOledObject.getCanvas().drawString(30, 20, "Pump is Off.");
                                            theOledObject.display();
                                            pumpOff(pumpPin);
                                            recLog.append(dateFormat.format(Calendar.getInstance().getTime()))
                                                    .append(" The moisture level is now sufficient, after seconds "+2*i+ " of watering.\n");
                                            executor.shutdown();
                                            try {
                                                Thread.sleep(2000);
                                            } catch (InterruptedException e) {
                                                throw new RuntimeException(e);
                                            }
                                            theOledObject.clear();

                                        }
                                        //Second Check (If Dry)
                                        else {
                                            theOledObject.getCanvas().drawString(10, 0, "Soil is still Dry!");
                                            theOledObject.getCanvas().drawString(10, 40, "Moisture Value: " + sensorValue);
                                            theOledObject.getCanvas().drawString(30, 20, "Pump is On.");
                                            theOledObject.display();
                                            pumpOn(pumpPin);
                                            recLog.append(dateFormat.format(Calendar.getInstance().getTime()))
                                                    .append(" The moisture level is still low, watering for 2 seconds.\n");
                                            try {
                                                Thread.sleep(2000);
                                            } catch (InterruptedException e) {
                                                throw new RuntimeException(e);
                                            }
                                            pumpOff(pumpPin);
                                            try {
                                                Thread.sleep(2000);
                                            } catch (InterruptedException e) {
                                                throw new RuntimeException(e);
                                            }

                                        }

                                }
                        }
                        //First Check (If Wet)
                        else {

                            theOledObject.getCanvas().drawString(20, 0, "The Soil is Wet!");
                            theOledObject.getCanvas().drawString(10, 40, "Moisture Value: " + sensorValue);
                            theOledObject.getCanvas().drawString(30, 20, "Pump is Off.");
                            theOledObject.display();
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            recLog.append(dateFormat.format(Calendar.getInstance().getTime()))
                                    .append(" The moisture level is sufficient. Stopping watering process.\n");
                            executor.shutdown();
                            theOledObject.clear();

                        }
                }
            }, 0, 20, TimeUnit.SECONDS);

            while (!executor.isTerminated()) {
                // Keep waiting until the executor is terminated
            }
        }catch (Exception ex){
            System.out.println("Couldn't connect to Arduino board.");
            throw ex;
        }finally {
            myArduinoBoard.stop();
            System.out.println("Arduino Board Stopped. End of Watering Process.");
        }
    }

    public static String processLog() {
        return recLog.toString();
    }


    private static void pumpOn(Pin pumpPin){
        try{
            pumpPin.setValue(1);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private static void pumpOff(Pin pumpPin){
        try{
            pumpPin.setValue(0);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private static int getSensorValue(Pin sensorPin){
        int moistureValue;
        moistureValue = (int) sensorPin.getValue();

        return moistureValue;
    }


}
