### Rapsberry pi configuration
Follow these steps to set up the Rapsberry Pi, all links on this page are taken from [Navio2 docs](https://docs.emlid.com/navio2/common/ardupilot/configuring-raspberry-pi/), and this is an [archived version of the Navio2 docs](https://web.archive.org/web/20200906175643/https://docs.emlid.com/navio2/common/ardupilot/configuring-raspberry-pi/).
### Initial setup and first ssh session
1. Download SD card image with [the emlid Raspbian](http://files.emlid.com/images/emlid-raspbian-20200922.img.xz).
2. Use [Etcher](https://etcher.io/) to Flash to OS the the SD card.
3. Remove and re-insert the SD card.
4. ensure that contains the following
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