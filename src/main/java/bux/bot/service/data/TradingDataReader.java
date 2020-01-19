package bux.bot.service.data;

import bux.bot.model.general.TradingData;
import bux.bot.model.general.TradingProduct;

import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Collectors;

public class TradingDataReader {
    public static TradingData readFromConsole() {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Please input product id you would like trading bux.bot to trade: ");

            TradingProduct tradingProduct;
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

            System.out.print("Please input buy price to open position: ");
            float buyPrice;
            while (true) {
                try {
                    buyPrice = Float.valueOf(scanner.nextLine());
                    if (buyPrice <= 0) {
                        System.out.print("Trading product price should be positive, try again: ");
                    } else {
                        break;
                    }
                } catch (NumberFormatException nfe) {
                    System.out.print("Wrong number format, try again: ");
                }
            }

            System.out.print("Please input sell price to close position with profit: ");
            float sellPrice;
            while (true) {
                try {
                    sellPrice = Float.valueOf(scanner.nextLine());
                    if (sellPrice <= 0) {
                        System.out.print("Trading product price should be positive, try again: ");
                    } else if (sellPrice <= buyPrice) {
                        System.out.print("Sell price should be more than buy price, try again: ");
                    } else {
                        break;
                    }
                } catch (NumberFormatException nfe) {
                    System.out.print("Wrong number format, try again: ");
                }
            }

            System.out.print("Please input sell price to close position with loss in a case price moves to a wrong direction: ");
            float stopLossPrice;
            while (true) {
                try {
                    stopLossPrice = Float.valueOf(scanner.nextLine());
                    if (stopLossPrice <= 0) {
                        System.out.print("Trading product price should be positive, try again: ");
                    } else if (stopLossPrice >= buyPrice) {
                        System.out.print("Stop loss price should be less than buy price, try again: ");
                    } else {
                        break;
                    }
                } catch (NumberFormatException nfe) {
                    System.out.print("Wrong number format, try again: ");
                }
            }

            return TradingData
                    .builder()
                    .tradingProduct(tradingProduct)
                    .openPrice(buyPrice)
                    .closePrice(sellPrice)
                    .stopLossPrice(stopLossPrice)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return TradingData.builder().build();
    }
}
