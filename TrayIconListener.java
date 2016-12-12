// TrayIconListener.java
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
 * Interface that declares a callback method for mouse events in a tray icon.
 **/
public interface TrayIconListener
{

  /**
   * Callback method called when a mouse event occurs from a tray icon.
   * The parameter is used to get information about the event.
   *
   * @param icon the TrayIcon to get the type of the mouse event
   */
  void iconEvent(TrayIcon icon);
}