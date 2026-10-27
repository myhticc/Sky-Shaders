import java.nio.file.*;
import java.util.Map;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.adapter.MappingNsRenamer;
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch;
import net.fabricmc.mappingio.format.tiny.Tiny2FileWriter;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

public final class PrepareMappings {
    public static void main(String[] args) throws Exception {
        var tree = new MemoryMappingTree();
        MappingReader.read(Path.of(args[0]), tree);
        var names = MappingReader.getNamespaces(Path.of(args[1]));
        // ProGuard maps Mojang names (source) to obfuscated names (target).
        var merge = new MappingNsRenamer(tree, Map.of(names.get(0), "named", names.get(1), "official"));
        MappingReader.read(Path.of(args[1]), new MappingSourceNsSwitch(merge, names.get(1)));
        try (var writer = new Tiny2FileWriter(Files.newBufferedWriter(Path.of(args[2])), false)) {
            tree.accept(writer);
        }
        System.out.println("Prepared mappings: " + tree.getSrcNamespace() + " -> " + tree.getDstNamespaces());
    }
}
