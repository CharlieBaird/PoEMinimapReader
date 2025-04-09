package com.charliebaird.teensybottinglib;

import com.charliebaird.teensybottinglib.InputCodes.*;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class TeensyBot
{
    private final MouseMotionHandler mouseMotionHandler;
    private final TeensyController teensy;

    public TeensyBot()
    {
        teensy = new TeensyController();
        mouseMotionHandler = new MouseMotionHandler(teensy);
        HeldKeys = new HashSet<>();
        HeldMouseClicks = new HashSet<>();

        // Make sure snippet runs to close connection to Teensy
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            teensy.close();
            System.out.println("Teensy connection closed");

            // Safety: unpress / unclick anything held down on a crash
            for (KeyCode key : HeldKeys)
            {
                System.out.println("Exiting, releasing "+ key.getSerialValue());
                delayMS(10);
                keyRelease(key);
                delayMS(10);
            }

            for (MouseCode key : HeldMouseClicks)
            {
                System.out.println("Exiting, releasing "+ key.getSerialValue());
                delayMS(10);
                mouseRelease(key);
                delayMS(10);
            }
        }));
    }

    public void delayMS(int ms)
    {
        int max = ms * 2;
        int value = (int) Math.floor(Math.random() * (max - ms + 1) + ms);

        try {
            Thread.sleep(value);
        } catch (InterruptedException ignored) {
        }
    }

    public void mouseMoveGeneralLocation(Point p)
    {
        mouseMotionHandler.mouseMoveGeneralLocation(p.x, p.y);
    }

    public void mouseMoveExactLocation(Point p)
    {
        mouseMotionHandler.mouseMoveExactLocation(p.x, p.y);
    }

    public void mouseMoveRelative(Point p)
    {
        mouseMotionHandler.mouseMoveRelative(p.x, p.y);
    }

    private final Set<KeyCode> HeldKeys;
    private final Set<MouseCode> HeldMouseClicks;

    public void mouseClick(MouseCode mouseCode)
    {
        String serial = mouseCode.getSerialValue();
        if (!HeldMouseClicks.contains(mouseCode)) {
            HeldMouseClicks.add(mouseCode);
            teensy.mousePress(serial);
            delayMS(50);
            teensy.mouseRelease(serial);
            HeldMouseClicks.remove(mouseCode);
        }
        else {
            mouseRelease(mouseCode);
        }
    }

    public void mousePress(MouseCode mouseCode)
    {
        String serial = mouseCode.getSerialValue();
        if (!HeldMouseClicks.contains(mouseCode)) {
            System.out.println("Pressing " + serial);
            HeldMouseClicks.add(mouseCode);
            teensy.mousePress(serial);
        }
        else
        {
            System.out.println("Already pressing " + serial);
        }
    }

    public void mouseRelease(MouseCode mouseCode)
    {
        String serial = mouseCode.getSerialValue();
        if (HeldMouseClicks.contains(mouseCode)) {
            System.out.println("Releasing " + serial);
            HeldMouseClicks.remove(mouseCode);
            teensy.mouseRelease(serial);
        }
        else {
            System.out.println("Cannot release " + serial);
        }
    }

    public void keyClick(KeyCode keyCode)
    {
        String serial = keyCode.getSerialValue();
        if (!HeldKeys.contains(keyCode)) {
            HeldKeys.add(keyCode);
            teensy.keyPress(serial);
            delayMS(60);
            teensy.keyRelease(serial);
            HeldKeys.remove(keyCode);
        } else {
            keyRelease(keyCode);
        }
    }

    public void keyPress(KeyCode keyCode)
    {
        String serial = keyCode.getSerialValue();
        if (!HeldKeys.contains(keyCode)) {
            HeldKeys.add(keyCode);
            teensy.keyPress(serial);
        }
    }

    public void keyRelease(KeyCode keyCode)
    {
        String serial = keyCode.getSerialValue();
        if (HeldKeys.contains(keyCode)) {
            HeldKeys.remove(keyCode);
            teensy.keyRelease(serial);
        }
    }

    public void keyCombo(KeyCode... combo)
    {
        for (KeyCode key : combo) {
            keyPress(key);
            delayMS(80);
        }

        delayMS(60);

        for (int i = combo.length - 1; i >= 0; i--) {
            keyRelease(combo[i]);
            delayMS(15);
        }
    }

}
