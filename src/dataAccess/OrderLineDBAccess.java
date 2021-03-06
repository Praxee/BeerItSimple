package dataAccess;

import exception.ConnectionException;
import exception.DataQueryException;
import model.OrderLine;
import model.Product;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class OrderLineDBAccess implements OrderLineDataAccess {
    Connection connection;

    public OrderLineDBAccess() throws ConnectionException {
        connection = DBConnection.getInstance();
    }

    @Override
    public ArrayList<OrderLine> getAllOrderLineBetweenDates(LocalDate startDate, LocalDate endDate) throws DataQueryException {

        String sqlInstructions = "SELECT ol.* FROM OrderLine ol\n" +
                                 "JOIN `order` o ON o.reference = ol.Orderreference\n" +
                                 "WHERE ? <= o.startingDate AND ? >= o.startingDate";

        ArrayList<OrderLine> orderLinesList = new ArrayList<>();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sqlInstructions);
            Date start = Date.valueOf(startDate);
            Date end = Date.valueOf(endDate);
            preparedStatement.setDate(1, start);
            preparedStatement.setDate(2, end);

            ResultSet data = preparedStatement.executeQuery();


            OrderLine ol;
            while (data.next()) {

                ol = new OrderLine(
                        new Product(data.getInt("ol.Productcode")),
                        null,
                        data.getInt("ol.quantity"),
                        data.getDouble("ol.salesUnitPrice")
                );
                orderLinesList.add(ol);
            }

        } catch (SQLException e) {
            throw new DataQueryException();
        }

        return orderLinesList;
    }
}
