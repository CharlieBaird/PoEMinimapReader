package com.charliebaird.teensybottinglib.MouseFactories;

import com.charliebaird.teensybottinglib.MouseFactories.Support.TeensyRelativeMouseAccessor;
import com.charliebaird.teensybottinglib.MouseFactories.Support.TeensyRelativeSystemCalls;
import com.charliebaird.teensybottinglib.TeensyController;
import com.github.joonasvali.naturalmouse.support.DefaultOvershootManager;
import com.github.joonasvali.naturalmouse.support.DefaultSpeedManager;

public class TeensyNoOvershootRelativeMotionFactory extends GeneralTeensyMotionFactory
{
    public TeensyNoOvershootRelativeMotionFactory(TeensyController teensyController)
    {
        super(teensyController);

        TeensyRelativeMouseAccessor teensyRelativeMouseAccessor = new TeensyRelativeMouseAccessor();
        getNature().setSystemCalls(new TeensyRelativeSystemCalls(teensyController, teensyRelativeMouseAccessor));
        getNature().setMouseInfo(teensyRelativeMouseAccessor);

        getNature().setReactionTimeVariationMs(0);
        DefaultOvershootManager overshootManager = (DefaultOvershootManager) getOvershootManager();
        overshootManager.setOvershoots(0);

        DefaultSpeedManager manager = new DefaultSpeedManager(flows);
        manager.setMouseMovementBaseTimeMs(150);
        setSpeedManager(manager);
    }
}
