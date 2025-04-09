package com.charliebaird.TeensyBottingLib.MouseFactories;

import com.charliebaird.TeensyBottingLib.TeensyIO;

public abstract class GeneralTeensyMotionFactory extends GeneralMotionFactory
{
    protected final TeensyIO teensyIO;

    public GeneralTeensyMotionFactory(TeensyIO teensyIO)
    {
        super();
        this.teensyIO = teensyIO;
    }
}
