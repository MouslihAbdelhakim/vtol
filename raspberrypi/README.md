### Rapsberry pi configuration
Follow these steps to set up the Rapsberry Pi, all links on this page are taken from [Navio2 docs](https://docs.emlid.com/navio2/common/ardupilot/configuring-raspberry-pi/), and this is an [archived version of the Navio2 docs](https://web.archive.org/web/20200906175643/https://docs.emlid.com/navio2/common/ardupilot/configuring-raspberry-pi/).

### Initial setup and first ssh session
1. Download SD card image with [the emlid Raspbian](http://files.emlid.com/images/emlid-raspbian-20200922.img.xz)
2. Use [Etcher](https://etcher.io/) to Flash to OS the the SD card
3. Remove and re-insert the SD card
4. force the Raspberry Pi to connect to wifi on start up by ensuring that `/Volumes/boot/wpa_supplicant.conf` look like this: 
```
country=JP # previous valies was GB
ctrl_interface=DIR=/var/run/wpa_supplicant GROUP=netdev
update_config=1

network={
	ssid="<get it from key chain access>"
	#psk="emrooftop"
	psk=<get it from key chain access>
}
```
5. Use `nmap -sn 192.168.1.*` to scan for all hosts on the wifi netwrok, and try to figure out which ip address corresponds to the PI
6. `ssh pi@192.168.1.7`, `raspberry` is the default password.
7. Set a static ip by adding the text below to the end of `/etc/network/interfaces`
```
auto wlan0
  iface wlan0 inet static
    address 192.168.1.200
    netmask 255.255.255.0
    gateway 192.168.1.1
    dns-nameservers 8.8.8.8 8.8.4.4
```
8. `sudo reboot`, and test that you can `ssh pi@192.168.1.7`
9. `sudo apt-get update && sudo apt-get dist-upgrade`