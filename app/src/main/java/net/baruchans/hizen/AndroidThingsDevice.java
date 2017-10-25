package net.baruchans.hizen;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

import static android.content.Context.BLUETOOTH_SERVICE;

public class AndroidThingsDevice {
    private Context context;

    public AndroidThingsDevice(Context context) {
        this.context = context;

        getBluetoothStatus();
    }

    public String getBluetoothStatus() {
        BluetoothManager manager = (BluetoothManager) context.getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = manager.getAdapter();

        String log = "";
        if (adapter == null) {
            log = "bluetooth is not supported.";
        } else {
            if (adapter.isEnabled()) {
                log = "bluetooth is already enabled.";
            } else {
                log = "bluetooth is enabling...";
                adapter.enable();

                // wait
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        Log.i(this.getClass().getName(), log);

        return log;
    }

}
