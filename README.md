# Win32droid - box86 & Wine based Win32 emulator for Android
----
### Before we start with the explanations, I want to give a huge thanks to [ptitSeb](https://github.com/ptitSeb) and the [Wine developers](https://wiki.winehq.org/Who%27s_Who), who developed the x86 Linux Userspace Emulator box86 and the Windows compatibility layer respectively. Without the constant dedication to box86 by ptitSeb and his efforts to get Wine x86 to run on box86, aswell as the constant improvements to Wine by the Wine devs, this project would have never been possible.
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
Currently only software rendering via Mesa LLVMpipe is supported, so games, especially 3D ones will run **_SLOW_**. There is also an option to use virgl to get 3D acceleration but the state of virgl on Android is currently not very good and using virgl will cause a lot of graphical issues.
Don't expect to run GTA IV or something similar on this emulator.<br/>
As a reference you can take the following results that I got on a Snapdragon 865 device:<br/>
* Silent Hill 2, safe settings (minimum graphics, 640x480) runs at a _stunning_ 8-13 FPS in the intro scene (restroom scene & outside)<br/>
* Cinebench R11.5 32 Bit Mode, Multicore CPU benchmark: Score = 2.30
* Unigine Valley running with interpreter mode at around 2 to 5 seconds per frame (you read that right, **seconds per frame**)

### Will 3D Acceleration be supported in the future?
I would like to implement it, but at the moment I can't do much about it. I am a simple dev, who has recently gotten into Linux and Java coding. I don't know how to code in C/C++ (yet) and I don't have any graphics API knowledge (OpenGL, Vulkan, ...),
so don't keep your expectations too high. I am currently also busy with my university studies, so I can't dedicate much time (if at all) to this project and even less into acquiring and mastering a new coding language and graphics API. <br/> 
A few ideas to be able to provide 3D acceleration in the future would be: <br/>
-- to update the virglrenderer-android port by mittorn and use the latest virgl source code which would allow us to use full OpenGL 4.3 via emulation ontop of OpenGL ES 3.2. <br/>
-- to get libhybris to work in order to be able to directly access the Android OpenGL ES GPU drivers and then use a solution like GL4ES or virgl vtest for GL to GLES translation. <br/>
-- to get the Mesa DRI/DRM driver for Adreno, Mali, etc to work in order to use /dev/dri/card0 to access the GPU and provide a solid OpenGL backend. <br/>
-- to get the LOAX-Server project to work to provide GLES 2.0 acceleration and then use GL4ES or virgl vtest for GL to GLES translation.

### ETA for xyz WEN?
No, there will be no ETAs whatsoever.

----
## Known Issues:
* Software requiring DirectX is likely to suffer from crashes or won't boot at all. A fix for some DX applications is to use box86 in interpreter mode, however this will slow down the emulation to a crawl. An example would be Unigine Valley (DX9 and DX11 benchmark) which only runs when using interpreter mode and doesn't boot at all with dynarec or Silent Hill 2 which immediately crashes in the menus when using dynarec and only runs properly when using the interpreter.
It is recommended to use ExaGear for DirectX software until box86 gets patches for those issues.
* Running the Wine Desktop with another resolution than the game's default full screen resolution might cause crashes.
* Passing resolution arguments to XServer XSDL isn't supported, so setting up a custom resolution profile in XServer XSDL is required before changing the Wine Desktop resolution, otherwise Wine just renders a small window.
* Running XServer XSDL and Wine at different resolutions will cause problems, so please **make sure to run Wine and XServerXSDL at the _same_ resolution**.
* Some installers don't work, so it is **_recommended_** to provide your program in a **_portable format_** if possible (e.g.: portable .exe, game preinstalled in folder format). Again, this might be fixed when using interpreter mode, but no guarantee is given whatsoever that it will work.
* **_At the current state, crashes or freezes in any application are very likely to happen, especially when using DirectX and/or the dynamic recompiler. This doesn't happen on "normal" Linux machines, so it is probably an issue with box86 not working properly on Android kernels. I would still recommend to use ExaGear for now._**

----
## Goals & future plans (goals are subject to changes without notice):
### Short-term goals
* Fix bugs of course :)
* Try to clean up the rootfs OBB, to shrink its size and consume less phone storage
* Try to find and implement an existing solution to the 3D acceleration problem

### Mid-term goals
* Implement Proot in order to make the app compatible with non-rooted devices
* Provide a better UI and/or rewrite it and create a proper options menu

### Long-term goals
* Learn C/C++ and OpenGL to be able to port virgl or a similar project to Android in order to provide a solid, hardware accelerated OpenGL backend for games.

----
## Setup:
Installation instructions:
* **_PLEASE READ THE ENTIRE INSTRUCTIONS BEFORE BEGINNING THE INSTALLATION_**

* Install [XServer XSDL](https://play.google.com/store/apps/details?id=x.org.server) and [BusyBox](https://play.google.com/store/apps/details?id=stericson.busybox)
* **_Please make sure that BusyBox is installed properly before continuing, otherwise the setup will not work properly and you will need to wipe the Win32droid data before attempting a fresh installation. Please note that BusyBox might get uninstalled after a device reboot, so always make sure that it is installed before using Win32droid_**
* Download and install the Win32droid APK from the latest release
* Download the .obb from the latest release and put it into the following directory (create it if it doesn't exist): /storage/emulated/0/Android/obb/com.grima04.win32droid
* Launch the App and wait for the setup process to finish (keep an eye on the Terminal at the bottom)
* Start Wine with the WINE button, wait like half a minute and then return to the Win32droid App and press STOP (this step is only required once for the first startup after a fresh installation)
* Afterwards, start Wine again. Now you should see the Wine Desktop and the Wine Explorer showing up
* Now you can browse through your internal storage and launch a Windows program
* If your program requires OpenGL or DirectX, enable the software rendering switch before starting Wine
* If your program crashes, try to launch it with the box86 interpreter mode enabled. This will be much slower but might fix the program, since the interpreter x86 emulation is more accurate
* To launch the Wine configuration or registry edit window, press WINECFG or REGEDIT respectively
* To update box86, just press the UPDATE BOX86 button
* NB: If you have already previously installed a .obb cache and you want to update the cache, you need to wipe the App data in the Android system settings and then proceed as indicated above. **_WARNING: this will of course wipe your entire Wine cache and installed files, savegames, etc. Make sure to backup everything you need before proceeding. You have been warned!_**
* **_I am not reponsible for any damage/harm caused to you or your device, data, etc resulting from the usage of this app. The emulation is very taxing on the CPU, so keep your CPU and battery temperatures under constant observation_**

----
## Credits & Third Party components:
* [Ubuntu on Termux](https://github.com/EXALAB/Anlinux-Resources/blob/master/Scripts/Installer/Ubuntu/ubuntu.sh) GPL-2.0 Licence
* [Ubuntu rootfs](https://github.com/EXALAB/Anlinux-Resources/blob/master/Rootfs/Ubuntu/armhf/ubuntu-rootfs-armhf.tar.xz) GPL-2.0 Licence
* [Termux armhf on aarch64 devices](https://www.youtube.com/watch?v=crP4K8p9Z50)
* [box86 emulator](https://github.com/ptitSeb/box86) MIT Licence
* [Mesa LLVMpipe driver](https://gitlab.freedesktop.org/mesa/mesa) MIT Licence (for the part used in this project)
* [Wine](https://www.winehq.org) LGPL Licence
* [Play on Linux prebuilt x86 Wine](https://www.playonlinux.com/wine) LGPL Licence
* [XServer XSDL](https://github.com/pelya/xserver-xsdl) Modified MIT Licence (X.Org version)
* [BusyBox for Android](https://play.google.com/store/apps/details?id=stericson.busybox) GPL-2.0 Licence
----
## Gallery:
* Main UI
![](https://github.com/Grima04/Win32droid/blob/master/Gallery/Main_UI.jpg?raw=true)
* Cinebench R11.5
![](https://github.com/Grima04/Win32droid/blob/master/Gallery/Cinebench_R_11_5.jpg?raw=true)
* Unigine Valley
![](https://github.com/Grima04/Win32droid/blob/master/Gallery/Unigine_Valley.jpg?raw=true)
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
