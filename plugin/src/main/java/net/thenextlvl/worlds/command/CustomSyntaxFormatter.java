package net.thenextlvl.worlds.command;

import cloud.commandframework.arguments.StandardCommandSyntaxFormatter;
import core.annotation.MethodsReturnNotNullByDefault;
import core.annotation.ParametersAreNotNullByDefault;

@MethodsReturnNotNullByDefault
@ParametersAreNotNullByDefault
public class CustomSyntaxFormatter<C> extends StandardCommandSyntaxFormatter<C> {
    @Override
    protected FormattingInstance createInstance() {
        return new FormattingInstance() {
            @Override
            public String getOptionalPrefix() {
                return "(";
            }

            @Override
            public String getOptionalSuffix() {
                return ")";
            }

            @Override
            public String getRequiredPrefix() {
                return "[";
            }

            @Override
            public String getRequiredSuffix() {
                return "]";
            }

            @Override
            public void appendPipe() {
                appendName(" | ");
            }
        };
    }
}
