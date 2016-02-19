package spreadsheet;

import spreadsheet.api.CellLocation;
import spreadsheet.api.SpreadsheetInterface;
import spreadsheet.api.value.StringValue;
import spreadsheet.api.value.Value;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Spreadsheet implements SpreadsheetInterface {
    private Map<CellLocation, Cell> cellMap= new HashMap();
    private Set<Cell> cellSet = new HashSet<>();

    public void add(Cell cell){
        cellSet.add(cell);
    }

    public boolean isIn(Cell cell){
        return cellSet.contains(cell);
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
        for (Cell c : cellSet){

            c.setValue(new StringValue(c.getExpression()));
            cellSet.remove(c);

        }


    }
}
