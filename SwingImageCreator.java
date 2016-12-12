// SwingImageCreator.java

package ch.aplu.jaw;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.*;

/**
 *  Class to export Swing components into a Buffered Image
 *  by Jacobus Steenkamp's article:Bringing Swing to the WEB, Sun, http://java.net
 *
 * @author Jacobus Steenkamp
 */

public class SwingImageCreator {

   /**
    * Creates a buffered image of type TYPE_INT_RGB
    * from the supplied component. This method will
    * use the preferred size of the component as the
    * image's size.
    * @param component the component to draw
    * @return an image of the component
    */
   public static BufferedImage createImage(JComponent component){
      return createImage(component, BufferedImage.TYPE_INT_RGB);
   }

   /**
    * Creates a buffered image (of the specified type)
    * from the supplied component. This method will use
    * the preferred size of the component as the image's size.
    * @param component the component to draw
    * @param imageType the type of buffered image to draw
    *
    * @return an image of the component
    */
   public static BufferedImage createImage(JComponent component,
                                           int imageType){
      Dimension componentSize = component.getPreferredSize();
      component.setSize(componentSize); //Make sure these
                                        //are the same
      BufferedImage bi = new BufferedImage(componentSize.width,
                                           componentSize.height,
                                           imageType);
      Graphics2D g2D = bi.createGraphics();
      g2D.fillRect(0, 0, bi.getWidth(), bi.getHeight());
      component.paint(g2D);
      return bi;
   }
}