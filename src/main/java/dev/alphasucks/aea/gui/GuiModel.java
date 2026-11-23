package dev.alphasucks.aea.gui;

import dev.alphasucks.aea.port.condition.ItemCondition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.class_1799;
import org.jetbrains.annotations.Nullable;

/**
 * Everything derived from a GUI's element list that client and server must agree on: the flattened slot
 * list (its order is the slot index order of the menu), slot/grid/slider ids, and per-element indexes.
 * Element indexes are the positions in {@code elements}; they are what the network packets refer to.
 */
public final class GuiModel {
    /** One GUI slot. {@code localX/localY}: item position (16x16) inside the element's box, unscaled. */
    public record SlotDef(int index, int element, String id, GuiElement.SlotType type, Optional<ItemCondition> itemCondition,
                          Optional<Integer> maxStack, @Nullable class_1799 displayItem, int localX, int localY, boolean background,
                          int row, int col) {
        public boolean isDisplay() {
            return type == GuiElement.SlotType.DISPLAY;
        }

        public boolean acceptsInsert() {
            return type == GuiElement.SlotType.INPUT || type == GuiElement.SlotType.STORAGE;
        }
    }

    /** A slot grid: {@code cells[row][col]} is the slot index or -1; rows/cols are the full (untrimmed) grid size. */
    public record Grid(String id, int element, int rows, int cols, int[][] cells, int rowStart, int colStart, int shownRows, int shownCols) {
        public List<Integer> slotIndices() {
            List<Integer> out = new ArrayList<>();
            for (int[] row : cells) {
                for (int idx : row) {
                    if (idx >= 0) {
                        out.add(idx);
                    }
                }
            }
            return out;
        }
    }

    private final List<GuiElement> elements;
    private final List<SlotDef> slots;
    private final Map<String, Integer> slotById;
    private final Map<String, Grid> gridById;
    private final Map<Integer, Grid> gridByElement;
    private final Map<String, Integer> sliderById;
    private final int[] firstSlot;
    private final int[] slotCount;
    private final boolean hasInteractive;
    private final boolean hasProgress;
    private final boolean hasServerEntities;
    private final boolean hasConditions;

    GuiModel(List<GuiElement> elements, boolean showInventory) {
        this.elements = elements;
        List<SlotDef> slotList = new ArrayList<>();
        Map<String, Integer> slotIds = new LinkedHashMap<>();
        Map<String, Grid> grids = new LinkedHashMap<>();
        Map<Integer, Grid> gridsByElement = new HashMap<>();
        Map<String, Integer> sliders = new LinkedHashMap<>();
        int[] first = new int[elements.size()];
        int[] count = new int[elements.size()];
        boolean interactive = showInventory;
        boolean progress = false;
        boolean serverEntities = false;
        boolean conditions = false;
        for (int e = 0; e < elements.size(); e++) {
            GuiElement el = elements.get(e);
            first[e] = slotList.size();
            conditions |= el.common().condition().isPresent();
            if (el instanceof GuiElement.Slot s) {
                interactive = true;
                String id = s.id().orElse("slot_" + e);
                if (slotIds.containsKey(id) || grids.containsKey(id)) {
                    throw new IllegalArgumentException("duplicate slot id '" + id + "'");
                }
                SlotDef def = new SlotDef(slotList.size(), e, id, s.slotType(), s.itemCondition(), s.maxStack(),
                    s.item().orElse(null), 1, 1, s.background(), 0, 0);
                slotIds.put(id, def.index());
                slotList.add(def);
            } else if (el instanceof GuiElement.SlotGrid g) {
                interactive = true;
                String id = g.id().orElse("grid_" + e);
                if (slotIds.containsKey(id) || grids.containsKey(id)) {
                    throw new IllegalArgumentException("duplicate slot id '" + id + "'");
                }
                boolean[][] on = g.cells();
                int minR = Integer.MAX_VALUE, maxR = -1, minC = Integer.MAX_VALUE, maxC = -1;
                for (int r = 0; r < g.rows(); r++) {
                    for (int c = 0; c < g.columns(); c++) {
                        if (on[r][c]) {
                            minR = Math.min(minR, r);
                            maxR = Math.max(maxR, r);
                            minC = Math.min(minC, c);
                            maxC = Math.max(maxC, c);
                        }
                    }
                }
                int rowStart = 0, colStart = 0, shownRows = g.rows(), shownCols = g.columns();
                if (g.trim() && maxR >= 0) {
                    rowStart = Math.max(0, minR - 1);
                    colStart = Math.max(0, minC - 1);
                    shownRows = Math.min(g.rows() - 1, maxR + 1) - rowStart + 1;
                    shownCols = Math.min(g.columns() - 1, maxC + 1) - colStart + 1;
                }
                int[][] cells = new int[g.rows()][g.columns()];
                int pad = (g.cellSize() - 16) / 2;
                for (int r = 0; r < g.rows(); r++) {
                    for (int c = 0; c < g.columns(); c++) {
                        cells[r][c] = -1;
                        if (!on[r][c]) {
                            continue;
                        }
                        String cellId = id + "[" + (r + 1) + "," + (c + 1) + "]";
                        SlotDef def = new SlotDef(slotList.size(), e, cellId, g.slotType(), g.itemCondition(), g.maxStack(),
                            g.item().orElse(null), (c - colStart) * g.cellSize() + pad, (r - rowStart) * g.cellSize() + pad,
                            g.background(), r + 1, c + 1);
                        cells[r][c] = def.index();
                        slotIds.put(cellId, def.index());
                        slotList.add(def);
                    }
                }
                Grid grid = new Grid(id, e, g.rows(), g.columns(), cells, rowStart, colStart, shownRows, shownCols);
                grids.put(id, grid);
                gridsByElement.put(e, grid);
            } else if (el instanceof GuiElement.Slider s) {
                interactive = true;
                if (sliders.containsKey(s.id())) {
                    throw new IllegalArgumentException("duplicate slider id '" + s.id() + "'");
                }
                sliders.put(s.id(), e);
            } else if (el instanceof GuiElement.Button) {
                interactive = true;
            } else if (el instanceof GuiElement.Image img) {
                progress |= img.progress().isPresent();
            } else if (el instanceof GuiElement.EntityView ev) {
                serverEntities |= ev.resolvedOnServer();
            }
            count[e] = slotList.size() - first[e];
        }
        this.slots = List.copyOf(slotList);
        this.slotById = Collections.unmodifiableMap(slotIds);
        this.gridById = Collections.unmodifiableMap(grids);
        this.gridByElement = Collections.unmodifiableMap(gridsByElement);
        this.sliderById = Collections.unmodifiableMap(sliders);
        this.firstSlot = first;
        this.slotCount = count;
        this.hasInteractive = interactive;
        this.hasProgress = progress;
        this.hasServerEntities = serverEntities;
        this.hasConditions = conditions;
    }

    public List<GuiElement> elements() {
        return elements;
    }

    public int elementCount() {
        return elements.size();
    }

    @Nullable
    public GuiElement element(int index) {
        return index >= 0 && index < elements.size() ? elements.get(index) : null;
    }

    public List<SlotDef> slots() {
        return slots;
    }

    public int slotCount() {
        return slots.size();
    }

    public int firstSlotOf(int element) {
        return firstSlot[element];
    }

    public int slotCountOf(int element) {
        return slotCount[element];
    }

    @Nullable
    public Grid gridOfElement(int element) {
        return gridByElement.get(element);
    }

    @Nullable
    public Grid grid(String id) {
        return gridById.get(id);
    }

    /** Slot indices for a slot id or a grid id (all its cells, row by row); empty if unknown. */
    public List<Integer> resolveSlots(String id) {
        Integer single = slotById.get(id);
        if (single != null) {
            return List.of(single);
        }
        Grid grid = gridById.get(id);
        return grid == null ? List.of() : grid.slotIndices();
    }

    /** Element index of the slider with this id, or -1. */
    public int sliderElement(String id) {
        Integer e = sliderById.get(id);
        return e == null ? -1 : e;
    }

    public Map<String, Integer> sliders() {
        return sliderById;
    }

    public boolean hasInteractive() {
        return hasInteractive;
    }

    public boolean hasProgress() {
        return hasProgress;
    }

    public boolean hasServerEntities() {
        return hasServerEntities;
    }

    public boolean hasConditions() {
        return hasConditions;
    }
}
