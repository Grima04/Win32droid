/*
        Win32droid, the experimental Windows Emulator for rooted Android devices based on box86 and Wine
        Copyright (C) 2020-2021  Grima04

        This program is free software; you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation; either version 2 of the License, or
        (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License along
        with this program; if not, write to the Free Software Foundation, Inc.,
        51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/

package com.grima04.win32droid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class MainActivity extends AppCompatActivity {

    protected static boolean isRooted = false;
    protected static ProcessBuilder setupProcessBuilder = null;
    protected static Process setupProcess = null;
    protected static ProcessBuilder emulationProcessBuilder = null;
    protected static Process emulationProcess = null;
    protected static ProcessBuilder updateBox86ProcessBuilder;
    protected static Process updateBox86Process;
    protected static ProcessBuilder applyPatchProcessBuilder;
    protected static Process applyPatchProcess;
    protected static BufferedWriter shellWriter = null;
    protected static boolean printConsole = true;
    protected static Switch hardwareRendering;
    protected static Switch useInterpreter;
    protected static Switch softwareRendering;
    protected static Switch useGalliumHUD;
    protected static Switch disableGLSLcache;
    protected static EditText customWidth;
    protected static EditText customHeight;
    protected static EditText cpuAffinity;
    protected TextView terminal;
    protected Button start;
    protected Button stop;
    protected Button winecfg;
    protected Button regedit;
    protected Button updateBox86;
    protected Button applyPatch;
    protected static long processPID = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        terminal = (TextView) findViewById(R.id.terminalOutput);
        terminal.setMovementMethod(new ScrollingMovementMethod());
        start = (Button) findViewById(R.id.startEmulation);
        stop = (Button) findViewById(R.id.stopEmulation);
        winecfg = (Button) findViewById(R.id.winecfg);
        regedit = (Button) findViewById(R.id.regedit);
        updateBox86 = (Button) findViewById(R.id.updateBox86);
        applyPatch = (Button) findViewById(R.id.applyPatch);
        softwareRendering = (Switch) findViewById(R.id.softwareRendering);
        hardwareRendering = (Switch) findViewById(R.id.hardwareRendering);
        useInterpreter = (Switch) findViewById(R.id.useInterpreter);
        disableGLSLcache = (Switch) findViewById(R.id.disableGLSLcache);
        useGalliumHUD = (Switch) findViewById(R.id.useGalliumHUD);
        customWidth = (EditText) findViewById(R.id.width);
        customHeight = (EditText) findViewById(R.id.height);
        cpuAffinity = (EditText) findViewById(R.id.cpuAffinity);
        final String privatePath = getFilesDir().toString();

        //Disable Hardware Rendering when selecting Software Rendering and vice versa
        fixRenderingBackendButtons();

        //Disable buttons of non implemented functions
        //hardwareRendering.setEnabled(false);
        //cpuAffinity.setEnabled(false);
        useGalliumHUD.setEnabled(false);

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
                    //shellWriter.write("su");
                    //shellWriter.flush();
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
                    }catch(Exception f){
                        f.printStackTrace();
                    }
                }

            }/*else{
                Toast starting = Toast.makeText(getApplicationContext(),"Starting to extract the OBB. Please wait...",Toast.LENGTH_LONG);
                if(extractOBB()){
                    Toast success = Toast.makeText(getApplicationContext(),"Successfully extracted the Ubuntu rootfs OBB!",Toast.LENGTH_LONG);
                    success.show();
                }else{
                    Toast failed = Toast.makeText(getApplicationContext(),"Successfully extracted the Ubuntu rootfs OBB!",Toast.LENGTH_LONG);
                    failed.show();
                }
            }*/
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

        updateBox86.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateBox86();
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
                    shellWriter.write("echo 'Stopping emulation...'");
                    shellWriter.newLine();
                    shellWriter.flush();
                    emulationProcess.destroy();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        applyPatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                applyPatch();
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
            e.printStackTrace();
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
        assert windowManager != null;
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
        //InputStream consoleOut = process.getInputStream();
        //InputStream consoleError = process.getErrorStream();
        BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    String consoleText = null;
                    while((consoleText = outputReader.readLine()) != null){
                        textView.setText(consoleText+"\n");
                        System.out.println(consoleText);
                    }
                    while((consoleText = errorReader.readLine()) != null){
                        textView.setText(consoleText+"\n");
                        System.out.println(consoleText);
                    }
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                } catch (Exception f){
                    //
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
            envVar = "BOX86_LIBGL=/usr/lib/arm-linux-gnueabihf/accelerated/libGL.so.1 VTEST_SOCK=";
        }
        /*if (useGL4ES.isChecked()){
            envVar = envVar.split(" ")[1] + " " + envVar.split(" ")[2] + " BOX86_LIBGL=/usr/lib/arm-linux-gnueabihf/libGL-gl4es.so.1";
        }*/
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
        if ((cpuAffinity.getText() + "").contains("CPU")){
            res = "";
        }else if ((cpuAffinity.getText()+"") != ""){
            res = "taskset -c " + cpuAffinity.getText() + " ";
        }else{
            res = "";
        }
        return res;
    }

    private static String getEmulationMode(){
        String res = "";
        if(useInterpreter.isChecked()){
            res = "BOX86_DYNAREC=0 ";
        }else{
            res = "";
        }
        return res;
    }

    private static boolean extractOBB(){
        InputStream inputStream = null;
        ZipInputStream zipInputStream = null;
        ZipEntry zipEntry = null;
        byte[] buffer = new byte[1024];
        int counter = 0;
        String filename = "";
        File directory = null;
        FileOutputStream fileOutputStream = null;
        try{
            inputStream = new FileInputStream("/sdcard/Android/obb/com.grima04.win32droid/ubuntu.focal.armhf.rootfs.obb");
            zipInputStream = new ZipInputStream(new BufferedInputStream(inputStream));
            while((zipEntry = zipInputStream.getNextEntry()) != null){
                filename = zipEntry.getName();

                if(zipEntry.isDirectory()){
                    directory = new File("/data/data/com.grima04.win32droid/files/" + filename);
                    directory.mkdirs();
                    continue;
                }

                fileOutputStream = new FileOutputStream("/data/data/com.grima04.win32droid/files/" + filename);

                while((counter = zipInputStream.read(buffer)) != -1){
                    fileOutputStream.write(buffer,0,counter);
                }

                fileOutputStream.close();
                zipInputStream.closeEntry();
            }
            zipInputStream.close();
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
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

    private void applyPatch(){
        isRooted = isDeviceRooted();
        if(isRooted){
            Toast toastApplyPatch = Toast.makeText(getApplicationContext(),"Applying patch ...",Toast.LENGTH_SHORT);
            toastApplyPatch.show();
            try{
                printConsole = true;
                applyPatchProcessBuilder = new ProcessBuilder("/bin/sh");
                applyPatchProcess = applyPatchProcessBuilder.start();
                shellWriter = new BufferedWriter(new OutputStreamWriter(applyPatchProcess.getOutputStream()));
                shellWriter.write("su");
                shellWriter.newLine();
                shellWriter.flush();
                shellWriter.write("cp /sdcard/Android/obb/com.grima04.win32droid/patch.obb /data/data/com.grima04.win32droid/files");
                shellWriter.newLine();
                shellWriter.flush();
                shellWriter.write("cd /data/data/com.grima04.win32droid/files");
                shellWriter.newLine();
                shellWriter.flush();
                shellWriter.write("unzip patch.obb");
                shellWriter.newLine();
                shellWriter.flush();
                shellWriter.write("rm patch.obb");
                shellWriter.newLine();
                shellWriter.flush();
                shellWriter.write("echo 'Done! You can now safely start Wine'");
                shellWriter.newLine();
                shellWriter.flush();
                shellWriter.write("exit");
                shellWriter.newLine();
                shellWriter.flush();
                printConsoleOutput(applyPatchProcess,terminal);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    private void updateBox86(){
        isRooted = isDeviceRooted();
        if(isRooted){
            Toast toastBeginUpdate = Toast.makeText(getApplicationContext(),"Updating box86 ...",Toast.LENGTH_SHORT);
            toastBeginUpdate.show();
            try {
                printConsole = true;
                updateBox86ProcessBuilder = new ProcessBuilder("/bin/sh");
                updateBox86Process = updateBox86ProcessBuilder.start();
                //processPID = getPid(emulationProcess);
                shellWriter = new BufferedWriter(new OutputStreamWriter(updateBox86Process.getOutputStream()));
                //Print info to Terminal
                shellWriter.write("echo 'Downloading and updating box86. Please wait...'");
                shellWriter.newLine();
                shellWriter.flush();
                //Get root permissions
                shellWriter.write("su");
                shellWriter.newLine();
                shellWriter.flush();
                //cd into the Ubuntu rootfs and start the chroot jail environment via premade script
                shellWriter.write("cd /data/data/com.grima04.win32droid/files && sh chroot.sh");
                shellWriter.newLine();
                shellWriter.flush();
                //Remove previous source code folder
                shellWriter.write("rm -r box86");
                shellWriter.newLine();
                shellWriter.flush();
                //Download box86 source code
                shellWriter.write("git clone https://github.com/ptitSeb/box86");
                shellWriter.newLine();
                shellWriter.flush();
                //Build & setup box86
                shellWriter.write("cd box86 && mkdir build && cd build && cmake .. -DARM_DYNAREC=ON -DCMAKE_BUILD_TYPE=RelWithDebInfo && make -j4 install");
                shellWriter.newLine();
                shellWriter.flush();
                //Print message to inform the user that the updating process has been completed
                shellWriter.write("echo 'Done! You can now safely start Wine'");
                shellWriter.newLine();
                shellWriter.flush();
                //Print the shell output in the Android Studio Logcat and on the application TextView
                printConsoleOutput(updateBox86Process,terminal);
            }catch(Exception e){
                e.printStackTrace();
            }
        }else{
            Toast toastFailed = Toast.makeText(getApplicationContext(),"Failed to update box86.\nAre you rooted?",Toast.LENGTH_LONG);
            toastFailed.show();
        }
    }

    private void startEmulation(Button button){
        isRooted = isDeviceRooted();
        if (isRooted){
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
                //cd into the Ubuntu rootfs and start the chroot jail environment via premade script
                shellWriter.write("cd /data/data/com.grima04.win32droid/files && sh chroot.sh");
                shellWriter.newLine();
                shellWriter.flush();
                //Setup XServer XSDL display and audio output
                shellWriter.write("export DISPLAY=:0 && export PULSE_SERVER=tcp:127.0.0.1:4713");
                shellWriter.newLine();
                shellWriter.flush();
                //Wait 10s to give XServer XSDL enough time to start up
                Thread.sleep(10000);
                //Launch box86 with the Wine explorer, the Wine Desktop size being the fullscreen size of the device
                shellWriter.write("env BOX86_ALLOWMISSINGLIBS=1 BOX86_FIX_64BIT_INODES=1 " + getEmulationMode() + getRenderingBackend() + " " + getMesaOptions() + " " + getTaskset() + " box86 ~/wine/bin/wine explorer /desktop=win32droid," + getCustomResolution() + " " + wineService);
                shellWriter.newLine();
                shellWriter.flush();
                //Print the shell output in the Android Studio Logcat and on the application TextView
                printConsoleOutput(emulationProcess,terminal);
            }catch(Exception e){
                e.printStackTrace();
            }


        }else{
            Toast toastFailed = Toast.makeText(getApplicationContext(),"Failed to start emulation.\nAre you rooted?",Toast.LENGTH_LONG);
            toastFailed.show();
        }
    }
}