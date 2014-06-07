package com.andres.multiwork.pc;

import com.andres.multiwork.pc.connection.ConnectionManager;
import com.andres.multiwork.pc.screens.ScreenManager;
import com.protocolanalyzer.api.UARTProtocol;
import com.protocolanalyzer.api.utils.Configuration;
import org.apache.commons.configuration.XMLConfiguration;

import java.util.ResourceBundle;

/**
 * Global constants and parameters
 */
public final class GlobalValues {

    public static ScreenManager screenManager;
    public static XMLConfiguration xmlSettings;
    public static Configuration channelsSettings;
    public static ResourceBundle resourceBundle;

    public static ConnectionManager connectionManager;

    public static final int channelDisabled = -1;
    public static final int i2cProtocol = 50;
    public static final int uartProtocol = 51;
    public static final int clockProtocol = 52;

    public static final int parityEven = UARTProtocol.Parity.Even.ordinal();
    public static final int parityOdd = UARTProtocol.Parity.Odd.ordinal();
    public static final int parityNone = UARTProtocol.Parity.NoParity.ordinal();

    public static final int channelsNumber = 8;
    public static int screenWidth = 1024;
    public static int screenHeight = 768;

    // Can't instantiate this
    private GlobalValues(){}

}
