package me.miran.executors.bridge;

import me.miran.Util;
import me.miran.executors.InputExecutor;
import me.miran.mixin.minecraft.KeyBindingAccessor;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

public abstract class BridgeExecutor extends InputExecutor {

    protected final Vec3d target;
    protected boolean running = true;

    public BridgeExecutor(int priority, boolean queuedExecutor, Vec3d target) {
        super(priority, queuedExecutor);
        this.target = target;
    }

    public final void tick(ClientPlayerEntity player, GameOptions options) {
        if (!equipBuildingBlock(player)) {
            player.sendMessage(Text.of("no building blocks :("));
            running = false;
            return;
        }


        tickBridge(player, options);
    }

    private boolean equipBuildingBlock(ClientPlayerEntity player) {
        PlayerInventory inv = player.getInventory();
        ItemStack buildStack = null;

        for (ItemStack stack : inv.main) {
            Block block = Util.asBlock(stack.getItem());
            if (block == null) continue;

            if (block.getDefaultState().isFullCube(player.getWorld(),player.getBlockPos())) {
                buildStack = stack;
                break;
            }
        }

        if (buildStack == null) {
            return false;
        }

        int slot = inv.getSlotWithStack(buildStack);
        if (slot < 9) {
            inv.selectedSlot = slot;
        } else {
            player.networkHandler.sendPacket(new PickFromInventoryC2SPacket(slot));
        }

        return true;
    }

    protected abstract void tickBridge(ClientPlayerEntity player, GameOptions options);

    public static void placeBlock() {
        KeyBindingAccessor keyBinding = (KeyBindingAccessor) MinecraftClient.getInstance().options.useKey;

        KeyBinding.onKeyPressed(keyBinding.getBoundKey());


        /*PlayerEntity player = MinecraftClient.getInstance().player;
        HitResult r = player.raycast(4, 0, false);
        if (r == null || r.getType() != HitResult.Type.BLOCK) return;

        PlayerInteractBlockC2SPacket packet = new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, (BlockHitResult) r, 0);


        MinecraftClient.getInstance().getNetworkHandler().sendPacket(packet);*/
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
