// JawGadget.java
/*
  This software is Open Source Free Software, so you may
    - run the code for any purpose
    - study how the code works and adapt it to your needs
    - integrate all or parts of the code in your own programs
    - redistribute copies of the code
    - improve the code and release your improvements to the public
  However the use of the code is entirely your responsibility.
 */

package ch.aplu.jaw;

import java.awt.*;
import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.awt.image.*;
import javax.imageio.ImageIO;


/**
 * Class to create transparent frameless desktop windows
 * with selectable opacity and tray icons with standard or balloon tool tips.
 * The window can be dragged by pressing the left mouse button.<br>
 * Only supported on Windows 2000 and higher.<br><br>
 *
 * Mouse events are reported from native code via a native circular FIFO event message
 * queue that is periodically polled by a special Java thread. This thread calls the
 * mouseEvent() method of the NativeMouseListener. mouseEvent() is synchronized with
 * the registered NativeMouseListener.
 *
 * @author Aegidius Pluess
 *
 * @version 1.0.2
 */
public class JawGadget
{

// ------------ Inner class GadgetEventThread ---------------------
private class GadgetEventThread extends Thread
{
  int[] values = new int[3];

  GadgetEventThread()
  {
    nh.createBuf(values, 1000);
  }

  public void run()
  {
    int waitCycle = 0;
    while (isThreadRunning)
    {
      if (isEventsEnabled)
      {
        synchronized(nativeMouseListener)
        {
          // Poll native buffer
          if (nh.readBuf(3) == 3)
          {
            mouse.setValues(values[0], values[1], values[2]);
            nativeMouseListener.mouseEvent(mouse);
          }
          // Check for key event
          if (nativeKeyListener != null)
          {
            if (waitCycle == 0)  // Check only each 100 ms
            {
              waitCycle = 10;
              int keyCodeExt = nh.invoke(-5);
              if (keyCodeExt != 0)
              {
                 int keyCode = keyCodeExt & 0x00FF;
                 int modifier = (keyCodeExt & 0xFF00) >> 8;
                 nativeKeyListener.keyPressed(keyCode, modifier);
              }
            }
            else
              waitCycle--;
          }
        }
      }

      try
      {
        Thread.currentThread().sleep(10);
      }
      catch (InterruptedException ex) {}

    }
  }
}

  // Native variables
  String imagePath;
  String windowTitle;
  String enumInfo;
  boolean isTopMost;

  // Instance variables
  private NativeHandler nh;  // "Has-a" relationship
  private GadgetEventThread gadgetEventThread;
  private String dll = "jawgadget";
  private boolean isWindowVisible = true;
  private boolean isImageLoaded = false;

  private NativeKeyListener nativeKeyListener = null;
  private NativeMouseListener nativeMouseListener = null;
  private NativeMouse mouse = new NativeMouse();
  private volatile boolean isThreadRunning;
  private volatile boolean isEventsEnabled;

  /**
   * Creates a JawGadet instance and allocates native resources.
   * Neither an image nor an icon is shown.
   * If changeCursor is true, the cursor icon is changed to a 'hand' when
   * the cursor position is inside the JawGadet.
   * Resources must be released by calling destroy().
   *
   * @see #destroy()
   */
  public JawGadget(boolean changeCursor)
  {
    nh = new NativeHandler(dll, this, 0, "",  // select handler #0, no title
                           0, 0, // Position
                           1, 1,  // Don't use heigth = width = 0,
                                  // no WM_PAINT is generated
                           NativeHandler.WS_POPUP |
                           NativeHandler.WS_EX_TRANSPARENT);
    nh.expose(this);
    if (changeCursor)
      nh.invoke(-4); // Change cursor to 'HAND'
  }

  /**
   * Creates a JawGadet instance and allocates native resources.
   * Neither an image nor an icon is shown.
   * Resources must be released by calling destroy().
   *
   * @see #destroy()
   */
  public JawGadget()
  {
    this(false);
  }


  /**
   * Shows image given by imagePath at given upper left corner (ulx, uly)
   * with given opaque percentage (0..100) and given transparency color.
   * isTopMost determines, if the windows remains a top level window, e.g. is always
   * in the foreground of all other windows.<br>
   * Pixels with the given RGB color (red, green, blue) will be transparent.<br>
   * jpeg, gif, wmf and bmp image format is supported. Uncompressed formats are appropriate
   * for transparent windows, because white areas becoming transparent are maintained.<br>
   * Does not return, until native system reports successful image load.
   * If imagePath = null, the previously loaded image is used.
   *
   * @param imagePath the filename of the image file. If drive:\dir is missing, the directory of the Java class file is assumed
   * @param percent the percentage of opacity in range 0..100
   * @param color the RGB color of pixels, that will get transparent
   * @param isTopMost sets the window to the topmost level in windows z-order
   * @param ulx the x-coordinate of the window's upper left corner
   * @param uly the y-coordinate of the window's upper left corner
   */
  public void showImage(String imagePath, int percent, Color color, boolean isTopMost,
                        int ulx, int uly)
  {
    this.isTopMost = isTopMost;
    loadImage(imagePath, new Point(ulx, uly), percent,
              color.getRed(), color.getGreen(), color.getBlue());
  }

  /**
   * Same as showImage(imagePath, percent, color, isTopMost,  ulx, uly), but uses the current position.
   *
   * @param imagePath the filename of the image file. If drive:\dir is missing, the directory of the Java class file is assumed
   * @param percent the percentage of opacity in range 0..100
   * @param isTopMost sets the window to the topmost level in windows Z-order
   * @param color the RGB color of pixels, that will get transparent
   *
   * @see #showImage(String imagePath, int percent, Color color, boolean isTopMost, int ulx, int uly)
   */
  public void showImage(String imagePath, int percent, Color color, boolean isTopMost)
  {
    this.isTopMost = isTopMost;
    loadImage(imagePath, null, percent,
              color.getRed(), color.getGreen(), color.getBlue());
  }


  /**
   * Same as showImage(imagePath, percent, color, isTopMost, int ulx, int uly),
   * but white pixels will be transparent.
   * White color is defined as RGB(255, 255, 255).
   * @param imagePath the filename of the image file. If drive:\dir is missing, the directory of the Java class file is assumed
   * @param percent the percentage of opacity in range 0..100
   * @param isTopMost sets the window to the topmost level in windows Z-order
   * @param ulx the x-coordinate of the window's upper left corner
   * @param uly the y-coordinate of the window's upper left corner
   *
   * @see #showImage(String imagePath, int percent, Color color, boolean isTopMost, int ulx, int uly)
   */
  public void showImage(String imagePath, int percent, boolean isTopMost, int ulx, int uly)
  {
    this.isTopMost = isTopMost;
    loadImage(imagePath, new Point(ulx, uly), percent, 255, 255, 255);
  }

  /**
   * Same as showImage(imagePath, percent, isTopMost, int ulx, int uly), but uses the current position.
   * White pixels with RGB(255, 255, 255) will be transparent.
   * @param imagePath the filename of the image file. If drive:\dir is missing, the directory of the Java class file is assumed
   * @param percent the percentage of opacity in range 0..100
   * @param isTopMost sets the window to the topmost level in windows Z-order
   *
   * @see #showImage(String imagePath, int percent, boolean isTopMost, int ulx, int uly)
   */
  public void showImage(String imagePath, int percent, boolean isTopMost)
  {
    this.isTopMost = isTopMost;
    loadImage(imagePath, null, percent, 255, 255, 255);
  }


  /**
   * Hides the image.
   * The allocated resources are not released and the current position of the window is maintained.
   *
   * @see #destroy()
   */
  public void hideImage()
  {
    nh.hideWindow();
  }

  /**
   * Closes the native window or tray icon and releases all native resources.
   * Should be called whenever the program is about to terminate.
   */
  public void destroy()
  {
    if (isThreadRunning)
    {
      isThreadRunning = false;
      try
      {
        gadgetEventThread.join(2000);  // Wait maximum 2s
      }
      catch (InterruptedException ex) {}
    }
    nh.destroy();
  }

 /**
  * Registers the given listener, in order to get mouse events from the native window.
  * The given iconEventMask is an OR-combination of the following static flags of
  * class NativeMouse:<br>
  *   - left mouse button down:                                   lPress<br>
  *   - left mouse button up:                                     lRelease<br>
  *   - left mouse button clicked (fast down/up):                 lClick<br>
  *   - left mouse button double-clicked (fast down/up/down/up):  lDClick<br>
  *   - same for right mouse button:    rPress, rRelease, rClick, rDlick<br>
  *   - mouse enters window:                                      enter<br>
  *   - mouse leaves window:                                      leave<br>
  *   - mouse moved inside window:                                move<br><br>
  * When a mouse event occurs, the implementation of NativeMouseListener.mouseEvent(NativeMouse mouse)
  * is called.
  *
  * @see ch.aplu.jaw.NativeMouse
  * @see ch.aplu.jaw.NativeMouseListener
  */
  public void addNativeMouseListener(NativeMouseListener listener, int mouseEventMask)
  {
    nh.flushBuf();
    nativeMouseListener = listener;
    nh.invoke(mouseEventMask);
    if (!isThreadRunning)
    {
      isThreadRunning = true;
      isEventsEnabled = true;
      gadgetEventThread =
         new GadgetEventThread();
      gadgetEventThread.start();
    }
  }

  public void addNativeKeyListener(NativeKeyListener listener)
  {
    nativeKeyListener = listener;
  }

 /**
  * Registers the given listener, in order to get mouse events when the mouse
  * cursor is inside the tray icon.
  * The given iconEventMask is an OR-combination of the following static flags of
  * class NativeMouse:<br>
  *   - left mouse button down:                                   lPress<br>
  *   - left mouse button up:                                     lRelease<br>
  *   - left mouse button clicked (fast down/up):                 lClick<br>
  *   - left mouse button double-clicked (fast down/up/down/up):  lDClick<br>
  *   - same for right mouse button:    rPress, rRelease, rClick, rDlick<br>
  *   - mouse moved (coordinates not retrievable):                move<br><br>
  *
  * When a mouse event occurs, the implementation of TrayIconListener.iconEvent(TrayIcon icon)
  * is called.
  *
  * @see ch.aplu.jaw.NativeMouse
  * @see ch.aplu.jaw.TrayIcon
  * @see ch.aplu.jaw.TrayIconListener
  *
  */
  public void addTrayIconListener(TrayIconListener listener,
                                  int iconEventMask)
  {
    nh.addTrayIconListener(listener, iconEventMask);
  }

  /**
   * Same as addTrayIconListener(listener, iconEventMask),
   * but only lRelease is registered.
   *
   * @see #addTrayIconListener(TrayIconListener listener, int iconEventMask)
   **/
  public void addTrayIconListener(TrayIconListener listener)
  {
    nh.addTrayIconListener(listener, NativeMouse.lRelease);
  }

  /**
   * Shows tray icon given by iconPath.
   * Size of icon is normally 32x32 bit with some transparent areas.<br>
   * Windows 2000 supports icons of a color depth up to the current display mode.<br>
   * Windows XP supports icons up to 32 BPP.
   * When the mouse cursor is moved into the icon,
   * a standard tool tip (rectangular shape) holding the given text is displayed.
   *
   * @param iconPath the filename of the icon file. If drive:\dir is missing, the directory of the Java class file is assumed
   * @param tooltip the text to be displayed in the tool tip area. When empty, no tool tip is shown
   */
  public void showIcon(String iconPath, String tooltip)
  {
    nh.showIcon(iconPath, tooltip);
  }

  /**
   * Same as showIcon(iconPath, tooltip), but no tool tip is shown.
   *
   * @param iconPath the filename of the icon file. If drive:\dir is missing, the directory of the Java class file is assumed
   *
   * @see #showIcon(String iconPath, String tooltip)
   */
  public void showIcon(String iconPath)
  {
    nh.showIcon(iconPath);
  }

  /**
   * Hides the tray icon.
   * The allocated resources are not released.
   *
   * @see #destroy()
   **/
  public void hideIcon()
  {
    nh.hideIcon();
  }

  /**
   * Shows a ballon tool tip at the tray icon that contains the given title
   * and text for the given timeout time.<br>
   * Minimum and maximum timeout is enforced by the operation system.<br>
   * Given flag selects a small icon to the balloon. Values:<br>
   * 0: no icon<br>
   * 1: info icon (letter i)<br>
   * 2: warning icon (exclamation)<br>
   * 3: error icon (cross)
   *
   * @param title the title (up to 63 characters) shown bold in the tool tip. When empty, no title is shown
   * @param text the text (up to 253 characters) to be displayed in the tool tip area. When empty, no text is shown
   * @param flag an integer 0..3 selecting the type of a small icon
   * @param timeout the time in milliseconds until the tool tip disappears
   */
  public void showBalloonTooltip(String title, String text, int flag, int timeout)
  {
    nh.showBalloonTooltip(title, text, flag, timeout);
  }

  /**
   * Same as showBalloonTooltip(title, text, flag, timeout) with timeout = 2000.
   *
   * @see #showBalloonTooltip(String title, String text, int flag, int timeout)
   */
  public void showBalloonTooltip(String title, String text, int flag)
  {
    nh.showBalloonTooltip(title, text, flag, 2000);
  }

  /**
   * Same as showBalloonTooltip(title, text, flag, timeout)
   * with flag = 0 and timeout = 2000.
   *
   * @see #showBalloonTooltip(String title, String text, int flag, int timeout)
   */
  public void showBalloonTooltip(String title, String text)
  {
    nh.showBalloonTooltip(title, text, 0, 2000);
  }

  /**
   * Waits the given amount of time using Thread.currentThread.sleep().
   *
   * @param time the time to wait (in milliseconds)
   */
  public void sleep(int time)
  {
    try
    {
      Thread.currentThread().sleep(time);
    }
    catch (InterruptedException ex) {}
  }

  /**
   * Transforms the given component and stores it in a BMP image file.
   * The component should be a Swing component. The component's paintComponent()
   * method is called to render the image. Override this method and do
   * your graphics operation there. The component must be packed into a
   * unvisible JFrame, in order to enforce the layout. The code may be tested,
   * by displaying the JFrame as a Java screen window.  A setVisible(true) must suffice.<br>
   *
   * The file can then be loaded in a JawGadget window.<br>
   * Use this method in a synchronized block, because it should not be interrupted by another thread.
   *
   * @param component a packed Swing JComponent
   * @param file output file
   */
  public void writeBMP(JComponent component, File file)
  {
    BufferedImage bi =
      SwingImageCreator.createImage(component, BufferedImage.TYPE_3BYTE_BGR);
      // Must use TYPE_3BYTE_BGR in order to maintain white portions for transparency
    try
    {
      FileOutputStream fos = new FileOutputStream(file);
      ImageIO.write(bi, "BMP", fos);
      fos.close();
    }
    catch (IOException ex)
    {
      throw new RuntimeException("Can't create BMP image");
    }
  }

  /**
   * Same as writeMMP(component, file) with output pathname.
   *
   * @param component a packed Swing JComponent
   * @param pathname the file path of the output file. If drive:\dir is missing, the directory of the Java class file is assumed
   */
  public void writeBMP(JComponent component, String pathname)
  {
    writeBMP(component, new File(pathname));
  }


  /**
   * Enables or disables the callbacks from mouse events.
   * If disabled, mouse events are completely lost and
   * do not remain in the event queue for later processing.
   *
   * @param enable if true, the mouse events are enabled, otherwise disabled
   */
  public void enableMouseEvents(boolean enable)
  {
    isEventsEnabled = enable;
  }


  /**
   * Activates the Gadget's window.
   * If successful return true; otherwise false
   */
  public boolean activate()
  {
    int rc = nh.invoke(-2);
    if (rc == 0)
      return false;
    return true;
  }

  /**
   * Activates window with given title. Returns true, if successfull.
   *
   * @param windowTitle the text in the window's title bar.
   */
  public boolean activate(String windowTitle)
  {
    this.windowTitle = windowTitle;
    int rc = nh.invoke(-3);
    if (rc == 0)
      return false;
    return true;
  }

  /**
   * Returns windows's upper left x-coordinate.
   */
  public int getX()
  {
    return nh.invoke(-6);
  }

  /**
   * Returns windows's upper left y-coordinate.
   */
  public int getY()
  {
    return nh.invoke(-7);
  }

  /**
   * Returns an enumeration of non-empty titles of all current windows.
   * Titles are separated by the newline character.
   */
  public String enumWindows()
  {
    nh.invoke(-8);
    return enumInfo;
  }

  /**
   * Move the window to given position.
   * @param ulx the x-coordinate of the window's upper left corner
   * @param uly the y-coordinate of the window's upper left corner
   */
  public void setPosition(int ulx, int uly)
  {
    nh.setWindowPosition(ulx, uly);
  }

  // ========================== Private methods ================================
  private void loadImage(String imagePath, Point ulc, int percent,
                         int red, int green, int blue)
  {
    if (imagePath != null)
    {
      this.imagePath = getFullPath(imagePath);
      nh.invoke(-1);  // Load image in native code
      isImageLoaded = true;
//      System.out.print("Loading image now...");
    }
    else
      if (!isImageLoaded)
          throw new RuntimeException("showImage() with imagePath = null, but image not yet loaded.");

    if (ulc != null)
      nh.setWindowPosition(ulc.x, ulc.y);
    if (imagePath != null)
      nh.showWindow(percent, red, green, blue, true);  // Wait until loading is complete
    else
      nh.showWindow(percent, red, green, blue, false);
  }

  private String getFullPath(String imagePath)
  {
    boolean fileFound = false;
    String fullPath;
    String fileName = "";
    String userHome = "";

    // Check if file exists
    File imageFile = new File(imagePath);
    fullPath = imageFile.getAbsolutePath();
    fileName = imageFile.getName();
    if (imageFile.exists())
      fileFound = true;

    if (fileFound)
      return fullPath;
    else
      throw new RuntimeException("Image file\n" + fileName + "\nnot found " +
                                 "in given or default directory");
  }

}
