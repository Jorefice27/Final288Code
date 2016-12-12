// TrayIcon.java
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
 * Class to retrieve information about a mouse event from a tray icon.
 */
public class TrayIcon
{
  private int evt;

  /**
   * Creates a TrayIcon instance and intializes event type.
   */
  public TrayIcon()
  {
    this.evt = NativeMouse.idle;
  }

  protected void setEvent(int evt)
  {
    this.evt = evt;
  }

  /**
   * Retrieve current event type as integer.
   *
   * @return an integer with one of the predefined values of class NativeMouse.
   * (NativeMouse.enter and NativeMouse.leave are not supported.)
   */
  public int getEvent()
  {
    return evt;
  }

 }