package com.example.androidcamera2_2;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import android.os.Bundle;
//
//public class socket extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_socket);
//        findViewById()
//    }
//}

//package com.example.socket;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Timer;

public class socket extends AppCompatActivity {

    private Button button1;
    private Button button2;
    private Button button3;
    private EditText editip;
    private EditText file_name;
    String ipadress = null;
    int port = 18895;
    int port_file = 18893;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket);

        //找到对应的控件
        editip = findViewById(R.id.ip_socket);
        file_name = findViewById(R.id.img_name_socket);
        button1 = findViewById(R.id.button_socket);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //当按下传送字符串时
                if (editip.getText().toString().equals(""))
                {
                    //判断端口号和IP地址是否为空
                    //Toast.makeText("你的端口号或IP地址填写错误");
                }
                else
                {
                    ipadress = editip.getText().toString();
                    Log.d("上面的输入栏的端口:",String.valueOf(port));
                    Log.d("ip地址:",ipadress);
                }
                EditText edittext = findViewById(R.id.text_socket);
                String text = edittext.getText().toString();
                Thread thread2 = new Thread(new SendAndGetStr(ipadress, port, text));
                thread2.start();
            }
        });

        button2 = (Button) findViewById(R.id.button2_socket);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (editip.getText().toString().equals(""))
                {
                    return;
                }
                else
                {
                    ipadress = editip.getText().toString();
                    Log.d("下面的输入栏的端口（传文件的端口），传文件名的端口为这个端口减一:",String.valueOf(port_file));
                    Log.d("ip地址:",ipadress);
                }
                Thread thread2 = new Thread(new SendStr(ipadress, port_file-1, file_name.getText().toString()));
                thread2.start();
                Thread thread3 = new Thread(new SendImg((EditText) findViewById(R.id.img_name_socket)));
                thread3.start();
            }
        });

        button3 = (Button) findViewById(R.id.button_continues);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText editText1,editText2;
                TextView textView;
                editText1 = findViewById(R.id.num_begin);
                editText2 = findViewById(R.id.num_end);
                textView = findViewById(R.id.led);

                int begin,end;
                begin = Integer.parseInt(editText1.getText().toString());
                end = Integer.parseInt(editText2.getText().toString());
                Timer timer = new Timer();
                ipadress = editip.getText().toString();
                for(int i = begin;i<=end;i++)
                {
                    Thread threads = new Thread(new SendStr(ipadress, port_file-1, String.valueOf(i)+".jpg"));
                    threads.start();
                    Thread threadss = new Thread(new SendImg_name(String.valueOf(i)+".jpg"));
                    threadss.start();
                    textView.setText(String.valueOf(i));
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }



    public class SendImg implements Runnable{
        private EditText ImgName;
        SendImg(EditText in)
        {
            ImgName = in;
        }
        @Override
        public void run() {
            int length = 0;
            byte[] sendBytes = null;
            Socket socket = null;
            DataOutputStream dos = null;
            FileInputStream fis = null;
            String imgname;
            imgname = ImgName.getText().toString();


            try {
                try {
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(ipadress, port_file),
                            1000);
                    dos = new DataOutputStream(socket.getOutputStream());
                    Log.d("外置目录",String.valueOf(getExternalCacheDir()));
                    File file = new File(getExternalCacheDir()+"/"+imgname);
                    fis = new FileInputStream(file);
                    sendBytes = new byte[1024];
                    while ((length = fis.read(sendBytes, 0, sendBytes.length)) > 0) {
                        dos.write(sendBytes, 0, length);
                        dos.flush();
                    }
                } finally {
                    if (dos != null)
                        dos.close();
                    if (fis != null)
                        fis.close();
                    if (socket != null)
                        socket.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class SendImg_name implements Runnable{
        private EditText ImgName;
        private String imageName;
        SendImg_name(String in)
        {
            imageName = in;
        }
        @Override
        public void run() {
            int length = 0;
            byte[] sendBytes = null;
            Socket socket = null;
            DataOutputStream dos = null;
            FileInputStream fis = null;
            String imgname;
            imgname = imageName;


            try {
                try {
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(ipadress, port_file),
                            1000);
                    dos = new DataOutputStream(socket.getOutputStream());
                    Log.d("外置目录",String.valueOf(getExternalCacheDir()));
                    File file = new File(getExternalCacheDir()+"/"+imgname);
                    fis = new FileInputStream(file);
                    sendBytes = new byte[1024];
                    while ((length = fis.read(sendBytes, 0, sendBytes.length)) > 0) {
                        dos.write(sendBytes, 0, length);
                        dos.flush();
                    }
                } finally {
                    if (dos != null)
                        dos.close();
                    if (fis != null)
                        fis.close();
                    if (socket != null)
                        socket.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    class SendStr implements Runnable {

        private String ipadress=null;
        private int port = 0;
        private String text = null;
        SendStr(String ip,int port_,String text_)
        {
            text = text_;
            ipadress = ip;
            port=port_;
        }
        @Override
        public void run() {
            Socket socket = null;

            if (text.equals("") || text == null) {
                return;
            } else {
                try {
                    Log.d("传送字符串的SendSte函数接收到的ip地址", ipadress);
                    Log.d("传送字符串的SendSte函数接收到的端口", String.valueOf(port));
                    socket = new Socket(ipadress, port);
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    Log.d("SendStr函数里一个莫名其妙的输出", "out=" + out.toString() + " socket=" + socket.toString());
                    out.writeUTF(text);
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    Log.d("SendStr函数错误", "无法获取IP:" + e.getMessage());
                } finally {
                    if (null != socket) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    class SendAndGetStr implements Runnable {

        private String ipadress=null;
        private int port = 0;
        private String text = null;
        SendAndGetStr(String ip,int port_,String text_)
        {
            text = text_;
            ipadress = ip;
            port=port_;
        }
        @Override
        public void run() {
            Socket socket = null;

            if (text.equals("") || text == null) {
                return;
            } else {
                try {
                    Log.d("传送字符串的SendSte函数接收到的ip地址", ipadress);
                    Log.d("传送字符串的SendSte函数接收到的端口", String.valueOf(port));
                    socket = new Socket(ipadress, port);
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    Log.d("SendStr函数里一个莫名其妙的输出", "out=" + out.toString() + " socket=" + socket.toString());
                    out.writeUTF(text);
                    //socket.shutdownOutput();

                    Log.d("进度", "1");
                    DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                    Log.d("进度", "2");
                    String input = inputStream.readUTF();
                    Log.d("进度", "3");
                    TextView textView = findViewById(R.id.strshow_socket);
                    Log.d("进度", "4");
                    textView.setText(input);
                    Log.d("进度", "5");
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    Log.d("发生错误", "无法获取IP:" + e.getMessage());
                } finally {
                    if (null != socket) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
//    class SendFlag implements Runnable {
//        //传送标志位，第一位为固定的“begin”
//        //第二位为文件类型，分为“file”和“string”
//        //第三位为文件后缀名
//        public String FLAG;
//        public String FILE_TYPE = "";
//        String FILE = "file";
//        String STR = "string";
//
//        public  SendFlag(String modeflag, String file_type)
//        {
//            FLAG = modeflag;
//            FILE_TYPE = file_type;
//        }
//
//        @Override
//        public void run() {
//            if(FLAG.equals(FILE) || FLAG.equals(STR))
//            {
//                sendflag("begin");
//                sendflag(FLAG);
//                sendflag(FILE_TYPE);
//            }
//        }
//
//
//        private void sendflag(String str)
//        {
//            Socket socket = null;
//
//            try {
//                Log.d("Tankai", "Thread=" + Thread.currentThread().getName());
//                socket = new Socket(ipadress, port);
//                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
//                Log.d("Tankai", "out=" + out.toString() + " socket=" + socket.toString());
//                out.writeUTF(str);
//                out.flush();
//                out.close();
//
//            } catch (IOException e) {
//                Log.d("Tankai", "无法获取IP:" + e.getMessage());
//            } finally {
//                if (null != socket) {
//                    try {
//                        socket.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//    }
}