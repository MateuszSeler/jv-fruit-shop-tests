package core.basesyntax.service;

import core.basesyntax.db.StockDao;
import core.basesyntax.service.operations.Balance;
import core.basesyntax.service.operations.Operation;
import core.basesyntax.service.operations.Purchase;
import core.basesyntax.service.operations.Return;
import core.basesyntax.service.operations.Supply;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class InventoryFromCsv implements Inventory {
    private final StockDao stockDao;
    private final String fileName;

    public InventoryFromCsv(StockDao stockDao, String fileName) {
        this.stockDao = stockDao;
        this.fileName = fileName;
    }

    @Override
    public void synchronizeWithTheStorage() {
        Map<String, Operation> operationMap = new HashMap<>();
        operationMap.put("b", new Balance(stockDao));
        operationMap.put("s", new Supply(stockDao));
        operationMap.put("r", new Return(stockDao));
        operationMap.put("p", new Purchase(stockDao));

        OperationStrategy operationStrategy = new OperationStrategyImpl(operationMap);

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line = reader.readLine();
            while (line != null) {
                String[] splitDataFromCurrentLine = line.split(",");
                if (splitDataFromCurrentLine.length != 3) {
                    throw new InvalidDataException("wrong data format");
                }
                try {
                    Integer.parseInt(splitDataFromCurrentLine[2]);
                } catch (NumberFormatException e) {
                    throw new InvalidDataException("amount is not a number");
                }
                operationStrategy.getOperation(splitDataFromCurrentLine[0])
                        .update(splitDataFromCurrentLine[1],
                                Integer.parseInt(splitDataFromCurrentLine[2]));
                line = reader.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Cant read from file", e);
        }
    }
}