package com.github.devcyntrix.deathchest.command.admin.argument;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.captions.Caption;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import cloud.commandframework.exceptions.parsing.ParserException;
import com.github.devcyntrix.deathchest.DeathChestPlugin;
import com.google.gson.internal.bind.util.ISO8601Utils;
import org.bukkit.util.StringUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;

public class DateArgument<C> extends CommandArgument<C, Date> {
    protected DateArgument(
            DeathChestPlugin deathChestPlugin,
            boolean required,
            @NonNull String name,
            @NonNull String defaultValue,
            @Nullable BiFunction<CommandContext<C>, String, List<String>> suggestionsProvider,
            @NonNull ArgumentDescription defaultDescription
    ) {
        super(required, name, new DateParser<>(deathChestPlugin), defaultValue, Date.class, suggestionsProvider, defaultDescription);
    }

    public static <C> CommandArgument.@NonNull Builder<C, Date> newBuilder(
            DeathChestPlugin deathChestPlugin,
            final @NonNull String name
    ) {
        return new DateArgument.Builder<>(deathChestPlugin, name);
    }

    public static <C> @NonNull CommandArgument<C, Date> of(
            DeathChestPlugin deathChestPlugin,
            final @NonNull String name
    ) {
        return DateArgument.<C>newBuilder(deathChestPlugin, name).asRequired().build();
    }

    public static final class Builder<C> extends CommandArgument.Builder<C, Date> {

        private final DeathChestPlugin deathChestPlugin;

        private Builder(DeathChestPlugin deathChestPlugin, final @NonNull String name) {
            super(Date.class, name);
            this.deathChestPlugin = deathChestPlugin;
        }

        @Override
        public @NonNull CommandArgument<C, Date> build() {
            return new DateArgument<>(
                    deathChestPlugin,
                    this.isRequired(),
                    this.getName(),
                    this.getDefaultValue(),
                    this.getSuggestionsProvider(),
                    this.getDefaultDescription()
            );
        }
    }

    public static final class DateParser<C> implements ArgumentParser<C, Date> {

        private final DeathChestPlugin deathChestPlugin;

        DateParser(DeathChestPlugin deathChestPlugin) {
            this.deathChestPlugin = deathChestPlugin;
        }

        @Override
        public @NonNull ArgumentParseResult<@NonNull Date> parse(
                @NonNull CommandContext<@NonNull C> commandContext,
                @NonNull Queue<@NonNull String> inputQueue
        ) {
            String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(DateParser.class, commandContext));
            }

            Date date;
            try {
                date = ISO8601Utils.parse(input, new ParsePosition(0));
            } catch (ParseException e) {
                return ArgumentParseResult.failure(new DateParseException(input, commandContext));
            }

            inputQueue.remove();
            return ArgumentParseResult.success(date);
        }

        @Override
        public @NonNull List<@NonNull String> suggestions(@NonNull CommandContext<C> commandContext, @NonNull String input) {
            return StringUtil.copyPartialMatches(
                    input,
                    deathChestPlugin.getReportManager().getReportDates().stream().map(ISO8601Utils::format).toList(),
                    new ArrayList<>()
            );
        }
    }

    public static final class DateParseException extends ParserException {

        private final String input;

        public DateParseException(
                final @NonNull String input,
                final @NonNull CommandContext<?> context
        ) {
            super(
                    DateArgument.DateParser.class,
                    context,
                    Caption.of("argument.parse.failure.date"),
                    CaptionVariable.of("input", input)
            );
            this.input = input;
        }

        public @NonNull String getInput() {
            return this.input;
        }
    }
}
