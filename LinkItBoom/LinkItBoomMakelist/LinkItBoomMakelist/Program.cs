using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System;
using System.IO;
namespace LinkItBoomMakelist
{
    class Program
    {
        static void Main(string[] args)
        {
            try {
                Console.WriteLine("Hello World!");
                String[] files;
                String path = "asdf";
                path = System.IO.Path.GetDirectoryName(System.Reflection.Assembly.GetExecutingAssembly().GetName().CodeBase);
                path = path.Remove(0, 6);
                System.IO.StreamWriter file = new System.IO.StreamWriter(path + "\\TEST.TXT");
                string lines = "";
                string[] filePaths = Directory.GetFiles(path, "*.mp3");

                foreach (string x in filePaths)
                {
                    lines = lines + x.Remove(0, path.Length) + " ";
                    Console.WriteLine(x.Remove(0, path.Length).Replace("/", "").Replace("\\", ""));

                    //file.WriteLine(x.Remove(0, path.Length + 1));


                }
                file.WriteLine(lines);
                Console.WriteLine(lines);
                Console.WriteLine(path);
                Console.WriteLine("Songs:" + filePaths.Length);
                file.Close();
            }
            catch(Exception e) { }
        }
    }
}