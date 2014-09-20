## MultiWork Project

This is a PC client for MultiWork hardware. Currently in Beta I need to test Bluetooth connection and add USB connection too.
It is a Logic Analyzer, Frecuencimeter and LC Meter.
The hardware is made by me with a dsPIC Microcontroller and communicates with the PC through Bluetooth with an HC-06 module. 

## Logic Analyzer Features
* 8 channels available
* Capable of decoding UART, I2C, SPI (Beta) and 1-Wire (Beta) communications
* Data can be showed in a list with the decoded data or in a chart to see waveform
* JavaFX 8 Modern UI
* Up to 40MSPS
* Buffer size for 16000 samples
* Trigger by state change for each channel
* Ability to import CSV files from oscilloscopes so we can sample data with oscilloscopes and analyze it using MultiWork

## Requirements
* Java 8.0</li>
* Electronic hardware which sample the data, schematics and PCB can be found [here](https://www.dropbox.com/sh/oq76xrg0jv6cvfu/KZ4UXd6o5D/MultiWork%20Altium)
* dsPIC firmware can be found [here](https://github.com/dragondgold/MultiWork_dsPIC)

## Libraries/API
* MultiWork uses [Highcharts](http://www.highcharts.com/) to show data waveform and [Blacklabel annotations plugin](https://github.com/blacklabel/annotations)
* The Protocol Decoder API written by me which you can see [here](https://github.com/dragondgold/ProtocolDecoderAPI).

## License

The Multiwork project is released under [BSD 2-Clause License](http://opensource.org/licenses/BSD-2-Clause)