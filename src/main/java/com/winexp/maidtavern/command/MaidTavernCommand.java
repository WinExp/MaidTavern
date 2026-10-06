package com.winexp.maidtavern.command;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import com.winexp.maidtavern.logistics.waiter.Order;
import com.winexp.maidtavern.logistics.waiter.WaiterOrderManager;
import com.winexp.maidtavern.maid.behavior.brewing.BrewingList;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.NbtTagArgument;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.command.EnumArgument;

import java.util.UUID;

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

    private static int order(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayer();
        UUID uuid = player == null ? null : player.getUUID();
        WaiterOrderManager manager = WaiterOrderManager.get(source.getLevel());
        Order order = Order.CODEC_WITHOUT_UUID.parse(NbtOps.INSTANCE, NbtTagArgument.getNbtTag(context, "order")).getOrThrow();
        manager.order(order, uuid);
        return 1;
    }

    private static int orderClear(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        level.getDataStorage().set(WaiterOrderManager.NAME, WaiterOrderManager.factory(level).constructor().get());
        return 1;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                literal("maidtavern")
                        .then(
                                literal("order_policy")
                                        .requires(source -> source.hasPermission(2))
                                        .then(
                                                argument("maid", EntityArgument.entity())
                                                        .then(
                                                                argument("order_policy", EnumArgument.enumArgument(BrewingList.OrderPolicy.class))
                                                                        .executes(MaidTavernCommand::orderPolicy)
                                                        )
                                        )
                        )
                        .then(
                                literal("order")
                                        .requires(source -> source.hasPermission(2))
                                        .then(
                                                literal("add")
                                                        .then(
                                                                argument("order", NbtTagArgument.nbtTag())
                                                                        .executes(MaidTavernCommand::order)
                                                        )
                                        )
                                        .then(
                                                literal("clear")
                                                        .executes(MaidTavernCommand::orderClear)
                                        )
                        )
        );
    }
}
