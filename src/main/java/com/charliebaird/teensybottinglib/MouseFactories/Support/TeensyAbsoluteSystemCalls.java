package com.charliebaird.teensybottinglib.MouseFactories.Support;

import com.charliebaird.teensybottinglib.TeensyController;

public class TeensyAbsoluteSystemCalls extends SystemCallsParent
{
    private final TeensyController teensyController;

    public TeensyAbsoluteSystemCalls(TeensyController teensyController)
    {
        super();
        this.teensyController = teensyController;
    }

    @Override
    public void setMousePosition(int x, int y)
    {
        teensyController.mouseMove(x, y);
    }
}
