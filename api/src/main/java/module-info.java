import org.jspecify.annotations.NullMarked;

@NullMarked
module net.thenextlvl.worlds {
    exports net.thenextlvl.worlds.event;
    exports net.thenextlvl.worlds.generator;
    exports net.thenextlvl.worlds.preset;
    exports net.thenextlvl.worlds;
    exports net.thenextlvl.worlds.experimental;

    requires com.google.common;
    requires com.google.gson;
    requires net.kyori.adventure.key;
    requires net.kyori.adventure;
    requires net.kyori.examination.api;
    requires net.thenextlvl.binder;
    requires org.bukkit;
    requires org.slf4j;

    requires static org.jetbrains.annotations;
    requires static org.jspecify;
}