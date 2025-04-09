package com.charliebaird.teensybottinglib.MouseFactories;

import com.charliebaird.teensybottinglib.MouseFactories.Support.TeensyAbsoluteMouseAccessor;
import com.charliebaird.teensybottinglib.MouseFactories.Support.TeensyAbsoluteSystemCalls;
import com.charliebaird.teensybottinglib.TeensyController;
import com.github.joonasvali.naturalmouse.support.DefaultOvershootManager;

public class TeensyOvershootAbsoluteMotionFactory extends GeneralTeensyMotionFactory
{
    public TeensyOvershootAbsoluteMotionFactory(TeensyController controller)
    {
        super(controller);
        getNature().setSystemCalls(new TeensyAbsoluteSystemCalls(teensyController));
        getNature().setMouseInfo(new TeensyAbsoluteMouseAccessor());

        getNature().setReactionTimeVariationMs(100);
        DefaultOvershootManager overshootManager = (DefaultOvershootManager) getOvershootManager();
        overshootManager.setOvershoots(2);
    }
}
