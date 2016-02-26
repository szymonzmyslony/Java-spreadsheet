package spreadsheet;

import spreadsheet.api.CellLocation;
import spreadsheet.api.ExpressionUtils;
import spreadsheet.api.SpreadsheetInterface;
import spreadsheet.api.value.InvalidValue;
import spreadsheet.api.value.LoopValue;
import spreadsheet.api.value.Value;
import spreadsheet.api.value.ValueVisitor;

import java.util.*;

public class Spreadsheet implements SpreadsheetInterface {
    private Map<CellLocation, Cell> cellMap = new HashMap();
    private Set<Cell> toRecompute = new HashSet<>();

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
        Cell temp = cellMap.get(location);

        Cell cell = new Cell(location, this);
        cell.setExpression(expression);
        //  cell.setValue(new StringValue(expression));
        cellMap.put(location, cell);


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
            toRecompute.remove(c);
            if (c.getValue().equals(LoopValue.INSTANCE)) {

            } else if (dependsOnLoop(c)) {
                c.setValue(new InvalidValue(c.getExpression()));
            } else {
                // c.setValue(new StringValue(c.getExpression()));
            }
        }
    }

    private boolean dependsOnLoop(Cell cell) {
        Iterator<Cell> iterator = cell.getDependsOn().iterator();
        while (iterator.hasNext()) {
            Cell c = iterator.next();
            if (c.getValue().equals(LoopValue.INSTANCE) || c.getValue().equals(new InvalidValue(c.getExpression()))) {
                System.out.println(c.toString() + "depends on loop");
                return true;

            }
        }

        return false;
    }

    private void recomputeCell(Cell c) {
        Deque<Cell> quue = new ArrayDeque<>();
        checkLoops(c, new LinkedHashSet<Cell>());
        if (!(c.getValue().equals(LoopValue.INSTANCE) || dependsOnLoop(c))) {
            quue.add(c);

            while (!quue.isEmpty()) {
                Cell current = quue.getFirst();
                Iterator<Cell> iterator = current.getDependsOn().iterator();
                while (iterator.hasNext()) {
                    Cell cell = iterator.next();
                    if (isIn(cell)) {
                        quue.addFirst(cell);
                        recomputeCell(cell);
                        //quue.addLast(current);
                    }

                }
                quue.addLast(current);
                calculateCellValue(current);
                toRecompute.remove(current);
            }
        }
    }

    private void calculateCellValue(Cell cell) {
        Map<CellLocation, Double> values = new HashMap<>();
        Iterator<Cell> iterator = cell.getDependsOn().iterator();
        while (iterator.hasNext()) {
            Cell c = iterator.next();
            final boolean[] refbool = new boolean[1];
            final double[] ref = new double[1];
            refbool[0] = false;

            c.getValue().visit(new ValueVisitor() {
                @Override
                public void visitDouble(double value) {
                    refbool[0] = true;
                    ref[0] = value;

                }

                @Override
                public void visitLoop() {

                }

                @Override
                public void visitString(String expression) {

                }

                @Override
                public void visitInvalid(String expression) {


                }
            });
            if (refbool[0]) {
                values.put(c.getCellLocation(), ref[0]);

            }
        }


        ExpressionUtils.computeValue(cell.getExpression(), values);
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
        toRecompute.removeAll(cells);
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

