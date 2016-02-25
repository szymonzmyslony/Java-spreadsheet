package spreadsheet;

import spreadsheet.api.CellLocation;
import spreadsheet.api.SpreadsheetInterface;
import spreadsheet.api.value.InvalidValue;
import spreadsheet.api.value.LoopValue;
import spreadsheet.api.value.StringValue;
import spreadsheet.api.value.Value;

import java.util.*;

public class Spreadsheet implements SpreadsheetInterface {
    private Map<CellLocation, Cell> cellMap= new HashMap();
    private Set<Cell> toRecompute = new HashSet<>();

    public void add(Cell cell){
        toRecompute.add(cell);
    }


    public void addtoMap(CellLocation l, Cell cell) {
        cellMap.put(l, cell);
    }

    public Map<CellLocation, Cell> getCellMap() {
        return cellMap;
    }

    public boolean isIn(Cell cell){
        return toRecompute.contains(cell);
    }

    public boolean isInMap(CellLocation l) {
        return cellMap.containsKey(l);
    }




   

    @Override
    public String getExpression(CellLocation location) {
        Cell temp = cellMap.get(location);
        return temp==null?null:temp.getExpression();
            }


    @Override
    public void setExpression(CellLocation location, String expression) {
        Cell temp = cellMap.get(location);

            Cell cell = new Cell(location, this);
            cell.setExpression(expression);
            cell.setValue(new StringValue(expression));
            cellMap.put(location, cell);


    }

    @Override
    public Value getValue(CellLocation location) {
        Cell temp = cellMap.get(location);
        return temp==null?null:temp.getValue();
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
                c.setValue(new StringValue(c.getExpression()));
            }
        }
    }

    private boolean dependsOnLoop(Cell cell) {
        for (Cell c : cell.getDependsOn()) {
            if (c.getValue().equals(LoopValue.INSTANCE)) {
                return true;
            }
        }

        return false;
    }

    private void recomputeCell(Cell c) {
        checkLoops(c, new LinkedHashSet<Cell>());


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

