package dev.falseresync.libhudcompat;

import com.github.davidmoten.rtree2.Entries;
import com.github.davidmoten.rtree2.Entry;
import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Geometries;
import com.github.davidmoten.rtree2.geometry.Rectangle;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class LibHudCompat {
    protected static final Map<Identifier, Entry<Identifier, Rectangle>> entries = new HashMap<>();
    protected static final BiMap<Rectangle, RegionChangeListener> listeners = HashBiMap.create(100);
    protected static RTree<Identifier, Rectangle> tree = RTree.create();

    /**
     * Tries to occupy a screen region. If the region is completely free, you occupy it.
     * Otherwise, nothing happens.
     *
     * @param id Identifier of your HUD component. Please include your ModID here.
     * @return true if the region has been occupied, false if there are occupants already in the region
     */
    public static boolean tryOccupyRegion(Identifier id, int x, int y, int width, int height) {
        var rectangle = Geometries.rectangle(x, y, x + width, y + height);
        if (isRegionFree(rectangle)) {
            createEntryAndAddRegion(id, rectangle);
            return true;
        }

        return false;
    }

    /**
     * Tells you whether the region is completely free
     *
     * @return true if the region is already free, false if there are occupants already in the region
     */
    public static boolean isRegionFree(int x, int y, int width, int height) {
        return isRegionFree(Geometries.rectangle(x, y, x + width, y + height));
    }

    /**
     * Ignore other occupants and occupy a region
     */
    public static void forceOccupyRegion(Identifier id, int x, int y, int width, int height) {
        createEntryAndAddRegion(id, Geometries.rectangle(x, y, x + width, y + height));
    }

    /**
     * Ignore other occupants and occupy a region
     */
    public static void freeRegion(Identifier id) {
        var entry = entries.get(id);
        if (entry != null) {
            tree = tree.delete(entry);
            listeners.forEach((listenerRegion, listener) -> {
                if (isRegionFree(listenerRegion)) {
                    listener.onRegionChange(RegionChange.OCCUPIED);
                }
            });
        }
    }

    /**
     * Listen to changes in a specific region.
     */
    public static void addListener(int x, int y, int width, int height, RegionChangeListener listener) {
        listeners.put(Geometries.rectangle(x, y, x + width, y + height), listener);
    }

    public static void removeListener(RegionChangeListener listener) {
        listeners.inverse().remove(listener);
    }

    @ApiStatus.Internal
    protected static boolean isRegionFree(Rectangle region) {
        return !tree.search(region).iterator().hasNext();
    }

    @ApiStatus.Internal
    protected static void createEntryAndAddRegion(Identifier id, Rectangle region) {
        var entry = Entries.entry(id, region);
        entries.put(id, entry);
        tree = tree.add(entry);
        listeners.forEach((listenerRegion, listener) -> {
            if (!isRegionFree(listenerRegion)) {
                listener.onRegionChange(RegionChange.OCCUPIED);
            }
        });
    }

    @FunctionalInterface
    public interface RegionChangeListener {
        void onRegionChange(RegionChange change);
    }

    public enum RegionChange {
        OCCUPIED,
        FREED
    }
}
