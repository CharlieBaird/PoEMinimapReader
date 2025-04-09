package com.charliebaird.teensybottinglib.MouseFactories;

import com.charliebaird.teensybottinglib.MouseFactories.Support.TeensyAbsoluteMouseAccessor;
import com.charliebaird.teensybottinglib.MouseFactories.Support.TeensyAbsoluteSystemCalls;
import com.charliebaird.teensybottinglib.TeensyIO;
import com.github.joonasvali.naturalmouse.support.DefaultOvershootManager;

public class TeensyNoOvershootAbsoluteMotionFactory extends GeneralTeensyMotionFactory
{
    public TeensyNoOvershootAbsoluteMotionFactory(TeensyIO controller)
    {
        super(controller);
        getNature().setSystemCalls(new TeensyAbsoluteSystemCalls(teensyIO));
        getNature().setMouseInfo(new TeensyAbsoluteMouseAccessor());

        getNature().setReactionTimeVariationMs(0);
        DefaultOvershootManager overshootManager = (DefaultOvershootManager) getOvershootManager();
        overshootManager.setOvershoots(0);
    }
}
