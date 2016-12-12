// NativeMouse.java
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

/**
 * Class to retrieve information about a mouse event.
 */
public class NativeMouse
{
  private int x = 0;
  private int y = 0;
  private int evt = idle;

  /**
   * No mouse event occured.
   */
  public static final int idle   = 0;

  /**
   * Left mouse button down.
   */
  public static final int lPress   = 1;

  /**
   * Left mouse button up.
   */
  public static final int lRelease = 2;

  /**
   * Left mouse button down and up in a quick sequence.<br>
   * Press, release, click are also generated in this order.
   */
  public static final int lClick   = 4;

  /**
   * Left mouse button down, up, down, up in a quick sequence.<br>
   * Press, release, double-click, release is generated in this order.<br>
   * When double-click is registered, the click event is reported only
   * after a timeout of about 500 ms to distinguish click and double-click.<br><br>
   * A pending click is suspended, when a mouse move is detected.
   */
  public static final int lDClick  = 8;

  /**
   * Right mouse button down.
   */

  public static final int rPress   = 16;
  /**
   * Right mouse button up.
   */
  public static final int rRelease = 32;

  /**
   * Right mouse button down and up in a quick sequence.<br>
   * Press, release, click are also generated in this order.
   */
  public static final int rClick   = 64;

  /**
   * Right mouse button down, up, down, up in a quick sequence.<br>
   * Press, release, double-click, release is generated in this order.<br>
   * When double-click is registered, the click event is reported only
   * after a timeout of about 500 ms to distinguish click and double-click.<br><br>
   * A pending click is suspended, when a mouse move is detected.
   */
  public static final int rDClick  = 128;

  /**
   * Mouse cursor enters the window.
   * (getX(), getY() reports 0.)
   */
  public static final int enter  = 256;

  /**
   * Mouse cursor leaves the window.
   * getX(), getY() reports 0.
   */
  public static final int leave  = 512;

  /**
   * Mouse cursor is moved inside the window.
   * Because many events are generated, use it with precaution.
   */
  public static final int move  = 1024;


  /**
   * Creates a NativeMouse instance and intializes event type and cursor position.
   */
  public NativeMouse()
  {
    this.evt = idle;
    this.x = 0;
    this.y = 0;
  }

  // Package access only
  void setValues(int evt, int x, int y)
  {
    this.evt = evt;
    this.x = x;
    this.y = y;
  }

  /**
   * Retrieve current event type as integer.
   * @return an integer with one of the predefined values
   */
  public int getEvent()
  {
    return evt;
  }

  /**
   * Retrieve the current x-position of the mouse cursor with respect to the upper left corner in pixels.
   * @return number of pixels of the current mouse x-coordinate
   */
  public int getX()
  {
    return x;
  }

  /**
   * Retrieve the current y-position of the mouse cursor with respect to the upper left corner in pixels.
   * @return number of pixels of the current mouse y-coordinate
   */
  public int getY()
  {
    return y;
  }
 }