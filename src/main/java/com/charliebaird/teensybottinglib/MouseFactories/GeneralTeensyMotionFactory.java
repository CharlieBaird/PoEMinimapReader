package com.charliebaird.teensybottinglib.MouseFactories;

import com.charliebaird.teensybottinglib.TeensyController;

public abstract class GeneralTeensyMotionFactory extends GeneralMotionFactory
{
    protected final TeensyController teensyController;

    public GeneralTeensyMotionFactory(TeensyController teensyController)
    {
        super();
        this.teensyController = teensyController;
    }
}
