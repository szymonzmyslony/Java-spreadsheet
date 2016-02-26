package spreadsheet;

import spreadsheet.api.CellLocation;
import spreadsheet.api.ExpressionUtils;
import spreadsheet.api.observer.Observer;
import spreadsheet.api.value.InvalidValue;
import spreadsheet.api.value.Value;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Szymon on 17.02.2016.
 */
public class Cell implements Observer<Cell> {
    private CellLocation cellLocation;
    private Spreadsheet spreadsheet;
    private String expression = "";
    private Value value = null;
    private Set<Cell> dependsOn = new HashSet();
    private Set<Observer<Cell>> observers = new HashSet<>();

    public Cell(CellLocation cellLocation, Spreadsheet spreadsheet) {
        this.cellLocation = cellLocation;
        this.spreadsheet = spreadsheet;

    }

    public Set<Cell> getDependsOn() {
        return dependsOn;
    }

    public CellLocation getCellLocation() {
        return cellLocation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Cell cell = (Cell) o;

        if (cellLocation != null ? !cellLocation.equals(cell.cellLocation) : cell.cellLocation != null) return false;
        return spreadsheet != null ? spreadsheet.equals(cell.spreadsheet) : cell.spreadsheet == null;

    }

    @Override
    public int hashCode() {
        int result = cellLocation != null ? cellLocation.hashCode() : 0;
        result = 31 * result + (spreadsheet != null ? spreadsheet.hashCode() : 0);
        return result;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        for (Cell c : dependsOn) {
            c.removeObserver(this);
            dependsOn.remove(c);
        }
        this.expression = expression;
        setValue(new InvalidValue(expression));
        spreadsheet.add(this);
        Set<CellLocation> newLocations = ExpressionUtils.getReferencedLocations(expression);
        for (CellLocation location : newLocations) {
            Cell cell;
            if (spreadsheet.isInMap(location)) {
                dependsOn.add(spreadsheet.getCellMap().get(location));
            } else {
                cell = new Cell(location, spreadsheet);
                cell.observers.add(this);
                spreadsheet.addtoMap(location, cell);
                dependsOn.add(cell);
            }
        }
        for (Observer<Cell> cell : observers) {
            cell.update(this);
        }


    }

   private void removeObserver(Observer<Cell> observer){
       observers.remove(observer);
   }


    @Override
    public void update(Cell changed) {
        if (!spreadsheet.isIn(changed)) {
            spreadsheet.add(this);
            this.setValue(new InvalidValue(this.expression));
            for (Observer<Cell> cell : observers) {
                cell.update(this);
            }

        }



    }
}
