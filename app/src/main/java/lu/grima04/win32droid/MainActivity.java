//win32droid, the Windows Emulator for Android based on box86

package lu.grima04.win32droid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.grima04.win32droid.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainActivity extends AppCompatActivity {

    protected static boolean isRooted = false;
    protected static ProcessBuilder setupProcessBuilder = null;
    protected static Process setupProcess = null;
    protected static ProcessBuilder emulationProcessBuilder = null;
    protected static Process emulationProcess = null;
    protected static BufferedWriter shellWriter = null;
    protected static boolean printConsole = true;
    protected static Switch hardwareRendering;
    protected static Switch useGL4ES;
    protected static Switch softwareRendering;
    protected static Switch useGalliumHUD;
    protected static Switch disableGLSLcache;
    protected static EditText customWidth;
    protected static EditText customHeight;
    protected static EditText cpuAffinity;
    protected static TextView terminal;
    protected static Button start;
    protected static Button stop;
    protected static Button winecfg;
    protected static Button regedit;
    protected static long processPID = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        terminal = (TextView) findViewById(R.id.terminalOutput);
        start = (Button) findViewById(R.id.startEmulation);
        stop = (Button) findViewById(R.id.stopEmulation);
        winecfg = (Button) findViewById(R.id.winecfg);
        regedit = (Button) findViewById(R.id.regedit);
        softwareRendering = (Switch) findViewById(R.id.softwareRendering);
        hardwareRendering = (Switch) findViewById(R.id.hardwareRendering);
        useGL4ES = (Switch) findViewById(R.id.useGL4ES);
        disableGLSLcache = (Switch) findViewById(R.id.disableGLSLcache);
        useGalliumHUD = (Switch) findViewById(R.id.useGalliumHUD);
        customWidth = (EditText) findViewById(R.id.width);
        customHeight = (EditText) findViewById(R.id.height);
        cpuAffinity = (EditText) findViewById(R.id.cpuAffinity);
        final String privatePath = getFilesDir().toString();
        final String obbPath = getObbDir().toString()+"/ubuntu.focal.armhf.rootfs.obb";

        //Disable Hardware Rendering when selecting Software Rendering and vice versa
        fixRenderingBackendButtons();

        //Disable buttons of non implemented functions
        useGL4ES.setEnabled(false);
        hardwareRendering.setEnabled(false);
        cpuAffinity.setEnabled(false);

        customWidth.setText(getScreenResolution(getApplicationContext()).split("x")[0]);
        customHeight.setText(getScreenResolution(getApplicationContext()).split("x")[1]);

        try {
            if(isDirEmpty(Paths.get(privatePath))==false){
                //
            }else{
                try{
                    //If rootfs is empty on application launch -> extract the obb containing the rootfs
                    Toast toastSetup = Toast.makeText(getApplicationContext(),"Setting up emulation environment.\nPlease wait...",Toast.LENGTH_LONG);
                    toastSetup.show();
                    setupProcessBuilder = new ProcessBuilder("/bin/sh");
                    setupProcess = setupProcessBuilder.start();
                    BufferedWriter shellWriter = new BufferedWriter(new OutputStreamWriter(setupProcess.getOutputStream()));
                    shellWriter.write("su");
                    shellWriter.newLine();
                    shellWriter.flush();
                    shellWriter.write("cp /sdcard/Android/obb/com.grima04.win32droid/ubuntu.focal.armhf.rootfs.obb /data/data/com.grima04.win32droid/files");
                    shellWriter.newLine();
                    shellWriter.flush();
                    shellWriter.write("cd /data/data/com.grima04.win32droid/files");
                    shellWriter.newLine();
                    shellWriter.flush();
                    shellWriter.write("unzip ubuntu.focal.armhf.rootfs.obb");
                    shellWriter.newLine();
                    shellWriter.flush();
                    shellWriter.write("rm ubuntu.focal.armhf.rootfs.obb");
                    shellWriter.newLine();
                    shellWriter.flush();
                    shellWriter.write("echo 'Done! You can now safely start Wine'");
                    shellWriter.newLine();
                    shellWriter.flush();
                    shellWriter.write("exit");
                    shellWriter.newLine();
                    shellWriter.flush();
                    printConsoleOutput(setupProcess,terminal);
                }catch(Exception e){
                    Toast unpackingFailed = Toast.makeText(getApplicationContext(),"Failed to setup the emulation environment!",Toast.LENGTH_SHORT);
                    unpackingFailed.show();
                    try {
                        //emulationProcess = Runtime.getRuntime().exec("rm -r " + privatePath);
                    }catch(Exception f){}
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startEmulation(start);
            }
        });

        regedit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startEmulation(regedit);
            }
        });

        winecfg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startEmulation(winecfg);
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    printConsole = false;
                    //Kill XServer XSDL
                    Process process = Runtime.getRuntime().exec("su -c am force-stop x.org.server");
                    //Kill box86
                    Process killProcess = Runtime.getRuntime().exec("su -c killall box86");
                    /*shellWriter.write("box86 ~/wine/bin/wineserver -k");
                    shellWriter.newLine();
                    shellWriter.flush();
                    shellWriter.write("echo 'Stopping emulation...'");
                    shellWriter.newLine();
                    shellWriter.flush();
                    shellWriter.write("exit");
                    shellWriter.newLine();
                    shellWriter.flush();
                    printConsole = false;*/
                    emulationProcess.destroy();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static boolean isDeviceRooted(){
        //Check for root rights
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[]{"su", "-c", "id"});
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String output = in.readLine();
            System.out.println(output);
            if (output != null && output.toLowerCase().contains("uid=0")) return true;
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            if (process != null) process.destroy();
        }return false;
    }

    private static String getScreenResolution(Context context){
        //Get the device's full screen resolution, independent of screen orientation
        int width = 0;
        int height = 0;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point resolution = new Point();
        windowManager.getDefaultDisplay().getRealSize(resolution);
        //Fix wrong resolution if app is started in portrait mode
        if(resolution.x > resolution.y){
            width = resolution.x;
            height = resolution.y;
        }else if(resolution.x < resolution.y){
            width = resolution.y;
            height = resolution.x;
        }
        String res = width+"x"+height;
        return res;
    }

    private static boolean isDirEmpty(final Path directory) throws IOException {
        //Check if the Ubuntu rootfs exists
        try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
            return !dirStream.iterator().hasNext();
        }
    }

    private static void printConsoleOutput(Process process, TextView textView){
        InputStream console = process.getInputStream();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[2048];
                int len = -1;
                while(printConsole){
                    try {
                        if (!((len = console.read(buffer)) > 0)) break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //System.out.write(buffer, 0, len);
                    textView.setText(new String(buffer, StandardCharsets.UTF_8));
                }
            }
        });
        thread.start();
    }

    private static String getRenderingBackend(){
        String envVar = "";
        if (softwareRendering.isChecked()){
            envVar = "BOX86_LIBGL=/usr/lib/arm-linux-gnueabihf/software/libGL.so.1";
        }else if (hardwareRendering.isChecked()){
            envVar = "BOX86_LIBGL=/usr/lib/arm-linux-gnueabihf/accelerated/libGL-virgl-es2gl.so.1 LIBGL_DRIVERS_PATH=/usr/lib/arm-linux-gnueabihf/accelerated LIBGL_ALWAYS_SOFTWARE=1 GALLIUM_DRIVER=virpipe";
        }
        if (useGL4ES.isChecked()){
            envVar = envVar.split(" ")[1] + " " + envVar.split(" ")[2] + " BOX86_LIBGL=/usr/lib/arm-linux-gnueabihf/libGL-gl4es.so.1";
        }
        return envVar;
    }

    private static void fixRenderingBackendButtons(){
        softwareRendering.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                hardwareRendering.setChecked(false);
            }
        });

        hardwareRendering.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                softwareRendering.setChecked(false);
            }
        });
    }

    private static String getCustomResolution(){
        String env = "";
        env = customWidth.getText() + "x" + customHeight.getText();
        return env;
    }

    private static String getMesaOptions(){
        String env = "";
        if (useGalliumHUD.isChecked()){
            env = "GALLIUM_HUD=cpu,fps ";
        }
        if(disableGLSLcache.isChecked()){
            env = env + "MESA_GLSL_CACHE_DISABLE=true ";
        }
        return env;
    }

    private static String getTaskset(){
        String res = "";
        if ((cpuAffinity.getText()+"").contains("CPU")==true){
            res = "";
        }else if ((cpuAffinity.getText()+"") != ""){
            res = "taskset --cpu-list " + cpuAffinity.getText();
        }else{
            res = "";
        }
        return res;
    }

    private static synchronized long getPid(Process process){
        long pid = -1;

        try{
            if (process.getClass().getName().equals("java.lang.UNIXProcess")){
                Field field = process.getClass().getDeclaredField("pid");
                field.setAccessible(true);
                pid = field.getLong(process);
                field.setAccessible(false);
            }
        }catch(Exception e){
            pid = -1;
        }
        return pid;
    }

    private void startEmulation(Button button){
        isRooted = isDeviceRooted();
        if (isRooted==true){
            String wineService = "";
            Toast toastSuccess = Toast.makeText(getApplicationContext(),"Starting emulation ...",Toast.LENGTH_SHORT);
            toastSuccess.show();

            //Start the XServer XSDL application
            Intent launchXServerXSDL = getPackageManager().getLaunchIntentForPackage("x.org.server");
            startActivity(launchXServerXSDL);

            if(button==start){
                wineService = "explorer /root,sdcard";
            }else if(button==winecfg){
                wineService = "winecfg";
            }else if(button==regedit){
                wineService = "regedit";
            }



            try {
                printConsole = true;
                emulationProcessBuilder = new ProcessBuilder("/bin/sh");
                emulationProcess = emulationProcessBuilder.start();
                processPID = getPid(emulationProcess);
                shellWriter = new BufferedWriter(new OutputStreamWriter(emulationProcess.getOutputStream()));
                //Get root permissions
                shellWriter.write("su");
                shellWriter.newLine();
                shellWriter.flush();
                //cd into the Ubuntu rootfs
                shellWriter.write("cd /data/data/com.grima04.win32droid/files");
                shellWriter.newLine();
                shellWriter.flush();
                //Start the chroot jail environment via premade script
                shellWriter.write("sh chroot.sh");
                shellWriter.newLine();
                shellWriter.flush();
                //Setup XServer XSDL display output
                shellWriter.write("export DISPLAY=:0");
                shellWriter.newLine();
                shellWriter.flush();
                //Setup XServer XSDL audio output
                shellWriter.write("export PULSE_SERVER=tcp:127.0.0.1:4713");
                shellWriter.newLine();
                shellWriter.flush();
                Thread.sleep(10000);
                //Launch box86 with the Wine explorer, the Wine Desktop size being the fullscreen size of the device
                //shellWriter.write("env " + getRenderingBackend() + " box86 ~/wine/bin/wine explorer /desktop=win32droid," + getScreenResolution(getApplicationContext()) + " explorer");
                shellWriter.write("env " + getRenderingBackend() + " " + getMesaOptions() + " box86 ~/wine/bin/wine explorer /desktop=win32droid," + getCustomResolution() + " " + wineService);
                shellWriter.newLine();
                shellWriter.flush();

                //Print the shell output in the Android Studio Logcat and on the application TextView
                printConsoleOutput(emulationProcess,terminal);
            }catch(Exception e){
                System.out.println(e);
            }


        }else{
            Toast toastFailed = Toast.makeText(getApplicationContext(),"Failed to start emulation.\nAre you rooted?",Toast.LENGTH_LONG);
            toastFailed.show();
        }
    }
}