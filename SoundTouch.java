// SoundTouch.java

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
 * Java wrapping class to the C++ audio processing converter 'SoundTouch'
 * by Olli Parviainen (www.surina.net/soundtouch).<br>
 * The native library soundtouch.dll must reside in the current directory or
 * the environment path.<br>
 * The input format to the converter must have PCM_SIGNED mono or stereo format
 * (16 bit little endian).
 *
 * Because the Java Sound API does not support arrays of shorts (16 bit), a byte
 * array is used for input to and output from the converter.
 *
 */
public class SoundTouch
{
  private NativeHandler nh;

  /**
   * Constant for setSetting().
   */
  public static final int SETTING_USE_AA_FILTER = 0;
  /**
   * Constant for setSetting().
   */
  public static final int SETTING_AA_FILTER_LENGTH = 1;
  /**
   * Constant for setSetting().
   */
  public static final int SETTING_USE_QUICKSEEK = 2;
  /**
   * Constant for setSetting().
   */
  public static final int SETTING_SEQUENCE_MS = 3;
  /**
   * Constant for setSetting().
   */
  public static final int SETTING_SEEKWINDOW_MS = 4;
  /**
   * Constant for setSetting().
   */
  public static final int SETTING_OVERLAP_MS = 5;


  // Native variables
  private String versionString;
  private float newRate;
  private float newTempo;
  private float newPitch;
  private int newPitchInt;
  private int numChannels;
  private int srate;
  private byte[] inBuffer;
  private int numBytes;
  private int settingId;
  private int value;
  private byte[] outBuffer;
  private int maxBytes;
  private int maxSamples;

  /**
   * Creates a new SoundTouch instance.
   * To release all resources, destroy() must be called.
   * @see #destroy
   */
  public SoundTouch()
  {
    nh = new NativeHandler("soundtouch");
    nh.expose(this);
  }

  /**
   * Releases all resources.
   */
  public void destroy()
  {
    nh.destroy();
  }

  /**
   * Gets SoundTouch library version string.
   */
  public String getVersionString()
  {
    nh.invoke(0);
    return versionString;
  }

  /**
   * Gets SoundTouch library version Id.
   */
  public int getVersionId()
  {
    return nh.invoke(1);
  }

  /**
   * Sets new rate control value. Normal rate = 1.0, smaller values
   * represent slower rate, larger faster rates.
   */
  public void setRate(float newRate)
  {
    this.newRate = newRate;
    nh.invoke(2);
  }

  /**
   * Sets new tempo control value. Normal tempo = 1.0, smaller values
   * represent slower tempo, larger faster tempo.
   */
  public void setTempo(float newTempo)
  {
    this.newTempo = newTempo;
    nh.invoke(3);
  }

  /**
   * Sets new rate control value as a difference in percents compared
   * to the original rate (-50 .. +100 %).
   */
  public void setRateChange(float newRate)
  {
    this.newRate = newRate;
    nh.invoke(4);
  }

  /**
   * Sets new tempo control value as a difference in percents compared
   * to the original tempo (-50 .. +100 %).
   */
  public void setTempoChange(float newTempo)
  {
    this.newTempo = newTempo;
    nh.invoke(5);
  }

  /**
   * Sets new pitch control value. Original pitch = 1.0, smaller values
   * represent lower pitches, larger values higher pitch.
   */
  public void setPitch(float newPitch)
  {
    this.newPitch = newPitch;
    nh.invoke(6);
  }

  /**
   * Sets pitch change in octaves compared to the original pitch
   * (-1.00 .. +1.00).
   */
  public void setPitchOctaves(float newPitch)
  {
    this.newPitch = newPitch;
    nh.invoke(7);
  }

  /**
   * Sets pitch change in semi-tones compared to the original pitch
   * (-12 .. +12).
   */
  public void setPitchSemiTones(int newPitch)
  {
    this.newPitchInt = newPitchInt;
    nh.invoke(8);
  }

  /**
   * Sets pitch change in semi-tones compared to the original pitch
   * (-12 .. +12).
   */
  public void setPitchSemiTones(float newPitch)
  {
    this.newPitch = newPitch;
    nh.invoke(9);
  }

  /**
   * Sets the number of channels, 1 = mono, 2 = stereo.
   */
  public void setChannels(int numChannels)
  {
    this.numChannels = numChannels;
    nh.invoke(10);
  }

  /**
   * Sets sample rate in samples per seconds.
   */
  public void setSampleRate(int srate)
  {
    this.srate = srate;
    nh.invoke(11);
  }

  /**
   * Flushs the last samples from the processing pipeline to the output.
   * Clears also the internal processing buffers.<br>
   *
   * Note: This method is meant for extracting the last samples of a sound
   * stream. This function may introduce additional blank samples in the end
   * of the sound stream, and thus it's not recommended to call this method
   * in the middle of a sound stream.
   */
  public void flush()
  {
    nh.invoke(12);
  }

  /**
   * Adds given number of bytes from the given buffer into
   * the input of the converter. Notice that sample rate has to be set before
   * calling this method.
   * If mono, nbBytes must be multiple of 2 (2 bytes per sample).
   * If stereo, nbBytes must be multiple of 4 (4 bytes per sample).
   * @throws RuntimeException if native call fails
   * (e.g. if the sample rate has not been set  before calling this method)
   */
  public void putSamples(byte[] inBuffer, int numBytes) throws RuntimeException
  {
    this.inBuffer = inBuffer;
    this.numBytes = numBytes;
    nh.invoke(13);
  }

  /**
   * Clears all the samples in the object's output and internal processing
   * buffers.
   */
  public void clear()
  {
    nh.invoke(14);
  }

  /**
   * Changes a setting controlling the processing system behaviour.<br>
   * settingId and value:<br>
   * SETTING_USE_AA_FILTER<br>
   * Enable/disable anti-alias filter in pitch transposer (0 = disable)<br><br>
   * SETTING_AA_FILTER_LENGTH<br>
   * Pitch transposer anti-alias filter length (8 .. 128 taps, default = 32)<br><br>
   * SETTING_USE_QUICKSEEK<br>
   * Enable/disable quick seeking algorithm in tempo changer routine
   * (enabling quick seeking lowers CPU utilization but causes a minor sound
   * quality compromising)<br><br>
   * SETTING_SEQUENCE_MS<br>
   * Time-stretch algorithm single processing sequence length in milliseconds. This determines
   * to how long sequences the original sound is chopped in the time-stretch algorithm.
   * See README for more information.<br><br>
   * SETTING_SEEKWINDOW_MS<br>
   * Time-stretch algorithm seeking window length in milliseconds for algorithm that finds the
   * best possible overlapping location. This determines from how wide window the algorithm
   * may look for an optimal joining location when mixing the sound sequences back together.
   * See README for more information.<br><br>
   * SETTING_OVERLAP_MS<br>
   * Time-stretch algorithm overlap length in milliseconds. When the chopped sound sequences
   * are mixed back together, to form a continuous sound stream, this parameter defines over
   * how long period the two consecutive sequences are let to overlap each other.
   * See README for more information.
   */
  public boolean setSetting(int settingId, int value)
  {
    this.settingId = settingId;
    this.value = value;
    int rc = nh.invoke(15);
    if (rc == 1)
      return true;
    else
      return false;
  }

  /**
   * Returns a setting controlling the processing system behaviour.
   * @see #setSetting
   */
  public int getSetting(int settingId)  /// Setting ID number, see SETTING_... defines.
  {
    this.settingId = settingId;
    return nh.invoke(16);
  }

  /**
   * Returns number of samples currently unprocessed.
   * If mono, the size of a sample is 2 bytes.
   * If stereo, the size of a sample is 4 bytes.
   */
  public int numUnprocessedSamples()
  {
    return nh.invoke(17);
  }

  /**
   * Outputs samples from beginning of the sample buffer. Copies up to maxBytes
   * bytes to outBuffer and removes them from the sample buffer.
   * If there are less than maxBytes in the buffer, returns all that available.<br>
   * Return number of bytes copied in outBuffer.
   * If mono, maxBytes must be multiple of 2 (2 bytes per sample)
   * If stereo, maxBytes must be multiple of 4 (4 bytes per sample)
   */
  public int receiveSamples(byte[] outBuffer, int maxBytes)
  {
    this.outBuffer = outBuffer;
    this.maxBytes = maxBytes;
    return nh.invoke(18);
  }


  /**
   * Removes up to the given number of samples from the beginning of sample buffer.
   * If mono, the size of a sample is 2 bytes.
   * If stereo, the size of a sample is 4 bytes.
   */
  public int receiveSamples(int maxSamples)
  {
    this.maxSamples = maxSamples;
    return nh.invoke(19);
  }

  /**
   * Returns number of samples currently available.
   * If mono, the size of a sample is 2 bytes.
   * If stereo, the size of a sample is 4 bytes.
   */
  public int numSamples()
  {
    return nh.invoke(20);
  }


  /**
   * Returns nonzero if there aren't any samples available for outputting.
   */
  public int isEmpty()
  {
    return nh.invoke(21);
  }

  // Callback from C++
  private void throwRuntimeException()
  {
    throw new RuntimeException("Runtime error occurred when calling native putSample()");
  }

}
