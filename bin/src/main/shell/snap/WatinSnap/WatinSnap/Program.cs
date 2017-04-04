using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using WatiN.Core;
using WatiN.Core.Native.InternetExplorer;
using System.Text.RegularExpressions;
using System.IO;
using System.Threading;
using SHDocVw;
using System.Diagnostics;

namespace WatinSnap
{
    class Program
    {
        [STAThread]
        static void Main(string[] args)
        {

            IE ie = null;
            String url = "http://localhost:8000/windows/pages/multipleframe.html";
            url = "baidu.com";
//            url = "sina.com";
            String file = "d:\\test2.png";

            Boolean debug = false;

            if (args.Length >= 2)
            {
                url = args[0];
                file = args[1];

            }

            if (args.Length >= 3 && args[2] == "debug")
            {
                debug = true;
            }
            Console.WriteLine("Capture " + url + " to " + file);

            ie = new IE();

            ie.GoToNoWait(url);
            try
            {

                ie.ShowWindow(WatiN.Core.Native.Windows.NativeMethods.WindowShowStyle.Maximize);


                Stream s = new FileStream("beidou_ad.user.js", FileMode.Open);
                StreamReader sr = new StreamReader(s, Encoding.Default);
                String script = sr.ReadToEnd().ToString();

                ie.WaitForComplete(30);

                Thread.Sleep(1000);

                ie.RunScript(script);

                if (debug)
                {
                    Stream st = new FileStream("html.txt", FileMode.Create);
                    StreamWriter sw = new StreamWriter(st, Encoding.Default);
                    sw.Write(ie.Html);
                    sw.Close();
                    st.Close();
                }

                //插入截图判断逻辑

                ie.CaptureWebPageToFile(file);

            }
            catch (Exception e)
            {
                Console.WriteLine(e.Message);
            }
            finally
            {
                if (ie != null)
                {
                    int procId = ie.ProcessID;

                    foreach (Process thisproc in System.Diagnostics.Process.GetProcesses())
                    {
                        if (thisproc.Id == procId)
                        {
                            Execute("ntsd -c q -p " + procId, 1000);
                            break;
                        }
                    }
               }
            }
        }

        /**/
        /// 
        /// 执行DOS命令，返回DOS命令的输出
        /// 
        /// dos命令
        /// 等待命令执行的时间（单位：毫秒），如果设定为0，则无限等待
        /// 返回输出，如果发生异常，返回空字符串
        public static string Execute(string dosCommand, int milliseconds)
        {
            string output = "";     //输出字符串
            if (dosCommand != null && dosCommand != "")
            {
                Process process = new Process();     //创建进程对象
                ProcessStartInfo startInfo = new ProcessStartInfo();
                startInfo.FileName = "cmd.exe";      //设定需要执行的命令
                startInfo.Arguments = "/C " + dosCommand;   //设定参数，其中的“/C”表示执行完命令后马上退出
                startInfo.UseShellExecute = false;     //不使用系统外壳程序启动
                startInfo.RedirectStandardInput = false;   //不重定向输入
                startInfo.RedirectStandardOutput = true;   //重定向输出
                startInfo.CreateNoWindow = true;     //不创建窗口
                process.StartInfo = startInfo;
                try
                {
                    if (process.Start())       //开始进程
                    {
                        if (milliseconds == 0)
                            process.WaitForExit();     //这里无限等待进程结束
                        else
                            process.WaitForExit(milliseconds);  //这里等待进程结束，等待时间为指定的毫秒
                        output = process.StandardOutput.ReadToEnd();//读取进程的输出
                    }
                }
                catch
                {
                }
                finally
                {
                    if (process != null)
                        process.Close();
                }
            }
            return output;
        }

    }
}
