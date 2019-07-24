package org.myhome;

import org.myhome.wemo.VirtualSwitch;
import org.myhome.wemo.VirtualSwitchAction;
import org.myhome.wemo.WemoEmulator;

public class Demo implements VirtualSwitchAction {

    private boolean state;

    public static void main(String[] args) {
        Demo demo = new Demo();
        demo.run();
    }

    private void run() {
        WemoEmulator client = new WemoEmulator();
        client.addVirtualSwitch("XXX special switch", 30000, this);
        client.addVirtualSwitch("AAA special switch", 30001, this);
        client.run();
    }

    public void changeStatus(VirtualSwitch virtualSwitch, boolean on) {
        state = on;
        System.out.println(virtualSwitch.getName() + ": " + on);
    }

    public boolean getState(VirtualSwitch virtualSwitch) {
        return state;
    }

}
