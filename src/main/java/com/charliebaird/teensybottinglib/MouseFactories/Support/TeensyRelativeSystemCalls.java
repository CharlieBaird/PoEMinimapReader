package com.charliebaird.teensybottinglib.MouseFactories.Support;

import com.charliebaird.teensybottinglib.TeensyController;

import java.awt.*;

public class TeensyRelativeSystemCalls extends SystemCallsParent
{
    private final TeensyController teensyController;
    private final TeensyRelativeMouseAccessor relativeMouseAccessor;

    public TeensyRelativeSystemCalls(TeensyController teensyController, TeensyRelativeMouseAccessor mouseAccessor)
    {
        super();
        this.teensyController = teensyController;
        this.relativeMouseAccessor = mouseAccessor;
    }

    @Override
    public void setMousePosition(int x, int y)
    {
        int dx = relativeMouseAccessor.getMousePosition().x - x;
        int dy = relativeMouseAccessor.getMousePosition().y - y;
        relativeMouseAccessor.setLoc(new Point(x, y));
        teensyController.mouseMoveRelative(dx, dy);
        System.out.println("Setting position");
    }
}
