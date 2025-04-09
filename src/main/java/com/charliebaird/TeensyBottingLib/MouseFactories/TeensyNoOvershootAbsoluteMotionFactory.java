package com.charliebaird.TeensyBottingLib.MouseFactories;

import com.charliebaird.TeensyBottingLib.MouseFactories.Support.TeensyAbsoluteMouseAccessor;
import com.charliebaird.TeensyBottingLib.MouseFactories.Support.TeensyAbsoluteSystemCalls;
import com.charliebaird.TeensyBottingLib.TeensyIO;
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
