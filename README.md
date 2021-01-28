# Win32droid - box86 & Wine based Win32 emulator for Android
----
## Before we start with the explanations, I want to give a huge shout-out to ptitSeb (https://github.com/ptitSeb) and the Wine developers, who developed the x86 Linux Userspace Emulator box86 and the Windows compatibility layer respectively. Without the constant dedication to box86 by ptitSeb and his efforts to get Wine x86 to run on box86, aswell as the constant improvements to Wine by the Wine devs, this project would have never been possible.
## FAQ:
### What is Win32droid?
Win32droid is an experimental software to emulate Windows x86 games on **_rooted_** Android ARM/ARM64 devices, based on the OpenSource projects Wine and box86

### How does it work?
Win32droid uses BusyBox for Android to chroot into an Ubuntu Focal environment which has box86, Wine x86 and Mesa LLVMpipe preinstalled. 
Afterwards, after having chrooted into the environment, the Win32droid app executes a customized shell command to launch box86 with Wine on your device while simultaneously launching XServer XSDL as the X Server backend for Wine to render it's screen on.
box86 is used as the x86 emulation backend in order to be able to use Wine x86 and Wine is used as a translation layer for Windows software on Linux. Graphics are currently processed
via the Mesa LLVMpipe software rendering OpenGL backend combined with Wine's D3D wrappers.

### In what state is Win32droid at the moment?
Currently, some Windows x86 programs and games should boot or run. 
I haven't tested much software on it yet but during my testing Cinebench R11.5 and Silent Hill 2 could boot and run on the emulator. 
To get a better idea of what might be able to run or boot, please refer to the box86 and Wine compatibility lists.

### How fast is the emulator?
Don't expect this to be fast. While the box86 dynamic recompiler is quite decent and fast, the main bottleneck at the moment is the graphics backend. 
Currently only software rendering via Mesa LLVMpipe is supported, so games, especially 3D ones will run **_SLOW_**. 
Don't expect to run GTA IV or something similar on this emulator.<br/>
As a reference you can take the following results that I got on a Snapdragon 865 device:<br/>
* Silent Hill 2, safe settings (minimum graphics, 640x480) runs at a _stunning_ 8-13 FPS in the intro scene (restroom scene & outside)<br/>
* Cinebench R11.5 32 Bit Mode, Multicore CPU benchmark: Score = 2.30

### Will 3D Acceleration be supported in the future?
I would like to implement it, but at the moment I can't do much about it. I am a simple dev, who has recently gotten into Linux and Java coding. I don't know how to code in C/C++ (yet) and I don't have any graphics API knowledge (OpenGL, Vulkan, ...),
so don't keep your expectations too high. I am currently also busy with my university studies, so I can't dedicate much time (if at all) to this project and even less into acquiring and mastering a new coding language and graphics API. <br/>
The only viable possibility that I know of to **_maybe_** get 3D hardware acceleration would be to compile and install Mesa DRM/DRI in order to be able to use the GPU directly, however not a lot of devices currently support DRM. Although my device (Snapdragon 865 SoC) has DRM, it requires Mesa to be compiled for Freedreno with a KGSL DRM backend, since Qualcomm currently uses a custom DRM implementation on Android, however the Mesa compilation for the KGSL target currently fails, so I have no way of testing it. Another idea would be to get some kind of GL-over-the-network software to run but at the moment the projects that can accomplish this are in a really bad state and/or abandoned (see virglrenderer-android or android-gl-streaming), so it won't be possible to use this method in the near future.

### ETA for xyz WEN?
No, there will be no ETAs whatsoever. Like many other devs already say/said, ETAs and people asking every second for an "ETA WEN?" have been a plague and only slowed down development or even
made some devs quit if it was too toxic. Please understand that we also have lives, a family, jobs/studies, etc. We aren't some aliens sitting 24/7 in front of the PC and spitting out code
as everyone pleases and commands.

----
## Known Issues:
* Software requiring DirectX 9 (and probably also DX10/11) currently crashes / doesn't boot at all. A fix for some DX9 applications is to use box86 in interpreter mode, however this will slow down the emulation to a crawl.
* Mesa LLVMpipe seems to have some problems with the Wine D3D wrapper, causing random crashes/freezes (e.g.: Silent Hill 2 crashes after a few seconds to a few minutes max.).
* Running the Wine Desktop with another resolution than the game's default full screen resolution might cause crashes.
* Passing resolution arguments to XServer XSDL isn't supported, so setting up a custom resolution profile in XServer XSDL is required before changing the Wine Desktop resolution, otherwise Wine just renders a small window.
* Running XServer XSDL and Wine at different resolutions will cause problems, so please **make sure to run Wine and XServerXSDL at the _same_ resolution**.
* Some installers don't work, so it is **_recommended_** to provide your program in a **_portable format_** if possible (e.g.: portable .exe, game preinstalled in folder format). Again, this might be fixed when using interpreter mode, but no guarantee is given whatsoever that it will work.

----
## Goals & future plans (goals are subject to changes without notice):
### Short-term goals
* Fix bugs of course :)
* Try to clean up the rootfs OBB, to shrink its size and consume less phone storage
* Try to implement Mesa DRM KGSL as soon as it is fixed to attempt to get GPU acceleration to work on Qualcomm devices with DRM

### Mid-term goals
* Implement Proot in order to make the app compatible with non-rooted devices
* Provide a better UI and/or rewrite it and create a proper options menu
* Try to get the LOAX-Server project to work with Win32droid, to provide hardware acceleration via OpenGL ES 2 and combine it with a wrapper for Desktop OpenGL (like GL4ES, Regal or virgl-vtest as GL to GLES translators)

### Long-term goals
* Learn C/C++ and OpenGL to be able to port virgl or a similar project to Android in order to provide a solid, hardware accelerated OpenGL backend for games.

----
## Setup:
Installation instructions:
* **_PLEASE READ THE ENTIRE INSTRUCTIONS BEFORE BEGINNING THE INSTALLATION_**

* Install XServer XSDL and BusyBox (https://play.google.com/store/apps/details?id=x.org.server and https://play.google.com/store/apps/details?id=stericson.busybox)
* **_Please make sure that BusyBox is installed properly before continuing, otherwise the setup will not work properly and you will need to wipe the Win32droid data before attempting a fresh installation. Please note that BusyBox might get uninstalled after a device reboot, so always make sure that it is installed before using Win32droid_**
* Download and install the Wine32droid APK from the latest release
* Download the .obb from the latest release and put it into the following directory (create it if it doesn't exist): /storage/emulated/0/Android/obb/com.grima04.wine32droid
* Launch the App and wait for the setup process to finish (keep an eye on the Terminal at the bottom)
* Start Wine with the WINE button, wait like half a minute and then return to the Wine32droid App and press STOP (this step is only required once for the first startup after a fresh installation)
* Afterwards, start Wine again. Now you should see the Wine Desktop and the Wine Explorer showing up
* Now you can browse through your internal storage and launch a Windows program
* If your program requires OpenGL or DirectX, enable the software rendering switch before starting Wine
* If your program crashes, try to launch it with the box86 interpreter mode enabled. This will be much slower but might fix the program, since the interpreter x86 emulation is more accurate
* To launch the Wine configuration or registry edit window, press WINECFG or REGEDIT respectively
* To update box86, just press the UPDATE BOX86 button
* NB: If you have already previously installed a .obb cache and you want to update the cache, you need to wipe the App data in the Android system settings and then proceed as indicated above. **_WARNING: this will of course wipe your entire Wine cache and installed files, savegames, etc. Make sure to backup everything you need before proceeding. You have been warned!_**
* **_I am not reponsible for any damage/harm caused to you or your device, data, etc resulting from the usage of this app. The emulation is very taxing on the CPU, so keep your CPU and battery temperatures under constant observation_**

----
## Gallery:
* Main UI
![](https://github.com/Grima04/Win32droid/blob/master/Gallery/Main_UI.jpg?raw=true)
* Cinebench R11.5
![](https://github.com/Grima04/Win32droid/blob/master/Gallery/Cinebench_R_11_5.jpg?raw=true)
* Silent Hill 2
![](https://github.com/Grima04/Win32droid/blob/master/Gallery/Silent_Hill_2_Title_Screen.jpg?raw=true)
![](https://github.com/Grima04/Win32droid/blob/master/Gallery/Silent_Hill_2_Intro_Scene_Mirror.jpg?raw=true)
![](https://github.com/Grima04/Win32droid/blob/master/Gallery/Silent_Hill_2_Urinal_Camera.jpg?raw=true)
![](https://github.com/Grima04/Win32droid/blob/master/Gallery/Silent_Hill_2_Mirror_2.jpg?raw=true)
![](https://github.com/Grima04/Win32droid/blob/master/Gallery/Silent_Hill_2_Outside_1.jpg?raw=true)
![](https://github.com/Grima04/Win32droid/blob/master/Gallery/Silent_Hill_2_Outside_2.jpg?raw=true)
![](https://github.com/Grima04/Win32droid/blob/master/Gallery/Silent_Hill_2_Car.jpg?raw=true)
![](https://github.com/Grima04/Win32droid/blob/master/Gallery/Silent_Hill_2_Map.jpg?raw=true)
![](https://github.com/Grima04/Win32droid/blob/master/Gallery/Silent_Hill_2_Toluca_Lake_Sign.jpg?raw=true)
