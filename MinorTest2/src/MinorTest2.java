import org.firmata4j.firmata.FirmataDevice;
import org.firmata4j.I2CDevice;
import org.firmata4j.Pin;
import  org.firmata4j.ssd1306.SSD1306;
import org.firmata4j.ssd1306.MonochromeCanvas;
import  java.io.IOException;


public class MinorTest2 {

    public static void main(String[] args) throws IOException, InterruptedException{

            // Initializing Board
            var myUSBPort = "COM3";
            var myArduinoBoard = new FirmataDevice(myUSBPort);

            // Starting Board
            myArduinoBoard.start();
            myArduinoBoard.ensureInitializationIsDone();

            //Very Important
            var waterTask = new waterTest2(myArduinoBoard);

            //Also Important
            waterTest2.process();
            //System.out.println(waterTest2.processLog());


    }
}