package cr.codingale.things;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;

import nz.geek.android.things.drivers.adc.I2cAdc;

public class MainActivity extends Activity implements WebServer
        .WebserverListener {
    private String TAG = "Things:";
    private WebServer server;
    private final String PIN_LED = "BCM18";
    public Gpio mLedGpio;

    // Potentiometer
    private I2cAdc adc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        server = new WebServer(8180, this, this);
        PeripheralManager service = PeripheralManager.getInstance();
        try {
            mLedGpio = service.openGpio(PIN_LED);
            mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            I2cAdc.I2cAdcBuilder builder = I2cAdc.builder();
            adc = builder.address(0).fourSingleEnded().withConversionRate(100).build();
            adc.startConversions();
        } catch (IOException e) {
            Log.e(TAG, "Error en el API PeripheralIO", e);
        }
    }

    @Override
    protected void onDestroy() {
        server.stop();
        if (mLedGpio != null) {
            try {
                mLedGpio.close();
            } catch (IOException e) {
                Log.e(TAG, "Error en el API PeripheralIO", e);
            } finally {
                mLedGpio = null;
            }
        }
        super.onDestroy();
    }

    @Override
    public void switchLEDon() {
        try {
            mLedGpio.setValue(true);
            Log.i(TAG, "LED switched ON");
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }

    @Override
    public void switchLEDoff() {
        try {
            mLedGpio.setValue(false);
            Log.i(TAG, "LED switched OFF");
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }

    @Override
    public Boolean getLedStatus() {
        try {
            return mLedGpio.getValue();
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
            return false;
        }
    }

    @Override
    public String getPotentiometer() {
        String s = "";
        int value = 0;
        for (int i = 0; i < 4; i++) {
            s += " Canal" + i + ": " + adc.readChannel(i);
            if (i == 0) {
                value = adc.readChannel(i);
            }
        }
        double volt = value * 3.3 / 255;
        String voltageString = String.format("%.2f", volt);
        Log.d(TAG, voltageString+"V");

        return voltageString+"V";
    }
}
