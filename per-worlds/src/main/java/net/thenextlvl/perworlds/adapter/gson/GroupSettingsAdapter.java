package net.thenextlvl.perworlds.adapter.gson;

import com.google.gson.InstanceCreator;
import net.thenextlvl.perworlds.GroupSettings;
import net.thenextlvl.perworlds.group.PaperGroupSettings;
import org.jspecify.annotations.NullMarked;

import java.lang.reflect.Type;

@NullMarked
public class GroupSettingsAdapter implements InstanceCreator<GroupSettings> {
    @Override
    public GroupSettings createInstance(Type type) {
        return new PaperGroupSettings();
    }
}
