package net.thenextlvl.worlds.command;

import core.annotation.MethodsReturnNotNullByDefault;
import core.annotation.ParametersAreNotNullByDefault;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.syntax.StandardCommandSyntaxFormatter;

@MethodsReturnNotNullByDefault
@ParametersAreNotNullByDefault
public class CustomSyntaxFormatter<C> extends StandardCommandSyntaxFormatter<C> {
    public CustomSyntaxFormatter(CommandManager<C> manager) {
        super(manager);
    }

    @Override
    protected FormattingInstance createInstance() {
        return new FormattingInstance() {
            @Override
            public String optionalPrefix() {
                return "(";
            }

            @Override
            public String optionalSuffix() {
                return ")";
            }

            @Override
            public String requiredPrefix() {
                return "[";
            }

            @Override
            public String requiredSuffix() {
                return "]";
            }

            @Override
            public void appendPipe() {
                appendName(" | ");
            }
        };
    }
}
