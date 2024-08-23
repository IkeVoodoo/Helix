package me.ikevoodoo.helix.builtin.commands.parsers;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import me.ikevoodoo.helix.BukkitHelixProvider;
import me.ikevoodoo.helix.api.Helix;
import me.ikevoodoo.helix.api.logging.HelixLogger;
import me.ikevoodoo.helix.commands.parsers.RepositoryArgumentParser;

import java.util.concurrent.CompletableFuture;

public class RepositoryEntryParser implements ArgumentType<String> {

    public static final RepositoryEntryParser INSTANCE = new RepositoryEntryParser();

    @Override
    public String parse(StringReader reader) {
        final int start = reader.getCursor();
        while (reader.canRead() && (StringReader.isAllowedInUnquotedString(reader.peek()) || reader.peek() == '=')) {
            reader.skip();
        }
        return reader.getString().substring(start, reader.getCursor());
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        var nodes = context.getNodes();
        var last = nodes.isEmpty() ? null : nodes.get(nodes.size() - 1);
        var previous = nodes.isEmpty() ? null : last.getNode();

        if (!(previous instanceof ArgumentCommandNode<?, ?> arg) || !(arg.getType() instanceof RepositoryArgumentParser parser)) {
            return builder.buildFuture();
        }

        var repoString = last.getRange().get(context.getInput());
        try {
            var repoId = parser.parse(new StringReader(repoString));

            var manager = ((BukkitHelixProvider) Helix.provider()).getRepositoryManager();
            var repo = manager.getRepositories().get(repoId);

            var current = builder.getInput();

            var versionSpace = current.indexOf("==");
            if (versionSpace != -1) {
                var entry = repo.getEntries().get(current.substring(0, versionSpace));
                if (entry == null) {
                    return builder.buildFuture();
                }

                for (var version : entry.versions().supportedVersions().keySet()) {
                    builder.suggest(version.toString());
                }

                return builder.buildFuture();
            }

            for (var entry : repo.getEntries().values()) {
                builder.suggest(entry.resourceId());
            }
        } catch (CommandSyntaxException e) {
            HelixLogger.reportError(e);
        }

        return builder.buildFuture();
    }
}
