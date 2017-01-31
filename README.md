# PipeCutter
[![Build Status](https://travis-ci.org/zhivko/PipeCutter.svg?branch=master)](https://travis-ci.org/zhivko/PipeCutter)

Plasma 4 axis pipe cutter with BBB, CRAMPS and machinekit integration. Source code in JAVA.

Used to generate GCODE of Autodesk Inventor pipe like this:
![pipe](https://raw.githubusercontent.com/zhivko/PipeCutter/master/screenshots/pipe.png)
![pipeCutter](https://raw.githubusercontent.com/zhivko/PipeCutter/master/screenshots/PipeCutter.gif)  

And runs that gcode on BeagleBone Black  
![bbb](https://raw.githubusercontent.com/zhivko/PipeCutter/master/screenshots/REV_A5A.jpg)

With CRAMPS hat
![pipe](https://raw.githubusercontent.com/zhivko/PipeCutter/master/screenshots/cramps.jpg)

You can check my Google "Machinekit PipeCutter" album to better get idea of project:
[G+ Album](https://goo.gl/photos/4A623DBE1DQRwYfcA "Google+ Album")

**Steps to start SW**  
You would need:
minimum java 1.7
Apache Maven 3.3.3
clone GIT repository
```
git clone https://github.com/zhivko/PipeCutter
```
build self contained jar with running this command in cloned repo:
```
mvn package
```
start PipeCutter with:
```
java -jar ./target/SurfaceDemo-standalone-jar-with-dependencies.jar
```

# Linux notes
##Network setup, name resolution, internet sharing
###Nameserver on BBB
On beaglebone add nameservers with following:
```
echo nameserver 8.8.8.8 | sudo tee /etc/resolv.conf  #prints to screen as well
```
If you using USB connected BBB you would probably need to define gateway like this:
```
sudo route add default gw 192.168.7.1
```

###Internet sharing for USB connected BeagleBone Black
On host pc:
This should show you interface that has ip 192.168.7.1. For me it is ***enx544a16c5d02c***
```
klemen@dell:~$ ifconfig
enp3s0    Link encap:Ethernet  HWaddr f8:ca:b8:2b:6a:25  
          UP BROADCAST MULTICAST  MTU:1500  Metric:1
          RX packets:0 errors:0 dropped:0 overruns:0 frame:0
          TX packets:0 errors:0 dropped:0 overruns:0 carrier:0
          collisions:0 txqueuelen:1000 
          RX bytes:0 (0.0 B)  TX bytes:0 (0.0 B)

enx544a16c5d02c Link encap:Ethernet  HWaddr 54:4a:16:c5:d0:2c  
          inet addr:192.168.7.1  Bcast:192.168.7.3  Mask:255.255.255.252
          inet6 addr: fe80::494b:835b:a0ea:8f72/64 Scope:Link
          UP BROADCAST RUNNING MULTICAST  MTU:1500  Metric:1
          RX packets:1678 errors:0 dropped:0 overruns:0 frame:0
          TX packets:1662 errors:0 dropped:0 overruns:0 carrier:0
          collisions:0 txqueuelen:1000 
          RX bytes:192962 (192.9 KB)  TX bytes:140059 (140.0 KB)

lo        Link encap:Local Loopback  
          inet addr:127.0.0.1  Mask:255.0.0.0
          inet6 addr: ::1/128 Scope:Host
          UP LOOPBACK RUNNING  MTU:65536  Metric:1
          RX packets:33259 errors:0 dropped:0 overruns:0 frame:0
          TX packets:33259 errors:0 dropped:0 overruns:0 carrier:0
          collisions:0 txqueuelen:1 
          RX bytes:3308053 (3.3 MB)  TX bytes:3308053 (3.3 MB)

wlp2s0    Link encap:Ethernet  HWaddr 78:0c:b8:b3:33:c3  
          inet addr:192.168.2.120  Bcast:192.168.2.255  Mask:255.255.255.0
          inet6 addr: fe80::3e4f:2c0a:3a59:eb81/64 Scope:Link
          UP BROADCAST RUNNING MULTICAST  MTU:1500  Metric:1
          RX packets:1046848 errors:0 dropped:0 overruns:0 frame:0
          TX packets:876534 errors:0 dropped:0 overruns:0 carrier:0
          collisions:0 txqueuelen:1000 
          RX bytes:1071362743 (1.0 GB)  TX bytes:532466200 (532.4 MB)
```      
With interface that has ip of 192.168.7.1:
```
sudo iptables --append FORWARD --in-interface enx544a16c5d02c -j ACCEPT
```
With interface that has internet connection (in my case that is: wlp2s0)
```
sudo iptables --table nat --append POSTROUTING --out-interface wlp2s0 -j MASQUERADE
```
Enable IP forwarding with:
```
sudo sysctl -w net.ipv4.ip_forward=1
sudo /etc/init.d/procps restart
```

**Internet sharing on windows for USB connected BeagleBone Black**
Follow: http://lanceme.blogspot.hr/2013/06/windows-7-internet-sharing-for.html
```
sudo su
ifconfig usb0 192.168.7.2 netmask 255.255.255.252
route add default gw 192.168.7.1
echo 'nameserver 8.8.8.8' >> /etc/resolv.conf
```

##Back-up BBB (write eMMC to uSD)
sudo /opt/scripts/tools/eMMC/beaglebone-black-make-microSD-flasher-from-eMMC.sh
##BBB becomes unresponsive
http://dave.cheney.net/2013/09/22/two-point-five-ways-to-access-the-serial-console-on-your-beaglebone-black
##AVAHI Daemon doesn't always bring up beaglebone.local
```
sudo systemctl --system daemon-reload
sudo systemctl start avahi-daemon.service
sudo systemctl status avahi-daemon.service
```
***avahi service on pc host wont start***
check avahi service status with:
```
sudo systemctl status avahi-daemon
```
additional log available with
```
journalctl -xe
```
change domain name in /etc/avahi/avahi-daemon.conf to
```
domain-name=local
```
reload avahi daemon with
```
sudo systemctl restart avahi-daemon
```
change to use only ipv4
```
sudo nano /etc/avahi/avahi-daemon.conf
use-ipv6=no
```
***Enable lingering for long running processes started with machinekit user***
```
loginctl enable-linger machinekit
```

##flashing eMMC from uSD card

1. change uEnv.txt as below (notice last line ```init-eMMC-flasher-v3.sh```).
2. reboot. The blue on-board LEDs should light in sequence and then continue to flash for the next 5–25 minutes (depending on the distribution used and the speed of the SD card). The latest distribution flashes in a Cylon/Knightrider pattern.
3. Wait until the LEDs stop blinking and all 4 LEDs are fully lit. This process can take 5-25 minutes depending on the image used. If the flashing procedure fails—for example, no LEDs flash, or it keeps running for more than 45 minutes — then disconnect the power and try reflashing.
4. Remove the micro-SD card. This is important, as you could end up flashing the eMMC again by accident.

The line mentioned in the instructions is the last line of uEnv.txt. Fresh copy of bone-debian-8.2-tester-2gb-armhf-2015-11-12-2gb.img.xz and /boot/uEnv.txt looks like this after I uncommented the eMMC flasher command (last line):

```
#Docs: http://elinux.org/Beagleboard:U-boot_partitioning_layout_2.0

uname_r=4.1.12-ti-r29
#uuid=
#dtb=

##BeagleBone Black/Green dtb's for v4.1.x (BeagleBone White just works..)

##BeagleBone Black: HDMI (Audio/Video) disabled:
#dtb=am335x-boneblack-emmc-overlay.dtb

##BeagleBone Black: eMMC disabled:
#dtb=am335x-boneblack-hdmi-overlay.dtb

##BeagleBone Black: HDMI Audio/eMMC disabled:
#dtb=am335x-boneblack-nhdmi-overlay.dtb

##BeagleBone Black: HDMI (Audio/Video)/eMMC disabled:
#dtb=am335x-boneblack-overlay.dtb

##BeagleBone Black: wl1835
#dtb=am335x-boneblack-wl1835mod.dtb

##BeagleBone Black: replicape
#dtb=am335x-boneblack-replicape.dtb

##BeagleBone Green: eMMC disabled
#dtb=am335x-bonegreen-overlay.dtb

cmdline=coherent_pool=1M quiet cape_universal=enable

#In the event of edid real failures, uncomment this next line:
#cmdline=coherent_pool=1M quiet cape_universal=enable video=HDMI-A-1:1024x768@60e

##Example v3.8.x
#cape_disable=capemgr.disable_partno=
#cape_enable=capemgr.enable_partno=

##Example v4.1.x
#cape_disable=bone_capemgr.disable_partno=
#cape_enable=bone_capemgr.enable_partno=

##Disable HDMI/eMMC (v3.8.x)
#cape_disable=capemgr.disable_partno=BB-BONELT-HDMI,BB-BONELT-HDMIN,BB-BONE-EMMC-2G

##Disable HDMI (v3.8.x)
#cape_disable=capemgr.disable_partno=BB-BONELT-HDMI,BB-BONELT-HDMIN

##Disable eMMC (v3.8.x)
#cape_disable=capemgr.disable_partno=BB-BONE-EMMC-2G

##Audio Cape (needs HDMI Audio disabled) (v3.8.x)
#cape_disable=capemgr.disable_partno=BB-BONELT-HDMI
#cape_enable=capemgr.enable_partno=BB-BONE-AUDI-02


##enable Generic eMMC Flasher:
##make sure, these tools are installed: dosfstools rsync
cmdline=init=/opt/scripts/tools/eMMC/init-eMMC-flasher-v3.sh
```


##flashing BBB from RobertNelson Machinekit image
Get image from:  
```
wget https://rcn-ee.com/rootfs/bb.org/testing/2016-06-19/machinekit/bone-debian-8.5-machinekit-armhf-2016-06-19-4gb.img.xz
```
Write image to uSD card with:  
```
xzcat bone-debian-8.5-machinekit-armhf-2016-06-19-4gb.img.xz | sudo dd of=/dev/sdX
or with progress bar
pv bone-debian-8.5-machinekit-armhf-2016-06-19-4gb.img.xz | xzcat | sudo dd of=/dev/mmcblk0
```
Where sdX is sdA, sdB or sdC...
To find out which one check command and compare size of each partition with your uSD card capacity
```
klemen@dell:~$ df
Filesystem     1K-blocks     Used Available Use% Mounted on
udev             4014236        0   4014236   0% /dev
tmpfs             806708    18036    788672   3% /run
/dev/sda5      475243624 16586736 434492844   4% /
tmpfs            4033536   437124   3596412  11% /dev/shm
tmpfs               5120        4      5116   1% /run/lock
tmpfs            4033536        0   4033536   0% /sys/fs/cgroup
/dev/sda1         507904    29396    478508   6% /boot/efi
tmpfs             806708       80    806628   1% /run/user/1000
/dev/sda3      488281248 51698648 436582600  11% /media/klemen/OS

```
More detailed instructions in:
http://elinux.org/Beagleboard:BeagleBoneBlack_Debian#microSD.2FStandalone:_.28machinekit.29_Based_on_Debian_Jessie_.28new.29
<br>
***PostInstall procedure***  
Configure locale
```
sudo dpkg-reconfigure locales
```
Configure timezone
```
sudo dpkg-reconfigure tzdata
```
Test if xenomai is working
```
latency-test
```

##Machinekit notes
###Building machinekit from source
check:
```
http://www.machinekit.io/docs/developing/machinekit-developing/
```
You will need to add swap if you plan to build Machinekit on BBB
```
sudo mkdir -p /var/cache/swap/
sudo swapoff /var/cache/swap/swapfile
sudo dd if=/dev/zero of=/var/cache/swap/swapfile bs=3M count=256
sudo chmod 0600 /var/cache/swap/swapfile
sudo mkswap /var/cache/swap/swapfile
sudo swapon /var/cache/swap/swapfile
```
Short steps:
```
cd ~/git/machinekit
debian/configure -prx
sudo mk-build-deps -ir
cd src
make clean
./autogen.sh
./configure --with-platform-beaglebone
~/git/machinekit/scripts/check-system-configuration.sh
make
```


If you get this error while building:
```
Makefile warning: Warning: File `main.cpp' has modification time 2.1e+04 s in the future
```
issue:
```
find ~/git/machinekit/src -type f -exec touch {} +
```
###Building machinekit java protobuff classes
On BBB or system where you want to build protobuf java machinkeit classes clone repository
```
git clone https://github.com/machinekoder/machinetalk-protobuf.git
```
checkout java branch with:
```
git checkout java
```
Start build of java remote protobuff classes for machinekit with
```
make clean all
```
Then you get java remote classes that are used in PipeCutter machinekit client app.

 

###Hal remote components
https://github.com/mhaberler/asciidoc-sandbox/wiki/Remote-HAL-Components
###LinuxCnc related
**halcmd**
To show one pin value:
```
halcmd show pin motion.spindle-*
halcmd show pin stepgen.*
```
To log pin value to file:
```
(
  while true
  do
    halcmd show pin *.f-error >> ~/f-error.log
    sleep 0.100
  done
) &
disown
```
**install ordinary component *.comp**
```
comp --install ./git/machinekit/mycomponents/mythc.comp
```
**instal instantiable component *.icomp**
```
instcomp --install ./git/machinekit/mycomponents/udp.icomp
```
#Plasma integration
Plasma used: Powermax 45 XP
<br>
[Powermax 45 XP manual](./screenshots/OM_809240r1_PMX45XP.pdf)
<br>
Plasma max voltage: 360V
<br>
Voltage divider in Powermax 45 XP: set to 1:40
<br>
![HyperthermVoltageDivider](https://raw.githubusercontent.com/zhivko/PipeCutter/master/screenshots/HyperthermVoltageDivider.png)
<br>
Max voltage at thcad: 360.0/40.0=9.0V
<br>
The BBB & Hypertherm CPC connection
![PLASMA_WIREUP](https://raw.githubusercontent.com/zhivko/PipeCutter/master/screenshots/PLASMA_WIREUP.jpg)
Pins to be used on BBB:
```
CRAMPS_P503_2 MISO P929 ...  for thc voltage from mesa thc 10v - needs to go to pru_generic encoder
CRAMPS_P503_6 MOSI P930 ...  arc OK - transfer start CNC machine motion
CRAMPS_P503_4 SCK  P931 ...  plasma start physically connected to 3.3V relay
```
Fritzing connections below:
![Fritzing pipecuter setup](https://raw.githubusercontent.com/zhivko/PipeCutter/master/screenshots/PipeCutter_bb.png)


##THCAD
[THCAD 10V manual](./screenshots/THCAD_10v.pdf)
<br>
OUTPUT FREQUENCY switch: use F/32: 1000000Hz/32 = 31250Hz
<br>
UNIPOLAR (0V to +10V)/ BIPOLAR MODE (-5V to +5V) switch: use unipolar (0V to +10V)
<br>
![THCAD_10V](https://raw.githubusercontent.com/zhivko/PipeCutter/master/screenshots/THCAD_10V.png)
Thcad calibration:
<br>
![THCAD_10V](https://raw.githubusercontent.com/zhivko/PipeCutter/master/screenshots/thcad10_calibration.ods)

##Linuxcnc thc − Torch Height Control component
[Linuxcnc THC comp](http://linuxcnc.org/docs/html/man/man9/thc.9.html)

#Next steps
stmbl servo controler that uses "cascaded pid with feed forward, and clamping"
https://github.com/rene-dev/stmbl/blob/newstuff/src/comps/ypid.comp

Stmbl servo driver:
https://github.com/rene-dev/stmbl/blob/master/hw/kicad/v4.0/doc/stmbl_4.0.pdf

Interface to cdsteinkuhler de0 nano soc hat:
https://github.com/rene-dev/stmbl/tree/bob/hw/kicad/bob/db25_bob
bottom board
https://seafile.ist-wunderbar.com/f/4a41a8bc28/

CdSteinkuhler de0 nano soc altera Cyclone 5 hat:
http://blog.machinekit.io/2016/11/you-will-recall-that-while-back-charles.html
https://oshpark.com/shared_projects/ZSjsiCUd



