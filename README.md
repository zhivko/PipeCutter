# PipeCutter
[![Build Status](https://travis-ci.org/zhivko/PipeCutter.svg?branch=master)](https://travis-ci.org/zhivko/PipeCutter)

Plasma 4 axis pipe cutter with BBB, CRAMPS and machinekit integration. Source code in JAVA.

Used to generate GCODE of Autodesk Inventor pipe like this:

![pipe]({{site.baseurl}}/screenshots/pipe.png)
![pipeCutter]({{site.baseurl}}/screenshots/PipeCutter.gif)
And runs that gcode on [BeagleBone Black]
![bbb]({{site.baseurl}}/screenshots/REV_A5A.jpg)



![pipe]({{site.baseurl}}/screenshots/cramps.jpg)
You can check my Google "Machinekit PipeCutter" album to better get idea of project:
[G+ Album](https://goo.gl/photos/4A623DBE1DQRwYfcA)

**Functionalities**
Produce GCODE
Stepping through GCODE reading motion.program-line hal pin
<img src="https://github.com/zhivko/PipeCutter/blob/master/screenshots/steppingThroughGcodeWorks.png" alt="SteppingThroughGcode" width="50%" height="50%">
**Steps to start SW**
You would need:
minimum java 1.7
Apache Maven 3.3.3
clone this GIT repository
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
On beaglebone  issue add nameservers with following:
```
sudo echo "nameserver 193.189.160.13" >> /etc/resolv.conf
sudo echo "nameserver 193.189.160.13" >> /etc/resolv.conf
```
###Internet sharing
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
With interface that has ip of 192.168.7.2:
```
sudo iptables --append FORWARD --in-interface enx544a16c5d02c -j ACCEPT
```
With interface that has internet connection (in my case that is: wlp2s0)
```
sudo iptables --table nat --append POSTROUTING --out-interface wlp2s0 -j MASQUERADE
```

##Backing up BBB
http://elinux.org/BeagleBone_Black_Extracting_eMMC_contents
##BBB becomes unresponsive
http://dave.cheney.net/2013/09/22/two-point-five-ways-to-access-the-serial-console-on-your-beaglebone-black
##AVAHI Daemon doesn't always bring up beaglebone.local
```
sudo systemctl --system daemon-reload'
sudo systemctl start avahi-daemon.service
sudo systemctl status avahi-daemon.service```
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
```wget https://rcn-ee.com/rootfs/bb.org/testing/2016-06-19/machinekit/bone-debian-8.5-machinekit-armhf-2016-06-19-4gb.img.xz```
<br>
Write image to uSD card with:
```
xzcat bone-debian-8.5-machinekit-armhf-2016-06-19-4gb.img.xz | sudo dd of=/dev/sdX
```
Where sdX is sdA, sdB or sdC...
To find out wich one check command and compare size of each partition with your uSD card capacity
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

##Machinekit notes
###Hal remote components
https://github.com/mhaberler/asciidoc-sandbox/wiki/Remote-HAL-Components
###LinuxCnc related
**halcmd**
To show one pin value:
```
halcmd && show pin motion.spindle-*
```
To monitor pin value:
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
