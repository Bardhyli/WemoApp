package org.myhome.wemo;

public interface VirtualSwitchAction {
    void changeStatus(VirtualSwitch virtualSwitch, boolean on);
    boolean getState(VirtualSwitch virtualSwitch);
}
