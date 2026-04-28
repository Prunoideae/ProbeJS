package moe.wolfgirl.probejs.legacy.features;

import com.google.gson.JsonObject;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class DelegatedCommandSourceStack extends CommandSourceStack {
    private final List<JsonObject> messageStack = new ArrayList<>();

    public DelegatedCommandSourceStack(CommandSourceStack stack) {
        this(stack, stack.getPosition());
    }

    public DelegatedCommandSourceStack(CommandSourceStack stack, Vec3 position) {
        this(stack, position, null);
    }

    public DelegatedCommandSourceStack(CommandSourceStack stack, Vec3 position, @Nullable Entity executor) {
        super(stack.source, position,
                stack.getRotation(), stack.getLevel(),
                4, stack.getTextName(),
                stack.getDisplayName(), stack.getServer(),
                executor
        );
    }

    private static JsonObject construct(String type, String content) {
        JsonObject object = new JsonObject();
        object.addProperty("type", type);
        object.addProperty("content", content);
        return object;
    }

    @Override
    public void sendSuccess(Supplier<Component> messageSupplier, boolean allowLogging) {
        messageStack.add(construct("success", messageSupplier.get().getString()));
    }

    @Override
    public void sendFailure(Component message) {
        messageStack.add(construct("failure", message.getString()));
    }

    @Override
    public void sendSystemMessage(Component message) {
        messageStack.add(construct("system", message.getString()));
    }

    public List<JsonObject> getCommandMessages() {
        var messages = List.copyOf(messageStack);
        messageStack.clear();
        return messages;
    }
}
