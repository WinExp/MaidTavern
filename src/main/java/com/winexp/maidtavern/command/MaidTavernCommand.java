package com.winexp.maidtavern.command;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import com.winexp.maidtavern.maid.brewing.BrewingList;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.server.command.EnumArgument;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class MaidTavernCommand {
    private static int orderPolicy(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        if (!(EntityArgument.getEntity(context, "maid") instanceof EntityMaid maid)) {
            source.sendFailure(Component.literal("选择的实体不是女仆"));
            return 0;
        }
        BrewingList.OrderPolicy orderPolicy = context.getArgument("order_policy", BrewingList.OrderPolicy.class);
        BrewingList brewingList = maid.getBrain().getMemory(MaidTavernEntities.BREWING_LIST.get()).orElse(null);
        if (brewingList == null) {
            source.sendFailure(Component.literal("女仆没有酿造列表"));
            return 0;
        }
        brewingList = new BrewingList.Builder(brewingList).orderPolicy(orderPolicy).build();
        maid.getBrain().setMemory(MaidTavernEntities.BREWING_LIST.get(), brewingList);

        return 1;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("maidtavern").then(
                literal("order_policy")
                        .then(
                                argument("maid", EntityArgument.entity())
                                        .then(
                                                argument("order_policy", EnumArgument.enumArgument(BrewingList.OrderPolicy.class))
                                                        .executes(MaidTavernCommand::orderPolicy)
                                        )
                        )
                )
        );
    }
}
