# PipeCutter
[![Build Status](https://travis-ci.org/zhivko/PipeCutter.svg?branch=master)](https://travis-ci.org/zhivko/PipeCutter)

Plasma 4 axis pipe cutter with BBB, CRAMPS and machinekit integration. Source code in JAVA.

Used to generate GCODE of Autodesk Inventor pipe like this:
<br>
<img src="./screenshots/pipe.png" alt="Pipe" width="50%" height="50%">
<br>
![alt tag](./screenshots/PipeCutter.gif)
<br>
And runs that gcode on [BeagleBone Black](https://beagleboard.org/black).
<br>
<img src="./screenshots/cramps.jpg" alt="Cramps" width="50%" height="50%">
<br>
You can check my Google "Machinekit PipeCutter" album to better get idea of project:
<br>
[G+ Album](https://goo.gl/photos/4A623DBE1DQRwYfcA)
<br>
<br>

**Functionalities**
<br>
Produce GCODE
<br>
Stepping through GCODE reading motion.program-line hal pin
<br>
<img src="./screenshots/steppingThroughGcodeWorks.png" alt="SteppingThroughGcode" width="50%" height="50%">
<br>


**Steps to start SW**
You would need:
<br>
OpenJDK Runtime Environment (build 1.8.0_45-internal-b14)
<br>
and
<br>
Apache Maven 3.3.3
<br>

1. clone this GIT repository
<br>
`git clone https://github.com/zhivko/PipeCutter`
<br>
2. build self contained jar with running this command in cloned repo:
<br>
`mvn package`
<br>
3. start PipeCutter with:
<br>
`java -jar ./target/SurfaceDemo-standalone-jar-with-dependencies.jar`
<br>

# Linux notes
##Backing up BBB
http://elinux.org/BeagleBone_Black_Extracting_eMMC_contents
##BBB becomes unresponsive
http://dave.cheney.net/2013/09/22/two-point-five-ways-to-access-the-serial-console-on-your-beaglebone-black
<br>
##AVAHI Daemon doesn't always bring up beaglebone.local
```
sudo systemctl --system daemon-reload'
sudo systemctl start avahi-daemon.service
sudo systemctl status avahi-daemon.service
```
##Adding swap file on BBB
```
sudo mkdir -p /var/cache/swap/
sudo dd if=/dev/zero of=/var/cache/swap/swapfile bs=1M count=256
sudo chmod 0600 /var/cache/swap/swapfile
sudo mkswap /var/cache/swap/swapfile
sudo swapon /var/cache/swap/swapfile
```
##flashing eMMC from uSD card
Login to machinekit BBB instance and navigate to /opt/scripts/tools/eMMC/
```
cd /opt/scripts/tools/eMMC/
```
and run the file manually...
```
sudo ./init-eMMC-flasher-v3.sh
```
##flashing BBB from RobertNelson Machinekit image
Get image from:
```
wget https://rcn-ee.com/rootfs/bb.org/testing/2016-06-19/machinekit/bone-debian-8.5-machinekit-armhf-2016-06-19-4gb.img.xz
```
Write image to uSD card with:
```
xzcat bone-debian-8.5-machinekit-armhf-2016-06-19-4gb.img.xz | sudo dd of=/dev/sdX
```
More detailed instructions in:
http://elinux.org/Beagleboard:BeagleBoneBlack_Debian#microSD.2FStandalone:_.28machinekit.29_Based_on_Debian_Jessie_.28new.29

##Machinekit notes
###Hal remote components
https://github.com/mhaberler/asciidoc-sandbox/wiki/Remote-HAL-Components
###LinuxCnc related
**halcmd**
```
show pin motion.spindle-*
(
  while true
  do
    halcmd show pin *.f-error >> ~/f-error.log
    sleep 0.100
  done
) &
disown
```
