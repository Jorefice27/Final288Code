// NativeKeyListener.java
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
 * Interface that declares a callback method for key events in a native window.
 **/
public interface NativeKeyListener
{

 /**
   * Callback method called when a key event occurs.
   * The parameter is used to get the (native) keyCode and a modifier
   * for combined key-strokes.
   *
   * @param keyCode the (native) keyCode
   * @param modifier a modfier mask for combined key-strokes
   * (1: ctrl-key, 2: shift-key, 4: alt-key).
   *
   */
  void keyPressed(int keyCode, int modifier);
}