package spreadsheet;

import spreadsheet.api.CellLocation;
import spreadsheet.api.ExpressionUtils;
import spreadsheet.api.SpreadsheetInterface;
import spreadsheet.api.value.LoopValue;
import spreadsheet.api.value.Value;
import spreadsheet.api.value.ValueVisitor;

import java.util.*;

public class Spreadsheet implements SpreadsheetInterface {
    private Map<CellLocation, Cell> cellMap = new HashMap();
    private Set<Cell> toRecompute = new HashSet<>();
    private Set<Cell> toBeIgnored = new HashSet<>();


    public void add(Cell cell) {
        toRecompute.add(cell);
    }


    public void addtoMap(CellLocation l, Cell cell) {
        cellMap.put(l, cell);
    }

    public Map<CellLocation, Cell> getCellMap() {
        return cellMap;
    }

    public boolean isIn(Cell cell) {
        return toRecompute.contains(cell);
    }

    public boolean isInMap(CellLocation l) {
        return cellMap.containsKey(l);
    }


    @Override
    public String getExpression(CellLocation location) {
        Cell temp = cellMap.get(location);
        return temp == null ? null : temp.getExpression();
    }


    @Override
    public void setExpression(CellLocation location, String expression) {
        Cell cell = getCell(location);
        cell.setExpression(expression);


    }

    public Cell getCell(CellLocation location) {
        if (!cellMap.containsKey(location)) {
            cellMap.put(location, new Cell(location, this));
        }
        return cellMap.get(location);
    }

    @Override
    public Value getValue(CellLocation location) {
        Cell temp = cellMap.get(location);
        return temp == null ? null : temp.getValue();
    }

    @Override
    public void recompute() {
        Iterator<Cell> iterator = toRecompute.iterator();
        while (iterator.hasNext()) {
            Cell c = iterator.next();
            recomputeCell(c);
            if (!toBeIgnored.contains(c)) {
                recomputeCell(c);
            }
            iterator.remove();
        }
        toBeIgnored.clear();
    }

   /* private boolean dependsOnLoop(Cell cell) {
        Iterator<Cell> iterator = cell.getDependsOn().iterator();
        while (iterator.hasNext()) {
            Cell c = iterator.next();
            if (c.getValue().equals(LoopValue.INSTANCE)|| c.getValue().equals(new InvalidValue(c.getExpression()))) {
                System.out.println(c.toString() + "depends on loop");
                return true;

            }
        }

        return false
    }*/

    private void recomputeCell(Cell c) {
        Deque<Cell> quue = new ArrayDeque<>();
        checkLoops(c, new LinkedHashSet<Cell>());
        if (!toBeIgnored.contains(c)) {
            quue.add(c);
            while (!quue.isEmpty()) {
                Cell current = quue.pollFirst();
                boolean flag = false;
                Iterator<Cell> iterator = current.getDependsOn().iterator();
                while (iterator.hasNext()) {
                    Cell cell = iterator.next();
                    if (isIn(cell) && !(toBeIgnored.contains(cell))) {
                        quue.addFirst(cell);
                        flag = true;
                    }

                }
                if (flag) {
                    quue.addLast(current);
                } else {
                    calculateCellValue(current);
                    toBeIgnored.add(current);
                }
                // toRecompute.remove(current);
            }
        }
    }

    private void calculateCellValue(final Cell cell) {
        final Map<CellLocation, Double> values = new HashMap<>();
        Iterator<Cell> iterator = cell.getDependsOn().iterator();
        while (iterator.hasNext()) {
            final Cell c = iterator.next();


            c.getValue().visit(new ValueVisitor() {
                @Override
                public void visitDouble(double value) {
                    values.put(c.getCellLocation(), value);

                }

                @Override
                public void visitLoop() {

                }

                @Override
                public void visitString(String expression) {

                }

                @Override
                public void visitInvalid(String expression) {
                    //  cell.setValue(new InvalidValue(cell.getExpression()));


                }
            });
        }
        Value newvalue = ExpressionUtils.computeValue(cell.getExpression(), values);
        cell.setValue(newvalue);
    }


    private void checkLoops(Cell c, LinkedHashSet<Cell> cellSeen) {
        if (cellSeen.contains(c)) {
            markAsLoop(c, cellSeen);
        } else {
            cellSeen.add(c);
            for (Cell cell : c.getDependsOn()) {
                checkLoops(cell, cellSeen);
            }
            cellSeen.remove(c);
        }
    }


    private void markAsLoop(Cell startCell, LinkedHashSet<Cell> cells) {
        toBeIgnored.addAll(cells);
        boolean flag = false;
        Iterator<Cell> iterator = cells.iterator();
        while (iterator.hasNext()) {
            Cell cell = iterator.next();
            if (cell.equals(startCell)) {
                flag = true;
            }
            if (flag) {
                cell.setValue(LoopValue.INSTANCE);
            }

        }


    }


}

