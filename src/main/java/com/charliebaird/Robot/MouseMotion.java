package com.charliebaird.Robot;

import com.github.joonasvali.naturalmouse.api.MouseMotionFactory;
import com.github.joonasvali.naturalmouse.support.*;
import com.github.joonasvali.naturalmouse.util.FlowTemplates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MouseMotion
{
    private MouseMotionFactory factory;

    public MouseMotion()
    {
        MouseMotionFactory factory = new MouseMotionFactory(new DefaultMouseMotionNature());
        List<Flow> flows = new ArrayList<>(Arrays.asList(
                new Flow(FlowTemplates.variatingFlow()),
//                new Flow(FlowTemplates.slowStartupFlow()),
                new Flow(FlowTemplates.slowStartup2Flow()),
                new Flow(FlowTemplates.adjustingFlow()),
                new Flow(FlowTemplates.jaggedFlow())
        ));
        DefaultSpeedManager manager = new DefaultSpeedManager(flows);
        factory.setDeviationProvider(new SinusoidalDeviationProvider(SinusoidalDeviationProvider.DEFAULT_SLOPE_DIVIDER));
        factory.setNoiseProvider(new DefaultNoiseProvider(DefaultNoiseProvider.DEFAULT_NOISINESS_DIVIDER));
        factory.getNature().setReactionTimeVariationMs(0);
        manager.setMouseMovementBaseTimeMs(250);

        DefaultOvershootManager overshootManager = (DefaultOvershootManager) factory.getOvershootManager();
        overshootManager.setOvershoots(0);

        factory.setSpeedManager(manager);

        this.factory = factory;
    }

    public void move(int x, int y)
    {
        try {
            factory.move(x, y);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
