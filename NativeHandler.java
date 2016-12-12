// NativeHandler.java
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

/**
 * Class that provides the link between Java and native code for the JAW (Java Api Wrapper).
 * Native code must reside in a Windows DLL, created with the distributed
 * C++-library jaw.lib. Consult the distribution of the JAW package for more
 * information and examples.<br><br>
 * Strongly inspired from Tal Liron's article 'Enhance your Java application
 * with Java Native Interface'
 *
 */
public class NativeHandler
{

// ------------ Inner class ---------------------
class IconEventThread extends Thread
{
  public void run()
  {
    while (isThreadRunning)
    {
      // Poll native buffer
      synchronized(trayIconListener)
      {
        if (readIconEventBuf(iconEventBuf, 1) == 1)
        {
          trayIcon.setEvent(iconEventBuf[0]);
          trayIconListener.iconEvent(trayIcon);
        }
      }
      try
      {
        Thread.currentThread().sleep(50);
      }
      catch (InterruptedException ex) {}
    }
//    System.out.println("IconEventThread.run() terminated");
  }
}
// -----------------------------------------------

  // Data block to be retrieved by native code
  // in order to simplify the parameter passing to NativeWindow ctor
  protected long ref = 0;  // Pointer to NativeWindow instance
  private Object exposedObj = null;
  private int select;
  private String title;
  private int xPos;
  private int yPos;
  private int width;
  private int height;
  private long windowStyle;
  private String iconPath = null;
  private String tooltip = "";
  private String balloon_tooltip_title = "";
  private String balloon_tooltip = "";
  private int iconEventMask;
  // End of data block

  private final int defaultBufSize = 1000;
  private boolean isEnabled = false;
  private TrayIconListener trayIconListener;
  private TrayIcon trayIcon = new TrayIcon();

  private final int iconEventBufSize = 100;
  private int[] iconEventBuf = new int[iconEventBufSize];
  private boolean isTrayIconVisible = false;
  private volatile boolean isThreadRunning = false;
  private IconEventThread iconEventThread;

  /**
   * Creates an overlapped window. An overlapped window has a title bar and a border.
   */
  public static final long WS_OVERLAPPED = 0x00000000L;

  /**
   * Creates a window that has a System-menu box in its title bar. The WS_CAPTION style must also be specified.
   */
  public static final long WS_SYSMENU = 0x00080000L;

  /**
   * Creates a window that has a thin-line border.
   */
  public static final long WS_BORDER = 0x00800000L;

  /**
   * Creates a pop-up window.
   */
  public static final long WS_POPUP = 0x80000000L;

  /**
   * Creates a window that has a Minimize button.
   */
  public static final long WS_MINIMIZEBOX = 0x00020000L;

  /**
   * Creates a window that has a Maximize button.
   */
  public static final long WS_MAXIMIZEBOX = 0x00010000L;

  /**
   * Creates a window that has a sizing border.
   */
  public static final long WS_THICKFRAME = 0x00040000L;

  /**
   * Creates a window that has a title bar (includes the WS_BORDER style).
   */
  public static final long WS_CAPTION = 0x00C00000L;

  /**
   * Creates a window that is initially visible.
   */
  public static final long WS_VISIBLE = 0x10000000L;

  /**
   * Creates a window that is transparent (partially opaque).
   */
  public static final long WS_EX_TRANSPARENT = 0x00000020L;
//  public static final long WS_CHILD = 0x40000000L;  // Not allowed in JAW

  /**
   * Creates a window that is initially minimized.
   */
  public static final long WS_MINIMIZE = 0x20000000L;

  /**
   * Creates a window that is initially maximized.
   */
  public static final long WS_MAXIMIZE = 0x01000000L;

  /**
   * Creates a window that has a border of a style typically used with dialog boxes.
   * A window with this style cannot have a title bar.
   */
  public static final long WS_DLGFRAME = 0x00400000L;

  /**
   * Creates a window that has a vertical scroll bar.
   */
  public static final long WS_VSCROLL = 0x00200000L;

  /**
   * Creates a window that has a horizontal scroll bar.
   */
  public static final long WS_HSCROLL = 0x00100000L;


  /**
   * Same as WS_SYSMENU | WS_MAXIMIZEBOX | WS_MINIMIZEBOX | WS_THICKFRAME | WS_CAPTION | WS_VISIBLE

   */
  public static final long WS_DEFAULT = WS_SYSMENU |
                                        WS_MAXIMIZEBOX |
                                        WS_MINIMIZEBOX |
                                        WS_THICKFRAME |
                                        WS_CAPTION |
                                        WS_VISIBLE;

  /**
   * Same as WS_OVERLAPPED | WS_CAPTION | WS_SYSMENU | WS_THICKFRAME | WS_MINIMIZEBOX | WS_MAXIMIZEBOX
   */
  public static final long WS_OVERLAPPEDWINDOW = WS_OVERLAPPED |
                                                 WS_CAPTION |
                                                 WS_SYSMENU |
                                                 WS_THICKFRAME |
                                                 WS_MINIMIZEBOX |
                                                 WS_MAXIMIZEBOX;


  /**
   * Same as WS_POPUP | WS_BORDER | WS_SYSMENU
   */
  public static final long WS_POPUPWINDOW = WS_POPUP |
                                            WS_BORDER |
                                            WS_SYSMENU;


  // ================================== Private instance variables =======================
  private synchronized native void nativeEnable(int select);
  private synchronized native void nativeDestroy(long ref);
  private synchronized native void nativeSetWindowPosition(long ref, int xPos, int yPos);
  private synchronized native void nativeSetWindowSize(long ref, int width, int height);
  private synchronized native void nativeShowWindow(long ref, boolean isVisible, int percent,
                                              int red, int green, int blue, boolean wait);
  private synchronized native int nativeInvoke(long ref, int val);
  private synchronized native void nativeCreateBuf(long ref, boolean[] ary, int bufSize);
  private synchronized native void nativeCreateBuf(long ref, char[] ary, int bufSize);
  private synchronized native void nativeCreateBuf(long ref, byte[] ary, int bufSize);
  private synchronized native void nativeCreateBuf(long ref, short[] ary, int bufSize);
  private synchronized native void nativeCreateBuf(long ref, int[] ary, int bufSize);
  private synchronized native void nativeCreateBuf(long ref, long[] ary, int bufSize);
  private synchronized native void nativeCreateBuf(long ref, float[] ary, int bufSize);
  private synchronized native void nativeCreateBuf(long ref, double[] ary, int bufSize);
  private synchronized native int nativeReadBuf(long ref, int count);
  private synchronized native void nativeFlushBuf(long ref);
  private synchronized native int nativeCountBuf(long ref);
  private synchronized native boolean nativeIsOverflow(long ref, boolean reset);
  private synchronized native void nativeStartThread(long ref);
  private synchronized native void nativeStopThread(long ref);
  private synchronized native void nativeExpose(long ref, Object obj);
  private synchronized native void nativeCreateTrayIcon(long ref);
  private synchronized native void nativeModifyTrayIcon(long ref);
  private synchronized native void nativeDeleteTrayIcon(long ref);
  private synchronized native void nativeCreateIconEventBuf(long ref, int[] ary, int bufSize);
  private synchronized native int nativeReadIconEventBuf(long ref, int[] ary, int count);
  private synchronized native void nativeShowBalloonTooltip(long ref, int timeout, int flag);
  private int aryLength;

  /**
   * Constructs a NativeHandler and creates a native window with given title, position, size and style.
   * The given native library must be a valid windows DLL.
   * If the fully qualified path (drive:\dir\filename.dll) is given, the specified file is loaded.
   * If only the filename (without extension) is given, searches the current directory
   * and then the environment path to load the filename.dll.<br>
   * The given object reference will be used to 'expose' the object to the
   * native code, i.e. to open access to (even private) instance variables and methods
   * from the C/C++ code.<br>
   * 'select' makes the choice for one of several user-defined subclasses of
   * class WindowHandler. It may have any value, if only one subclass is defined.<br>
   * windowStyle determines the style of the native window as explained in the
   * API documentation for CreateWindow(). It is an OR-combination of the
   * static window-style-constants of this class. Not all styles are appropriate for JAW applications.
   * If no style is appropriate, pass 0.<br>
   * If window style contains WS_VISIBLE the window is immediately visible, otherwise
   * showWindow() must be called.
   *
   * @param dllPath the path of the native library (DLL). If only the filename is given, searches in current directory and environment path
   * @param exposedObj the object that opens access from native code
   * @param select the selection of a subclass of WindowHandler
   * @param title the window's title in the title bar
   * @param ulx the x-coordinate of the window's upper left corner (in pixels)
   * @param uly the y-coordinate of the window's upper left corner (in pixels)
   * @param width the width of the window (in pixels)
   * @param height the height of the window (in pixels)
   * @param windowStyle the style of the window (OR-combination of style-constants)

   * @throws UnexpectedLinkError if the given library cannot be loaded
   *
   */
  public NativeHandler(String dllPath, Object exposedObj, int select, String title,
                       int ulx, int uly, int width, int height, long windowStyle)
  {
    this.exposedObj = exposedObj;
    loadLib(dllPath);
    init(select, title, ulx, uly, width, height, windowStyle);
  }

  /**
   * Same as NativeHandler(dllPath, exposedObj, select, title, ulx, uly, width, height, windowStyle)
   * with select = 0.
   *
   * @param dllPath the path of the native library (DLL). If only the filename is given, searches in current directory and environment path
   * @param exposedObj the object that opens access from native code
   * @param title the window's title in the title bar
   * @param ulx the x-coordinate of the window's upper left corner (in pixels)
   * @param uly the y-coordinate of the window's upper left corner (in pixels)
   * @param width the width of the window (in pixels)
   * @param height the height of the window (in pixels)
   * @param windowStyle the style of the window (OR-combination of style-constants)

   * @throws UnexpectedLinkError if the given library cannot be loaded
   *
   * @see #NativeHandler(String dllPath, Object exposedObj, int select, String title,
                       int ulx, int uly, int width, int height, long windowStyle)
   */
  public NativeHandler(String dllPath, Object exposedObj, String title,
                       int ulx, int uly, int width, int height, long windowStyle)
  {
    this.exposedObj = exposedObj;
    loadLib(dllPath);
    init(0, title, ulx, uly, width, height, windowStyle);
  }

  /**
   * Same as NativeHandler(dllPath, exposedObj, select, title, ulx, uly, width, height, windowStyle)
   * with no exposed object and select = 0.
   *
   * @param dllPath the path of the native library (DLL). If only the filename is given, searches in current directory and environment path
   * @param title the window's title in the title bar
   * @param ulx the x-coordinate of the window's upper left corner (in pixels)
   * @param uly the y-coordinate of the window's upper left corner (in pixels)
   * @param width the width of the window (in pixels)
   * @param height the height of the window (in pixels)
   * @param windowStyle the style of the window (OR-combination of style-constants)

   * @throws UnexpectedLinkError if the given library cannot be loaded
   *
   * @see #NativeHandler(String dllPath, Object exposedObj, int select, String title,
                       int ulx, int uly, int width, int height, long windowStyle)
   */
  public NativeHandler(String dllPath, String title,
                       int ulx, int uly, int width, int height, long windowStyle)
  {
    loadLib(dllPath);
    init(0, title, ulx, uly, width, height, windowStyle);
  }

  /**
   * Constructs a NativeHandler to be used without the native window.
   *
   * @param dllPath the path of the native library (DLL). If only the filename is given, searches in current directory and environment path
   * @param exposedObj the object that opens access from native code
   * @param select the selection of a subclass of WindowHandler
   *
   * @throws UnexpectedLinkError if the given library cannot be loaded
  */
  public NativeHandler(String dllPath, Object exposedObj, int select)
  {
    this.exposedObj = exposedObj;
    loadLib(dllPath);
    init(select, "", 0, 0, 0, 0, 0L);
  }

  /**
   * Constructs a NativeHandler to be used without the native window.
   * Same as NativeHandler(dllPath, exposedObj, select)
   * with select = 0
   *
   * @param dllPath the path of the native library (DLL). If only the filename is given, searches in current directory and environment path
   * @param exposedObj the object that opens access from native code
   *
   * @throws UnexpectedLinkError if the given library cannot be loaded
   *
   * @see #NativeHandler(String dllPath, Object exposedObj, int select)
  */
  public NativeHandler(String dllPath, Object exposedObj)
  {
    this.exposedObj = exposedObj;
    loadLib(dllPath);
    init(0, "", 0, 0, 0, 0, 0L);
  }

  /**
   * Constructs a NativeHandler to be used without the native window.
   * Same as NativeHandler(dllPath, exposedObj, select)
   * with no exposed object
   *
   * @param dllPath the path of the native library (DLL). If only the filename is given, searches in current directory and environment path
   * @param select the selection of a subclass of WindowHandler
   *
   * @throws UnexpectedLinkError if the given library cannot be loaded
   *
   * @see #NativeHandler(String dllPath, Object exposedObj, int select)
  */
  public NativeHandler(String dllPath, int select)
  {
    loadLib(dllPath);
    init(select, "", 0, 0, 0, 0, 0L);
  }

  /**
   * Constructs a NativeHandler to be used without the native window.
   * Same as NativeHandler(dllPath, exposedObj, select)
   * with no exposed object and select = 0
   *
   * @param dllPath the path of the native library (DLL). If only the filename is given, searches in current directory and environment path
   *
   * @throws UnexpectedLinkError if the given library cannot be loaded
   *
   * @see #NativeHandler(String dllPath, Object exposedObj, int select)
   */
  public NativeHandler(String dllPath)
  {
    loadLib(dllPath);
    init(0, "", 0, 0, 0, 0, 0L);
  }

  /**
   * Allow the native code to have access to the given object.
   * Even private variables and methods are exposed.
   *
   * @param exposedObj the object that opens access from native code
   */
  public void expose(Object exposedObj)
  {
    if (isEnabled)
      nativeExpose(ref, exposedObj);
  }

  /**
  * Shows the native window.
  * Only allowed, if the NativeHandler instance was created with the appropriate windows options.
  * For transparent windows (windows style WS_EX_TRANSPARENT) the window is shown completely opaque
  * and RGB(255, 255, 255) is used as color for transparent areas
  **/
  public void showWindow()
  {
    if (isEnabled)
      nativeShowWindow(ref, true, 100, 255, 255, 255, false);
  }

  /**
  * Shows a transparent native window with given opacity.
  * Only allowed, if the NativeHandler instance was created with the appropriate windows options, especially
  * windows style WS_EX_TRANSPARENT.
  * RGB(255, 255, 255) is used as color for transparent areas.
  *
  * @param percent the percentage of opacity in range 0..100
  **/
  public void showWindow(int percent)
  {
    if (isEnabled)
      nativeShowWindow(ref, true, percent, 255, 255, 255, false);
  }

  /**
  * Shows a transparent native window with given opacity and RGB color for transparent areas.
  * Only allowed, if the NativeHandler instance was created with the appropriate windows options, especially
  * windows style WS_EX_TRANSPARENT.
  *
  * @param percent the percentage of opacity in range 0..100
  * @param red the red portion of the transparency color (0..255)
  * @param green the green portion of the transparency color (0..255)
  * @param blue the blue portion of the transparency color (0..255)
  **/
  public void showWindow(int percent, int red, int green, int blue)
  {
    if (isEnabled)
      nativeShowWindow(ref, true, percent, red, green, blue, false);
  }

  /**
  * Hides the window, but does not release any resources.
  **/
  public void hideWindow()
  {
    if (isEnabled)
      nativeShowWindow(ref, false, 100, 255, 255, 255, false);
  }

  /**
   * Moves the window to the given position (upper left corner).
   * If the window is visible, it will be put on top of all other windows.
   *
   * @param ulx the x-coordinate of the window's upper left corner (in pixels)
   * @param uly the y-coordinate of the window's upper left corner (in pixels)
   */
  public void setWindowPosition(int ulx, int uly)
  {
    if (isEnabled)
      nativeSetWindowPosition(ref, ulx, uly);
  }

  /**
   * Resizes the window to given size (width, height).
   * If the window is visible, it will be put on top of all other windows.
   *
   * @param width the width of the window (in pixels)
   * @param height the height of the window (in pixels)
   */
  public void setWindowSize(int width, int height)
  {
    if (isEnabled)
      nativeSetWindowSize(ref, width, height);
  }

  /**
   * Closes the native window and releases all native resources.
   * Should be called whenever the program is about to terminate.
   * Any further call to NativeHandler's methods will return immediatly.
   */
  public void destroy()
  {
    if (isEnabled)
    {
      isEnabled = false;
      if (isThreadRunning)
      {
        isThreadRunning = false;
        try
        {
          iconEventThread.join(2000);  // Wait maximum 2s
        }
        catch (InterruptedException ex) {}
      }
      nativeDestroy(ref);
    }
  }

  /**
   * Invokes the native function with given value.
   *
   * @param val the integer value to be passed to the native function
   *
   * @return the integer return value from the native function call
   */
  public int invoke(int val)
  {
    if (isEnabled)
      return nativeInvoke(ref, val);
    else
      return 0;
  }

  /**
   * Invokes the native function with value = 0.
   *
   * @return the integer return value from the native function call
   */
  public int invoke()
  {
    return invoke(0);
  }

  /**
   * Creates a (native) JNIBuffer of  given size and links the
   * native buffer and the the given Java buffer. The native code initializes the
   * elements of the Java buffer to 0.<br>
   * Deletes any other JNIBuffer that has been created before
   * (only one buffer can be allocated).<br>
   * The JNIBuffer is organized as a circular FIFO buffer. In a typical application
   * the native code fills it asynchronously to the Java thread. readBuf(int count)
   * transfers up to count elements (or as many as all available) from the
   * JNIBuffer to the Java buffer, where they ready to be used.<br><br>
   *
   * <code>
   * Example:<br><br>
   * char[] values = new char[4];<br>
   * createBuf(values, 6);<br><br>
   *
   * Native code fills JNIBuffer:<br>
   * writeChar('A');<br>
   * writeChar('B');<br>
   * writeChar('C'):<br><br>
   *
   * JNIBuffer<char> (circular) size 6<br>
   * -------------------------<br>
   * | A | B | C | &nbsp;  | &nbsp;  | &nbsp;  |<br>
   * -------------------------<br><br>
   *
   * Java code transfers up to 2 elements:<br>
   * readBuf(2);<br><br>
   *
   * Java buffer size 4<br>
   * -----------------<br>
   * | A | B | &nbsp; | &nbsp;  |<br>
   * -----------------<br><br>
   * </code>
   *
   * @param ary the Java array getting the native data
   * @param bufSize the size (number of elements) of the native buffer.
   *
   * @see #readBuf(int count)
   */
  public void createBuf(boolean[] ary, int bufSize)
  {
    if (isEnabled && bufSize > 0)
    {
      aryLength = ary.length;
      nativeCreateBuf(ref, ary, bufSize);
    }
  }

  /**
   * Same as createBuf(ary, bufSize) with bufSize = 1000.
   *
   * @see #createBuf(boolean[] ary, int bufsize)
   */
  public void createBuf(boolean[] ary)
  {
    createBuf(ary, defaultBufSize);
  }

  /**
   * Same as createBuf(boolean[] ary, int bufSize), but for a char array.
   *
   * @see #createBuf(boolean[] ary, int bufsize)
   */
  public void createBuf(char[] ary, int bufSize)
  {
    if (isEnabled && bufSize > 0)
    {
      aryLength = ary.length;
      nativeCreateBuf(ref, ary, bufSize);
    }
  }

  /**
   * Same as createBuf(char[] ary, int bufSize) with bufSize = 1000.
   *
   * @see #createBuf(boolean[] ary, int bufsize)
   */
  public void createBuf(char[] ary)
  {
    createBuf(ary, defaultBufSize);
  }

  /**
   * Same as createBuf(boolean[] ary, int bufSize), but for a byte array.
   *
   * @see #createBuf(boolean[] ary, int bufsize)
   */
  public void createBuf(byte[] ary, int bufSize)
  {
    if (isEnabled && bufSize > 0)
    {
      aryLength = ary.length;
      nativeCreateBuf(ref, ary, bufSize);
    }
  }

  /**
   * Same as createBuf(byte[] ary, int bufSize) with bufSize = 1000.
   *
   * @see #createBuf(boolean[] ary, int bufsize)
   */
  public void createBuf(byte[] ary)
  {
    createBuf(ary, defaultBufSize);
  }

  /**
   * Same as createBuf(boolean[] ary, int bufSize), but for a short array.
   *
   * @see #createBuf(boolean[] ary, int bufsize)
   */
  public void createBuf(short[] ary, int bufSize)
  {
    if (isEnabled && bufSize > 0)
    {
      aryLength = ary.length;
      nativeCreateBuf(ref, ary, bufSize);
    }
  }

  /**
   * Same as createBuf(short[] ary, int bufSize) with bufSize = 1000.
   *
   * @see #createBuf(boolean[] ary, int bufsize)
   */
  public void createBuf(short[] ary)
  {
    createBuf(ary, defaultBufSize);
  }

  /**
   * Same as createBuf(boolean[] ary, int bufSize), but for an int array.
   *
   * @see #createBuf(boolean[] ary, int bufsize)
   */
  public void createBuf(int[] ary, int bufSize)
  {
    if (isEnabled && bufSize > 0)
    {
      aryLength = ary.length;
      nativeCreateBuf(ref, ary, bufSize);
    }
  }

  /**
   * Same as createBuf(int[] ary, int bufSize) with bufSize = 1000.
   *
   * @see #createBuf(boolean[] ary, int bufsize)
   */
  public void createBuf(int[] ary)
  {
    createBuf(ary, defaultBufSize);
  }

  /**
   * Same as createBuf(boolean[] ary, int bufSize), but for a long array.
   *
   * @see #createBuf(boolean[] ary, int bufsize)
   */
  public void createBuf(long[] ary, int bufSize)
  {
    if (isEnabled && bufSize > 0)
    {
      aryLength = ary.length;
      nativeCreateBuf(ref, ary, bufSize);
    }
  }

  /**
   * Same as createBuf(long[] ary, int bufSize) with bufSize = 1000.
   *
   * @see #createBuf(boolean[] ary, int bufsize)
   */
  public void createBuf(long[] ary)
  {
    createBuf(ary, defaultBufSize);
  }

  /**
   * Same as createBuf(boolean[] ary, int bufSize), but for a float array.
   *
   * @see #createBuf(boolean[] ary, int bufsize)
   */
  public void createBuf(float[] ary, int bufSize)
  {
    if (isEnabled && bufSize > 0)
    {
      aryLength = ary.length;
      nativeCreateBuf(ref, ary, bufSize);
    }
  }

  /**
   * Same as createBuf(float[] ary, int bufSize) with bufSize = 1000.
   *
   * @see #createBuf(boolean[] ary, int bufsize)
   */
  public void createBuf(float[] ary)
  {
    createBuf(ary, defaultBufSize);
  }

  /**
   * Same as createBuf(boolean[] ary, int bufSize), but for a double array.
   *
   * @see #createBuf(boolean[] ary, int bufsize)
   */
  public void createBuf(double[] ary, int bufSize)
  {
    if (isEnabled && bufSize > 0)
    {
      aryLength = ary.length;
      nativeCreateBuf(ref, ary, bufSize);
    }
  }

  /**
   * Same as createBuf(double[] ary, int bufSize) with bufSize = 1000.
   *
   * @see #createBuf(boolean[] ary, int bufsize)
   */
  public void createBuf(double[] ary)
  {
    createBuf(ary, defaultBufSize);
  }

  /**
   * Reads up to count elements from a native JNIBuffer into the previously
   * created Java buffer.
   *
   * @param count the maximal number of elements to fetch from the JNIBuffer
   *
   * @return the number of elements effectively copied, -1, if the JNIBuffer
   * has not been created or count is less than 0 or greater than the
   * Java array length
   *
   * @see #createBuf(boolean[] ary, int bufsize)
   */
  public int readBuf(int count)
  {
    if (isEnabled)
    {
      if (count == 0)
        return 0;
      if (count < 0 || count > aryLength)
        return -1;
      int rc = nativeReadBuf(ref, count);
      return rc;
    }
    return -1;
  }

  /**
   * Flushes the native JNIBuffer.
   * Does not modify the corresponding Java array, passed by createBuf()
   * Resets a possible overflow error condition.
   * Does nothing, if the JNIBuffer has not been created.
   *
   * @see #createBuf(boolean[] ary, int bufsize)
   */
  public void flushBuf()
  {
    if (isEnabled)
      nativeFlushBuf(ref);
  }

  /**
   * Returns the count of available (pending) elements in the native JNIBuffer.
   * Does not modify neither the native JNIBuffer nor the corresponding Java array, passed by createBuf().
   *
   * @return the number of available elements, -1, if the JNIBuffer has not been created
   *
   * @see #createBuf(boolean[] ary, int bufsize)
   */
  public int countBuf()
  {
    if (isEnabled)
      return nativeCountBuf(ref);
    return -1;
  }

  /**
   * Checks for a possible overflow of the JNIBuffer.
   * This happens when the circular FIFO JNIBuffer is filled. New elements will be lost,
   * but old elements are not overwritten.
   *
   * The error condition is reset by either calling isBufOverflow(true) or flushBuf().
   * Retrieving elements will cause the JNIBuffer to work normally, but the error condition
   * is not reset.
   *
   * @param reset if true, resets a possible overflow condition, if false, the condition is unchanged
   *
   * @return true, for an overflow condition, otherwise false
   *
   * @see #flushBuf()
   * @see #createBuf(boolean[] ary, int bufsize)
  **/
  public boolean isBufOverflow(boolean reset)
  {
    if (isEnabled)
      return nativeIsOverflow(ref, reset);
    return false;
  }

  /**
   * Start a native high priority thread.
   * See: Documentation for native class WindowHandler
   */
  public void startThread()
  {
    if (isEnabled)
      nativeStartThread(ref);
  }

  /**
   * Stop a native high priority thread.
   *
   * See: Documentation for native class WindowHandler
   */
  public void stopThread()
  {
    if (isEnabled)
      nativeStopThread(ref);
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
    if (!isEnabled)
      return;

    trayIconListener = listener;
    this.iconEventMask = iconEventMask;
    // new native buf; if exists, old is deleted
    // iconEventMask is transfered to native code
    createIconEventBuf(iconEventBuf, iconEventBufSize);
    if (!isThreadRunning)
    {
       isThreadRunning = true;
       iconEventThread = new IconEventThread();
       iconEventThread.start();
    }
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
    if (isEnabled)
    {
      this.iconPath = iconPath;
      this.tooltip = tooltip;
      if (isTrayIconVisible)
        nativeModifyTrayIcon(ref);
      else
      {
        isTrayIconVisible = true;
        nativeCreateTrayIcon(ref);
      }
    }
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
    showIcon(iconPath, "");
  }

  /**
   * Hides the tray icon.
   * The allocated resources are not released.
   *
   * @see #destroy()
   **/
  public void hideIcon()
  {
    if (isEnabled && isTrayIconVisible)
    {
      isTrayIconVisible = false;
      nativeDeleteTrayIcon(ref);
    }
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
    if (isEnabled && isTrayIconVisible)
    {
      balloon_tooltip_title = title;
      balloon_tooltip = text;
      nativeShowBalloonTooltip(ref, timeout, flag);
    }
  }

  // =========================== Private methods ===========================
  private void loadLib(String dllPath)
  {
    try
    {
      System.loadLibrary(dllPath);
    }
    catch (UnsatisfiedLinkError e)
    {
      try
      {
        System.load(dllPath);
      }
      catch (UnsatisfiedLinkError ex)
      {
        throw new RuntimeException("Can't load DLL " + dllPath);
      }
    }
  }

  private void init(int select, String title,
                    int xPos, int yPos, int width, int height,
                    long windowStyle)
  {
    this.title = title;
    this.xPos = xPos;
    this.yPos = yPos;
    this.width = width;
    this.height = height;
    this.windowStyle = windowStyle;
    nativeEnable(select);
    isEnabled = true;
  }

  private void createIconEventBuf(int[] ary, int bufSize)
  {
    if (isEnabled && bufSize > 0)
      nativeCreateIconEventBuf(ref, ary, bufSize);
  }

  private int readIconEventBuf(int[] ary, int count)
  {
    if (isEnabled)
    {
      if (count == 0)
        return 0;
      if (count < 0 || count > ary.length)
        return -1;
      return nativeReadIconEventBuf(ref, ary, count);
    }
    return -1;
  }


  // =========================== Protected methods ===========================
  // Wait until loaded
  protected void showWindow(int percent, int red, int green, int blue, boolean wait)
  {
    if (isEnabled)
      nativeShowWindow(ref, true, percent, red, green, blue, wait);
  }

}
