package com.charliebaird.teensybottinglib.MouseFactories.Support;

import com.charliebaird.teensybottinglib.TeensyIO;

import java.awt.*;

public class TeensyRelativeSystemCalls extends SystemCallsParent
{
    private final TeensyIO teensyIO;
    private final TeensyRelativeMouseAccessor relativeMouseAccessor;

    public TeensyRelativeSystemCalls(TeensyIO teensyIO, TeensyRelativeMouseAccessor mouseAccessor)
    {
        super();
        this.teensyIO = teensyIO;
        this.relativeMouseAccessor = mouseAccessor;
    }

    @Override
    public void setMousePosition(int x, int y)
    {
        int dx = relativeMouseAccessor.getMousePosition().x - x;
        int dy = relativeMouseAccessor.getMousePosition().y - y;
        relativeMouseAccessor.setLoc(new Point(x, y));
        teensyIO.mouseMoveRelative(dx, dy);
        System.out.println("Setting position");
    }
}
