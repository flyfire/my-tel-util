package eu.chainfire.gingerbreak;

import android.app.ProgressDialog;

class MainActivity$Startup$1UpdateRunnable
  implements Runnable
{
  public String message = "";

  public void run()
  {
    if (MainActivity.Startup.access$2(this$1) != null)
      MainActivity.Startup.access$2(this$1).setMessage(message);
  }
}

/* Location:           E:\开发工具\android开发工具\反编译工具\apktool2.2\gingerbreak\gingerbreak_dex2jar.jar
 * Qualified Name:     eu.chainfire.gingerbreak.MainActivity.Startup.1UpdateRunnable
 * JD-Core Version:    0.6.0
 */