import java.nio.file.*;
import net.fabricmc.tinyremapper.*;
import net.fabricmc.tinyremapper.extension.mixin.MixinExtension;

public final class Remap {
    public static void main(String[] args) throws Exception {
        var builder = TinyRemapper.newRemapper()
            .withMappings(TinyUtils.createTinyMappingProvider(Path.of(args[0]), args[1], args[2]))
            .renameInvalidLocals(true).rebuildSourceFilenames(true);
        if (args[2].equals("intermediary")) builder.extension(new MixinExtension());
        var remapper = builder.build();
        try (var out = new OutputConsumerPath.Builder(Path.of(args[4])).build()) {
            var input = Path.of(args[3]);
            for (int i = 5; i < args.length; i++) remapper.readClassPath(Path.of(args[i]));
            out.addNonClassFiles(input);
            remapper.readInputs(input);
            remapper.apply(out);
        } finally { remapper.finish(); }
    }
}
