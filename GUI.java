package pgk28gui;

import ch.aplu.xboxcontroller.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.shape.Arc;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.effect.ImageInput;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.VLineTo;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import pgk28gui.Link;

/**
 *
 * @author johnn_000
 */
public class GUI extends Application{
    
    private final int SCENEWIDTH = 1920;
    private final int SCENEHEIGHT = 1080;
    private Rectangle bg;
    private Rectangle detectionTextBox;
    private Rectangle detectionAlertBox;
    private Rectangle botStatusBox;
    private Circle bot;
    private Line botL;
    private Line botR;
    private Pillar[] pillars = new Pillar[5];
    private Circle[] mapPoints = new Circle[5];
    private static Rectangle connect;
    private Arc arc;
    private Group root;
    private Text mainHUDtext;
    private static Text leftBumper;
    private static Text rightBumper;
    private static Text borderSensor;
    private static Text cliffSensor;
    private static Text retrievalZone;
    private static Text stopText;
    private static Text moveText;
    private static Text turnTextL;
    private static Text turnTextR;
    private static Text sweepText;
    private static Text angle;
    private ObservableList<Node> children;
    private ArrayList<Text> detectionText;
    private Link comm;
    private Controller controller;
    private int index = 0;
    private int currentAngle = 0;
    private char turned = '0';
    private enum event {LEFTBUMP, RIGHTBUMP, BORDERSENS, CLIFFSENS, RETSENS};

    @Override
    public void start(Stage stage) throws Exception
    {
        setBackground();       
        makeMap();
        setMainHUDtext();
        setDetectionTextBox();        
        setDetectionAlertBox();
        setStatusBox();
        makeConnect();
        //add objects to root
        setGroup();
        //Initialize controller support
        controller = new Controller();
        
        //receive data from bot
        CommReceive r = new CommReceive();
        new Thread(r).start();
        //Create scene
        Scene scene = new Scene(root, SCENEWIDTH, SCENEHEIGHT);
        scene.setFill(Color.BLACK);
        
        //Set title
        stage.setTitle("VORTEX Headquarters");
        
        //Add scene to stage
        stage.setScene(scene);
        
        //Display stage
        stage.show();
        
    }
    
    private void setBackground()
    {
        //Load background
//        Image image = new Image("http://wallpaperswide.com/download/hud_1_0-wallpaper-1920x1080.jpg"); //1080p
        Image image = new Image("http://wallpaperswide.com/download/hud_1_0-wallpaper-3840x2160.jpg"); //4k
        bg = new Rectangle();
        ImageInput imageInput = new ImageInput();
        imageInput.setX(0);
        imageInput.setY(0);
        imageInput.setSource(image);
        bg.setEffect(imageInput);
        
        
    }
    
    private void makeMap()
    {
        //Create arc for map
        arc = new Arc();
        arc.setCenterX(SCENEWIDTH / 2);
        arc.setCenterY(SCENEHEIGHT);
        arc.setRadiusX(240.0f);
        arc.setRadiusY(240.0f);
        arc.setStartAngle(0.0f);
        arc.setLength(180.0f); //degrees 
        arc.setFill(Color.AQUA);
        arc.setOpacity(0.8);
        arc.setType(ArcType.ROUND);
        bot = new Circle(SCENEWIDTH / 2, SCENEHEIGHT, 47.025);
        bot.setFill(Color.BLACK);
        bot.setOpacity(0.5);
        botL = new Line((SCENEWIDTH / 2) - 47.025, SCENEHEIGHT, (SCENEWIDTH / 2) - 47.025, SCENEHEIGHT - 240);
        botR = new Line((SCENEWIDTH / 2) + 47.025, SCENEHEIGHT, (SCENEWIDTH / 2) + 47.025, SCENEHEIGHT - 240);
        
        
        //initialize all map points
        for(int i = 0; i < mapPoints.length; i++)
        {
            mapPoints[i] = new Circle(0, 0, 10);
            mapPoints[i].setFill(Color.TRANSPARENT);
        }
    }
    
    private void setMainHUDtext()
    {
        mainHUDtext = new Text();
        mainHUDtext.setText("VORTEX Mission Control HUD");
        mainHUDtext.setFont(Font.font("sans-serif", FontWeight.BOLD, FontPosture.REGULAR, 40));
        mainHUDtext.setX(SCENEWIDTH * 0.363); 
        mainHUDtext.setY(SCENEHEIGHT * 0.045);
        mainHUDtext.setFill(Color.CADETBLUE);
    }
    
    private void setDetectionTextBox()
    {
        //set up detection text box
        detectionTextBox = new Rectangle();
        detectionTextBox.setX(SCENEWIDTH * 0.05);
        detectionTextBox.setY(SCENEHEIGHT * 0.1);
        detectionTextBox.setWidth(700);
        detectionTextBox.setHeight(350);
        detectionTextBox.setArcWidth(detectionTextBox.getWidth()* 0.0714);
        detectionTextBox.setArcHeight(detectionTextBox.getHeight() * 0.114);
        detectionTextBox.setFill(Color.CYAN);
        detectionTextBox.setOpacity(0.30);
        
        //add text
        detectionText = new ArrayList<Text>();
        int offset = 0;
        
        //Set up detection text and space out lines
        for(int i = 0; i < 5; i++)
        {
            Text text = new Text("");
            text.setX(SCENEWIDTH * 0.06);
            text.setY(SCENEHEIGHT * 0.13 + offset);
            text.setFont(Font.font("sans-serif", FontWeight.BOLD, FontPosture.REGULAR, 22));
            text.setFill(Color.AQUA);
            detectionText.add(text);
            offset += 75;
        }
        
    }
    
    private void setDetectionAlertBox()
    {
        //Create Alert Box
        detectionAlertBox = new Rectangle();
        detectionAlertBox.setX(SCENEWIDTH * 0.65);
        detectionAlertBox.setY(SCENEHEIGHT * 0.16);
        detectionAlertBox.setWidth(600);
        detectionAlertBox.setHeight(325);
        detectionAlertBox.setArcWidth(detectionAlertBox.getWidth()* 0.0714);
        detectionAlertBox.setArcHeight(detectionAlertBox.getHeight() * 0.114);
        detectionAlertBox.setFill(Color.CYAN);
        detectionAlertBox.setOpacity(0.3);
        
        //Set up left bumper alert
        leftBumper = new Text("Left Bumper Has Been Triggered");
        leftBumper.setFont(Font.font("sans-serif", FontWeight.BOLD, FontPosture.REGULAR, 30));
        leftBumper.setOpacity(0.1);
        leftBumper.setX(SCENEWIDTH * 0.67);
        leftBumper.setY(SCENEHEIGHT * 0.2);
        
        //Set up right bumper alert
        rightBumper = new Text("Right Bumper Has Been Triggered");
        rightBumper.setFont(Font.font("sans-serif", FontWeight.BOLD, FontPosture.REGULAR, 30));
        rightBumper.setOpacity(0.1);
        rightBumper.setX(SCENEWIDTH * 0.67);
        rightBumper.setY(SCENEHEIGHT * 0.25);
                
        //Set up light sensor alert
        borderSensor = new Text("Border Sensor Has Been Triggered");
        borderSensor.setFont(Font.font("sans-serif", FontWeight.BOLD, FontPosture.REGULAR, 30));
        borderSensor.setOpacity(0.1);
        borderSensor.setX(SCENEWIDTH * 0.67);
        borderSensor.setY(SCENEHEIGHT * 0.3);
        
        //Set up cliff sensor alert
        cliffSensor = new Text("Cliff Sensor Has Been Triggered");
        cliffSensor.setFont(Font.font("sans-serif", FontWeight.BOLD, FontPosture.REGULAR, 30));
        cliffSensor.setOpacity(0.1);
        cliffSensor.setX(SCENEWIDTH * 0.67);
        cliffSensor.setY(SCENEHEIGHT * 0.35);
        
        //Set up retrieval zone detection alert
        retrievalZone = new Text("Retrieval Zone Detected!");
        retrievalZone.setFont(Font.font("sans-serif", FontWeight.BOLD, FontPosture.REGULAR, 30));
        retrievalZone.setOpacity(0.1);
        retrievalZone.setX(SCENEWIDTH * 0.67);
        retrievalZone.setY(SCENEHEIGHT * 0.4);
        
    }
    
    //-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //Alert Light Up Trigger Stuff
    //-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    public void triggerEvent(event e){
        LightMeUp test = null;
        //Lights up given text for 3-6 seconds
        if (e == event.LEFTBUMP) {
            test = new LightMeUp(leftBumper, 3000);
        }
        if (e == event.RIGHTBUMP) {
            test = new LightMeUp(rightBumper, 3000);
        }
        if (e == event.BORDERSENS) {
            test = new LightMeUp(borderSensor, 3000);
        }
        if(e == event.CLIFFSENS)
        {
            test = new LightMeUp(cliffSensor, 3000);
        }
        if(e == event.RETSENS)
        {
            test = new LightMeUp(retrievalZone, 6000);
        }
        if (test != null) {
            new Thread(test).start();
        }
    }
    
    private class LightMeUp implements Runnable {

        private Text textToLightUp;
        private int durationMili;
        
        public LightMeUp(Text textToLightUp, int durationMili) 
        {
            this.textToLightUp = textToLightUp;
            this.durationMili = durationMili;
        }

        //Light up alert for given time and have it fade back to low opacity
        @Override
        public void run()
        {
            try 
            {
                //Light up code
                if(durationMili == 6000)
                {
                    textToLightUp.setFill(Color.SPRINGGREEN);
                }
                else
                {
                    textToLightUp.setFill(Color.YELLOW);
                }
                textToLightUp.setOpacity(1);
                Thread.sleep(durationMili);
                //Fade back to nearly transparent
                for(int i = 0; i < 90; i++)
                {
                    textToLightUp.setOpacity(textToLightUp.getOpacity() - .01);
                    Thread.sleep(8);
                }
                textToLightUp.setFill(Color.BLACK);
            } 
            catch (InterruptedException ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    
    } 
    
    //-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //Receive Stuff 
    //-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    public void triggerReceive(event e)
    {
        CommReceive x = null;
        String message;
        try 
        {
            message = comm.recieveMessage();
        }
        catch (IOException ex)
        {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private class CommReceive implements Runnable
    {
        ArrayList<String> messages;
        
        public CommReceive() 
        {
            try {
                comm = new Link();
            } catch (IOException ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                updateDetectionText("Failed to connect");
            }
        }

        @Override
        public void run() {
            boolean object = false;
            while(true)
            {
                try {
                    //Recieve a new message and parse with scanner
                    //Substring.(1) must be used because of bug in UART send code provided
                    String message = comm.recieveMessage();
                    Scanner sc = new Scanner(message);
                    String scanned = sc.nextLine();
                    scanned.trim();
                    System.out.println(scanned.substring(1));
                    //Left bumper triggered
                    if(scanned.substring(1).equals("LB"))
                    {
                        triggerEvent(event.LEFTBUMP);
                        controller.vibrate(1, 0);
                    }
                    //Right bumper triggered
                    if(scanned.substring(1).equals("RB"))
                    {
                        triggerEvent(event.RIGHTBUMP);
                        controller.vibrate(0, 1);
                    }
                    //Retrieval zone reached
                    if(scanned.substring(1).equals("LS"))
                    {
                        triggerEvent(event.RETSENS);
                    }
                    //Cliff found
                    if(scanned.substring(1).equals("CF"))
                    {
                        triggerEvent(event.CLIFFSENS);
                    }
                    //Border Reached
                    if(scanned.substring(1).equals("BB"))
                    {
                        triggerEvent(event.BORDERSENS);                        
                    }
                    //Bot is stopped
                    if(scanned.substring(1).equals("Stop"));
                    {
                        //update stoped notificaton
                        stopText.setFill(Color.YELLOW);
                        stopText.setOpacity(1);
                        
                        //turn off other movement notifications
                        moveText.setFill(Color.BLACK);
                        moveText.setOpacity(0.1);
                        turnTextL.setFill(Color.BLACK);
                        turnTextL.setOpacity(0.1);
                        turnTextR.setFill(Color.BLACK);
                        turnTextR.setOpacity(0.1);
                    }
                    //Bot moving
                    if(scanned.substring(1).equals("Move"))
                    {
                        //update moving notification
                        moveText.setFill(Color.YELLOW);
                        moveText.setOpacity(1);
                        //Turn off stop notification
                        stopText.setFill(Color.BLACK);
                        stopText.setOpacity(0.1);
                    }
                    //Currently sweeping
                    if((scanned.length() == 2 && scanned.equals("SS") || (scanned.length() == 3 && scanned.substring(1).equals("SS"))))
                    {
                        //turn on sweeping notification  
                        for(int i = 0; i < 5; i ++)
                        {
                            updateDetectionText("");
                        }
                        index = 0;
                        sweepText.setFill(Color.YELLOW);
                        sweepText.setOpacity(1);
                    }
                    //Finished sweeping
                    if(scanned.substring(1).equals("FS"))
                    {
                        //turn off sweeping notification
                        sweepText.setFill(Color.BLACK);
                        sweepText.setOpacity(0.1);
                    }
                    //Bot turning right
                    if(scanned.substring(1).equals("TR"))
                    {
                        turnTextR.setFill(Color.YELLOW);
                        turnTextR.setOpacity(1);
                    }
                    //Bot turning left
                    if(scanned.substring(1).equals("TL"))
                    {
                        turnTextL.setFill(Color.YELLOW);
                        turnTextL.setOpacity(1);
                    }
                    //Object detection logic
                    //End of object detection string
                    if(scanned.substring(1).equals("end"))
                    {
                        object = false;      
                        System.out.println("Updating");
                        updateMap();
                    }
                    //Detect objects and add to pillar array
                    if(object)
                    {


                        String[] strs = scanned.substring(1).split(" ");
                        int angle = Integer.parseInt(strs[0]);
                        double distance = Double.parseDouble(strs[1]);
                        if(distance <= 85)
                        {
                            double width = Double.parseDouble(strs[2]);
                            addPillar(angle, distance, width);
                        }
                    }
                    //Start of object detection string
                    if(scanned.substring(1).equals("Object"))
                    {
                        object = true;
                    }
                    sc.close();
                    
                } catch (IOException ex) {
                    Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    private void setStatusBox()
    {
        
        //Set up status box
        botStatusBox = new Rectangle();
        botStatusBox.setX(SCENEWIDTH * 0.65);
        botStatusBox.setY(SCENEHEIGHT * 0.55);
        botStatusBox.setWidth(600); 
        botStatusBox.setHeight(225);
        botStatusBox.setArcWidth(botStatusBox.getWidth()* 0.0714);
        botStatusBox.setArcHeight(botStatusBox.getHeight() * 0.114);
        botStatusBox.setFill(Color.CYAN);
        botStatusBox.setOpacity(0.3);
        
        //Initialize sweep text
        sweepText = new Text("Sweeping");
        sweepText.setX(SCENEWIDTH * 0.66);
        sweepText.setY(SCENEHEIGHT * 0.58);
        sweepText.setFont(Font.font("sans-serif", FontWeight.BOLD, FontPosture.REGULAR, 30));
        sweepText.setFill(Color.BLACK);
        sweepText.setOpacity(0.1);
        
        //Initialize moving Text
        moveText = new Text("Moving");
        moveText.setX(SCENEWIDTH * 0.66);
        moveText.setY(SCENEHEIGHT * 0.62);
        moveText.setFont(Font.font("sans-serif", FontWeight.BOLD, FontPosture.REGULAR, 30));
        moveText.setFill(Color.BLACK);
        moveText.setOpacity(0.1);
        
        //Initialize stop text
        stopText = new Text("Stopped");
        stopText.setX(SCENEWIDTH * 0.66);
        stopText.setY(SCENEHEIGHT * 0.66);
        stopText.setFont(Font.font("sans-serif", FontWeight.BOLD, FontPosture.REGULAR, 30));
        stopText.setFill(Color.BLACK);
        stopText.setOpacity(0.1);
        
        //Initialize turning text
        turnTextL = new Text("Turning Left");
        turnTextL.setX(SCENEWIDTH * 0.66);
        turnTextL.setY(SCENEHEIGHT * 0.7);
        turnTextL.setFont(Font.font("sans-serif", FontWeight.BOLD, FontPosture.REGULAR, 30));
        turnTextL.setFill(Color.BLACK);
        turnTextL.setOpacity(0.1);
        
        turnTextR = new Text("Turning Right");
        turnTextR.setX(SCENEWIDTH * 0.66);
        turnTextR.setY(SCENEHEIGHT * 0.74);
        turnTextR.setFont(Font.font("sans-serif", FontWeight.BOLD, FontPosture.REGULAR, 30));
        turnTextR.setFill(Color.BLACK);
        turnTextR.setOpacity(0.1);
    }
    
    //connect = current angle
    private void makeConnect()
    {
        connect = new Rectangle();
        connect.setX(SCENEWIDTH * 0.05);
        connect.setY(SCENEHEIGHT * 0.872);
        connect.setWidth(400);
        connect.setHeight(130);
        connect.setArcWidth(connect.getWidth()* 0.0714);
        connect.setArcHeight(connect.getHeight() * 0.114);
        connect.setFill(Color.WHITE);
        connect.setOpacity(0.3);
        angle = new Text("0 degrees");
        angle.setX(SCENEWIDTH * 0.05 + 75);
        angle.setY(SCENEHEIGHT * 0.95);
        angle.setFont(Font.font("sans-serif", FontWeight.BOLD, FontPosture.REGULAR, 50));
        angle.setFill(Color.WHITE);
        angle.setOpacity(0.7);
    }
        
    private void setGroup()
    {
        //Add all elements to root group
        root = new Group(bg);
        children = root.getChildren();
        children.add(mainHUDtext);
        children.add(arc);
        children.add(detectionTextBox);
        children.add(detectionAlertBox);
        children.add(botStatusBox);
        children.add(leftBumper);
        children.add(rightBumper);
        children.add(borderSensor);
        children.add(cliffSensor);
        children.add(retrievalZone);
        children.add(sweepText);   
        children.add(stopText);
        children.add(moveText);
        children.add(turnTextL);
        children.add(turnTextR);
        children.add(connect);
        children.add(angle);
        children.addAll(detectionText);
        children.add(bot);
        children.add(botL);
        children.add(botR);
        children.addAll(mapPoints);
    }
    
    //Shift current detection text down one line and add next line
    private void updateDetectionText(String text)
    {
        for(int i = detectionText.size() - 1; i > 0; i--)
        {
            detectionText.get(i).setText(detectionText.get(i - 1).getText());
        }
        detectionText.get(0).setText(text);
    }
    
    private void updateMap()
    {
        //Hide all current map points
        for(int i = 0; i < mapPoints.length; i++)
        {
            mapPoints[i].setFill(Color.TRANSPARENT);
        }
        //Update map points and detection text with new pillar info
        for(int i = 0; i < pillars.length; i++)
        {
            if(pillars[i] == null)
            {
                break;
            }
           mapPoints[i].setCenterX(pillars[i].centerX());
           mapPoints[i].setCenterY(pillars[i].centerY());
           mapPoints[i].setRadius(pillars[i].getRadius());
           mapPoints[i].setFill(pillars[i].getCircle().getFill());
           updateDetectionText("Object " + pillars[i].getDistance() + " cm away at " + pillars[i].getAngle() + " degrees" + ", width: " + pillars[i].getWdith());
           pillars[i] = null;
        }
    }
    
    //Adds a new pillar to the pillar array
    private void addPillar(int angle, double dist, double width)
    {
        if(index >= 5)
        {
            index = 0;
        }
       Pillar p = new Pillar(angle, dist, width);
       pillars[index++] = p;
    }
    
    //-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //Private Pillar Class
    //-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    private class Pillar {

    private Circle c;
    private double dist;
    private double angle;
    public double width;

    public Pillar(double angle, double dist, double width)
    {
        this.dist = dist;
        this.angle = angle;
        angle = (angle * 3.14159) / 180; //convert angle from degrees to radians
        this.width = width;
        int x0 = SCENEWIDTH / 2;
        int y0 = SCENEHEIGHT;
        double cm = 228 / 80f; //pixels per cm
        double x = dist * Math.cos(angle); //x dist in cm
        double y = dist * Math.sin(angle); //y dist in cm
        c = new Circle();
        c.setRadius((width / 2) * cm);
        c.setCenterX(x0 + (x * cm));
        c.setCenterY(y0 - (y * cm));
        if(isTargetPillar())
        {
            c.setFill(Color.GREEN);
        }
        else
        {
            c.setFill(Color.RED);
        }
    }
    
    //Return the map representation of the pillar
    public Circle getCircle()
    {
        return c;
    }
    
    //Return the center x coordinate of the circle
    public double centerX()
    {
        return c.getCenterX();
    }
    
    //Return the center y coordinate of the circle
    public double centerY()
    {
        return c.getCenterY();
    }
    
    //Returns distance from bot
    public double getDistance()
    {
        return dist;
    }
    
    //Returns width of pillar
    public double getWdith()
    {
        return width;
    }
    
    //Returns radius of map circle
    public double getRadius()
    {
        return c.getRadius();
    }
    
    //Returns pillars angle from bot
    public double getAngle()
    {
        return angle;
    }
    
    //Returns true if the pillar is one of the thinner target pillars
    public boolean isTargetPillar()
    {
        return width < 5.25;
    }
}
    //-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //Private Controller class    
    //-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    private class Controller extends XboxControllerAdapter
    {
        private XboxController x;
        private int i;
        
        public Controller()
        {
            x = new XboxController();
            x.addXboxControllerListener(this);
            i = 0;
        }
        
        //vibrates the left and/or right half of the controller
        public void vibrate(int left, int right)
        {
            if(right > 0)
            {
                right = 65535 / 2;
            }
            if(left > 0)
            {
                left = 65535 / 2;
            }
            x.vibrate(left, right, 333);
        }
        /*
         * Triggers signal movement, 
         * Bumpers signal turns,
         * ABXY signal magnitude
         */
        
        @Override
        public void rightTrigger(double value)
        {
            if(value == 1)
            {
                //Move forward
                comm.sendByte('1'); 
            }
        }
        @Override
        public void leftTrigger(double value)
        {
            if(value == 1)
            {
                //Move backwards
                comm.sendByte('2'); 
            }
        }
        @Override
        public void leftShoulder(boolean pressed)
        {
            if(pressed)
            {
                //Turn left
                turned = 'l';
                comm.sendByte('3');
            }
        }
        @Override
        public void rightShoulder(boolean pressed)
        {
            if(pressed)
            {
                //Turn right
                turned = 'r';
                comm.sendByte('4');
            }
        }
        @Override
        public void buttonA(boolean pressed)
        {
            //Largest magnitude
            if(pressed)
            {
                //Update current angle if turned
                if(turned == 'r')
                {
                    updateAngle('r', 90);
                }
                else if(turned == 'l')
                {
                    updateAngle('l', 90);
                }
                comm.sendByte('d');
                //Reset turned flag
                turned = '0';
                angle.setText(currentAngle + " degrees");
            }
        }
        @Override
        public void buttonX(boolean pressed)
        {
            //2nd largest magnitude
            if(pressed)
            {
                  if(turned == 'r')
                {
                    updateAngle('r', 45);
                }
                else if(turned == 'l')
                {
                    updateAngle('l', 45);
                }
                comm.sendByte('c');
                turned = '0';
                angle.setText(currentAngle + " degrees");
            }
        }
        @Override
        public void buttonY(boolean pressed)
        {
            //Second smallest magnitude
            if(pressed)
            {
                  if(turned == 'r')
                {
                    updateAngle('r', 10);
                }
                else if(turned == 'l')
                {
                    updateAngle('l', 10);
                }
                comm.sendByte('a');
                turned = '0';
                angle.setText(currentAngle + " degrees");
            }
        }
        @Override
        public void buttonB(boolean pressed)
        {
            //Smallest magnitude
            if(pressed)
            {
                if(turned == 'r')
                {
                    updateAngle('r', 25);
                }
                else if(turned == 'l')
                {
                    updateAngle('l', 25);
                }
                comm.sendByte('b');
                turned = '0';
                angle.setText(currentAngle + " degrees");
            }
        }
        @Override
        public void start(boolean pressed)
        {
            //Perform sweep
            if(pressed)
            {
                comm.sendByte('5');
                turned = '0';
                System.out.println("Pressed Start");
            }
        }
        @Override
        public void back(boolean pressed)
        {
            //Set current angle to 0
            if(pressed)
            {
                currentAngle = 0;
                turned = '0';
                angle.setText("0 degrees");
            }
        }
        
        //Update angle for turn, keep angle between [-180, 180] degrees
        public void updateAngle(char t, int ang)
        {
            if(t == 'r')
                {
                    currentAngle -= ang;
                }
                else if(t == 'l')
                {
                    currentAngle += ang;                    
                }
                if(currentAngle < -180)
                {
                    currentAngle += 360;
                }
                else if(currentAngle > 180)
                {
                    currentAngle -= 360;
                }
        }
    }
    //-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    public static void main(String[] args)
    {
        
        launch(args);
       
    }
}