### Rapsberry pi configuration
Follow these steps to set up the Rapsberry Pi, all links on this page are taken from [Navio2 docs](https://docs.emlid.com/navio2/common/ardupilot/configuring-raspberry-pi/), and this is an [archived version of the Navio2 docs](https://web.archive.org/web/20200906175643/https://docs.emlid.com/navio2/common/ardupilot/configuring-raspberry-pi/).

### Initial setup and your first ssh session
1. Download the [the emlid version of the Raspberry Pi OS](http://files.emlid.com/images/emlid-raspbian-20200922.img.xz)
2. Use [Etcher](https://etcher.io/) to Flash the OS the the the SD card
3. Remove and re-insert the SD card
4. force the Raspberry Pi to connect to wifi on start-up by ensuring that `/Volumes/boot/wpa_supplicant.conf` looks like this: 
```
country=JP # previous value was GB
ctrl_interface=DIR=/var/run/wpa_supplicant GROUP=netdev
update_config=1

network={
	ssid="<get it from the key chain access>"
	#psk="emrooftop"
	psk=<get it from the key chain access>
}
```
5. Use `nmap -sn 192.168.1.*` to scan for all hosts on the wifi netwrok, and figure out which ip address corresponds to the Raspberry Pi
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
8. `sudo reboot`, and test that you can `ssh pi@192.168.1.200`
9. `sudo apt-get update && sudo apt-get dist-upgrade`
10. exit the ssh session, and on the your local machine `ssh-copy-id pi@192.168.1.200 && ssh-add` to enable ssh loging without password
