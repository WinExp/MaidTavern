package com.winexp.maidtavern.compat.maid;

import com.github.tartaricacid.touhoulittlemaid.api.ILittleMaid;
import com.github.tartaricacid.touhoulittlemaid.api.LittleMaidExtension;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.ExtraMaidBrainManager;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import com.winexp.maidtavern.maid.grape.TaskGrape;
import com.winexp.maidtavern.maid.brewing.TaskBrewing;

@LittleMaidExtension
public class LittleMaidCompat implements ILittleMaid {
    @Override
    public void addMaidTask(TaskManager manager) {
        manager.add(new TaskGrape());
        manager.add(new TaskBrewing());
    }

    @Override
    public void addExtraMaidBrain(ExtraMaidBrainManager manager) {
        manager.addExtraMaidBrain(new TavernExtraMaidBrain());
    }
}
