package bux.bot.service.data;

import bux.bot.model.shared.TradingData;
import bux.bot.model.shared.TradingProduct;
import com.savoirtech.logging.slf4j.json.LoggerFactory;
import com.savoirtech.logging.slf4j.json.logger.Logger;
import javafx.util.converter.BigDecimalStringConverter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Collectors;

public class TradingDataReader {
    private static Logger logger = LoggerFactory.getLogger(TradingDataReader.class);

    public static TradingData readFromConsole() {
        try (Scanner scanner = new Scanner(System.in)) {
            TradingProduct tradingProduct = getTradingProduct(scanner);
            BigDecimal buyPrice = getBuyPrice(scanner, tradingProduct);
            BigDecimal sellPrice = getSellPrice(scanner, buyPrice, tradingProduct);
            BigDecimal stopLossPrice = getStopLossPrice(scanner, buyPrice, tradingProduct);

            return TradingData
                    .builder()
                    .tradingProduct(tradingProduct)
                    .openPrice(buyPrice)
                    .closePrice(sellPrice)
                    .stopLossPrice(stopLossPrice)
                    .build();
        } catch (Exception e) {
            System.out.println("An error during input occurred.");
            logger
                    .error()
                    .exception("An error during input occurred.", e)
                    .log();
        }

        return TradingData.builder().build();
    }

    private static TradingProduct getTradingProduct(Scanner scanner) {
        TradingProduct tradingProduct;
        System.out.print("Please input product id you would like bux-bot to trade: ");

        while ((tradingProduct = TradingProduct.getById(scanner.nextLine())).equals(TradingProduct.UNKNOWN)) {
            System.out.print(String.format(
                    "Trading product with this id doesn't exist, fetch one of [%s]: ",
                    Arrays
                            .stream(TradingProduct.values())
                            .filter(p -> !p.equals(TradingProduct.UNKNOWN))
                            .map(TradingProduct::getId)
                            .collect(Collectors.joining(", "))
            ));
        }
        return tradingProduct;
    }

    private static BigDecimal getBuyPrice(Scanner scanner, TradingProduct tradingProduct) {
        System.out.print("Please input buy price to open position: ");

        BigDecimal buyPrice = null;
        boolean isDataValid = false;

        while (!isDataValid) {
            try {
                buyPrice = new BigDecimalStringConverter().fromString(scanner.nextLine());

                if (buyPrice.compareTo(BigDecimal.valueOf(0)) <= 0) {
                    System.out.print("Trading product price should be positive, try again: ");
                    continue;
                }

                if (buyPrice.scale() > tradingProduct.getDecimals()) {
                    System.out.print(String.format(
                            "Buy price precision should not be more than %s for this product, try again: ",
                            tradingProduct.getDecimals()
                    ));
                    continue;
                }

                isDataValid = true;
            } catch (NumberFormatException nfe) {
                System.out.print("Wrong number format, try again: ");
            }
        }

        return buyPrice;
    }

    private static BigDecimal getSellPrice(Scanner scanner, BigDecimal buyPrice, TradingProduct tradingProduct) {
        System.out.print("Please input sell price to close position with profit: ");

        BigDecimal sellPrice = null;
        boolean isDataValid = false;

        while (!isDataValid) {
            try {
                sellPrice = new BigDecimalStringConverter().fromString(scanner.nextLine());

                if (sellPrice.compareTo(BigDecimal.valueOf(0)) <= 0) {
                    System.out.print("Sell price should be positive, try again: ");
                    continue;
                }

                if (sellPrice.compareTo(buyPrice) <= 0) {
                    System.out.print("Sell price should be more than buy price, try again: ");
                    continue;
                }

                if (sellPrice.scale() > tradingProduct.getDecimals()) {
                    System.out.print(String.format(
                            "Sell price precision should not be more than %s for this product, try again: ",
                            tradingProduct.getDecimals()
                    ));
                    continue;
                }

                isDataValid = true;
            } catch (NumberFormatException nfe) {
                System.out.print("Wrong number format, try again: ");
            }
        }

        return sellPrice;
    }

    private static BigDecimal getStopLossPrice(Scanner scanner, BigDecimal buyPrice, TradingProduct tradingProduct) {
        System.out.print("Please input sell price to close position with loss in a case price moves to a wrong direction: ");

        BigDecimal stopLossPrice = null;
        boolean isDataValid = false;

        while (!isDataValid) {
            try {
                stopLossPrice = new BigDecimalStringConverter().fromString(scanner.nextLine());

                if (stopLossPrice.compareTo(BigDecimal.valueOf(0)) <= 0) {
                    System.out.print("Buy price should be positive, try again: ");
                    continue;
                }

                if (stopLossPrice.compareTo(buyPrice) >= 0) {
                    System.out.print("Stop loss price should be less than buy price, try again: ");
                    continue;
                }

                if (stopLossPrice.scale() > tradingProduct.getDecimals()) {
                    System.out.print(String.format(
                            "Stop loss price precision should not be more than %s for this product, try again: ",
                            tradingProduct.getDecimals()
                    ));
                    continue;
                }

                isDataValid = true;
            } catch (NumberFormatException nfe) {
                System.out.print("Wrong number format, try again: ");
            }
        }

        return stopLossPrice;
    }
}
