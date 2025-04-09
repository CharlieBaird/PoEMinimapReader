package com.charliebaird.teensybottinglib.MouseFactories;

import com.charliebaird.teensybottinglib.TeensyIO;

public abstract class GeneralTeensyMotionFactory extends GeneralMotionFactory
{
    protected final TeensyIO teensyIO;

    public GeneralTeensyMotionFactory(TeensyIO teensyIO)
    {
        super();
        this.teensyIO = teensyIO;
    }
}
