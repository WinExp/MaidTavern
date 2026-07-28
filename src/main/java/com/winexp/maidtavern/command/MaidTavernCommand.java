package com.winexp.maidtavern.command;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.winexp.maidtavern.entity.MaidTavernEntities;
import com.winexp.maidtavern.maid.brew.StorageBinding;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class MaidTavernCommand {
    public static int bindStorage(StorageBinding.Type type, CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        if (!(EntityArgument.getEntity(context, "maid") instanceof EntityMaid maid)) {
            source.sendFailure(Component.literal("选择的实体不是女仆"));
            return 0;
        }
        BlockPos pos = BlockPosArgument.getBlockPos(context, "pos");
        StorageBinding binding = maid.getBrain().getMemory(MaidTavernEntities.STORAGE_BINDING.get()).orElse(StorageBinding.EMPTY);
        binding = binding.add(type, pos);
        maid.getBrain().setMemory(MaidTavernEntities.STORAGE_BINDING.get(), binding);
        return 1;
    }

    public static int clearStorage(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        if (!(EntityArgument.getEntity(context, "maid") instanceof EntityMaid maid)) {
            source.sendFailure(Component.literal("选择的实体不是女仆"));
            return 0;
        }
        maid.getBrain().eraseMemory(MaidTavernEntities.STORAGE_BINDING.get());
        return 1;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> literalBindStorage(String name, StorageBinding.Type type) {
        return literal(name).then(
                argument("pos", BlockPosArgument.blockPos()).executes(context ->
                        bindStorage(type, context))
        );
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("maidtavern").then(
                literal("bind_storage")
                        .then(
                                argument("maid", EntityArgument.entity()).then(
                                        literalBindStorage("ingredients", StorageBinding.Type.INGREDIENTS)
                                ).then(
                                        literalBindStorage("results", StorageBinding.Type.RESULTS)
                                ).then(
                                        literalBindStorage("byproducts", StorageBinding.Type.BYPRODUCTS)
                                ).then(
                                        literal("clear").executes(MaidTavernCommand::clearStorage)
                                )
                        )
                )
        );
    }
}
